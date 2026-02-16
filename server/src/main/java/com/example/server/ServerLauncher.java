package com.example.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Server application launcher.
 * Starts the game server and provides a simple console interface.
 */
public class ServerLauncher {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  KryoNet Game Server POC");
        System.out.println("========================================");
        System.out.println();
        
        GameServer server = new GameServer();
        server.start();
        
        // Add shutdown hook for clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.stop();
        }));
        
        // Simple console command interface
        System.out.println("\nServer commands:");
        System.out.println("  status - Show server status");
        System.out.println("  players - List connected players");
        System.out.println("  broadcast <message> - Send message to all players");
        System.out.println("  quit - Stop the server");
        System.out.println();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while (server.isRunning() && (line = reader.readLine()) != null) {
                handleCommand(server, line.trim());
            }
        } catch (Exception e) {
            System.err.println("Console error: " + e.getMessage());
        }
        
        server.stop();
    }
    
    private static void handleCommand(GameServer server, String input) {
        if (input.isEmpty()) return;
        
        String[] parts = input.split(" ", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (command) {
            case "status":
                System.out.println("Server status: " + (server.isRunning() ? "RUNNING" : "STOPPED"));
                System.out.println("Connected players: " + server.getNetworkServer().getConnectionCount());
                break;
                
            case "players":
                System.out.println("Connected players:");
                server.getNetworkServer().forEachConnection((id, conn) -> {
                    String name = conn.getPlayerName();
                    if (name != null) {
                        System.out.println("  [" + id + "] " + name + 
                            " at (" + conn.getX() + ", " + conn.getY() + ")");
                    } else {
                        System.out.println("  [" + id + "] (not logged in)");
                    }
                });
                break;
                
            case "broadcast":
            case "say":
                if (!args.isEmpty()) {
                    var msg = com.example.network.packets.ChatMessage.system(args);
                    server.getNetworkServer().sendToAllTCP(msg);
                    System.out.println("Broadcast sent: " + args);
                } else {
                    System.out.println("Usage: broadcast <message>");
                }
                break;
                
            case "quit":
            case "exit":
            case "stop":
                System.out.println("Stopping server...");
                server.stop();
                System.exit(0);
                break;
                
            case "help":
                System.out.println("Commands: status, players, broadcast <msg>, quit");
                break;
                
            default:
                System.out.println("Unknown command: " + command + " (type 'help' for commands)");
        }
    }
}
