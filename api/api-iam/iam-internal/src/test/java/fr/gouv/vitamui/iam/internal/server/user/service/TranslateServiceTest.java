package fr.gouv.vitamui.iam.internal.server.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.extension.ExtendWith;

class TranslateServiceTest {

    private final TranslateService translateService = new TranslateService();

    @ParameterizedTest
    @MethodSource("translateParameters")
    void should_translate_by_key(String key, String expectedValue) {
        String translated = translateService.translate(key);
        assertThat(translated).isEqualTo(expectedValue);
    }

    @Test
    void should_translate_boolean() {
        assertThat(translateService.translate(true)).isEqualTo("oui");
        assertThat(translateService.translate(false)).isEqualTo("non");
    }

    private static Stream<Arguments> translateParameters() {
        return Stream.of(Arguments.of("true", "oui"), Arguments.of("false", "non"), Arguments.of("FRENCH", "Français"), Arguments.of("ENGLISH", "Anglais"), Arguments.of("NOMINATIVE", "Nominatif"), Arguments.of("GENERIC", "Générique"), Arguments.of("ENABLED", "actif"), Arguments.of("DISABLED", "inactif"), Arguments.of("BLOCKED", "bloqué"), Arguments.of("REMOVED", "supprimé"), Arguments.of("ANONYM", "anonyme"), Arguments.of("unknown", "unknown"), Arguments.of(null, null));
    }
}
