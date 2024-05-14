package fr.gouv.vitamui.commons.utils;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurePathUtilsTest {

    @Test
    @ParameterizedTest
    @ValueSource(
        strings = {
            "../file.pdf",
            "/../etc/hosts/file.pdf",
            "file.pdf\n../../mmm",
            "file.pdf\r../../mmm",
            "/file\0.exe.pdf",
            "file..pdf",
        }
    )
    void testBuildFullPathKo(String untrustedFileName) {
        assertThrows(SecurityException.class, () -> {
            SecurePathUtils.buildFilePath("/tmp", untrustedFileName);
        });
    }

    @Test
    @ParameterizedTest
    @MethodSource("testBuildFullPathOkParameters")
    void testBuildFullPathOk(String trustedPartialPath, String untrustedFileName, String expectedResult) {
        var fullPath = SecurePathUtils.buildFilePath(trustedPartialPath, untrustedFileName);
        assertEquals(expectedResult, fullPath);
    }

    @Test
    @ParameterizedTest
    @Disabled
    @MethodSource("buildFullSecuredFilePathOkParameters")
    void buildFullSecuredFilePathOk(
        String trustedPartialPath,
        String untrustedBasePath,
        String untrustedFileName,
        String expectedResult
    ) {
        assertEquals(
            expectedResult,
            SecurePathUtils.buildFullSecuredFilePath(trustedPartialPath, untrustedBasePath, untrustedFileName)
        );
    }

    @Test
    @ParameterizedTest
    @MethodSource("buildFullSecuredFilePathKoParameters")
    void buildFullSecuredFilePathKo(String trustedPartialPath, String untrustedBasePath, String untrustedFileName) {
        assertThrows(SecurityException.class, () -> {
            SecurePathUtils.buildFullSecuredFilePath(trustedPartialPath, untrustedBasePath, untrustedFileName);
        });
    }

    private static Stream<Arguments> testBuildFullPathOkParameters() {
        return Stream.of(
            Arguments.of("/tmp", "item1.pdf", "/tmp/item1.pdf"),
            Arguments.of("/tmp", "item1.pdf", "/tmp/item1.pdf"),
            Arguments.of("/tmp", "item1.pdf", "/tmp/item1.pdf"),
            Arguments.of("/tmp", "item1.pdf", "/tmp/item1.pdf"),
            Arguments.of("tmp", "item1.pdf", "tmp/item1.pdf"),
            Arguments.of("tmp", "item1.pdf", "tmp/item1.pdf"),
            Arguments.of("tmp", "item1.pdf", "tmp/item1.pdf"),
            Arguments.of("tmp", "item1.pdf", "tmp/item1.pdf"),
            Arguments.of("tmp/", "item1.pdf", "tmp/item1.pdf"),
            Arguments.of("tmp/", "item1.pdf", "tmp/item1.pdf"),
            Arguments.of("tmp/", "item1.pdf", "tmp/item1.pdf"),
            Arguments.of("tmp/", "item1.pdf", "tmp/item1.pdf")
        );
    }

    private static Stream<Arguments> buildFullSecuredFilePathOkParameters() {
        return Stream.of(
            Arguments.of("/tmp", "/client1/", "item1.pdf", "/tmp/client1/item1.pdf"),
            Arguments.of("/tmp", "/client1", "item1.pdf", "/tmp/client1/item1.pdf"),
            Arguments.of("/tmp", "client1/", "item1.pdf", "/tmp/client1/item1.pdf"),
            Arguments.of("/tmp", "client1", "item1.pdf", "/tmp/client1/item1.pdf"),
            Arguments.of("tmp", "/client1/", "item1.pdf", "tmp/client1/item1.pdf"),
            Arguments.of("tmp", "/client1", "item1.pdf", "tmp/client1/item1.pdf"),
            Arguments.of("tmp", "client1/", "item1.pdf", "tmp/client1/item1.pdf"),
            Arguments.of("tmp", "client1", "item1.pdf", "tmp/client1/item1.pdf"),
            Arguments.of("tmp/", "/client1/", "item1.pdf", "tmp/client1/item1.pdf"),
            Arguments.of("tmp/", "/client1", "item1.pdf", "tmp/client1/item1.pdf"),
            Arguments.of("tmp/", "client1/", "item1.pdf", "tmp/client1/item1.pdf"),
            Arguments.of("tmp/", "client1", "item1.pdf", "tmp/client1/item1.pdf")
        );
    }

    private static Stream<Arguments> buildFullSecuredFilePathKoParameters() {
        return Stream.of(
            Arguments.of("/tmp", "../etc/", "toto.pdf"),
            Arguments.of("/tmp", "/client1\0/..", "toto.pdf"),
            Arguments.of("/tmp", "/client1", "toto.exe\0.pdf"),
            Arguments.of("/tmp", "/client1", "../../etc/toto.pdf"),
            Arguments.of("tmp", "../etc/", "toto.pdf"),
            Arguments.of("tmp", "/client1\0/..", "toto.pdf"),
            Arguments.of("tmp", "/client1", "toto.exe\0.pdf"),
            Arguments.of("tmp", "/client1", "../../etc/toto.pdf"),
            Arguments.of("tmp/", "../etc/", "toto.pdf"),
            Arguments.of("tmp/", "/client1\0/..", "toto.pdf"),
            Arguments.of("tmp/", "/client1", "toto.exe\0.pdf"),
            Arguments.of("tmp/", "/client1", "../../etc/toto.pdf")
        );
    }
}
