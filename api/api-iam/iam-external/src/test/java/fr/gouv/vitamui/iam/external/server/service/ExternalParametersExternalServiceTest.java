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
package fr.gouv.vitamui.iam.external.server.service;

import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ParameterDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.client.ExternalParametersInternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExternalParametersExternalServiceTest {

    @Mock
    private ExternalParametersExternalService externalParametersExternalService;

    @Mock
    private final ExternalParametersInternalRestClient externalParametersInternalRestClient= Mockito.mock(ExternalParametersInternalRestClient.class);

    private final ExternalSecurityService externalSecurityService = Mockito.mock(ExternalSecurityService.class);

    private static final String TEST_IDENTIFIER = "identifier";
    private static final String TEST_NAME = "name";
    private static final String TEST_KEY = "key";
    private static final String TEST_VALUE = "value";

    @Before
    public void before() {
        final ExternalParametersDto dto = new ExternalParametersDto();
        dto.setIdentifier(TEST_IDENTIFIER);
        dto.setName(TEST_NAME);

        ParameterDto parameter = new ParameterDto();
        parameter.setKey(TEST_KEY);
		parameter.setValue(TEST_VALUE);
		List<ParameterDto> parameters = new ArrayList<ParameterDto>();
		parameters.add(parameter);
        dto.setParameters(parameters);

    	externalParametersExternalService = new ExternalParametersExternalService(externalParametersInternalRestClient, externalSecurityService);
        Mockito.reset(externalParametersInternalRestClient, externalSecurityService);

    	Mockito.when(externalSecurityService.getHttpContext()).thenReturn(new ExternalHttpContext(null, null, null, null));
    	Mockito.when(externalSecurityService.getUser()).thenReturn(new AuthUserDto());
        Mockito.when(externalParametersInternalRestClient.getMyExternalParameters(Mockito.any())).thenReturn(dto);
    }

    @Test
    public void testOne() {
        ExternalParametersDto dto = externalParametersExternalService.getMyExternalParameters();

        assertNotNull(dto);
        assertEquals(dto.getIdentifier(), TEST_IDENTIFIER);
        assertEquals(dto.getName(), TEST_NAME);

        assertNotNull(dto.getParameters());
        assertEquals(dto.getParameters().size(), 1);
        assertEquals(dto.getParameters().get(0).getKey(), TEST_KEY);
        assertEquals(dto.getParameters().get(0).getValue(), TEST_VALUE);
    }
}

