package fr.gouv.vitamui.commons.api.dtos;

import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
public class ExportSearchResultParam {

    //Export CSV headers
    //French
    private static final String FR_GUID_FIELD_HEADER_NAME = "Identifiant technique de l'UA";
    public static final String FR_ARCHIVE_UNIT_TYPE = "Type";
    private static final String FR_ORIGINATING_AGENCY_FIELD_HEADER_NAME = "Service producteur de l'UA";
    private static final String FR_DESCRIPTION_LEVEL_FIELD_HEADER_NAME = "Niveau de description";
    private static final String FR_ARCHIVE_UNIT_NAME_FIELD_HEADER_NAME = "Nom de l’UA";
    private static final String FR_BEGIN_DATE_FIELD_HEADER_NAME = "Date de debut de l’UA";
    private static final String FR_END_DATE_FIELD_HEADER_NAME = "Date de fin de l’UA";
    private static final String FR_DESCRIPTION_FIELD_HEADER_NAME = "Description de l'UA";

    public static final String FR_AU_FILING_SCHEME = "Plan de classement";
    public static final String FR_AU_HOLDING_SCHEME = "Arbre de positionnement";
    public static final String FR_AU_WITHOUT_OBJECT = "Archive sans objet";
    public static final String FR_AU_WITH_OBJECT = "Archive avec objet";

    //English
    private static final String EN_GUID_FIELD_HEADER_NAME = "Archive unit technical identifier";
    public static final String EN_ARCHIVE_UNIT_TYPE = "Type";
    private static final String EN_ORIGINATING_AGENCY_FIELD_HEADER_NAME = "Archive unit Originating agency";
    private static final String EN_DESCRIPTION_LEVEL_FIELD_HEADER_NAME = "Description level";
    private static final String EN_ARCHIVE_UNIT_NAME_FIELD_HEADER_NAME = "Archive unit Name";
    private static final String EN_BEGIN_DATE_FIELD_HEADER_NAME = "Archive unit start date";
    private static final String EN_END_DATE_FIELD_HEADER_NAME = "Archive unit end date";
    private static final String EN_DESCRIPTION_FIELD_HEADER_NAME = "Archive unit Description";

    public static final String EN_AU_FILING_SCHEME = "Filling scheme";
    public static final String EN_AU_HOLDING_SCHEME = "Holding scheme";
    public static final String EN_AU_WITHOUT_OBJECT = "Archive without object";
    public static final String EN_AU_WITH_OBJECT = "Archive with object";

    //Date patterns
    private static final String FR_PATTERN_DATE = "dd/MM/yyyy";
    private static final String EN_PATTERN_DATE = "MM/dd/yyyy";

    private Map<String, String> descriptionLevelMap;
    private String patternDate;
    private List<String> headers;
    private char separator = ';';

    public ExportSearchResultParam(Locale locale) {
        if (locale.equals(Locale.FRENCH)) {
            this.headers = List.of(
                FR_GUID_FIELD_HEADER_NAME,
                FR_ARCHIVE_UNIT_TYPE,
                FR_ORIGINATING_AGENCY_FIELD_HEADER_NAME,
                FR_DESCRIPTION_LEVEL_FIELD_HEADER_NAME,
                FR_ARCHIVE_UNIT_NAME_FIELD_HEADER_NAME,
                FR_BEGIN_DATE_FIELD_HEADER_NAME,
                FR_END_DATE_FIELD_HEADER_NAME,
                FR_DESCRIPTION_FIELD_HEADER_NAME
            );
            this.patternDate = FR_PATTERN_DATE;
            this.descriptionLevelMap = Map.of(
                "File",
                "Dossier",
                "Item",
                "Document",
                "RecordGrp",
                "Dossier",
                "Fonds",
                "Fonds",
                "Subfonds",
                "Subfonds",
                "Class",
                "Class",
                "Collection",
                "Collection",
                "Serie",
                "Serie",
                "Subseries",
                "Subseries",
                "SubGrp :SubGrp",
                "OtherLevel :OtherLevel"
            );
        } else if (locale.equals(Locale.ENGLISH)) {
            this.headers = List.of(
                EN_GUID_FIELD_HEADER_NAME,
                FR_ARCHIVE_UNIT_TYPE,
                EN_ORIGINATING_AGENCY_FIELD_HEADER_NAME,
                EN_DESCRIPTION_LEVEL_FIELD_HEADER_NAME,
                EN_ARCHIVE_UNIT_NAME_FIELD_HEADER_NAME,
                EN_BEGIN_DATE_FIELD_HEADER_NAME,
                EN_END_DATE_FIELD_HEADER_NAME,
                EN_DESCRIPTION_FIELD_HEADER_NAME
            );
            this.patternDate = EN_PATTERN_DATE;
            this.descriptionLevelMap = Map.of(
                "File",
                "File",
                "Item",
                "File",
                "RecordGrp",
                "RecordGrp",
                "Fonds",
                "Fonds",
                "Subfonds",
                "Subfonds",
                "Class",
                "Class",
                "Collection",
                "Collection",
                "Serie",
                "Serie",
                "Subseries",
                "Subseries",
                "SubGrp :SubGrp",
                "OtherLevel :OtherLevel"
            );
        }
    }
}
