package com.dict.gossip.protocol;

import com.dict.gossip.model.Node;

import java.util.List;

/**
 * 邻居选择器：从集群中选出 gossip 目标
 */
public interface PeerSelector {

    /**
     * 选择 K 个节点作为本轮的 gossip 目标（排除自身）
     *
     * @param self 本节点
     * @param fanout 需要选择的节点数量
     * @return 选中的节点列表，数量可能小于 fanout（若集群节点不足）
     */
    List<Node> selectPeers(Node self, int fanout);
}
