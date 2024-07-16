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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ContextModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.dto.ContextVitamDto;
import fr.gouv.vitamui.referential.common.dto.PermissionDto;
import fr.gouv.vitamui.referential.common.dto.converter.ContextDtoConverterUtil;
import fr.gouv.vitamui.referential.common.utils.ReferentialDtoBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VitamContextServiceTest {

    @Mock
    private AdminExternalClient adminExternalClient;

    private ObjectMapper objectMapper;

    private VitamContextService vitamContextService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(VitamContextServiceTest.class);
        objectMapper = new ObjectMapper();
        vitamContextService = new VitamContextService(adminExternalClient, objectMapper);
    }

    @Test
    public void patchContext_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        final String id = "0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(
            adminExternalClient.updateContext(any(VitamContext.class), any(String.class), any(JsonNode.class))
        ).thenReturn(new RequestResponseOK().setHttpCode(200));

        assertThatCode(() -> vitamContextService.patchContext(vitamContext, id, jsonNode)).doesNotThrowAnyException();
    }

    @Test
    public void patchContext_should_return_ok_when_vitamclient_400()
        throws AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        final String id = "0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(
            adminExternalClient.updateContext(any(VitamContext.class), any(String.class), any(JsonNode.class))
        ).thenReturn(new RequestResponseOK().setHttpCode(400));

        assertThatCode(() -> vitamContextService.patchContext(vitamContext, id, jsonNode)).doesNotThrowAnyException();
    }

    @Test
    public void patchContext_should_throw_AccessExternalClientException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        final String id = "0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(
            adminExternalClient.updateContext(any(VitamContext.class), any(String.class), any(JsonNode.class))
        ).thenThrow(new AccessExternalClientException("Exception thrown by Vitam"));

        assertThatCode(() -> vitamContextService.patchContext(vitamContext, id, jsonNode)).isInstanceOf(
            AccessExternalClientException.class
        );
    }

    @Test
    public void patchContext_should_throw_InvalidParseOperationException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        final String id = "0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(
            adminExternalClient.updateContext(any(VitamContext.class), any(String.class), any(JsonNode.class))
        ).thenThrow(new InvalidParseOperationException("Exception thrown by Vitam"));

        assertThatCode(() -> vitamContextService.patchContext(vitamContext, id, jsonNode)).isInstanceOf(
            InvalidParseOperationException.class
        );
    }

    @Test
    public void findContexts_should_return_ok_when_vitamclient_return_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findContexts(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(200)
        );

        assertThatCode(() -> vitamContextService.findContexts(vitamContext, jsonNode)).doesNotThrowAnyException();
    }

    @Test
    public void findContexts_should_throw_BadRequestException_when_vitamclient_return_400()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findContexts(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(400)
        );

        assertThatCode(() -> vitamContextService.findContexts(vitamContext, jsonNode)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void findContexts_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findContexts(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> vitamContextService.findContexts(vitamContext, jsonNode)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    public void findContextById_should_return_ok_when_vitamclient_return_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String contextId = "CId_0";

        when(adminExternalClient.findContextById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(200)
        );

        assertThatCode(() -> vitamContextService.findContextById(vitamContext, contextId)).doesNotThrowAnyException();
    }

    @Test
    public void findContextById_should_throw_InternalServerException_when_vitamclient_return_400()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String contextId = "CId_0";

        when(adminExternalClient.findContextById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(400)
        );

        assertThatCode(() -> vitamContextService.findContextById(vitamContext, contextId)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void findContextById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String contextId = "CId_0";

        when(adminExternalClient.findContextById(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> vitamContextService.findContextById(vitamContext, contextId)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    public void should_convert_contextDto_to_vitamContextDto() {
        ContextDto contextModels = ReferentialDtoBuilder.getContextDto();
        List<ContextVitamDto> contextVitamDtos = ContextDtoConverterUtil.convertContextsToModelOfCreation(
            Collections.singletonList(contextModels)
        );
        final JsonNode jsonNodeContextDto = objectMapper.convertValue(contextModels.getPermissions(), JsonNode.class);
        final JsonNode jsonNodeContextVitamDto = objectMapper.convertValue(
            contextVitamDtos.get(0).getPermissions(),
            JsonNode.class
        );

        assertThat(jsonNodeContextDto).isEqualTo(jsonNodeContextVitamDto);
    }

    @Test
    public void should_convert_contextDtos_to_vitamContextDtos() {
        ContextDto contextModels = ReferentialDtoBuilder.buildContextDto(null);
        Set<PermissionDto> permissionDtos = ReferentialDtoBuilder.buildPermissions();
        contextModels.setPermissions(permissionDtos);

        List<ContextVitamDto> contextVitamDtos = ContextDtoConverterUtil.convertContextsToModelOfCreation(
            Collections.singletonList(contextModels)
        );

        final JsonNode jsonNodeContextDto = objectMapper.convertValue(contextModels.getPermissions(), JsonNode.class);
        final JsonNode jsonNodeContextVitamDto = objectMapper.convertValue(
            contextVitamDtos.get(0).getPermissions(),
            JsonNode.class
        );

        assertThat(jsonNodeContextDto).hasSameElementsAs(jsonNodeContextVitamDto);
    }
}
