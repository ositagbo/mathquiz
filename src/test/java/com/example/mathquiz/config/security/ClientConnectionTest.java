package com.example.mathquiz.config.security;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ClientConnectionTest {
    private MockWebServer mockWebServer;
    private CloseableHttpClient httpClient;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        httpClient = HttpClients.createDefault();
    }

    @AfterEach
    void teardown() throws IOException {
        httpClient.close();
        mockWebServer.shutdown();
    }

    @Test
    void testGetRequest() throws Exception {
        // Arrange
        String expectedResponse = "{\"status\":\"OK\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(expectedResponse)
                .addHeader("Content-Type", "application/json")
        );

        String url = mockWebServer.url("/api/v1/data").toString();

        // Act
        try (CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {
            // Assert Response
            String actualResponse = EntityUtils.toString(response.getEntity());

            assertThat(response.getCode()).isEqualTo(200);
            assertThat(actualResponse).isEqualTo(expectedResponse);
            assertThat(response.getFirstHeader("Content-Type").getValue())
                    .contains("application/json");
        }

        // Assert Request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/data");
    }

    @Autowired
    private Registry<ConnectionSocketFactory> socketRegistry;

    @Test
    void testSSLHandshake() throws Exception {
        String url = mockWebServer.url("/api/v1/data").toString();
        try (CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager(socketRegistry))
                .build()) {

            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            assertThat(response.getCode()).isEqualTo(200);
        }
    }
}
