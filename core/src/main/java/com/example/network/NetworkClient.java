package com.example.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.example.network.packets.PacketRegistry;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Network client wrapper for KryoNet.
 * Handles connection management and message dispatching.
 */
public class NetworkClient {
    
    private final Client client;
    private final CopyOnWriteArrayList<NetworkListener> listeners;
    private volatile boolean connected;
    private String playerName;
    
    public NetworkClient() {
        this.client = new Client(NetworkConfig.WRITE_BUFFER_SIZE, NetworkConfig.OBJECT_BUFFER_SIZE);
        this.listeners = new CopyOnWriteArrayList<>();
        this.connected = false;
        
        // Register all packet classes
        PacketRegistry.register(client.getKryo());
        
        // Set up internal listener
        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                NetworkClient.this.connected = true;
                for (NetworkListener listener : listeners) {
                    listener.onConnected();
                }
            }
            
            @Override
            public void disconnected(Connection connection) {
                NetworkClient.this.connected = false;
                for (NetworkListener listener : listeners) {
                    listener.onDisconnected();
                }
            }
            
            @Override
            public void received(Connection connection, Object object) {
                for (NetworkListener listener : listeners) {
                    listener.onReceived(object);
                }
            }
        });
    }
    
    /**
     * Connect to a server.
     * @param host Server hostname or IP
     * @param tcpPort TCP port
     * @param udpPort UDP port (use -1 for TCP only)
     * @throws IOException if connection fails
     */
    public void connect(String host, int tcpPort, int udpPort) throws IOException {
        client.start();
        if (udpPort > 0) {
            client.connect(NetworkConfig.CONNECTION_TIMEOUT, host, tcpPort, udpPort);
        } else {
            client.connect(NetworkConfig.CONNECTION_TIMEOUT, host, tcpPort);
        }
    }
    
    /**
     * Connect using default configuration.
     * @throws IOException if connection fails
     */
    public void connect() throws IOException {
        connect(NetworkConfig.DEFAULT_HOST, NetworkConfig.TCP_PORT, NetworkConfig.UDP_PORT);
    }
    
    /**
     * Connect to a specific host using default ports.
     * @param host Server hostname or IP
     * @throws IOException if connection fails
     */
    public void connect(String host) throws IOException {
        connect(host, NetworkConfig.TCP_PORT, NetworkConfig.UDP_PORT);
    }
    
    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        client.close();
        connected = false;
    }
    
    /**
     * Send a packet via TCP (reliable, ordered).
     * @param packet The packet to send
     */
    public void sendTCP(Object packet) {
        if (connected) {
            client.sendTCP(packet);
        }
    }
    
    /**
     * Send a packet via UDP (fast, unreliable).
     * Best for frequent position updates.
     * @param packet The packet to send
     */
    public void sendUDP(Object packet) {
        if (connected) {
            client.sendUDP(packet);
        }
    }
    
    /**
     * Add a network listener.
     * @param listener The listener to add
     */
    public void addListener(NetworkListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a network listener.
     * @param listener The listener to remove
     */
    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Check if connected to a server.
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Get the underlying KryoNet client.
     * @return The KryoNet client instance
     */
    public Client getClient() {
        return client;
    }
    
    /**
     * Get the connection ID assigned by the server.
     * @return Connection ID, or -1 if not connected
     */
    public int getConnectionId() {
        return client.getID();
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    /**
     * Dispose of resources.
     * Call this when the client is no longer needed.
     */
    public void dispose() {
        disconnect();
        client.stop();
    }
}
