package fr.gouv.vitamui.referential.common.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.gouv.vitam.common.model.administration.ContextStatus;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.PermissionDto;

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
