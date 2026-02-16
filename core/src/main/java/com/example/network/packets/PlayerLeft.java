package com.example.network.packets;

/**
 * Server -> Client: Notification that a player left.
 */
public class PlayerLeft {
    
    /** The player's ID who left */
    public int playerId;
    
    /** The player's name who left */
    public String playerName;
    
    /** Reason for leaving (optional) */
    public String reason;
    
    /** Required for Kryo serialization */
    public PlayerLeft() {
    }
    
    public PlayerLeft(int playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
    
    public PlayerLeft(int playerId, String playerName, String reason) {
        this(playerId, playerName);
        this.reason = reason;
    }
    
    @Override
    public String toString() {
        return "PlayerLeft{playerId=" + playerId + ", name='" + playerName + "'}";
    }
}
