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
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitam.common.model.administration.RuleMeasurementEnum;
import fr.gouv.vitam.common.model.administration.RuleType;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.rest.dto.RuleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VitamRuleServiceTest {

    @Mock
    private AdminExternalClient adminExternalClient;

    @Mock
    private AccessExternalClient accessExternalClient;

    private VitamRuleService vitamRuleService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(VitamRuleServiceTest.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        vitamRuleService = new VitamRuleService(adminExternalClient, objectMapper, accessExternalClient);
    }

    @Test
    public void findRules_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode select = JsonHandler.createObjectNode();

        when(adminExternalClient.findRules(vitamContext, select)).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(200)
        );

        assertThatCode(() -> vitamRuleService.findRules(vitamContext, select)).doesNotThrowAnyException();
    }

    @Test
    public void findRules_should_throw_BadRequestException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode select = JsonHandler.createObjectNode();

        when(adminExternalClient.findRules(vitamContext, select)).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(400)
        );

        assertThatCode(() -> vitamRuleService.findRules(vitamContext, select)).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findRules_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode select = JsonHandler.createObjectNode();

        when(adminExternalClient.findRules(vitamContext, select)).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> vitamRuleService.findRules(vitamContext, select)).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findRuleById_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "id_0";

        when(adminExternalClient.findRuleById(vitamContext, id)).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(200)
        );

        assertThatCode(() -> vitamRuleService.findRuleById(vitamContext, id)).doesNotThrowAnyException();
    }

    @Test
    public void findRuleById_should_throw_BadRequestException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "id_0";

        when(adminExternalClient.findRuleById(vitamContext, id)).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(400)
        );

        assertThatCode(() -> vitamRuleService.findRuleById(vitamContext, id)).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findRuleById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String id = "id_0";

        when(adminExternalClient.findRuleById(vitamContext, id)).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> vitamRuleService.findRuleById(vitamContext, id)).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void export_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");

        when(accessExternalClient.selectOperations(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().addResult(logbookOperation).setHttpCode(200)
        );

        when(adminExternalClient.downloadRulesCsvAsStream(any(VitamContext.class), any(String.class))).thenReturn(
            Response.status(200).build()
        );

        assertThatCode(() -> vitamRuleService.export(vitamContext)).doesNotThrowAnyException();
    }

    @Test
    public void export_should_throw_VitamClientException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");

        when(accessExternalClient.selectOperations(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().setHttpCode(400)
        );

        assertThatCode(() -> vitamRuleService.export(vitamContext)).isInstanceOf(VitamClientException.class);
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

        assertThatCode(() -> vitamRuleService.export(vitamContext)).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void patchRule_should_return_ok_when_findRules_ok()
        throws VitamClientException, InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        FileRulesModel patchRule = new FileRulesModel();

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(200)
        );

        when(
            adminExternalClient.createRules(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<FileRulesModel>().setHttpCode(200));

        assertThatCode(() -> vitamRuleService.patchRule(vitamContext, id, patchRule)).doesNotThrowAnyException();
    }

    @Test
    public void patchRule_should_throw_BadRequestException_when_findRules_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        FileRulesModel patchRule = new FileRulesModel();

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(400)
        );

        assertThatCode(() -> vitamRuleService.patchRule(vitamContext, id, patchRule)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void patchRule_should_throw_VitamClientException_when_vitamclient_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";
        FileRulesModel patchRule = new FileRulesModel();

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> vitamRuleService.patchRule(vitamContext, id, patchRule)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    public void deleteRule_should_return_ok_when_findRules_ok()
        throws VitamClientException, InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(200)
        );

        when(
            adminExternalClient.createRules(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<FileRulesModel>().setHttpCode(200));

        assertThatCode(() -> vitamRuleService.deleteRule(vitamContext, id)).doesNotThrowAnyException();
    }

    @Test
    public void deleteRule_should_throw_BadRequestException_when_findRules_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(400)
        );

        assertThatCode(() -> vitamRuleService.deleteRule(vitamContext, id)).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void deleteRule_should_throw_VitamClientException_when_findRules_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        String id = "id_0";

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception throw by vitam")
        );

        assertThatCode(() -> vitamRuleService.deleteRule(vitamContext, id)).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void create_should_return_ok_when_findRules_ok()
        throws VitamClientException, InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamContext = new VitamContext(1);
        FileRulesModel newRule = new FileRulesModel();
        newRule.setRuleType(RuleType.AppraisalRule);
        newRule.setRuleMeasurement(RuleMeasurementEnum.YEAR);

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(200)
        );

        when(
            adminExternalClient.createRules(any(VitamContext.class), any(InputStream.class), any(String.class))
        ).thenReturn(new RequestResponseOK<FileRulesModel>().setHttpCode(200));

        assertThatCode(() -> vitamRuleService.createRule(vitamContext, newRule)).doesNotThrowAnyException();
    }

    @Test
    public void create_should_throw_BadRequestException_when_findRules_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        FileRulesModel newRule = new FileRulesModel();

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(
            new RequestResponseOK<FileRulesModel>().setHttpCode(400)
        );

        assertThatCode(() -> vitamRuleService.createRule(vitamContext, newRule)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void create_should_throw_VitamClientException_when_findRules_VitamClientException()
        throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        FileRulesModel newRule = new FileRulesModel();

        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenThrow(
            new VitamClientException("Exception thrown by vitam")
        );

        assertThatCode(() -> vitamRuleService.createRule(vitamContext, newRule)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    public void checkRule_should_return_exception_when_vitam_response_is_null() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(1);
        RuleDto ruleDto = new RuleDto();

        assertThatCode(() -> vitamRuleService.checkExistenceOfRuleInVitam(ruleDto, vitamContext)).hasMessage(
            "The body is not found"
        );
        assertThatCode(() -> vitamRuleService.checkExistenceOfRuleInVitam(ruleDto, vitamContext)).isInstanceOf(
            BadRequestException.class
        );
    }

    @Test
    public void checkRule_should_not_throw_ConflictException_when_requested_ruleId_and_ruleType_already_exist_in_vitam()
        throws VitamClientException {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        final FileRulesModel existingRule = new FileRulesModel();
        existingRule.setId("id_0");
        existingRule.setRuleId("APP-00001");
        existingRule.setRuleType(RuleType.AppraisalRule);

        final FileRulesModel secondExistingRule = new FileRulesModel();
        secondExistingRule.setId("id_1");
        secondExistingRule.setRuleId("ACC-00001");
        secondExistingRule.setRuleType(RuleType.AccessRule);

        RuleDto ruleDto = new RuleDto();
        ruleDto.setId("id_0");
        ruleDto.setRuleId("APP-00001");
        ruleDto.setRuleType("AppraisalRule");

        // When
        final RequestResponseOK<FileRulesModel> mockResponse = new RequestResponseOK<>();
        mockResponse.setHttpCode(200);
        mockResponse.addResult(existingRule);
        mockResponse.addResult(secondExistingRule);
        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(mockResponse);

        // Then
        assertThat(mockResponse.getResults()).hasSize(2);
        assertThatCode(
            () -> vitamRuleService.checkExistenceOfRuleInVitam(ruleDto, vitamContext)
        ).doesNotThrowAnyException();
    }

    @Test
    public void checkRule_should_throw_ConflictException_when_requested_rule_does_not_exists_in_vitam()
        throws VitamClientException {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        final FileRulesModel existingRule = new FileRulesModel();
        existingRule.setId("id_0");
        existingRule.setRuleId("APP-00001");
        existingRule.setRuleType(RuleType.AppraisalRule);

        RuleDto ruleDto = new RuleDto();
        ruleDto.setId("id_0");
        ruleDto.setRuleId("APP-00002");
        ruleDto.setRuleType("AppraisalRule");

        // When
        final RequestResponseOK<FileRulesModel> mockResponse = new RequestResponseOK<>();
        mockResponse.setHttpCode(200);
        mockResponse.addResult(existingRule);
        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(mockResponse);

        // Then
        assertThat(mockResponse.getResults()).hasSize(1);
        assertThatCode(() -> vitamRuleService.checkExistenceOfRuleInVitam(ruleDto, vitamContext)).isInstanceOf(
            ConflictException.class
        );
    }

    @Test
    public void checkRule_should_not_throw_ConflictException_when_requested_ruleId_already_exist_in_vitam()
        throws VitamClientException {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        final FileRulesModel existingRule = new FileRulesModel();
        existingRule.setId("id_0");
        existingRule.setRuleId("APP-00001");
        existingRule.setRuleType(RuleType.AppraisalRule);

        final FileRulesModel secondExistingRule = new FileRulesModel();
        secondExistingRule.setId("id_1");
        secondExistingRule.setRuleId("ACC-00001");
        secondExistingRule.setRuleType(RuleType.AccessRule);

        final FileRulesModel storageExistingRule = new FileRulesModel();
        secondExistingRule.setId("id_2");
        secondExistingRule.setRuleId("STO-00001");
        secondExistingRule.setRuleType(RuleType.StorageRule);

        RuleDto ruleDto = new RuleDto();
        ruleDto.setId("id_0");
        ruleDto.setRuleId("APP-00001");

        // When
        final RequestResponseOK<FileRulesModel> mockResponse = new RequestResponseOK<>();
        mockResponse.setHttpCode(200);
        mockResponse.addResult(existingRule);
        mockResponse.addResult(secondExistingRule);
        mockResponse.addResult(storageExistingRule);
        when(adminExternalClient.findRules(any(VitamContext.class), any(JsonNode.class))).thenReturn(mockResponse);

        // Then
        assertThat(mockResponse.getResults()).hasSize(3);
        assertThatCode(
            () -> vitamRuleService.checkExistenceOfRuleInVitam(ruleDto, vitamContext)
        ).doesNotThrowAnyException();
    }

    @Test
    public void checkRule_should_throw_BadRequestException_when_requested_rule_is_null() throws VitamClientException {
        // Given
        VitamContext vitamContext = new VitamContext(1);
        final FileRulesModel existingRule = new FileRulesModel();
        existingRule.setId("id_0");
        existingRule.setRuleId("APP-00001");
        existingRule.setRuleType(RuleType.AppraisalRule);

        final FileRulesModel secondExistingRule = new FileRulesModel();
        secondExistingRule.setId("id_1");
        secondExistingRule.setRuleId("ACC-00001");
        secondExistingRule.setRuleType(RuleType.AccessRule);

        final FileRulesModel storageExistingRule = new FileRulesModel();
        secondExistingRule.setId("id_2");
        secondExistingRule.setRuleId("STO-00001");
        secondExistingRule.setRuleType(RuleType.StorageRule);

        RuleDto ruleDto = new RuleDto();

        // When
        final RequestResponseOK<FileRulesModel> mockResponse = new RequestResponseOK<>();
        mockResponse.setHttpCode(200);
        mockResponse.addResult(existingRule);
        mockResponse.addResult(secondExistingRule);
        mockResponse.addResult(storageExistingRule);

        // Then
        assertThat(mockResponse.getResults()).hasSize(3);
        assertThatCode(() -> vitamRuleService.checkExistenceOfRuleInVitam(ruleDto, vitamContext)).isInstanceOf(
            BadRequestException.class
        );

        assertThatCode(() -> vitamRuleService.checkExistenceOfRuleInVitam(ruleDto, vitamContext)).hasMessage(
            "The body is not found"
        );
    }
}
