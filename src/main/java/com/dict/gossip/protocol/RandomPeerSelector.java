package com.dict.gossip.protocol;

import com.dict.gossip.cluster.GossipCluster;
import com.dict.gossip.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 随机邻居选择器
 */
public class RandomPeerSelector implements PeerSelector {

    private final GossipCluster cluster;

    public RandomPeerSelector(GossipCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public List<Node> selectPeers(Node self, int fanout) {
        List<Node> others = cluster.getOtherNodes(self);
        if (others.isEmpty()) {
            return List.of();
        }
        List<Node> shuffled = new ArrayList<>(others);
        Collections.shuffle(shuffled);
        int take = Math.min(fanout, shuffled.size());
        return shuffled.subList(0, take);
    }
}
