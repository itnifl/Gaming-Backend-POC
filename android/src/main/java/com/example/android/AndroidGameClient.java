package com.example.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.example.network.NetworkClient;
import com.example.network.NetworkConfig;
import com.example.network.NetworkListener;
import com.example.network.packets.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Android game client with touch controls.
 */
public class AndroidGameClient extends ApplicationAdapter {
    
    private final String serverHost;
    private final String playerName;
    
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    
    private NetworkClient networkClient;
    private boolean connected;
    private int localPlayerId = -1;
    
    // Local player position
    private float localX;
    private float localY;
    private float targetX;
    private float targetY;
    private float speed = 300f;
    
    // Other players
    private final Map<Integer, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();
    
    // Chat messages
    private final CopyOnWriteArrayList<String> chatMessages = new CopyOnWriteArrayList<>();
    private static final int MAX_CHAT_MESSAGES = 5;
    
    // Network stats
    private long lastPingTime = 0;
    private long latency = 0;
    private int pingSequence = 0;
    
    // Position update timer
    private float positionUpdateTimer = 0;
    
    public AndroidGameClient(String serverHost, String playerName) {
        this.serverHost = serverHost;
        this.playerName = playerName;
    }
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
        
        // Initial position at center
        localX = Gdx.graphics.getWidth() / 2f;
        localY = Gdx.graphics.getHeight() / 2f;
        targetX = localX;
        targetY = localY;
        
        setupTouchInput();
        setupNetworking();
        connectToServer();
    }
    
    private void setupTouchInput() {
        GestureDetector gestureDetector = new GestureDetector(new GestureAdapter() {
            @Override
            public boolean tap(float x, float y, int count, int button) {
                // Convert screen coordinates to world coordinates
                targetX = x;
                targetY = Gdx.graphics.getHeight() - y;
                
                // Double tap to send chat
                if (count >= 2 && connected && localPlayerId >= 0) {
                    ChatMessage chat = ChatMessage.player(localPlayerId, playerName, "Hello from Android!");
                    networkClient.sendTCP(chat);
                }
                return true;
            }
            
            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                targetX = x;
                targetY = Gdx.graphics.getHeight() - y;
                return true;
            }
        });
        
        Gdx.input.setInputProcessor(gestureDetector);
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
                addChatMessage("[System] Disconnected");
                
                // Auto-reconnect after 3 seconds
                Gdx.app.postRunnable(() -> {
                    try {
                        Thread.sleep(3000);
                        if (!connected) {
                            connectToServer();
                        }
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                });
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
                addChatMessage("[System] Connecting...");
                networkClient.connect(serverHost);
            } catch (Exception e) {
                Gdx.app.error("Network", "Failed to connect: " + e.getMessage());
                addChatMessage("[Error] " + e.getMessage());
            }
        }).start();
    }
    
    private void handlePacket(Object packet) {
        if (packet instanceof LoginResponse) {
            LoginResponse response = (LoginResponse) packet;
            if (response.success) {
                localPlayerId = response.playerId;
                addChatMessage("[System] Logged in!");
            } else {
                addChatMessage("[Error] " + response.message);
            }
        } else if (packet instanceof PlayerList) {
            PlayerList list = (PlayerList) packet;
            for (PlayerList.PlayerEntry entry : list.players) {
                remotePlayers.put(entry.playerId, new RemotePlayer(entry.playerId, entry.playerName, entry.x, entry.y));
            }
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
        
        // Update movement
        updateMovement(delta);
        
        // Update network
        updateNetwork(delta);
        
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Draw game
        drawGame();
        drawUI();
    }
    
    private void updateMovement(float delta) {
        if (!connected || localPlayerId < 0) return;
        
        // Move towards target
        float dx = targetX - localX;
        float dy = targetY - localY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 5) {
            float moveAmount = Math.min(speed * delta, distance);
            localX += (dx / distance) * moveAmount;
            localY += (dy / distance) * moveAmount;
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
            shapeRenderer.circle(player.x, player.y, 30);
        }
        
        // Draw local player (green)
        if (localPlayerId >= 0) {
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.circle(localX, localY, 30);
        }
        
        // Draw target indicator (white, small)
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(targetX, targetY, 5);
        
        shapeRenderer.end();
        
        // Draw player names
        batch.begin();
        font.setColor(Color.WHITE);
        
        for (RemotePlayer player : remotePlayers.values()) {
            font.draw(batch, player.name, player.x - 50, player.y + 60);
        }
        
        if (localPlayerId >= 0) {
            font.draw(batch, playerName, localX - 50, localY + 60);
        }
        
        batch.end();
    }
    
    private void drawUI() {
        batch.begin();
        
        float screenHeight = Gdx.graphics.getHeight();
        
        // Draw connection status
        font.setColor(connected ? Color.GREEN : Color.RED);
        font.draw(batch, connected ? "Connected" : "Disconnected", 20, screenHeight - 20);
        
        // Draw stats
        font.setColor(Color.WHITE);
        font.draw(batch, "Players: " + (remotePlayers.size() + (localPlayerId >= 0 ? 1 : 0)), 20, screenHeight - 60);
        font.draw(batch, "Latency: " + latency + "ms", 20, screenHeight - 100);
        
        // Draw controls hint
        font.setColor(Color.GRAY);
        font.draw(batch, "Tap to move | Double-tap to chat", 20, 40);
        
        // Draw chat messages
        font.setColor(Color.YELLOW);
        int y = 200;
        for (int i = chatMessages.size() - 1; i >= 0 && y < 400; i--) {
            font.draw(batch, chatMessages.get(i), 20, y);
            y += 40;
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
        
        RemotePlayer(int id, String name, float x, float y) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
        }
        
        void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
