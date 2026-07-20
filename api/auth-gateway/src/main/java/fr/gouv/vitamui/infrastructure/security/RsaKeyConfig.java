package fr.gouv.vitamui.infrastructure.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-path:classpath:certs/public.pem}")
    private Resource publicKeyResource;

    @Value("${spring.security.oauth2.resourceserver.jwt.private-key-path:classpath:certs/private.pem}")
    private Resource privateKeyResource;

    @Bean
    public RSAPublicKey publicKey() throws Exception {
        String key = new String(
            publicKeyResource.getInputStream().readAllBytes(),
            StandardCharsets.UTF_8);

        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) keyFactory.generatePublic(
            new X509EncodedKeySpec(decoded));
    }

    @Bean
    public RSAPrivateKey privateKey() throws Exception {
        String key = new String(
            privateKeyResource.getInputStream().readAllBytes(),
            StandardCharsets.UTF_8);

        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(
            new PKCS8EncodedKeySpec(decoded));
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(
        RSAPublicKey publicKey,
        RSAPrivateKey privateKey) {

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .build();

        JWKSet jwkSet = new JWKSet(rsaKey);

        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
