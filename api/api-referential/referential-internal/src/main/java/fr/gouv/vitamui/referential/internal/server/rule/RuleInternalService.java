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
package fr.gouv.vitamui.referential.internal.server.rule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import fr.gouv.vitamui.commons.vitam.api.dto.RuleNodeResponseDto;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.access.external.api.AdminCollections;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.referential.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.common.service.VitamRuleService;

@Service
public class RuleInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(RuleInternalService.class);

    private ObjectMapper objectMapper;

    private RuleConverter converter;

    private LogbookService logbookService;

    private VitamRuleService ruleService;

    @Autowired
    public RuleInternalService(ObjectMapper objectMapper, RuleConverter converter,
            LogbookService logbookService, VitamRuleService ruleService) {
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.logbookService = logbookService;
        this.ruleService = ruleService;
    }

    public RuleDto getOne(VitamContext vitamContext, String identifier) {
        try {
            RequestResponse<FileRulesModel> requestResponse = ruleService.findRuleById(vitamContext, identifier);
            final RuleNodeResponseDto ruleResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class);
            if (ruleResponseDto.getResults().size() == 0) {
                return null;
            } else {
                return converter.convertVitamToDto(ruleResponseDto.getResults().get(0));
            }
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to get rule", e);
        }
    }

    public List<RuleDto> getAll(VitamContext vitamContext) {
        final RequestResponse<FileRulesModel> requestResponse;
        LOGGER.debug("Get ALL Rules !");
        try {
            requestResponse = ruleService
                    .findRules(vitamContext, new Select().getFinalSelect());
            LOGGER.debug("Response: {}", requestResponse);
            final RuleNodeResponseDto ruleResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class);
            return converter.convertVitamsToDtos(ruleResponseDto.getResults());
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find rules", e);
        }
    }

    public PaginatedValuesDto<RuleDto> getAllPaginated(final Integer pageNumber, final Integer size,
            final Optional<String> orderBy, final Optional<DirectionDto> direction, VitamContext vitamContext,
            Optional<String> criteria) {

        Map<String, Object> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (criteria.isPresent()) {
                TypeReference<HashMap<String, Object>> typRef = new TypeReference<HashMap<String, Object>>() {};
                vitamCriteria = objectMapper.readValue(criteria.get(), typRef);
            }

            query = VitamQueryHelper.createQueryDSL(vitamCriteria, pageNumber, size, orderBy, direction);
        } catch (InvalidParseOperationException | InvalidCreateOperationException ioe) {
            throw new InternalServerException("Unable to find rules with pagination", ioe);
        } catch ( IOException e ) {
            throw new InternalServerException("Can't parse criteria as Vitam query", e);
        }

        RuleNodeResponseDto results = this.findAll(vitamContext, query);
        boolean hasMore = pageNumber * size + results.getHits().getSize() < results.getHits().getTotal();

        final List<RuleDto> valuesDto = converter.convertVitamsToDtos(results.getResults());
        LOGGER.debug("Formats in page: {}", valuesDto);
        return new PaginatedValuesDto<>(valuesDto, pageNumber, results.getHits().getSize(), hasMore);
    }

    private RuleNodeResponseDto findAll(VitamContext vitamContext, JsonNode query) {
        final RequestResponse<FileRulesModel> requestResponse;
        try {
            requestResponse = ruleService.findRules(vitamContext, query);

            final RuleNodeResponseDto ruleResponseDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class);

            LOGGER.debug("Formats: {}", ruleResponseDto);

            return ruleResponseDto;
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Unable to find rules", e);
        }
    }

    public Boolean check(VitamContext vitamContext, RuleDto accessContractDto) {
        List<RuleDto> ruleDtoList = new ArrayList<>();
        ruleDtoList.add(accessContractDto);
        try {
            return !ruleService.checkAbilityToCreateRuleInVitam(converter.convertDtosToVitams(ruleDtoList), vitamContext);
        } catch (ConflictException e) {
          return true;
        } catch (VitamUIException e) {
            throw new InternalServerException("Unable to check rule", e);
        }
    }

    public RuleDto create(VitamContext vitamContext, RuleDto ruleDto) {
        LOGGER.debug("Try to create Rule {} {}", ruleDto, vitamContext);
        try {
            RequestResponse<?> requestResponse = ruleService.createRule(vitamContext, converter.convertDtoToVitam(ruleDto));
            final FileRulesModel ruleVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), FileRulesModel.class);
            return converter.convertVitamToDto(ruleVitamDto);
        } catch (InvalidParseOperationException | AccessExternalClientException | IOException | VitamClientException | JAXBException e) {
            throw new InternalServerException("Unable to create rule", e);
        }
    }

    public RuleDto patch(VitamContext vitamContext,final Map<String, Object> partialDto){
        LOGGER.debug("Try to patch rule {} {}", partialDto, vitamContext);
        String ruleId = (String) partialDto.get("id");
        RuleDto rule = this.getOne(vitamContext, ruleId);
        LOGGER.debug("Before update rule Dto : {}", rule);
        partialDto.forEach((key,value) ->
        {
            if (!"id".equals(key)) {
                try {
                    BeanUtilsBean.getInstance().copyProperty(rule, key, value);
                } catch (InvocationTargetException | IllegalAccessException e) {
                	LOGGER.warn(e.getMessage());
                }
            }
        });
        LOGGER.debug("Updated rule Dto {} : ", rule);
        FileRulesModel ruleVitam = converter.convertDtoToVitam(rule);
        LOGGER.debug("Converted rule Vitam DTO : {}", ruleVitam);
        try {
            RequestResponse<?> requestResponse = ruleService.patchRule(vitamContext, ruleId, ruleVitam);
            final FileRulesModel ruleVitamDto = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), FileRulesModel.class);
            LOGGER.debug("Patched rule Vitam DTO : {}", ruleVitamDto);
            RuleDto result = converter.convertVitamToDto(ruleVitamDto);
            LOGGER.debug("Result DTO : {}", result);
            return result;
        } catch (InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException | JAXBException e) {
            throw new InternalServerException("Unable to patch rule", e);
        }
    }

    public void delete(VitamContext context, String ruleId) {
        LOGGER.debug("Try to delete rule {} {}", ruleId, context);

        try {
            ruleService.deleteRule(context, ruleId);
        } catch (InvalidParseOperationException | AccessExternalClientException | VitamClientException | IOException | JAXBException e) {
            throw new InternalServerException("Unable to delete rule", e);
        }
    }

    public Response export(VitamContext context) {
        try {
            return ruleService.export(context);
        } catch (InvalidParseOperationException | InvalidCreateOperationException | VitamClientException e) {
            throw new InternalServerException("Unable to export agencies", e);
        }
    }

    public JsonNode findHistoryByIdentifier(VitamContext vitamContext, final String identifier) throws VitamClientException {
        try {
            return logbookService.selectOperations(VitamQueryHelper.buildOperationQuery(identifier),vitamContext).toJsonNode();
        } catch (InvalidCreateOperationException e) {
            throw new InternalServerException("Unable to fetch history", e);
        }
    }

}
