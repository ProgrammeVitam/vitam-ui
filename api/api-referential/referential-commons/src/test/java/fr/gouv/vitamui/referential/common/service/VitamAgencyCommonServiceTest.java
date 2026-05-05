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
import com.google.common.io.CharStreams;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyCommonService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VitamAgencyCommonServiceTest {

    @Mock
    private AdminExternalClient adminExternalClient;

    @Mock
    private AccessExternalClient accessExternalClient;

    @Mock
    private AgencyCommonService agencyCommonService;

    @InjectMocks
    private VitamAgencyCommonService vitamAgencyCommonService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        vitamAgencyCommonService = new VitamAgencyCommonService(
            adminExternalClient,
            agencyCommonService,
            objectMapper,
            accessExternalClient
        );
    }

    @Test
    void export_should_return_ok_when_vitamclient_ok() throws VitamClientException {
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
            vitamAgencyCommonService.export(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    void export_should_throw_VitamClientException_when_vitamclient_400() throws VitamClientException {
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
            vitamAgencyCommonService.export(vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    void export_should_throw_VitamClientException_when_vitamclient_throw_VitamClientException()
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
            vitamAgencyCommonService.export(vitamContext);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    void patchAgency_should_return_ok_when_vitamclient_ok() throws Exception {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgenciesModel patchAgency = new AgenciesModel();

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );
        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));

        assertThatCode(() -> {
            vitamAgencyCommonService.patchAgency(vitamContext, id, patchAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    void patchAgency_should_return_400_when_vitamclient_400() throws Exception {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgenciesModel patchAgency = new AgenciesModel();

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );
        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(400));

        assertThatCode(() -> vitamAgencyCommonService.deleteAgency(vitamContext, id)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    void patchAgency_should_throw_VitamClientException_when_vitamclient_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        AgenciesModel patchAgency = new AgenciesModel();

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            vitamAgencyCommonService.patchAgency(vitamContext, id, patchAgency);
        }).isInstanceOf(VitamClientException.class);
    }

    @Test
    void patchAgency_should_return_ok_with_additional_properties() throws Exception {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        AgenciesModel patchAgency = new AgenciesModel();
        patchAgency.setId("agency_01");
        patchAgency.setName("AlexAgency");
        patchAgency.setDescription("AlexAgency super description");
        patchAgency.setPlaces(List.of("Place 1", "Place 2"));
        patchAgency.setLocalStatus("Local status");

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>()
                .setHttpCode(200)
                .addResult(new AgenciesModel().setId(patchAgency.getId()).setName(patchAgency.getName()))
        );
        final StringBuilder actualCsvContent = new StringBuilder();
        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenAnswer(invocation -> {
            final InputStream is = invocation.getArgument(1);
            actualCsvContent.append(CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8)));
            return new RequestResponseOK<>().setHttpCode(200);
        });

        // When
        assertThatCode(() -> {
            vitamAgencyCommonService.patchAgency(vitamContext, patchAgency.getId(), patchAgency);
        }).doesNotThrowAnyException();
        // Then
        verify(adminExternalClient).createAgencies(
            any(VitamContext.class),
            any(InputStream.class),
            eq("Agencies.json")
        );
        String expectedCsvContent = PropertiesUtils.getResourceAsString("agency/agency_with_additional_properties.csv");
        Assertions.assertEquals(expectedCsvContent, actualCsvContent.toString());
    }

    @Test
    void patchAgency_should_return_ok_with_additionnal_properties_and_existing() throws Exception {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        AgenciesModel patchAgency = new AgenciesModel();
        patchAgency.setId("agency_01");
        patchAgency.setName("AlexAgency");
        patchAgency.setDescription("AlexAgency super description");

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>()
                .setHttpCode(200)
                .addAllResults(
                    List.of(
                        new AgenciesModel()
                            .setId(patchAgency.getId())
                            .setName(patchAgency.getName())
                            .setEntityType("entity_type_to_remove")
                            .setCreationDate("2024-12-25T12:34:56.123")
                            .setUpdateDate("2024-12-25T12:34:56.123")
                            .setTenant(4),
                        new AgenciesModel()
                            .setId("agency_15")
                            .setName("agency_15")
                            .setEntityType("entity_type_to_keep")
                            .setCreationDate("2024-12-25T12:34:56.123")
                            .setUpdateDate("2024-12-25T12:34:56.123")
                            .setTenant(4)
                    )
                )
        );
        final StringBuilder actualCsvContent = new StringBuilder();
        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenAnswer(invocation -> {
            final InputStream is = invocation.getArgument(1);
            actualCsvContent.append(CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8)));
            return new RequestResponseOK<>().setHttpCode(200);
        });

        // When
        assertThatCode(() -> {
            vitamAgencyCommonService.patchAgency(vitamContext, patchAgency.getId(), patchAgency);
        }).doesNotThrowAnyException();
        // Then
        verify(adminExternalClient).createAgencies(
            any(VitamContext.class),
            any(InputStream.class),
            eq("Agencies.json")
        );

        String expectedCsvContent = PropertiesUtils.getResourceAsString(
            "agency/agency_with_additional_properties_and_others.csv"
        );
        Assertions.assertEquals(expectedCsvContent, actualCsvContent.toString());
    }

    @Test
    void deleteAgency_should_return_ok_when_vitamclient_ok()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<>().setHttpCode(200));

        assertThatCode(() -> {
            vitamAgencyCommonService.deleteAgency(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    void deleteAgency_should_return_400_when_vitamclient_400()
        throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );

        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(400));

        assertThatCode(() -> vitamAgencyCommonService.deleteAgency(vitamContext, id)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    void deleteAgency_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception throw by vitam")
        );

        assertThatCode(() -> vitamAgencyCommonService.deleteAgency(vitamContext, id)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    void create_should_return_ok_when_vitamclient_ok() throws Exception {
        VitamContext vitamContext = new VitamContext(1);
        AgenciesModel newAgency = new AgenciesModel();

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(200)
        );
        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));

        assertThatCode(() -> {
            vitamAgencyCommonService.create(vitamContext, newAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    void create_should_return_ok_when_vitamclient_400() throws Exception {
        VitamContext vitamContext = new VitamContext(1);
        AgenciesModel newAgency = new AgenciesModel();

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<AgenciesModel>().setHttpCode(400)
        );
        when(
            adminExternalClient.createAgencies(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<AgenciesModel>().setHttpCode(200));

        assertThatCode(() -> {
            vitamAgencyCommonService.create(vitamContext, newAgency);
        }).doesNotThrowAnyException();
    }

    @Test
    void create_should_throw_VitamClientException_when_vitamclient_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        AgenciesModel newAgency = new AgenciesModel();

        when(agencyCommonService.findAgencies(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            vitamAgencyCommonService.create(vitamContext, newAgency);
        }).isInstanceOf(VitamClientException.class);
    }
}
