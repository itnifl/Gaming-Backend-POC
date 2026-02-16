package com.example.network.packets;

/**
 * Client -> Server: Ping request for latency measurement.
 */
public class Ping {
    
    /** Timestamp when ping was sent */
    public long timestamp;
    
    /** Sequence number for matching responses */
    public int sequence;
    
    /** Required for Kryo serialization */
    public Ping() {
    }
    
    public Ping(int sequence) {
        this.timestamp = System.currentTimeMillis();
        this.sequence = sequence;
    }
    
    @Override
    public String toString() {
        return "Ping{sequence=" + sequence + "}";
    }
}
