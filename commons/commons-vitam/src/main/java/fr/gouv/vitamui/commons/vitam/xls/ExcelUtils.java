package fr.gouv.vitamui.commons.vitam.xls;

import fr.gouv.vitamui.commons.vitam.xls.dto.SheetDto;
import fr.gouv.vitamui.commons.vitam.xls.dto.ValueDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class ExcelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);
    private static final int FIRST_ELEMENT = 0;
    private static final int SECOND_ELEMENT = 1;
    private static final int MAX_ROWS_IN_MEMORY = 500;
    public static final String DATE_FORMAT_FR_WITH_SLASH = "dd/MM/yyyy";
    public static final DateTimeFormatter DATE_FORMATTER_FR_WITH_SLASH = DateTimeFormatter.ofPattern(
        DATE_FORMAT_FR_WITH_SLASH
    );

    private ExcelUtils() {}

    public static ByteArrayResource generateWorkbook(final List<SheetDto> sheets) throws IOException {
        LOGGER.info("Excel file generation generation process started");
        try (
            final SXSSFWorkbook workbook = new SXSSFWorkbook(MAX_ROWS_IN_MEMORY);
            final ByteArrayOutputStream fos = new ByteArrayOutputStream()
        ) {
            sheets.forEach(sheetDto -> {
                final List<String> columns = sheetDto.getColumns();
                final SXSSFSheet sheet = workbook.createSheet(sheetDto.getName());
                sheet.trackAllColumnsForAutoSizing();
                createHeaderSheet(sheet, columns, sheetDto.getColumnsFormat(), workbook);
                final AtomicInteger counterRow = new AtomicInteger(SECOND_ELEMENT);
                sheetDto
                    .getLines()
                    .forEach(line -> {
                        final SXSSFRow row = sheet.createRow(counterRow.get());
                        int currentMaxLineInRow = 1;
                        for (int i = 0; i < columns.size(); i++) {
                            final String column = columns.get(i);
                            final ValueDto val = line.get(column);
                            if (Objects.nonNull(val) && Objects.nonNull(val.getValue())) {
                                final SXSSFCell cell = row.createCell(i);
                                setValue(workbook, cell, val);
                                currentMaxLineInRow = getNumberOfLineInCell(sheetDto, currentMaxLineInRow, cell);
                            }
                        }
                        // To autosize rows in sheet by calculating the maximum number of lines that a cell has in one row
                        if (currentMaxLineInRow != 1) {
                            row.setHeightInPoints((row.getHeightInPoints() * currentMaxLineInRow));
                        }
                        counterRow.getAndIncrement();
                    });
                autoSizeColumns(sheet, columns);
            });

            workbook.write(fos);
            LOGGER.info("Excel file generation process successfully completed");
            return new ByteArrayResource(fos.toByteArray());
        }
    }

    private static void setDefaultColumnStyle(
        final SXSSFWorkbook workbook,
        final Sheet sheet,
        final int columnIndex,
        final String columnFormat
    ) {
        if (StringUtils.isNotBlank(columnFormat)) {
            final CellStyle cellStyle = workbook.createCellStyle();
            final DataFormat dataFormat = workbook.createDataFormat();
            cellStyle.setDataFormat(dataFormat.getFormat(columnFormat));
            sheet.setDefaultColumnStyle(columnIndex, cellStyle);
        }
    }

    private static int getNumberOfLineInCell(
        final SheetDto sheetDto,
        final int currentMaxLineInRow,
        final SXSSFCell cell
    ) {
        int numberOfLines = currentMaxLineInRow;
        if (sheetDto.isAutoSizeRows() && cell.getCellType().equals(CellType.STRING)) {
            numberOfLines = cell.getStringCellValue().split("\n").length;
            if (numberOfLines > 1) {
                final CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
                cellStyle.setWrapText(true);
                cell.setCellStyle(cellStyle);
            }
        }
        return Math.max(numberOfLines, currentMaxLineInRow);
    }

    private static void createHeaderSheet(
        final Sheet sheet,
        final List<String> columns,
        final Map<String, String> columnsFormat,
        final SXSSFWorkbook workbook
    ) {
        final CellStyle style = workbook.createCellStyle();
        final Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        final Row row = sheet.createRow(FIRST_ELEMENT);
        for (int i = 0; i < columns.size(); i++) {
            final Cell cell = row.createCell(i);
            final String column = columns.get(i);
            final String columnFormat = columnsFormat.get(column);
            cell.setCellValue(column);
            cell.setCellStyle(style);
            // Set default column style for all cells in the column
            setDefaultColumnStyle(workbook, sheet, i, columnFormat);
        }
    }

    private static void autoSizeColumns(final SXSSFSheet sheet, final List<String> columns) {
        for (int i = 0; i < columns.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void setValue(final Workbook workbook, final Cell cell, final ValueDto val) {
        switch (val.getType()) {
            case STRING:
                cell.setCellValue(String.valueOf(val.getValue()));
                break;
            case DOUBLE:
                cell.setCellValue(Double.parseDouble(val.getValue().toString()));
                break;
            case DATE:
                {
                    try {
                        final CellStyle cellStyle = workbook.createCellStyle();
                        final CreationHelper createHelper = workbook.getCreationHelper();
                        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT_FR_WITH_SLASH));
                        cell.setCellValue(LocalDate.parse(val.getValue().toString()));
                        cell.setCellStyle(cellStyle);
                    } catch (final DateTimeParseException dateTimeParseException) {
                        LOGGER.warn(
                            String.format(
                                "Cannot parse date %s. The raw value has been exported instead",
                                val.getValue()
                            ),
                            dateTimeParseException
                        );
                        cell.setCellValue(String.valueOf(val.getValue()));
                    }
                }
                break;
        }
    }

    public static XSSFWorkbook getExcelFile(final Path filePath) throws IOException {
        return new XSSFWorkbook(Files.newInputStream(filePath));
    }

    public static int getHeaderRowSize(final XSSFSheet sheet) {
        if (Objects.isNull(sheet) || Objects.isNull(sheet.getRow(0))) {
            return 0;
        } else {
            return sheet.getRow(0).getPhysicalNumberOfCells();
        }
    }

    public static boolean isRowEmpty(final Row row, final int maxColumnToCheck) {
        for (int i = 0; i < maxColumnToCheck; i++) {
            final Cell cell = row.getCell(i);
            if (!isCellEmpty(cell)) {
                return false;
            }
        }
        return true;
    }

    public static int getNumberOfNonEmptyRow(final XSSFSheet sheet) {
        int nbRow = 0;
        for (final Row row : sheet) {
            if (!isRowEmpty(row, getHeaderRowSize(sheet))) {
                nbRow++;
            }
        }
        return nbRow;
    }

    public static boolean isCellEmpty(final Cell cell) {
        if (Objects.isNull(cell)) {
            return true;
        }

        final boolean isBlankCell = cell.getCellType() == CellType.BLANK;
        final boolean isValueEmpty = getStringValue(cell).replace('\u00A0', ' ').isBlank();
        return isBlankCell || isValueEmpty;
    }

    public static String getStringValue(final Cell cell) {
        return new DataFormatter().formatCellValue(cell);
    }

    public static boolean isNumericValue(final Cell cell) {
        return StringUtils.isNumeric(getStringValue(cell));
    }

    public static double getNumericValue(final Cell cell) {
        return Double.parseDouble(getStringValue(cell));
    }

    public static String getFrWithSlashFormatDate(final Cell cell) {
        return cell.getLocalDateTimeCellValue().format(DATE_FORMATTER_FR_WITH_SLASH);
    }
}
