package com.dict.gossip.rumor;

import com.dict.gossip.model.Node;
import com.dict.gossip.protocol.PeerSelector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 谣言传播协议
 * 收到谣言后继续向邻居传播，直到谣言"变老"（接收次数达阈值）后停止
 */
public class RumorSpreader {

    private final Node self;
    private final RumorStore store;
    private final PeerSelector peerSelector;
    private final RumorTransport transport;
    private final int fanout;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public RumorSpreader(Node self, RumorStore store, PeerSelector peerSelector,
                         RumorTransport transport, int fanout) {
        this.self = self;
        this.store = store;
        this.peerSelector = peerSelector;
        this.transport = transport;
        this.fanout = fanout;
    }

    /**
     * 注入一条新谣言（由本节点产生，开始传播）
     */
    public void inject(String content) {
        Rumor rumor = new Rumor(UUID.randomUUID().toString(), content, self.getId(), System.currentTimeMillis());
        store.onReceive(rumor, self.getId());
        spreadRumors();
    }

    /** 启动谣言传播循环 */
    public void start() {
        running.set(true);
        new Thread(this::spreadLoop, "rumor-" + self.getId()).start();
    }

    public void stop() {
        running.set(false);
    }

    private void spreadLoop() {
        while (running.get()) {
            spreadRumors();
            sleep(1000);
        }
    }

    /** 传播一轮：将未变老的谣言发给随机邻居 */
    private void spreadRumors() {
        List<Rumor> active = store.getActiveRumors();
        if (active.isEmpty()) return;

        List<Node> peers = peerSelector.selectPeers(self, fanout);
        if (peers.isEmpty()) return;

        for (Rumor rumor : active) {
            for (Node peer : peers) {
                sendRumor(peer, rumor);
            }
        }
    }

    private void sendRumor(Node target, Rumor rumor) {
        RumorMessage msg = new RumorMessage(self.getId(), rumor);
        transport.send(target, msg);
    }

    /** 接收来自其他节点的谣言 */
    public void onReceive(RumorMessage msg) {
        if (msg.getRumor() == null) return;
        if (msg.getSenderId().equals(self.getId())) return;

        Rumor rumor = msg.getRumor();
        boolean isNew = store.onReceive(rumor, msg.getSenderId());
        if (isNew && listener != null) {
            listener.onNewRumor(rumor);
        }
    }

    private RumorListener listener;

    /** 设置新谣言到达回调 */
    public void setRumorListener(RumorListener listener) {
        this.listener = listener;
    }

    /** 新谣言到达时回调 */
    @FunctionalInterface
    public interface RumorListener {
        void onNewRumor(Rumor rumor);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
