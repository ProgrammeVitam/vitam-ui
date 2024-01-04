package fr.gouv.vitamui.iam.internal.server.user.service;

import fr.gouv.vitamui.commons.vitam.xls.ExcelFileGenerator;
import fr.gouv.vitamui.iam.common.dto.ConnectionHistoryDto;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectionHistoryExportService extends ExcelFileGenerator<ConnectionHistoryDto> {

    public static final List<String> CONNECTION_HISTORY_EXPORT_TITLE = List.of(
        "ID de l’utilisateur",
        "Date de connexion",
        "Heure de connexion"
    );

    private List<ConnectionHistoryDto> data;

    @Override
    public List<ConnectionHistoryDto> getData() {
        return this.data;
    }

    @Override
    public List<String> getTitles() {
        return CONNECTION_HISTORY_EXPORT_TITLE;
    }

    @Override
    public String getSheetName() {
        return "Données";
    }

    @Override
    public String getFileName(String identificationElement) {
        return String.format("reports-export-connection-%s.xlsx", DATE_TIME_FORMATTER_ISO_WITH_MS.format(Instant.now()));
    }

    @Override
    protected void insertDataRows(Sheet sheet, List<ConnectionHistoryDto> data, Optional<CellStyle> dateStyle) {
        int rowIndex = 1;
        for(ConnectionHistoryDto element : data) {
            final Row row = sheet.createRow(rowIndex);
            insertRow(row, element, dateStyle);
            rowIndex += 1;
        }
    }

    public Resource generateWorkbook(final List<ConnectionHistoryDto> data) throws IOException {
        this.data  = data;
        var output =  this.createFile();
        return new ByteArrayResource(output);
    }

    public void insertRow(Row row, ConnectionHistoryDto data, Optional<CellStyle> dateStyle) {
        cellValueOf(row, 0, data.getUserId());

        if(data.getConnectionDateTime() == null ){
            return;
        }

        Instant instant = data.getConnectionDateTime().toInstant();
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

        cellValueOf(row, 1, ldt.toLocalDate(), dateStyle);
        cellValueOf(row, 2, ldt.toLocalTime().toString());
    }

}
