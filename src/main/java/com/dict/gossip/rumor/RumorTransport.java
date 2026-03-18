package com.dict.gossip.rumor;

import com.dict.gossip.model.Node;

/**
 * 谣言传输接口
 */
public interface RumorTransport {

    /**
     * 向目标节点发送谣言
     */
    void send(Node target, RumorMessage message);
}
