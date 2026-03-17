package com.dict.gossip.transport;

import com.dict.gossip.model.GossipMessage;
import com.dict.gossip.model.Node;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 基于 HTTP 的 Gossip 传输实现（骨架）
 */
public class HttpGossipTransport implements com.dict.gossip.protocol.GossipTransport {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void send(Node target, GossipMessage message) {
        // TODO: 1. 将 message 序列化为请求体
        // TODO: 2. 构建 HTTP POST 请求，如 POST http://host:port/gossip
        // TODO: 3. 发送并处理响应（可异步）
        String url = "http://" + target.getAddress() + "/gossip";
        byte[] body = message.serialize();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                // 记录失败，可选重试
            }
        } catch (Exception e) {
            // 网络异常，节点可能宕机，可记录
            e.printStackTrace();
        }
    }
}
