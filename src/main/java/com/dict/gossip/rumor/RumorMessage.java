package com.dict.gossip.rumor;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

/**
 * 谣言传播消息体
 */
public class RumorMessage {

    private static final Gson GSON = new Gson();

    private String senderId;
    private Rumor rumor;

    public RumorMessage() {}

    public RumorMessage(String senderId, Rumor rumor) {
        this.senderId = senderId;
        this.rumor = rumor;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Rumor getRumor() {
        return rumor;
    }

    public void setRumor(Rumor rumor) {
        this.rumor = rumor;
    }

    public byte[] serialize() {
        return GSON.toJson(this).getBytes(StandardCharsets.UTF_8);
    }

    public static RumorMessage deserialize(byte[] bytes) {
        String json = new String(bytes, StandardCharsets.UTF_8);
        return GSON.fromJson(json, RumorMessage.class);
    }
}
