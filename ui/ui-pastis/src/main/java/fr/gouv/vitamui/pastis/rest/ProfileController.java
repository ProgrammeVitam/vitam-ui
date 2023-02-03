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
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.pastis.service.ProfileService;
import fr.gouv.vitamui.referential.common.dto.ProfileDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

@Api(tags = "profile")
@RestController
@RequestMapping("${ui-pastis.prefix}" + RestApi.PROFILE)
@Consumes("application/json")
@Produces("application/json")
public class ProfileController extends AbstractUiRestController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileController.class);
    private static final String MANDATORY_ID_MESSAGE = "The Identifier is a mandatory parameter: ";
    protected final ProfileService service;

    @Autowired
    public ProfileController(final ProfileService service) {
        this.service = service;
    }

    /**
     * Get All Profiles
     *
     * @param criteria
     * @return
     */
    @ApiOperation(value = "Get entity")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<ProfileDto> getAll(final Optional<String> criteria) throws InvalidParseOperationException {

        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("Get all with criteria={}", criteria);
        return service.getAll(buildUiHttpContext(), criteria);
    }

    /**
     * Get all Profiles paginated
     *
     * @param page
     * @param size
     * @param criteria
     * @param orderBy
     * @param direction
     * @return a list of profiles
     */
    @ApiOperation(value = "Get entities paginated")
    @GetMapping(params = {"page", "size"})
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<ProfileDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam final Optional<String> criteria, @RequestParam final Optional<String> orderBy,
        @RequestParam final Optional<DirectionDto> direction) throws InvalidParseOperationException {
        if(orderBy.isPresent()){
            SanityChecker.checkSecureParameter(orderBy.get());
        }
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria,
            orderBy, direction);
        return service.getAllPaginated(page, size, criteria, orderBy, direction, buildUiHttpContext());
    }

    /**
     * Get Profile by Identifier
     *
     * @param identifier
     * @return a profile
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "Get profile by ID")
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    @ResponseStatus(HttpStatus.OK)
    public ProfileDto getById(final @PathVariable("identifier") String identifier) throws UnsupportedEncodingException,
        InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter(MANDATORY_ID_MESSAGE, identifier);
        SanityChecker.checkSecureParameter(identifier);
        LOGGER.debug("getById {} / {}", identifier, URLEncoder.encode(identifier, StandardCharsets.UTF_8));
        return service.getOne(buildUiHttpContext(), URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString()));
    }

    /**
     * Download Profile
     *
     * @param id Identifier of Metadata
     * @return Profile file
     */
    @ApiOperation(value = "download profile by id")
    @GetMapping(value = RestApi.DOWNLOAD_PROFILE + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> download(final @PathVariable("id") String id) throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter(MANDATORY_ID_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("download {} profile with id :{}", id);
        Resource body = service.download(buildUiHttpContext(), id).getBody();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment")
            .body(body);
    }

    /**
     * Import a Profile file document (xsd or rng, ...) in a profile
     *
     * @param id id of the archival profile
     * @param file MultipartFile file to import
     * @return The jaxRs Response
     */
    @ApiOperation(value = "Importer un fichier xsd ou rng dans un profil")
    @PutMapping(value = RestApi.UPDATE_PROFILE_FILE + CommonConstants.PATH_ID)
    public ResponseEntity<JsonNode> importProfileFile(final @PathVariable("id") String id,
        @RequestParam("file") MultipartFile file) throws IOException, InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter("profileFile stream is a mandatory parameter: ", file);
        SanityChecker.checkSecureParameter(id);
        ParameterChecker.checkParameter(MANDATORY_ID_MESSAGE, id);
        LOGGER.debug("Update profile file with id :{}", id);
        return service.updateProfileFile(buildUiHttpContext(), id, file);
    }


    /**
     * Modify Profile by Identifier
     *
     * @param profileDto
     * @return
     */
    @ApiOperation(value = "Update entity")
    @PutMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<JsonNode> updateProfile(@Valid @RequestBody final ProfileDto profileDto) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(profileDto);
        LOGGER.debug("update profile {}", profileDto.getId());
        return service.updateProfile(buildUiHttpContext(), profileDto);
    }


    /**
     * Create Profile
     *
     * @param profileDto
     * @return a profile
     */
    @ApiOperation(value = "Create Archival Profile")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ProfileDto> create(@Valid @RequestBody ProfileDto profileDto) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(profileDto);
        LOGGER.debug("create profile={}", profileDto);
        ProfileDto result = service.create(buildUiHttpContext(), profileDto);
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /***
     * Import profile from json file
     * @param request HTTP request
     * @param file the file to import
     * @return the Vitam response
     */
    @ApiOperation(value = "import profile")
    @PostMapping(CommonConstants.PATH_IMPORT)
    public ResponseEntity<JsonNode> importProfiles(@Context HttpServletRequest request, MultipartFile file) throws InvalidParseOperationException {
        LOGGER.debug("Import profile from a file {}", file != null ? file.getOriginalFilename() : null);
        return service.importProfiles(buildUiHttpContext(), file);
    }



    /**
     * Check access
     *
     * @param profileDto
     * @return a ResponseEntity
     */
    @ApiOperation(value = "Check ability to create profile")
    @PostMapping(path = CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(@RequestBody ProfileDto profileDto) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(profileDto);
        LOGGER.debug("check ability to create profile={}", profileDto);
        final boolean exist = service.check(buildUiHttpContext(), profileDto);
        LOGGER.debug("response value={}" + exist);
        return RestUtils.buildBooleanResponse(exist);
    }



}
