package com.example.network.packets;

/**
 * Bidirectional: Player position update.
 * Client -> Server: Report local player position.
 * Server -> Client: Broadcast other players' positions.
 * 
 * Typically sent via UDP for better performance.
 */
public class PlayerPosition {
    
    /** Player's connection ID */
    public int playerId;
    
    /** X coordinate */
    public float x;
    
    /** Y coordinate */
    public float y;
    
    /** Velocity X (optional, for interpolation) */
    public float vx;
    
    /** Velocity Y (optional, for interpolation) */
    public float vy;
    
    /** Timestamp for lag compensation */
    public long timestamp;
    
    /** Required for Kryo serialization */
    public PlayerPosition() {
    }
    
    public PlayerPosition(int playerId, float x, float y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.timestamp = System.currentTimeMillis();
    }
    
    public PlayerPosition(int playerId, float x, float y, float vx, float vy) {
        this(playerId, x, y);
        this.vx = vx;
        this.vy = vy;
    }
    
    @Override
    public String toString() {
        return "PlayerPosition{playerId=" + playerId + ", x=" + x + ", y=" + y + "}";
    }
}
