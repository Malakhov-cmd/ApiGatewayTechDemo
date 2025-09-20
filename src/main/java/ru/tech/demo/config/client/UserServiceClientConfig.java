package ru.tech.demo.config.client;

import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Configuration
public class UserServiceClientConfig {
    private static final Logger log = LoggerFactory.getLogger(UserServiceClientConfig.class);

    @Value("${userService.url}")
    private String baseUrlToUserService;

    @Value("${server.ssl.key-store}")
    private String keyStore;
    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;


    @Value("${server.ssl.trust-store}")
    private String trustStore;
    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Bean("userServiceWebClient")
    public WebClient userServiceWebClient() {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

        //Загружаем keystore
        try {
            KeyStore keyStoreType = KeyStore.getInstance("PKCS12");

            FileInputStream keyStoreFile = new FileInputStream(keyStore);
            keyStoreType.load(keyStoreFile, keyStorePassword.toCharArray());
            keyStoreFile.close();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStoreType, keyStorePassword.toCharArray());

            sslContextBuilder.keyManager(kmf);
        } catch (KeyStoreException | FileNotFoundException | UnrecoverableKeyException | NoSuchAlgorithmException |
                 CertificateException e) {
            log.error("Error while loading keystore for UserService. Details: {}", e.toString());
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("Error while reading keystore for UserService. Details: {}", e.toString());
            throw new RuntimeException(e);
        }

        //Загружаем truststore
        try {
            KeyStore trustStoreType = KeyStore.getInstance("PKCS12");

            FileInputStream trustStoreFile = new FileInputStream(trustStore);
            trustStoreType.load(trustStoreFile, trustStorePassword.toCharArray());
            trustStoreFile.close();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStoreType);

            sslContextBuilder.trustManager(tmf);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | FileNotFoundException e) {
            log.error("Error while loading truststore for UserService. Details: {}", e.toString());
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("Error while reading truststore for UserService. Details: {}", e.toString());
            throw new RuntimeException(e);
        }

        // === Создаём SSL-контекст ===
        HttpClient httpClient = HttpClient
                .create()
                .secure(t -> {
                    try {
                        t.sslContext(sslContextBuilder.build());
                    } catch (SSLException e) {
                        log.error("Error while build SSL context for UserService. Details: {}", e.toString());
                        throw new RuntimeException(e);
                    }
                });

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrlToUserService)
                .build();
    }
}
