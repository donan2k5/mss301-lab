package com.fudn.gateway;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutesTests {

    private static final HttpServer productServer = server();
    private static final HttpServer orderServer = server();
    private static final HttpServer inventoryServer = server();
    private static final AtomicReference<String> orderBody = new AtomicReference<>();
    private static final AtomicReference<String> inventoryQuery = new AtomicReference<>();

    static {
        productServer.createContext("/api/products", exchange -> {
            byte[] body = "[]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        orderServer.createContext("/api/order", exchange -> {
            orderBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = "Order Placed Successfully".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        inventoryServer.createContext("/api/inventory", exchange -> {
            inventoryQuery.set(exchange.getRequestURI().getRawQuery());
            byte[] body = "true".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        productServer.start();
        orderServer.start();
        inventoryServer.start();
    }

    @DynamicPropertySource
    static void serviceUrls(DynamicPropertyRegistry registry) {
        registry.add("services.product.url", () -> url(productServer));
        registry.add("services.order.url", () -> url(orderServer));
        registry.add("services.inventory.url", () -> url(inventoryServer));
    }

    @AfterAll
    static void stopServers() {
        productServer.stop(0);
        orderServer.stop(0);
        inventoryServer.stop(0);
    }

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void routesProducts() throws Exception {
        var response = send("GET", "/api/products", null);
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void routesOrderWithBody() throws Exception {
        String body = "{\"skuCode\":\"iphone_15\",\"price\":1000,\"quantity\":1}";
        var response = send("POST", "/api/order", body);
        assertEquals(201, response.statusCode());
        assertEquals("Order Placed Successfully", response.body());
        assertEquals(body, orderBody.get());
    }

    @Test
    void routesInventoryWithQuery() throws Exception {
        var response = send("GET", "/api/inventory?skuCode=iphone_15&quantity=1", null);
        assertEquals(200, response.statusCode());
        assertEquals("true", response.body());
        assertTrue(inventoryQuery.get().contains("skuCode=iphone_15"));
        assertTrue(inventoryQuery.get().contains("quantity=1"));
    }

    @Test
    void unknownRouteReturnsNotFound() throws Exception {
        assertEquals(404, send("GET", "/api/unknown", null).statusCode());
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpServer server() {
        try {
            return HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static String url(HttpServer server) {
        return "http://localhost:" + server.getAddress().getPort();
    }
}
