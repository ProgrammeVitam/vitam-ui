package fr.gouv.vitamui.commons.vitam.api.administration;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.RuleType;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class AccessContractServiceTest {

    AccessContractService accessContractService = new AccessContractService(null);

    @Test
    public void serializeAccessContracts() throws Exception {
        ByteArrayInputStream byteArrayInputStream = accessContractService.serializeAccessContracts(
            List.of(accessContractModelDto_01(), accessContractModelDto_02())
        );
        int n = byteArrayInputStream.available();
        byte[] bytes = new byte[n];
        byteArrayInputStream.read(bytes, 0, n);
        String s = new String(bytes, StandardCharsets.UTF_8);

        JSONAssert.assertEquals(
            s,
            PropertiesUtils.getResourceAsString("data/access-contract/access-contracts.json"),
            true
        );
    }

    AccessContractModel accessContractModelDto_01() {
        AccessContractModel accessContractModelDto = new AccessContractModel();
        accessContractModelDto.setId("id");
        accessContractModelDto.setVersion(12);
        accessContractModelDto.setName("name");
        accessContractModelDto.setIdentifier("identifier");
        accessContractModelDto.setDescription("description");
        accessContractModelDto.setStatus(ActivationStatus.INACTIVE);
        accessContractModelDto.setWritingPermission(true);
        accessContractModelDto.setEveryOriginatingAgency(true);
        accessContractModelDto.setEveryDataObjectVersion(true);
        accessContractModelDto.setAccessLog(ActivationStatus.ACTIVE);
        accessContractModelDto.setRootUnits(Set.of("rootUnits"));
        accessContractModelDto.setExcludedRootUnits(Set.of("excludedRootUnits"));
        accessContractModelDto.setRuleCategoryToFilter(Set.of(RuleType.AppraisalRule));
        accessContractModelDto.setRuleCategoryToFilterForTheOtherOriginatingAgencies(
            Set.of(RuleType.DisseminationRule)
        );
        accessContractModelDto.setOriginatingAgencies(Set.of("originatingAgencies"));
        accessContractModelDto.setDoNotFilterFilingSchemes(true);
        accessContractModelDto.setDataObjectVersion(Set.of("dataObjectVersion"));
        accessContractModelDto.setWritingRestrictedDesc(true);
        return accessContractModelDto;
    }

    AccessContractModel accessContractModelDto_02() {
        AccessContractModel accessContractModelDto = new AccessContractModel();
        accessContractModelDto.setId("idesr");
        accessContractModelDto.setVersion(1442);
        accessContractModelDto.setName("namdrtdrte");
        accessContractModelDto.setIdentifier("identifdrtbdrtier");
        accessContractModelDto.setDescription("descriptbsrttion");
        accessContractModelDto.setStatus(ActivationStatus.ACTIVE);
        accessContractModelDto.setWritingPermission(false);
        accessContractModelDto.setEveryOriginatingAgency(false);
        accessContractModelDto.setEveryDataObjectVersion(false);
        accessContractModelDto.setAccessLog(ActivationStatus.INACTIVE);
        accessContractModelDto.setRootUnits(null);
        accessContractModelDto.setExcludedRootUnits(null);
        accessContractModelDto.setRuleCategoryToFilter(null);
        accessContractModelDto.setRuleCategoryToFilterForTheOtherOriginatingAgencies(null);
        accessContractModelDto.setOriginatingAgencies(null);
        accessContractModelDto.setDoNotFilterFilingSchemes(false);
        accessContractModelDto.setDataObjectVersion(null);
        accessContractModelDto.setWritingRestrictedDesc(false);
        return accessContractModelDto;
    }
}
