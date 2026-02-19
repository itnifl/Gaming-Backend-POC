# KryoNet LibGDX Multiplayer POC

A practical multiplayer game networking example built for TDT4240 Software Architecture. <br/> This project demonstrates how to implement real-time client-server communication using KryoNet with LibGDX.

## What This Project Does

This is a simple multiplayer game where players can:
- Connect to a central server
- Move around a shared 2D space
- See other players moving in real-time
- Send chat messages

It's intentionally minimal to serve as a starting point for your own multiplayer game.

---

## Prerequisites

Before running this project, ensure you have:

### Java Development Kit (JDK)

**Required:** JDK 11 or newer (JDK 17 or 21 recommended for long-term support).

Verify your installation:
```powershell
java -version
```

**Suggested (if not installed):** Download from [Adoptium](https://adoptium.net/).

Java is a specification, not a single download—multiple vendors provide JDK implementations (Oracle JDK, Amazon Corretto, Azul Zulu, etc.). Any of them work. Adoptium is suggested because it's vendor-neutral, has no licensing restrictions, and provides straightforward installers for all platforms.

### VS Code Extensions

Install these extensions via the Extensions panel (`Ctrl+Shift+X`):

| Extension | Publisher | Required? | Purpose |
|-----------|-----------|-----------|---------|
| **Extension Pack for Java** | Microsoft | **Yes** | Java language support, debugging, project management |
| **Gradle for Java** | Microsoft | **Yes** | Build support (included in Extension Pack) |
| **Debugger for Java** | Microsoft | **Yes** | Run/Debug configs (included in Extension Pack) |

> **Tip:** Installing "Extension Pack for Java" automatically installs all necessary extensions including Gradle support.

### Gradle (Build Tool)

**No manual installation required.** This project uses the Gradle Wrapper (`gradlew.bat` / `gradlew`), which automatically downloads Gradle 9.3.1 on first run.

---

## Quick Start with VS Code

**[Watch the video walkthrough](demo-media/howtostart.mp4)** if you prefer a visual guide.

### Starting the Server (VS Code Debug)

1. Open the **Run and Debug** panel by clicking the play button with a bug icon in the left sidebar, or press `Ctrl+Shift+D`
2. At the top of the panel, click the dropdown menu (it may say "No Configurations" or show a previous config)
3. Select **"Run Server"**
4. Click the green play button, or press `F5`

The server starts in the integrated terminal. You should see:
```
========================================
  KryoNet Game Server POC
========================================
Server started on TCP:27960 UDP:27961
```

### Starting a Client (VS Code Debug)

With the server still running:

1. Go back to the **Run and Debug** panel (`Ctrl+Shift+D`)
2. Click the dropdown and select **"Run Desktop Client"**
3. Click the green play button

A LibGDX window opens showing your player as a green circle.

### Running Multiple Clients

You can start additional clients:
1. Select **"Run Desktop Client 2"** from the dropdown
2. Click play

Each client appears as a different colored circle. You'll see them move in real-time.

### Alternative: Using the Terminal

If the debug buttons don't appear, use the terminal instead:

**Terminal 1 - Server:**
```powershell
.\gradlew.bat :server:run
```

**Terminal 2 - Client** (press `` Ctrl+Shift+` `` to open a new terminal):
```powershell
.\gradlew.bat :desktop:run
```

### Controls

| Key | Action |
|-----|--------|
| WASD / Arrows | Move your player |
| Enter | Send a test chat message |
| P | Measure ping/latency |
| R | Reconnect to server |

---

## Project Structure

```
Gaming-Backend-POC/
├── core/                           # Shared networking code
│   └── com.example.network/
│       ├── NetworkClient.java      # KryoNet client wrapper (Facade)
│       ├── NetworkServer.java      # KryoNet server wrapper (Facade)
│       ├── NetworkConfig.java      # Ports, timeouts, buffer sizes
│       ├── NetworkListener.java    # Simplified listener interface
│       └── packets/
│           ├── PacketRegistry.java # Central packet registration
│           ├── LoginRequest.java   # Client → Server: join game
│           ├── LoginResponse.java  # Server → Client: login result
│           ├── PlayerPosition.java # Position updates (UDP)
│           ├── PlayerJoined.java   # Broadcast: new player
│           ├── PlayerLeft.java     # Broadcast: player disconnected
│           ├── PlayerList.java     # Server → Client: all current players
│           ├── ChatMessage.java    # Chat messages (TCP)
│           ├── Ping.java           # Latency measurement request
│           └── Pong.java           # Latency measurement response
├── server/                         # Standalone server application
│   └── com.example.server/
│       ├── ServerLauncher.java     # Entry point
│       └── GameServer.java         # Game logic, validation, broadcasting
├── desktop/                        # LibGDX desktop client
│   └── com.example.desktop/
│       ├── DesktopLauncher.java    # Entry point
│       └── GameClientScreen.java   # Rendering, input, network callbacks
└── android/                        # Android client (tap to move)
```

---

## Understanding the Networking

### Why KryoNet?

We chose KryoNet for this project because:

1. **Built for games.** Created by the same developer as LibGDX, designed specifically for game networking needs.
2. **TCP + UDP support.** Real time games need both protocols:
   - **TCP** for messages that must arrive (login, chat, game events)
   - **UDP** for frequent updates where speed matters more than reliability (positions)
3. **Simple API.** Get multiplayer working quickly without deep networking knowledge.
4. **Kryo serialization.** Your Java objects are automatically converted to compact bytes, much faster and smaller than JSON.
5. **Works with LibGDX and Android.** No extra integration work needed.

#### Why LibGDX and Android Integration "Just Works"

KryoNet was created by Nathan Sweet, the same developer behind LibGDX. This shared origin means:

- **Same threading philosophy** — Both libraries expect you to handle threading explicitly. KryoNet's network thread and LibGDX's render thread are designed to coexist (you bridge them with `ConcurrentHashMap`, as shown in this project).
- **Pure Java, no native dependencies** — KryoNet uses only Java NIO (non-blocking I/O), which is available on all platforms including Android. No JNI, no platform-specific code.
- **Gradle-friendly** — Add one dependency line to your `build.gradle` and it works across desktop, Android, and iOS (via RoboVM).
- **Small footprint** — KryoNet + Kryo add ~200KB to your APK. Compare to alternatives that pull in megabytes of transitive dependencies.

For Android specifically, KryoNet respects mobile constraints:
- Works on any network thread you provide (doesn't force `AsyncTask` or other Android-specific patterns)
- No special permissions beyond `INTERNET`
- Handles WiFi ↔ mobile data transitions gracefully (TCP reconnection)

### About KryoNet's Java Version

KryoNet was written for **Java 7** and the library hasn't been updated since 2018. However, this is perfectly acceptable for several reasons:

1. **Java is backwards compatible.** Code written for Java 7 runs fine on Java 11, 17, 21, or any newer version. Your project uses Java 11+, and KryoNet works without issues.
2. **Networking APIs haven't changed.** The Java NIO classes that KryoNet uses (channels, selectors, buffers) are stable and haven't been deprecated.
3. **It's feature complete.** KryoNet does what it needs to do. Networking libraries don't need constant updates unless there are security issues.
4. **Battle tested.** Many LibGDX games in production use KryoNet. Stability is more important than recency.

**Can it be updated?** The library is open source, so anyone could fork it and update the code style to use newer Java features (records, var, etc.). But functionally, there's nothing to fix. It works correctly as is.

If you need a more actively maintained option for production, consider **Netty** (see comparison below).

### KryoNet Basics

KryoNet has four core concepts:

- **`Server`** — Listens on TCP/UDP ports, accepts connections, broadcasts messages
- **`Client`** — Connects to a server, sends and receives messages
- **`Kryo`** — Serializer that converts Java objects to bytes (and back)
- **`Listener`** — Callback interface for connect/disconnect/receive events

Minimal example:
```java
// Server
Server server = new Server();
server.getKryo().register(MyPacket.class);
server.start();
server.bind(27960, 27961);  // TCP, UDP

// Client
Client client = new Client();
client.getKryo().register(MyPacket.class);
client.start();
client.connect(5000, "localhost", 27960, 27961);
client.sendTCP(new MyPacket());
```

### Connection Objects and Broadcasting

Every connected client is represented by a `Connection` object on the server. Key concepts:

**Connection IDs:**
- Each connection gets a unique integer ID (`connection.getID()`)
- IDs start at 1 and increment for each new connection
- IDs are reused after disconnection
- Use IDs to identify players in packets (e.g., `playerId` field)

**Sending messages from the server:**
```java
// To one client
connection.sendTCP(packet);
connection.sendUDP(packet);

// To all clients
server.sendToAllTCP(packet);
server.sendToAllUDP(packet);

// To all except one (e.g., don't echo position back to sender)
server.sendToAllExceptTCP(connection.getID(), packet);
server.sendToAllExceptUDP(connection.getID(), packet);
```

**Disconnect detection:**
- KryoNet uses TCP keepalive to detect dead connections
- When a client disconnects (or times out), `listener.disconnected(connection)` is called
- Always clean up player data in the disconnect handler

**Buffer sizes** (set in constructor):
```java
// Server(writeBufferSize, objectBufferSize)
Server server = new Server(16384, 4096);
```
- `writeBufferSize`: Max bytes queued for sending (larger = more pending messages)
- `objectBufferSize`: Max size of a single serialized object (larger = bigger packets allowed)

If you see "Buffer overflow" errors, increase these values.

### How Network Communication Works

The client and server communicate through a simple request-response flow:

1. **Client connects** to the server via TCP (reliable connection)
2. **Client sends LoginRequest** with the player's name
3. **Server validates** and sends back LoginResponse (success/failure)
4. **Server broadcasts PlayerJoined** to all other connected clients
5. **During gameplay**, clients send PlayerPosition updates via UDP (fast, frequent)
6. **Server relays** position updates to all other clients
7. **Chat messages** go through TCP (must arrive reliably)
8. **When a client disconnects**, the server broadcasts PlayerLeft to others

The key insight: **the server is authoritative**. Clients send their intended actions, the server decides what actually happens, and broadcasts the result to everyone.

### The Packet System

Packets are simple Java classes that hold data. KryoNet uses the Kryo library to automatically convert these objects to bytes for transmission.

```java
// A packet is just a data class
public class PlayerPosition {
    public int playerId;
    public float x;
    public float y;
    public long timestamp;
    
    public PlayerPosition() {} // Required by Kryo!
}
```

**Why the empty constructor?** Kryo uses **reflection** to deserialize packets. When bytes arrive over the network, Kryo:
1. Reads the class ID from the byte stream
2. Calls `Class.newInstance()` to create an empty object (requires no-arg constructor)
3. Uses reflection to set each field value from the remaining bytes

Without a no-arg constructor, step 2 fails with an `InstantiationException`.

**No interfaces or abstract classes needed.** Kryo serializes POJOs (Plain Old Java Objects) directly. Unlike Java's built-in serialization, packets don't need to implement `Serializable`. Kryo inspects the class at registration time and generates optimized serialization code. This is why:
- Fields must be `public` (or have getters/setters)
- You can use any field types Kryo knows about
- Nested objects work automatically if registered

**Registration is critical.** Every packet class must be registered in the same order on both client and server:

```java
// In PacketRegistry.java - this runs on BOTH client and server
public static void register(Kryo kryo) {
    kryo.register(LoginRequest.class);    // ID 0
    kryo.register(LoginResponse.class);   // ID 1
    kryo.register(PlayerPosition.class);  // ID 2
    // Order matters! Same order = same IDs
}
```

If the client registers `LoginRequest` first but the server registers `LoginResponse` first, they'll misinterpret each other's messages.

### TCP vs UDP: When to Use Each

| Use TCP for... | Use UDP for... |
|----------------|----------------|
| Login/logout | Position updates |
| Chat messages | Input states |
| Game events (damage, item pickup) | Animation states |
| Anything that *must* arrive | Anything sent many times per second |

```java
// TCP - guaranteed delivery, use for important messages
client.sendTCP(new ChatMessage("Hello!"));

// UDP - fast but may be lost, use for frequent updates
client.sendUDP(new PlayerPosition(playerId, x, y));
```

---

## Architecture: Layered Design

This project uses a **3-tier layered architecture**, where each layer has a single responsibility and only communicates with adjacent layers.

```
┌─────────────────────────────────────────────────────────┐
│  Presentation Layer (desktop/, android/)                │
│  - Rendering, input handling, UI                        │
│  - Platform-specific code (LibGDX, Android SDK)         │
└─────────────────────────┬───────────────────────────────┘
                          │ uses
┌─────────────────────────▼───────────────────────────────┐
│  Application Layer (server/GameServer)                  │
│  - Game logic, validation, state management             │
│  - Coordinates player interactions                      │
└─────────────────────────┬───────────────────────────────┘
                          │ uses
┌─────────────────────────▼───────────────────────────────┐
│  Network Layer (core/)                                  │
│  - Transport (KryoNet), serialization (Kryo)            │
│  - Packet definitions, connection management            │
└─────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Module(s) | Contains |
|-------|-----------|----------|
| **Presentation** | `desktop/`, `android/` | `GameClientScreen`, `AndroidGameClient`, rendering, input |
| **Application** | `server/` | `GameServer`, game rules, validation, broadcasting |
| **Network** | `core/` | `NetworkClient`, `NetworkServer`, packets, config |

### Why Layers Matter

- **Separation of concerns** — Each layer does one thing well
- **Testability** — You can test game logic without rendering
- **Portability** — Swap `desktop/` for `android/` without changing `core/`
- **Team scaling** — Different developers can work on different layers

### What About MVC?

MVC (Model-View-Controller) is a finer-grained pattern *within* the Presentation layer. Currently, `GameClientScreen` combines all three roles:

```
GameClientScreen (current "god class")
├── Model:      localX, localY, remotePlayers, chatMessages
├── View:       SpriteBatch, ShapeRenderer, render()
└── Controller: handleInput(), handlePacket(), network callbacks
```

**Should you refactor to MVC?** Consider these trade-offs:

| Approach | Pros | Cons |
|----------|------|------|
| **Keep as-is** | Simple, easy to follow, good for learning | Harder to test, grows unwieldy |
| **Extract Model** | Testable game state, cleaner separation | More files, indirection |
| **Full MVC** | Maximum separation, industry standard, highly maintainable long-term, foundation for enterprise patterns | Overkill for small projects |

**Suggestion:** If your client grows beyond ~500 lines, extract a `GameState` class:

```java
// GameState.java - the Model
public class GameState {
    private float localX, localY;
    private int localPlayerId = -1;
    private final Map<Integer, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();
    private final List<String> chatMessages = new CopyOnWriteArrayList<>();
    
    // Getters, setters, business logic (collision detection, etc.)
}

// GameClientScreen.java - becomes View + Controller
public class GameClientScreen extends ApplicationAdapter {
    private GameState state;  // Model
    private GameRenderer renderer;  // Could extract View too
    // Input handling and network callbacks update `state`
}
```

This gives you testable state without full MVC complexity.

### Other Architectural Patterns to Consider

| Pattern | When to Use | Complexity |
|---------|-------------|------------|
| **State Pattern** | Multiple game states (menu, playing, paused, game over) | Low |
| **Entity-Component-System** | Many entity types with shared behaviors | High |
| **Event Bus** | Decouple subsystems (audio, UI, gameplay) | Medium |
| **Repository Pattern** | Persistent data (save games, leaderboards) | Medium |

**Recommendation for a project:** The current architecture is appropriate for a networking demonstration. If you extend it into a full game, consider adding the **State Pattern** for game states first—it's high value for low effort.

---

## Programming Patterns Inherent to KryoNet

This section explains the **design patterns that KryoNet forces you to use**. Unlike optional architectural choices, these patterns are built into how KryoNet works—you cannot avoid them.

> **For beginners:** A design pattern is a proven solution to a common coding problem. Think of it like a recipe: instead of inventing how to make bread from scratch, you follow a recipe that others have perfected. Patterns give names to these "recipes" so developers can communicate efficiently.
>
> For a comprehensive catalog, see [refactoring.guru/design-patterns](https://refactoring.guru/design-patterns).

### Why Does KryoNet Force Specific Patterns?

KryoNet is a **framework**, not a library. The difference matters:
- A **library** is code you call when you want to.
- A **framework** is code that calls *your* code—it controls the flow.

KryoNet controls how packets are received (callbacks, not polling), how classes are identified (registration order), and what thread your code runs on. These constraints naturally lead to specific patterns.

### Pattern Summary Table

| Pattern | Category | Required? | Why KryoNet Forces It |
|---------|----------|-----------|----------------------|
| **[Observer](#1-observer-pattern-required)** | Behavioral | **Yes** | KryoNet uses callbacks (`Listener.received()`). No polling API exists. |
| **[Command](#2-command-pattern-required)** | Behavioral | **Yes** | All packets arrive as `Object`. You must dispatch by type. |
| **[Registry](#3-registry-pattern-required)** | Creational | **Yes** | Kryo assigns class IDs by registration order. Client/server must match. |
| **[Producer-Consumer](#4-producer-consumer-pattern-required-for-games)** | Concurrency | **Yes*** | Network thread is separate from game loop. Shared state needs synchronization. |
| **[Connector](#5-connector-pattern-recommended)** | Architectural | Recommended | KryoNet offers TCP+UDP. Abstracting protocol choice improves design. |
| **[Facade](#6-facade-pattern-recommended)** | Structural | Recommended | KryoNet's raw API is verbose. Wrapping simplifies usage. |

\* Producer-Consumer is technically avoidable if you process everything on the network thread, but that blocks network I/O and causes lag—don't do it.

### Visual: How Packets Flow Through the Patterns

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT SIDE                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  [User Input]                                                               │
│       │                                                                     │
│       ▼                                                                     │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────────────────┐ │
│  │   COMMAND   │───▶│  REGISTRY   │───▶│   CONNECTOR (TCP or UDP)        │ │
│  │ PlayerPos   │    │ ID=5 → bytes│    │   Chooses protocol, sends       │ │
│  └─────────────┘    └─────────────┘    └───────────────┬─────────────────┘ │
│                                                         │  [NETWORK]        │
└─────────────────────────────────────────────────────────┼───────────────────┘
                                                          │
                        ════════════════════════════════════════════════
                                                          │
┌─────────────────────────────────────────────────────────┼───────────────────┐
│                           SERVER SIDE                   │                   │
├─────────────────────────────────────────────────────────┼───────────────────┤
│                                                         ▼                   │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                     CONNECTOR (receives bytes)                        │ │
│  └───────────────────────────────────────┬───────────────────────────────┘ │
│                                          │                                  │
│                                          ▼                                  │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │    REGISTRY: bytes → ID=5 → PlayerPosition.class → new instance      │ │
│  └───────────────────────────────────────┬───────────────────────────────┘ │
│                                          │                                  │
│                                          ▼                                  │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │    OBSERVER: listener.received(connection, playerPosObject)          │ │
│  │              ──────────────────────────────────────────────          │ │
│  │              (runs on NETWORK THREAD)                                 │ │
│  └───────────────────────────────────────┬───────────────────────────────┘ │
│                                          │                                  │
│                                          ▼                                  │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │    COMMAND: if (packet instanceof PlayerPosition) handlePosition()   │ │
│  └───────────────────────────────────────┬───────────────────────────────┘ │
│                                          │                                  │
│                                          ▼                                  │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │    PRODUCER-CONSUMER: write to ConcurrentHashMap (shared buffer)     │ │
│  │                       Game loop reads on render thread               │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 1. Observer Pattern (Required)

#### What is it?

Imagine a newspaper subscription. You (the **observer**) subscribe to a newspaper (the **subject**). When a new edition is published, the newspaper automatically delivers it to all subscribers. The newspaper doesn't know what you do with it—read it, recycle it, or use it to wrap fish. It just delivers.

In code terms:
- **Subject** — The object that has interesting events (KryoNet's network connection)
- **Observer** — Objects that want to know when events happen (your game code)
- **Subscribe** — Register to receive notifications
- **Notify** — Subject calls a method on all observers when something happens

#### Why KryoNet REQUIRES This Pattern

KryoNet is **event-driven**, not poll-driven. Compare:

```java
// ❌ POLLING (not how KryoNet works)
// You would have to constantly check for new data
while (running) {
    Object packet = client.poll();  // This method doesn't exist!
    if (packet != null) {
        handlePacket(packet);
    }
    Thread.sleep(10);
}

// ✅ OBSERVER (how KryoNet actually works)
// KryoNet calls YOUR code when data arrives
client.addListener(new Listener() {
    @Override
    public void received(Connection connection, Object packet) {
        // KryoNet calls this for you automatically!
        handlePacket(packet);
    }
});
```

**There is no `client.poll()` method.** KryoNet's `Listener` interface is the *only* way to receive packets. You must implement callbacks—the Observer pattern is literally the API.

#### KryoNet's Built-in Observer: The Listener Interface

KryoNet provides this interface (simplified):

```java
// KryoNet's Listener interface - this IS the Observer pattern
public abstract class Listener {
    // Called when a client connects (server-side) or connection established (client-side)
    public void connected(Connection connection) {}
    
    // Called when connection is lost
    public void disconnected(Connection connection) {}
    
    // Called when a packet arrives - THIS IS WHERE YOUR GAME LOGIC GOES
    public void received(Connection connection, Object object) {}
    
    // Called when connection becomes idle (optional)
    public void idle(Connection connection) {}
}
```

#### Complete Example: Implementing an Observer

```java
// Step 1: Create your observer by extending Listener (or implementing NetworkListener)
public class GameClientListener extends Listener {
    
    private final GameScreen gameScreen;  // Reference to update the UI
    
    public GameClientListener(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }
    
    @Override
    public void connected(Connection connection) {
        System.out.println("Connected to server!");
        // Maybe show "Connected" on screen, enable the "Play" button, etc.
        gameScreen.setConnectionStatus("Connected");
    }
    
    @Override
    public void disconnected(Connection connection) {
        System.out.println("Disconnected from server!");
        // Show reconnect dialog, disable controls, etc.
        gameScreen.setConnectionStatus("Disconnected");
        gameScreen.showReconnectDialog();
    }
    
    @Override
    public void received(Connection connection, Object packet) {
        // This is called on KryoNet's NETWORK THREAD, not the game thread!
        // Be careful with thread safety (see Producer-Consumer pattern)
        
        if (packet instanceof PlayerPosition) {
            PlayerPosition pos = (PlayerPosition) packet;
            gameScreen.updatePlayerPosition(pos.playerId, pos.x, pos.y);
        } 
        else if (packet instanceof ChatMessage) {
            ChatMessage chat = (ChatMessage) packet;
            gameScreen.addChatMessage(chat.senderName + ": " + chat.message);
        }
        else if (packet instanceof GameOver) {
            GameOver over = (GameOver) packet;
            gameScreen.showGameOverScreen(over.winnerId);
        }
    }
}

// Step 2: Register your observer with the subject (KryoNet client)
public class Main {
    public static void main(String[] args) {
        Client kryoClient = new Client();
        GameScreen gameScreen = new GameScreen();
        
        // Subscribe to events
        kryoClient.addListener(new GameClientListener(gameScreen));
        
        // You can add multiple observers!
        kryoClient.addListener(new SoundEffectListener());  // Plays sounds on events
        kryoClient.addListener(new AnalyticsListener());    // Tracks metrics
        kryoClient.addListener(new LoggingListener());      // Logs for debugging
        
        // Start the client - it will call your listeners when events occur
        kryoClient.start();
        kryoClient.connect(5000, "localhost", 27960, 27961);
    }
}
```

#### Multiple Observers Example

One powerful aspect of Observer is that you can have **many observers** for the same events:

```java
// Observer 1: Updates the game display
public class GameDisplayListener extends Listener {
    @Override
    public void received(Connection conn, Object packet) {
        if (packet instanceof PlayerPosition) {
            // Update sprite positions on screen
        }
    }
}

// Observer 2: Plays sound effects (different class, different responsibility)
public class SoundEffectListener extends Listener {
    @Override
    public void received(Connection conn, Object packet) {
        if (packet instanceof PlayerPosition) {
            // Play footstep sounds
        }
        if (packet instanceof ChatMessage) {
            // Play notification ding
        }
    }
}

// Observer 3: Logs events for debugging
public class DebugLogListener extends Listener {
    @Override
    public void received(Connection conn, Object packet) {
        System.out.println("[DEBUG] Received: " + packet.getClass().getSimpleName());
    }
}

// All three get notified of the SAME events
client.addListener(new GameDisplayListener());
client.addListener(new SoundEffectListener());
client.addListener(new DebugLogListener());
```

This follows the **Single Responsibility Principle**: each observer does one thing well.

#### Common Mistake: Blocking the Network Thread

```java
// ❌ BAD: Don't do heavy work in the callback!
@Override
public void received(Connection connection, Object packet) {
    if (packet instanceof LargeDataPacket) {
        // This blocks KryoNet's network thread!
        saveToDatabase(packet);     // Slow I/O operation
        processHugeList(packet);    // CPU-intensive work
        Thread.sleep(1000);         // Never do this!
    }
}

// ✅ GOOD: Store data and process elsewhere
@Override
public void received(Connection connection, Object packet) {
    if (packet instanceof LargeDataPacket) {
        // Quick: just store it in a thread-safe queue
        pendingPackets.add(packet);
        // Process later in your game loop (see Producer-Consumer pattern)
    }
}
```

---

### 2. Command Pattern (Required)

If you've used KryoNet, you've already used this pattern—you just might not have known the name.

Think of a restaurant. When you order food, the waiter writes your order on a slip of paper. That slip is a **command object**—it encapsulates everything needed to fulfill your request (what dish, how cooked, any modifications). The kitchen doesn't need to talk to you directly; it just processes the command slip.

In code terms:
- **Command** — An object that encapsulates a request with all its data
- **Invoker** — The thing that triggers the command (the network)
- **Receiver** — The thing that executes the command (your handler method)

#### Why KryoNet REQUIRES This Pattern

KryoNet's `Listener.received()` method has this signature:

```java
public void received(Connection connection, Object object)
//                                          ^^^^^^
//                       This is just "Object" - could be ANYTHING!
```

The `object` parameter can be *any* class you've registered with Kryo. KryoNet doesn't know or care what type it is—that's your job to figure out. This design **forces** you to use the Command pattern:

```java
// ❌ Without Command pattern - this doesn't work!
public void received(Connection connection, Object object) {
    // What is "object"? Could be LoginRequest, ChatMessage, PlayerPosition...
    // There's no way to know without checking!
    
    // This won't compile - Object has no "playerName" field
    String name = object.playerName;
}

// ✅ With Command pattern - each packet is a self-contained command
public void received(Connection connection, Object object) {
    // Check the type and dispatch to the appropriate handler
    if (object instanceof LoginRequest) {
        LoginRequest cmd = (LoginRequest) object;
        handleLogin(connection, cmd.playerName, cmd.clientVersion);
    } 
    else if (object instanceof PlayerPosition) {
        PlayerPosition cmd = (PlayerPosition) object;
        handlePosition(connection, cmd.playerId, cmd.x, cmd.y);
    }
    else if (object instanceof ChatMessage) {
        ChatMessage cmd = (ChatMessage) object;
        handleChat(connection, cmd.senderName, cmd.message);
    }
}
```

#### Creating Command Classes (Packets)

Each packet class is a command object. Here's the anatomy:

```java
/**
 * Command: Request to log in to the server
 * 
 * Sender:   Client
 * Receiver: Server
 * Protocol: TCP (must arrive reliably)
 */
public class LoginRequest {
    // Data fields - everything needed to execute this command
    public String playerName;      // Who is logging in?
    public String clientVersion;   // What version are they running?
    
    // REQUIRED: No-argument constructor for Kryo deserialization
    public LoginRequest() {}
    
    // Optional: Convenience constructor for sending
    public LoginRequest(String playerName, String clientVersion) {
        this.playerName = playerName;
        this.clientVersion = clientVersion;
    }
    
    // Optional: toString for debugging
    @Override
    public String toString() {
        return "LoginRequest{name='" + playerName + "', version='" + clientVersion + "'}";
    }
}
```

**Why the empty constructor?** When bytes arrive over the network, Kryo needs to:
1. Create an empty object: `new LoginRequest()` (needs no-arg constructor!)
2. Set each field from the byte stream using reflection

Without the empty constructor, Kryo throws `InstantiationException`.

#### Complete Example: Multiple Command Types

```java
// --- Packet classes (each is a "command") ---

public class LoginRequest {
    public String playerName;
    public LoginRequest() {}
}

public class LoginResponse {
    public boolean success;
    public int assignedPlayerId;   // -1 if failed
    public String message;         // "Welcome!" or "Server full"
    public LoginResponse() {}
}

public class PlayerPosition {
    public int playerId;
    public float x, y;
    public long timestamp;
    public PlayerPosition() {}
}

public class ChatMessage {
    public String senderName;
    public String message;
    public long timestamp;
    public ChatMessage() {}
}

// --- Server-side handler ---

public class GameServerListener extends Listener {
    
    @Override
    public void received(Connection connection, Object object) {
        // Dispatch based on command type
        if (object instanceof LoginRequest) {
            handleLogin(connection, (LoginRequest) object);
        } 
        else if (object instanceof PlayerPosition) {
            handlePosition(connection, (PlayerPosition) object);
        } 
        else if (object instanceof ChatMessage) {
            handleChat(connection, (ChatMessage) object);
        }
        // Add more handlers as you add more packet types
    }
    
    private void handleLogin(Connection conn, LoginRequest cmd) {
        System.out.println(cmd.playerName + " is logging in...");
        
        // Validate the command
        if (cmd.playerName == null || cmd.playerName.isEmpty()) {
            conn.sendTCP(new LoginResponse(false, -1, "Name required"));
            return;
        }
        
        // Execute the command
        int playerId = conn.getID();
        players.put(playerId, new Player(cmd.playerName));
        
        // Send response
        conn.sendTCP(new LoginResponse(true, playerId, "Welcome!"));
        
        // Notify others
        server.sendToAllExceptTCP(playerId, new PlayerJoined(playerId, cmd.playerName));
    }
    
    private void handlePosition(Connection conn, PlayerPosition cmd) {
        // Validate: Is this player allowed to send positions?
        // Validate: Are coordinates within bounds?
        // Execute: Update server state
        // Relay: Send to other players
    }
    
    private void handleChat(Connection conn, ChatMessage cmd) {
        // Validate: Check for profanity, length limits, rate limiting
        // Execute: Add timestamp
        // Relay: Broadcast to all players
    }
}
```

#### Benefits of Command Pattern with KryoNet

| Benefit | Explanation |
|---------|-------------|
| **Type safety** | `instanceof` checks are safer than manual type codes |
| **Self-documenting** | Each packet class documents its fields |
| **Easy to extend** | Add new packet = new class + register + handler |
| **Loggable** | You can log packets: `System.out.println(packet)` |
| **Queueable** | You can store packets in a list for later processing |

#### Common Mistake: Forgetting to Register

```java
// You create a shiny new packet class...
public class PowerUp {
    public int type;
    public float x, y;
    public PowerUp() {}
}

// ❌ But forget to register it!
public class PacketRegistry {
    public static void register(Kryo kryo) {
        kryo.register(LoginRequest.class);
        kryo.register(PlayerPosition.class);
        // Oops! PowerUp not registered!
    }
}

// Result: KryoException when sending PowerUp
// "Class is not registered: com.example.packets.PowerUp"
```

**Always add new packets to `PacketRegistry`!**

---

### 3. Registry Pattern (Required)

This is the pattern most likely to bite you if you ignore it.

Imagine a wedding venue's seating chart. Instead of each guest wandering around looking for their seat, there's one master chart at the entrance. Everyone checks the chart, finds their table number, and goes directly there.

The Registry pattern works the same way:
- **Central location** — One place where related configurations are managed
- **Consistent view** — Everyone uses the same source of truth
- **Single point of change** — Need to add something? Update one place, not many.

#### Why Kryo REQUIRES This Pattern

Kryo uses **integer IDs** internally to identify classes. When you register a class, Kryo assigns it the next available ID:

```java
kryo.register(LoginRequest.class);   // Gets ID 0
kryo.register(PlayerPosition.class); // Gets ID 1
kryo.register(ChatMessage.class);    // Gets ID 2
```

When Kryo sends a `LoginRequest` over the network, it doesn't send the class name—it sends `0`. When the receiver gets `0`, it looks up which class has ID `0`.

**Here's the critical problem:**

```java
// SERVER registers packets in this order:
kryo.register(LoginRequest.class);    // ID 0
kryo.register(LoginResponse.class);   // ID 1
kryo.register(PlayerPosition.class);  // ID 2

// CLIENT accidentally registers in different order (copy-paste bug, alphabetical ordering, etc.)
kryo.register(LoginRequest.class);    // ID 0 ✓ (matches)
kryo.register(PlayerPosition.class);  // ID 1 ✗ (server says LoginResponse is 1!)
kryo.register(LoginResponse.class);   // ID 2 ✗ (server says PlayerPosition is 2!)

// RESULT: Server sends LoginResponse (ID=1), client thinks it's PlayerPosition!
// You get ClassCastException, corrupted data, or silent bugs.
```

**The fix: A single Registry class used by BOTH client and server.**

#### Complete Registry Implementation

```java
// PacketRegistry.java - the ONLY place where packets are registered
package com.example.network.packets;

import com.esotericsoftware.kryo.Kryo;

/**
 * Central registry for all packet classes.
 * 
 * CRITICAL: This class MUST be used by both client and server!
 * If client and server register classes in different orders,
 * packets will be misinterpreted (class ID mismatch).
 * 
 * HOW TO ADD A NEW PACKET:
 * 1. Create your packet class in this package
 * 2. Add kryo.register(YourPacket.class) at THE END of this method
 * 3. Never reorder existing registrations!
 */
public class PacketRegistry {
    
    public static void register(Kryo kryo) {
        // Authentication
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
        
        // Player state
        kryo.register(PlayerPosition.class);
        kryo.register(PlayerJoined.class);
        kryo.register(PlayerLeft.class);
        
        // Game events
        kryo.register(ChatMessage.class);
        kryo.register(PingRequest.class);
        kryo.register(PingResponse.class);
        
        // Add new packets at the end!
    }
}
```

#### Using the Registry

```java
public class NetworkClient {
    private final Client kryoClient;
    
    public NetworkClient() {
        this.kryoClient = new Client(16384, 4096);
        PacketRegistry.register(kryoClient.getKryo());  // Same as server
        kryoClient.start();
    }
}

public class NetworkServer {
    private final Server kryoServer;
    
    public NetworkServer() {
        this.kryoServer = new Server(16384, 4096);
        PacketRegistry.register(kryoServer.getKryo());  // Same as client
        kryoServer.start();
    }
}
```

Because both `NetworkClient` and `NetworkServer` call `PacketRegistry.register()`, they will always have identical class ID mappings.

#### What About Just Using a Map/Dictionary?

A common misconception: "Isn't this just a dictionary?"

No. A dictionary **stores and retrieves** data. Our `PacketRegistry`:
- **Configures** an external system (Kryo) 
- **Doesn't store** any data itself
- **Enforces order** (which a HashMap doesn't guarantee)

```java
// ❌ This is a Dictionary pattern - NOT what we're doing
Map<String, Class<?>> packets = new HashMap<>();
packets.put("login", LoginRequest.class);
Class<?> cls = packets.get("login");  // Lookup by key

// ✅ This is the Registry pattern - configuring Kryo
public static void register(Kryo kryo) {
    kryo.register(LoginRequest.class);  // No lookup, just configuration
    // We're telling Kryo "this class exists and should have the next ID"
}
```

#### Common Mistakes

```java
// ❌ MISTAKE 1: Registering packets in both client AND server code separately
// File: DesktopLauncher.java
kryo.register(LoginRequest.class);

// File: ServerLauncher.java  
kryo.register(LoginRequest.class);
// These might get out of sync!

// ❌ MISTAKE 2: Adding a packet and forgetting to register it
public class DamagePacket {
    public int damage;
    public DamagePacket() {}
}
// Oops! Never added to PacketRegistry!
// Result: KryoException("Class is not registered: DamagePacket")

// ❌ MISTAKE 3: Inserting a new packet in the middle
public static void register(Kryo kryo) {
    kryo.register(LoginRequest.class);    // ID 0
    kryo.register(NewPacket.class);       // ID 1 - WRONG! Breaks existing!
    kryo.register(LoginResponse.class);   // ID 2 - Was 1, now different!
}
// ✅ CORRECT: Always add at the END
```

---

### 4. Producer-Consumer Pattern (Required for Games)

If your game freezes or shows corrupted data, this is probably why.

Imagine a restaurant kitchen:
- **Waiters (producers)** bring order slips and put them on a rack
- **Chefs (consumers)** take slips from the rack and cook the orders
- **The rack (buffer)** decouples waiters from chefs—they don't need to talk directly

Neither blocks the other:
- Waiters don't wait for chefs to finish cooking before taking new orders
- Chefs cook at their own pace, grabbing orders when ready

#### Why KryoNet Games REQUIRE This Pattern

KryoNet has its own **network thread** that runs separately from your game's **render thread**:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    YOUR GAME APPLICATION                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌──────────────────────┐        ┌──────────────────────┐         │
│   │   NETWORK THREAD     │        │   RENDER THREAD      │         │
│   │   (KryoNet)          │        │   (LibGDX/Game Loop) │         │
│   ├──────────────────────┤        ├──────────────────────┤         │
│   │ • Polls socket       │        │ • Runs at 60 FPS     │         │
│   │ • Deserializes bytes │        │ • Processes input    │         │
│   │ • Calls listeners    │        │ • Updates game logic │         │
│   │ • Runs continuously  │        │ • Renders graphics   │         │
│   └──────────┬───────────┘        └──────────┬───────────┘         │
│              │                               │                      │
│              │     ┌─────────────────────┐   │                      │
│              └────▶│  SHARED BUFFER      │◀──┘                      │
│                    │  (ConcurrentHashMap)│                          │
│                    │                     │                          │
│   WRITE ──────────▶│  playerId → {x, y} │────────────▶ READ        │
│   (network thread) │  playerId → {x, y} │   (render thread)        │
│                    └─────────────────────┘                          │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

If you try to share data without synchronization:

```java
// ❌ WRONG: Regular HashMap is NOT thread-safe
private Map<Integer, RemotePlayer> remotePlayers = new HashMap<>();

// Network thread (KryoNet callback)
@Override
public void received(Connection conn, Object packet) {
    if (packet instanceof PlayerPosition) {
        PlayerPosition pos = (PlayerPosition) packet;
        // Writing to HashMap from network thread
        remotePlayers.put(pos.playerId, new RemotePlayer(pos.x, pos.y));
    }
}

// Render thread (LibGDX)
@Override
public void render() {
    // Reading from HashMap on render thread - RACE CONDITION!
    for (RemotePlayer player : remotePlayers.values()) {
        draw(player);  // May see partially updated data, or crash!
    }
}
```

#### Complete Producer-Consumer Implementation

```java
// The shared buffer: thread-safe collections
public class GameClientScreen extends ApplicationAdapter {
    
    // Thread-safe - can be written by network thread, read by render thread
    private final ConcurrentHashMap<Integer, RemotePlayer> remotePlayers 
        = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<String> chatMessages 
        = new CopyOnWriteArrayList<>();
    
    // Local state (only touched by render thread, no sync needed)
    private float localX = 400, localY = 300;
    private int localPlayerId = -1;
}

// The producer: network thread writes to the buffer
public class GameNetworkListener extends Listener {
    private final GameClientScreen screen;
    
    public GameNetworkListener(GameClientScreen screen) {
        this.screen = screen;
    }
    
    @Override
    public void received(Connection connection, Object packet) {
        // This runs on NETWORK thread - keep it fast!
        if (packet instanceof PlayerPosition) {
            PlayerPosition pos = (PlayerPosition) packet;
            screen.remotePlayers.put(pos.playerId, 
                new RemotePlayer(pos.playerId, pos.x, pos.y));
        }
        else if (packet instanceof ChatMessage) {
            ChatMessage chat = (ChatMessage) packet;
            screen.chatMessages.add(chat.senderName + ": " + chat.message);
        }
        else if (packet instanceof PlayerLeft) {
            PlayerLeft left = (PlayerLeft) packet;
            screen.remotePlayers.remove(left.playerId);
        }
    }
}

// The consumer: render thread reads from the buffer
public class GameClientScreen extends ApplicationAdapter {
    
    @Override
    public void render() {
        // This runs on RENDER thread at 60 FPS
        
        // Draw remote players (red) - reading from the buffer
        // ConcurrentHashMap's iterator is thread-safe ("weakly consistent")
        shapeRenderer.setColor(Color.RED);
        for (RemotePlayer player : remotePlayers.values()) {
            shapeRenderer.circle(player.x, player.y, 20);
        }
        
        shapeRenderer.end();
        
        // Draw chat messages - reading from thread-safe list
        spriteBatch.begin();
        int y = 100;
        for (String message : chatMessages) {
            font.draw(spriteBatch, message, 10, y);
            y += 20;
        }
        spriteBatch.end();
    }
}
```

#### Why ConcurrentHashMap Works Here

| Operation | Thread | What Happens |
|-----------|--------|--------------|
| `remotePlayers.put(id, player)` | Network | Thread-safe write; doesn't block render |
| `remotePlayers.values()` | Render | Returns a "weakly consistent" view |
| `for (player : remotePlayers.values())` | Render | Sees a consistent snapshot; won't crash |

"Weakly consistent" means the iterator won't throw `ConcurrentModificationException` and will reflect *some* state of the map (perhaps not the absolute latest write, but a valid state).

For a game, this is perfect—if the render thread misses one frame's worth of position updates, you won't even notice. The next frame will catch up.

#### Why NOT Use `synchronized`?

```java
// ❌ SLOWER APPROACH: Explicit synchronization
private Map<Integer, RemotePlayer> remotePlayers = new HashMap<>();

// Network thread
public void received(...) {
    synchronized(remotePlayers) {         // Blocks if render is reading!
        remotePlayers.put(id, player);
    }
}

// Render thread
public void render() {
    synchronized(remotePlayers) {         // Blocks if network is writing!
        for (RemotePlayer p : remotePlayers.values()) {
            draw(p);
        }
    }
}
// Result: Threads block each other, causing lag spikes
```

With `ConcurrentHashMap`:
- Writers don't block readers
- Multiple writers can work simultaneously (on different keys)
- No explicit `synchronized` blocks needed

---

### 5. Connector Pattern (Recommended)

KryoNet gives you both TCP and UDP on the same connection. The Connector pattern helps you use them wisely.

In software architecture, a **Connector** is an abstraction that handles communication between components. Instead of components talking directly to each other, they communicate through a connector that:

- **Hides protocol details** — Components don't care if you're using TCP, UDP, HTTP, or carrier pigeons
- **Encapsulates connection logic** — Retry logic, buffering, error handling
- **Allows protocol switching** — Swap TCP for WebSockets without changing application code

Think of it like a universal power adapter—you plug your device into the adapter, and it handles converting to the local socket type.

#### Why KryoNet Benefits from Connectors

KryoNet supports **both TCP and UDP** on the same connection. This is powerful but requires careful thought about when to use each:

```java
// Without Connector thinking - scattered protocol decisions
public void sendPosition(float x, float y) {
    PlayerPosition pos = new PlayerPosition(localPlayerId, x, y);
    client.sendUDP(pos);  // Why UDP? It's buried in game code.
}

public void sendChat(String message) {
    ChatMessage chat = new ChatMessage(playerName, message);
    client.sendTCP(chat);  // Why TCP? Also buried in game code.
}

public void sendLogin(String name) {
    LoginRequest login = new LoginRequest(name);
    client.sendTCP(login);  // Protocol choice scattered everywhere!
}
```

With the Connector pattern, protocol selection is centralized:

```java
public class NetworkClient {
    private final Client kryoClient;
    
    /** Send via TCP - for things that MUST arrive (login, chat, scores) */
    public void sendReliable(Object packet) {
        kryoClient.sendTCP(packet);
    }
    
    /** Send via UDP - for frequent updates where speed beats reliability */
    public void sendFast(Object packet) {
        kryoClient.sendUDP(packet);
    }
}
```

#### Complete Connector Implementation

```java
public class NetworkClient {
    private final Client kryoClient;
    
    /** Send packet via TCP. Use for: login, chat, game events, scores. */
    public void sendReliable(Object packet) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server");
        }
        kryoClient.sendTCP(packet);
    }
    
    /** Send packet via UDP. Use for: positions, velocities, animations. */
    public void sendFast(Object packet) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server");
        }
        kryoClient.sendUDP(packet);
    }
    
    // High-level API hides protocol choice from game code
    
    public void login(String playerName) {
        sendReliable(new LoginRequest(playerName));
    }
    
    public void updatePosition(float x, float y) {
        sendFast(new PlayerPosition(localPlayerId, x, y, System.currentTimeMillis()));
    }
    
    public void sendChat(String message) {
        sendReliable(new ChatMessage(playerName, message, System.currentTimeMillis()));
    }
}
```

#### When to Use Each Channel

| Channel | Protocol | Use For | Why |
|---------|----------|---------|-----|
| **Reliable** | TCP | Login, logout, chat, scores, game over | Must arrive; order matters |
| **Fast** | UDP | Positions, velocities, animations | Speed > reliability; old data is stale |

```java
// Game code uses semantic methods - doesn't care about TCP vs UDP
networkClient.updatePosition(newX, newY);    // UDP internally
networkClient.sendChat("Hello everyone!");   // TCP internally
networkClient.login(playerName);             // TCP internally
```

#### Connector vs Facade: What's the Difference?

These patterns work together but serve different purposes:

| Aspect | Connector | Facade |
|--------|-----------|--------|
| **Focus** | Communication protocols | API simplification |
| **Abstracts** | How data travels | Complex subsystem internals |
| **Example** | `sendReliable()` vs `sendFast()` | `NetworkClient` wrapping KryoNet |

Our `NetworkClient` is **both**:
- **Facade**: Hides KryoNet's complexity (buffer sizes, threading, Kryo registration)
- **Connector**: Abstracts protocol choice (TCP vs UDP)

---

### 6. Facade Pattern (Recommended)

KryoNet's raw API works, but it's verbose. A facade makes your life easier.

Imagine buying coffee at a café. You say "large latte please" and get a drink. But behind the counter:
- Grind the beans
- Dose espresso into portafilter
- Tamp and lock into machine
- Pull shot for 25 seconds
- Steam milk to 65°C
- Pour in specific pattern
- Add lid if to-go

The barista is a **facade**—a simplified interface to a complex process.

In code:
- **Facade** — A class that provides a simple API to a complex subsystem
- **Subsystem** — Multiple classes/components that work together
- **Client** — Code that uses the facade instead of the subsystem directly

#### Why KryoNet Benefits from a Facade

Using KryoNet directly requires many steps:

```java
// ❌ WITHOUT FACADE: Raw KryoNet usage (verbose!)
Client client = new Client(16384, 4096);  // Magic numbers!

// Register packets (required, easy to forget)
Kryo kryo = client.getKryo();
kryo.register(LoginRequest.class);
kryo.register(LoginResponse.class);
kryo.register(PlayerPosition.class);
// ... don't forget any!

// Start background thread (required before connect)
client.start();

// Connect with timeout
try {
    client.connect(5000, "192.168.1.100", 27960, 27961);
} catch (IOException e) {
    // Handle connection failure
}

// Add listener (must extend Listener class)
client.addListener(new Listener() {
    @Override
    public void received(Connection connection, Object object) {
        // Handle packets
    }
    @Override 
    public void disconnected(Connection connection) {
        // Handle disconnection  
    }
});

// Track connection state yourself
boolean isConnected = client.isConnected();
```

With a facade:

```java
// ✅ WITH FACADE: Clean and simple
NetworkClient client = new NetworkClient();
client.connect("192.168.1.100");
client.addListener(myGameListener);
client.sendReliable(new LoginRequest("PlayerName"));
```

#### Complete Facade Implementation

```java
package com.example.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.example.network.packets.PacketRegistry;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Facade over KryoNet's Client class.
 * 
 * Simplifies network client usage by:
 * - Handling buffer size configuration
 * - Automating packet registration via PacketRegistry
 * - Starting the network thread automatically
 * - Providing a simpler listener interface
 * - Managing connection state
 */
public class NetworkClient {
    
    private final Client kryoClient;
    private final List<NetworkListener> listeners = new CopyOnWriteArrayList<>();
    
    public NetworkClient() {
        // Create with reasonable buffer sizes
        this.kryoClient = new Client(
            NetworkConfig.WRITE_BUFFER_SIZE,
            NetworkConfig.OBJECT_BUFFER_SIZE
        );
        
        // Register all packet types
        PacketRegistry.register(kryoClient.getKryo());
        
        // Bridge KryoNet's Listener to our simpler NetworkListener
        kryoClient.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                for (NetworkListener listener : listeners) {
                    listener.onConnected();
                }
            }
            
            @Override
            public void disconnected(Connection connection) {
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
        
        kryoClient.start();
    }
    
    /** Connect using just the hostname. Uses defaults for ports and timeout. */
    public boolean connect(String host) {
        try {
            kryoClient.connect(
                NetworkConfig.CONNECTION_TIMEOUT,
                host,
                NetworkConfig.TCP_PORT,
                NetworkConfig.UDP_PORT
            );
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
    
    public void addListener(NetworkListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }
    
    public void sendReliable(Object packet) {
        kryoClient.sendTCP(packet);
    }
    
    public void sendFast(Object packet) {
        kryoClient.sendUDP(packet);
    }
    
    public boolean isConnected() {
        return kryoClient.isConnected();
    }
    
    public void disconnect() {
        kryoClient.close();
    }
}
```

#### The Simplified Listener Interface

```java
/**
 * Simplified listener interface for network events.
 * 
 * Unlike KryoNet's Listener class (which has many methods and requires
 * Connection handling), this interface focuses on what game code cares about.
 */
public interface NetworkListener {
    void onConnected();
    void onDisconnected();
    void onReceived(Object packet);
}
```

#### Benefits of the Facade

| Without Facade | With Facade |
|----------------|-------------|
| Know buffer sizes | Just `new NetworkClient()` |
| Register each packet | Automatic via PacketRegistry |
| Call `start()` before `connect()` | Handled in constructor |
| Handle IOException | Returns boolean |
| Extend `Listener` class | Implement simple interface |
| Track connection state | `isConnected()` method |

**The facade doesn't add functionality—it makes existing functionality easier to use.**

---

### Pattern Summary: How They Work Together

Here's the complete flow of patterns when a player sends their position:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  CLIENT SENDS POSITION                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. FACADE: networkClient.updatePosition(x, y)                              │
│     └── Simplifies: No buffers, ports, or raw KryoNet calls                 │
│                                                                             │
│  2. CONNECTOR: sendFast() → UDP (not TCP)                                   │
│     └── Abstracts: Game code doesn't decide protocol                        │
│                                                                             │
│  3. COMMAND: new PlayerPosition(id, x, y) packet created                    │
│     └── Encapsulates: All data needed for this request                      │
│                                                                             │
│  4. REGISTRY: Kryo looks up PlayerPosition → ID 2                           │
│     └── Ensures: Same ID on client and server                               │
│                                                                             │
│  ════════════════════ NETWORK (UDP) ════════════════════                    │
│                                                                             │
│  5. REGISTRY: Server Kryo sees ID 2 → PlayerPosition.class                  │
│     └── Deserializes: Bytes back to object                                  │
│                                                                             │
│  6. OBSERVER: listener.received(conn, playerPosObject)                      │
│     └── Notifies: All registered listeners                                  │
│                                                                             │
│  7. COMMAND: if (object instanceof PlayerPosition) handlePosition()         │
│     └── Dispatches: To appropriate handler                                  │
│                                                                             │
│  8. PRODUCER-CONSUMER: remotePlayers.put(id, pos) // network thread         │
│                        render() reads remotePlayers  // render thread       │
│     └── Decouples: Network from rendering                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Quick Reference: Which Pattern to Use When

| Situation | Pattern | Example |
|-----------|---------|---------|
| Receiving network events | Observer | `client.addListener(myListener)` |
| Sending typed messages | Command | `new ChatMessage("Hi!")` packet |
| Registering packet classes | Registry | `PacketRegistry.register(kryo)` |
| Network thread → game thread | Producer-Consumer | `ConcurrentHashMap` |
| TCP vs UDP selection | Connector | `sendReliable()` vs `sendFast()` |
| Simplifying KryoNet API | Facade | `NetworkClient` wrapper |

---

## Chat System

**Packet:** `ChatMessage` in [core/network/packets/ChatMessage.java](core/src/main/java/com/example/network/packets/ChatMessage.java)
**Protocol:** TCP (reliable delivery required for chat)
**Flow:** Client → Server → All Clients (broadcast)
**Sending:** `client.sendTCP(chatMessage)` from desktop/android client
**Receiving:** `GameServer.handleChat(connection, message)` validates sender, sets timestamp, broadcasts via `networkServer.sendToAllTCP(message)`

---

## Movement System

**Packet:** `PlayerPosition` in [core/network/packets/PlayerPosition.java](core/src/main/java/com/example/network/packets/PlayerPosition.java)
**Protocol:** UDP (speed over reliability for real-time updates)
**Flow:** Client → Server → All Other Clients (excludes sender)
**Sending:** `networkClient.sendUDP(pos)` at fixed rate (`NetworkConfig.POSITION_UPDATE_RATE`)
**Receiving:** `GameServer.handlePosition(connection, position)` stores position server-side, broadcasts via `networkServer.sendToAllExceptUDP(senderId, position)`

---

## Thread Synchronization

**Problem:** KryoNet receives packets on its network thread, but LibGDX renders on the main/GL thread.
**Solution:** Thread-safe collections bridge the two threads without explicit locking.
**Server storage:** `ConcurrentHashMap<Integer, PlayerConnection>` in [core/network/NetworkServer.java](core/src/main/java/com/example/network/NetworkServer.java)
**Client storage:** `ConcurrentHashMap<Integer, RemotePlayer>` in [desktop/GameClientScreen.java](desktop/src/main/java/com/example/desktop/GameClientScreen.java)
**Write (network thread):** `remotePlayers.put(playerId, new RemotePlayer(...))` when packets arrive
**Read (render thread):** Iterate `remotePlayers` during `render()` to draw all players
**Why it works:** `ConcurrentHashMap` allows simultaneous reads/writes without blocking. Chat uses `CopyOnWriteArrayList` for the same reason.

### KryoNet's Threading Model

KryoNet spawns a dedicated **update thread** when you call `client.start()` or `server.start()`. This thread:
- Polls the network socket using Java NIO selectors
- Deserializes incoming bytes into packet objects via Kryo
- Invokes your `Listener.received()` callback **on the network thread**

This means your listener code runs concurrently with your game's render loop. If both threads access shared state (like player positions), you need synchronization.

### Thread-Safe Collections (Producer-Consumer Implementation)

This section implements the [Producer-Consumer Pattern](#4-producer-consumer-pattern-required-for-games) described above. Without thread-safe collections, the render thread might read a partially-written object or miss updates entirely.

### Thread-Safe Collections Used

| Collection | Location | Why |
|------------|----------|-----|
| `ConcurrentHashMap<Integer, PlayerConnection>` | NetworkServer | Multiple clients connect/disconnect while server iterates |
| `ConcurrentHashMap<Integer, RemotePlayer>` | GameClientScreen | Position updates arrive while rendering |
| `CopyOnWriteArrayList<NetworkListener>` | NetworkClient | Listeners may be added/removed during callbacks |
| `CopyOnWriteArrayList<String>` | GameClientScreen (chat) | Messages arrive while rendering chat log |

**Why not `synchronized`?** Explicit locks block threads. `ConcurrentHashMap` uses lock striping internally—different keys can be written simultaneously, and reads never block. For a game running at 60 FPS, this matters.

### The Listener Callback Model

KryoNet uses the Observer pattern for thread communication. When a packet arrives:

1. KryoNet's update thread deserializes the packet
2. It calls `listener.received(connection, object)` on all registered listeners
3. Your listener writes to a thread-safe collection
4. Your render loop reads from that collection on the next frame

This decouples network I/O from rendering—packets are processed as fast as they arrive, while rendering proceeds at its own pace.

---

## Framework Comparison for Game Server Setup

### KryoNet

[GitHub Repository](https://github.com/EsotericSoftware/kryonet)

| Pros | Cons |
|------|------|
| Made for LibGDX games | Java only |
| Very simple API | Last updated 2018 (but stable) |
| TCP + UDP built-in | No matchmaking or rooms |
| Fast Kryo serialization | Limited documentation |
| Works on Android | |

**Best for:** Prototypes, LibGDX games, learning networking basics

### Netty

[Official Website](https://netty.io/) · [GitHub Repository](https://github.com/netty/netty)

| Pros | Cons |
|------|------|
| Industry standard (used by Minecraft) | Steep learning curve |
| Extremely high performance | Verbose boilerplate code |
| Actively maintained | You handle serialization yourself |
| Excellent documentation | Overkill for simple games |

**Best for:** Production servers, when you need maximum control

### Socket.IO (Java Client)

[Official Website](https://socket.io/) · [Java Client GitHub](https://github.com/socketio/socket.io-client-java)

| Pros | Cons |
|------|------|
| Works with web browsers | TCP only (higher latency) |
| Built-in rooms and namespaces | Extra server dependency |
| Easy cross-platform support | Not ideal for real-time action |
| Good for chat features | |

**Best for:** Turn-based games, chat-heavy games, games with web clients

### WebSockets

[MDN WebSocket Guide](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API) · [Java-WebSocket Library](https://github.com/TooTallNate/Java-WebSocket)

| Pros | Cons |
|------|------|
| Browser compatible | TCP only |
| Standard protocol | Manual message handling |
| Works almost everywhere | No built-in serialization |

**Best for:** Browser games, maximum compatibility

### Recommendation for TDT4240

**Use KryoNet** if you're building a real-time LibGDX game and want to focus on gameplay rather than networking infrastructure. It's the fastest path to a working multiplayer prototype.
**Consider Netty** if you want to learn industry tools or need production-grade performance, but budget extra time for the learning curve.

---

## Common Issues

### "Address already in use"

This means another server instance is already running on the same port. Solutions:

1. **Check your terminals.** Look for existing "Run: ServerLauncher" terminals at the bottom of VS Code and close them.
2. **Kill Java processes:**
   ```powershell
   taskkill /F /IM java.exe
   ```
3. **Restart VS Code.** This closes all terminals and their processes.

### VS Code doesn't show run configurations

1. Make sure **Extension Pack for Java** is installed (`Ctrl+Shift+X`, search for it)
2. Run `Ctrl+Shift+P` → "Java: Clean Java Language Server Workspace"
3. Run `Ctrl+Shift+P` → "Developer: Reload Window"

### Client can't connect to server

1. Start the server first, wait for "Server started" message
2. Check Windows Firewall isn't blocking Java
3. Make sure you're connecting to `localhost` (or the correct IP for remote servers)

### Packets not being received

1. Verify packet registration order is identical in `PacketRegistry.java` on both sides
2. Make sure all packets have a no-argument constructor
3. Check that packet fields are `public`

---

## Extending This Project

### Adding a New Packet Type

1. Create the packet class in `core/src/main/java/com/example/network/packets/`:

```java
public class PlayerDamage {
    public int targetId;
    public int damage;
    public int attackerId;
    
    public PlayerDamage() {} // Required by Kryo!
}
```

2. Register it in `PacketRegistry.java` (add at the end to maintain order):

```java
kryo.register(PlayerDamage.class);
```

3. Handle it in your server/client:

```java
if (packet instanceof PlayerDamage) {
    PlayerDamage dmg = (PlayerDamage) packet;
    players.get(dmg.targetId).takeDamage(dmg.damage);
}
```

---

## Resources

### Libraries Used
- [KryoNet GitHub](https://github.com/EsotericSoftware/kryonet) — Source code and examples
- [Kryo Serialization](https://github.com/EsotericSoftware/kryo) — How the serializer works
- [LibGDX Wiki](https://libgdx.com/wiki/) — LibGDX documentation

### Learning
- [Refactoring Guru: Design Patterns](https://refactoring.guru/design-patterns) — Comprehensive pattern catalog with examples
- [Game Networking Articles](https://gafferongames.com/) — In depth networking concepts by Glenn Fiedler
- [Java Concurrency in Practice](https://jcip.net/) — The definitive guide to thread safety (book)

### Reference
- [ConcurrentHashMap Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html) — Thread safe map used in this project
- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html) — Build tool documentation
- [Java NIO Tutorial](https://docs.oracle.com/javase/tutorial/essential/io/fileio.html) — Non blocking I/O that KryoNet uses
