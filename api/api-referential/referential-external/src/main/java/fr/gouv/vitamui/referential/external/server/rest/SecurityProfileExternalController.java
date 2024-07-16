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

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.SecurityProfileExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.SECURITY_PROFILES_URL)
@Getter
@Setter
public class SecurityProfileExternalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityProfileExternalController.class);

    @Autowired
    private SecurityProfileExternalService securityProfileExternalService;

    @GetMapping
    @Secured(ServicesData.ROLE_GET_SECURITY_PROFILES)
    public Collection<SecurityProfileDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all customer criteria={}", criteria);
        SanityChecker.sanitizeCriteria(criteria);
        return securityProfileExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_SECURITY_PROFILES)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<SecurityProfileDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) {
        SanityChecker.sanitizeCriteria(criteria);
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            orderBy,
            direction
        );
        return securityProfileExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_SECURITY_PROFILES)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public SecurityProfileDto getOne(final @PathVariable("identifier") String identifier)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", identifier);
        SanityChecker.checkSecureParameter(identifier);
        LOGGER.debug("get security profile identifier={}");
        return securityProfileExternalService.getOne(identifier);
    }

    @Secured({ ServicesData.ROLE_GET_SECURITY_PROFILES })
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(
        @RequestBody SecurityProfileDto securityProfileDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(securityProfileDto);
        LOGGER.debug("check exist accessContract={}", securityProfileDto);
        final boolean exist = securityProfileExternalService.check(securityProfileDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Secured(ServicesData.ROLE_CREATE_SECURITY_PROFILES)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public SecurityProfileDto create(final @Valid @RequestBody SecurityProfileDto securityProfileDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(securityProfileDto);
        LOGGER.debug("Create {}", securityProfileDto);
        return securityProfileExternalService.create(securityProfileDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_SECURITY_PROFILES)
    public SecurityProfileDto patch(
        final @PathVariable("id") String id,
        @RequestBody final Map<String, Object> partialDto
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(
            StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update."
        );
        return securityProfileExternalService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_GET_SECURITY_PROFILES)
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for accessContract with id :{}", id);
        return securityProfileExternalService.findHistoryById(id);
    }

    @Secured(ServicesData.ROLE_DELETE_SECURITY_PROFILES)
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Event Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Delete securityProfile with id :{}", id);
        securityProfileExternalService.delete(id);
    }
}
