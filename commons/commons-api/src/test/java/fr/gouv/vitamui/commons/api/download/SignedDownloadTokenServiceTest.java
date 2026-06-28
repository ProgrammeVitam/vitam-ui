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

import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignedDownloadTokenServiceTest {

    private static final String SECRET = "download-secret";
    private static final String RESOURCE = "logbook-report";
    private static final long TTL_SECONDS = 60;
    private static final long CLOCK_SKEW_SECONDS = 5;

    // Token generated at t=100 → issuedAt=100, expiresAt=160
    private static final long GENERATION_TIME = 100L;

    private SignedDownloadTokenService serviceAt(long epochSecond) {
        return new SignedDownloadTokenService(
            new ObjectMapper(),
            SECRET,
            TTL_SECONDS,
            CLOCK_SKEW_SECONDS,
            Clock.fixed(Instant.ofEpochSecond(epochSecond), ZoneOffset.UTC)
        );
    }

    private DownloadClaims buildClaims() {
        DownloadClaims claims = new DownloadClaims();
        claims.setResource(RESOURCE);
        claims.setSubject("userId");
        claims.setTenantId(10);
        claims.setAccessContractId("accessContractId");
        claims.setApplicationSessionId("applicationSessionId");
        claims.setParameters(Map.of("id", "operationId", "downloadType", "report"));
        return claims;
    }

    @Test
    void shouldGenerateValidToken() {
        DownloadClaims claims = buildClaims();
        SignedDownloadTokenService service = serviceAt(GENERATION_TIME);

        SignedDownloadTokenService.SignedDownloadToken token = service.generate(claims);
        DownloadClaims validatedClaims = service.validate(token.value(), RESOURCE);

        // generate() mutates the input claims on purpose (issuedAt/expiresAt are set in place)
        assertThat(claims.getIssuedAt()).isEqualTo(100);
        assertThat(claims.getExpiresAt()).isEqualTo(160);
        assertThat(token.expiresAt()).isEqualTo(160);
        assertThat(validatedClaims.getIssuedAt()).isEqualTo(100);
        assertThat(validatedClaims.getExpiresAt()).isEqualTo(160);
        assertThat(validatedClaims.getParameters()).containsEntry("id", "operationId");
    }

    @Test
    void shouldApplyClaimsEnricherBeforeSigning() {
        DownloadClaims claims = new DownloadClaims();
        claims.setResource(RESOURCE);

        SignedDownloadTokenService service = new SignedDownloadTokenService(
            new ObjectMapper(),
            SECRET,
            TTL_SECONDS,
            CLOCK_SKEW_SECONDS,
            Clock.fixed(Instant.ofEpochSecond(GENERATION_TIME), ZoneOffset.UTC),
            tokenClaims -> tokenClaims.setSubject("enrichedUserId")
        );

        SignedDownloadTokenService.SignedDownloadToken token = service.generate(claims);
        DownloadClaims validatedClaims = service.validate(token.value(), RESOURCE);

        assertThat(validatedClaims.getSubject()).isEqualTo("enrichedUserId");
    }

    // --- Construction ---

    @Test
    void shouldRejectBlankSecret() {
        assertThatThrownBy(
            () -> new SignedDownloadTokenService(new ObjectMapper(), "", TTL_SECONDS, CLOCK_SKEW_SECONDS)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(
            () -> new SignedDownloadTokenService(new ObjectMapper(), "   ", TTL_SECONDS, CLOCK_SKEW_SECONDS)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    // --- Blank / malformed token ---

    @Test
    void shouldRejectBlankToken() {
        SignedDownloadTokenService service = serviceAt(GENERATION_TIME);

        assertThatThrownBy(() -> service.validate("", RESOURCE))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining("Missing");

        assertThatThrownBy(() -> service.validate("   ", RESOURCE))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining("Missing");
    }

    @Test
    void shouldRejectMalformedToken_tooFewParts() {
        SignedDownloadTokenService service = serviceAt(GENERATION_TIME);

        assertThatThrownBy(() -> service.validate("onlyone", RESOURCE)).isInstanceOf(UnAuthorizedException.class);

        assertThatThrownBy(() -> service.validate("two.parts", RESOURCE)).isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    void shouldRejectMalformedToken_tooManyParts() {
        SignedDownloadTokenService service = serviceAt(GENERATION_TIME);
        String token = service.generate(buildClaims()).value();

        assertThatThrownBy(() -> service.validate(token + ".extra", RESOURCE)).isInstanceOf(
            UnAuthorizedException.class
        );
    }

    // --- Signature ---

    @Test
    void shouldRejectInvalidSignature() {
        SignedDownloadTokenService service = serviceAt(GENERATION_TIME);
        String token = service.generate(buildClaims()).value();

        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature";

        assertThatThrownBy(() -> service.validate(tamperedToken, RESOURCE))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining("signature");
    }

    @Test
    void shouldRejectTamperedPayload() {
        SignedDownloadTokenService service = serviceAt(GENERATION_TIME);
        String token = service.generate(buildClaims()).value();

        String[] parts = token.split("\\.");
        // Encode a different payload and pair it with the original signature
        String fakePayload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"resource\":\"other-resource\"}".getBytes());
        String tamperedToken = parts[0] + "." + fakePayload + "." + parts[2];

        assertThatThrownBy(() -> service.validate(tamperedToken, RESOURCE)).isInstanceOf(UnAuthorizedException.class);
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        SignedDownloadTokenService generatorService = new SignedDownloadTokenService(
            new ObjectMapper(),
            "secret-A",
            TTL_SECONDS,
            CLOCK_SKEW_SECONDS,
            Clock.fixed(Instant.ofEpochSecond(GENERATION_TIME), ZoneOffset.UTC)
        );
        SignedDownloadTokenService validatorService = new SignedDownloadTokenService(
            new ObjectMapper(),
            "secret-B",
            TTL_SECONDS,
            CLOCK_SKEW_SECONDS,
            Clock.fixed(Instant.ofEpochSecond(GENERATION_TIME), ZoneOffset.UTC)
        );

        String token = generatorService.generate(buildClaims()).value();

        assertThatThrownBy(() -> validatorService.validate(token, RESOURCE))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining("signature");
    }

    // --- Expiry ---

    @Test
    void shouldRejectExpiredToken() {
        // Generated at t=100, expiresAt=160, clockSkew=5 → expired after t=165
        String token = serviceAt(GENERATION_TIME).generate(buildClaims()).value();

        assertThatThrownBy(() -> serviceAt(166L).validate(token, RESOURCE))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining("Expired");
    }

    @Test
    void shouldAcceptTokenAtLastValidSecondWithClockSkew() {
        // expiresAt=160, clockSkew=5 → last valid second is t=165
        String token = serviceAt(GENERATION_TIME).generate(buildClaims()).value();

        DownloadClaims result = serviceAt(165L).validate(token, RESOURCE);
        assertThat(result.getResource()).isEqualTo(RESOURCE);
    }

    // --- issuedAt / future token ---

    @Test
    void shouldRejectTokenUsedBeforeIssuedAt() {
        // Generated at t=100 → issuedAt=100, clockSkew=5 → invalid before t=95
        String token = serviceAt(GENERATION_TIME).generate(buildClaims()).value();

        assertThatThrownBy(() -> serviceAt(94L).validate(token, RESOURCE))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining("Expired");
    }

    @Test
    void shouldAcceptTokenAtFirstValidSecondWithClockSkew() {
        // issuedAt=100, clockSkew=5 → first valid second is t=95
        String token = serviceAt(GENERATION_TIME).generate(buildClaims()).value();

        DownloadClaims result = serviceAt(95L).validate(token, RESOURCE);
        assertThat(result.getResource()).isEqualTo(RESOURCE);
    }

    // --- Resource ---

    @Test
    void shouldRejectWrongResource() {
        String token = serviceAt(GENERATION_TIME).generate(buildClaims()).value();

        assertThatThrownBy(() -> serviceAt(GENERATION_TIME).validate(token, "other-resource"))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessageContaining("resource");
    }
}
