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
package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.internal.client.AgencyInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.AgencyInternalWebClient;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgencyExternalServiceTest extends ExternalServiceTest {
    @Mock
    private AgencyInternalRestClient agencyInternalRestClient;
    @Mock
    private AgencyInternalWebClient agencyInternalWebClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private AgencyExternalService agencyExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        agencyExternalService = new AgencyExternalService(externalSecurityService, agencyInternalRestClient, agencyInternalWebClient);
    }

    @Test
    public void getAll_should_return_AgencyDtoList_when_agencyInternalRestClient_return_AgencyDtoList() {
        List<AgencyDto> list = new ArrayList<>();
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setTenant(1);
        list.add(agencyDto);

        when(agencyInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);

        assertThatCode(() -> {
            agencyExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_AgencyDto_when_agencyInternalRestClient_return_AgencyDto() {
        AgencyDto agencyDto = new AgencyDto();
        agencyDto.setTenant(1);

        when(agencyInternalRestClient.create(any(InternalHttpContext.class), any(AgencyDto.class)))
            .thenReturn(agencyDto);

        assertThatCode(() -> {
            agencyExternalService.create(new AgencyDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_boolean_when_agencyInternalRestClient_return_boolean() {
        when(agencyInternalRestClient.check(any(InternalHttpContext.class), any(AgencyDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            agencyExternalService.check(new AgencyDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_agencyInternalRestClient_return_ok() {
        when(agencyInternalRestClient.deleteWithResponse(any(InternalHttpContext.class), any(String.class)))
        	.thenReturn(new ResponseEntity<Boolean>(true, HttpStatus.OK));
        String id = "1";

        assertThatCode(() -> {
            agencyExternalService.deleteWithResponse(id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void export_should_return_ok_when_agencyInternalRestClient_return_ok() {
        when(agencyInternalRestClient.export(any(InternalHttpContext.class)))
            .thenReturn(new ResponseEntity<Resource>(HttpStatus.ACCEPTED));

        assertThatCode(() -> {
            agencyExternalService.export();
        }).doesNotThrowAnyException();
    }

    @Test
    public void import_should_return_ok() throws IOException {
	    File file = new File("src/test/resources/data/import_agencies_valid.csv");
	    FileInputStream input = new FileInputStream(file);
	    MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "text/csv", IOUtils.toByteArray(input));

	    String stringReponse = "{\"httpCode\":\"201\"}";
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode jsonResponse = mapper.readTree(stringReponse);

        when(agencyInternalWebClient.importAgencies(any(InternalHttpContext.class), any(String.class), any(MultipartFile.class)))
        	.thenReturn(jsonResponse);

        assertThatCode(() -> {
        	agencyExternalService.importAgencies(file.getName(), multipartFile);
        }).doesNotThrowAnyException();
    }
}
