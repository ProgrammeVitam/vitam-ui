package fr.gouv.vitamui.referential.internal.server.accesscontract;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import fr.gouv.vitam.common.model.administration.ContextStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@ToString
@Getter
@Setter
public class AccessContractCSVDto {

    @CsvBindByName(column = AccessContractCSVUtils.IDENTIFIER)
    private String identifier;

    @CsvBindByName(column = AccessContractCSVUtils.NAME, required = true)
    private String name;

    @CsvBindByName(column = AccessContractCSVUtils.DESCRIPTION)
    private String description;

    @CsvBindByName(column = AccessContractCSVUtils.STATUS)
    private ContextStatus status;

    @CsvBindByName(column = AccessContractCSVUtils.WRITING_PERMISSION)
    private Boolean writingPermission;

    @CsvBindByName(column = AccessContractCSVUtils.EVERY_ORIGINATING_AGENCY)
    private Boolean everyOriginatingAgency;

    @CsvBindAndSplitByName(
        column = AccessContractCSVUtils.ORIGINATING_AGENCY,
        elementType = String.class,
        splitOn = "[ |]+"
    )
    private Set<String> originatingAgencies;

    @CsvBindByName(column = AccessContractCSVUtils.EVERY_DATA_OBJECT_VERSION)
    private Boolean everyDataObjectVersion;

    @CsvBindAndSplitByName(
        column = AccessContractCSVUtils.DATA_OBJECT_VERSION,
        elementType = String.class,
        splitOn = "[ |]+"
    )
    private Set<String> dataObjectVersion;

    @CsvBindAndSplitByName(column = AccessContractCSVUtils.ROOT_UNITS, elementType = String.class, splitOn = "[ |]+")
    private Set<String> rootUnits;

    @CsvBindAndSplitByName(
        column = AccessContractCSVUtils.EXCLUDED_ROOT_UNITS,
        elementType = String.class,
        splitOn = "[ |]+"
    )
    private Set<String> excludedRootUnits;

    @CsvBindByName(column = AccessContractCSVUtils.ACCESS_LOG)
    private ContextStatus accessLog;

    @CsvBindAndSplitByName(
        column = AccessContractCSVUtils.RULE_CATEGORY_TO_FILTER,
        elementType = String.class,
        splitOn = "[ |]+"
    )
    private Set<String> ruleCategoryToFilter;

    @CsvBindByName(column = AccessContractCSVUtils.WRITING_RESTRICTED_DESC)
    private Boolean writingRestrictedDesc;
}
