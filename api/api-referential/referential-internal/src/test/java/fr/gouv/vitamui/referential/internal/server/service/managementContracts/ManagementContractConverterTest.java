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

package fr.gouv.vitamui.referential.internal.server.service.managementContracts;

import fr.gouv.vitamui.commons.api.domain.ManagementContractDto;
import fr.gouv.vitamui.commons.api.domain.ManagementContractModelDto;
import fr.gouv.vitamui.commons.api.domain.PersistentIdentifierPolicyDto;
import fr.gouv.vitamui.commons.api.domain.PersistentIdentifierPolicyMgtContractDto;
import fr.gouv.vitamui.commons.api.domain.PersistentIdentifierUsageMgtContractDto;
import fr.gouv.vitamui.commons.api.enums.IntermediaryVersionEnum;
import fr.gouv.vitamui.referential.common.dto.ManagementContractVitamDto;
import fr.gouv.vitamui.referential.internal.server.managementcontract.converter.ManagementContractConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ManagementContractConverterTest {

    private final ManagementContractConverter managementContractConverter = new ManagementContractConverter();

    @Test
    void testConvertVitamUiMgtContractToVitamMgtContract() {
        // Given
        ManagementContractDto managementContractDto = new ManagementContractDto();
        managementContractDto.setActivationDate("12/12/2022");
        managementContractDto.setIdentifier("contractIdentifier");
        managementContractDto.setId("contractId");
        managementContractDto.setName("ContractName");

        // Créez une politique d'identifiant persistant pour le contrat de gestion
        PersistentIdentifierPolicyMgtContractDto persistentIdentifierPolicyMgtContractDto =
            new PersistentIdentifierPolicyMgtContractDto();
        persistentIdentifierPolicyMgtContractDto.setPersistentIdentifierPolicyType("ARK");
        persistentIdentifierPolicyMgtContractDto.setPersistentIdentifierUnit(true);
        persistentIdentifierPolicyMgtContractDto.setPersistentIdentifierAuthority("12354");

        // Créez une liste de usages d'identifiant persistant pour la politique
        List<PersistentIdentifierUsageMgtContractDto> persistentIdentifierUsages = new ArrayList<>();
        PersistentIdentifierUsageMgtContractDto usageMgtContractDto1 = new PersistentIdentifierUsageMgtContractDto();
        usageMgtContractDto1.setUsageName("BinaryMaster");
        usageMgtContractDto1.setInitialVersion(true);
        usageMgtContractDto1.setIntermediaryVersion(IntermediaryVersionEnum.LAST);
        persistentIdentifierUsages.add(usageMgtContractDto1);

        // Ajoutez la liste de usages à la politique d'identifiant persistant
        persistentIdentifierPolicyMgtContractDto.setPersistentIdentifierUsages(persistentIdentifierUsages);

        // Ajoutez la politique d'identifiant persistant au contrat de gestion
        managementContractDto.setPersistentIdentifierPolicyList(List.of(persistentIdentifierPolicyMgtContractDto));

        // When
        ManagementContractModelDto managementContractModelDto = managementContractConverter
            .convertVitamUiManagementContractToVitamMgt(managementContractDto);

        // Then
        assertThat(managementContractModelDto).isNotNull()
            .isInstanceOf(ManagementContractModelDto.class);
        assertThat(managementContractDto).usingRecursiveComparison()
            .isEqualTo(managementContractModelDto);

        // Assurez-vous que la politique d'identifiant persistant est correctement mappée dans la liste
        List<PersistentIdentifierPolicyDto> persistentIdentifierPolicyList =
            managementContractModelDto.getPersistentIdentifierPolicyList();
        assertThat(persistentIdentifierPolicyList).isNotNull().hasSize(1);

        PersistentIdentifierPolicyDto persistentIdentifierPolicyDto = persistentIdentifierPolicyList.get(0);
        assertThat(persistentIdentifierPolicyDto).isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(persistentIdentifierPolicyMgtContractDto);
    }


    @Test
    void testConvertVitamMgtContractToVitamUiMgtContract() {
        // Given
        ManagementContractVitamDto managementContractModelDto = new ManagementContractVitamDto();
        managementContractModelDto.setActivationDate("12/12/2022");
        managementContractModelDto.setIdentifier("contractIdentifier");
        managementContractModelDto.setId("contractId");
        managementContractModelDto.setName("ContractName");

        // When
        ManagementContractDto managementContractDto = managementContractConverter
            .convertVitamMgtContractToVitamUiDto(managementContractModelDto);

        // Then
        assertThat(managementContractDto).isNotNull()
            .isInstanceOf(ManagementContractDto.class);
        /* assertThat(managementContractModelDto).isEqualToComparingFieldByField(managementContractDto);*/

    }
}
