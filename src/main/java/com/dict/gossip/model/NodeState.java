package com.dict.gossip.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点本地状态
 * 使用 key-value 存储，每个条目带版本号，用于合并时冲突解决
 */
public class NodeState {

    /** key -> (value, version) */
    private final Map<String, StateEntry> data = new ConcurrentHashMap<>();

    public void put(String key, String value, long version) {
        // 可扩展：仅当 version 更大时才覆盖（LWW）
        data.put(key, new StateEntry(value, version));
    }

    public String get(String key) {
        StateEntry entry = data.get(key);
        return entry != null ? entry.value : null;
    }

    public long getVersion(String key) {
        StateEntry entry = data.get(key);
        return entry != null ? entry.version : 0;
    }

    /** 获取完整状态快照，用于 gossip 发送 */
    public Map<String, StateEntry> snapshot() {
        return new ConcurrentHashMap<>(data);
    }

    /** 合并远程状态到本地（Last-Write-Wins 策略） */
    public void merge(Map<String, StateEntry> remote) {
        // TODO: 理解并尝试修改合并策略，如取并集、CRDT 等
        for (Map.Entry<String, StateEntry> e : remote.entrySet()) {
            StateEntry local = data.get(e.getKey());
            if (local == null || e.getValue().version > local.version) {
                data.put(e.getKey(), e.getValue());
            }
        }
    }

    public record StateEntry(String value, long version) {}
}
