package fr.gouv.vitamui.referential.server.service.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ExportCSVUtilsTest {

    private static List<Arguments> inputAndExpected() {
        return Arrays.asList(
            arguments(new String[] { "A", "B", "C" }, new String[] { "A", "B", "C" }),
            arguments(new String[] { "A", null, "C" }, new String[] { "A", null, "C" }),
            arguments(new String[] { "A", "+1+1", "C" }, new String[] { "A", "\t+1+1", "C" }),
            arguments(new String[] { "A", "-1+1", "C" }, new String[] { "A", "\t-1+1", "C" }),
            arguments(new String[] { "A", "=1+1", "C" }, new String[] { "A", "\t=1+1", "C" }),
            arguments(new String[] { "A", "@1+1", "C" }, new String[] { "A", "\t@1+1", "C" })
        );
    }

    @ParameterizedTest
    @MethodSource("inputAndExpected")
    public void test_escapeCSVFields(String[] input, String[] expected) {
        assertThat(ExportCSVUtils.escapeCSVFields(input))
            .isEqualTo(expected)
            .withFailMessage("Expected " + Arrays.toString(expected) + " but got " + Arrays.toString(expected));
    }
}
