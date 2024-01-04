package fr.gouv.vitamui.iam.internal.server.user.service;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookEventDto;
import fr.gouv.vitamui.commons.vitam.xls.ExcelFileGeneratorUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitamui.commons.logbook.common.EventType.*;

@Service
@RequiredArgsConstructor
public class UserExportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> SHEETS = List.of("Liste_utilisateurs", "Historique_modifications");
    private static final List<String> USER_LIST_COLUMNS_TITLES = List.of("Identifiant du user", "Nom", "Prénom", "Adresse e-mail", "Numéro de mobile",
        "Numéro de fixe", "Adresse ", "Code postal", "Ville", "Pays", "Code du centre", "Code du site", "Code interne", "Langue de l’interface",
        "Niveau du groupe ", "Groupe de profils", "Type (nominatif ou générique)", "Compte subrogeable par le support (O/N)",
        "Validation en deux étapes (O/N)", "Mise à jour automatique SSO", "Date de dernière connexion", "Compte (actif / inactif)"
    );

    private static final List<String> USER_OPERATIONS_COLUMNS_TITLES = List.of("Identifiant du user", "Date d’activité",
        "Heure d’activité", "Activité réalisée par le user", "Activité", "Ancienne valeur", "Nouvelle valeur");

    private  static  final List<EventType> EXT_VITAMUI_CREATE_EVENTS = List.of(
        EXT_VITAMUI_CREATE_USER,
        EXT_VITAMUI_CREATE_USER_INFO,
        EXT_VITAMUI_CREATE_OWNER
    );

    private final TranslateService translateService;
    private final DateFormatService dateFormatService;
    private final OperationParser operationParser;

    public void createXlsxFile(List<UserDto> data, List<LogbookEventDto> userOperations, Map<String, String> userInfoLangMap, Map<String, String> userGroupNameMap, OutputStream xlsOutputStream) throws IOException {
        final Workbook workbook = new XSSFWorkbook();
        SHEETS.forEach(workbook::createSheet);

        final Sheet userInformationSheet = workbook.getSheetAt(0);
        buildUserListSheet(workbook, userInformationSheet, data, userInfoLangMap, userGroupNameMap);

        final Sheet userOperationSheet = workbook.getSheetAt(1);
        buildUserOperationsSheet(workbook, userOperationSheet, userOperations);

        workbook.write(xlsOutputStream);
        workbook.close();
    }

    private void buildUserOperationsSheet(Workbook workbook, Sheet sheet, List<LogbookEventDto> userOperations) {
        ExcelFileGeneratorUtils.addHeaderTitles(USER_OPERATIONS_COLUMNS_TITLES, sheet, styleHeaderCells(workbook));
        insertUserOperationRows(sheet, userOperations);
        ExcelFileGeneratorUtils.resizeColumns(sheet, USER_OPERATIONS_COLUMNS_TITLES.size());

    }

    private void insertUserOperationRows(Sheet sheet, List<LogbookEventDto> userOperations) {
        var rowNum = 1;
        for (LogbookEventDto operation : userOperations) {
            var row = sheet.createRow(rowNum++);
            insertUserOperationColumns(operation, row);
        }
    }

    private void insertUserOperationColumns(LogbookEventDto operation, Row row) {
        row.createCell(0).setCellValue(operation.getObId());
        String operationDateTime = operation.getEvDateTime();
        row.createCell(1).setCellValue(dateFormatService.formatDate(operationDateTime));
        row.createCell(2).setCellValue(dateFormatService.formatTime(operationDateTime));
        row.createCell(3).setCellValue(operationParser.parseUserId(operation.getEvIdAppSession()));
        row.createCell(4).setCellValue(translateService.translate(operation.getEvType()));
        row.createCell(5).setCellValue(parseOldValues(operation));
        row.createCell(6).setCellValue(parseNewValues(operation));
    }

    private String parseOldValues(LogbookEventDto operation) {
        if (isCreateEvent(operation.getEvType())) {
            return null;
        }

        String data = operation.getEvDetData();
        return operationParser.parseOldValues(data);
    }

    private String parseNewValues(LogbookEventDto operation) {
        if (isCreateEvent(operation.getEvType())) {
            return null;
        }

        String data = operation.getEvDetData();
        return operationParser.parseNewValues(data);
    }

    private boolean isCreateEvent(final String event) {
        return EXT_VITAMUI_CREATE_EVENTS.stream().anyMatch(e -> event.equals(e.name()));
    }

    private void buildUserListSheet(Workbook workbook, Sheet sheet, List<UserDto> users, Map<String, String> userInfoLangMap, Map<String, String> userGroupNameMap) {
        ExcelFileGeneratorUtils.addHeaderTitles(USER_LIST_COLUMNS_TITLES, sheet, styleHeaderCells(workbook));
        insertUserRows(sheet, users, userInfoLangMap, userGroupNameMap);
        ExcelFileGeneratorUtils.resizeColumns(sheet, USER_LIST_COLUMNS_TITLES.size());
    }

    protected void insertUserRows(Sheet sheet, List<UserDto> users, Map<String, String> userInfoLangMap, Map<String, String> userGroupNameMap) {
        if (CollectionUtils.isEmpty(users)) {
            throw new UnexpectedDataException("Users list data is empty");
        }

        var rowNum = 1;
        for (UserDto user : users) {
            var row = sheet.createRow(rowNum++);
            var lastConnectionDate = user.getLastConnection() == null ? null : user.getLastConnection().format(DATE_FORMAT);
            var userLang = userInfoLangMap == null ? null : userInfoLangMap.get(user.getUserInfoId());
            var userGroupName = userGroupNameMap == null ? null : userGroupNameMap.get(user.getGroupId());

            row.createCell(0).setCellValue(user.getIdentifier());
            row.createCell(1).setCellValue(user.getLastname());
            row.createCell(2).setCellValue(user.getFirstname());
            row.createCell(3).setCellValue(user.getEmail());
            row.createCell(4).setCellValue(user.getMobile());
            row.createCell(5).setCellValue(user.getPhone());
            row.createCell(6).setCellValue(user.getAddress().getStreet());
            row.createCell(7).setCellValue(user.getAddress().getZipCode());
            row.createCell(8).setCellValue(user.getAddress().getCity());
            row.createCell(9).setCellValue(user.getAddress().getCountry());
            row.createCell(10).setCellValue(String.join(",", user.getCenterCodes()));
            row.createCell(11).setCellValue(user.getSiteCode());
            row.createCell(12).setCellValue(user.getInternalCode());
            row.createCell(13).setCellValue(translateService.translate(userLang));
            row.createCell(14).setCellValue(user.getLevel());
            row.createCell(15).setCellValue(userGroupName);
            row.createCell(16).setCellValue(translateService.translate(user.getType().name()));
            row.createCell(17).setCellValue(translateService.translate(user.isSubrogeable()));
            row.createCell(18).setCellValue(translateService.translate(user.isOtp()));
            row.createCell(19).setCellValue(translateService.translate(user.isAutoProvisioningEnabled()));
            row.createCell(20).setCellValue(lastConnectionDate);
            row.createCell(21).setCellValue(translateService.translate(user.getStatus().name()));
        }
    }

    protected CellStyle styleHeaderCells(final Workbook workbook) {
        final Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        final CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        return headerCellStyle;
    }
}
