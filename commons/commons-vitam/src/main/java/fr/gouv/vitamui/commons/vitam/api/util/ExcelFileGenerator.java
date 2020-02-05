/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.vitam.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public abstract class ExcelFileGenerator<D> {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    public abstract List<D> getData();

    public abstract List<String> getTitles();

    public abstract String getSheetName();

    public abstract String getFileName(String identificationElement);

    public byte[] createFile() throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        final Sheet sheet = workbook.createSheet(getSheetName());

        final CellStyle headerCellStyle = styleHeaderCells(workbook);
        addHeaderTitles(getTitles(), sheet, headerCellStyle);

        // insert data
        insertDataRows(sheet, getData(), Optional.empty());

        // Resize all columns to fit the content size
        for (int i = 0; i < getTitles().size(); i++) {
            sheet.autoSizeColumn(i);
        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // ByteArrayResource bar =  new ByteArrayResource(byteArray)
        // FileOutputStream fileOut = new FileOutputStream(getFileName());
        workbook.write(outputStream);
        outputStream.close();

        // Closing the workbook
        workbook.close();
        return outputStream.toByteArray();
    }

    public void createXlsFile(final List<D> data, final OutputStream xlsOutputStream) throws IOException {
        final Workbook workbook = new HSSFWorkbook();
        final Sheet sheet = workbook.createSheet(getSheetName());

        final CellStyle headerCellStyle = styleHeaderCells(workbook);
        addHeaderTitles(getTitles(), sheet, headerCellStyle);

        // insert data
        insertDataRows(sheet, data, Optional.of(styleDateCell(workbook, DATE_FORMAT)));

        // Resize all columns to fit the content size
        for (int i = 0; i < getTitles().size(); i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(xlsOutputStream);
        // Closing the workbook
        workbook.close();
    }

    private void addHeaderTitles(final List<String> titles, final Sheet sheet, final CellStyle headerCellStyle) {
        final Row headerRow = sheet.createRow(0);
        for (int i = 0; i < titles.size(); i++) {
            final Cell cell = headerRow.createCell(i);
            cell.setCellValue(titles.get(i));
            cell.setCellStyle(headerCellStyle);
        }
    }

    public CellStyle styleDateCell(final Workbook workbook, final String datePattern) {
        // Create Cell Style for formatting Date
        final CellStyle dateCellStyle = workbook.createCellStyle();
        final CreationHelper creationHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat(datePattern));

        return dateCellStyle;
    }

    protected abstract void insertDataRows(Sheet sheet, List<D> data, Optional<CellStyle> dateStyle);

    private CellStyle styleHeaderCells(final Workbook workbook) {
        final Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        final CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        return headerCellStyle;
    }
}
