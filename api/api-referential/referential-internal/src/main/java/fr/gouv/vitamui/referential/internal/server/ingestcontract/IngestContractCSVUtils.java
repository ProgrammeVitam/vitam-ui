package fr.gouv.vitamui.referential.internal.server.ingestcontract;

import fr.gouv.vitamui.referential.internal.server.utils.ImportCSVUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class IngestContractCSVUtils extends ImportCSVUtils {

    private IngestContractCSVUtils(){

    }

    public static final String IDENTIFIER = "Identifier";
    public static final String NAME = "Name*";
    public static final String DESCRIPTION = "Description";
    public static final String STATUS = "Status";
    public static final String ARCHIVE_PROFILES = "ArchiveProfiles";
    public static final String CHECK_PARENT_LINK = "CheckParentLink";
    public static final String CHECK_PARENT_ID = "CheckParentId";
    public static final String LINK_PARENT_ID = "LinkParentId";
    public static final String FORMAT_UNIDENTIFIED_AUTHORIZED = "FormatUnidentifiedAuthorized";
    public static final String EVERY_FORMAT_TYPE = "EveryFormatType";
    public static final String FORMAT_TYPE = "FormatType";
    public static final String MANAGEMENT_CONTRACT_ID = "ManagementContractId";
    public static final String COMPUTED_INHERITED_RULES_AT_INGEST = "ComputedInheritedRulesAtIngest";
    public static final String MASTER_MANDATORY = "MasterMandatory";
    public static final String EVERY_DATA_OBJECT_VERSION = "EveryDataObjectVersion";
    public static final String DATA_OBJECT_VERSION = "DataObjectVersion";

    public static void checkImportFile(MultipartFile ingestContractFile, boolean isIdentifierMandatory) {
        checkImportFile(ingestContractFile, buildIngestContractColumns(isIdentifierMandatory));
    }

    private static List<ColumnDetails> buildIngestContractColumns(boolean isIdentifierMandatory) {
        List<ColumnDetails> expectedColumns = new ArrayList<>();
        expectedColumns.add(ColumnDetails.builder().index(0).columnName(IDENTIFIER).columnType(ColumnType.STRING).mandatory(isIdentifierMandatory).build());
        expectedColumns.add(ColumnDetails.builder().index(1).columnName(NAME).columnType(ColumnType.STRING).mandatory(true).build());
        expectedColumns.add(ColumnDetails.builder().index(2).columnName(DESCRIPTION).columnType(ColumnType.STRING).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(3).columnName(STATUS).columnType(ColumnType.CONTEXT_STATUS).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(4).columnName(ARCHIVE_PROFILES).columnType(ColumnType.STRING).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(5).columnName(CHECK_PARENT_LINK).columnType(ColumnType.CHECK_PARENT_LINK_TYPE).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(6).columnName(CHECK_PARENT_ID).columnType(ColumnType.STRING).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(7).columnName(LINK_PARENT_ID).columnType(ColumnType.STRING).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(8).columnName(FORMAT_UNIDENTIFIED_AUTHORIZED).columnType(ColumnType.A_BOOLEAN).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(9).columnName(EVERY_FORMAT_TYPE).columnType(ColumnType.A_BOOLEAN).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(10).columnName(FORMAT_TYPE).columnType(ColumnType.STRING).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(11).columnName(MANAGEMENT_CONTRACT_ID).columnType(ColumnType.STRING).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(12).columnName(COMPUTED_INHERITED_RULES_AT_INGEST).columnType(ColumnType.A_BOOLEAN).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(13).columnName(MASTER_MANDATORY).columnType(ColumnType.A_BOOLEAN).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(14).columnName(EVERY_DATA_OBJECT_VERSION).columnType(ColumnType.A_BOOLEAN).mandatory(false).build());
        expectedColumns.add(ColumnDetails.builder().index(15).columnName(DATA_OBJECT_VERSION).columnType(ColumnType.DATA_OBJECT_VERSION_TYPE).mandatory(false).build());
        return expectedColumns;
    }
}
