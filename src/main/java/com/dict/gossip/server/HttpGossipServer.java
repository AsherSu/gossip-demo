package com.dict.gossip.server;

import com.dict.gossip.model.GossipMessage;
import com.dict.gossip.protocol.GossipProtocol;
import com.dict.gossip.rumor.RumorMessage;
import com.dict.gossip.rumor.RumorSpreader;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 基于 HttpServer 的 Gossip + 谣言接收端
 */
public class HttpGossipServer {

    private final GossipProtocol protocol;
    private final RumorSpreader rumorSpreader;
    private final int port;
    private HttpServer server;

    public HttpGossipServer(GossipProtocol protocol, int port) {
        this(protocol, null, port);
    }

    public HttpGossipServer(GossipProtocol protocol, RumorSpreader rumorSpreader, int port) {
        this.protocol = protocol;
        this.rumorSpreader = rumorSpreader;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/gossip", new GossipHandler());
        if (rumorSpreader != null) {
            server.createContext("/rumor", new RumorHandler());
        }
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class GossipHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream in = exchange.getRequestBody()) {
                    byte[] body = in.readAllBytes();
                    GossipMessage msg = GossipMessage.deserialize(body);
                    protocol.onReceive(msg);
                }
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    private class RumorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream in = exchange.getRequestBody()) {
                    byte[] body = in.readAllBytes();
                    RumorMessage msg = RumorMessage.deserialize(body);
                    rumorSpreader.onReceive(msg);
                }
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}
