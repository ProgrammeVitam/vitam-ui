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
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.referential.common.dto.ProfileDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.ProfileExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.PROFILES_URL)
@Getter
@Setter
public class ProfileExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileExternalController.class);

    @Autowired
    private ProfileExternalService profileExternalService;

    @GetMapping()
    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES)
    public Collection<ProfileDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all profile criteria={}", criteria);
        SanityChecker.sanitizeCriteria(criteria);
        return profileExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES)
    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<ProfileDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy,
            direction);
        return profileExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public ProfileDto getOne(final @PathVariable("identifier") String identifier) {
        LOGGER.debug("get profile identifier={}");
        ParameterChecker.checkParameter("Identifier is mandatory : ", identifier);
        return profileExternalService.getOne(identifier);
    }

    @GetMapping(RestApi.DOWNLOAD_PROFILE + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> download(final @PathVariable("id") String id) {
        LOGGER.debug("download profile with id :{}", id);
        ParameterChecker.checkParameter("Event Identifier is mandatory : ", id);
        return profileExternalService.download(id);
    }


    /**
     * Import a Profile file document (xsd or rng, ...)
     *
     * @param id id of the archival profile
     * @param file MultipartFile representing the data to import
     * @return The jaxRs Response
     */
    @Secured(ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES)
    @PutMapping(value = RestApi.UPDATE_PROFILE_FILE + CommonConstants.PATH_ID)
    public ResponseEntity<JsonNode> importProfileFile(final @PathVariable("id") String id,
        @RequestParam("file") MultipartFile file) throws IOException {
        LOGGER.debug("Update {}  profile file with id :{}", id);
        LOGGER.debug("Import profile file {}", file);
        ParameterChecker.checkParameter("profileFile stream is a mandatory parameter: ", file);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
        return profileExternalService.updateProfileFile(id, file);
    }


    /**
     * Update an Archival Profile
     *
     * @param id id of the archival Profile
     * @param dto Entity to update
     * @return entity updated
     */
    @Secured(ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES)
    @PutMapping(CommonConstants.PATH_ID)
    public ResponseEntity<JsonNode> update(final @PathVariable("id") String id,
        final @Valid @RequestBody ProfileDto dto) throws InvalidParseOperationException {
        LOGGER.debug("Update {} with {}", id, dto);
        SanityChecker.sanitizeCriteria(id);
        Assert.isTrue(StringUtils.equals(id, dto.getId()),
            "Unable to update profile : the DTO id must match the path id");
        return profileExternalService.updateProfile(dto);
    }

    /**
     * Create an Archival Profile
     *
     * @param ProfileDto Entity to create
     * @return entity created
     */
    @Secured(ServicesData.ROLE_CREATE_ARCHIVE_PROFILES)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ProfileDto create(final @Valid @RequestBody ProfileDto ProfileDto) {
        LOGGER.debug("Create {}", ProfileDto);
        ApiUtils.checkValidity(ProfileDto);
        return profileExternalService.create(ProfileDto);
    }


    /***
     * Import profile
     * @param fileName the file name
     * @param file the agency csv file to import
     * @return the vitam response
     */
    @Secured(ServicesData.ROLE_IMPORT_ARCHIVE_PROFILES)
    @PostMapping(CommonConstants.PATH_IMPORT)
    public ResponseEntity<JsonNode> importArchivalProfiles(@RequestParam("fileName") String fileName,
        @RequestParam("file") MultipartFile file) {
        LOGGER.debug("Import file archivalProfile {}", fileName);
        ParameterChecker.checkParameter("The fileName is mandatory parameter :", fileName);
        SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
        return profileExternalService.importProfiles(fileName, file);
    }

    @Secured({ServicesData.ROLE_GET_ARCHIVE_PROFILES})
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(@RequestBody ProfileDto ProfileDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("check exist accessContract={}", ProfileDto);
        ApiUtils.checkValidity(ProfileDto);
        final boolean exist = profileExternalService.check(ProfileDto);
        return RestUtils.buildBooleanResponse(exist);
    }



    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES)
    public ProfileDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update.");
        return profileExternalService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES)
    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for accessContract with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return profileExternalService.findHistoryById(id);
    }
}
