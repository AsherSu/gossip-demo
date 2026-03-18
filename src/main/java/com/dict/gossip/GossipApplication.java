package com.dict.gossip;

import com.dict.gossip.config.GossipConfig;
import com.dict.gossip.model.Node;
import com.dict.gossip.model.NodeState;
import com.dict.gossip.cluster.GossipCluster;
import com.dict.gossip.protocol.DefaultGossipProtocol;
import com.dict.gossip.protocol.GossipProtocol;
import com.dict.gossip.protocol.RandomPeerSelector;
import com.dict.gossip.rumor.DefaultRumorSpreader;
import com.dict.gossip.rumor.HttpRumorTransport;
import com.dict.gossip.rumor.RumorSpreader;
import com.dict.gossip.rumor.RumorStore;
import com.dict.gossip.server.HttpGossipServer;
import com.dict.gossip.transport.HttpGossipTransport;

/**
 * Gossip 应用入口（示例）
 */
public class GossipApplication {

    public static void main(String[] args) throws Exception {
        // 1. 配置
        GossipConfig config = new GossipConfig();
        config.setPort(8080);
        config.setFanout(2);
        config.setGossipIntervalMs(2000);

        // 2. 本节点
        Node self = new Node("node-1", "localhost", config.getPort());
        NodeState state = new NodeState();

        // 3. 集群（可配置多个节点）
        GossipCluster cluster = new GossipCluster();
        cluster.addNode(self);
        cluster.addNode(new Node("node-2", "localhost", 8081));
        cluster.addNode(new Node("node-3", "localhost", 8082));

        // 4. Gossip 组件
        RandomPeerSelector peerSelector = new RandomPeerSelector(cluster);
        HttpGossipTransport transport = new HttpGossipTransport();
        GossipProtocol protocol = new DefaultGossipProtocol(self, state, config, peerSelector, transport);

        // 5. 谣言传播组件
        RumorStore rumorStore = new RumorStore(3);  // 接收 3 次后变老
        HttpRumorTransport rumorTransport = new HttpRumorTransport();
        RumorSpreader rumorSpreader = new DefaultRumorSpreader(self, rumorStore, peerSelector, rumorTransport, config.getFanout());

        // 6. 启动接收服务（gossip + rumor）
        HttpGossipServer server = new HttpGossipServer(protocol, rumorSpreader, config.getPort());
        server.start();

        // 7. 写入初始数据（可选）
        state.put("key1", "value1", System.currentTimeMillis());

        // 8. 启动 gossip 协议
        protocol.start();

        // 9. 启动谣言传播
        rumorSpreader.start();

        // 10. 注入谣言示例（可选，用于测试）
        // rumorSpreader.inject("Hello from node-1!");

        System.out.println("Gossip node " + self.getId() + " started on port " + config.getPort());
        Thread.currentThread().join();
    }
}
