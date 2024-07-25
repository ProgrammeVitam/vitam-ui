package fr.gouv.vitamui.referential.internal.server.accesscontract;

import fr.gouv.vitamui.referential.internal.server.utils.ImportCSVUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class AccessContractCSVUtils extends ImportCSVUtils {

    private AccessContractCSVUtils() {}

    public static final String IDENTIFIER = "Identifier";
    public static final String NAME = "Name*";
    public static final String DESCRIPTION = "Description";
    public static final String STATUS = "Status";
    public static final String WRITING_PERMISSION = "WritingPermission";
    public static final String EVERY_ORIGINATING_AGENCY = "EveryOriginatingAgency";
    public static final String ORIGINATING_AGENCIES = "OriginatingAgencies";
    public static final String EVERY_DATA_OBJECT_VERSION = "EveryDataObjectVersion";
    public static final String DATA_OBJECT_VERSION = "DataObjectVersion";
    public static final String ROOT_UNITS = "RootUnits";
    public static final String EXCLUDED_ROOT_UNITS = "ExcludedRootUnits";
    public static final String ACCESS_LOG = "AccessLog";
    public static final String RULE_CATEGORY_TO_FILTER = "RuleCategoryToFilter";
    public static final String RULE_CATEGORY_TO_FILTER_FOR_THE_OTHER_ORIGINATING_AGENCIES =
        "RuleCategoryToFilterForTheOtherOriginatingAgencies";
    public static final String DO_NOT_FILTER_FILING_SCHEMES = "DoNotFilterFilingSchemes";
    public static final String WRITING_RESTRICTED_DESC = "WritingRestrictedDesc";

    public static final String CREATION_DATE = "CreationDate";
    public static final String LAST_UPDATE = "LastUpdate";
    public static final String ACTIVATION_DATE = "ActivationDate";
    public static final String DESACTIVATION_DATE = "DesactivationDate";

    public static void checkImportFile(MultipartFile accessContractFile, boolean isIdentifierMandatory) {
        checkImportFile(accessContractFile, buildAccessContractColumns(isIdentifierMandatory));
    }

    private static List<ColumnDetails> buildAccessContractColumns(boolean isIdentifierMandatory) {
        List<ColumnDetails> expectedColumns = new ArrayList<>();
        expectedColumns.add(
            ColumnDetails.builder()
                .index(0)
                .columnName(IDENTIFIER)
                .columnType(ColumnType.STRING)
                .mandatory(isIdentifierMandatory)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder().index(1).columnName(NAME).columnType(ColumnType.STRING).mandatory(true).build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(2)
                .columnName(DESCRIPTION)
                .columnType(ColumnType.STRING)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(3)
                .columnName(STATUS)
                .columnType(ColumnType.CONTEXT_STATUS)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(4)
                .columnName(WRITING_PERMISSION)
                .columnType(ColumnType.A_BOOLEAN)
                .mandatory(false)
                .build()
        );
        // accessRights :
        expectedColumns.add(
            ColumnDetails.builder()
                .index(5)
                .columnName(EVERY_ORIGINATING_AGENCY)
                .columnType(ColumnType.A_BOOLEAN)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(6)
                .columnName(ORIGINATING_AGENCIES)
                .columnType(ColumnType.STRING)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(7)
                .columnName(EVERY_DATA_OBJECT_VERSION)
                .columnType(ColumnType.A_BOOLEAN)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(8)
                .columnName(DATA_OBJECT_VERSION)
                .columnType(ColumnType.DATA_OBJECT_VERSION_TYPE)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(9)
                .columnName(ROOT_UNITS)
                .columnType(ColumnType.STRING)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(10)
                .columnName(EXCLUDED_ROOT_UNITS)
                .columnType(ColumnType.STRING)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(11)
                .columnName(ACCESS_LOG)
                .columnType(ColumnType.CONTEXT_STATUS)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(12)
                .columnName(RULE_CATEGORY_TO_FILTER)
                .columnType(ColumnType.RULE_TYPE)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(13)
                .columnName(WRITING_RESTRICTED_DESC)
                .columnType(ColumnType.A_BOOLEAN)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(14)
                .columnName(RULE_CATEGORY_TO_FILTER_FOR_THE_OTHER_ORIGINATING_AGENCIES)
                .columnType(ColumnType.RULE_TYPE)
                .mandatory(false)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(15)
                .columnName(DO_NOT_FILTER_FILING_SCHEMES)
                .columnType(ColumnType.A_BOOLEAN)
                .mandatory(false)
                .build()
        );
        return expectedColumns;
    }
}
