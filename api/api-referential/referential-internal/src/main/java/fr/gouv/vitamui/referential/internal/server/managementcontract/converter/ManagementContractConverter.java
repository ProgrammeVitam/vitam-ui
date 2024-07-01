/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */
package fr.gouv.vitamui.referential.internal.server.managementcontract.converter;

import fr.gouv.vitamui.commons.api.domain.ManagementContractDto;
import fr.gouv.vitamui.commons.api.domain.ManagementContractModelDto;
import fr.gouv.vitamui.commons.api.domain.StorageDetailDto;
import fr.gouv.vitamui.commons.api.domain.StorageManagementContractDto;
import fr.gouv.vitamui.commons.api.domain.VersionRetentionPolicyDto;
import fr.gouv.vitamui.commons.api.domain.VersionRetentionPolicyMgtContractDto;
import fr.gouv.vitamui.commons.api.domain.VersionUsageDto;
import fr.gouv.vitamui.commons.api.domain.VersionUsageMgtContractDto;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.referential.common.dto.ManagementContractVitamDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ManagementContractConverter {

    public ManagementContractVitamDto convertVitamUiManagementContractToVitamMgt(final ManagementContractDto dto) {
        ManagementContractVitamDto managementContractVitamDto = VitamUIUtils.copyProperties(
            dto,
            new ManagementContractVitamDto()
        );
        StorageDetailDto storageDetailDto = new StorageDetailDto();
        if (dto.getStorage() != null) {
            storageDetailDto.setObjectStrategy(dto.getStorage().getObjectStrategy());
            storageDetailDto.setUnitStrategy(dto.getStorage().getUnitStrategy());
            storageDetailDto.setObjectGroupStrategy(dto.getStorage().getObjectGroupStrategy());
            managementContractVitamDto.setStorage(storageDetailDto);
        }
        VersionRetentionPolicyDto versionRetentionPolicyDto = new VersionRetentionPolicyDto();
        if (dto.getVersionRetentionPolicy() != null) {
            versionRetentionPolicyDto.setInitialVersion(dto.getVersionRetentionPolicy().isInitialVersion());
            versionRetentionPolicyDto.setIntermediaryVersion(dto.getVersionRetentionPolicy().getIntermediaryVersion());
            Set<VersionUsageDto> versionUsageDtos = new HashSet<>();
            if (
                dto.getVersionRetentionPolicy().getUsages() != null &&
                !dto.getVersionRetentionPolicy().getUsages().isEmpty()
            ) {
                dto
                    .getVersionRetentionPolicy()
                    .getUsages()
                    .forEach(
                        versionUsage ->
                            versionUsageDtos.add(VitamUIUtils.copyProperties(versionUsage, new VersionUsageDto()))
                    );
            }
            versionRetentionPolicyDto.setUsages(versionUsageDtos);
            managementContractVitamDto.setVersionRetentionPolicy(versionRetentionPolicyDto);
        }

        return managementContractVitamDto;
    }

    public ManagementContractDto convertVitamMgtContractToVitamUiDto(
        final ManagementContractVitamDto managementContractVitamDto
    ) {
        final ManagementContractDto managementContractDto = VitamUIUtils.copyProperties(
            managementContractVitamDto,
            new ManagementContractDto()
        );
        if (managementContractVitamDto.getStorage() != null) {
            managementContractDto.setStorage(
                VitamUIUtils.copyProperties(managementContractVitamDto.getStorage(), new StorageManagementContractDto())
            );
        }

        VersionRetentionPolicyMgtContractDto versionRetentionPolicyMgtContractDto =
            new VersionRetentionPolicyMgtContractDto();

        if (managementContractVitamDto.getVersionRetentionPolicy() != null) {
            versionRetentionPolicyMgtContractDto.setInitialVersion(
                managementContractVitamDto.getVersionRetentionPolicy().getInitialVersion()
            );
            versionRetentionPolicyMgtContractDto.setIntermediaryVersion(
                managementContractVitamDto.getVersionRetentionPolicy().getIntermediaryVersion()
            );
            Set<VersionUsageMgtContractDto> versionUsageMgtContractDtoSet = new HashSet<>();
            if (
                managementContractVitamDto.getVersionRetentionPolicy().getUsages() != null &&
                !managementContractVitamDto.getVersionRetentionPolicy().getUsages().isEmpty()
            ) {
                managementContractVitamDto
                    .getVersionRetentionPolicy()
                    .getUsages()
                    .forEach(
                        usageVersion ->
                            versionUsageMgtContractDtoSet.add(
                                VitamUIUtils.copyProperties(usageVersion, new VersionUsageMgtContractDto())
                            )
                    );
            }
            versionRetentionPolicyMgtContractDto.setUsages(versionUsageMgtContractDtoSet);
            managementContractDto.setVersionRetentionPolicy(versionRetentionPolicyMgtContractDto);
        }
        return managementContractDto;
    }

    public List<ManagementContractModelDto> convertVitamUiListMgtContractToVitamListMgtContract(
        final List<ManagementContractDto> managementContractDtoList
    ) {
        return managementContractDtoList
            .stream()
            .map(this::convertVitamUiManagementContractToVitamMgt)
            .collect(Collectors.toList());
    }

    public List<ManagementContractDto> convertVitamListMgtContractToVitamUIMgtContractDtos(
        final List<ManagementContractVitamDto> managementContractVitamDtoList
    ) {
        return managementContractVitamDtoList
            .stream()
            .map(this::convertVitamMgtContractToVitamUiDto)
            .collect(Collectors.toList());
    }
}
