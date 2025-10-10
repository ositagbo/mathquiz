package com.example.mathquiz.config.security;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class ClientConnection {

    @Bean
    public Registry<ConnectionSocketFactory> createSocketRegistry() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException, KeyManagementException {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", buildSSLSocketFactory())
                .build();
    }

    @Bean
    public PoolingHttpClientConnectionManager connectionManager(Registry<ConnectionSocketFactory> registry) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(50);
        return cm;
    }

    private ConnectionSocketFactory buildSSLSocketFactory() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException, KeyManagementException {
        return new SSLConnectionSocketFactory(
                SSLContexts.custom()
                        .loadTrustMaterial(new File("cert/truststore.jks"), "changeit".toCharArray())
                        .build(),
                new String[]{"TLSv1.3"},
                null,
                NoopHostnameVerifier.INSTANCE
        );
    }

    public void configureConnectionManager() {
            // 1. Create registry explicitly
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", SSLConnectionSocketFactory.getSocketFactory())
                    .build();

            // 2. Initialize connection manager with registry
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);

            // 3. Access factory through your registry reference (NOT via connection manager)
            ConnectionSocketFactory factory = registry.lookup("https");
    }


}
