package fr.gouv.vitamui.domain.ports;

import fr.gouv.vitamui.domain.Identity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IdentityProviderResolver {

    private final List<IdentityProvider> providers = new ArrayList<>();
    private final JwtDecoder jwtDecoder;

    public IdentityProviderResolver(JwtDecoder jwtDecoder, List<IdentityProvider> providers) {
        this.jwtDecoder = jwtDecoder;
        if (providers != null) {
            this.providers.addAll(providers);
        }
    }

    public Identity authenticate(String token) {
        // 1. Décoder et valider le jeton via Spring Security (Signature, Expiration, Issuer)
        Jwt jwt = jwtDecoder.decode(token);
        String issuer = jwt.getIssuer().toString();

        // 2. Trouver le provider qui supporte cet émetteur
        return providers
            .stream()
            .filter(p -> p.supports(issuer))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No provider found for issuer: " + issuer))
            .authenticate(token);
    }
}
