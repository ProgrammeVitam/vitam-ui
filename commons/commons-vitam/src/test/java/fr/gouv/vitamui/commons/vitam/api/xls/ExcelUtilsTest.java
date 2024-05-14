package fr.gouv.vitamui.commons.vitam.api.xls;

import fr.gouv.vitamui.commons.vitam.xls.ExcelUtils;
import fr.gouv.vitamui.commons.vitam.xls.dto.SheetDto;
import fr.gouv.vitamui.commons.vitam.xls.dto.Type;
import fr.gouv.vitamui.commons.vitam.xls.dto.ValueDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelUtilsTest {

    @Test
    void should_generate_workbook_with_success() throws IOException {
        // Given
        final String column1 = "Lastname";
        final String column2 = "Firstname";
        final String column3 = "Age";
        final SheetDto sheet1 = new SheetDto();
        sheet1.setName("Sheet 1");
        sheet1.setColumns(List.of(column1, column2, column3));
        sheet1.setLines(Collections.emptyList());
        final SheetDto sheet2 = new SheetDto();
        sheet2.setName("Sheet 2");
        sheet2.setColumns(List.of(column1, column2));
        final List<Map<String, ValueDto>> lines2 = new ArrayList<>();
        lines2.add(
            Map.of(
                column1,
                ValueDto.builder().value("Jean").type(Type.STRING).build(),
                column2,
                ValueDto.builder().value("XXXX").type(Type.STRING).build()
            )
        );
        lines2.add(
            Map.of(
                column1,
                ValueDto.builder().value("Pierre").type(Type.STRING).build(),
                column2,
                ValueDto.builder().value("XXXX").type(Type.STRING).build()
            )
        );
        sheet2.setLines(lines2);
        final SheetDto sheet3 = new SheetDto();
        sheet3.setName("Sheet 3");
        sheet3.setColumns(List.of(column3));
        final List<Map<String, ValueDto>> lines3 = new ArrayList<>();
        lines3.add(Map.of(column3, ValueDto.builder().value(23).type(Type.DOUBLE).build()));
        lines3.add(Map.of(column3, ValueDto.builder().value(27).type(Type.DOUBLE).build()));
        sheet3.setLines(lines3);
        // When
        final ByteArrayResource file = ExcelUtils.generateWorkbook(List.of(sheet1, sheet2, sheet3));
        final XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        // Then
        assertThat(workbook.getNumberOfSheets()).isEqualTo(3);
        assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("Sheet 1");
        assertThat(workbook.getSheetAt(0).getPhysicalNumberOfRows()).isEqualTo(1);
        assertThat(workbook.getSheetAt(1).getSheetName()).isEqualTo("Sheet 2");
        assertThat(workbook.getSheetAt(1).getPhysicalNumberOfRows()).isEqualTo(3);
        assertThat(workbook.getSheetAt(2).getSheetName()).isEqualTo("Sheet 3");
        assertThat(workbook.getSheetAt(2).getPhysicalNumberOfRows()).isEqualTo(3);
    }

    @Test
    void should_generate_empty_workbook() throws IOException {
        // Given
        final ByteArrayResource file = ExcelUtils.generateWorkbook(Collections.emptyList());
        // When
        final XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        // Then
        assertThat(workbook.getNumberOfSheets()).isZero();
    }

    @Test
    void should_generate_workbook_with_one_empty_sheet() throws IOException {
        // Given
        final SheetDto sheet = new SheetDto();
        sheet.setName("Sheet 1");
        sheet.setColumns(List.of("column 1", "column 2"));
        sheet.setLines(Collections.emptyList());
        final ByteArrayResource file = ExcelUtils.generateWorkbook(List.of(sheet));
        // When
        final XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        // Then
        assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
        assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("Sheet 1");
        assertThat(workbook.getSheetAt(0).getPhysicalNumberOfRows()).isEqualTo(1);
    }

    @Test
    void should_generate_formatted_date() {
        //Given
        final XSSFWorkbook workbook = new XSSFWorkbook();
        final XSSFSheet sheet = workbook.createSheet();
        final Row row = sheet.createRow(0);
        final Cell cell = row.createCell(0);
        // When
        cell.setCellValue(Date.from(LocalDate.of(1999, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        // Then
        assertThat(ExcelUtils.getFrWithSlashFormatDate(cell)).isEqualTo("31/12/1999");
    }
}
