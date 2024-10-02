package fr.gouv.vitamui.commons.api.converter;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.RuleType;
import fr.gouv.vitamui.commons.api.domain.AccessContractDto;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessContractConverterTest {

    @Test
    @SneakyThrows
    public void should_convert_empty_access_contract() {
        Map<String, Object> emptyAccessContract = Map.of();
        JsonNode mapped = AccessContractConverter.convertToUpperCaseFields(emptyAccessContract);
        assertThat(mapped).hasToString("{}");
    }

    @Test
    public void convertVitamToDto_with_deprecated() {
        AccessContractModel accessContractModel = (AccessContractModel) new AccessContractModel()
            .setDataObjectVersion(Set.of("DataObjectVersion"))
            .setOriginatingAgencies(Set.of("OriginatingAgencies"))
            .setWritingPermission(true)
            .setWritingRestrictedDesc(true)
            .setEveryOriginatingAgency(true)
            .setEveryDataObjectVersion(true)
            .setRootUnits(Set.of("RootUnits"))
            .setExcludedRootUnits(Set.of("ExcludedRootUnits"))
            .setAccessLog(ActivationStatus.ACTIVE)
            .setRuleCategoryToFilter(Set.of(RuleType.AppraisalRule))
            .setRuleCategoryToFilterForTheOtherOriginatingAgencies(Set.of(RuleType.DisseminationRule))
            .setSkipFilingSchemeRuleCategoryFilter(true)
            .setDoNotFilterFilingSchemes(true)
            //AbstractContractModel
            .setId("Id")
            .setTenant(12)
            .setVersion(14)
            .setName("Name")
            .setIdentifier("Identifier")
            .setDescription("Description")
            .setStatus(ActivationStatus.ACTIVE)
            .setCreationDate("Creationdate")
            .setLastUpdate("Lastupdate")
            .setActivationDate("Activationdate")
            .setDeactivationDate("Deactivationdate");

        AccessContractDto accessContractDto = AccessContractConverter.convertVitamToDto(accessContractModel);

        assertThat(accessContractDto.getId()).isEqualTo("Id");
        assertThat(accessContractDto.getVersion()).isEqualTo(14);
        assertThat(accessContractDto.getName()).isEqualTo("Name");
        assertThat(accessContractDto.getIdentifier()).isEqualTo("Identifier");
        assertThat(accessContractDto.getDescription()).isEqualTo("Description");
        assertThat(accessContractDto.getStatus()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(accessContractDto.getCreationDate()).isEqualTo("Creationdate");
        assertThat(accessContractDto.getLastUpdate()).isEqualTo("Lastupdate");
        assertThat(accessContractDto.getActivationDate()).isEqualTo("Activationdate");
        assertThat(accessContractDto.getDeactivationDate()).isEqualTo("Deactivationdate");
        assertThat(accessContractDto.getDataObjectVersion()).isEqualTo(Set.of("DataObjectVersion"));
        assertThat(accessContractDto.getOriginatingAgencies()).isEqualTo(Set.of("OriginatingAgencies"));
        assertThat(accessContractDto.getWritingPermission()).isEqualTo(true);
        assertThat(accessContractDto.getWritingRestrictedDesc()).isEqualTo(true);
        assertThat(accessContractDto.getEveryOriginatingAgency()).isEqualTo(true);
        assertThat(accessContractDto.getEveryDataObjectVersion()).isEqualTo(true);
        assertThat(accessContractDto.getRootUnits()).isEqualTo(Set.of("RootUnits"));
        assertThat(accessContractDto.getExcludedRootUnits()).isEqualTo(Set.of("ExcludedRootUnits"));
        assertThat(accessContractDto.getAccessLog()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(accessContractDto.getRuleCategoryToFilter()).isEqualTo(Set.of(RuleType.AppraisalRule));
        assertThat(accessContractDto.getRuleCategoryToFilterForTheOtherOriginatingAgencies()).isEqualTo(
            Set.of(RuleType.DisseminationRule)
        );
        assertThat(accessContractDto.getDoNotFilterFilingSchemes()).isEqualTo(true);
    }

    @Test
    public void convertVitamToDto() {
        AccessContractModel accessContractModel = (AccessContractModel) new AccessContractModel()
            .setDataObjectVersion(Set.of("DataObjectVersion"))
            .setOriginatingAgencies(Set.of("OriginatingAgencies"))
            .setWritingPermission(true)
            .setWritingRestrictedDesc(true)
            .setEveryOriginatingAgency(true)
            .setEveryDataObjectVersion(true)
            .setRootUnits(Set.of("RootUnits"))
            .setExcludedRootUnits(Set.of("ExcludedRootUnits"))
            .setAccessLog(ActivationStatus.ACTIVE)
            .setRuleCategoryToFilter(Set.of(RuleType.AppraisalRule))
            .setRuleCategoryToFilterForTheOtherOriginatingAgencies(Set.of(RuleType.DisseminationRule))
            .setSkipFilingSchemeRuleCategoryFilter(true)
            .setDoNotFilterFilingSchemes(true)
            //AbstractContractModel
            .setId("Id")
            .setTenant(12)
            .setVersion(14)
            .setName("Name")
            .setIdentifier("Identifier")
            .setDescription("Description")
            .setStatus(ActivationStatus.ACTIVE)
            .setCreationDate("Creationdate")
            .setLastUpdate("Lastupdate")
            .setActivationDate("Activationdate")
            .setDeactivationDate("Deactivationdate");

        AccessContractDto accessContractDto = AccessContractConverter.convertVitamToDto(accessContractModel);

        assertThat(accessContractDto.getId()).isEqualTo("Id");
        assertThat(accessContractDto.getVersion()).isEqualTo(14);
        assertThat(accessContractDto.getName()).isEqualTo("Name");
        assertThat(accessContractDto.getIdentifier()).isEqualTo("Identifier");
        assertThat(accessContractDto.getDescription()).isEqualTo("Description");
        assertThat(accessContractDto.getStatus()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(accessContractDto.getCreationDate()).isEqualTo("Creationdate");
        assertThat(accessContractDto.getLastUpdate()).isEqualTo("Lastupdate");
        assertThat(accessContractDto.getActivationDate()).isEqualTo("Activationdate");
        assertThat(accessContractDto.getDeactivationDate()).isEqualTo("Deactivationdate");
        assertThat(accessContractDto.getDataObjectVersion()).isEqualTo(Set.of("DataObjectVersion"));
        assertThat(accessContractDto.getOriginatingAgencies()).isEqualTo(Set.of("OriginatingAgencies"));
        assertThat(accessContractDto.getWritingPermission()).isEqualTo(true);
        assertThat(accessContractDto.getWritingRestrictedDesc()).isEqualTo(true);
        assertThat(accessContractDto.getEveryOriginatingAgency()).isEqualTo(true);
        assertThat(accessContractDto.getEveryDataObjectVersion()).isEqualTo(true);
        assertThat(accessContractDto.getRootUnits()).isEqualTo(Set.of("RootUnits"));
        assertThat(accessContractDto.getExcludedRootUnits()).isEqualTo(Set.of("ExcludedRootUnits"));
        assertThat(accessContractDto.getAccessLog()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(accessContractDto.getRuleCategoryToFilter()).isEqualTo(Set.of(RuleType.AppraisalRule));
        assertThat(accessContractDto.getRuleCategoryToFilterForTheOtherOriginatingAgencies()).isEqualTo(
            Set.of(RuleType.DisseminationRule)
        );
        assertThat(accessContractDto.getDoNotFilterFilingSchemes()).isEqualTo(true);
    }

    @Test
    public void convertDtoToVitam() {
        AccessContractDto accessContractDto = new AccessContractDto()
            .setDataObjectVersion(Set.of("DataObjectVersion"))
            .setOriginatingAgencies(Set.of("OriginatingAgencies"))
            .setWritingPermission(true)
            .setWritingRestrictedDesc(true)
            .setEveryOriginatingAgency(true)
            .setEveryDataObjectVersion(true)
            .setRootUnits(Set.of("RootUnits"))
            .setExcludedRootUnits(Set.of("ExcludedRootUnits"))
            .setAccessLog(ActivationStatus.ACTIVE)
            .setRuleCategoryToFilter(Set.of(RuleType.AppraisalRule))
            .setRuleCategoryToFilterForTheOtherOriginatingAgencies(Set.of(RuleType.DisseminationRule))
            .setDoNotFilterFilingSchemes(true)
            .setVersion(14)
            .setName("Name")
            .setIdentifier("Identifier")
            .setDescription("Description")
            .setStatus(ActivationStatus.ACTIVE)
            .setCreationDate("Creationdate")
            .setLastUpdate("Lastupdate")
            .setActivationDate("Activationdate")
            .setDeactivationDate("Deactivationdate");
        //AbstractContractModel;
        accessContractDto.setId("Id");

        AccessContractModel accessContractModel = AccessContractConverter.convertDtoToVitam(accessContractDto);

        assertThat(accessContractModel.getId()).isEqualTo("Id");
        assertThat(accessContractModel.getTenant()).isEqualTo(null);
        assertThat(accessContractModel.getVersion()).isEqualTo(14);
        assertThat(accessContractModel.getName()).isEqualTo("Name");
        assertThat(accessContractModel.getIdentifier()).isEqualTo("Identifier");
        assertThat(accessContractModel.getDescription()).isEqualTo("Description");
        assertThat(accessContractModel.getStatus()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(accessContractModel.getCreationDate()).isEqualTo(null);
        assertThat(accessContractModel.getLastUpdate()).isEqualTo(null);
        assertThat(accessContractModel.getActivationDate()).isEqualTo(null);
        assertThat(accessContractModel.getDeactivationDate()).isEqualTo(null);
        assertThat(accessContractModel.getDataObjectVersion()).isEqualTo(Set.of("DataObjectVersion"));
        assertThat(accessContractModel.getOriginatingAgencies()).isEqualTo(Set.of("OriginatingAgencies"));
        assertThat(accessContractModel.getWritingPermission()).isEqualTo(true);
        assertThat(accessContractModel.getWritingRestrictedDesc()).isEqualTo(true);
        assertThat(accessContractModel.getEveryOriginatingAgency()).isEqualTo(true);
        assertThat(accessContractModel.getEveryDataObjectVersion()).isEqualTo(true);
        assertThat(accessContractModel.getRootUnits()).isEqualTo(Set.of("RootUnits"));
        assertThat(accessContractModel.getExcludedRootUnits()).isEqualTo(Set.of("ExcludedRootUnits"));
        assertThat(accessContractModel.getAccessLog()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(accessContractModel.getRuleCategoryToFilter()).isEqualTo(Set.of(RuleType.AppraisalRule));
        assertThat(accessContractModel.getRuleCategoryToFilterForTheOtherOriginatingAgencies()).isEqualTo(
            Set.of(RuleType.DisseminationRule)
        );
        assertThat(accessContractModel.getDoNotFilterFilingSchemes()).isEqualTo(true);
    }

    @Test
    @SneakyThrows
    public void should_convert_partial_access_contract() {
        Map<String, Object> partialDto = givenPartialAccessContract();
        JsonNode mapped = AccessContractConverter.convertToUpperCaseFields(partialDto);
        String expected =
            "{\"DeactivationDate\":\"deactivationDate\",\"EveryDataObjectVersion\":true,\"OriginatingAgencies\":[\"r3\",\"r4\"],\"Description\":\"description\",\"CreationDate\":\"creationDate\",\"AccessLog\":\"accessLog\",\"RootUnits\":[\"r1\",\"r2\"],\"DataObjectVersion\":[\"v1\",\"v2\"],\"WritingRestrictedDesc\":true,\"ExcludedRootUnits\":[\"r3\",\"r4\"],\"LastUpdate\":\"lastUpdate\",\"Name\":\"name\",\"RuleCategoryToFilter\":[\"r3\",\"r4\"],\"EveryOriginatingAgency\":false,\"WritingPermission\":true,\"ActivationDate\":\"activationDate\",\"Status\":\"status\"}";
        assertThat(mapped).hasToString(expected);
    }

    private Map<String, Object> givenPartialAccessContract() {
        return Stream.of(
            new Object[][] {
                { "name", "name" },
                { "everyOriginatingAgency", false },
                { "everyDataObjectVersion", true },
                { "writingPermission", true },
                { "writingRestrictedDesc", true },
                { "description", "description" },
                { "accessLog", "accessLog" },
                { "activationDate", "activationDate" },
                { "deactivationDate", "deactivationDate" },
                { "lastUpdate", "lastUpdate" },
                { "creationDate", "creationDate" },
                { "status", "status" },
                { "rootUnits", List.of("r1", "r2") },
                { "excludedRootUnits", List.of("r3", "r4") },
                { "ruleCategoryToFilter", List.of("r3", "r4") },
                { "originatingAgencies", List.of("r3", "r4") },
                { "dataObjectVersion", List.of("v1", "v2") },
            }
        ).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));
    }
}
