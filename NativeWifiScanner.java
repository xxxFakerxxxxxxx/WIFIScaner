package com.example.wifi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class NativeWifiScanner {

    public interface WifiScannerLib extends Library {
        WifiScannerLib INSTANCE = loadLibrary();

        Pointer scan_wifi_networks();
        int get_networks_count(Pointer networks);
        void free_networks(Pointer networks);
        String get_networks_json();
        void free_string(Pointer ptr);

        static WifiScannerLib loadLibrary() {
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();
            String libName = "";
            String libExtension = "";

            if (os.contains("win")) {
                libName = "wifi_scanner";
                libExtension = ".dll";
            } else if (os.contains("mac")) {
                libName = "libwifi_scanner";
                libExtension = ".dylib";
            } else if (os.contains("nix") || os.contains("nux")) {
                libName = "libwifi_scanner";
                libExtension = ".so";
            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + os);
            }

            // Try to load from system library path first
            try {
                return Native.load(libName, WifiScannerLib.class);
            } catch (UnsatisfiedLinkError e) {
                // Fallback to loading from resources
                try {
                    String resourcePath = "/native/" + 
                                         (os.contains("win") ? "windows" : 
                                          os.contains("mac") ? "macos" : "linux") + 
                                         "/" + arch + "/" + libName + libExtension;
                    
                    InputStream in = NativeWifiScanner.class.getResourceAsStream(resourcePath);
                    if (in == null) {
                        throw new RuntimeException("Native library not found in resources: " + resourcePath);
                    }
                    
                    Path tempDir = Files.createTempDirectory("native-libs");
                    tempDir.toFile().deleteOnExit();
                    Path tempLib = tempDir.resolve(libName + libExtension);
                    Files.copy(in, tempLib, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Set executable permission on Unix-like systems
                    if (!Platform.isWindows()) {
                        tempLib.toFile().setExecutable(true);
                    }
                    
                    return Native.load(tempLib.toAbsolutePath().toString(), WifiScannerLib.class);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to extract native library", ex);
                }
            }
        }
    }

    public static List<WifiNetwork> scanNetworks() {
        try {
            // Try JSON API first
            String json = WifiScannerLib.INSTANCE.get_networks_json();
            if (json != null && !json.isEmpty()) {
                return WifiNetwork.fromJson(json);
            }
        } catch (Exception e) {
            System.err.println("JSON API failed, falling back to raw API: " + e.getMessage());
        }

        // Fallback to raw API
        return scanNetworksRaw();
    }

    private static List<WifiNetwork> scanNetworksRaw() {
        Pointer networksPtr = WifiScannerLib.INSTANCE.scan_wifi_networks();
        if (networksPtr == null) {
            return new ArrayList<>();
        }

        List<WifiNetwork> networks = new ArrayList<>();
        try {
            int count = WifiScannerLib.INSTANCE.get_networks_count(networksPtr);
            
            // Calculate structure size based on platform
            int structSize = Platform.is64Bit() ? 56 : 28;
            
            for (int i = 0; i < count; i++) {
                Pointer networkPtr = networksPtr.share(i * structSize);
                
                String ssid = networkPtr.getPointer(0).getString(0);
                String bssid = networkPtr.getPointer(Platform.is64Bit() ? 8 : 4).getString(0);
                int signalStrength = networkPtr.getInt(Platform.is64Bit() ? 16 : 8);
                int frequency = networkPtr.getInt(Platform.is64Bit() ? 20 : 12);
                int channel = networkPtr.getInt(Platform.is64Bit() ? 24 : 16);
                String security = networkPtr.getPointer(Platform.is64Bit() ? 32 : 20).getString(0);
                String mode = networkPtr.getPointer(Platform.is64Bit() ? 40 : 24).getString(0);
                
                networks.add(new WifiNetwork(
                    ssid != null ? ssid : "Unknown",
                    bssid != null ? bssid : "00:00:00:00:00:00",
                    signalStrength,
                    frequency,
                    channel,
                    security != null ? security : "UNKNOWN",
                    mode != null ? mode : "UNKNOWN"
                ));
            }
        } finally {
            WifiScannerLib.INSTANCE.free_networks(networksPtr);
        }
        
        return networks;
    }

    public static void main(String[] args) {
        try {
            System.out.println("Scanning Wi-Fi networks...");
            List<WifiNetwork> networks = scanNetworks();
            
            System.out.println("Found " + networks.size() + " networks:");
            for (WifiNetwork network : networks) {
                System.out.println(network);
            }
        } catch (Exception e) {
            System.err.println("Error scanning networks: " + e.getMessage());
            e.printStackTrace();
        }
    }
}