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

package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class VitamAgencyServiceTest {

    @Mock
    private AdminExternalClient adminExternalClient;

    @Mock
    private AccessExternalClient accessExternalClient;

    @Mock
    private AgencyService agencyService;

    @InjectMocks
    private VitamAgencyService vitamAgencyService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        vitamAgencyService = new VitamAgencyService(
            adminExternalClient,
            agencyService,
            objectMapper,
            accessExternalClient
        );
    }

    @Test
    public void export_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");

        when(accessExternalClient.selectOperations(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().addResult(logbookOperation).setHttpCode(200)
        );

        when(adminExternalClient.downloadAgenciesCsvAsStream(any(VitamContext.class), any(String.class))).thenReturn(
            Response.status(200).build()
        );

        assertThatCode(() -> {
            vitamAgencyService.export(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void export_should_throw_VitamClientException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");

        when(accessExternalClient.selectOperations(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(400)
        );

        when(adminExternalClient.downloadAgenciesCsvAsStream(any(VitamContext.class), any(String.class))).thenReturn(
            Response.status(400).build()
        );

        assertThatCode(() -> {
            vitamAgencyService.export(vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void export_should_throw_VitamClientException_when_vitamclient_throw_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");

        when(accessExternalClient.selectOperations(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        when(adminExternalClient.downloadAgenciesCsvAsStream(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            vitamAgencyService.export(vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void patchAgency_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgencyModelDto patchAgency = new AgencyModelDto();

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        assertThatCode(() -> {
            vitamAgencyService.patchAgency(vitamContext, id, patchAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchAgency_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgencyModelDto patchAgency = new AgencyModelDto();

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );

        assertThatCode(() -> {
            vitamAgencyService.patchAgency(vitamContext, id, patchAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patchAgency_should_throw_VitamClientException_when_vitamclient_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgencyModelDto patchAgency = new AgencyModelDto();

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            vitamAgencyService.patchAgency(vitamContext, id, patchAgency);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void deleteAgency_should_return_ok_when_vitamclient_ok()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<>().setHttpCode(200));

        assertThatCode(() -> {
            vitamAgencyService.deleteAgency(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void deleteAgency_should_return_ok_when_vitamclient_400()
        throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<>().setHttpCode(400));

        assertThatCode(() -> {
            vitamAgencyService.deleteAgency(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void deleteAgency_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception throw by vitam")
        );

        assertThatCode(() -> {
            vitamAgencyService.deleteAgency(vitamContext, id);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        AgencyModelDto newAgency = new AgencyModelDto();

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        assertThatCode(() -> {
            vitamAgencyService.create(vitamContext, newAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        AgencyModelDto newAgency = new AgencyModelDto();

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );

        assertThatCode(() -> {
            vitamAgencyService.create(vitamContext, newAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_VitamClientException_when_vitamclient_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        AgencyModelDto newAgency = new AgencyModelDto();

        when(agencyService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            vitamAgencyService.create(vitamContext, newAgency);
        }).isInstanceOf(VitamClientException.class);
    }
}
