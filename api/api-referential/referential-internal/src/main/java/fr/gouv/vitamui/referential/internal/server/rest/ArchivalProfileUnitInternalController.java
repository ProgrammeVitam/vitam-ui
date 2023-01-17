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

package fr.gouv.vitamui.referential.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        SanityChecker.sanitizeCriteria(criteria);
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
    public ArchivalProfileUnitDto update(final @PathVariable("id") String id, final @RequestBody ArchivalProfileUnitDto dto) throws InvalidParseOperationException, AccessExternalClientException {
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
