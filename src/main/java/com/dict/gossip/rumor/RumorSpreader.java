package com.dict.gossip.rumor;

/**
 * 谣言传播协议接口
 */
public interface RumorSpreader {

    /** 注入一条新谣言（由本节点产生，开始传播） */
    void inject(String content);

    /** 启动谣言传播循环 */
    void start();

    /** 停止谣言传播循环 */
    void stop();

    /** 接收来自其他节点的谣言 */
    void onReceive(RumorMessage msg);

    /** 设置新谣言到达回调 */
    void setRumorListener(RumorListener listener);

    /** 新谣言到达时回调 */
    @FunctionalInterface
    interface RumorListener {
        void onNewRumor(Rumor rumor);
    }
}
