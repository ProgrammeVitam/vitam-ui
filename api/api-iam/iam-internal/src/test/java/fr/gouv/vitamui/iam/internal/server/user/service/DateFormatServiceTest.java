package fr.gouv.vitamui.iam.internal.server.user.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DateFormatServiceTest {

    private final DateFormatService dateFormatService = new DateFormatService();

    @ParameterizedTest
    @MethodSource("dateParameters")
    void should_format_date(String text, String expectedValue) {
        assertThat(dateFormatService.formatDate(text)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("timeParameters")
    void should_format_time(String text, String expectedValue) {
        assertThat(dateFormatService.formatTime(text)).isEqualTo(expectedValue);
    }

    private static Stream<Arguments> dateParameters() {
        return Stream.of(
            Arguments.of("2023-06-13T13:11:36.418", "13/06/2023"),
            Arguments.of("2023-06", null),
            Arguments.of(null, null)
        );
    }

    private static Stream<Arguments> timeParameters() {
        return Stream.of(
            Arguments.of("2023-06-13T13:11:36.418", "13:11:36"),
            Arguments.of("2023-06", null),
            Arguments.of(null, null)
        );
    }
}
