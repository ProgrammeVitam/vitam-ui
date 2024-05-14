package fr.gouv.vitamui.referential.internal.server.ingestcontract;

import fr.gouv.vitamui.referential.internal.server.utils.ExportCSVParams;

import java.util.List;

public class ExportIngestContracts extends ExportCSVParams {

    //Export ingest contracts CSV headers
    private static final String IDENTIFIER = "Identifier";
    private static final String NAME = "Name";
    private static final String DESCRIPTION = "Description";
    private static final String STATUS = "Status";
    private static final String ARCHIVE_PROFILES = "ArchiveProfiles";
    private static final String CHECK_PARENT_LINK = "CheckParentLink";
    private static final String CHECK_PARENT_ID = "CheckParentId";
    private static final String LINK_PARENT_ID = "LinkParentId";
    private static final String FORMAT_UNIDENTIFIED_AUTHORIZED = "FormatUnidentifiedAuthorized";
    private static final String EVERY_FORMAT_TYPE = "EveryFormatType";
    private static final String FORMAT_TYPE = "FormatType";
    private static final String MANAGEMENT_CONTRACT_ID = "ManagementContractId";
    private static final String COMPUTED_INHERITED_RULES_AT_INGEST = "ComputedInheritedRulesAtIngest";
    private static final String MASTER_MANDATORY = "MasterMandatory";
    private static final String EVERY_DATA_OBJECT_VERSION = "EveryDataObjectVersion";
    private static final String DATA_OBJECT_VERSION = "DataObjectVersion";
    private static final String ACTIVATION_DATE = "ActivationDate";
    private static final String DESACTIVATION_DATE = "DesactivationDate";

    public ExportIngestContracts() {
        List<String> headers = List.of(
            IDENTIFIER,
            NAME,
            DESCRIPTION,
            STATUS,
            ARCHIVE_PROFILES,
            CHECK_PARENT_LINK,
            CHECK_PARENT_ID,
            LINK_PARENT_ID,
            FORMAT_UNIDENTIFIED_AUTHORIZED,
            EVERY_FORMAT_TYPE,
            FORMAT_TYPE,
            MANAGEMENT_CONTRACT_ID,
            COMPUTED_INHERITED_RULES_AT_INGEST,
            MASTER_MANDATORY,
            EVERY_DATA_OBJECT_VERSION,
            DATA_OBJECT_VERSION,
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
