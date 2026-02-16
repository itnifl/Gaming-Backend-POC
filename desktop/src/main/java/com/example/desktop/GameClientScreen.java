package com.example.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.network.NetworkClient;
import com.example.network.NetworkConfig;
import com.example.network.NetworkListener;
import com.example.network.packets.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple game client demonstrating KryoNet networking with LibGDX.
 */
public class GameClientScreen extends ApplicationAdapter {
    
    private final String serverHost;
    private final String playerName;
    
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    
    private NetworkClient networkClient;
    private boolean connected;
    private int localPlayerId = -1;
    
    // Local player position
    private float localX = 400;
    private float localY = 300;
    private float speed = 200f;
    
    // Other players
    private final Map<Integer, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();
    
    // Chat messages
    private final CopyOnWriteArrayList<String> chatMessages = new CopyOnWriteArrayList<>();
    private static final int MAX_CHAT_MESSAGES = 10;
    
    // Network stats
    private long lastPingTime = 0;
    private long latency = 0;
    private int pingSequence = 0;
    
    // Position update timer
    private float positionUpdateTimer = 0;
    
    public GameClientScreen(String serverHost, String playerName) {
        this.serverHost = serverHost;
        this.playerName = playerName;
    }
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        
        setupNetworking();
        connectToServer();
    }
    
    private void setupNetworking() {
        networkClient = new NetworkClient();
        networkClient.setPlayerName(playerName);
        
        networkClient.addListener(new NetworkListener() {
            @Override
            public void onConnected() {
                Gdx.app.log("Network", "Connected to server!");
                connected = true;
                
                // Send login request
                LoginRequest login = new LoginRequest(playerName);
                networkClient.sendTCP(login);
            }
            
            @Override
            public void onDisconnected() {
                Gdx.app.log("Network", "Disconnected from server");
                connected = false;
                localPlayerId = -1;
                remotePlayers.clear();
                addChatMessage("[System] Disconnected from server");
            }
            
            @Override
            public void onReceived(Object packet) {
                handlePacket(packet);
            }
        });
    }
    
    private void connectToServer() {
        new Thread(() -> {
            try {
                Gdx.app.log("Network", "Connecting to " + serverHost + "...");
                networkClient.connect(serverHost);
            } catch (Exception e) {
                Gdx.app.error("Network", "Failed to connect: " + e.getMessage());
                addChatMessage("[Error] Failed to connect: " + e.getMessage());
            }
        }).start();
    }
    
    private void handlePacket(Object packet) {
        if (packet instanceof LoginResponse) {
            LoginResponse response = (LoginResponse) packet;
            if (response.success) {
                localPlayerId = response.playerId;
                addChatMessage("[System] Logged in as " + playerName + " (ID: " + localPlayerId + ")");
                Gdx.app.log("Network", "Login successful! ID: " + localPlayerId);
            } else {
                addChatMessage("[Error] Login failed: " + response.message);
                Gdx.app.error("Network", "Login failed: " + response.message);
            }
        } else if (packet instanceof PlayerList) {
            PlayerList list = (PlayerList) packet;
            for (PlayerList.PlayerEntry entry : list.players) {
                remotePlayers.put(entry.playerId, new RemotePlayer(entry.playerId, entry.playerName, entry.x, entry.y));
            }
            Gdx.app.log("Network", "Received player list: " + list.players.size() + " players");
        } else if (packet instanceof PlayerJoined) {
            PlayerJoined joined = (PlayerJoined) packet;
            if (joined.playerId != localPlayerId) {
                remotePlayers.put(joined.playerId, new RemotePlayer(joined.playerId, joined.playerName, joined.x, joined.y));
            }
        } else if (packet instanceof PlayerLeft) {
            PlayerLeft left = (PlayerLeft) packet;
            remotePlayers.remove(left.playerId);
        } else if (packet instanceof PlayerPosition) {
            PlayerPosition pos = (PlayerPosition) packet;
            RemotePlayer player = remotePlayers.get(pos.playerId);
            if (player != null) {
                player.setPosition(pos.x, pos.y);
            }
        } else if (packet instanceof ChatMessage) {
            ChatMessage chat = (ChatMessage) packet;
            addChatMessage(chat.toString());
        } else if (packet instanceof Pong) {
            Pong pong = (Pong) packet;
            latency = pong.getRoundTripTime();
        }
    }
    
    private void addChatMessage(String message) {
        chatMessages.add(message);
        while (chatMessages.size() > MAX_CHAT_MESSAGES) {
            chatMessages.remove(0);
        }
    }
    
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        
        // Handle input
        handleInput(delta);
        
        // Update network
        updateNetwork(delta);
        
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Draw game
        drawGame();
        drawUI();
    }
    
    private void handleInput(float delta) {
        if (!connected || localPlayerId < 0) return;
        
        float dx = 0, dy = 0;
        
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx += speed * delta;
        }
        
        if (dx != 0 || dy != 0) {
            localX += dx;
            localY += dy;
            
            // Clamp to screen bounds
            localX = Math.max(20, Math.min(localX, Gdx.graphics.getWidth() - 20));
            localY = Math.max(20, Math.min(localY, Gdx.graphics.getHeight() - 20));
        }
        
        // Send chat message on Enter (simple test message)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            ChatMessage chat = ChatMessage.player(localPlayerId, playerName, "Hello from " + playerName + "!");
            networkClient.sendTCP(chat);
        }
        
        // Reconnect on R
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && !connected) {
            connectToServer();
        }
        
        // Send ping on P
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            networkClient.sendTCP(new Ping(pingSequence++));
        }
    }
    
    private void updateNetwork(float delta) {
        if (!connected || localPlayerId < 0) return;
        
        // Send position updates at fixed rate
        positionUpdateTimer += delta;
        if (positionUpdateTimer >= NetworkConfig.POSITION_UPDATE_RATE) {
            positionUpdateTimer = 0;
            
            PlayerPosition pos = new PlayerPosition(localPlayerId, localX, localY);
            networkClient.sendUDP(pos);
        }
        
        // Auto-ping every 2 seconds
        if (System.currentTimeMillis() - lastPingTime > 2000) {
            lastPingTime = System.currentTimeMillis();
            networkClient.sendTCP(new Ping(pingSequence++));
        }
    }
    
    private void drawGame() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw remote players (blue)
        shapeRenderer.setColor(Color.BLUE);
        for (RemotePlayer player : remotePlayers.values()) {
            shapeRenderer.circle(player.x, player.y, 15);
        }
        
        // Draw local player (green)
        if (localPlayerId >= 0) {
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.circle(localX, localY, 15);
        }
        
        shapeRenderer.end();
        
        // Draw player names
        batch.begin();
        font.setColor(Color.WHITE);
        
        for (RemotePlayer player : remotePlayers.values()) {
            font.draw(batch, player.name, player.x - 30, player.y + 30);
        }
        
        if (localPlayerId >= 0) {
            font.draw(batch, playerName + " (you)", localX - 30, localY + 30);
        }
        
        batch.end();
    }
    
    private void drawUI() {
        batch.begin();
        
        // Draw connection status
        font.setColor(connected ? Color.GREEN : Color.RED);
        font.draw(batch, connected ? "Connected" : "Disconnected", 10, Gdx.graphics.getHeight() - 10);
        
        // Draw stats
        font.setColor(Color.WHITE);
        font.draw(batch, "Players: " + (remotePlayers.size() + (localPlayerId >= 0 ? 1 : 0)), 10, Gdx.graphics.getHeight() - 30);
        font.draw(batch, "Latency: " + latency + "ms", 10, Gdx.graphics.getHeight() - 50);
        font.draw(batch, "ID: " + localPlayerId, 10, Gdx.graphics.getHeight() - 70);
        
        // Draw controls
        font.setColor(Color.GRAY);
        font.draw(batch, "WASD: Move | Enter: Chat | P: Ping | R: Reconnect", 10, 20);
        
        // Draw chat messages
        font.setColor(Color.YELLOW);
        int y = 150;
        for (int i = chatMessages.size() - 1; i >= 0 && y < 350; i--) {
            font.draw(batch, chatMessages.get(i), 10, y);
            y += 18;
        }
        
        batch.end();
    }
    
    @Override
    public void dispose() {
        if (networkClient != null) {
            networkClient.dispose();
        }
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
    
    /**
     * Represents a remote player.
     */
    private static class RemotePlayer {
        final int id;
        final String name;
        float x, y;
        float targetX, targetY;
        
        RemotePlayer(int id, String name, float x, float y) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
        }
        
        void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
        }
    }
}
