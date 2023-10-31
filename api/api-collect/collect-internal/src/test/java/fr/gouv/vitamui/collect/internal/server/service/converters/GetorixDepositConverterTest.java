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

package fr.gouv.vitamui.collect.internal.server.service.converters;

import fr.gouv.vitamui.collect.common.dto.ArchaeologistGetorixAddressDto;
import fr.gouv.vitamui.collect.common.dto.DepositStatus;
import fr.gouv.vitamui.collect.common.dto.GetorixDepositDto;
import fr.gouv.vitamui.collect.internal.server.domain.GetorixDepositModel;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GetorixDepositConverterTest {


    private final GetorixDepositConverter getorixDepositConverter = new GetorixDepositConverter();
    @Test
    void convertGetorixDepositDtoGetorixDepositModel() {
        GetorixDepositDto getorixDepositDto = buildGetorixDepositDto();
        GetorixDepositModel getorixDepositModel = getorixDepositConverter.convertDtoToEntity(getorixDepositDto);
        assertNotNull(getorixDepositModel);
        assertNotNull(getorixDepositModel.getArchaeologistGetorixAddress());
        assertThat(getorixDepositDto).isEqualToComparingFieldByField(getorixDepositModel);
    }

    @Test
    void convertGetorixDepositModeltoGetorixDepositD() {
        GetorixDepositModel getorixDepositModel = buildGetorixDepositModel();
        GetorixDepositDto getorixDepositDto = getorixDepositConverter.convertEntityToDto(getorixDepositModel);
        assertNotNull(getorixDepositDto);
        assertNotNull(getorixDepositDto.getArchaeologistGetorixAddress());
        assertThat(getorixDepositDto).isEqualToComparingFieldByField(getorixDepositModel);
    }

    private GetorixDepositDto buildGetorixDepositDto() {
        ArchaeologistGetorixAddressDto archaeologistGetorixAddressDto = new ArchaeologistGetorixAddressDto();
        archaeologistGetorixAddressDto.setCommune("Paris");
        archaeologistGetorixAddressDto.setDepartment("Paris");
        archaeologistGetorixAddressDto.setPlaceName("Paris");
        archaeologistGetorixAddressDto.setInseeNumber("erer_488744");

        GetorixDepositDto getorixDepositDto = new GetorixDepositDto();

        getorixDepositDto.setId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
        getorixDepositDto.setOperationName("operation name");
        getorixDepositDto.setOperationType("SEARCH");
        getorixDepositDto.setDepositStatus(DepositStatus.IN_PROGRESS);
        getorixDepositDto.setFirstScientificOfficerFirstName("firstName");
        getorixDepositDto.setFirstScientificOfficerLastName("lastName");
        getorixDepositDto.setArchaeologistGetorixAddress(archaeologistGetorixAddressDto);
        getorixDepositDto.setNationalNumber("abcd_157486");
        getorixDepositDto.setUserId("userId");
        getorixDepositDto.setCreationDate(OffsetDateTime.now());
        getorixDepositDto.setFurniture(true);
        getorixDepositDto.setTenantIdentifier(15);
        getorixDepositDto.setArchiveVolume(1975);
        getorixDepositDto.setPrescriptionOrderNumber(56);
        getorixDepositDto.setOriginatingAgency("originatingAgencyIdentifier");
        getorixDepositDto.setVersatileService("TransferringAgencyIdentifier");
        getorixDepositDto.setProjectId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaabq");

        return getorixDepositDto;
    }

    private GetorixDepositModel buildGetorixDepositModel() {
        ArchaeologistGetorixAddressDto archaeologistGetorixAddressDto = new ArchaeologistGetorixAddressDto();
        archaeologistGetorixAddressDto.setCommune("Paris");
        archaeologistGetorixAddressDto.setDepartment("Paris");
        archaeologistGetorixAddressDto.setPlaceName("Paris");
        archaeologistGetorixAddressDto.setInseeNumber("erer_488744");

        GetorixDepositModel getorixDepositModel = new GetorixDepositModel();

        getorixDepositModel.setId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
        getorixDepositModel.setOperationName("operation name");
        getorixDepositModel.setOperationType("SEARCH");
        getorixDepositModel.setDepositStatus(DepositStatus.IN_PROGRESS);
        getorixDepositModel.setFirstScientificOfficerFirstName("firstName");
        getorixDepositModel.setFirstScientificOfficerLastName("lastName");
        getorixDepositModel.setArchaeologistGetorixAddress(archaeologistGetorixAddressDto);
        getorixDepositModel.setNationalNumber("abcd_157486");
        getorixDepositModel.setUserId("userId");
        getorixDepositModel.setCreationDate(OffsetDateTime.now());
        getorixDepositModel.setFurniture(true);
        getorixDepositModel.setTenantIdentifier(15);
        getorixDepositModel.setArchiveVolume(1975);
        getorixDepositModel.setPrescriptionOrderNumber(56);
        getorixDepositModel.setOriginatingAgency("originatingAgencyIdentifier");
        getorixDepositModel.setVersatileService("TransferringAgencyIdentifier");
        getorixDepositModel.setProjectId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaabq");

        return getorixDepositModel;
    }
}
