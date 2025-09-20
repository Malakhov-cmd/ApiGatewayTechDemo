package ru.tech.demo.config.security.jwt;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Configuration
public class JwtKeyConfig {

    @Value("${server.ssl.custom.jwt-private-key}")
    private String jwtPrivateKeyPath;

    @Value("${server.ssl.custom.jwt-public-key}")
    private String jwtPublicKeyPath;

    @Value("${userService.jwks.key-id}")
    private String jwksKeyID;

    @Bean
    public RSAKey rsaKey() throws Exception {
        // Загружаем приватный ключ
        byte[] privBytes = Files.readAllBytes(Path.of(jwtPrivateKeyPath));
        String privateKeyPem = new String(privBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(privateKeyPem)));

        // Загружаем публичный ключ
        byte[] pubBytes = Files.readAllBytes(Path.of(jwtPublicKeyPath));
        String publicKeyPem = new String(pubBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(java.util.Base64.getDecoder().decode(publicKeyPem)));

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(jwksKeyID) // для ротации ключей
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        return (jwkSelector, context) -> jwkSelector.select(new com.nimbusds.jose.jwk.JWKSet(rsaKey));
    }
}
