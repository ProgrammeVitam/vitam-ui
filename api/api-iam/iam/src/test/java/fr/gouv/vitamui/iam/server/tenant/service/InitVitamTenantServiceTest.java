package fr.gouv.vitamui.iam.server.tenant.service;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.IngestContractDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractCommonService;
import fr.gouv.vitamui.commons.vitam.api.administration.IngestContractCommonService;
import fr.gouv.vitamui.commons.vitam.api.dto.AccessContractResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.IngestContractResponseDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class InitVitamTenantServiceTest {

    @InjectMocks
    private InitVitamTenantService initVitamTenantService;

    @Mock
    private AccessContractCommonService accessContractCommonService;

    @Mock
    private IngestContractCommonService ingestContractCommonService;

    @Mock
    private SecurityService securityService;

    @Mock
    private TenantConverter tenantConverter;

    private final Resource ingestContractHolding = new ClassPathResource("data/tenant/ingest-contract/holding.json");

    private final Resource itemsIngestContract = new ClassPathResource("data/tenant/ingest-contract/items.json");

    private final Resource fullAccessAccessContract = new ClassPathResource(
        "data/tenant/access-contract/full-access.json"
    );

    private final Resource logbookAccessContract = new ClassPathResource("data/tenant/access-contract/logbook.json");

    private final Resource holdingAccessContract = new ClassPathResource("data/tenant/access-contract/holding.json");

    private AccessContractModel fullAccessAccessContractDto;

    private AccessContractModel logbookAccessContractDto;

    private AccessContractModel holdingAccessContractDto;

    private IngestContractDto ingestContractHoldingDto;

    private IngestContractDto itemsIngestContractDto;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws InvalidParseOperationException, IOException {
        initVitamTenantService.setHoldingIngestContract(ingestContractHolding);
        initVitamTenantService.setFullAccessAccessContract(fullAccessAccessContract);
        initVitamTenantService.setLogbookAccessContract(logbookAccessContract);
        initVitamTenantService.setHoldingAccessContract(holdingAccessContract);
        initVitamTenantService.setObjectMapper(objectMapper);
        initVitamTenantService.setMandatory(true);
        fullAccessAccessContractDto = JsonHandler.getFromInputStream(
            fullAccessAccessContract.getInputStream(),
            AccessContractModel.class
        );
        logbookAccessContractDto = JsonHandler.getFromInputStream(
            logbookAccessContract.getInputStream(),
            AccessContractModel.class
        );
        holdingAccessContractDto = JsonHandler.getFromInputStream(
            holdingAccessContract.getInputStream(),
            AccessContractModel.class
        );
        ingestContractHoldingDto = JsonHandler.getFromInputStream(
            ingestContractHolding.getInputStream(),
            IngestContractDto.class
        );
        itemsIngestContractDto = JsonHandler.getFromInputStream(
            itemsIngestContract.getInputStream(),
            IngestContractDto.class
        );

        initVitamTenantService.setContractResources(
            Map.of(
                InitVitamTenantService.HOLDING_ACCESS_CONTRACT_NAME,
                holdingAccessContract,
                InitVitamTenantService.HOLDING_INGEST_CONTRACT_NAME,
                ingestContractHolding,
                InitVitamTenantService.LOGBOOK_ACCESS_CONTRACT_NAME,
                logbookAccessContract,
                InitVitamTenantService.ITEMS_INGEST_CONTRACT_NAME,
                itemsIngestContract,
                InitVitamTenantService.FULL_ACCESS_CONTRACT_NAME,
                fullAccessAccessContract
            )
        );
    }

    @Test
    void initTenantIsNotMandatory() {
        initVitamTenantService.setMandatory(false);
        initVitamTenantService.init(new Tenant(), new ExternalParametersDto());
    }

    @Test
    void initTenantSucceedAsAccessAndIngestContractAlreadyExist()
        throws VitamClientException, InvalidParseOperationException, JacksonException {
        TenantDto tenantDto = new TenantDto();
        tenantDto.setIdentifier(10);

        ExternalParametersDto externalParametersDto = new ExternalParametersDto();
        externalParametersDto.setIdentifier("10");
        externalParametersDto.setName("test");

        RequestResponse<AccessContractModel> requestResponse = Mockito.mock(RequestResponse.class);
        Mockito.when(
            accessContractCommonService.findAccessContracts(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(requestResponse);
        List<AccessContractModel> results = List.of(
            holdingAccessContractDto,
            logbookAccessContractDto,
            fullAccessAccessContractDto
        );
        JsonHandler.toJsonNode(results);
        AccessContractResponseDto response = new AccessContractResponseDto();
        response.setResults(results);
        Mockito.when(
            objectMapper.treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class)
        ).thenReturn(response);

        RequestResponse<IngestContractModel> requestResponseIngest = Mockito.mock(RequestResponse.class);
        Mockito.when(
            ingestContractCommonService.findIngestContracts(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(requestResponseIngest);
        List<IngestContractDto> ingestsContract = List.of(ingestContractHoldingDto, itemsIngestContractDto);
        JsonHandler.toJsonNode(results);
        IngestContractResponseDto responseIngest = new IngestContractResponseDto();
        responseIngest.setResults(ingestsContract);
        Mockito.when(
            objectMapper.treeToValue(requestResponse.toJsonNode(), IngestContractResponseDto.class)
        ).thenReturn(responseIngest);

        initVitamTenantService.init(tenantDto, externalParametersDto);
    }
}
