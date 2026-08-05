package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.iam.auth.contract.PasswordPolicyDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La politique publiée doit être exactement celle qu'IAM applique.
 *
 * Le serveur d'authentification affiche les contraintes, IAM les vérifie ; tant que chacun lit sa propre
 * configuration, un écart entre les deux fichiers produit un formulaire qui accepte ce qu'IAM refusera.
 */
class CasServicePasswordPolicyTest {

    private final CasService casService = new CasService();

    @Test
    @DisplayName("les valeurs publiées sont celles de la configuration d'IAM")
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
    @DisplayName("les libellés suivent l'ordre de la configuration, caractères spéciaux intercalés")
    void flattensMessagesInConfigurationOrder() {
        final var specialChars = new PasswordConfiguration.SpecialChars();
        specialChars.setMessages(List.of("au moins un caractère spécial"));

        final var defaultConstraint = new PasswordConfiguration.PasswordDefaultConstraints();
        defaultConstraint.setMessages(List.of("au moins 12 caractères"));
        defaultConstraint.setSpecialChars(specialChars);

        final var customConstraint = new PasswordConfiguration.PasswordCustomConstraints();
        customConstraint.setMessages(List.of("pas de mot du dictionnaire"));

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
            "au moins 12 caractères",
            "au moins un caractère spécial",
            "pas de mot du dictionnaire"
        );
    }

    @Test
    @DisplayName("une configuration absente donne une politique vide plutôt qu'une erreur")
    void toleratesMissingConfiguration() {
        casService.setPasswordConfiguration(null);

        final PasswordPolicyDto policy = casService.getPasswordPolicy();

        assertThat(policy.getMinLength()).isNull();
        assertThat(policy.getProfile()).isNull();
        assertThat(policy.getMessages()).isEmpty();
    }
}
