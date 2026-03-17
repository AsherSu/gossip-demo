package com.dict.gossip.model;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Gossip 消息体
 */
public class GossipMessage {

    private static final Gson GSON = new Gson();

    private String senderId;
    private String senderAddress;
    private Map<String, NodeState.StateEntry> state;
    private long timestamp;

    public GossipMessage() {}

    public GossipMessage(String senderId, String senderAddress,
                         Map<String, NodeState.StateEntry> state, long timestamp) {
        this.senderId = senderId;
        this.senderAddress = senderAddress;
        this.state = state;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public Map<String, NodeState.StateEntry> getState() {
        return state;
    }

    public void setState(Map<String, NodeState.StateEntry> state) {
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 序列化为 JSON 字节（可替换为 Protobuf 等） */
    public byte[] serialize() {
        return GSON.toJson(this).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /** 从 JSON 字节反序列化 */
    public static GossipMessage deserialize(byte[] bytes) {
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        return GSON.fromJson(json, GossipMessage.class);
    }
}
