package fr.gouv.vitamui.referential.internal.server.ingestcontract;

import fr.gouv.vitamui.commons.api.dtos.ErrorImportFile;
import fr.gouv.vitamui.commons.api.enums.ErrorImportFileMessage;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.referential.internal.server.utils.ImportCSVUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngestContractCSVUtilsTest {

    private MultipartFile buildMultipartFileFromPath(String path) throws IOException {
        File file = new File(path);
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile(file.getName(), file.getName(), "text/plain", IOUtils.toByteArray(input));
    }

    @Test
    void check_should_throw_BadRequestException_with_multiple_errors() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath(
            "src/test/resources/data/import_ingest_contracts_invalid_bad_rows.csv"
        );

        List<String> expectedArgs = List.of(
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('A')
                    .error(ErrorImportFileMessage.MANDATORY_VALUE)
                    .data(null)
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('B')
                    .error(ErrorImportFileMessage.MANDATORY_VALUE)
                    .data(null)
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('C')
                    .error(ErrorImportFileMessage.ISO_8859_1_ONLY)
                    .data("Not all text is created equal. Some is missing bytes. ðŸŒ")
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('D')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('F')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('I')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('J')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('M')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('N')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('O')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            ),
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(2)
                    .column('P')
                    .error(ErrorImportFileMessage.NOT_ALLOWED_VALUE)
                    .data("toto")
                    .build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> IngestContractCSVUtils.checkImportFile(file, true))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("Errors in rows found")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_throw_BadRequestException_with_only_headers() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath(
            "src/test/resources/data/import_ingest_contracts_invalid_only_headers.csv"
        );

        List<String> expectedArgs = List.of(
            ImportCSVUtils.errorToJson(
                ErrorImportFile.builder().error(ErrorImportFileMessage.AT_LEAST_ONE_LINE).build()
            )
        );

        // When
        // Then
        assertThatThrownBy(() -> IngestContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("No contract to import in the import file")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_throw_BadRequestException_with_headers_order_not_matching() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath(
            "src/test/resources/data/import_ingest_contracts_invalid_headers_order_not_matching.csv"
        );

        List<String> expectedArgs = List.of(
            IngestContractCSVUtils.errorToJson(
                ErrorImportFile.builder()
                    .line(1)
                    .column('A')
                    .error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMN_NAME)
                    .data("Identifier")
                    .build()
            ),
            IngestContractCSVUtils.errorToJson(
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
        assertThatThrownBy(() -> IngestContractCSVUtils.checkImportFile(file, false))
            .isInstanceOf(BadRequestException.class)
            .isNotNull()
            .hasMessageContaining("The headers names in the file does not match with the expected")
            .hasFieldOrPropertyWithValue("args", expectedArgs);
    }

    @Test
    void check_should_not_throws_Exception() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath("src/test/resources/data/import_ingest_contracts_valid.csv");

        // When
        // Then
        assertThatCode(() -> IngestContractCSVUtils.checkImportFile(file, false)).doesNotThrowAnyException();
    }

    @Test
    void check_should_not_throws_Exception_with_wrong_ids() throws IOException {
        // Given
        MultipartFile file = buildMultipartFileFromPath(
            "src/test/resources/data/import_ingest_contracts_invalid_wrong_ids.csv"
        );

        // When
        // Then
        assertThatCode(() -> IngestContractCSVUtils.checkImportFile(file, false)).doesNotThrowAnyException();
    }
}
