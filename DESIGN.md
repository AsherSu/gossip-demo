# Gossip 协议设计文档

## 1. 协议概述

Gossip 协议是一种**去中心化**的分布式信息传播协议，每个节点周期性地与随机选择的邻居节点交换状态，最终实现**最终一致性**。

### 核心特点
- **无中心节点**：所有节点对等
- **随机传播**：每次随机选择若干节点交换
- **最终一致**：经过多轮传播，信息会扩散到整个集群
- **容错性强**：单点故障不影响整体

---

## 2. 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                    GossipCluster (集群管理)                   │
│  - 维护所有节点列表                                            │
│  - 提供节点发现/选择                                           │
└─────────────────────────────────────────────────────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  GossipNode  │◄──►│  GossipNode  │◄──►│  GossipNode  │
│  (节点 A)    │    │  (节点 B)    │    │  (节点 C)    │
│  - 本地状态   │    │  - 本地状态   │    │  - 本地状态   │
│  - 接收/发送  │    │  - 接收/发送  │    │  - 接收/发送  │
└──────────────┘    └──────────────┘    └──────────────┘
         │                    │                    │
         └────────────────────┼────────────────────┘
                              ▼
                    GossipMessage (消息格式)
                    - 发送方状态
                    - 版本/时间戳
```

---

## 3. 核心组件

| 组件 | 职责 |
|------|------|
| **GossipConfig** | 配置项：传播间隔、每轮选择节点数、端口等 |
| **Node** | 节点标识：id、地址、端口 |
| **NodeState** | 节点本地状态（如 key-value 数据），带版本号 |
| **GossipMessage** |  gossip 消息体：发送方信息 + 状态快照 |
| **GossipProtocol** | 协议核心：选择邻居、发送消息、合并状态 |
| **GossipCluster** | 集群管理：注册节点、启动 gossip 循环 |

---

## 4. 消息格式

```
GossipMessage {
    senderId: String      // 发送方节点 ID
    senderAddress: String // 发送方地址（用于回复）
    state: Map<K,V>       // 状态数据（可序列化）
    version: long         // 版本号，用于冲突解决
    timestamp: long       // 时间戳
}
```

---

## 5. 算法流程

### 5.1 主循环（每个节点独立执行）

```
while (running) {
    1. 从集群中随机选择 K 个节点（排除自己）
    2. 构建 GossipMessage（包含本节点当前状态）
    3. 向每个选中的节点发送消息
    4. 等待 gossipInterval 毫秒
}
```

### 5.2 接收与合并

```
onReceive(GossipMessage msg) {
    1. 解析对方状态
    2. 与本节点状态合并（merge 策略：取最新版本 / 取并集等）
    3. 更新本地状态
}
```

### 5.3 合并策略（关键实现点）

- **Last-Write-Wins (LWW)**：同一 key 取 version 或 timestamp 更大的
- **Merge Union**：不同 key 取并集
- **CRDT**：若使用 CRDT 结构，可无冲突合并

---

## 6. 关键实现点（供填充）

| 位置 | 说明 |
|------|------|
| `selectPeers()` | 从集群中随机选择 K 个节点，需排除自身 |
| `mergeState()` | 合并远程状态与本地状态，实现 LWW 或自定义策略 |
| `sendGossip()` | 将 GossipMessage 发送到目标节点（HTTP/gRPC/自定义） |
| `receiveGossip()` | 接收并解析消息，调用 mergeState |
| `serialize/deserialize` | 消息序列化，用于网络传输 |

---

## 7. 谣言传播（Rumor Mongering）

谣言传播是 Gossip 的一种变体：每条消息（谣言）独立传播，收到后继续转发，直到"变老"后停止。

### 7.1 流程

```
1. 节点 A 产生谣言 -> inject(rumor)
2. A 将谣言发给随机 K 个邻居
3. 邻居收到后：若为新谣言，记录并继续传播
4. 当某节点收到同一谣言达到阈值次（如 fanout 次），认为"变老"，停止传播
5. 最终谣言扩散到整个集群
```

### 7.2 关键点

| 组件 | 说明 |
|------|------|
| **Rumor** | 谣言内容：id、content、originator、timestamp |
| **RumorStore** | 存储谣言 + 接收次数，判断是否"变老" |
| **RumorSpreader** | 传播逻辑：inject、spreadLoop、onReceive |
| **RumorTransport** | 谣言网络传输 |

---

## 8. 项目结构

```
src/main/java/com/dict/gossip/
├── GossipApplication.java      # 入口，组装并启动
├── config/
│   └── GossipConfig.java       # 配置
├── model/
│   ├── Node.java               # 节点标识
│   ├── NodeState.java          # 本地状态 + merge 逻辑 ★
│   └── GossipMessage.java      # 消息格式 + 序列化
├── cluster/
│   └── GossipCluster.java     # 集群管理
├── protocol/
│   ├── GossipProtocol.java     # 协议接口
│   ├── DefaultGossipProtocol.java # 协议默认实现
│   ├── PeerSelector.java       # 邻居选择接口
│   └── RandomPeerSelector.java # 随机选择实现 ★
├── rumor/                      # 谣言传播 ★
│   ├── Rumor.java              # 谣言模型
│   ├── RumorStore.java         # 谣言存储 + 变老判断
│   ├── RumorMessage.java       # 谣言消息格式
│   ├── RumorSpreader.java      # 传播协议接口
│   ├── DefaultRumorSpreader.java # 传播默认实现
│   ├── RumorTransport.java     # 传输接口
│   └── HttpRumorTransport.java # HTTP 传输
├── transport/
│   └── HttpGossipTransport.java # HTTP 发送 ★
└── server/
    └── HttpGossipServer.java   # gossip + rumor 接收
```

★ 标注为建议重点理解/修改的文件

---

## 9. 扩展方向

- **故障检测**：通过心跳判断节点存活（如 SWIM）
- **反熵**：全量同步 vs 增量同步
- **分区容忍**：网络分区时的行为
- **安全性**：消息签名、加密
