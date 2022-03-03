package fr.gouv.vitamui.referential.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.ArchivalProfileUnitDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.internal.server.archivalprofileunit.ArchivalProfileUnitInternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.ARCHIVAL_PROFILE_URL)
@Getter
@Setter
public class ArchivalProfileUnitInternalController {
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ArchivalProfileUnitInternalController.class);

    @Autowired
    private ArchivalProfileUnitInternalService archivalProfileUnitInternalService;

    @Autowired
    private InternalSecurityService securityService;

    @GetMapping()
    public Collection<ArchivalProfileUnitDto> getAll(@RequestParam final Optional<String> criteria) {
        LOGGER.debug("get all archival unit profiles criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        LOGGER.debug("context={}", vitamContext);
        return archivalProfileUnitInternalService.getAll(vitamContext);
    }

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<ArchivalProfileUnitDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
                                                                      @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
                                                                      @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria, orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return archivalProfileUnitInternalService.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public ArchivalProfileUnitDto getOne(final @PathVariable("identifier") String identifier) throws UnsupportedEncodingException {
        LOGGER.debug("get archival unit profile identifier={} / {}", identifier, URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString()));
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return archivalProfileUnitInternalService.getOne(vitamContext, URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString()));
    }


    @PutMapping(CommonConstants.PATH_ID)
    public ArchivalProfileUnitDto update(final @PathVariable("id") String id, final @RequestBody ArchivalProfileUnitDto dto) throws AccessExternalClientException, InvalidParseOperationException {
        LOGGER.debug("Update {} with {}", id, dto);
         ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        LOGGER.debug("context={}", vitamContext);
        return archivalProfileUnitInternalService.update(dto, vitamContext);
    }

    @PostMapping
    public ArchivalProfileUnitDto create(@Valid @RequestBody ArchivalProfileUnitDto archivalUnitProfile, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("create archival unit profile={}", archivalUnitProfile);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        archivalUnitProfile.setTenant(0);
        return archivalProfileUnitInternalService.create(vitamContext, archivalUnitProfile);
    }

    @PostMapping(CommonConstants.PATH_IMPORT)
    public ResponseEntity<JsonNode> importArchivalUnitProfile(@RequestParam("fileName") String fileName, @RequestParam("file") MultipartFile file) {
        LOGGER.debug("import Archival Unit Profile by a file {}", fileName);
        SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return archivalProfileUnitInternalService.importProfile(vitamContext, fileName, file);
    }










    //TODO : Patch Check

    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> checkExist(@RequestBody ArchivalProfileUnitDto archivalProfile, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("check exist file format={}", archivalProfile);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        final boolean exist = archivalProfileUnitInternalService.check(vitamContext, archivalProfile);
        return RestUtils.buildBooleanResponse(exist);
    }






}
