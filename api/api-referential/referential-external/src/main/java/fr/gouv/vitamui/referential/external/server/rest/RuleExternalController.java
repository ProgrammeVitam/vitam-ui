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
package fr.gouv.vitamui.referential.external.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.RuleDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.RuleExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.RULES_URL)
@Getter
@Setter
public class RuleExternalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleExternalController.class);

    private RuleExternalService ruleExternalService;

    private static final String IDENTIFIER_MANDATORY_MESSAGE = "The Identifier is a mandatory parameter: ";

    @Autowired
    public RuleExternalController(final RuleExternalService ruleExternalService) {
        this.ruleExternalService = ruleExternalService;
    }

    @GetMapping
    @Secured(ServicesData.ROLE_GET_RULES)
    public Collection<RuleDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all rules criteria={}", criteria);
        SanityChecker.sanitizeCriteria(criteria);
        return ruleExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_RULES)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<RuleDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) throws InvalidParseOperationException, PreconditionFailedException {
        if (orderBy.isPresent()) {
            SanityChecker.checkSecureParameter(orderBy.get());
        }
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            orderBy,
            direction
        );
        return ruleExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_RULES)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public RuleDto getOne(final @PathVariable("identifier") String identifier)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, identifier);
        SanityChecker.checkSecureParameter(identifier);
        LOGGER.debug("get rule identifier={}");
        return ruleExternalService.getOne(identifier);
    }

    @Secured({ ServicesData.ROLE_GET_RULES })
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(
        @RequestBody RuleDto ruleDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(ruleDto);
        LOGGER.debug("check exist accessContract={}", ruleDto);
        final boolean exist = ruleExternalService.check(ruleDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Secured(ServicesData.ROLE_CREATE_RULES)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<Void> create(final @Valid @RequestBody RuleDto ruleDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, ruleDto);
        SanityChecker.sanitizeCriteria(ruleDto);
        LOGGER.debug("Create {}", ruleDto);
        return RestUtils.buildBooleanResponse(ruleExternalService.createRule(ruleDto));
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_RULES)
    public ResponseEntity<Void> patch(
        final @PathVariable("id") String id,
        @RequestBody final Map<String, Object> partialDto
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(
            StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update."
        );
        return RestUtils.buildBooleanResponse(ruleExternalService.patchRule(id, partialDto));
    }

    @Secured(ServicesData.ROLE_GET_RULES)
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for accessContract with id :{}", id);
        return ruleExternalService.findHistoryById(id);
    }

    @Secured(ServicesData.ROLE_DELETE_RULES)
    @DeleteMapping(CommonConstants.PATH_ID)
    public ResponseEntity<Void> delete(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Delete rule with id :{}", id);
        return RestUtils.buildBooleanResponse(ruleExternalService.deleteRule(id));
    }

    @Secured(ServicesData.ROLE_GET_RULES)
    @GetMapping("/export")
    public ResponseEntity<Resource> export() {
        return ruleExternalService.export();
    }

    /***
     * Import agencies from a csv file
     * @param file the agency csv file to import
     * @return the vitam response
     */
    @Secured(ServicesData.ROLE_IMPORT_RULES)
    @PostMapping(CommonConstants.PATH_IMPORT)
    public JsonNode importRules(@RequestParam("file") MultipartFile file) {
        LOGGER.debug("Import referential file {}", file != null ? file.getOriginalFilename() : null);
        if (file == null) {
            throw new BadRequestException("No file to check .");
        }
        SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
        SanityChecker.isValidFileName(file.getOriginalFilename());
        return ruleExternalService.importRules(file.getOriginalFilename(), file);
    }
}
