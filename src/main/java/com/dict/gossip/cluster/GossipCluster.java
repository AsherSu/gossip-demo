package com.dict.gossip.cluster;

import com.dict.gossip.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 集群管理：维护节点列表，提供节点发现
 */
public class GossipCluster {

    private final List<Node> nodes = new CopyOnWriteArrayList<>();

    public void addNode(Node node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
        }
    }

    public void removeNode(Node node) {
        nodes.remove(node);
    }

    public List<Node> getAllNodes() {
        return new ArrayList<>(nodes);
    }

    /** 获取除指定节点外的所有节点 */
    public List<Node> getOtherNodes(Node exclude) {
        List<Node> result = new ArrayList<>();
        for (Node n : nodes) {
            if (!n.equals(exclude)) {
                result.add(n);
            }
        }
        return result;
    }

    public int size() {
        return nodes.size();
    }
}
