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
package fr.gouv.vitamui.referential.internal.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ManagementContractDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.internal.server.managementcontract.service.ManagementContractInternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.MANAGEMENT_CONTRACTS_URL)
@Getter
@Setter
public class ManagementContractInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        ManagementContractInternalController.class
    );

    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";

    @Autowired
    private ManagementContractInternalService managementContractInternalService;

    @Autowired
    private InternalSecurityService securityService;

    @GetMapping
    public Collection<ManagementContractDto> getAll(@RequestParam final Optional<String> criteria) {
        LOGGER.debug("get all management contract criteria={}", criteria);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return managementContractInternalService.getAll(vitamContext);
    }

    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<ManagementContractDto> getAllPaginated(
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
            "getPaginateEntities managementContract page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            criteria,
            orderBy,
            direction
        );
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return managementContractInternalService.getAllPaginated(
            page,
            size,
            orderBy,
            direction,
            vitamContext,
            criteria
        );
    }

    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public ManagementContractDto getOne(final @PathVariable("identifier") String identifier)
        throws UnsupportedEncodingException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, identifier);
        SanityChecker.checkSecureParameter(identifier);
        LOGGER.debug(
            "get managementContract identifier = {} / {}",
            identifier,
            URLDecoder.decode(identifier, StandardCharsets.UTF_8)
        );
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return managementContractInternalService.getOne(
            vitamContext,
            URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString())
        );
    }

    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> checkExist(
        @RequestBody ManagementContractDto managementContractDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(managementContractDto);
        LOGGER.debug("check exist managementContract = {}", managementContractDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        managementContractDto.setTenant(tenant);
        final boolean exist = managementContractInternalService.check(vitamContext, managementContractDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @PostMapping
    public ManagementContractDto create(
        @Valid @RequestBody ManagementContractDto managementContractDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(managementContractDto);
        LOGGER.debug("create managementContract = {}", managementContractDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        managementContractDto.setTenant(tenant);
        return managementContractInternalService.create(vitamContext, managementContractDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    public ManagementContractDto patch(
        final @PathVariable("id") String id,
        @RequestBody final ManagementContractDto partialDto
    )
        throws InvalidParseOperationException, PreconditionFailedException, AccessExternalClientException, JsonProcessingException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.sanitizeCriteria(partialDto);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        Assert.isTrue(
            StringUtils.equals(id, partialDto.getId()),
            "The DTO identifier must match the path identifier for update."
        );
        return managementContractInternalService.patch(vitamContext, partialDto);
    }

    @GetMapping(CommonConstants.PATH_ID + "/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for managementContract with id :{}", id);
        return managementContractInternalService.findHistoryByIdentifier(vitamContext, id);
    }
}
