package com.example.network;

/**
 * Network configuration constants.
 * Centralized configuration for both client and server.
 */
public final class NetworkConfig {
    
    private NetworkConfig() {
        // Prevent instantiation
    }
    
    // Connection settings
    public static final int TCP_PORT = 27960;
    public static final int UDP_PORT = 27961;
    public static final String DEFAULT_HOST = "localhost";
    
    // Timeouts (in milliseconds)
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int KEEP_ALIVE_INTERVAL = 1000;
    
    // Buffer sizes
    public static final int WRITE_BUFFER_SIZE = 16384;
    public static final int OBJECT_BUFFER_SIZE = 4096;
    
    // Game settings
    public static final int MAX_PLAYERS = 100;
    public static final float POSITION_UPDATE_RATE = 1f / 20f; // 20 updates per second
}
