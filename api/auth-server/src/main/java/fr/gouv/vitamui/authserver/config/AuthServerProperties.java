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
    private List<Client> clients = List.of();
    private Token token = new Token();
    private Cors cors = new Cors();
    private PasswordReset passwordReset = new PasswordReset();

    @Data
    public static class Iam {
        private String baseUrl;
        private String internalApplicationId;
        // When set, the RestClient to IAM uses this Spring SSL bundle (`spring.ssl.bundle.jks.<name>`)
        // for mTLS — client keystore identifies us to IAM, truststore validates IAM's cert. Leave null
        // to fall back to the legacy trustAllCerts mode (dev only).
        private String sslBundle;
        private boolean trustAllCerts = false;
        // Skips hostname verification on outgoing mTLS requests to IAM. Needed in dev because the
        // certificate CN (iam) doesn't match the URL host (localhost). Never set to true in prod.
        private boolean disableHostnameVerification = false;
    }

    @Data
    public static class Client {
        private String clientId;
        private List<String> redirectUris = List.of();
        private List<String> postLogoutRedirectUris = List.of();
    }

    @Data
    public static class Token {
        private int accessTokenTtlMinutes = 240;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of();
    }

    @Data
    public static class PasswordReset {
        // TTL of a reset nonce — kept short to shrink the window in which a stolen inbox helps.
        private long ttlMinutes = 30;
        // TTL of a first-connection nonce — much longer than a reset, so a user who's away for a day
        // still has a chance to open the email from their real inbox.
        private long firstConnectionTtlHours = 24;
        // From address on outgoing reset emails.
        private String from = "no-reply@vitamui.local";
        // Public base URL of the auth-server (protocol + host + port). Used to build the reset link
        // embedded in the email. Must match how the browser reaches SAS or the link 404s.
        private String baseUrl;
    }
}
