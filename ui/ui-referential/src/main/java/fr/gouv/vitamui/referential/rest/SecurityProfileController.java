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
package fr.gouv.vitamui.referential.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.service.SecurityProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "securityProfile")
@RestController
@RequestMapping("${ui-referential.prefix}/security-profile")
@Consumes("application/json")
@Produces("application/json")
public class SecurityProfileController extends AbstractUiRestController {

    protected final SecurityProfileService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SecurityProfileController.class);

    @Autowired
    public SecurityProfileController(final SecurityProfileService service) {
        this.service = service;
    }

    @ApiOperation(value = "Get entity")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<SecurityProfileDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("Get all with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return service.getAll(buildUiHttpContext(), criteria);
    }

    @ApiOperation(value = "Get entities paginated")
    @GetMapping(params = { "page", "size" })
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<SecurityProfileDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam final Optional<String> criteria, @RequestParam final Optional<String> orderBy, @RequestParam final Optional<DirectionDto> direction) {
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria, orderBy, direction);
        return service.getAllPaginated(page, size, criteria, orderBy, direction, buildUiHttpContext());
    }

    @ApiOperation(value = "Get agency by ID")
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    @ResponseStatus(HttpStatus.OK)
    public SecurityProfileDto getById(final @PathVariable("identifier") String identifier) throws UnsupportedEncodingException {
        LOGGER.debug("getById {} / {}", identifier, URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString()));
        return service.getOne(buildUiHttpContext(), URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString()));
    }

    /**
     * Check access contract.
     *
     * @param securityProfileDto
     * @return
     */
    @ApiOperation(value = "Check ability to create securityProfile")
    @PostMapping(path = CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(@RequestBody SecurityProfileDto securityProfileDto) {
        LOGGER.debug("check ability to create securityProfile={}", securityProfileDto);
        final boolean exist = service.check(buildUiHttpContext(), securityProfileDto);
        LOGGER.debug("response value={}" + exist);
        return RestUtils.buildBooleanResponse(exist);
    }

    @ApiOperation(value = "Create securityProfile")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityProfileDto create(@Valid @RequestBody  SecurityProfileDto securityProfileDto) {
        LOGGER.debug("create securityProfile={}", securityProfileDto);
        return service.create(buildUiHttpContext(), securityProfileDto);
    }

    @ApiOperation(value = "Patch entity")
    @PatchMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public SecurityProfileDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch User {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch securityProfile : the DTO id must match the path id.");
        return service.patch(buildUiHttpContext(), partialDto, id);
    }

    @ApiOperation(value = "get history by securityProfile's id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable String id) {
        LOGGER.debug("get logbook for securityProfile with id :{}", id);
        return service.findHistoryById(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "delete securityProfile")
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(final @PathVariable String id) {
        LOGGER.debug("delete securityProfile with id :{}", id);
        service.delete(buildUiHttpContext(), id);
    }

}
