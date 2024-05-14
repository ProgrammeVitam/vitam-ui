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

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.ContextDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.ContextExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.CONTEXTS_URL)
@Getter
@Setter
public class ContextExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ContextExternalController.class);

    @Autowired
    private ContextExternalService contextExternalService;

    @GetMapping
    @Secured(ServicesData.ROLE_GET_CONTEXTS)
    public Collection<ContextDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all context criteria={}", criteria);
        SanityChecker.sanitizeCriteria(criteria);
        return contextExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_CONTEXTS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<ContextDto> getAllPaginated(
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
        return contextExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_CONTEXTS)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public ContextDto getOne(final @PathVariable("identifier") String identifier) {
        SanityChecker.checkSecureParameter(identifier);
        LOGGER.debug("get context identifier={}");
        return contextExternalService.getOne(identifier);
    }

    @Secured({ ServicesData.ROLE_GET_CONTEXTS })
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(
        @Valid @RequestBody ContextDto contextDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) {
        LOGGER.debug("check exist context = {}", contextDto);
        SanityChecker.sanitizeCriteria(contextDto);
        ApiUtils.checkValidity(contextDto);
        final boolean exist = contextExternalService.check(contextDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Secured(ServicesData.ROLE_CREATE_CONTEXTS)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ContextDto create(final @Valid @RequestBody ContextDto contextDto) {
        SanityChecker.sanitizeCriteria(contextDto);
        LOGGER.debug("Create {}", contextDto);
        return contextExternalService.create(contextDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_CONTEXTS)
    public ContextDto patch(final @PathVariable("id") String id, @RequestBody final ContextDto partialDto) {
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(
            StringUtils.equals(id, partialDto.getId()),
            "The DTO identifier must match the path identifier for update."
        );
        SanityChecker.checkSecureParameter(id);
        return contextExternalService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_GET_CONTEXTS)
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id) {
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for context with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return contextExternalService.findHistoryById(id);
    }
}
