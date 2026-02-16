package com.example.network.packets;

/**
 * Client -> Server: Request to log in to the server.
 */
public class LoginRequest {
    
    /** Player's display name */
    public String playerName;
    
    /** Client version for compatibility checking */
    public String clientVersion;
    
    /** Required for Kryo serialization */
    public LoginRequest() {
    }
    
    public LoginRequest(String playerName) {
        this.playerName = playerName;
        this.clientVersion = "1.0.0";
    }
    
    public LoginRequest(String playerName, String clientVersion) {
        this.playerName = playerName;
        this.clientVersion = clientVersion;
    }
    
    @Override
    public String toString() {
        return "LoginRequest{playerName='" + playerName + "', version='" + clientVersion + "'}";
    }
}
