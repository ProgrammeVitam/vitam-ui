package fr.gouv.vitamui.referential.common.utils;

import java.util.HashSet;
import java.util.Set;

import fr.gouv.vitam.common.model.administration.ContextStatus;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.PermissionDto;
import fr.gouv.vitamui.referential.common.dto.RuleDto;

public class ReferentialDtoBuilder {

    public static ContextDto buildContextDto(final String id) {
        final ContextDto contextDto = new ContextDto();
        contextDto.setId(id);
        contextDto.setIdentifier("identifier");
        contextDto.setName("name");
        contextDto.setStatus(ContextStatus.ACTIVE.toString());
        contextDto.setEnableControl(true);
        contextDto.setSecurityProfile("securityProfile");
        // contextDto.setPermissions(buildPermissions());

        return contextDto;
    }

    public static RuleDto buildRuleDto(final String id, final String ruleId,
                                       final String ruleType, final String ruleValue,
                                       final String ruleDescription, final String ruleDuration,
                                       final String ruleMeasurement) {
        final RuleDto ruleDto = new RuleDto();
        ruleDto.setId(id);
        ruleDto.setRuleId(ruleId);
        ruleDto.setRuleType(ruleType);
        ruleDto.setRuleValue(ruleValue);
        ruleDto.setRuleDescription(ruleDescription);
        ruleDto.setRuleDuration(ruleDuration);
        ruleDto.setRuleMeasurement(ruleMeasurement);

        return ruleDto;
    }

    private static Set<PermissionDto> buildPermissions() {
        Set<String> contracts = new HashSet<String>();
        contracts.add("contract");

        PermissionDto permission1 = new PermissionDto();
        permission1.setTenant("tenant_1");
        permission1.setAccessContracts(contracts);
        permission1.setIngestContracts(contracts);

        PermissionDto permission2 = new PermissionDto();
        permission2.setTenant("tenant_2");
        permission2.setAccessContracts(contracts);
        permission2.setIngestContracts(contracts);

        Set<PermissionDto> permissions = new HashSet<PermissionDto>();
        permissions.add(permission1);
        permissions.add(permission2);
        return permissions;
    }

}
