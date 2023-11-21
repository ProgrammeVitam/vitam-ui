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
 */

package fr.gouv.vitamui.referential.internal.server.managementcontract;

import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.DataObjectVersionType;
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicy;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicyTypeEnum;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierUsage;
import fr.gouv.vitam.common.model.administration.StorageDetailModel;
import fr.gouv.vitam.common.model.administration.VersionRetentionPolicyModel;
import fr.gouv.vitam.common.model.administration.VersionUsageModel;
import fr.gouv.vitamui.commons.api.domain.ManagementContractDto;
import fr.gouv.vitamui.commons.api.domain.PersistentIdentifierPolicyMgtContractDto;
import fr.gouv.vitamui.commons.api.domain.PersistentIdentifierUsageMgtContractDto;
import fr.gouv.vitamui.commons.api.domain.StorageManagementContractDto;
import fr.gouv.vitamui.commons.api.domain.VersionRetentionPolicyMgtContractDto;
import fr.gouv.vitamui.commons.api.domain.VersionUsageMgtContractDto;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ManagementContractDtoToModelConverter
    implements Converter<ManagementContractDto, ManagementContractModel> {

    @Override
    public ManagementContractModel convert(@NotNull ManagementContractDto source) {
        ManagementContractModel managementContractModel = new ManagementContractModel();
        copyBasicProperties(source, managementContractModel);
        managementContractModel.setStorage(convertStorage(source.getStorage()));
        managementContractModel.setVersionRetentionPolicy(
            convertVersionRetentionPolicy(source.getVersionRetentionPolicy()));
        managementContractModel.setPersistentIdentifierPolicyList(
            convertPersistentIdentifierPolicies(source.getPersistentIdentifierPolicyList()));
        return managementContractModel;
    }

    private void copyBasicProperties(ManagementContractDto source, ManagementContractModel target) {
        target.setTenant(source.getTenant());
        target.setVersion(source.getVersion());
        target.setName(source.getName());
        target.setIdentifier(source.getIdentifier());
        target.setDescription(source.getDescription());
        target.setStatus(source.getStatus() != null ? Enum.valueOf(ActivationStatus.class, source.getStatus()) : null);
        target.setCreationdate(source.getCreationDate());
        target.setLastupdate(source.getLastUpdate());
        target.setDeactivationdate(source.getDeactivationDate());
        target.setActivationdate(source.getActivationDate());
    }

    private StorageDetailModel convertStorage(StorageManagementContractDto storageDto) {
        if (storageDto == null) {
            return null;
        }
        StorageDetailModel storageModel = new StorageDetailModel();
        storageModel.setUnitStrategy(storageDto.getUnitStrategy());
        storageModel.setObjectGroupStrategy(storageDto.getObjectGroupStrategy());
        storageModel.setObjectStrategy(storageDto.getObjectStrategy());
        return storageModel;
    }

    private VersionRetentionPolicyModel convertVersionRetentionPolicy(VersionRetentionPolicyMgtContractDto dto) {
        if (dto == null) {
            return null;
        }
        VersionRetentionPolicyModel versionRetentionPolicyModel = new VersionRetentionPolicyModel();
        versionRetentionPolicyModel.setInitialVersion(dto.isInitialVersion());
        versionRetentionPolicyModel.setIntermediaryVersion(
            Optional.ofNullable(dto.getIntermediaryVersion())
                .map(intermediaryVersionDto -> Enum.valueOf(VersionUsageModel.IntermediaryVersionEnum.class, intermediaryVersionDto.name()))
                .orElseThrow());
        versionRetentionPolicyModel.setUsages(convertVersionUsages(dto.getUsages()));
        return versionRetentionPolicyModel;
    }

    private Set<VersionUsageModel> convertVersionUsages(Set<VersionUsageMgtContractDto> usageDtos) {
        if (usageDtos == null) {
            return null;
        }
        return usageDtos.stream().map(this::convertVersionUsage).collect(Collectors.toSet());
    }

    private VersionUsageModel convertVersionUsage(VersionUsageMgtContractDto dto) {
        if (dto == null) {
            return null;
        }
        VersionUsageModel versionUsageModel = new VersionUsageModel();
        versionUsageModel.setUsageName(dto.getUsageName());
        versionUsageModel.setInitialVersion(dto.isInitialVersion());
        versionUsageModel.setIntermediaryVersion(
            Optional.ofNullable(dto.getIntermediaryVersion())
                .map(intermediaryVersionDto -> Enum.valueOf(VersionUsageModel.IntermediaryVersionEnum.class, intermediaryVersionDto.name()))
                .orElseThrow());
        return versionUsageModel;
    }

    private List<PersistentIdentifierPolicy> convertPersistentIdentifierPolicies(
        List<PersistentIdentifierPolicyMgtContractDto> policyDtos) {
        if (policyDtos == null) {
            return null;
        }
        return policyDtos.stream().map(this::convertPersistentIdentifierPolicy).collect(Collectors.toList());
    }

    private PersistentIdentifierPolicy convertPersistentIdentifierPolicy(
        PersistentIdentifierPolicyMgtContractDto dto) {
        if (dto == null) {
            return null;
        }
        PersistentIdentifierPolicy policy = new PersistentIdentifierPolicy();
        policy.setPersistentIdentifierAuthority(dto.getPersistentIdentifierAuthority());
        policy.setPersistentIdentifierUnit(dto.isPersistentIdentifierUnit());
        policy.setPersistentIdentifierPolicyType(
            Optional.ofNullable(dto.getPersistentIdentifierPolicyType())
                .map(policyTypeDto -> Enum.valueOf(PersistentIdentifierPolicyTypeEnum.class, policyTypeDto))
                .orElseThrow());
        policy.setPersistentIdentifierUsages(convertPersistentIdentifierUsages(dto.getPersistentIdentifierUsages()));
        return policy;
    }

    private List<PersistentIdentifierUsage> convertPersistentIdentifierUsages(
        List<PersistentIdentifierUsageMgtContractDto> usageDtos) {
        if (usageDtos == null) {
            return null;
        }
        return usageDtos.stream().map(this::convertPersistentIdentifierUsage).collect(Collectors.toList());
    }

    private PersistentIdentifierUsage convertPersistentIdentifierUsage(
        PersistentIdentifierUsageMgtContractDto dto) {
        if (dto == null) {
            return null;
        }
        PersistentIdentifierUsage usage = new PersistentIdentifierUsage();
        usage.setUsageName(
            Optional.ofNullable(dto.getUsageName())
                .map(DataObjectVersionType::fromName)
                .orElseThrow());
        usage.setInitialVersion(dto.isInitialVersion());
        usage.setIntermediaryVersion(
            Optional.ofNullable(dto.getIntermediaryVersion())
                .map(intermediaryVersionDto -> Enum.valueOf(VersionUsageModel.IntermediaryVersionEnum.class, intermediaryVersionDto.name()))
                .orElseThrow());
        return usage;
    }
}