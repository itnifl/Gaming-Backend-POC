package com.example.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * Android launcher for the LibGDX game client.
 * 
 * Note: For a production app, you would want to add:
 * - Server address configuration UI
 * - Player name input
 * - Network state handling (wifi vs mobile data)
 */
public class AndroidLauncher extends AndroidApplication {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.useAccelerometer = false;
        config.useCompass = false;
        
        // For this POC, connect to localhost (emulator's host)
        // 10.0.2.2 is the special IP that redirects to the host machine from Android emulator
        // For real device testing, replace with your server's local IP (e.g., 192.168.1.x)
        String serverHost = "10.0.2.2";
        String playerName = "Android" + (int)(Math.random() * 1000);
        
        initialize(new AndroidGameClient(serverHost, playerName), config);
    }
}
