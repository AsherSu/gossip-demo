package com.dict.gossip.rumor;

import com.dict.gossip.model.Node;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 基于 HTTP 的谣言传输
 */
public class HttpRumorTransport implements RumorTransport {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void send(Node target, RumorMessage message) {
        String url = "http://" + target.getAddress() + "/rumor";
        byte[] body = message.serialize();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // 网络异常
            e.printStackTrace();
        }
    }
}
