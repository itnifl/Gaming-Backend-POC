package com.example.network.packets;

/**
 * Server -> Client: Notification that a player joined.
 */
public class PlayerJoined {
    
    /** The new player's ID */
    public int playerId;
    
    /** The new player's name */
    public String playerName;
    
    /** Initial X position */
    public float x;
    
    /** Initial Y position */
    public float y;
    
    /** Required for Kryo serialization */
    public PlayerJoined() {
    }
    
    public PlayerJoined(int playerId, String playerName, float x, float y) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "PlayerJoined{playerId=" + playerId + ", name='" + playerName + "'}";
    }
}
