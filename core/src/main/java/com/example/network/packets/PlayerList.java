package com.example.network.packets;

import java.util.ArrayList;
import java.util.List;

/**
 * Server -> Client: List of all currently connected players.
 * Sent after successful login to sync the client with existing players.
 */
public class PlayerList {
    
    /** List of player entries */
    public List<PlayerEntry> players;
    
    /** Required for Kryo serialization */
    public PlayerList() {
        this.players = new ArrayList<>();
    }
    
    public void addPlayer(int playerId, String playerName, float x, float y) {
        players.add(new PlayerEntry(playerId, playerName, x, y));
    }
    
    /**
     * Individual player entry in the list.
     */
    public static class PlayerEntry {
        public int playerId;
        public String playerName;
        public float x;
        public float y;
        
        /** Required for Kryo serialization */
        public PlayerEntry() {
        }
        
        public PlayerEntry(int playerId, String playerName, float x, float y) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.x = x;
            this.y = y;
        }
    }
    
    @Override
    public String toString() {
        return "PlayerList{count=" + players.size() + "}";
    }
}
