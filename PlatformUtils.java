package com.example.wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PlatformUtils {
    
    public static boolean isAdmin() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return checkWindowsAdmin();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return checkUnixAdmin();
        }
        
        return false;
    }
    
    private static boolean checkWindowsAdmin() {
        try {
            Process process = Runtime.getRuntime().exec("net session");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
    
    private static boolean checkUnixAdmin() {
        try {
            Process process = Runtime.getRuntime().exec("id -u");
            process.waitFor();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            
            return output != null && output.trim().equals("0");
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
    
    public static void requestAdminRights() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            requestWindowsAdmin();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            requestUnixAdmin();
        }
    }
    
    private static void requestWindowsAdmin() {
        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + "/bin/java";
            String classpath = System.getProperty("java.class.path");
            String className = NativeWifiScanner.class.getCanonicalName();
            
            String command = "powershell Start-Process -Verb RunAs \"" + javaBin + 
                "\" -ArgumentList '-cp', '\"" + classpath + "\"', '" + className + "'";
            
            Runtime.getRuntime().exec(command);
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Failed to request admin rights: " + e.getMessage());
        }
    }
    
    private static void requestUnixAdmin() {
        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + "/bin/java";
            String classpath = System.getProperty("java.class.path");
            String className = NativeWifiScanner.class.getCanonicalName();
            
            String command = "sudo " + javaBin + " -cp \"" + classpath + "\" " + className;
            
            Runtime.getRuntime().exec(command);
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Failed to request admin rights: " + e.getMessage());
        }
    }
}