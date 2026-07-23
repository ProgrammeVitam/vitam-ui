/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "vitamui.auth-server")
public class AuthServerProperties {

    private String issuer;
    private Iam iam = new Iam();
    private PortalClient portalClient = new PortalClient();
    private Token token = new Token();
    private Cors cors = new Cors();

    @Data
    public static class Iam {
        private String baseUrl;
        private String internalApplicationId;
        private boolean trustAllCerts = false;
    }

    @Data
    public static class PortalClient {
        private String clientId;
        private String redirectUri;
        private String postLogoutRedirectUri;
    }

    @Data
    public static class Token {
        private int accessTokenTtlMinutes = 240;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of();
    }
}
