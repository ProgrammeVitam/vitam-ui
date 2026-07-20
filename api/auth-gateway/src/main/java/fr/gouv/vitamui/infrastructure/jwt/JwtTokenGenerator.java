package fr.gouv.vitamui.infrastructure.jwt;

import fr.gouv.vitamui.domain.SecurityContext;
import fr.gouv.vitamui.domain.ports.TokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.HOURS;

@Component
public class JwtTokenGenerator implements TokenGenerator {

    @Value("${auth-gateway.access-token.issuer}")
    private String issuer;

    private final JwtEncoder encoder;

    public JwtTokenGenerator(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String generate(SecurityContext context) {
        Instant now = Instant.now();
        Instant expiry = now.plus(1, HOURS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(expiry)
            .subject(context.authenticatedUser().id().toString())
            .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

