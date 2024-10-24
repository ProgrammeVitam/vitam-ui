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
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.FileFormatDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.FileFormatExternalService;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.FILE_FORMATS_URL)
@Getter
@Setter
public class FileFormatExternalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFormatExternalController.class);

    @Autowired
    private FileFormatExternalService fileFormatExternalService;

    @GetMapping
    @Secured(ServicesData.ROLE_GET_FILE_FORMATS)
    public Collection<FileFormatDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all customer criteria={}", criteria);
        SanityChecker.sanitizeCriteria(criteria);
        return fileFormatExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_FILE_FORMATS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<FileFormatDto> getAllPaginated(
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
        return fileFormatExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_FILE_FORMATS)
    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public Object getByIdOrHistory(HttpServletRequest request)
        throws UnsupportedEncodingException, InvalidParseOperationException {
        LOGGER.debug("getByIdOrHistory ");
        String requestURL = request.getRequestURL().toString();
        String path = StringUtils.substringAfter(requestURL, RestApi.FILE_FORMATS_URL + "/");
        if (StringUtils.endsWith(path, "/history")) {
            return findHistoryById(StringUtils.substringBefore(path, "/history"));
        } else {
            return getOne(StringUtils.removeEndIgnoreCase(path, "/"));
        }
    }

    private FileFormatDto getOne(final @PathVariable("identifier") String identifier) {
        LOGGER.debug("get file format identifier={}");
        ParameterChecker.checkParameter("Identifier is mandatory : ", identifier);
        return fileFormatExternalService.getOne(identifier);
    }

    @Secured({ ServicesData.ROLE_GET_FILE_FORMATS })
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(
        @RequestBody FileFormatDto fileFormatDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) {
        SanityChecker.sanitizeCriteria(fileFormatDto);
        LOGGER.debug("check exist accessContract={}", fileFormatDto);
        ApiUtils.checkValidity(fileFormatDto);
        final boolean exist = fileFormatExternalService.check(fileFormatDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Secured(ServicesData.ROLE_CREATE_FILE_FORMATS)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public FileFormatDto create(final @Valid @RequestBody FileFormatDto fileFormatDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(fileFormatDto);
        LOGGER.debug("Create {}", fileFormatDto);
        ApiUtils.checkValidity(fileFormatDto);
        return fileFormatExternalService.create(fileFormatDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_FILE_FORMATS)
    public FileFormatDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(partialDto);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        Assert.isTrue(
            StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update."
        );
        return fileFormatExternalService.patch(partialDto);
    }

    private LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id) {
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for accessContract with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return fileFormatExternalService.findHistoryById(id);
    }

    @Secured(ServicesData.ROLE_DELETE_FILE_FORMATS)
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Delete fileFormat with id :{}", id);
        fileFormatExternalService.delete(id);
    }

    @Secured(ServicesData.ROLE_EXPORT_FILE_FORMATS)
    @GetMapping("/export")
    public ResponseEntity<Resource> export() {
        return fileFormatExternalService.export();
    }

    /***
     * Import file format from a xml file
     * @param file the file format xml to import
     * @return the vitam response
     */
    @Secured(ServicesData.ROLE_IMPORT_FILE_FORMATS)
    @PostMapping(CommonConstants.PATH_IMPORT)
    public JsonNode importFileFormats(@RequestParam("file") MultipartFile file) {
        ParameterChecker.checkParameter("The fileName is mandatory parameter : ", file.getOriginalFilename());
        SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
        SanityChecker.isValidFileName(file.getOriginalFilename());
        LOGGER.debug("Import file format file {}", file.getOriginalFilename());
        return fileFormatExternalService.importFileFormats(file.getOriginalFilename(), file);
    }
}
