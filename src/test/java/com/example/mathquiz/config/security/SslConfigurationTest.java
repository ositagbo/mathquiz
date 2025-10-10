package com.example.mathquiz.config.security;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.tls.HandshakeCertificates;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SslConfigurationTest {
    private MockWebServer mockWebServer;
    private CloseableHttpClient httpClient;
    private SSLConnectionSocketFactory sslSocketFactory;

    @BeforeEach
    void setup() throws Exception {
        // 1. Configure MockWebServer with TLS
        HandshakeCertificates serverCerts = new HandshakeCertificates.Builder()
                .addPlatformTrustedCertificates()
                .build();

        mockWebServer = new MockWebServer();
        mockWebServer.useHttps(serverCerts.sslSocketFactory(), false);
        mockWebServer.start();

        // 2. Extract server certificate
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        X509Certificate serverCert = (X509Certificate) KeyStore.getInstance("PKCS12")
                .getCertificate("test-alias");
        //X509Certificate serverCert = (X509Certificate) serverCerts.keyManager().getCertificateChain("")[0];

        // 3. Configure client SSL context
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("server", serverCert);

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, null)
                .build();

        // 4. Create SSL socket factory matching production config
        sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1.3"},
                null,
                NoopHostnameVerifier.INSTANCE
        );

        // 5. Create connection manager with pooling
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", sslSocketFactory)
                        .build(),
                PoolConcurrencyPolicy.STRICT,
                PoolReusePolicy.LIFO,
                Timeout.ofMinutes(5)
        );
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(50);
        cm.setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(30))
                .build());

        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    @Test
    void testSuccessfulSslHandshake() throws Exception {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("SSL Handshake Successful"));

        // Act
        try (CloseableHttpResponse response = httpClient.execute(
                new HttpGet(mockWebServer.url("/secure").toString()))) {

            // Assert
            assertThat(response.getCode()).isEqualTo(200);
            assertThat(EntityUtils.toString(response.getEntity()))
                    .isEqualTo("SSL Handshake Successful");
        }

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getRequestUrl().isHttps()).isTrue();
    }

    @Test
    void testTlsVersionEnforcement() throws Exception {
        // Test server with older TLS version
        HandshakeCertificates oldTlsCerts = new HandshakeCertificates.Builder()
                .addPlatformTrustedCertificates()
                .build();

        try (MockWebServer tls12Server = new MockWebServer()) {
            tls12Server.useHttps(oldTlsCerts.sslSocketFactory(), false);
            tls12Server.start();
            tls12Server.enqueue(new MockResponse().setResponseCode(200));

            // Should fail due to TLS version mismatch
            assertThatThrownBy(() ->
                    httpClient.execute(new HttpGet(tls12Server.url("/").toString())))
                    .isInstanceOf(SSLHandshakeException.class)
                    .hasMessageContaining("handshake_failure");
        }
    }

    @AfterEach
    void teardown() throws IOException {
        httpClient.close();
        mockWebServer.shutdown();
    }
}
