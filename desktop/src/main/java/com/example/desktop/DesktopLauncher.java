package com.example.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Desktop launcher for the LibGDX game client.
 */
public class DesktopLauncher {
    
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("KryoNet LibGDX POC - Client");
        config.setWindowedMode(800, 600);
        config.setForegroundFPS(60);
        config.useVsync(true);
        
        // Parse command line args for server host
        String host = "localhost";
        String playerName = "Player" + (int)(Math.random() * 1000);
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--host") && i + 1 < args.length) {
                host = args[i + 1];
            } else if (args[i].equals("--name") && i + 1 < args.length) {
                playerName = args[i + 1];
            }
        }
        
        new Lwjgl3Application(new GameClientScreen(host, playerName), config);
    }
}
