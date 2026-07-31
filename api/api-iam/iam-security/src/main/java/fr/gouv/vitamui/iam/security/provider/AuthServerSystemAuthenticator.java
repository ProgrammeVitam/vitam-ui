/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.iam.security.provider;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.authentication.AuthenticationToken;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * Recognises the Spring Authorization Server as a peer service when it presents its client certificate
 * on the {@code /iam/v1/cas/*} endpoints. When the certificate's subject CN matches one of the
 * accepted CNs (configured via {@code iam.auth-server.accepted-cns}, default {@code cas-server}),
 * this authenticator returns a synthetic {@link AuthenticationToken} carrying {@link ServicesData#ROLE_SYSTEM_SAS}
 * so the downstream {@code @Secured} annotations can guard those endpoints without requiring a user token.
 *
 * <p>Returns {@code null} when the certificate is absent or the CN doesn't match — letting the normal
 * internal/external authentication path take over.
 *
 * <p>Not a {@code @Service}: this package is not component-scanned by the {@code iam} Spring Boot
 * application. Instantiated manually as a {@code @Bean} in {@code ApiIamServerConfig}, like the
 * other providers in this package.
 */
public class AuthServerSystemAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServerSystemAuthenticator.class);

    private final Set<String> acceptedCns;

    public AuthServerSystemAuthenticator(String acceptedCnsCsv) {
        this.acceptedCns = Set.of(acceptedCnsCsv.split("\\s*,\\s*"));
        LOGGER.info("Auth-server mTLS peer recognised for CNs: {}", acceptedCns);
    }

    public Authentication tryAuthenticate(HttpContext httpContext, X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        String cn = extractCn(certificate.getSubjectX500Principal());
        if (cn == null || !acceptedCns.contains(cn)) {
            return null;
        }

        AuthUserDto systemUser = new AuthUserDto();
        systemUser.setIdentifier("system-sas");
        systemUser.setEmail("system@auth-server.local");
        // Level "" grants access to all levels — this is the historical VitamUI convention for a
        // technical/system principal. The endpoint whitelist and @Secured("ROLE_SYSTEM_SAS") guard
        // it away from real users.
        systemUser.setLevel("");
        systemUser.setCustomerId(null);
        // Reuse the pre-auth HttpContext when available, otherwise let it be null — the ROLE_SYSTEM_SAS
        // paths don't need the tenant/token machinery.
        HttpContext contextForToken = httpContext;
        AuthenticationToken token = new AuthenticationToken(
            systemUser,
            contextForToken,
            certificate,
            List.of(ServicesData.ROLE_SYSTEM_SAS)
        );
        LOGGER.debug("Authenticated auth-server system peer (CN={})", cn);
        return token;
    }

    private static String extractCn(X500Principal principal) {
        String dn = principal.getName();
        for (String part : dn.split(",")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("CN=")) {
                return trimmed.substring(3);
            }
        }
        return null;
    }
}
