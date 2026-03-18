package com.dict.gossip.protocol;

import com.dict.gossip.config.GossipConfig;
import com.dict.gossip.model.GossipMessage;
import com.dict.gossip.model.Node;
import com.dict.gossip.model.NodeState;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gossip 协议核心逻辑
 */
public class GossipProtocol {

    private final Node self;
    private final NodeState state;
    private final GossipConfig config;
    private final PeerSelector peerSelector;
    private final GossipTransport transport;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public GossipProtocol(Node self, NodeState state, GossipConfig config,
                          PeerSelector peerSelector, GossipTransport transport) {
        this.self = self;
        this.state = state;
        this.config = config;
        this.peerSelector = peerSelector;
        this.transport = transport;
    }

    /** 启动 gossip 循环 */
    public void start() {
        running.set(true);
        new Thread(this::gossipLoop, "gossip-" + self.getId()).start();
    }

    public void stop() {
        running.set(false);
    }

    /** 主 gossip 循环 */
    private void gossipLoop() {
        while (running.get()) {
            try {
                doGossip();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sleep(config.getGossipIntervalMs());
        }
    }

    /** 执行一轮 gossip 反熵 */
    private void doGossip() {
        List<Node> peers = peerSelector.selectPeers(self, config.getFanout());
        GossipMessage msg = new GossipMessage(
                self.getId(),
                self.getAddress(),
                state.snapshot(),
                System.currentTimeMillis()
        );
        for (Node peer : peers) {
            sendGossip(peer, msg);
        }
    }

    /** 向目标节点发送 gossip 消息 */
    private void sendGossip(Node target, GossipMessage msg) {
        transport.send(target, msg);
    }

    /** 接收并处理来自其他节点的 gossip（由 transport 回调） */
    public void onReceive(GossipMessage msg) {
        if (msg.getSenderId().equals(self.getId())) {
            return;
        }
        if (msg.getState() != null && !msg.getState().isEmpty()) {
            state.merge(msg.getState());
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
