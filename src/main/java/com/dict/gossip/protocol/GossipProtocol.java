package com.dict.gossip.protocol;

import com.dict.gossip.model.GossipMessage;

/**
 * Gossip 协议接口
 */
public interface GossipProtocol {

    /** 启动 gossip 循环 */
    void start();

    /** 停止 gossip 循环 */
    void stop();

    /** 接收并处理来自其他节点的 gossip 消息 */
    void onReceive(GossipMessage msg);
}
