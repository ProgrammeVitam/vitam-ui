/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.ingest.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractCommonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestServiceTest {

    @Mock
    private IngestExternalParametersService ingestExternalParametersService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private AccessContractCommonService accessContractCommonService;

    @Mock
    private IngestAccessContractService ingestAccessContractService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private IngestService ingestService;

    @BeforeEach
    public void beforeEach() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ingestAccessContractService = new IngestAccessContractService(accessContractCommonService, objectMapper);
        VitamContext vitamContext = new VitamContext(1).setAccessContract("ContratTNR");
        doReturn(vitamContext).when(ingestExternalParametersService).buildVitamContextFromExternalParam();
    }

    /**
     * Test for <a href="https://assistance.programmevitam.fr/plugins/tracker/?aid=13172">#13172 bug</a>
     */
    @Test
    void getAllPaginatedWhenEmptyOriginatingAgenciesAndEveryOriginatingAgencyIsFalse() throws VitamClientException {
        final VitamContext vitamContext = new VitamContext(1);
        final String accessContract = "AccessContract42";
        final String criteria = "{\"evTypeProc\":\"INGEST\"}";
        vitamContext.setAccessContract(accessContract);
        when(ingestExternalParametersService.buildVitamContextFromExternalParam()).thenReturn(vitamContext);
        final AccessContractModel accessContractModel = new AccessContractModel();
        accessContractModel.setEveryOriginatingAgency(false);
        accessContractModel.setOriginatingAgencies(Collections.emptySet());

        final LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");
        logbookOperation.setEvents(Collections.emptyList());
        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().addResult(logbookOperation).setHttpCode(200)
        );

        Assertions.assertDoesNotThrow(
            () -> ingestService.getAllPaginated(0, 10, Optional.empty(), Optional.empty(), Optional.of(criteria))
        );
    }
}
