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

import fr.gouv.vitamui.commons.api.domain.AccessContractDto;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.internal.client.AccessContractInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.AccessContractInternalWebClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessContractExternalServiceTest extends ExternalServiceTest {

    @Mock
    private AccessContractInternalRestClient accessContractInternalRestClient;

    @Mock
    private ExternalSecurityService externalSecurityService;

    @Mock
    private AccessContractInternalWebClient accessContractInternalWebClient;

    private AccessContractExternalService accessContractExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        accessContractExternalService = new AccessContractExternalService(
            externalSecurityService,
            accessContractInternalRestClient,
            accessContractInternalWebClient
        );
    }

    @Test
    public void getAll_should_return_AccessContractDtoList_when_accessContractInternalRestClient_return_AccessContractDtoList() {
        List<AccessContractDto> list = new ArrayList<>();
        AccessContractDto accessContractDto = new AccessContractDto();
        accessContractDto.setDescription("description");
        list.add(accessContractDto);

        when(accessContractInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class))).thenReturn(
            new ArrayList<AccessContractDto>()
        );

        assertThatCode(() -> {
            accessContractExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_AccessContractDto_when_accessContractInternalRestClient_return_AccessContractDtoList() {
        AccessContractDto accessContractDto = new AccessContractDto();

        when(
            accessContractInternalRestClient.create(any(InternalHttpContext.class), any(AccessContractDto.class))
        ).thenReturn(accessContractDto);

        assertThatCode(() -> {
            accessContractExternalService.create(new AccessContractDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_boolean_when_accessContractInternalRestClient_return_boolean() {
        when(
            accessContractInternalRestClient.check(any(InternalHttpContext.class), any(AccessContractDto.class))
        ).thenReturn(true);

        assertThatCode(() -> {
            accessContractExternalService.check(new AccessContractDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void import_should_return_ok() throws IOException {
        String fileName = "import_access_contracts_valid.csv";
        MultipartFile multipartFile = new MockMultipartFile(
            fileName,
            fileName,
            "text/csv",
            getClass().getResourceAsStream("/data/" + fileName)
        );

        when(
            accessContractInternalWebClient.importAccessContracts(
                any(InternalHttpContext.class),
                any(MultipartFile.class)
            )
        ).thenReturn(new ResponseEntity<Void>(HttpStatus.CREATED));

        assertThatCode(() -> {
            accessContractExternalService.importAccessContracts(multipartFile);
        }).doesNotThrowAnyException();
    }
}
