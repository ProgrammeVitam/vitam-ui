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

package fr.gouv.vitamui.collect.internal.server.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.collect.common.dto.ArchaeologistGetorixAddressDto;
import fr.gouv.vitamui.collect.common.dto.DepositStatus;
import fr.gouv.vitamui.collect.common.dto.GetorixDepositDto;
import fr.gouv.vitamui.collect.internal.server.dao.GetorixDepositRepository;
import fr.gouv.vitamui.collect.internal.server.domain.GetorixDepositModel;
import fr.gouv.vitamui.collect.internal.server.service.converters.GetorixDepositConverter;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static fr.gouv.vitamui.iam.common.utils.IamDtoBuilder.buildUserDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class GetorixDepositInternalServiceTest {

    private GetorixDepositInternalService getorixDepositInternalService;

    private final GetorixDepositRepository getorixDepositRepository =
        mock(GetorixDepositRepository.class);

    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);


    private final GetorixDepositConverter getorixDepositConverter = new GetorixDepositConverter();


    @BeforeEach
    public void setup() throws Exception {

        getorixDepositInternalService = new GetorixDepositInternalService(sequenceRepository, getorixDepositRepository,
            getorixDepositConverter, internalSecurityService);

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void testCreateGetorixDeposit_ok_when_all_condition_ok() {
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.save(ArgumentMatchers.any())).thenReturn(other);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        final GetorixDepositDto created = getorixDepositInternalService.createGetorixDeposit(getorixDepositDto,
            new VitamContext(15));

        GetorixDepositDto result = new GetorixDepositDto();
        result.setUserId(created.getUserId());
        result.setFurniture(created.isFurniture());
        result.setTransactionId(created.getTransactionId());
        result.setDepositStatus(created.getDepositStatus());
        result.setTenantIdentifier(created.getTenantIdentifier());
        result.setArchaeologistGetorixAddress(created.getArchaeologistGetorixAddress());
        result.setProjectId(created.getProjectId());
        result.setId(created.getId());
        result.setOperationName(created.getOperationName());
        result.setOperationType(created.getOperationType());
        result.setFirstScientificOfficerFirstName(created.getFirstScientificOfficerFirstName());
        result.setFirstScientificOfficerLastName(created.getFirstScientificOfficerLastName());
        result.setNationalNumber(created.getNationalNumber());
        result.setCreationDate(created.getCreationDate());
        result.setArchiveVolume(created.getArchiveVolume());
        result.setPrescriptionOrderNumber(created.getPrescriptionOrderNumber());
        result.setOriginatingAgency(created.getOriginatingAgency());
        result.setVersatileService(created.getVersatileService());

        assertThat(result).isEqualToComparingFieldByField(created);
    }

    @Test
    void testCreateGetorixDeposit_ko_when_userId_not_correct() {

        VitamContext vitamContext = new VitamContext(15);
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        getorixDepositDto.setUserId("userId_new_value");
        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.save(ArgumentMatchers.any())).thenReturn(other);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        assertThatCode(()-> getorixDepositInternalService.createGetorixDeposit(getorixDepositDto, vitamContext))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("You can not create the deposit");
    }

    @Test
    void testCreateGetorixDeposit_ko_when_tenantIdentifier_not_correct() {
        VitamContext vitamContext = new VitamContext(1);
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.save(ArgumentMatchers.any())).thenReturn(other);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        assertThatCode(()-> getorixDepositInternalService.createGetorixDeposit(getorixDepositDto, vitamContext))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("You can not create the deposit");
    }

    @Test
    void testCreateGetorixDeposit_ko_when_user_not_connected() {

        VitamContext vitamContext = new VitamContext(15);
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.save(ArgumentMatchers.any())).thenReturn(other);

        Mockito.when(internalSecurityService.getUser()).thenReturn(null);

        assertThatCode(()-> getorixDepositInternalService.createGetorixDeposit(getorixDepositDto, vitamContext))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessage("You are not authorized to create the deposit ");
    }

    private AuthUserDto buildAuthUserDto() {
        return new AuthUserDto(buildUserDto("userId", "eee@eee.fr", "groupId", "customerId", "Level"));
    }

    private GetorixDepositModel buildGetorixDepositModel() {
        ArchaeologistGetorixAddressDto archaeologistGetorixAddressDto = new ArchaeologistGetorixAddressDto();
        archaeologistGetorixAddressDto.setCommune("Marrakech");
        archaeologistGetorixAddressDto.setDepartment("Kelaa");
        archaeologistGetorixAddressDto.setPlaceName("Sraghna");
        archaeologistGetorixAddressDto.setInseeNumber("erer_488744");

        GetorixDepositModel getorixDepositModel = new GetorixDepositModel();

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
