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

package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.ManagementContractDto;
import fr.gouv.vitamui.referential.internal.client.ManagementContractInternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagementContractExternalServiceTest extends ExternalServiceTest {

    @Mock
    private ManagementContractInternalRestClient managementContractInternalRestClient;

    @Mock
    private ExternalSecurityService externalSecurityService;

    private  ManagementContractExternalService managementContractExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        managementContractExternalService = new ManagementContractExternalService(externalSecurityService);
        managementContractExternalService.setManagementContractInternalRestClient(managementContractInternalRestClient);
    }

    @Test
    public void getAll_should_return_ManagementContractDtoList_when_managementContractInternalRestClient_return_ManagementContractDtoList() {
        List<ManagementContractDto> list = new ArrayList<>();
        ManagementContractDto mcd = new ManagementContractDto();
        mcd.setId("id");
        mcd.setTenant(0);
        list.add(mcd);

        when(managementContractInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);
        assertThatCode(() -> {
            managementContractExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_boolean_when_managementContractInternalRestClient_return_boolean() {
        ManagementContractDto mcd = new ManagementContractDto();
        mcd.setId("id");
        mcd.setTenant(0);

        when(managementContractInternalRestClient.check(any(InternalHttpContext.class), any(ManagementContractDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            managementContractExternalService.check(new ManagementContractDto());
        }).doesNotThrowAnyException();
    }
}
