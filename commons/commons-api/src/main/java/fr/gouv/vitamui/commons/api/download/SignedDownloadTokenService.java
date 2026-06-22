/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2026)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 */
package fr.gouv.vitamui.commons.api.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class SignedDownloadTokenService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long ttlSeconds;
    private final long clockSkewSeconds;
    private final Clock clock;
    private final Consumer<DownloadClaims> claimsEnricher;

    public SignedDownloadTokenService(
        ObjectMapper objectMapper,
        String secret,
        long ttlSeconds,
        long clockSkewSeconds
    ) {
        this(objectMapper, secret, ttlSeconds, clockSkewSeconds, Clock.systemUTC(), claims -> {});
    }

    public SignedDownloadTokenService(
        ObjectMapper objectMapper,
        String secret,
        long ttlSeconds,
        long clockSkewSeconds,
        Consumer<DownloadClaims> claimsEnricher
    ) {
        this(objectMapper, secret, ttlSeconds, clockSkewSeconds, Clock.systemUTC(), claimsEnricher);
    }

    SignedDownloadTokenService(
        ObjectMapper objectMapper,
        String secret,
        long ttlSeconds,
        long clockSkewSeconds,
        Clock clock
    ) {
        this(objectMapper, secret, ttlSeconds, clockSkewSeconds, clock, claims -> {});
    }

    SignedDownloadTokenService(
        ObjectMapper objectMapper,
        String secret,
        long ttlSeconds,
        long clockSkewSeconds,
        Clock clock,
        Consumer<DownloadClaims> claimsEnricher
    ) {
        if (StringUtils.isBlank(secret)) {
            throw new IllegalArgumentException("download.signed-url.secret must be configured");
        }
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
        this.clockSkewSeconds = clockSkewSeconds;
        this.clock = clock;
        this.claimsEnricher = Objects.requireNonNull(claimsEnricher);
    }

    public String generateSignedUrl(DownloadClaims claims, String downloadPath) {
        SignedDownloadToken signedToken = generate(claims);
        return UriComponentsBuilder.fromPath(downloadPath).queryParam("token", signedToken.value()).toUriString();
    }

    public SignedDownloadToken generate(DownloadClaims claims) {
        long now = Instant.now(clock).getEpochSecond();
        claimsEnricher.accept(claims);
        claims.setIssuedAt(now);
        claims.setExpiresAt(now + ttlSeconds);

        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encodeJson(claims);
            String content = header + "." + payload;
            return new SignedDownloadToken(content + "." + sign(content), claims.getExpiresAt());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize signed download token", e);
        }
    }

    public DownloadClaims validate(String token, String expectedResource) {
        if (StringUtils.isBlank(token)) {
            throw new UnAuthorizedException("Missing signed download token");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnAuthorizedException("Invalid signed download token");
        }

        String content = parts[0] + "." + parts[1];
        String expectedSignature = sign(content);
        if (
            !MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8)
            )
        ) {
            throw new UnAuthorizedException("Invalid signed download token signature");
        }

        try {
            DownloadClaims claims = objectMapper.readValue(BASE64_URL_DECODER.decode(parts[1]), DownloadClaims.class);
            long now = Instant.now(clock).getEpochSecond();
            if (claims.getExpiresAt() + clockSkewSeconds < now || claims.getIssuedAt() - clockSkewSeconds > now) {
                throw new UnAuthorizedException("Expired signed download token");
            }
            if (!StringUtils.equals(expectedResource, claims.getResource())) {
                throw new UnAuthorizedException("Invalid signed download token resource");
            }
            return claims;
        } catch (IOException | IllegalArgumentException e) {
            throw new UnAuthorizedException("Invalid signed download token payload");
        }
    }

    private String encodeJson(Object value) throws JsonProcessingException {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret, HMAC_SHA256));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign download token", e);
        }
    }

    public record SignedDownloadToken(String value, long expiresAt) {}
}
