package fr.gouv.vitamui.commons.vitam.api.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EvIdAppSessionParserTest {

    @ParameterizedTest
    @MethodSource("parseUserIdParameters")
    void should_parse_context(String evIdAppSession, String expectedContext) {
        assertThat(EvIdAppSessionParser.parseApiContextId(evIdAppSession)).isEqualTo(expectedContext);
    }

    @ParameterizedTest
    @MethodSource("parseUserIdParameters")
    void should_parse_user_id(String evIdAppSession, String ignoredExpectedContext, String expectedUserId) {
        assertThat(EvIdAppSessionParser.parseUserId(evIdAppSession)).isEqualTo(expectedUserId);
    }

    @ParameterizedTest
    @MethodSource("parseUserIdParameters")
    void should_parse_user_id(
        String evIdAppSession,
        String ignoredExpectedContext,
        String ignoredExpectedUserId,
        String expectedSubrogatorId
    ) {
        assertThat(EvIdAppSessionParser.parseSubrogatorId(evIdAppSession)).isEqualTo(expectedSubrogatorId);
    }

    private static Stream<Arguments> parseUserIdParameters() {
        return Stream.of(
            Arguments.of(
                "CUSTOMERS_APP:02e97361-9ea0-403b-a208-ec7fc8672d73:Contexte UI Identity:101:-:1",
                "Contexte UI Identity",
                "101",
                null
            ),
            Arguments.of(
                "CUSTOMERS_APP:02e97361-9ea0-403b-a208-ec7fc8672d73:Contexte UI Identity:102:42:1",
                "Contexte UI Identity",
                "102",
                "42"
            ),
            Arguments.of(
                "CUSTOMERS_APP:02e97361-9ea0-403b-a208-ec7fc8672d73:Contexte UI Identity",
                "Contexte UI Identity",
                null,
                null
            ),
            Arguments.of(
                "CUSTOMERS_APP:02e97361-9ea0-403b-a208-ec7fc8672d73:Contexte UI Identity:101",
                "Contexte UI Identity",
                "101",
                null
            ),
            Arguments.of("CUSTOMERS_APP", null, null, null),
            Arguments.of(null, null, null, null)
        );
    }
}
