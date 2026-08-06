package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.iam.auth.contract.PasswordPolicyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The published policy must be exactly the one IAM enforces.
 *
 * The authentication server displays the constraints and IAM checks them; as long as each reads its own
 * configuration, any drift between the two files yields a form that accepts what IAM will reject.
 */
class CasServicePasswordPolicyTest {

    private final CasService casService = new CasService();

    @Test
    @DisplayName("the published values are the ones from IAM's configuration")
    void publishesConfiguredValues() {
        final PasswordConfiguration configuration = new PasswordConfiguration();
        configuration.setLength(14);
        configuration.setProfile("custom");
        configuration.setMaxOldPassword(5);
        casService.setPasswordConfiguration(configuration);

        final PasswordPolicyDto policy = casService.getPasswordPolicy();

        assertThat(policy.getMinLength()).isEqualTo(14);
        assertThat(policy.getProfile()).isEqualTo("custom");
        assertThat(policy.getMaxOldPassword()).isEqualTo(5);
    }

    @Test
    @DisplayName("labels follow configuration order, with special characters interleaved")
    void flattensMessagesInConfigurationOrder() {
        final var specialChars = new PasswordConfiguration.SpecialChars();
        specialChars.setMessages(List.of("at least one special character"));

        final var defaultConstraint = new PasswordConfiguration.PasswordDefaultConstraints();
        defaultConstraint.setMessages(List.of("at least 12 characters"));
        defaultConstraint.setSpecialChars(specialChars);

        final var customConstraint = new PasswordConfiguration.PasswordCustomConstraints();
        customConstraint.setMessages(List.of("no dictionary word"));

        final var defaults = new LinkedHashMap<String, PasswordConfiguration.PasswordDefaultConstraints>();
        defaults.put("length", defaultConstraint);
        final var customs = new LinkedHashMap<String, PasswordConfiguration.PasswordCustomConstraints>();
        customs.put("dictionary", customConstraint);

        final var constraints = new PasswordConfiguration.PasswordConstraints();
        constraints.setDefaults(defaults);
        constraints.setCustoms(customs);

        final PasswordConfiguration configuration = new PasswordConfiguration();
        configuration.setConstraints(constraints);
        casService.setPasswordConfiguration(configuration);

        assertThat(casService.getPasswordPolicy().getMessages()).containsExactly(
            "at least 12 characters",
            "at least one special character",
            "no dictionary word"
        );
    }

    @Test
    @DisplayName("a missing configuration yields an empty policy rather than an error")
    void toleratesMissingConfiguration() {
        casService.setPasswordConfiguration(null);

        final PasswordPolicyDto policy = casService.getPasswordPolicy();

        assertThat(policy.getMinLength()).isNull();
        assertThat(policy.getProfile()).isNull();
        assertThat(policy.getMessages()).isEmpty();
    }
}
