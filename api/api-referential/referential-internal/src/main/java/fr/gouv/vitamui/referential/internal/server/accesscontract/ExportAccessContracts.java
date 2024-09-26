package fr.gouv.vitamui.referential.internal.server.accesscontract;

import fr.gouv.vitamui.referential.internal.server.utils.ExportCSVParams;

import java.util.List;

import static fr.gouv.vitamui.referential.internal.server.accesscontract.AccessContractCSVUtils.*;

public class ExportAccessContracts extends ExportCSVParams {

    public ExportAccessContracts() {
        List<String> headers = List.of(
            IDENTIFIER,
            NAME,
            DESCRIPTION,
            STATUS,
            WRITING_PERMISSION,
            EVERY_ORIGINATING_AGENCY,
            ORIGINATING_AGENCIES,
            EVERY_DATA_OBJECT_VERSION,
            DATA_OBJECT_VERSION,
            ROOT_UNITS,
            EXCLUDED_ROOT_UNITS,
            ACCESS_LOG,
            RULE_CATEGORY_TO_FILTER,
            WRITING_RESTRICTED_DESC,
            RULE_CATEGORY_TO_FILTER_FOR_THE_OTHER_ORIGINATING_AGENCIES,
            DO_NOT_FILTER_FILING_SCHEMES,
            CREATION_DATE,
            LAST_UPDATE,
            ACTIVATION_DATE,
            DESACTIVATION_DATE
        );
        this.setHeaders(headers);
    }

    @Override
    public char getSeparator() {
        return ';';
    }

    @Override
    public String getArrayJoinStr() {
        return " | ";
    }

    @Override
    public String getPatternDate() {
        return "dd/MM/yyyy";
    }
}
