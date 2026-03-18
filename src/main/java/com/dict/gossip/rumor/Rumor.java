package com.dict.gossip.rumor;

/**
 * 谣言（待传播的消息）
 */
public class Rumor {

    private String id;
    private String content;
    private String originatorId;
    private long timestamp;

    public Rumor() {}

    public Rumor(String id, String content, String originatorId, long timestamp) {
        this.id = id;
        this.content = content;
        this.originatorId = originatorId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public String getOriginatorId() { return originatorId; }
    public long getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setOriginatorId(String originatorId) { this.originatorId = originatorId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
