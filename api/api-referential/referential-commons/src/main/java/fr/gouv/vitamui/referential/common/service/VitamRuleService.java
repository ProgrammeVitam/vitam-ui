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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.*;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.RuleNodeResponseDto;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VitamRuleService {

	private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamRuleService.class);

    private final AdminExternalClient adminExternalClient;

    private final AccessExternalClient accessExternalClient;

    private ObjectMapper objectMapper;

    @Autowired
    public VitamRuleService(AdminExternalClient adminExternalClient, ObjectMapper objectMapper, AccessExternalClient accessExternalClient) {
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
        this.accessExternalClient = accessExternalClient;
    }

    public RequestResponse<FileRulesModel> findRules(final VitamContext vitamContext, final JsonNode select) throws VitamClientException {
		final RequestResponse<FileRulesModel> response = adminExternalClient.findRules(vitamContext, select);
		VitamRestUtils.checkResponse(response);
		return response;
    }

    public RequestResponse<FileRulesModel> findRuleById(final VitamContext vitamContext, final String ruleId) throws VitamClientException {
		final RequestResponse<FileRulesModel> response = adminExternalClient.findRuleById(vitamContext, ruleId);
		VitamRestUtils.checkResponse(response);
		return response;
    }

    /**
     * Patch only fields that can be updated
     * @param ruleToPatch
     * @param fieldsToApply
     */
    private void patchFields(FileRulesModel ruleToPatch, FileRulesModel fieldsToApply) {
        LOGGER.debug("Patching rule {} with fields {}", ruleToPatch, fieldsToApply);
        final String ruleType = fieldsToApply.getRuleType();
        if (ruleType != null) {
            ruleToPatch.setRuleType(ruleType);
        }

        final String ruleDuration = fieldsToApply.getRuleDuration();
        if (ruleDuration != null) {
            ruleToPatch.setRuleDuration(ruleDuration);
        }

        final String ruleMeasurement = fieldsToApply.getRuleMeasurement();
        if (ruleMeasurement != null) {
            ruleToPatch.setRuleMeasurement(ruleMeasurement);
        }

        final String ruleDescription = fieldsToApply.getRuleDescription();
        if (ruleDescription != null) {
            ruleToPatch.setRuleDescription(ruleDescription);
        }
    }

    public RequestResponse<?> patchRule(final VitamContext vitamContext, final String ruleId, FileRulesModel patchRule)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {

        RequestResponse<FileRulesModel> requestResponse = findRules(vitamContext, new Select().getFinalSelect());
        final List<FileRulesModel> actualRules = objectMapper
            .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class).getResults();

        LOGGER.debug("inputPatchRule {}", patchRule);

        LOGGER.debug("Actual rules before patching : {}", actualRules);
        actualRules.stream()
            .filter( rule -> ruleId.equals(rule.getRuleId()) )
            .forEach( rule -> {
                LOGGER.debug("Rule before patching {}", rule);
                this.patchFields(rule, patchRule);
                LOGGER.debug("Rule after patching {}", rule);
            }  );

        LOGGER.debug("Actual rules after patching : {}", actualRules);

        return importRules(vitamContext, actualRules);
    }

    public RequestResponse<?> deleteRule(final VitamContext vitamContext, final String ruleId)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {

        RequestResponse<FileRulesModel> requestResponse = findRules(vitamContext, new Select().getFinalSelect());
        final List<FileRulesModel> actualRules = objectMapper
            .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class).getResults();

        return importRules(vitamContext, actualRules.stream()
            .filter( rule -> !ruleId.equals(rule.getRuleId()) )
            .collect(Collectors.toList()));
    }

    public RequestResponse<?> createRule(final VitamContext vitamContext, FileRulesModel newRule)
            throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {

        RequestResponse<FileRulesModel> requestResponse = findRules(vitamContext, new Select().getFinalSelect());
        final List<FileRulesModel> actualRules = objectMapper
                .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class).getResults();

        LOGGER.debug("Before Add List: {}", actualRules);

        actualRules.add(newRule);

        LOGGER.debug("After Add List: {}", actualRules);

        return importRules(vitamContext, actualRules);
    }

    private RequestResponse importRules(final VitamContext vitamContext, final List<FileRulesModel> rulesModels)
        throws InvalidParseOperationException, AccessExternalClientException, IOException, JAXBException {
        try (ByteArrayInputStream byteArrayInputStream = serializeRules(rulesModels)) {
            return adminExternalClient.createRules(vitamContext, byteArrayInputStream, "Rules.csv");
        }
    }

    private ByteArrayInputStream serializeRules(final List<FileRulesModel> ruleDtos) throws IOException {
        final List<RuleCSVDto> listOfRules = convertDtosToCsvDtos(ruleDtos);
        LOGGER.debug("The json for creation rules, sent to Vitam {}", listOfRules);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            final CsvMapper csvMapper = new CsvMapper();
            final CsvSchema schema = csvMapper.schemaFor(RuleCSVDto.class)
                .withColumnSeparator(',').withHeader();

            final ObjectWriter writer = csvMapper.writer(schema);

            writer.writeValue(byteArrayOutputStream, listOfRules);

            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    private RuleCSVDto convertDtoToCsvDto(FileRulesModel rule) {
        RuleCSVDto csvDto = new RuleCSVDto();
        csvDto.setRuleId(rule.getRuleId());
        csvDto.setRuleType(rule.getRuleType());
        csvDto.setRuleValue(rule.getRuleValue());
        csvDto.setRuleDescription(rule.getRuleDescription());
        csvDto.setRuleDuration(rule.getRuleDuration());
        csvDto.setRuleMeasurement(rule.getRuleMeasurement());
        return csvDto;
    }

    private List<RuleCSVDto> convertDtosToCsvDtos(List<FileRulesModel> rules) {
        return rules.stream().map(this::convertDtoToCsvDto).collect(Collectors.toList());
    }

    /**
     * check if all conditions are Ok to create a new rule in the tenant
     * @param rules
     * @return true if the rule can be created, false if the rule already exists
     */
    public boolean checkAbilityToCreateRuleInVitam(final List<FileRulesModel> rules, VitamContext vitamContext) {

        if (rules != null && !rules.isEmpty()) {
            try {
                // check if tenant exist in Vitam
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<FileRulesModel> response = findRules(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new PreconditionFailedException("Can't create rule for the tenant : UNAUTHORIZED");
                }
                else if (response.getStatus() != HttpStatus.OK.value()) {
                    throw new UnavailableServiceException("Can't create rule for this tenant, Vitam response code : " + response.getStatus());
                }

                verifyRuleExistence(rules, response);
            }
            catch (final VitamClientException e) {
                throw new UnavailableServiceException("Can't create rules for this tenant, error while calling Vitam : " + e.getMessage());
            }
            return true;
        }
        throw new BadRequestException("The body is not found");
    }

    /**
     * Check if rule is not already created in Vitam.
     * @param checkRules the list of rules being tested
     * @param existingRules the list of existing rules in Vitam
     */
    private void verifyRuleExistence(final List<FileRulesModel> checkRules, final RequestResponse<FileRulesModel> existingRules) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final RuleNodeResponseDto existingRulesDto = objectMapper.treeToValue(existingRules.toJsonNode(), RuleNodeResponseDto.class);
            final List<String> checkRulesIds = checkRules.stream().map(checkRule -> checkRule.getRuleId()).collect(Collectors.toList());

            // For each existing rule, test if its id matches any of the checked rules ids.
            if (existingRulesDto.getResults().stream().anyMatch(existingRule -> checkRulesIds.contains(existingRule.getRuleId()))) {
                throw new ConflictException("Can't create rule, a rule with the same identifier already exist in Vitam");
            }
        }
        catch (final JsonProcessingException e) {
            throw new UnexpectedDataException("Can't create rule, Error while parsing Vitam response : " + e.getMessage());
        }
    }

    public Response export(VitamContext context) throws InvalidParseOperationException, InvalidCreateOperationException, VitamClientException {
        JsonNode query = VitamQueryHelper.getLastOperationQuery(VitamQueryHelper.RULE_IMPORT_OPERATION_TYPE);
        RequestResponse<LogbookOperation> lastImportOperationResponse = accessExternalClient.selectOperations(context, query);
        LogbookOperationsResponseDto lastImportOperation = VitamRestUtils.responseMapping(lastImportOperationResponse.toJsonNode(), LogbookOperationsResponseDto.class);

        if (lastImportOperation.getHits().getTotal() == 0) {
            throw new VitamClientException("Can't get a result while selecting last rule import");
        }

        return adminExternalClient.downloadRulesCsvAsStream(context, lastImportOperation.getResults().get(0).getEvId());
    }

}
