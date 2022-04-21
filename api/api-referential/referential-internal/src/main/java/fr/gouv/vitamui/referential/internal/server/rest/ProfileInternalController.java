/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 * <p>
 * contact@programmevitam.fr
 * <p>
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 * <p>
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
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
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalNotFoundException;
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
import fr.gouv.vitamui.referential.common.dto.ProfileDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.internal.server.profile.ProfileInternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.PROFILES_URL)
@Getter
@Setter
public class ProfileInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProfileInternalController.class);

    @Autowired
    private ProfileInternalService profileInternalService;

    @Autowired
    private InternalSecurityService securityService;

    @GetMapping()
    public Collection<ProfileDto> getAll(@RequestParam final Optional<String> criteria) {
        LOGGER.debug("get all archive profiles criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return profileInternalService.getAll(vitamContext);
    }

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<ProfileDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
                                                          @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
                                                          @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria, orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return profileInternalService.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public ProfileDto getOne(final @PathVariable("identifier") String identifier) throws UnsupportedEncodingException {
        LOGGER.debug("get profile identifier={} / {}", identifier, URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString()));
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return profileInternalService.getOne(vitamContext, URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString()));
    }

    @GetMapping(RestApi.DOWNLOAD_PROFILE + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> downloadByMetadataIdentifier(
        final @PathVariable("id") String id) throws AccessExternalNotFoundException, AccessExternalClientException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        LOGGER.debug("download profile with id :{}", id);
        Response response = profileInternalService.download(vitamContext, id);
        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            Resource resource = new InputStreamResource((InputStream) entity);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return null;
    }

    /**
     * Import a Profile file document (xsd or rng, ...)
     *
     * @param file MultipartFile representing the data to import
     * @param id id of the archival profile
     * @return The jaxRs Response
     */
    @PutMapping(value = RestApi.UPDATE_PROFILE_FILE + CommonConstants.PATH_ID)
    public JsonNode updateProfileFile(final @PathVariable("id") String id,
                                      @RequestParam("file") MultipartFile file) throws AccessExternalClientException {
        LOGGER.debug("Update {}  profile file with id :{}", id);
        ParameterChecker.checkParameter("profileFile stream is a mandatory parameter: ", file);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return profileInternalService.updateProfileFile(vitamContext, id, file);
    }


    @PutMapping(CommonConstants.PATH_ID)
    public JsonNode updateProfile(final @PathVariable("id") String id, final @RequestBody ProfileDto dto) throws AccessExternalClientException, InvalidParseOperationException {
        LOGGER.debug("Update {} with {}", id, dto);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        LOGGER.debug("context={}", vitamContext);
        return profileInternalService.updateProfile(dto, vitamContext);
    }

    @PostMapping
    public ProfileDto create(@Valid @RequestBody ProfileDto archivalProfile, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("create profile={}", archivalProfile);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        archivalProfile.setTenant(vitamContext.getTenantId());
        return profileInternalService.create(vitamContext, archivalProfile);
    }

    @PostMapping(CommonConstants.PATH_IMPORT)
    public ResponseEntity<JsonNode> importProfile(@RequestParam("fileName") String fileName, @RequestParam("file") MultipartFile file) {
        LOGGER.debug("import profile by a file {}", fileName);
        SafeFileChecker.checkSafeFilePath(file.getOriginalFilename());
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return profileInternalService.importProfile(vitamContext, fileName, file);
    }
}
