package com.example.network.packets;

import com.esotericsoftware.kryo.Kryo;

/**
 * Packet registry for Kryo serialization.
 * All packet classes must be registered here in the same order on both client and server.
 * 
 * IMPORTANT: Registration order matters! Always add new classes at the end.
 */
public final class PacketRegistry {
    
    private PacketRegistry() {
        // Prevent instantiation
    }
    
    /**
     * Register all packet classes with Kryo.
     * Must be called on both client and server before any network operations.
     * @param kryo The Kryo instance to register classes with
     */
    public static void register(Kryo kryo) {
        // Register primitive arrays (needed for some packets)
        kryo.register(byte[].class);
        kryo.register(int[].class);
        kryo.register(float[].class);
        kryo.register(String[].class);
        
        // Register packet classes - MAINTAIN THIS ORDER!
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
        kryo.register(PlayerPosition.class);
        kryo.register(ChatMessage.class);
        kryo.register(ChatMessage.MessageType.class);
        kryo.register(PlayerJoined.class);
        kryo.register(PlayerLeft.class);
        kryo.register(PlayerList.class);
        kryo.register(PlayerList.PlayerEntry.class);
        kryo.register(Ping.class);
        kryo.register(Pong.class);
        
        // Register common Java classes that might be used
        kryo.register(java.util.ArrayList.class);
        kryo.register(java.util.HashMap.class);
    }
}
