package fr.gouv.vitamui.referential.internal.server.accesscontract;

import fr.gouv.vitamui.commons.api.dtos.ErrorImportFile;
import fr.gouv.vitamui.commons.api.enums.ErrorImportFileMessage;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessContractUtilsTest {

    private MultipartFile buildMultipartFileFromPath(String fileName) throws IOException {
        return new MockMultipartFile(
            fileName,
            fileName,
            "text/csv",
            getClass().getResourceAsStream("/data/" + fileName)
        );
    }

    @Test
    void check_should_throw_BadRequestException_with_multiple_errors() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath("import_access_contracts_invalid_bad_rows.csv");

        List<String> expectedArgs = List.of(
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('A')
                    .error(ErrorImportFileMessage.MANDATORY_VALUE)
                    .data(null)
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('B')
                    .error(ErrorImportFileMessage.MANDATORY_VALUE)
                    .data(null)
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('C')
                    .error(ErrorImportFileMessage.ISO_8859_1_ONLY)
                    .data("Not all text is created equal. Some is missing bytes. ðŸŒ")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('D')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('E')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('F')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('H')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('I')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("tutu")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('I')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("titi")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('L')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('M')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('M')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("tata")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(3)
                    .column('N')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> AccessContractCSVUtils.checkImportFile(file, true))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("Errors in rows found")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_throw_BadRequestException_with_bad_rows_length() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath("import_access_contracts_invalid_bad_rows_length.csv");

        List<String> expectedArgs = List.of(
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder().line(2).error(ErrorImportFileMessage.BAD_ROWS_LENGTH_IN_LINE).build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder().line(3).error(ErrorImportFileMessage.BAD_ROWS_LENGTH_IN_LINE).build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder().line(4).error(ErrorImportFileMessage.BAD_ROWS_LENGTH_IN_LINE).build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> AccessContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("Errors in rows found")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_throw_BadRequestException_with_empty_file() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath("import_access_contracts_invalid_empty_file.csv");

        List<String> expectedArgs = List.of(
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder().error(ErrorImportFileMessage.FILE_IS_EMPTY).build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> AccessContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("The file is empty")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_throw_BadRequestException_with_headers_name_not_matching() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath(
            "import_access_contracts_invalid_headers_name_not_matching.csv"
        );

        List<String> expectedArgs = List.of(
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(1)
                    .column('A')
                    .error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMN_NAME)
                    .data("Identifier")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(1)
                    .column('B')
                    .error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMN_NAME)
                    .data("Name*")
                    .build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> AccessContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("The headers names in the file does not match with the expected")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_throw_BadRequestException_with_headers_order_not_matching() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath(
            "import_access_contracts_invalid_headers_order_not_matching.csv"
        );

        List<String> expectedArgs = List.of(
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(1)
                    .column('A')
                    .error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMN_NAME)
                    .data("Identifier")
                    .build()
            ),
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(1)
                    .column('B')
                    .error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMN_NAME)
                    .data("Name*")
                    .build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> AccessContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("The headers names in the file does not match with the expected")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_throw_BadRequestException_with_only_headers() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath("import_access_contracts_invalid_only_headers.csv");

        List<String> expectedArgs = List.of(
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder().error(ErrorImportFileMessage.AT_LEAST_ONE_LINE).build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> AccessContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("No contract to import in the import file")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_not_throws_Exception() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath("import_access_contracts_valid.csv");

        // When
        // Then
        assertThatCode(() -> AccessContractCSVUtils.checkImportFile(file, false)).doesNotThrowAnyException();
    }

    @Test
    void check_should_throw_BadRequestException_with_too_much_headers() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath("import_access_contracts_invalid_too_much_headers.csv");

        List<String> expectedArgs = List.of(
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder().error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMNS_LENGTH).build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> AccessContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("The headers length in the file does not match with the expected")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_throw_BadRequestException_with_not_enough_headers() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath("import_access_contracts_invalid_not_enough_headers.csv");

        List<String> expectedArgs = List.of(
            AccessContractCSVUtils.errorToJson(
                ErrorImportFile.builder().error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMNS_LENGTH).build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> AccessContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("The headers length in the file does not match with the expected")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }
}
