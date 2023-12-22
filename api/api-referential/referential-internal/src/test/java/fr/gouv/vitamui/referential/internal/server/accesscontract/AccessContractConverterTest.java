package fr.gouv.vitamui.referential.internal.server.accesscontract;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessContractConverterTest {
    private final AccessContractConverter accessContractConverter = new AccessContractConverter();

    @Test
    @SneakyThrows
    public void should_convert_empty_access_contract() {
        Map<String, Object> emptyAccessContract = Map.of();
        JsonNode mapped = accessContractConverter.convertToUpperCaseFields(emptyAccessContract);
        assertThat(mapped).hasToString("{}");
    }

    @Test
    @SneakyThrows
    public void should_convert_partial_access_contract() {
        Map<String, Object> partialDto = givenPartialAccessContract();
        JsonNode mapped = accessContractConverter.convertToUpperCaseFields(partialDto);
        String expected = "{\"DeactivationDate\":\"deactivationDate\",\"EveryDataObjectVersion\":true,\"OriginatingAgencies\":[\"r3\",\"r4\"],\"Description\":\"description\",\"CreationDate\":\"creationDate\",\"AccessLog\":\"accessLog\",\"RootUnits\":[\"r1\",\"r2\"],\"DataObjectVersion\":[\"v1\",\"v2\"],\"WritingRestrictedDesc\":true,\"ExcludedRootUnits\":[\"r3\",\"r4\"],\"LastUpdate\":\"lastUpdate\",\"Name\":\"name\",\"RuleCategoryToFilter\":[\"r3\",\"r4\"],\"EveryOriginatingAgency\":false,\"WritingPermission\":true,\"ActivationDate\":\"activationDate\",\"Status\":\"status\"}";
        assertThat(mapped).hasToString(expected);
    }

    private Map<String, Object> givenPartialAccessContract() {
        return Stream.of(new Object[][]{
            {"name", "name"},
            {"everyOriginatingAgency", false},
            {"everyDataObjectVersion", true},
            {"writingPermission", true},
            {"writingRestrictedDesc", true},
            {"description", "description"},
            {"accessLog", "accessLog"},
            {"activationDate", "activationDate"},
            {"deactivationDate", "deactivationDate"},
            {"lastUpdate", "lastUpdate"},
            {"creationDate", "creationDate"},
            {"status", "status"},
            {"rootUnits", List.of("r1", "r2")},
            {"excludedRootUnits", List.of("r3", "r4")},
            {"ruleCategoryToFilter", List.of("r3", "r4")},
            {"originatingAgencies", List.of("r3", "r4")},
            {"dataObjectVersion", List.of("v1", "v2")},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));
    }

}
