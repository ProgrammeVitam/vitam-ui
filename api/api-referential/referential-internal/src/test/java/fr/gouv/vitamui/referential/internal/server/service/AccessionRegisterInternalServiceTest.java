/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package fr.gouv.vitamui.referential.internal.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessionRegisterDetailModel;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.service.AccessionRegisterService;
import fr.gouv.vitamui.referential.internal.server.accessionregister.AccessionRegisterInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AccessionRegisterInternalServiceTest {

    @InjectMocks
    AccessionRegisterInternalService accessionRegisterInternalService;

    @Mock
    private AgencyService agencyService;

    @Mock
    private AdminExternalClient adminExternalClient;

    @Mock
    private AccessionRegisterService accessionRegisterService;

    @Mock
    private Logger log;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        accessionRegisterInternalService = new AccessionRegisterInternalService(
            objectMapper,
            adminExternalClient,
            agencyService,
            accessionRegisterService
        );

        doNothing().when(log).info(any());
    }

    @Test
    void should_call_appropriate_api_once_when_get_paginated_is_invoked()
        throws IOException, InvalidCreateOperationException, InvalidParseOperationException, VitamClientException {
        //Given
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> vitamCriteria = new HashMap<>();
        int pageNumber = 0;
        int size = 20;
        JsonNode query = VitamQueryHelper.createQueryDSL(
            vitamCriteria,
            pageNumber,
            size,
            Optional.empty(),
            Optional.empty()
        );

        doReturn(buildResponseFrom("data/accession-register-details-mocked.json", AccessionRegisterDetailModel.class))
            .when(adminExternalClient)
            .findAccessionRegisterDetails(vitamContext, query);
        doReturn(buildResponseFrom("data/agencies-mocked.json", AgenciesModel.class))
            .when(agencyService)
            .findAgencies(any(VitamContext.class), any(JsonNode.class));

        //When
        accessionRegisterInternalService.getAllPaginated(Optional.empty(), pageNumber, size, null, null, vitamContext);

        //Then
        verify(adminExternalClient, times(1)).findAccessionRegisterDetails(vitamContext, query);
        verify(agencyService, times(1)).findAgencies(any(VitamContext.class), any(JsonNode.class));
    }

    private <T> RequestResponse<T> buildResponseFrom(String filename, Class<T> clazz)
        throws IOException, InvalidParseOperationException {
        InputStream inputStream =
            AccessionRegisterInternalServiceTest.class.getClassLoader().getResourceAsStream(filename);
        assert inputStream != null;
        JsonNode data = objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class);
        return RequestResponseOK.getFromJsonNode(data, clazz);
    }
}
