/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.internal.server.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.gouv.vitam.common.model.administration.ContextModel;
import fr.gouv.vitam.common.model.administration.PermissionModel;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.PermissionDto;

public class ContextConverter {

    public ContextModel convertDtoToVitam(final ContextDto dto) {
        final ContextModel context = VitamUIUtils.copyProperties(dto, new ContextModel());

        return context;
    }

    public ContextDto convertVitamToDto(final ContextModel context) {
        final ContextDto dto = VitamUIUtils.copyProperties(context, new ContextDto());

        if ( context.getStatus() != null ) {
            dto.setStatus(context.getStatus().toString());
        }

        dto.setCreationDate(context.getCreationdate());
        dto.setLastUpdate(context.getLastupdate());
        dto.setDeactivationDate(context.getDeactivationdate());
        dto.setActivationDate(context.getActivationdate());
        dto.setEnableControl(context.isEnablecontrol());
        dto.setSecurityProfile(context.getSecurityProfileIdentifier());

        Set<PermissionDto> permissions = new HashSet<>();
        for (PermissionModel permission: context.getPermissions()) {
            PermissionDto permissionDto = new PermissionDto();
            permissionDto.setTenant(permission.getTenant().toString());
            permissionDto.setAccessContracts(permission.getAccessContract());
            permissionDto.setIngestContracts(permission.getIngestContract());
            permissions.add(permissionDto);
        }

        dto.setPermissions(permissions);

        return dto;
    }

    public List<ContextModel> convertDtosToVitams(final List<ContextDto> dtos) {
        return dtos.stream().map(this::convertDtoToVitam).collect(Collectors.toList());
    }

    public List<ContextDto> convertVitamsToDtos(final List<ContextModel> contexts) {
        return contexts.stream().map(this::convertVitamToDto).collect(Collectors.toList());
    }

}
