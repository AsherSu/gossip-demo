package com.dict.gossip.rumor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 谣言存储：记录已收到的谣言及来源
 * 用于判断谣言是否"变老"（old），从而停止传播
 */
public class RumorStore {

    /** 来自不同节点数达到此阈值后，谣言"变老"，不再传播 */
    private final int oldThreshold;

    /** rumorId -> (rumor, 已收到该谣言的节点集合) */
    private final Map<String, RumorEntry> rumors = new ConcurrentHashMap<>();

    public RumorStore(int oldThreshold) {
        this.oldThreshold = oldThreshold;
    }

    /**
     * 记录收到一次谣言（来自 senderId），返回是否为新谣言（首次收到）
     * 变老判断：收到谣言来自不同节点数 >= oldThreshold
     */
    public boolean onReceive(Rumor rumor, String senderId) {
        AtomicBoolean isNew = new AtomicBoolean(false);
        // 添加发送方到本地谣言条目
        rumors.compute(rumor.getId(), (id, entry) -> {
            if (entry == null) {
                isNew.set(true);
                RumorEntry e = new RumorEntry(rumor);
                e.addSender(senderId);
                return e;
            }
            entry.addSender(senderId);
            return entry;
        });
        return isNew.get();
    }

    public boolean isOld(String rumorId) {
        RumorEntry entry = rumors.get(rumorId);
        if (entry == null) return false;
        return entry.distinctSenderCount() >= oldThreshold;
    }

    // 获取所有未变老的谣言
    public List<Rumor> getActiveRumors() {
        return rumors.values().stream()
                .filter(e -> !isOld(e.rumor.getId()))
                .map(e -> e.rumor)
                .toList();
    }

    // 获取指定 ID 的谣言
    public Rumor get(String rumorId) {
        RumorEntry e = rumors.get(rumorId);
        return e != null ? e.rumor : null;
    }

    private static class RumorEntry {
        final Rumor rumor;
        // 谣言发送方
        final Set<String> senders = ConcurrentHashMap.newKeySet();

        RumorEntry(Rumor rumor) {
            this.rumor = rumor;
        }

        void addSender(String id) {
            senders.add(id);
        }

        int distinctSenderCount() {
            return senders.size();
        }
    }
}
