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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.DataObjectVersionType;
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicy;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicyTypeEnum;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierUsage;
import fr.gouv.vitam.common.model.administration.StorageDetailModel;
import fr.gouv.vitam.common.model.administration.VersionRetentionPolicyModel;
import fr.gouv.vitam.common.model.administration.VersionUsageModel;
import fr.gouv.vitamui.commons.api.domain.PersistentIdentifierPolicyDto;
import fr.gouv.vitamui.commons.api.domain.PersistentIdentifierUsageDto;
import fr.gouv.vitamui.commons.api.domain.StorageDetailDto;
import fr.gouv.vitamui.commons.api.domain.VersionRetentionPolicyDto;
import fr.gouv.vitamui.commons.api.enums.IntermediaryVersionEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PatchManagementContractModelTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testPatchManagementContractModel() throws JsonProcessingException {
        ManagementContractModel managementContractModel = new ManagementContractModel();
        managementContractModel.setId("aefqaaaaaeecgkwkabam4aml6kss4saaaaaq");
        managementContractModel.setTenant(1);
        managementContractModel.setVersion(12);
        managementContractModel.setName("CG - DRA - 1");
        managementContractModel.setIdentifier("CG - DRA - 1");
        managementContractModel.setDescription("xxx");
        managementContractModel.setStatus(ActivationStatus.INACTIVE);


        // Création de l'objet StorageDetailDto
        StorageDetailModel storageDetailModel = new StorageDetailModel();
        storageDetailModel.setUnitStrategy("default");
        storageDetailModel.setObjectGroupStrategy("default");
        storageDetailModel.setObjectStrategy("default");
        managementContractModel.setStorage(storageDetailModel);

        // Création de l'objet VersionRetentionPolicyDto
        VersionRetentionPolicyModel versionRetentionPolicyModel = new VersionRetentionPolicyModel();

        // Création de la liste d'objets PersistentIdentifierPolicyDto
        List<PersistentIdentifierPolicy> persistentIdentifierPolicyList = new ArrayList<>();

        PersistentIdentifierPolicy persistentIdentifierPolicyDto = new PersistentIdentifierPolicy();
        persistentIdentifierPolicyDto.setPersistentIdentifierPolicyType(PersistentIdentifierPolicyTypeEnum.ARK);
        persistentIdentifierPolicyDto.setPersistentIdentifierUnit(true);
        persistentIdentifierPolicyDto.setPersistentIdentifierAuthority("000043");

        // Création de la liste d'objets UsageDto
        List<PersistentIdentifierUsage> persistentIdentifierUsages = new ArrayList<>();

        PersistentIdentifierUsage binaryMasterUsage = new PersistentIdentifierUsage();
        binaryMasterUsage.setUsageName(DataObjectVersionType.BINARY_MASTER);
        binaryMasterUsage.setInitialVersion(true);
        binaryMasterUsage.setIntermediaryVersion(VersionUsageModel.IntermediaryVersionEnum.ALL);
        persistentIdentifierUsages.add(binaryMasterUsage);

        PersistentIdentifierUsage disseminationUsage = new PersistentIdentifierUsage();
        disseminationUsage.setUsageName(DataObjectVersionType.DISSEMINATION);
        disseminationUsage.setInitialVersion(true);
        disseminationUsage.setIntermediaryVersion(VersionUsageModel.IntermediaryVersionEnum.ALL);
        persistentIdentifierUsages.add(disseminationUsage);

        persistentIdentifierPolicyDto.setPersistentIdentifierUsages(persistentIdentifierUsages);

        persistentIdentifierPolicyList.add(persistentIdentifierPolicyDto);

        managementContractModel.setVersionRetentionPolicy(versionRetentionPolicyModel);
        managementContractModel.setPersistentIdentifierPolicyList(persistentIdentifierPolicyList);

        // Conversion de l'objet en chaîne JSON
        String json = objectMapper.writeValueAsString(managementContractModel);

        // Conversion de la chaîne JSON en objet PatchManagementContractDto
        PatchManagementContractModel patchedDto = objectMapper.readValue(json, PatchManagementContractModel.class);

        assertNotNull(patchedDto);

        String patchedJson = objectMapper.writeValueAsString(patchedDto);

        ManagementContractModel patchedManagementContract = objectMapper.readValue(patchedJson, ManagementContractModel.class);

        // Vérification que les champs ignorés par @JsonIgnore sont à null
        assertNull(patchedManagementContract.getId());
        assertNull(patchedManagementContract.getTenant());
        assertNull(patchedManagementContract.getVersion());
        assertNull(patchedManagementContract.getIdentifier());
        assertNull(patchedManagementContract.getVersionRetentionPolicy());
        assertNull(patchedManagementContract.getCreationdate());
        assertNull(patchedManagementContract.getLastupdate());
        assertNull(patchedManagementContract.getActivationdate());
        assertNull(patchedManagementContract.getDeactivationdate());
    }
}