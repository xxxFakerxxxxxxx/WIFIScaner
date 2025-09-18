package com.example.wifi;

import java.util.ArrayList;
import java.util.List;

public class WifiNetwork {
    private final String ssid;
    private final String bssid;
    private final int signalStrength;
    private final int frequency;
    private final int channel;
    private final String security;
    private final String mode;

    public WifiNetwork(String ssid, String bssid, int signalStrength, int frequency, 
                      int channel, String security, String mode) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.signalStrength = signalStrength;
        this.frequency = frequency;
        this.channel = channel;
        this.security = security;
        this.mode = mode;
    }

    public static List<WifiNetwork> fromJson(String json) {
        // Упрощенная реализация без Gson
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("SSID: %s | BSSID: %s | Signal: %d%% | Channel: %d | Security: %s | Mode: %s",
            ssid, bssid, signalStrength, channel, security, mode);
    }

    // Getters
    public String getSsid() { return ssid; }
    public String getBssid() { return bssid; }
    public int getSignalStrength() { return signalStrength; }
    public int getFrequency() { return frequency; }
    public int getChannel() { return channel; }
    public String getSecurity() { return security; }
    public String getMode() { return mode; }
}