package fr.gouv.vitamui.referential.internal.server.accesscontract;

import fr.gouv.vitamui.referential.internal.server.utils.ExportCSVParams;

import java.util.List;

public class ExportAccessContracts extends ExportCSVParams {

    //Export access contracts CSV headers
    private static final String NAME = "Name";
    private static final String IDENTIFIER = "Identifier";
    private static final String DESCRIPTION = "Description";
    private static final String STATUS = "Status";
    private static final String WRITING_PERMISSION = "WritingPermission";
    private static final String EVERY_ORIGINATING_AGENCY = "EveryOriginatingAgency";
    private static final String ORIGINATING_AGENCIES = "OriginatingAgencies";
    private static final String EVERY_DATA_OBJECT_VERSION = "EveryDataObjectVersion";
    private static final String DATA_OBJECT_VERSION = "DataObjectVersion";
    private static final String ROOT_UNITS = "RootUnits";
    private static final String EXCLUDED_ROOT_UNITS = "ExcludedRootUnits";
    private static final String ACCESS_LOG = "AccessLog";
    private static final String RULE_CATEGORY_TO_FILTER = "RuleCategoryToFilter";
    private static final String WRITING_RESTRICTED_DESC = "WritingRestrictedDesc";
    private static final String CREATION_DATE = "CreationDate";
    private static final String LAST_UPDATE = "LastUpdate";
    private static final String ACTIVATION_DATE = "ActivationDate";
    private static final String DESACTIVATION_DATE = "DesactivationDate";

    public ExportAccessContracts() {
        List<String> headers = List.of(IDENTIFIER, NAME, DESCRIPTION, STATUS, WRITING_PERMISSION, EVERY_ORIGINATING_AGENCY, ORIGINATING_AGENCIES, EVERY_DATA_OBJECT_VERSION,
                DATA_OBJECT_VERSION, ROOT_UNITS, EXCLUDED_ROOT_UNITS, ACCESS_LOG, RULE_CATEGORY_TO_FILTER, WRITING_RESTRICTED_DESC, CREATION_DATE, LAST_UPDATE, ACTIVATION_DATE, DESACTIVATION_DATE);
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
