package fr.gouv.vitamui.cas.config;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the header masking applied to the IAM request log line of {@link AppConfig}.
 */
public class AppConfigTest {

    @Test
    public void masksThePasswordOfThePasswordChangeCall() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("username", "user@test.com");
        headers.add("Password", "N3wPassw0rd!");
        headers.add("customerId", "customer");

        final HttpHeaders masked = AppConfig.maskSensitiveHeaders(headers);

        assertThat(masked.get("Password")).containsExactly("***");
        assertThat(masked.get("username")).containsExactly("user@test.com");
        assertThat(masked.get("customerId")).containsExactly("customer");
    }

    @Test
    public void masksTheUserToken() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Token", "token");

        assertThat(AppConfig.maskSensitiveHeaders(headers).get("X-User-Token")).containsExactly("***");
    }
}
