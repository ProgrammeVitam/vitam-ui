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
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.OntologyModel;
import fr.gouv.vitam.common.model.administration.OntologyType;
import fr.gouv.vitam.common.model.administration.StringSize;
import fr.gouv.vitam.common.model.administration.TypeDetail;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dto.OntologyDto;
import fr.gouv.vitamui.referential.common.service.OntologyService;
import fr.gouv.vitamui.referential.internal.server.ontology.OntologyConverter;
import fr.gouv.vitamui.referential.internal.server.ontology.OntologyInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class OntologyInternalServiceTest {

    @Mock
    private OntologyService ontologyService;

    @Mock
    private LogbookService logbookService;

    @InjectMocks
    private OntologyInternalService ontologyInternalService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OntologyConverter converter = new OntologyConverter();
        ontologyInternalService = new OntologyInternalService(ontologyService, objectMapper, converter, logbookService);
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologyById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        assertThatCode(() -> ontologyInternalService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologyById(any(VitamContext.class), any(String.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(400)
        );

        assertThatCode(() -> ontologyInternalService.getOne(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologyById(any(VitamContext.class), any(String.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> ontologyInternalService.getOne(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        assertThatCode(() -> ontologyInternalService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(400)
        );

        assertThatCode(() -> ontologyInternalService.getAll(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void getAll_should_throw_InternalServerException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> ontologyInternalService.getAll(vitamContext)).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void check_should_return_ok_when_vitamclient_ok() {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();

        when(ontologyService.checkAbilityToCreateOntologyInVitam(any(List.class), any(VitamContext.class))).thenReturn(
            true
        );

        assertThatCode(() -> ontologyInternalService.check(vitamContext, ontologyDto)).doesNotThrowAnyException();
    }

    @Test
    public void check_should_throw_BadRequestException_when_vitamclient_throws_BadRequestException() {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();

        when(ontologyService.checkAbilityToCreateOntologyInVitam(any(List.class), any(VitamContext.class))).thenThrow(
            new BadRequestException("Exception thrown by vitam")
        );

        assertThatCode(() -> ontologyInternalService.check(vitamContext, ontologyDto)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void check_should_throw_UnavailableServiceException_when_vitamclient_throws_UnavailableServiceException() {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();

        when(ontologyService.checkAbilityToCreateOntologyInVitam(any(List.class), any(VitamContext.class))).thenThrow(
            new UnavailableServiceException("Exception thrown by vitam")
        );

        assertThatCode(() -> ontologyInternalService.check(vitamContext, ontologyDto)).isInstanceOf(
            UnavailableServiceException.class
        );
    }

    @Test
    public void check_should_return_ok_when_vitamclient_throws_ConflictException() {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();

        when(ontologyService.checkAbilityToCreateOntologyInVitam(any(List.class), any(VitamContext.class))).thenThrow(
            new ConflictException("Exception thrown by vitam")
        );

        assertThatCode(() -> ontologyInternalService.check(vitamContext, ontologyDto)).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> ontologyInternalService.create(vitamContext, ontologyDto)).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_400_when_vitamclient_return_400()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatThrownBy(() -> ontologyInternalService.create(vitamContext, ontologyDto)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new AccessExternalClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> ontologyInternalService.create(vitamContext, ontologyDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new IOException("Exception thrown by vitam")
        );

        assertThatCode(() -> ontologyInternalService.create(vitamContext, ontologyDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void create_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        OntologyDto ontologyDto = new OntologyDto();
        ontologyDto.setId("1");

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new InvalidParseOperationException("Exception thrown by vitam")
        );

        assertThatCode(() -> ontologyInternalService.create(vitamContext, ontologyDto)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void delete_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_throw_exception_when_vitamclient_400()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(() -> {
            ontologyInternalService.delete(vitamContext, identifier);
        }).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new AccessExternalClientException("Exception throw by vitam")
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new IOException("Exception throw by vitam")
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void delete_should_throw_InternalServerException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new InvalidParseOperationException("Exception throw by vitam")
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void updateOntology_should_return_ok_when_vitamclient_ok()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).doesNotThrowAnyException();
    }

    @Test
    public void updateOntology_should_throw_exception_when_vitamclient_400()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void updateOntology_should_throw_AccessExternalClientException_when_vitamclient_throws_AccessExternalClientException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new AccessExternalClientException("Exception")
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void updateOntology_should_throw_IOException_when_vitamclient_throws_IOException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new IOException("Exception")
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void updateOntology_should_throw_InvalidParseOperationException_when_vitamclient_throws_InvalidParseOperationException()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().setHttpCode(200)
        );

        when(ontologyService.importOntologies(any(VitamContext.class), any(List.class))).thenThrow(
            new InvalidParseOperationException("Exception")
        );

        assertThatCode(() -> ontologyInternalService.delete(vitamContext, identifier)).isInstanceOf(
            InternalServerException.class
        );
    }

    @Test
    public void updateOntology_should_return_ok_when_some_fields_are_modifyed()
        throws AccessExternalClientException, IOException, InvalidParseOperationException, VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        OntologyModel model = new OntologyModel();
        model.setId("1");
        model.setIdentifier("identifier");
        model.setShortName("vocab");
        model.setType(OntologyType.TEXT);
        model.setTypeDetail(TypeDetail.STRING);
        model.setStringSize(StringSize.LARGE);

        OntologyModel model2 = new OntologyModel();
        model2.setId("2");
        model2.setIdentifier("2");
        model2.setShortName("vocbool");
        model2.setType(OntologyType.BOOLEAN);
        model2.setTypeDetail(TypeDetail.BOOLEAN);

        List<OntologyModel> ontologies = new ArrayList<>();
        ontologies.add(model);
        ontologies.add(model2);

        // Mocking behavior for findOntologies
        when(ontologyService.findOntologies(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK<OntologyModel>().addAllResults(ontologies).setHttpCode(200)
        );

        Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "1");
        partialDto.put("identifier", "identifier");
        partialDto.put("shortName", "vocabtext");
        partialDto.put("type", OntologyType.TEXT.toString());
        partialDto.put("typeDetail", TypeDetail.STRING.toString());
        partialDto.put("stringSize", StringSize.SHORT.toString());

        // Capturing argument passed to importOntologies
        ArgumentCaptor<List<OntologyModel>> captor = ArgumentCaptor.forClass(List.class);

        // Mocking behavior for importOntologies
        when(ontologyService.importOntologies(any(VitamContext.class), captor.capture())).thenReturn(null);

        // Calling the method under test
        OntologyDto patchedOntology = ontologyInternalService.patch(vitamContext, partialDto);

        // Assertions
        List<OntologyModel> capturedOntologies = captor.getValue();

        assertThat(capturedOntologies.size()).isEqualTo(2);

        assertThat(capturedOntologies.get(0).getIdentifier()).isEqualTo("identifier");
        assertThat(capturedOntologies.get(0).getShortName()).isEqualTo("vocabtext");
        assertThat(capturedOntologies.get(0).getType()).isEqualTo(OntologyType.TEXT);
        assertThat(capturedOntologies.get(0).getTypeDetail()).isEqualTo(TypeDetail.STRING);
        assertThat(capturedOntologies.get(0).getStringSize()).isEqualTo(StringSize.SHORT);

        assertThat(capturedOntologies.get(1).getIdentifier()).isEqualTo("2");
        assertThat(capturedOntologies.get(1).getShortName()).isEqualTo("vocbool");
        assertThat(capturedOntologies.get(1).getType()).isEqualTo(OntologyType.BOOLEAN);
        assertThat(capturedOntologies.get(1).getTypeDetail()).isEqualTo(TypeDetail.BOOLEAN);

        assertThat(patchedOntology.getIdentifier()).isEqualTo("identifier");
        assertThat(patchedOntology.getId()).isEqualTo("1");
        assertThat(patchedOntology.getShortName()).isEqualTo("vocabtext");
        assertThat(patchedOntology.getType()).isEqualTo(OntologyType.TEXT);
        assertThat(patchedOntology.getTypeDetail()).isEqualTo(TypeDetail.STRING);
        assertThat(patchedOntology.getStringSize()).isEqualTo(StringSize.SHORT);
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(200)
        );

        assertThatCode(
            () -> ontologyInternalService.findHistoryByIdentifier(vitamContext, identifier)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(400)
        );

        assertThatCode(
            () -> ontologyInternalService.findHistoryByIdentifier(vitamContext, identifier)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_throws_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenThrow(
            new VitamClientException(("Exception throws by vitam"))
        );

        assertThatCode(() -> ontologyInternalService.findHistoryByIdentifier(vitamContext, identifier)).isInstanceOf(
            VitamClientException.class
        );
    }
}
