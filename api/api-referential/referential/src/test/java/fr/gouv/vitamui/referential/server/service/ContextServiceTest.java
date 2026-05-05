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
package fr.gouv.vitamui.referential.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ContextModel;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.service.VitamContextCommonService;
import fr.gouv.vitamui.referential.server.service.context.ContextConverter;
import fr.gouv.vitamui.referential.server.service.context.ContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContextServiceTest {

    @Mock
    private VitamContextCommonService vitamContextCommonService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ContextService contextService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ContextConverter converter = new ContextConverter();
        contextService = new ContextService(
            vitamContextCommonService,
            objectMapper,
            converter,
            logbookService,
            securityService
        );
    }

    @Test
    void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(vitamContextCommonService.findContextById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(200)
        );

        assertThatCode(() -> contextService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(vitamContextCommonService.findContextById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(400)
        );

        assertThatCode(() -> contextService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    void getOne_should_throw_InternalServerException_when_vitamclient_throw_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(vitamContextCommonService.findContextById(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> contextService.getOne(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(vitamContextCommonService.findContexts(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(200)
        );

        assertThatCode(() -> contextService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(vitamContextCommonService.findContexts(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(400)
        );

        assertThatCode(() -> contextService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    void getAll_should_throw_InternalServerException_when_vitamclient_throw_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(vitamContextCommonService.findContexts(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> contextService.getAll(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(vitamContextCommonService.findContexts(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(200)
        );

        assertThatCode(() -> contextService.findAll(vitamContext, query)).doesNotThrowAnyException();
    }

    @Test
    void findAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(vitamContextCommonService.findContexts(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<ContextModel>().setHttpCode(400)
        );

        assertThatCode(() -> contextService.findAll(vitamContext, query)).doesNotThrowAnyException();
    }

    @Test
    void findAll_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(vitamContextCommonService.findContexts(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception throw by vitam")
        );

        assertThatCode(() -> contextService.findAll(vitamContext, query)).isInstanceOf(InternalServerException.class);
    }

    @Test
    void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        when(
            vitamContextCommonService.checkAbilityToCreateContextInVitam(any(List.class), any(VitamContext.class))
        ).thenReturn(true);

        assertThatCode(() -> contextService.check(vitamContext, contextDto)).doesNotThrowAnyException();
    }

    @Test
    void check_should_return_ok_when_vitamclient_400() {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        when(
            vitamContextCommonService.checkAbilityToCreateContextInVitam(any(List.class), any(VitamContext.class))
        ).thenThrow(new UnavailableServiceException("Exception throw by vitam"));

        assertThatCode(() -> contextService.check(vitamContext, contextDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        when(
            vitamContextCommonService.checkAbilityToCreateContextInVitam(any(List.class), any(VitamContext.class))
        ).thenThrow(new ConflictException("Exception throw by vitam"));

        assertThatCode(() -> contextService.check(vitamContext, contextDto)).doesNotThrowAnyException();
    }

    @Test
    void create_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        when(vitamContextCommonService.createContext(any(VitamContext.class), any(ContextDto.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> contextService.create(vitamContext, contextDto)).doesNotThrowAnyException();
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        when(vitamContextCommonService.createContext(any(VitamContext.class), any(ContextDto.class))).thenThrow(
            new AccessExternalClientException(("Exception throw by vitam"))
        );

        assertThatCode(() -> contextService.create(vitamContext, contextDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        when(vitamContextCommonService.createContext(any(VitamContext.class), any(ContextDto.class))).thenThrow(
            new IOException(("Exception throw by vitam"))
        );

        assertThatCode(() -> contextService.create(vitamContext, contextDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto contextDto = new ContextDto();

        when(vitamContextCommonService.createContext(any(VitamContext.class), any(ContextDto.class))).thenThrow(
            new InvalidParseOperationException(("Exception throw by vitam"))
        );

        assertThatCode(() -> contextService.create(vitamContext, contextDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void patch_should_return_ok_when_vitamclient_ok()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto partialDto = new ContextDto();
        partialDto.setIdentifier("identifier");

        when(
            vitamContextCommonService.patchContext(any(VitamContext.class), any(String.class), any(ObjectNode.class))
        ).thenReturn(new RequestResponseOK().setHttpCode(200));

        assertThatCode(() -> contextService.patch(vitamContext, partialDto)).doesNotThrowAnyException();
    }

    @Test
    void patch_should_return_ok_when_vitamclient_400()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto partialDto = new ContextDto();
        partialDto.setIdentifier("identifier");

        when(
            vitamContextCommonService.patchContext(any(VitamContext.class), any(String.class), any(ObjectNode.class))
        ).thenReturn(new RequestResponseOK().setHttpCode(400));

        assertThatCode(() -> contextService.patch(vitamContext, partialDto)).doesNotThrowAnyException();
    }

    @Test
    void patch_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto partialDto = new ContextDto();
        partialDto.setIdentifier("identifier");

        when(
            vitamContextCommonService.patchContext(any(VitamContext.class), any(String.class), any(ObjectNode.class))
        ).thenThrow(new InvalidParseOperationException("Exception throw by vitam"));

        assertThatCode(() -> contextService.patch(vitamContext, partialDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void patch_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(0);
        ContextDto partialDto = new ContextDto();
        partialDto.setIdentifier("identifier");

        when(
            vitamContextCommonService.patchContext(any(VitamContext.class), any(String.class), any(ObjectNode.class))
        ).thenThrow(new AccessExternalClientException("Exception throw by vitam"));

        assertThatCode(() -> contextService.patch(vitamContext, partialDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "id_0";

        when(
            logbookService.findEventsByIdentifierAndCollectionNames(
                any(String.class),
                any(String.class),
                any(VitamContext.class),
                anyList()
            )
        ).thenThrow(new VitamClientException("Exception"));

        assertThatCode(() -> contextService.findHistoryByIdentifier(vitamContext, id)).isInstanceOf(
            VitamClientException.class
        );
    }
}
