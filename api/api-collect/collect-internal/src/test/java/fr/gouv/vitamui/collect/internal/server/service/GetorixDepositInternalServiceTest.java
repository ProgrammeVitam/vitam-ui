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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import fr.gouv.vitam.collect.common.dto.ProjectDto;
import fr.gouv.vitam.collect.common.dto.TransactionDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.collect.common.dto.ArchaeologistGetorixAddressDto;
import fr.gouv.vitamui.collect.common.dto.DepositStatus;
import fr.gouv.vitamui.collect.common.dto.GetorixDepositDto;
import fr.gouv.vitamui.collect.common.dto.UnitFullPath;
import fr.gouv.vitamui.collect.internal.server.dao.GetorixDepositRepository;
import fr.gouv.vitamui.collect.internal.server.domain.GetorixDepositModel;
import fr.gouv.vitamui.collect.internal.server.service.converters.GetorixDepositConverter;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.commons.vitam.api.dto.TitleDto;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.gouv.vitamui.iam.common.utils.IamDtoBuilder.buildUserDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class GetorixDepositInternalServiceTest {

    private GetorixDepositInternalService getorixDepositInternalService;

    private final GetorixDepositRepository getorixDepositRepository =
        mock(GetorixDepositRepository.class);

    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    private final CollectService collectService = mock(CollectService.class);

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final GetorixDepositConverter getorixDepositConverter = new GetorixDepositConverter();

    ObjectMapper objectMapper = new ObjectMapper();

    final PodamFactory factory = new PodamFactoryImpl();

    public final String VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT =
        "dataGetorix/vitam_units_response.json";

    public final String VITAM_UNIT_ONE_RESULT_UNIT_WITH_UNITUPS_LIST =
        "dataGetorix/vitam_units_response_with_unitups_list.json";



    @BeforeEach
    public void setup() {

        getorixDepositInternalService = new GetorixDepositInternalService(sequenceRepository, getorixDepositRepository,
            getorixDepositConverter, internalSecurityService, collectService, objectMapper);

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void testCreateGetorixDeposit_ok_when_all_condition_ok() throws VitamClientException, JsonProcessingException {

        // GIVEN
        // Create Project OK
        final ProjectDto projectDto = factory.manufacturePojo(ProjectDto.class);
        RequestResponseOK<ProjectDto> responseFromVitam = new RequestResponseOK<>();
        responseFromVitam.setHttpCode(200);
        responseFromVitam.setHits(1, 1, 1, 1);
        responseFromVitam.addResult(projectDto);

        RequestResponse<JsonNode> mockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(responseFromVitam)).build());

        Mockito.when(collectService.initProject(any(), any()))
            .thenReturn(mockResponse);

        // Create transaction : OK
        final TransactionDto transactionDto = factory.manufacturePojo(TransactionDto.class);
        RequestResponseOK<TransactionDto> transactionResponseFromVitam = new RequestResponseOK<>();
        transactionResponseFromVitam.setHttpCode(200);
        transactionResponseFromVitam.setHits(1, 1, 1, 1);
        transactionResponseFromVitam.addResult(transactionDto);

        RequestResponse<JsonNode> transactionMockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(transactionResponseFromVitam)).build());

        Mockito.when(collectService.initTransaction(any(), any(), any()))
            .thenReturn(transactionMockResponse);

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
    void testCreateGetorixDeposit_ko_when_init_transaction_is_ko() throws VitamClientException, JsonProcessingException {

        // GIVEN
        // Create Project OK
        final ProjectDto projectDto = factory.manufacturePojo(ProjectDto.class);
        RequestResponseOK<ProjectDto> responseFromVitam = new RequestResponseOK<>();
        responseFromVitam.setHttpCode(200);
        responseFromVitam.setHits(1, 1, 1, 1);
        responseFromVitam.addResult(projectDto);

        RequestResponse<JsonNode> mockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(responseFromVitam)).build());

        Mockito.when(collectService.initProject(any(), any()))
            .thenReturn(mockResponse);

        RequestResponse<ProjectDto> transactionResponseFromVitam = new RequestResponse<>() {
            @Override
            public Response toResponse() {
                return null;
            }
        };
        RequestResponse<JsonNode> transactionMockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(transactionResponseFromVitam)).build());
        Mockito.when(collectService.initTransaction(any(), any(), any()))
            .thenReturn(transactionMockResponse);

        VitamContext vitamContext = new VitamContext(15);
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.save(ArgumentMatchers.any())).thenReturn(other);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        assertThatCode(()-> getorixDepositInternalService.createGetorixDeposit(getorixDepositDto, vitamContext))
            .isInstanceOf(InternalServerException.class)
            .hasMessage("Unable to create transaction");
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
            .hasMessage("You are not authorized to create the deposit");
    }

    @Test
    void testCreateGetorixDeposit_ko_when_init_project_is_ko() throws VitamClientException, JsonProcessingException {

        // GIVEN
        RequestResponse<ProjectDto> responseFromVitam = new RequestResponse<>() {
            @Override
            public Response toResponse() {
                return null;
            }
        };
        RequestResponse<JsonNode> mockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(responseFromVitam)).build());
        Mockito.when(collectService.initProject(any(), any()))
            .thenReturn(mockResponse);

        VitamContext vitamContext = new VitamContext(15);
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.save(ArgumentMatchers.any())).thenReturn(other);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        assertThatCode(()-> getorixDepositInternalService.createGetorixDeposit(getorixDepositDto, vitamContext))
            .isInstanceOf(InternalServerException.class)
            .hasMessage("Unable to create project");
    }

    @Test
    void testGetGetorixDepositById_ko_when_user_not_connected() {

        String getorixDepositId = "getorixDepositId";
        Mockito.when(internalSecurityService.getUser()).thenReturn(null);

        assertThatCode(()-> getorixDepositInternalService.getGetorixDepositById(getorixDepositId))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessage("You are not authorized to get the deposit details");
    }

    @Test
    void testGetGetorixDepositById_OK_when_all_conditions_ok() {

        String getorixDepositId = "getorixDepositId";
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.findOne((Query) any())).thenReturn(Optional.of(other));

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        GetorixDepositDto getorixDepositDtoResult = getorixDepositInternalService.getGetorixDepositById(getorixDepositId);
        assertThatCode(()-> getorixDepositInternalService.getGetorixDepositById(getorixDepositId))
            .doesNotThrowAnyException();
        assertThat(getorixDepositDtoResult).isNotNull();

    }

    @Test
    void testUpdateGetorixDepositDetails_KO_when_update_transaction_KO()
        throws JsonProcessingException, VitamClientException {

        // GIVEN
        VitamContext vitamContext = new VitamContext(1);
        String getorixDepositId = "getorixDepositId";
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());
        getorixDepositDto.setId(getorixDepositId);

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        // Update Vitam Collect Project : OK
        final ProjectDto projectDto = factory.manufacturePojo(ProjectDto.class);
        projectDto.setId("projectId");
        RequestResponseOK<ProjectDto> responseFromVitam = new RequestResponseOK<>();
        responseFromVitam.setHttpCode(200);
        responseFromVitam.setHits(1, 1, 1, 1);
        responseFromVitam.addResult(projectDto);

        RequestResponse<JsonNode> mockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(responseFromVitam)).build());

        // Update Vitam Collect Transaction : KO
        RequestResponse<TransactionDto> transactionResponseFromVitam = new RequestResponse<>() {
            @Override
            public Response toResponse() {
                return null;
            }
        };

        RequestResponse<JsonNode> transactionMockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(transactionResponseFromVitam)).build());

        // WHEN
        when(getorixDepositRepository.findOne((Query) any())).thenReturn(Optional.of(other));
        when(getorixDepositRepository.existsById(getorixDepositId)).thenReturn(true);
        Mockito.when(collectService.updateProject(any(), any()))
            .thenReturn(mockResponse);
        Mockito.when(collectService.updateTransaction(any(), any()))
            .thenReturn(transactionMockResponse);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        // THEN
        assertThatCode(()-> getorixDepositInternalService.updateGetorixDepositDetails(getorixDepositId, getorixDepositDto, vitamContext))
            .isInstanceOf(InternalServerException.class)
            .hasMessage("Unable to update transaction");
    }
    @Test
    void testUpdateGetorixDeposit_KO_when_Object_null() {
        VitamContext vitamContext = new VitamContext(1);
        assertThatCode(()-> getorixDepositInternalService.updateGetorixDepositDetails("getorixDepositId",null,
            vitamContext))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("The getorixDeposit should not be null");
    }

    @Test
    void testUpdateGetorixDeposit_KO_when_GetorixId_null() {
        VitamContext vitamContext = new VitamContext(1);
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        assertThatCode(()-> getorixDepositInternalService.updateGetorixDepositDetails(null, getorixDepositDto, vitamContext))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("The getorixDepositId should not be null");

    }

    @Test
    void testUpdateGetorixDeposit_KO_when_GetorixId_is_not_the_same_as_the_dto_object() {

        VitamContext vitamContext = new VitamContext(1);
        String getorixDepositId = "getorixDepositId";
        String getorixDepositSecondId = "getorixDepositSecondId";
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());
        getorixDepositDto.setId(getorixDepositSecondId);
        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.findOne((Query) any())).thenReturn(Optional.of(other));

        assertThatCode(()-> getorixDepositInternalService.updateGetorixDepositDetails(getorixDepositId, getorixDepositDto, vitamContext))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("The getorixDepositId should be correct");
    }

    @Test
    void testUpdateGetorixDeposit_ko_when_user_not_connected() {

        VitamContext vitamContext = new VitamContext(1);
        String getorixDepositId = "getorixDepositId";
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());

        getorixDepositDto.setId(getorixDepositId);
        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        when(getorixDepositRepository.findOne((Query) any())).thenReturn(Optional.of(other));
        Mockito.when(internalSecurityService.getUser()).thenReturn(null);

        assertThatCode(()-> getorixDepositInternalService.updateGetorixDepositDetails(getorixDepositId, getorixDepositDto, vitamContext))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessage("You are not authorized to update the deposit");
    }

    @Test
    void testUpdateGetorixDepositDetails_OK_when_all_conditions_ok()
        throws JsonProcessingException, VitamClientException {

        // GIVEN
        VitamContext vitamContext = new VitamContext(1);
        String getorixDepositId = "getorixDepositId";
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());
        getorixDepositDto.setId(getorixDepositId);

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        // Update Vitam Collect Project : OK
        final ProjectDto projectDto = factory.manufacturePojo(ProjectDto.class);
        projectDto.setId("projectId");
        RequestResponseOK<ProjectDto> responseFromVitam = new RequestResponseOK<>();
        responseFromVitam.setHttpCode(200);
        responseFromVitam.setHits(1, 1, 1, 1);
        responseFromVitam.addResult(projectDto);

        RequestResponse<JsonNode> mockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(responseFromVitam)).build());

        // Update Vitam Collect Transaction : OK
        final TransactionDto transactionDto = factory.manufacturePojo(TransactionDto.class);
        RequestResponseOK<TransactionDto> transactionResponseFromVitam = new RequestResponseOK<>();
        transactionResponseFromVitam.setHttpCode(200);
        transactionResponseFromVitam.setHits(1, 1, 1, 1);
        transactionResponseFromVitam.addResult(transactionDto);

        RequestResponse<JsonNode> transactionMockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(transactionResponseFromVitam)).build());

        // WHEN
        when(getorixDepositRepository.findOne((Query) any())).thenReturn(Optional.of(other));
        when(getorixDepositRepository.existsById(getorixDepositId)).thenReturn(true);
        Mockito.when(collectService.updateProject(any(), any()))
            .thenReturn(mockResponse);
        Mockito.when(collectService.updateTransaction(any(), any()))
            .thenReturn(transactionMockResponse);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        // THEN
        assertThatCode(()-> getorixDepositInternalService.updateGetorixDepositDetails(getorixDepositId, getorixDepositDto, vitamContext))
            .doesNotThrowAnyException();
    }

    @Test
    void testUpdateGetorixDeposit_ko_when_getorixDeposit_not_found() {

        VitamContext vitamContext = new VitamContext(1);
        String getorixDepositId = "getorixDepositId";
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());
        getorixDepositDto.setId(getorixDepositId);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        when(getorixDepositRepository.existsById(getorixDepositId)).thenReturn(false);

        assertThatCode(()-> getorixDepositInternalService.updateGetorixDepositDetails(getorixDepositId, getorixDepositDto, vitamContext))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Entity not found");
    }

    @Test
    void testUpdateGetorixDepositDetails_KO_when_update_project_KO()
        throws JsonProcessingException, VitamClientException {

        // GIVEN
        VitamContext vitamContext = new VitamContext(1);
        String getorixDepositId = "getorixDepositId";
        final GetorixDepositDto getorixDepositDto =
            getorixDepositConverter.convertEntityToDto(buildGetorixDepositModel());
        getorixDepositDto.setId(getorixDepositId);

        final GetorixDepositModel other = new GetorixDepositModel();
        VitamUIUtils.copyProperties(getorixDepositDto, other);
        other.setId(UUID.randomUUID().toString());

        // Update Vitam Collect Project : KO
        RequestResponse<ProjectDto> responseFromVitam = new RequestResponse<>() {
            @Override
            public Response toResponse() {
                return null;
            }
        };
        RequestResponse<JsonNode> mockResponse = RequestResponse
            .parseFromResponse(Response.ok(objectMapper.writeValueAsString(responseFromVitam)).build());

        // WHEN
        when(getorixDepositRepository.findOne((Query) any())).thenReturn(Optional.of(other));
        when(getorixDepositRepository.existsById(getorixDepositId)).thenReturn(true);
        Mockito.when(collectService.updateProject(any(), any()))
            .thenReturn(mockResponse);

        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        // THEN
        assertThatCode(()-> getorixDepositInternalService.updateGetorixDepositDetails(getorixDepositId, getorixDepositDto, vitamContext))
            .isInstanceOf(InternalServerException.class)
            .hasMessage("Unable to update project");
    }

    @Test
    void fetchTitle_should_return_title_when_title_is_present() {
        final String title = "default_title";
        final String response =  getorixDepositInternalService.fetchTitle(title, null);
        assertThat(response).isNotNull().isEqualTo("default_title");
    }
    @Test
    void fetchTitle_should_return_null_when_title_titleFr_titleEn_not_present() {
        final String result = getorixDepositInternalService.fetchTitle(null, null);
        assertThat(result).isNull();
    }

    @Test
    void fetchTitle_should_return_titleFr_when_titleFr_is_present() {
        final TitleDto titleDto = new TitleDto( );
        titleDto.setFr("titleFr");
        final String response =  getorixDepositInternalService.fetchTitle(null, titleDto);
        assertThat(response).isNotNull().isEqualTo("titleFr");
    }

    @Test
    void fetchTitle_should_return_titleEn_when_titleEn_is_present() {
        final TitleDto titleDto = new TitleDto( );
        titleDto.setEn("titleEn");
        final String response =  getorixDepositInternalService.fetchTitle(null, titleDto);
        assertThat(response).isNotNull().isEqualTo("titleEn");
    }

    @Test
    void getUnitFullPath_shouldThrowExceptionWhenFindArchiveUnitById_ko() throws VitamClientException {
        // GIVEN
        String unitId = "id";
        VitamContext vitamContext = new VitamContext(1);
        when(collectService.findUnitById(unitId, vitamContext))
            .thenThrow(new VitamClientException("EXCEPTION : Archive Unit not found"));
        // THEN
        assertThatCode(() ->getorixDepositInternalService.getUnitFullPath(unitId, vitamContext))
            .isInstanceOf(VitamClientException.class)
            .hasMessage("EXCEPTION : Archive Unit not found");
    }

    @Test
    void getUnitFullPath_shouldPasseWithSuccessWithListOfParentWhenFindArchiveUnitById_OK()
        throws VitamClientException, IOException, InvalidParseOperationException {
        // GIVEN
        String unitId = "id";
        VitamContext vitamContext = new VitamContext(1);

        // WHEN
        when(collectService.findUnitById(unitId, vitamContext))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITH_UNITUPS_LIST));

        when(collectService.findUnitById("aeaqaaaaaahmnykxabnagalrcg7878iaaaabq", vitamContext))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT));

        List<UnitFullPath> unitFullPathList = getorixDepositInternalService.getUnitFullPath(unitId, vitamContext);
        // THEN
        assertThatCode(() ->getorixDepositInternalService.getUnitFullPath(unitId, vitamContext))
            .doesNotThrowAnyException();

        assertThat(unitFullPathList).hasSize(3);
        assertThat(unitFullPathList.get(1).getId()).isEqualTo("aeaqaaaaaahmnykxabnagalrcg7878iaaaabq");
        assertThat(unitFullPathList.get(0).getId()).isEqualTo("ORPHANS_NODE");
        assertThat(unitFullPathList.get(0).getTitle()).isEqualTo("Mes Archives");
        assertThat(unitFullPathList.get(0).getUnitType()).isNull();
        assertThat(unitFullPathList.get(1).getTitle()).isEqualTo("Liste des Métros de Tokyo");
        assertThat(unitFullPathList.get(2).getTitle()).isEqualTo("title title of unit");
    }

    @Test
    void getUnitFullPath_shouldPasseWithSuccessWithoutParentWhenFindArchiveUnitById_OK()
        throws VitamClientException, IOException, InvalidParseOperationException {
        // GIVEN
        String unitId = "id";
        VitamContext vitamContext = new VitamContext(1);
        when(collectService.findUnitById(unitId, vitamContext))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT));

        List<UnitFullPath> unitFullPathList = getorixDepositInternalService.getUnitFullPath(unitId, vitamContext);
        // THEN
        assertThatCode(() ->getorixDepositInternalService.getUnitFullPath(unitId, vitamContext))
            .doesNotThrowAnyException();

        assertThat(unitFullPathList).hasSize(2);
        assertThat(unitFullPathList.get(1).getId()).isEqualTo("aeaqaaaaaahmnykxabnagalrcg7878iaaaabq");
        assertThat(unitFullPathList.get(0).getId()).isEqualTo("ORPHANS_NODE");
        assertThat(unitFullPathList.get(0).getTitle()).isEqualTo("Mes Archives");
        assertThat(unitFullPathList.get(0).getUnitType()).isNull();
        assertThat(unitFullPathList.get(1).getTitle()).isEqualTo("Liste des Métros de Tokyo");
    }

    @Test
    void testCreateGetorixDeposit_ko_when_getorixDepositDto_is_null() {

        VitamContext vitamContext = new VitamContext(155);
        final AuthUserDto user = buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        assertThatCode(()-> getorixDepositInternalService.createGetorixDeposit(null, vitamContext))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("The Getorix deposit information are not provided");
    }


    private RequestResponseOK<JsonNode> buildUnitMetadataResponse(String fileName)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = GetorixDepositInternalServiceTest.class.getClassLoader()
            .getResourceAsStream(fileName);
        assertThat(inputStream).isNotNull();
        return RequestResponseOK
            .getFromJsonNode(objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class));
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
        getorixDepositModel.setTenantIdentifier(15);
        getorixDepositModel.setArchiveVolume(1975);
        getorixDepositModel.setPrescriptionOrderNumber("56");
        getorixDepositModel.setOriginatingAgency("originatingAgencyIdentifier");
        getorixDepositModel.setVersatileService("TransferringAgencyIdentifier");
        getorixDepositModel.setProjectId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaabq");

        return getorixDepositModel;
    }
}
