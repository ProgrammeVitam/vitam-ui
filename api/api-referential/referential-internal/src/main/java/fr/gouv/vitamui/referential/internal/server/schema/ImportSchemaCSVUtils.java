package fr.gouv.vitamui.referential.internal.server.schema;

import fr.gouv.vitamui.referential.internal.server.utils.ImportCSVUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class ImportSchemaCSVUtils extends ImportCSVUtils {

    private ImportSchemaCSVUtils() {}

    public static final String PATH = "Path";
    public static final String CARDINALITY = "Cardinality";
    public static final String ISOBJECT = "IsObject";
    public static final String SHORTNAME = "ShortName";
    public static final String DESCRIPTION = "Description";

    public static void checkImportFile(MultipartFile importSchemaFile) {
        checkImportFile(importSchemaFile, buildImportSchemaColumns());
    }

    static List<ColumnDetails> buildImportSchemaColumns() {
        List<ColumnDetails> expectedColumns = new ArrayList<>();
        expectedColumns.add(
            ColumnDetails.builder().index(0).columnName(PATH).columnType(ColumnType.STRING).mandatory(true).build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(1)
                .columnName(CARDINALITY)
                .columnType(ColumnType.STRING)
                .mandatory(true)
                .build()
        );
        expectedColumns.add(
            ColumnDetails.builder().index(2).columnName(ISOBJECT).columnType(ColumnType.STRING).mandatory(false).build()
        );
        expectedColumns.add(
            ColumnDetails.builder().index(3).columnName(SHORTNAME).columnType(ColumnType.STRING).mandatory(true).build()
        );
        expectedColumns.add(
            ColumnDetails.builder()
                .index(4)
                .columnName(DESCRIPTION)
                .columnType(ColumnType.STRING)
                .mandatory(true)
                .build()
        );
        return expectedColumns;
    }
}
