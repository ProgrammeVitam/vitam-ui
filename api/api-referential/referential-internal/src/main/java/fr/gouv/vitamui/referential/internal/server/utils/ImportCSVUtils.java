package fr.gouv.vitamui.referential.internal.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import fr.gouv.vitam.common.model.administration.ContextStatus;
import fr.gouv.vitam.common.model.administration.DataObjectVersionType;
import fr.gouv.vitam.common.model.administration.RuleType;
import fr.gouv.vitamui.commons.api.dtos.ErrorImportFile;
import fr.gouv.vitamui.commons.api.enums.ErrorImportFileMessage;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ImportCSVUtils {

    private static final long MAX_OCTET_FILE_SIZE = 2_000_000;

    @Builder
    @Getter
    @Setter
    protected static class ColumnDetails {
        private int index;
        private String columnName;
        private boolean mandatory;
        private ColumnType columnType;
    }

    protected enum ColumnType {
        STRING,
        CONTEXT_STATUS,
        A_BOOLEAN,
        RULE_TYPE,
        DATA_OBJECT_VERSION_TYPE,
        CHECK_PARENT_LINK_TYPE
    }

    protected static void checkImportFile(MultipartFile file, List<ColumnDetails> expectedColumns) {

        checkFileSize(file);

        List<String[]> contracts = parseFileToContracts(file);

        checkHeaders(contracts.remove(0), expectedColumns);
        checkIfEmptyFile(contracts);

        List<ErrorImportFile> lineErrors = new ArrayList<>();

        int line = 2;
        for (String[] contract : contracts) {
            checkLine(contract, line, expectedColumns, lineErrors);
            line++;
        }

        if (!lineErrors.isEmpty()) {
            throw new BadRequestException("Errors in rows found", null, lineErrors.stream().map(ImportCSVUtils::errorToJson).collect(Collectors.toList()));
        }

    }

    private static void checkFileSize(MultipartFile file) {
        if (file.getSize() == 0) {
            throw new BadRequestException("The file is empty", null, List.of(errorToJson(ErrorImportFile.builder().error(ErrorImportFileMessage.FILE_IS_EMPTY).build())));
        }
        if (file.getSize() > MAX_OCTET_FILE_SIZE) {
            throw new BadRequestException("The size of the file is too big be imported", null, List.of(errorToJson(ErrorImportFile.builder().error(ErrorImportFileMessage.FILE_SIZE_TOO_BIG).build())));
        }
    }

    private static List<String[]> parseFileToContracts(MultipartFile file) {

        try (Reader fileReader = new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8)) {

            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withCSVParser(parser).build();
            return csvReader.readAll();

        } catch (IOException | CsvException e) {
            throw new BadRequestException("Unable to read the CSV file " + file.getOriginalFilename(), e);
        }
    }

    private static void checkHeaders(String[] headersLine, List<ColumnDetails> expectedColumns) {
        checkHeadersLength(headersLine, expectedColumns);
        checkHeadersName(headersLine, expectedColumns);

    }

    private static void checkHeadersLength(String[] headersLine, List<ColumnDetails> expectedColumns) {
        if (headersLine.length == 1) {
            throw new BadRequestException("Only one header found in the file", null, List.of(errorToJson(ErrorImportFile.builder().error(ErrorImportFileMessage.ONLY_ONE_HEADER_INCORRECT_SEPARATOR).data(headersLine[0]).build())));
        }
        if (headersLine.length != expectedColumns.size()) {
            throw new BadRequestException("The headers length in the file does not match with the expected", null, List.of(errorToJson(ErrorImportFile.builder().error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMNS_LENGTH).build())));
        }
    }

    private static void checkHeadersName(String[] headersLine, List<ColumnDetails> expectedColumns) {

        List<ErrorImportFile> headersNameErrors = new ArrayList<>();
        AtomicInteger headerIndex = new AtomicInteger(0);

        while (headerIndex.intValue() < headersLine.length) {

            expectedColumns.stream().filter(expectedColumn -> expectedColumn.getIndex() == headerIndex.intValue()).findFirst().ifPresentOrElse(
                (columnDetails -> {
                    if (!columnDetails.getColumnName().equals(headersLine[headerIndex.intValue()])) {
                        headersNameErrors.add(ErrorImportFile.builder().column(numberToLetter(headerIndex.intValue())).line(1).data(columnDetails.getColumnName()).error(ErrorImportFileMessage.FILE_MUST_RESPECT_COLUMN_NAME).build());
                    }
                }),
                () ->
                {
                    throw new InternalServerException("The header at " + headerIndex.intValue() + " position in the import file does not match with the expected columns!");
                });

            headerIndex.incrementAndGet();
        }

        if (!headersNameErrors.isEmpty()) {
            throw new BadRequestException("The headers names in the file does not match with the expected", null, headersNameErrors.stream().map(ImportCSVUtils::errorToJson).collect(Collectors.toList()));
        }
    }

    private static void checkIfEmptyFile(List<String[]> contracts) {
        if (contracts.isEmpty()) {
            throw new BadRequestException("No contract to import in the import file", null, List.of(errorToJson(ErrorImportFile.builder().error(ErrorImportFileMessage.AT_LEAST_ONE_LINE).build())));
        }
    }

    private static void checkLine(String[] line, int lineNumber, List<ColumnDetails> expectedColumns, List<ErrorImportFile> lineErrors) {

        if (line.length != expectedColumns.size()) {
            lineErrors.add(ErrorImportFile.builder().line(lineNumber).error(ErrorImportFileMessage.BAD_ROWS_LENGTH_IN_LINE).build());
            return;
        }

        for (int rowNumber = 0; rowNumber < line.length; rowNumber++) {
            checkRow(lineNumber, rowNumber, line[rowNumber], expectedColumns, lineErrors);
        }

    }

    private static void checkRow(int lineNumber, int rowNumber, String value, List<ColumnDetails> expectedColumns, List<ErrorImportFile> lineErrors) {

        if (!StandardCharsets.ISO_8859_1.newEncoder().canEncode(value)) {
            lineErrors.add(ErrorImportFile.builder().column(numberToLetter(rowNumber)).line(lineNumber).data(value).error(ErrorImportFileMessage.ISO_8859_1_ONLY).build());
        }

        ColumnDetails columnDetails = expectedColumns.stream().filter(expectedColumn -> expectedColumn.getIndex() == rowNumber).findFirst().orElseThrow(() ->
            new InternalServerException("The row " + rowNumber + " in the import file does not match with the expected columns!")
        );

        if (columnDetails.isMandatory() && StringUtils.isBlank(value)) {
            lineErrors.add(ErrorImportFile.builder().column(numberToLetter(rowNumber)).line(lineNumber).error(ErrorImportFileMessage.MANDATORY_VALUE).build());
        }

        if (!StringUtils.isBlank(value) && columnDetails.getColumnType().equals(ColumnType.CONTEXT_STATUS) && !EnumUtils.isValidEnum(ContextStatus.class, value)) {
            lineErrors.add(ErrorImportFile.builder().column(numberToLetter(rowNumber)).line(lineNumber).data(value).error(ErrorImportFileMessage.NOT_ALLOWED_VALUE).build());
        }

        if (!StringUtils.isBlank(value) && columnDetails.getColumnType().equals(ColumnType.A_BOOLEAN) && !("true".equals(value) || "false".equals(value))) {
            lineErrors.add(ErrorImportFile.builder().column(numberToLetter(rowNumber)).line(lineNumber).data(value).error(ErrorImportFileMessage.NOT_ALLOWED_VALUE).build());
        }

        if (!StringUtils.isBlank(value) && columnDetails.getColumnType().equals(ColumnType.DATA_OBJECT_VERSION_TYPE)) {
            Arrays.stream(value.split("\\|")).filter(dataObjectVersion -> DataObjectVersionType.fromName(dataObjectVersion.trim()) == null).forEach(dataObjectVersion ->
                lineErrors.add(ErrorImportFile.builder().column(numberToLetter(rowNumber)).line(lineNumber).data(dataObjectVersion.trim()).error(ErrorImportFileMessage.NOT_ALLOWED_VALUE).build())
            );
        }

        if (!StringUtils.isBlank(value) && columnDetails.getColumnType().equals(ColumnType.RULE_TYPE)) {
            Arrays.stream(value.split("\\|")).filter(ruleType -> RuleType.getEnumFromName(ruleType.trim()) == null).forEach(ruleType ->
                lineErrors.add(ErrorImportFile.builder().column(numberToLetter(rowNumber)).line(lineNumber).data(ruleType.trim()).error(ErrorImportFileMessage.NOT_ALLOWED_VALUE).build())
            );
        }

        if (!StringUtils.isBlank(value) && columnDetails.getColumnType().equals(ColumnType.CHECK_PARENT_LINK_TYPE) && !EnumUtils.isValidEnum(CheckParentLink.class, value)) {
            lineErrors.add(ErrorImportFile.builder().column(numberToLetter(rowNumber)).line(lineNumber).data(value).error(ErrorImportFileMessage.NOT_ALLOWED_VALUE).build());
        }

    }

    private static char numberToLetter(int i) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        if (i >= 0 && i <= 25) {
            return alphabet.substring(i, i + 1).charAt(0);
        }
        return '?';
    }

    public static String errorToJson(ErrorImportFile errorImportFile) {
        try {
            return JsonUtils.toJson(errorImportFile);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("The object " + errorImportFile + " could not have been parsed into a JSON String");
        }
    }

}
