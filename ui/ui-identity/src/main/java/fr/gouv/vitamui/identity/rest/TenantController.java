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
package fr.gouv.vitamui.identity.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.identity.service.TenantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Api(tags = "tenants")
@RestController
@RequestMapping("${ui-identity.prefix}/tenants")
public class TenantController extends AbstractUiRestController {

    private final TenantService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantController.class);

    @Autowired
    public TenantController(final TenantService service) {
        this.service = service;
    }

    @ApiOperation(value = "Get entity")
    @GetMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public TenantDto getOne(final @PathVariable String id) throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.info("Get tenant={}", id);
        return service.getOne(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "Get all entities")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<TenantDto> getAll(final Optional<String> criteria) throws InvalidParseOperationException,
        PreconditionFailedException {

        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("Get all with criteria={}", criteria);
        return service.getAll(buildUiHttpContext(), criteria);
    }

    @ApiOperation(value = "Create entity")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDto create(@RequestBody final TenantDto entityDto) throws InvalidParseOperationException, PreconditionFailedException {

        SanityChecker.sanitizeCriteria(entityDto);
        LOGGER.info("create class={}", entityDto.getClass().getName());
        return service.create(buildUiHttpContext(), entityDto);
    }

    @ApiOperation(value = "Update entity")
    @PutMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public TenantDto update(final @PathVariable("id") String id, @RequestBody final TenantDto entityDto)
        throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(entityDto);
        LOGGER.info("update class={}", entityDto.getClass().getName());
        Assert.isTrue(StringUtils.equals(id, entityDto.getId()), "The DTO identifier must match the path identifier for update.");
        return service.update(buildUiHttpContext(), entityDto);
    }

    @ApiOperation(value = "Patch entity")
    @PatchMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public TenantDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch tenant {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch tenant : the DTO id must match the path id");
        return service.patch(buildUiHttpContext(), partialDto, id);
    }

    @ApiOperation(value = "Check entity exist by criteria")
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) throws InvalidParseOperationException,
        PreconditionFailedException {

        SanityChecker.sanitizeCriteria(Optional.of(criteria));
        LOGGER.debug("check exists criteria={}", criteria);
        final boolean exist = service.checkExist(buildUiHttpContext(), criteria);
        LOGGER.debug("response value={}", exist);
        return RestUtils.buildBooleanResponse(exist);
    }

    @ApiOperation(value = "get history by tenant id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable String id)
        throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for tenant with id :{}", id);
        return service.findHistoryById(buildUiHttpContext(), id);
    }
}
