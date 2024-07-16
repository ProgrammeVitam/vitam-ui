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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitam.common.model.administration.RuleMeasurementEnum;
import fr.gouv.vitam.common.model.administration.RuleType;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.UnavailableServiceException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.rest.dto.RuleDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.RuleNodeResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.RuleCSVDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class VitamRuleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VitamRuleService.class);

    private final AdminExternalClient adminExternalClient;

    private final AccessExternalClient accessExternalClient;

    private ObjectMapper objectMapper;

    @Autowired
    public VitamRuleService(
        AdminExternalClient adminExternalClient,
        ObjectMapper objectMapper,
        AccessExternalClient accessExternalClient
    ) {
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
        this.accessExternalClient = accessExternalClient;
    }

    public RequestResponse<FileRulesModel> findRules(final VitamContext vitamContext, final JsonNode select)
        throws VitamClientException {
        final RequestResponse<FileRulesModel> response = adminExternalClient.findRules(vitamContext, select);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    public RequestResponse<FileRulesModel> findRuleById(final VitamContext vitamContext, final String ruleId)
        throws VitamClientException {
        final RequestResponse<FileRulesModel> response = adminExternalClient.findRuleById(vitamContext, ruleId);
        VitamRestUtils.checkResponse(response);
        return response;
    }

    /**
     * Patch only fields that can be updated
     *
     * @param ruleToPatch
     * @param fieldsToApply
     */
    private void patchFields(FileRulesModel ruleToPatch, FileRulesModel fieldsToApply) {
        LOGGER.debug("Patching rule {} with fields {}", ruleToPatch, fieldsToApply);
        final RuleType ruleType = fieldsToApply.getRuleType();
        if (ruleType != null) {
            ruleToPatch.setRuleType(ruleType);
        }

        final String ruleValue = fieldsToApply.getRuleValue();
        if (ruleValue != null) {
            ruleToPatch.setRuleValue(ruleValue);
        }

        final String ruleDuration = fieldsToApply.getRuleDuration();
        if (ruleDuration != null) {
            ruleToPatch.setRuleDuration(ruleDuration);
        }

        final RuleMeasurementEnum ruleMeasurement = fieldsToApply.getRuleMeasurement();
        if (ruleMeasurement != null) {
            ruleToPatch.setRuleMeasurement(ruleMeasurement);
        }

        final String ruleDescription = fieldsToApply.getRuleDescription();
        if (ruleDescription != null) {
            ruleToPatch.setRuleDescription(ruleDescription);
        }
    }

    public boolean patchRule(final VitamContext vitamContext, final String ruleId, FileRulesModel patchRule)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {
        RequestResponse<FileRulesModel> requestResponse = findRules(vitamContext, new Select().getFinalSelect());
        final List<FileRulesModel> actualRules = objectMapper
            .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class)
            .getResults();

        LOGGER.debug("inputPatchRule {}", patchRule);

        LOGGER.debug("Actual rules before patching : {}", actualRules);
        actualRules
            .stream()
            .filter(rule -> ruleId.equals(rule.getRuleId()))
            .forEach(rule -> {
                LOGGER.debug("Rule before patching {}", rule);
                this.patchFields(rule, patchRule);
                LOGGER.debug("Rule after patching {}", rule);
            });

        LOGGER.debug("Actual rules after patching : {}", actualRules);

        RequestResponse response = importRules(vitamContext, actualRules);
        // Check the import response. The response doesn't contain the patch rule
        return checkImportRulesResponse(response);
    }

    public boolean deleteRule(final VitamContext vitamContext, final String ruleId)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {
        RequestResponse<FileRulesModel> requestResponse = findRules(vitamContext, new Select().getFinalSelect());
        final List<FileRulesModel> actualRules = objectMapper
            .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class)
            .getResults();

        List<FileRulesModel> newRulesList = actualRules
            .stream()
            .filter(rule -> !ruleId.equals(rule.getRuleId()))
            .collect(Collectors.toList());

        RequestResponse response = importRules(vitamContext, newRulesList);
        return checkImportRulesResponse(response);
    }

    public boolean createRule(final VitamContext vitamContext, FileRulesModel newRule)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException, JAXBException {
        RequestResponse<FileRulesModel> requestResponse = findRules(vitamContext, new Select().getFinalSelect());
        final List<FileRulesModel> actualRules = objectMapper
            .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class)
            .getResults();

        LOGGER.debug("Before Add List: {}", actualRules);

        actualRules.add(newRule);

        LOGGER.debug("After Add List: {}", actualRules);

        RequestResponse response = importRules(vitamContext, actualRules);
        // Check the import response. The response doesn't contain the new rule
        return checkImportRulesResponse(response);
    }

    private RequestResponse importRules(final VitamContext vitamContext, final List<FileRulesModel> rulesModels)
        throws InvalidParseOperationException, AccessExternalClientException, IOException, JAXBException {
        try (
            ByteArrayInputStream byteArrayInputStream = serializeRules(rulesModels);
            ByteArrayInputStream debugStream = serializeRules(rulesModels)
        ) {
            return adminExternalClient.createRules(vitamContext, byteArrayInputStream, "Rules.csv");
        }
    }

    private ByteArrayInputStream serializeRules(final List<FileRulesModel> ruleDtos) throws IOException {
        final List<RuleCSVDto> listOfRules = convertDtosToCsvDtos(ruleDtos);
        LOGGER.debug("The json for creation rules, sent to Vitam {}", listOfRules);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            final CsvMapper csvMapper = new CsvMapper();
            final CsvSchema schema = csvMapper.schemaFor(RuleCSVDto.class).withColumnSeparator(',').withHeader();

            final ObjectWriter writer = csvMapper.writer(schema);

            writer.writeValue(byteArrayOutputStream, listOfRules);

            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    private RuleCSVDto convertDtoToCsvDto(FileRulesModel rule) {
        RuleCSVDto csvDto = new RuleCSVDto();
        csvDto.setRuleId(rule.getRuleId());
        if (rule.getRuleType() != null) {
            csvDto.setRuleType(rule.getRuleType().name());
        }
        csvDto.setRuleValue(rule.getRuleValue());
        csvDto.setRuleDescription(rule.getRuleDescription());
        csvDto.setRuleDuration(rule.getRuleDuration());
        if (rule.getRuleMeasurement() != null) {
            csvDto.setRuleMeasurement(rule.getRuleMeasurement().getType());
        }
        return csvDto;
    }

    private List<RuleCSVDto> convertDtosToCsvDtos(List<FileRulesModel> rules) {
        return rules.stream().map(this::convertDtoToCsvDto).collect(Collectors.toList());
    }

    /**
     * check if all conditions to check if a rule exists or not in VITAM
     *
     * @param ruleDto
     * @return true if the rule exists in VITAM
     * @throws BadRequestException when the requested rule is null
     * @throws ConflictException when the requested rule does not exist in VITAM
     * @throws JsonProcessingException
     * @throws UnavailableServiceException
     * @throws PreconditionFailedException when we are not authorized to make the check
     */
    public boolean checkExistenceOfRuleInVitam(final RuleDto ruleDto, VitamContext vitamContext) {
        if (ruleDto != null && ruleDto.getRuleId() != null) {
            try {
                // check if tenant exist in Vitam
                final JsonNode select = new Select().getFinalSelect();
                final RequestResponse<FileRulesModel> response = findRules(vitamContext, select);
                if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    LOGGER.error("Can't create rule for the tenant : UNAUTHORIZED");
                    throw new PreconditionFailedException("Can't create rule for the tenant : UNAUTHORIZED");
                } else if (response.getStatus() != HttpStatus.OK.value()) {
                    LOGGER.error("Can't create rule for this tenant, Vitam response code : " + response.getStatus());
                    throw new UnavailableServiceException(
                        "Can't create rule for this tenant, Vitam response code : " + response.getStatus()
                    );
                }

                verifyRuleExistence(ruleDto, response);
            } catch (final VitamClientException exception) {
                LOGGER.error("Can't create rules for this tenant, error while calling Vitam : " + exception);
                throw new UnavailableServiceException(
                    "Can't create rules for this tenant, error while calling Vitam : " + exception
                );
            }
            return true;
        }
        throw new BadRequestException("The body is not found");
    }

    /**
     * Check the existence of a rule in Vitam.
     *
     * @param checkRule the ruleDto to be tested
     * @param existingRules the list of existing rules in Vitam
     * @throws JsonProcessingException
     * @throws ConflictException when the rule does not exists in VITAM
     */
    private void verifyRuleExistence(final RuleDto checkRule, final RequestResponse<FileRulesModel> existingRules) {
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final RuleNodeResponseDto existingRulesDto = objectMapper.treeToValue(
                existingRules.toJsonNode(),
                RuleNodeResponseDto.class
            );
            if (checkRule.getRuleType() == null) {
                if (
                    existingRulesDto
                        .getResults()
                        .stream()
                        .noneMatch(existingRule -> existingRule.getRuleId().equals(checkRule.getRuleId()))
                ) {
                    LOGGER.error(
                        "Can't find the requested rule with id {} and category {}, this rule does not exist in VITAM",
                        checkRule.getRuleId(),
                        checkRule.getRuleType()
                    );
                    throw new ConflictException(
                        "Can't find the requested rule  with id and category, this rule does not exist in VITAM"
                    );
                }
            } else {
                if (
                    existingRulesDto
                        .getResults()
                        .stream()
                        .noneMatch(
                            existingRule ->
                                existingRule.getRuleId().equals(checkRule.getRuleId()) &&
                                existingRule.getRuleType().name().equals(checkRule.getRuleType())
                        )
                ) {
                    LOGGER.error("Can't find the requested rule with identifier, this rule does not exist in VITAM");
                    throw new ConflictException(
                        String.format(
                            "Can't find the requested rule with identifier %s, this rule does not exist in VITAM",
                            checkRule.getRuleId()
                        ),
                        checkRule.getRuleId()
                    );
                }
            }
        } catch (final JsonProcessingException exception) {
            LOGGER.error("Can't find the requested rule, Error while parsing Vitam response : ", exception);
            throw new UnexpectedDataException("Can't create rule, Error while parsing Vitam response : " + exception);
        }
    }

    public Response export(VitamContext context)
        throws InvalidParseOperationException, InvalidCreateOperationException, VitamClientException {
        JsonNode query = VitamQueryHelper.getLastOperationQuery(VitamQueryHelper.RULE_IMPORT_OPERATION_TYPE);
        RequestResponse<LogbookOperation> lastImportOperationResponse = accessExternalClient.selectOperations(
            context,
            query
        );
        LogbookOperationsResponseDto lastImportOperation = VitamRestUtils.responseMapping(
            lastImportOperationResponse.toJsonNode(),
            LogbookOperationsResponseDto.class
        );

        if (lastImportOperation.getHits().getTotal() == 0) {
            throw new VitamClientException("Can't get a result while selecting last rule import");
        }

        return adminExternalClient.downloadRulesCsvAsStream(context, lastImportOperation.getResults().get(0).getEvId());
    }

    public RequestResponse<?> importRules(VitamContext vitamContext, String fileName, MultipartFile file)
        throws InvalidParseOperationException, AccessExternalClientException, VitamClientException, IOException {
        LOGGER.debug("Import rule file {}", fileName);
        return this.adminExternalClient.createRules(vitamContext, file.getInputStream(), fileName);
    }

    /**
     * Check if a rule import has failed or not
     *
     * @param response: the response to check
     * @return true if the import successed , false otherwise
     */
    private boolean checkImportRulesResponse(RequestResponse response) {
        // Check the Vitam response (if status == 200, the rule has been deleted else if status == BAD_REQUEST, Vitam has not delete the rule else, a technical  exception occured)
        VitamRestUtils.checkResponse(
            response,
            HttpStatus.OK.value(),
            HttpStatus.CREATED.value(),
            HttpStatus.ACCEPTED.value(),
            HttpStatus.BAD_REQUEST.value()
        );
        return (
            response.getStatus() == HttpStatus.OK.value() ||
            response.getStatus() == HttpStatus.CREATED.value() ||
            response.getStatus() == HttpStatus.ACCEPTED.value()
        );
    }
}
