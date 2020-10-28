package fr.gouv.vitamui.referential.common.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.ContextStatus;
import fr.gouv.vitamui.referential.common.dto.*;

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

    public static AccessContractDto buildAccessContractDto(final String id) {
        final AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setId(id);
        accessContractDto.setIdentifier("identifier");
        accessContractDto.setName("name");
        accessContractDto.setStatus(ActivationStatus.ACTIVE.toString());

        return accessContractDto;
    }

    public static IngestContractDto buildIngestContractDto(final String id) {
        final IngestContractDto ingestContractDto = new IngestContractDto();
        ingestContractDto.setId(id);
        ingestContractDto.setIdentifier("identifier");
        ingestContractDto.setName("name");
        ingestContractDto.setStatus(ActivationStatus.ACTIVE);

        return ingestContractDto;
    }

    public static SecurityProfileDto buildSecurityProfileDto(final String id) {
        final SecurityProfileDto securityProfileDto = new SecurityProfileDto();
        securityProfileDto.setId(id);
        securityProfileDto.setIdentifier("identifier");
        securityProfileDto.setName("name");

        return securityProfileDto;
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
