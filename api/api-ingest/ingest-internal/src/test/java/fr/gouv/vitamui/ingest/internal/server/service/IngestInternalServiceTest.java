package fr.gouv.vitamui.ingest.internal.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IngestInternalServiceTest {

    @Mock
    private IngestExternalParametersService ingestExternalParametersService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private AccessContractService accessContractService;

    @Mock
    private AccessContractInternalService accessContractInternalService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private IngestInternalService ingestInternalService;

    @BeforeEach
    public void beforeEach() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        accessContractInternalService = new AccessContractInternalService(accessContractService, objectMapper);
    }

    /**
     * Test for <a href="https://assistance.programmevitam.fr/plugins/tracker/?aid=13172">#13172 bug</a>
     */
    @Test
    public void getAllPaginatedWhenEmptyOriginatingAgenciesAndEveryOriginatingAgencyIsFalse()
        throws VitamClientException {
        final VitamContext vitamContext = new VitamContext(1);
        final String accessContract = "AccessContract42";
        final String criteria = "{\"evTypeProc\":\"INGEST\"}";

        when(ingestExternalParametersService.retrieveProfilAccessContract()).thenReturn(Optional.of(accessContract));
        final AccessContractModel accessContractModel = new AccessContractModel();
        accessContractModel.setEveryOriginatingAgency(false);
        accessContractModel.setOriginatingAgencies(Collections.emptySet());

        final LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");
        logbookOperation.setEvents(Collections.emptyList());
        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().addResult(logbookOperation).setHttpCode(200)
        );

        assertDoesNotThrow(
            () ->
                ingestInternalService.getAllPaginated(
                    0,
                    10,
                    Optional.empty(),
                    Optional.empty(),
                    vitamContext,
                    Optional.of(criteria)
                )
        );
    }
}
