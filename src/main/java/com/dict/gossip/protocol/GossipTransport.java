package com.dict.gossip.protocol;

import com.dict.gossip.model.GossipMessage;
import com.dict.gossip.model.Node;

/**
 * 传输层抽象：负责 gossip 消息的发送
 */
public interface GossipTransport {

    /**
     * 向目标节点发送 gossip 消息
     *
     * @param target 目标节点
     * @param message gossip 消息
     */
    void send(Node target, GossipMessage message);
}
