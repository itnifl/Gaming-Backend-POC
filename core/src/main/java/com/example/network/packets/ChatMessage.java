package com.example.network.packets;

/**
 * Bidirectional: Chat message.
 * Client -> Server: Send a chat message.
 * Server -> Client: Broadcast chat messages to all players.
 */
public class ChatMessage {
    
    /** Sender's player ID (-1 for system messages) */
    public int senderId;
    
    /** Sender's display name */
    public String senderName;
    
    /** The message content */
    public String message;
    
    /** Timestamp when the message was sent */
    public long timestamp;
    
    /** Message type for different styling */
    public MessageType type;
    
    public enum MessageType {
        NORMAL,
        SYSTEM,
        WHISPER,
        EMOTE
    }
    
    /** Required for Kryo serialization */
    public ChatMessage() {
        this.type = MessageType.NORMAL;
    }
    
    /**
     * Create a player chat message.
     */
    public static ChatMessage player(int senderId, String senderName, String message) {
        ChatMessage chat = new ChatMessage();
        chat.senderId = senderId;
        chat.senderName = senderName;
        chat.message = message;
        chat.timestamp = System.currentTimeMillis();
        chat.type = MessageType.NORMAL;
        return chat;
    }
    
    /**
     * Create a system message.
     */
    public static ChatMessage system(String message) {
        ChatMessage chat = new ChatMessage();
        chat.senderId = -1;
        chat.senderName = "System";
        chat.message = message;
        chat.timestamp = System.currentTimeMillis();
        chat.type = MessageType.SYSTEM;
        return chat;
    }
    
    @Override
    public String toString() {
        return "[" + senderName + "]: " + message;
    }
}
