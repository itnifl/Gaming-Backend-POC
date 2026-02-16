package com.example.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.example.network.packets.PacketRegistry;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Network server wrapper for KryoNet.
 * Handles client connections and message broadcasting.
 */
public class NetworkServer {
    
    private final Server server;
    private final Map<Integer, PlayerConnection> connections;
    private final CopyOnWriteArrayList<ServerListener> listeners;
    private volatile boolean running;
    
    public NetworkServer() {
        this.server = new Server(NetworkConfig.WRITE_BUFFER_SIZE, NetworkConfig.OBJECT_BUFFER_SIZE);
        this.connections = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.running = false;
        
        // Register all packet classes
        PacketRegistry.register(server.getKryo());
        
        // Set up internal listener
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                PlayerConnection playerConn = new PlayerConnection(connection);
                connections.put(connection.getID(), playerConn);
                
                for (ServerListener listener : listeners) {
                    listener.onClientConnected(playerConn);
                }
                
                System.out.println("Client connected: " + connection.getID() + 
                    " from " + connection.getRemoteAddressTCP());
            }
            
            @Override
            public void disconnected(Connection connection) {
                PlayerConnection playerConn = connections.remove(connection.getID());
                
                if (playerConn != null) {
                    for (ServerListener listener : listeners) {
                        listener.onClientDisconnected(playerConn);
                    }
                }
                
                System.out.println("Client disconnected: " + connection.getID());
            }
            
            @Override
            public void received(Connection connection, Object object) {
                PlayerConnection playerConn = connections.get(connection.getID());
                
                if (playerConn != null) {
                    for (ServerListener listener : listeners) {
                        listener.onReceived(playerConn, object);
                    }
                }
            }
        });
    }
    
    /**
     * Start the server on specified ports.
     * @param tcpPort TCP port
     * @param udpPort UDP port (use -1 for TCP only)
     * @throws IOException if server fails to bind
     */
    public void start(int tcpPort, int udpPort) throws IOException {
        server.start();
        if (udpPort > 0) {
            server.bind(tcpPort, udpPort);
        } else {
            server.bind(tcpPort);
        }
        running = true;
        System.out.println("Server started on TCP:" + tcpPort + " UDP:" + udpPort);
    }
    
    /**
     * Start the server using default configuration.
     * @throws IOException if server fails to bind
     */
    public void start() throws IOException {
        start(NetworkConfig.TCP_PORT, NetworkConfig.UDP_PORT);
    }
    
    /**
     * Stop the server.
     */
    public void stop() {
        running = false;
        server.stop();
        connections.clear();
        System.out.println("Server stopped");
    }
    
    /**
     * Send a packet to a specific client via TCP.
     * @param connectionId The client's connection ID
     * @param packet The packet to send
     */
    public void sendToTCP(int connectionId, Object packet) {
        server.sendToTCP(connectionId, packet);
    }
    
    /**
     * Send a packet to a specific client via UDP.
     * @param connectionId The client's connection ID
     * @param packet The packet to send
     */
    public void sendToUDP(int connectionId, Object packet) {
        server.sendToUDP(connectionId, packet);
    }
    
    /**
     * Send a packet to all connected clients via TCP.
     * @param packet The packet to send
     */
    public void sendToAllTCP(Object packet) {
        server.sendToAllTCP(packet);
    }
    
    /**
     * Send a packet to all connected clients via UDP.
     * @param packet The packet to send
     */
    public void sendToAllUDP(Object packet) {
        server.sendToAllUDP(packet);
    }
    
    /**
     * Send a packet to all clients except one via TCP.
     * @param excludeConnectionId The connection to exclude
     * @param packet The packet to send
     */
    public void sendToAllExceptTCP(int excludeConnectionId, Object packet) {
        server.sendToAllExceptTCP(excludeConnectionId, packet);
    }
    
    /**
     * Send a packet to all clients except one via UDP.
     * @param excludeConnectionId The connection to exclude
     * @param packet The packet to send
     */
    public void sendToAllExceptUDP(int excludeConnectionId, Object packet) {
        server.sendToAllExceptUDP(excludeConnectionId, packet);
    }
    
    /**
     * Iterate over all connected players.
     * @param action The action to perform for each player
     */
    public void forEachConnection(BiConsumer<Integer, PlayerConnection> action) {
        connections.forEach(action);
    }
    
    /**
     * Get a player connection by ID.
     * @param connectionId The connection ID
     * @return The player connection, or null if not found
     */
    public PlayerConnection getConnection(int connectionId) {
        return connections.get(connectionId);
    }
    
    /**
     * Get the number of connected clients.
     * @return Number of connections
     */
    public int getConnectionCount() {
        return connections.size();
    }
    
    /**
     * Add a server listener.
     * @param listener The listener to add
     */
    public void addListener(ServerListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a server listener.
     * @param listener The listener to remove
     */
    public void removeListener(ServerListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Check if the server is running.
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Get the underlying KryoNet server.
     * @return The KryoNet server instance
     */
    public Server getServer() {
        return server;
    }
    
    /**
     * Represents a connected player with associated data.
     */
    public static class PlayerConnection {
        private final Connection connection;
        private String playerName;
        private float x, y;
        private long lastUpdateTime;
        
        public PlayerConnection(Connection connection) {
            this.connection = connection;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public int getId() {
            return connection.getID();
        }
        
        public Connection getConnection() {
            return connection;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }
        
        public float getX() {
            return x;
        }
        
        public void setX(float x) {
            this.x = x;
        }
        
        public float getY() {
            return y;
        }
        
        public void setY(float y) {
            this.y = y;
        }
        
        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
        
        public void sendTCP(Object packet) {
            connection.sendTCP(packet);
        }
        
        public void sendUDP(Object packet) {
            connection.sendUDP(packet);
        }
    }
    
    /**
     * Server-side event listener interface.
     */
    public interface ServerListener {
        void onClientConnected(PlayerConnection connection);
        void onClientDisconnected(PlayerConnection connection);
        void onReceived(PlayerConnection connection, Object packet);
    }
    
    /**
     * Adapter class for ServerListener with default empty implementations.
     */
    public static class ServerListenerAdapter implements ServerListener {
        @Override
        public void onClientConnected(PlayerConnection connection) {}
        
        @Override
        public void onClientDisconnected(PlayerConnection connection) {}
        
        @Override
        public void onReceived(PlayerConnection connection, Object packet) {}
    }
}
