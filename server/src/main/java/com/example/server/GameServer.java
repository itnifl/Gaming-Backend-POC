package com.example.server;

import com.example.network.NetworkConfig;
import com.example.network.NetworkServer;
import com.example.network.NetworkServer.PlayerConnection;
import com.example.network.NetworkServer.ServerListenerAdapter;
import com.example.network.packets.*;

/**
 * Main game server implementation.
 * Handles game logic and coordinates all player interactions.
 */
public class GameServer extends ServerListenerAdapter {
    
    private final NetworkServer networkServer;
    private volatile boolean running;
    
    public GameServer() {
        this.networkServer = new NetworkServer();
        this.networkServer.addListener(this);
    }
    
    /**
     * Start the game server.
     */
    public void start() {
        try {
            networkServer.start();
            running = true;
            System.out.println("Game server started successfully!");
            System.out.println("TCP Port: " + NetworkConfig.TCP_PORT);
            System.out.println("UDP Port: " + NetworkConfig.UDP_PORT);
            System.out.println("Max players: " + NetworkConfig.MAX_PLAYERS);
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Stop the game server.
     */
    public void stop() {
        running = false;
        networkServer.stop();
        System.out.println("Game server stopped.");
    }
    
    @Override
    public void onClientConnected(PlayerConnection connection) {
        System.out.println("New connection from client #" + connection.getId());
    }
    
    @Override
    public void onClientDisconnected(PlayerConnection connection) {
        String playerName = connection.getPlayerName();
        if (playerName != null) {
            // Notify all other players that this player left
            PlayerLeft leftPacket = new PlayerLeft(connection.getId(), playerName, "Disconnected");
            networkServer.sendToAllExceptTCP(connection.getId(), leftPacket);
            
            // Send system message
            ChatMessage systemMsg = ChatMessage.system(playerName + " has left the game.");
            networkServer.sendToAllTCP(systemMsg);
            
            System.out.println("Player '" + playerName + "' disconnected.");
        }
    }
    
    @Override
    public void onReceived(PlayerConnection connection, Object packet) {
        // Handle different packet types
        if (packet instanceof LoginRequest) {
            handleLogin(connection, (LoginRequest) packet);
        } else if (packet instanceof PlayerPosition) {
            handlePosition(connection, (PlayerPosition) packet);
        } else if (packet instanceof ChatMessage) {
            handleChat(connection, (ChatMessage) packet);
        } else if (packet instanceof Ping) {
            handlePing(connection, (Ping) packet);
        }
    }
    
    /**
     * Handle login request from a client.
     */
    private void handleLogin(PlayerConnection connection, LoginRequest request) {
        System.out.println("Login request from: " + request.playerName);
        
        // Check if server is full
        if (networkServer.getConnectionCount() > NetworkConfig.MAX_PLAYERS) {
            connection.sendTCP(LoginResponse.failure("Server is full"));
            return;
        }
        
        // Validate player name
        if (request.playerName == null || request.playerName.trim().isEmpty()) {
            connection.sendTCP(LoginResponse.failure("Invalid player name"));
            return;
        }
        
        // Check for duplicate names
        boolean nameTaken = false;
        for (var entry : getConnections()) {
            if (request.playerName.equalsIgnoreCase(entry.getPlayerName())) {
                nameTaken = true;
                break;
            }
        }
        
        if (nameTaken) {
            connection.sendTCP(LoginResponse.failure("Name already taken"));
            return;
        }
        
        // Accept the login
        connection.setPlayerName(request.playerName.trim());
        connection.setPosition(100 + (float)(Math.random() * 400), 100 + (float)(Math.random() * 400));
        
        // Send success response
        connection.sendTCP(LoginResponse.success(connection.getId()));
        
        // Send list of existing players to the new player
        PlayerList playerList = new PlayerList();
        networkServer.forEachConnection((id, player) -> {
            if (id != connection.getId() && player.getPlayerName() != null) {
                playerList.addPlayer(id, player.getPlayerName(), player.getX(), player.getY());
            }
        });
        connection.sendTCP(playerList);
        
        // Notify all other players about the new player
        PlayerJoined joinedPacket = new PlayerJoined(
            connection.getId(), 
            connection.getPlayerName(),
            connection.getX(),
            connection.getY()
        );
        networkServer.sendToAllExceptTCP(connection.getId(), joinedPacket);
        
        // Send system message
        ChatMessage systemMsg = ChatMessage.system(connection.getPlayerName() + " has joined the game!");
        networkServer.sendToAllTCP(systemMsg);
        
        System.out.println("Player '" + connection.getPlayerName() + "' logged in successfully.");
    }
    
    /**
     * Handle position update from a client.
     */
    private void handlePosition(PlayerConnection connection, PlayerPosition position) {
        // Update server-side position
        connection.setPosition(position.x, position.y);
        
        // Broadcast to all other players (via UDP for performance)
        position.playerId = connection.getId();
        networkServer.sendToAllExceptUDP(connection.getId(), position);
    }
    
    /**
     * Handle chat message from a client.
     */
    private void handleChat(PlayerConnection connection, ChatMessage message) {
        // Set the sender info from server-side data (prevent spoofing)
        message.senderId = connection.getId();
        message.senderName = connection.getPlayerName();
        message.timestamp = System.currentTimeMillis();
        
        System.out.println("Chat: " + message);
        
        // Broadcast to all players
        networkServer.sendToAllTCP(message);
    }
    
    /**
     * Handle ping request from a client.
     */
    private void handlePing(PlayerConnection connection, Ping ping) {
        connection.sendTCP(new Pong(ping));
    }
    
    /**
     * Get all player connections.
     */
    private Iterable<PlayerConnection> getConnections() {
        java.util.List<PlayerConnection> list = new java.util.ArrayList<>();
        networkServer.forEachConnection((id, conn) -> list.add(conn));
        return list;
    }
    
    /**
     * Get the network server instance.
     */
    public NetworkServer getNetworkServer() {
        return networkServer;
    }
    
    /**
     * Check if the server is running.
     */
    public boolean isRunning() {
        return running;
    }
}
