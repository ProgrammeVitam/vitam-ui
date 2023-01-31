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
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.ArchivalProfileUnitExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.ARCHIVAL_PROFILE_URL)
@Getter
@Setter
public class ArchivalProfileUnitExternalController {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivalProfileUnitExternalController.class);

    @Autowired
    private ArchivalProfileUnitExternalService archivalProfileUnitExternalService;

    @GetMapping()
    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES_UNIT)
    public Collection<ArchivalProfileUnitDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all archival unit profiles criteria={}", criteria);
        SanityChecker.sanitizeCriteria(criteria);
        return archivalProfileUnitExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES_UNIT)
    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<ArchivalProfileUnitDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy,
            direction);
        return archivalProfileUnitExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_ARCHIVE_PROFILES_UNIT)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public ArchivalProfileUnitDto getOne(final @PathVariable("identifier") String identifier)
        throws InvalidParseOperationException, PreconditionFailedException {
        LOGGER.debug("get archival unit profile identifier={}");
        ParameterChecker.checkParameter("Identifier is mandatory : " , identifier);
        SanityChecker.checkSecureParameter(identifier);
        return archivalProfileUnitExternalService.getOne(identifier);
    }

    @Secured(ServicesData.ROLE_UPDATE_ARCHIVE_PROFILES_UNIT)
    @PutMapping(CommonConstants.PATH_ID)
    public ArchivalProfileUnitDto update(final @PathVariable("id") String id, final @Valid @RequestBody ArchivalProfileUnitDto dto) throws InvalidParseOperationException {
        ParameterChecker.checkParameter("Identifier is mandatory : " , id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(dto);
        LOGGER.debug("Update archival unit profile with identifier metadata {} to {}", id, dto);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "Unable to update archival unit profile : the DTO id must match the path id");
        return archivalProfileUnitExternalService.update(dto);
    }

    /**
     * Create an Archival Profile Unit
     *
     * @param archivalProfileUnitDto Entity to create
     * @return entity created
     */
    @Secured(ServicesData.ROLE_CREATE_ARCHIVE_PROFILES_UNIT)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ArchivalProfileUnitDto create(final @Valid @RequestBody ArchivalProfileUnitDto archivalProfileUnitDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(archivalProfileUnitDto);
        LOGGER.debug("Create {}", archivalProfileUnitDto);
        ApiUtils.checkValidity(archivalProfileUnitDto);
        return archivalProfileUnitExternalService.create(archivalProfileUnitDto);
    }

    /***
     * Import Archival Unit Profile
     * @param fileName the file name
     * @param file the agency csv file to import
     * @return the vitam response
     */
    @Secured(ServicesData.ROLE_IMPORT_ARCHIVE_PROFILES_UNIT)
    @PostMapping(CommonConstants.PATH_IMPORT)
    public ResponseEntity<JsonNode> importArchivalUnitProfiles(@RequestParam("fileName") String fileName,
        @RequestParam("file") MultipartFile file)
        throws InvalidParseOperationException {
        LOGGER.debug("Import Archival Unit Profile file {}", fileName);
        ParameterChecker.checkParameter("The fileName is mandatory parameter :", fileName);
        if(file != null) {
            SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
            SanityChecker.isValidFileName(file.getOriginalFilename());
        }
        SanityChecker.checkSecureParameter(fileName);
        SanityChecker.isValidFileName(fileName);
        SafeFileChecker.checkSafeFilePath(fileName);
        return archivalProfileUnitExternalService.importArchivalUnitProfiles(fileName, file);
    }

    @Secured({ServicesData.ROLE_GET_PASTIS})
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(@RequestBody ArchivalProfileUnitDto archivalProfileUnitDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant)
        throws InvalidParseOperationException, PreconditionFailedException {
        ApiUtils.checkValidity(archivalProfileUnitDto);
        SanityChecker.sanitizeCriteria(archivalProfileUnitDto);
        LOGGER.debug("check exist accessContract={}", archivalProfileUnitDto);
        final boolean exist = archivalProfileUnitExternalService.check(archivalProfileUnitDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_PASTIS)
    public ArchivalProfileUnitDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return archivalProfileUnitExternalService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_GET_PASTIS)
    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : " , id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for accessContract with id :{}", id);
        return archivalProfileUnitExternalService.findHistoryById(id);
    }
}
