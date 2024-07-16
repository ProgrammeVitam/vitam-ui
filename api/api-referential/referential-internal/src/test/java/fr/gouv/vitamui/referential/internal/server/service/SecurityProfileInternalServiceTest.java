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
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.SecurityProfileModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.referential.common.service.VitamSecurityProfileService;
import fr.gouv.vitamui.referential.internal.server.securityprofile.SecurityProfileConverter;
import fr.gouv.vitamui.referential.internal.server.securityprofile.SecurityProfileInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SecurityProfileInternalServiceTest {

    @Mock
    private VitamSecurityProfileService vitamSecurityProfileService;

    @Mock
    private LogbookService logbookService;

    @InjectMocks
    private SecurityProfileInternalService securityProfileInternalService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SecurityProfileConverter converter = new SecurityProfileConverter();
        securityProfileInternalService = new SecurityProfileInternalService(
            vitamSecurityProfileService,
            objectMapper,
            converter,
            logbookService
        );
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(
            vitamSecurityProfileService.findSecurityProfileById(any(VitamContext.class), any(String.class))
        ).thenReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(200));

        assertThatCode(() -> {
            securityProfileInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(
            vitamSecurityProfileService.findSecurityProfileById(any(VitamContext.class), any(String.class))
        ).thenReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(400));

        assertThatCode(() -> {
            securityProfileInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(vitamSecurityProfileService.findSecurityProfileById(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            securityProfileInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(
            vitamSecurityProfileService.findSecurityProfiles(any(VitamContext.class), any(ObjectNode.class))
        ).thenReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(200));

        assertThatCode(() -> {
            securityProfileInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(
            vitamSecurityProfileService.findSecurityProfiles(any(VitamContext.class), any(ObjectNode.class))
        ).thenReturn(new RequestResponseOK<SecurityProfileModel>().setHttpCode(400));

        assertThatCode(() -> {
            securityProfileInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(
            vitamSecurityProfileService.findSecurityProfiles(any(VitamContext.class), any(ObjectNode.class))
        ).thenThrow(new VitamClientException("Exception thrown by vitam"));

        assertThatCode(() -> {
            securityProfileInternalService.getAll(vitamContext);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(vitamSecurityProfileService.findSecurityProfiles(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<SecurityProfileModel>().setHttpCode(200)
        );

        assertThatCode(() -> {
            securityProfileInternalService.findAll(vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(vitamSecurityProfileService.findSecurityProfiles(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<SecurityProfileModel>().setHttpCode(400)
        );

        assertThatCode(() -> {
            securityProfileInternalService.findAll(vitamContext, query);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode query = JsonHandler.createObjectNode();

        when(vitamSecurityProfileService.findSecurityProfiles(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            securityProfileInternalService.findAll(vitamContext, query);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        when(
            vitamSecurityProfileService.checkAbilityToCreateSecurityProfileInVitam(
                any(List.class),
                any(VitamContext.class)
            )
        ).thenReturn(true);

        assertThatCode(() -> {
            securityProfileInternalService.check(vitamContext, securityProfileDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        when(
            vitamSecurityProfileService.checkAbilityToCreateSecurityProfileInVitam(
                any(List.class),
                any(VitamContext.class)
            )
        ).thenThrow(new ConflictException("Exception thrown by vitam"));

        assertThatCode(() -> {
            securityProfileInternalService.check(vitamContext, securityProfileDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        when(
            vitamSecurityProfileService.createSecurityProfile(any(VitamContext.class), any(SecurityProfileModel.class))
        ).thenReturn(new RequestResponseOK().setHttpCode(200));

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext, securityProfileDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_400()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        when(
            vitamSecurityProfileService.createSecurityProfile(any(VitamContext.class), any(SecurityProfileModel.class))
        ).thenReturn(new RequestResponseOK().setHttpCode(400));

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext, securityProfileDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        when(
            vitamSecurityProfileService.createSecurityProfile(any(VitamContext.class), any(SecurityProfileModel.class))
        ).thenThrow(new VitamClientException("Exception thrown by vitam"));

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext, securityProfileDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        when(
            vitamSecurityProfileService.createSecurityProfile(any(VitamContext.class), any(SecurityProfileModel.class))
        ).thenThrow(new AccessExternalClientException("Exception thrown by vitam"));

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext, securityProfileDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        when(
            vitamSecurityProfileService.createSecurityProfile(any(VitamContext.class), any(SecurityProfileModel.class))
        ).thenThrow(new IOException("Exception thrown by vitam"));

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext, securityProfileDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidParseOperationException {
        VitamContext vitamContext = new VitamContext(0);
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();

        when(
            vitamSecurityProfileService.createSecurityProfile(any(VitamContext.class), any(SecurityProfileModel.class))
        ).thenThrow(new InvalidParseOperationException("Exception thrown by vitam"));

        assertThatCode(() -> {
            securityProfileInternalService.create(vitamContext, securityProfileDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");

        when(
            vitamSecurityProfileService.patchSecurityProfile(
                any(VitamContext.class),
                any(String.class),
                any(JsonNode.class)
            )
        ).thenReturn(new RequestResponseOK().setHttpCode(200));

        assertThatCode(() -> {
            securityProfileInternalService.patch(vitamContext, partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");

        when(
            vitamSecurityProfileService.patchSecurityProfile(
                any(VitamContext.class),
                any(String.class),
                any(JsonNode.class)
            )
        ).thenReturn(new RequestResponseOK().setHttpCode(400));

        assertThatCode(() -> {
            securityProfileInternalService.patch(vitamContext, partialDto);
        }).doesNotThrowAnyException();
    }

    @Test
    public void patch_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("identifier", "identifier");

        when(
            vitamSecurityProfileService.patchSecurityProfile(
                any(VitamContext.class),
                any(String.class),
                any(JsonNode.class)
            )
        ).thenThrow(new VitamClientException("Exception thrown by vitam"));

        assertThatCode(() -> {
            securityProfileInternalService.patch(vitamContext, partialDto);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(vitamSecurityProfileService.deleteSecurityProfile(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_400()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(vitamSecurityProfileService.deleteSecurityProfile(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(vitamSecurityProfileService.deleteSecurityProfile(any(VitamContext.class), any(String.class))).thenThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(vitamSecurityProfileService.deleteSecurityProfile(any(VitamContext.class), any(String.class))).thenThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(vitamSecurityProfileService.deleteSecurityProfile(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, InvalidParseOperationException, VitamClientException, IOException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(vitamSecurityProfileService.deleteSecurityProfile(any(VitamContext.class), any(String.class))).thenThrow(
            new IOException("Exception thrown by vitam")
        );

        assertThatCode(() -> {
            securityProfileInternalService.delete(vitamContext, id);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(
            logbookService.findEventsByIdentifierAndCollectionNames(
                any(String.class),
                any(String.class),
                any(VitamContext.class)
            )
        ).thenReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(200));

        assertThatCode(() -> {
            securityProfileInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(
            logbookService.findEventsByIdentifierAndCollectionNames(
                any(String.class),
                any(String.class),
                any(VitamContext.class)
            )
        ).thenReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(400));

        assertThatCode(() -> {
            securityProfileInternalService.findHistoryByIdentifier(vitamContext, id);
        }).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "identifier";

        when(
            logbookService.findEventsByIdentifierAndCollectionNames(
                any(String.class),
                any(String.class),
                any(VitamContext.class)
            )
        ).thenThrow(new VitamClientException("Exception thrown by vitam"));

        assertThatCode(() -> {
            securityProfileInternalService.findHistoryByIdentifier(vitamContext, id);
        }).isInstanceOf(VitamClientException.class);
    }
}
