package com.dict.gossip.rumor;

import com.dict.gossip.model.Node;
import com.dict.gossip.protocol.PeerSelector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 谣言传播协议默认实现
 */
public class DefaultRumorSpreader implements RumorSpreader {

    private final Node self;
    private final RumorStore store;
    private final PeerSelector peerSelector;
    private final RumorTransport transport;
    private final int fanout;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private RumorListener listener;

    public DefaultRumorSpreader(Node self, RumorStore store, PeerSelector peerSelector,
                               RumorTransport transport, int fanout) {
        this.self = self;
        this.store = store;
        this.peerSelector = peerSelector;
        this.transport = transport;
        this.fanout = fanout;
    }

    @Override
    public void inject(String content) {
        Rumor rumor = new Rumor(UUID.randomUUID().toString(), content, self.getId(), System.currentTimeMillis());
        store.onReceive(rumor, self.getId());
        spreadRumors();
    }

    @Override
    public void start() {
        running.set(true);
        new Thread(this::spreadLoop, "rumor-" + self.getId()).start();
    }

    @Override
    public void stop() {
        running.set(false);
    }

    private void spreadLoop() {
        while (running.get()) {
            spreadRumors();
            sleep(1000);
        }
    }

    private void spreadRumors() {
        List<Rumor> active = store.getActiveRumors();
        if (active.isEmpty()) return;

        List<Node> peers = peerSelector.selectPeers(self, fanout);
        if (peers.isEmpty()) return;

        for (Rumor rumor : active) {
            for (Node peer : peers) {
                RumorMessage msg = new RumorMessage(self.getId(), rumor);
                transport.send(peer, msg);
            }
        }
    }

    @Override
    public void onReceive(RumorMessage msg) {
        if (msg.getRumor() == null) return;
        if (msg.getSenderId().equals(self.getId())) return;

        Rumor rumor = msg.getRumor();
        boolean isNew = store.onReceive(rumor, msg.getSenderId());
        if (isNew && listener != null) {
            listener.onNewRumor(rumor);
        }
    }

    @Override
    public void setRumorListener(RumorListener listener) {
        this.listener = listener;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
