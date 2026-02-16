package com.example.network;

/**
 * Client-side network event listener interface.
 * Implement this to handle network events in your game.
 */
public interface NetworkListener {
    
    /**
     * Called when successfully connected to the server.
     */
    void onConnected();
    
    /**
     * Called when disconnected from the server.
     */
    void onDisconnected();
    
    /**
     * Called when a packet is received from the server.
     * @param packet The received packet object
     */
    void onReceived(Object packet);
    
    /**
     * Adapter class with default empty implementations.
     * Extend this if you only need to handle specific events.
     */
    class Adapter implements NetworkListener {
        @Override
        public void onConnected() {}
        
        @Override
        public void onDisconnected() {}
        
        @Override
        public void onReceived(Object packet) {}
    }
}
