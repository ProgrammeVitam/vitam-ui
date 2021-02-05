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
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.referential.common.dto.FileFormatDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.FileFormatExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping(RestApi.FILE_FORMATS_URL)
@Getter
@Setter
public class FileFormatExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(FileFormatExternalController.class);

    @Autowired
    private FileFormatExternalService fileFormatExternalService;

    @GetMapping()
    @Secured(ServicesData.ROLE_GET_FILE_FORMATS)
    public Collection<FileFormatDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all customer criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return fileFormatExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_FILE_FORMATS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<FileFormatDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        return fileFormatExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_FILE_FORMATS)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public FileFormatDto getOne(final @PathVariable("identifier") String identifier) {
        LOGGER.debug("get file format identifier={}");
        ParameterChecker.checkParameter("Identifier is mandatory : " , identifier);
        return fileFormatExternalService.getOne(identifier);
    }

    @Secured({ ServicesData.ROLE_GET_FILE_FORMATS })
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(@RequestBody FileFormatDto fileFormatDto, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("check exist accessContract={}", fileFormatDto);
        ApiUtils.checkValidity(fileFormatDto);
        final boolean exist = fileFormatExternalService.check(fileFormatDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Secured(ServicesData.ROLE_CREATE_FILE_FORMATS)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public FileFormatDto create(final @Valid @RequestBody FileFormatDto fileFormatDto) {
        LOGGER.debug("Create {}", fileFormatDto);
        ApiUtils.checkValidity(fileFormatDto);
        return fileFormatExternalService.create(fileFormatDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_FILE_FORMATS)
    public FileFormatDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return fileFormatExternalService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_GET_FILE_FORMATS)
    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for accessContract with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : " , id);
        return fileFormatExternalService.findHistoryById(id);
    }

    @Secured(ServicesData.ROLE_DELETE_FILE_FORMATS)
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(final @PathVariable("id") String id) {
        LOGGER.debug("Delete fileFormat with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : " , id);
        fileFormatExternalService.delete(id);
    }

    @Secured(ServicesData.ROLE_EXPORT_FILE_FORMATS)
    @GetMapping("/export")
    public ResponseEntity<Resource> export() {
        return fileFormatExternalService.export();
    }

    /***
     * Import file format from an xml file
     * @param fileName the file name
     * @param file the agency csv file to import
     * @return the vitam response
     */
    @Secured(ServicesData.ROLE_IMPORT_FILE_FORMATS)
    @PostMapping(CommonConstants.PATH_IMPORT)
    public JsonNode importFileFormats(@RequestParam("fileName") String fileName, @RequestParam("file") MultipartFile file) {
        LOGGER.debug("Import file format file {}", fileName);
        ParameterChecker.checkParameter("The fileName is mandatory parameter :", fileName);
        SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
        return fileFormatExternalService.importFileFormats(fileName, file);
    }
}
