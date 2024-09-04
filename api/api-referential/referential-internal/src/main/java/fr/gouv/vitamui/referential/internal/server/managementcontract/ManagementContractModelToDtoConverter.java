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

import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicy;
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
import fr.gouv.vitamui.commons.api.enums.IntermediaryVersionEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ManagementContractModelToDtoConverter
    implements Converter<ManagementContractModel, ManagementContractDto> {

    @Override
    public ManagementContractDto convert(@NotNull ManagementContractModel source) {
        ManagementContractDto managementContractDto = new ManagementContractDto();
        copyBasicProperties(source, managementContractDto);
        managementContractDto.setStorage(convertStorage(source.getStorage()));
        managementContractDto.setVersionRetentionPolicy(
            convertVersionRetentionPolicy(source.getVersionRetentionPolicy())
        );
        managementContractDto.setPersistentIdentifierPolicyList(
            convertPersistentIdentifierPolicies(source.getPersistentIdentifierPolicyList())
        );
        return managementContractDto;
    }

    private void copyBasicProperties(ManagementContractModel source, ManagementContractDto target) {
        if (source != null && target != null) {
            target.setTenant(source.getTenant());
            target.setVersion(source.getVersion());
            target.setName(source.getName());
            target.setIdentifier(source.getIdentifier());
            target.setDescription(source.getDescription());
            target.setStatus(source.getStatus() != null ? source.getStatus().name() : null);
            target.setCreationDate(source.getCreationdate());
            target.setLastUpdate(source.getLastupdate());
            target.setDeactivationDate(source.getDeactivationdate());
            target.setActivationDate(source.getActivationdate());
        }
    }

    private StorageManagementContractDto convertStorage(StorageDetailModel storage) {
        if (storage == null) {
            return null;
        }

        StorageManagementContractDto storageDto = new StorageManagementContractDto();
        storageDto.setUnitStrategy(storage.getUnitStrategy());
        storageDto.setObjectGroupStrategy(storage.getObjectGroupStrategy());
        storageDto.setObjectStrategy(storage.getObjectStrategy());
        return storageDto;
    }

    private VersionRetentionPolicyMgtContractDto convertVersionRetentionPolicy(
        VersionRetentionPolicyModel versionRetentionPolicy
    ) {
        if (versionRetentionPolicy == null) {
            return null;
        }

        VersionRetentionPolicyMgtContractDto dto = new VersionRetentionPolicyMgtContractDto();
        dto.setInitialVersion(versionRetentionPolicy.getInitialVersion());
        dto.setIntermediaryVersion(
            versionRetentionPolicy.getIntermediaryVersion() != null
                ? IntermediaryVersionEnum.valueOf(versionRetentionPolicy.getIntermediaryVersion().name())
                : null
        );
        dto.setUsages(convertVersionUsages(versionRetentionPolicy.getUsages()));
        return dto;
    }

    private Set<VersionUsageMgtContractDto> convertVersionUsages(Set<VersionUsageModel> versionUsages) {
        if (versionUsages == null) {
            return null;
        }

        return versionUsages.stream().map(this::convertVersionUsage).collect(Collectors.toSet());
    }

    private VersionUsageMgtContractDto convertVersionUsage(VersionUsageModel versionUsageModel) {
        if (versionUsageModel == null) {
            return null;
        }

        VersionUsageMgtContractDto dto = new VersionUsageMgtContractDto();
        dto.setUsageName(versionUsageModel.getUsageName());
        dto.setInitialVersion(versionUsageModel.getInitialVersion());
        dto.setIntermediaryVersion(
            versionUsageModel.getIntermediaryVersion() != null
                ? IntermediaryVersionEnum.valueOf(versionUsageModel.getIntermediaryVersion().name())
                : null
        );
        return dto;
    }

    private List<PersistentIdentifierPolicyMgtContractDto> convertPersistentIdentifierPolicies(
        List<PersistentIdentifierPolicy> policies
    ) {
        if (policies == null) {
            return null;
        }

        return policies.stream().map(this::convertPersistentIdentifierPolicy).collect(Collectors.toList());
    }

    private PersistentIdentifierPolicyMgtContractDto convertPersistentIdentifierPolicy(
        PersistentIdentifierPolicy policy
    ) {
        if (policy == null) {
            return null;
        }

        PersistentIdentifierPolicyMgtContractDto dto = new PersistentIdentifierPolicyMgtContractDto();
        dto.setPersistentIdentifierAuthority(policy.getPersistentIdentifierAuthority());
        dto.setPersistentIdentifierUnit(policy.isPersistentIdentifierUnit());
        dto.setPersistentIdentifierPolicyType(
            policy.getPersistentIdentifierPolicyType() != null
                ? policy.getPersistentIdentifierPolicyType().name()
                : null
        );
        dto.setPersistentIdentifierUsages(convertPersistentIdentifierUsages(policy.getPersistentIdentifierUsages()));
        return dto;
    }

    private List<PersistentIdentifierUsageMgtContractDto> convertPersistentIdentifierUsages(
        List<PersistentIdentifierUsage> usages
    ) {
        if (usages == null) {
            return null;
        }

        return usages.stream().map(this::convertPersistentIdentifierUsage).collect(Collectors.toList());
    }

    private PersistentIdentifierUsageMgtContractDto convertPersistentIdentifierUsage(PersistentIdentifierUsage usage) {
        if (usage == null) {
            return null;
        }

        PersistentIdentifierUsageMgtContractDto dto = new PersistentIdentifierUsageMgtContractDto();
        dto.setUsageName(usage.getUsageName() != null ? usage.getUsageName().getName() : null);
        dto.setInitialVersion(usage.isInitialVersion());
        dto.setIntermediaryVersion(
            usage.getIntermediaryVersion() != null
                ? IntermediaryVersionEnum.valueOf(usage.getIntermediaryVersion().name())
                : null
        );
        return dto;
    }
}
