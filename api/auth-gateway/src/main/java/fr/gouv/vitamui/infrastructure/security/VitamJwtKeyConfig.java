package fr.gouv.vitamui.infrastructure.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
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
public class VitamJwtKeyConfig {

    @Value("${vitam.security.jwt.public-key-path:classpath:certs/vitam-public.pem}")
    private Resource publicKeyResource;

    @Value("${vitam.security.jwt.private-key-path:classpath:certs/vitam-private.pem}")
    private Resource privateKeyResource;

    @Bean("vitamRsaPublicKey")
    public RSAPublicKey vitamRsaPublicKey() throws Exception {
        String key = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        key = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    @Bean("vitamRsaPrivateKey")
    public RSAPrivateKey vitamRsaPrivateKey() throws Exception {
        String key = new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        key = key
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    @Bean("vitamJwkSource")
    public JWKSource<SecurityContext> vitamJwkSource(
        @Qualifier("vitamRsaPublicKey") RSAPublicKey publicKey,
        @Qualifier("vitamRsaPrivateKey") RSAPrivateKey privateKey
    ) {
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("vitam-app-key").build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean("vitamJwtEncoder")
    public JwtEncoder vitamJwtEncoder(@Qualifier("vitamJwkSource") JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean("vitamJwtDecoder")
    public JwtDecoder vitamJwtDecoder(@Qualifier("vitamRsaPublicKey") RSAPublicKey publicKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("vitam"));

        return decoder;
    }
}
