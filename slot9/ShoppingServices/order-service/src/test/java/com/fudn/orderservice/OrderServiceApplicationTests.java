package com.fudn.orderservice;

import com.fudn.orderservice.stub.InventoryStubs;
import com.fudn.orderservice.repository.OrderRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "inventory.url"))
class OrderServiceApplicationTests {

    @ServiceConnection
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.3.0");

    @LocalServerPort
    private Integer port;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        orderRepository.deleteAll();
    }

    static {
        mySQLContainer.start();
    }

    @Test
    void shouldSubmitOrder() {
        String submitOrderJson = """
                {
                     "skuCode": "iphone_15",
                     "price": 1000,
                     "quantity": 1
                }
                """;

        InventoryStubs.stubInventoryCall("iphone_15", 1);

        var responseBodyString = RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .body().asString();

        assertThat(responseBodyString, Matchers.is("Order Placed Successfully"));
        verify(getRequestedFor(urlEqualTo("/api/inventory?skuCode=iphone_15&quantity=1")));
        assertEquals(1, orderRepository.count());
    }

    @Test
    void shouldRejectOrderWhenOutOfStock() {
        String submitOrderJson = """
                {
                     "skuCode": "iphone_15",
                     "price": 1000,
                     "quantity": 101
                }
                """;

        InventoryStubs.stubInventoryCall("iphone_15", 101, false);

        RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .statusCode(409);
        assertEquals(0, orderRepository.count());
    }

    @Test
    void shouldNotSaveOrderWhenInventoryIsUnavailable() {
        InventoryStubs.stubInventoryFailure("iphone_15", 1);

        RestAssured.given()
                .contentType("application/json")
                .body("""
                        {"skuCode":"iphone_15","price":1000,"quantity":1}
                        """)
                .when()
                .post("/api/order")
                .then()
                .statusCode(500);

        assertEquals(0, orderRepository.count());
    }
}
