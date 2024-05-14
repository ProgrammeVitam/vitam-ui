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
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.AgencyExternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.AGENCIES_URL)
@Getter
@Setter
public class AgencyExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AgencyExternalController.class);

    public static final String MANDATORY_IDENTIFIER = "Identifier is mandatory : ";

    @Autowired
    private AgencyExternalService agencyExternalService;

    @GetMapping
    @Secured(ServicesData.ROLE_GET_AGENCIES)
    public Collection<AgencyDto> getAll(final Optional<String> criteria) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("get all customer criteria={}", criteria);
        return agencyExternalService.getAll(criteria);
    }

    @Secured(ServicesData.ROLE_GET_AGENCIES)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<AgencyDto> getAllPaginated(
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
        return agencyExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_AGENCIES)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public AgencyDto getOne(final @PathVariable("identifier") String identifier)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.checkSecureParameter(identifier);
        LOGGER.debug("get agency identifier={}");
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, identifier);
        SanityChecker.checkSecureParameter(identifier);
        return agencyExternalService.getOne(identifier);
    }

    @Secured({ ServicesData.ROLE_GET_AGENCIES })
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(
        @RequestBody @Valid AgencyDto agencyDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(agencyDto);
        ApiUtils.checkValidity(agencyDto);
        LOGGER.debug("check exist accessContract={}", agencyDto);
        final boolean exist = agencyExternalService.check(agencyDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Secured(ServicesData.ROLE_CREATE_AGENCIES)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public AgencyDto create(final @Valid @RequestBody AgencyDto agencyDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(agencyDto);
        ApiUtils.checkValidity(agencyDto);
        LOGGER.debug("Create {}", agencyDto);
        return agencyExternalService.create(agencyDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_AGENCIES)
    public AgencyDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(partialDto);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(
            StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update."
        );
        return agencyExternalService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_GET_AGENCIES)
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for accessContract with id :{}", id);
        return agencyExternalService.findHistoryById(id);
    }

    @Secured(ServicesData.ROLE_DELETE_AGENCIES)
    @DeleteMapping(CommonConstants.PATH_ID)
    public ResponseEntity<Boolean> delete(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        LOGGER.debug("Delete agency with id :{}", id);
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        return agencyExternalService.deleteWithResponse(id);
    }

    @Secured(ServicesData.ROLE_EXPORT_AGENCIES)
    @GetMapping(CommonConstants.PATH_EXPORT)
    public ResponseEntity<Resource> export() {
        LOGGER.debug("export agencies");
        return agencyExternalService.export();
    }

    /***
     * Import agencies from a csv file
     * @param fileName the file name
     * @param file the agency csv file to import
     * @return the vitam response
     */
    @Secured(ServicesData.ROLE_IMPORT_AGENCIES)
    @PostMapping(CommonConstants.PATH_IMPORT)
    public JsonNode importAgencies(
        @RequestParam(value = "fileName", required = false) String fileName,
        @RequestParam("file") MultipartFile file
    ) {
        if (file != null) {
            SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
            SanityChecker.isValidFileName(file.getOriginalFilename());
        }

        if (Objects.isNull(fileName)) {
            fileName = file.getOriginalFilename();
        }

        SanityChecker.isValidFileName(fileName);
        SafeFileChecker.checkSafeFilePath(fileName);
        ParameterChecker.checkParameter("The fileName is mandatory parameter : ", fileName);
        LOGGER.debug("Import agency file {}", fileName);

        return agencyExternalService.importAgencies(fileName, file);
    }
}
