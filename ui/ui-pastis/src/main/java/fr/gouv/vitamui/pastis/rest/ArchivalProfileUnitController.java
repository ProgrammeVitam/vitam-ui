/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

package fr.gouv.vitamui.pastis.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.pastis.service.ArchivalProfileUnitService;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

@Api(tags = "archival-profile")
@RestController
@RequestMapping("${ui-pastis.prefix}" + RestApi.ARCHIVAL_PROFILE)
@Consumes("application/json")
@Produces("application/json")
public class ArchivalProfileUnitController extends AbstractUiRestController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ArchivalProfileUnitController.class);
    protected final ArchivalProfileUnitService service;

    @Autowired
    public ArchivalProfileUnitController(final ArchivalProfileUnitService service) {
        this.service = service;
    }


    /**
     * Get all Archival Unit Profiles
     *
     * @param criteria
     * @return
     */
    @ApiOperation(value = "Get entity")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<ArchivalProfileUnitDto> getAll(final Optional<String> criteria) throws InvalidParseOperationException {
        LOGGER.debug("Get all with criteria={}", criteria);
        SanityChecker.sanitizeCriteria(criteria);
        return service.getAll(buildUiHttpContext(), criteria);
    }

    /**
     * Get All Archival Unit Profiles Paginated
     *
     * @param page
     * @param size
     * @param criteria
     * @param orderBy
     * @param direction
     * @return
     */
    @ApiOperation(value = "Get entities paginated")
    @GetMapping(params = {"page", "size"})
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<ArchivalProfileUnitDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam final Optional<String> criteria, @RequestParam final Optional<String> orderBy,
        @RequestParam final Optional<DirectionDto> direction) throws InvalidParseOperationException {
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria,
            orderBy, direction);
        return service.getAllPaginated(page, size, criteria, orderBy, direction, buildUiHttpContext());
    }


    /**
     * Get Archival Unit Profile by Identifier
     *
     * @param identifier
     * @return
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "Get profile by ID")
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    @ResponseStatus(HttpStatus.OK)
    public ArchivalProfileUnitDto getById(final @PathVariable("identifier") String identifier)
        throws UnsupportedEncodingException, InvalidParseOperationException {
        LOGGER.debug("getById {} / {}", identifier, URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString()));
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", identifier);
        return service.getOne(buildUiHttpContext(), URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString()));
    }

    /**
     * Modify Archival Unit Profile by Identifier
     *
     * @param archivalProfileUnitDto
     * @return
     */

    @ApiOperation(value = "Update entity")
    @PutMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ArchivalProfileUnitDto update(@RequestBody final ArchivalProfileUnitDto archivalProfileUnitDto) throws InvalidParseOperationException {
        LOGGER.debug("update profile {}", archivalProfileUnitDto.getId());
        return service.update(buildUiHttpContext(), archivalProfileUnitDto);
    }

    /**
     * Create Archival Unit Profile
     *
     * @param archivalProfileUnitDto
     * @return
     */
    @ApiOperation(value = "Create Archival Unit Profile")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ArchivalProfileUnitDto> create(
        @Valid @RequestBody ArchivalProfileUnitDto archivalProfileUnitDto) throws InvalidParseOperationException {
        LOGGER.debug("create archival unit profile={}", archivalProfileUnitDto);
        ArchivalProfileUnitDto result = service.create(buildUiHttpContext(), archivalProfileUnitDto);
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Import Archival Unit Profile from json file
     *
     * @param request HTTP request
     * @param file the file to import
     * @return the Vitam response
     */
    @ApiOperation(value = "import Archival Unit Profile")
    @PostMapping(CommonConstants.PATH_IMPORT)
    public ResponseEntity<JsonNode> importProfiles(@Context HttpServletRequest request, MultipartFile file) throws InvalidParseOperationException {
        LOGGER.debug("Import Archival Unit Profile from a file {}", file != null ? file.getOriginalFilename() : null);
        return service.importArchivalUnitProfiles(buildUiHttpContext(), file);
    }



    /**
     * Check access
     *
     * @param archivalProfileUnitDto
     * @return
     */
    @ApiOperation(value = "Check ability to create ontology")
    @PostMapping(path = CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(@RequestBody ArchivalProfileUnitDto archivalProfileUnitDto) throws InvalidParseOperationException {
        LOGGER.debug("check ability to create profile={}", archivalProfileUnitDto);
        final boolean exist = service.check(buildUiHttpContext(), archivalProfileUnitDto);
        LOGGER.debug("response value={}" + exist);
        return RestUtils.buildBooleanResponse(exist);
    }



}



