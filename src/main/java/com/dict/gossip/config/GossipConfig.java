package com.dict.gossip.config;

/**
 * Gossip 协议配置
 */
public class GossipConfig {

    /** 每次 gossip 选择的邻居节点数量 */
    private int fanout = 3;

    /** gossip 间隔（毫秒） */
    private long gossipIntervalMs = 1000;

    /** 本节点监听端口 */
    private int port = 8080;

    public int getFanout() {
        return fanout;
    }

    public void setFanout(int fanout) {
        this.fanout = fanout;
    }

    public long getGossipIntervalMs() {
        return gossipIntervalMs;
    }

    public void setGossipIntervalMs(long gossipIntervalMs) {
        this.gossipIntervalMs = gossipIntervalMs;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
