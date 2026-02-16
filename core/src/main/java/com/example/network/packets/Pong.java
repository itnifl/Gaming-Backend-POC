package com.example.network.packets;

/**
 * Server -> Client: Pong response for latency measurement.
 */
public class Pong {
    
    /** Original timestamp from the ping */
    public long originalTimestamp;
    
    /** Server timestamp when pong was sent */
    public long serverTimestamp;
    
    /** Sequence number matching the ping */
    public int sequence;
    
    /** Required for Kryo serialization */
    public Pong() {
    }
    
    public Pong(Ping ping) {
        this.originalTimestamp = ping.timestamp;
        this.serverTimestamp = System.currentTimeMillis();
        this.sequence = ping.sequence;
    }
    
    /**
     * Calculate round-trip time in milliseconds.
     */
    public long getRoundTripTime() {
        return System.currentTimeMillis() - originalTimestamp;
    }
    
    @Override
    public String toString() {
        return "Pong{sequence=" + sequence + ", rtt=" + getRoundTripTime() + "ms}";
    }
}
