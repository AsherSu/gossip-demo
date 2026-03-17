package com.dict.gossip;

import com.dict.gossip.config.GossipConfig;
import com.dict.gossip.model.Node;
import com.dict.gossip.model.NodeState;
import com.dict.gossip.cluster.GossipCluster;
import com.dict.gossip.protocol.GossipProtocol;
import com.dict.gossip.protocol.RandomPeerSelector;
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

        // 4. 组件组装
        RandomPeerSelector peerSelector = new RandomPeerSelector(cluster);
        HttpGossipTransport transport = new HttpGossipTransport();
        GossipProtocol protocol = new GossipProtocol(self, state, config, peerSelector, transport);

        // 5. 启动接收服务
        HttpGossipServer server = new HttpGossipServer(protocol, config.getPort());
        server.start();

        // 6. 写入初始数据（可选）
        state.put("key1", "value1", System.currentTimeMillis());

        // 7. 启动 gossip 协议
        protocol.start();

        System.out.println("Gossip node " + self.getId() + " started on port " + config.getPort());
        Thread.currentThread().join();
    }
}
