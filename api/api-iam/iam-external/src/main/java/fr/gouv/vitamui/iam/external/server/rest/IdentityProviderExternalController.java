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
package fr.gouv.vitamui.iam.external.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.common.utils.IamUtils;
import fr.gouv.vitamui.iam.external.server.domain.dto.ProviderPatchType;
import fr.gouv.vitamui.iam.external.server.service.IdentityProviderExternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the identity providers.
 *
 * Endpoints of this controller have cross-customers and cross-tenant capacities. Only instance
 * administrators should be allowed to use this controller.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_PROVIDERS_URL)
@Getter
@Setter
@Api(tags = "identityproviders", value = "Identity Providers Management")
public class IdentityProviderExternalController implements CrudController<IdentityProviderDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IdentityProviderExternalController.class);

    @Autowired
    private IdentityProviderExternalService identityProviderCrudService;

    public IdentityProviderExternalController(final IdentityProviderExternalService identityProviderCrudService) {
        super();
        this.identityProviderCrudService = identityProviderCrudService;
    }

    @GetMapping
    @Secured(ServicesData.ROLE_GET_PROVIDERS)
    public List<IdentityProviderDto> getAll(final Optional<String> criteria, @RequestParam final Optional<String> embedded) {

        EnumUtils.checkValidEnum(ProviderEmbeddedOptions.class, embedded);
        LOGGER.debug("Get all criteria={} embedded={}", criteria, embedded);
        return identityProviderCrudService.getAll(criteria, embedded);
    }

    @GetMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_PROVIDERS)
    public IdentityProviderDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> embedded)
        throws PreconditionFailedException {

        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get {}", id);
        return identityProviderCrudService.getOne(id, embedded);
    }

    @GetMapping(CommonConstants.PATH_ID + "/idpMetadata")
    @Secured(ServicesData.ROLE_GET_PROVIDERS)
    public ResponseEntity<Resource> getIdpMetadataProviderByProviderId(final @PathVariable("id") String id)
        throws PreconditionFailedException, IOException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        final Resource resource = identityProviderCrudService.getMetadataProviderByProviderId(id, ProviderEmbeddedOptions.IDPMETADATA, IamUtils.buildOptionalEmbedded(ProviderEmbeddedOptions.IDPMETADATA));
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + "idpmetadata.xml");
        return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @GetMapping(CommonConstants.PATH_ID + "/spMetadata")
    @Secured(ServicesData.ROLE_GET_PROVIDERS)
    public ResponseEntity<Resource> getSpMetadataProviderByProviderId(final @PathVariable("id") String id)
        throws PreconditionFailedException, IOException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        final Resource resource = identityProviderCrudService.getMetadataProviderByProviderId(id, ProviderEmbeddedOptions.SPMETADATA, IamUtils.buildOptionalEmbedded(ProviderEmbeddedOptions.SPMETADATA));
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + "spmetadata.xml");
        return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(final String criteria) {
        throw new UnsupportedOperationException("checkExist not implemented");
    }

    @PostMapping
    @ApiIgnore
    @ApiOperation(value = "Create entity request to upload the file", produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Secured(ServicesData.ROLE_CREATE_PROVIDERS)
    public IdentityProviderDto create(@RequestPart final String provider, @RequestPart(value = "keystore",required = false) final MultipartFile keystore,
                                      @RequestPart(value = "idpMetadata",required = false) final MultipartFile idpMetadata) throws Exception {
        LOGGER.debug("Create provider: {}", provider);

        if(Objects.nonNull(keystore) && Objects.nonNull(idpMetadata)) {
            SanityChecker.isValidFileName(keystore.getOriginalFilename());
            SanityChecker.isValidFileName(idpMetadata.getOriginalFilename());
        }
        IdentityProviderDto dto = identityProviderCrudService.mapToIdentityProviderDto(keystore, idpMetadata, provider);
        return identityProviderCrudService.create(dto);
    }

    @Override
    public IdentityProviderDto create(final @Valid @RequestBody IdentityProviderDto dto) {
        throw new UnsupportedOperationException("checkExist not implemented");
    }

    @Override
    public IdentityProviderDto update(final @PathVariable("id") String id, final @Valid @RequestBody IdentityProviderDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    @Override
    @ApiOperation(value = "Update partially provider")
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_PROVIDERS)
    public IdentityProviderDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto)
        throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return identityProviderCrudService.patch(partialDto);
    }

    @PatchMapping(value = "/{id}/keystore")
    @ApiOperation(value = "Update keystore provider")
    @ResponseStatus(HttpStatus.OK)
    @ApiIgnore // FXME MDI - Ignore with Failed to execute goal 'convertSwagger2markup': Type of parameter 'provider' must not be blank
    public IdentityProviderDto patchProviderKeystore(final @RequestPart("keystore") MultipartFile keystore, final @RequestPart("provider") String provider,
                                                     final @PathVariable String id) throws IOException {
        LOGGER.debug("Update keystore provider id={} with partialDto={}", id, provider);
        ParameterChecker.checkParameter("Parameters are mandatory : ", keystore, provider, id);
        SanityChecker.isValidFileName(keystore.getOriginalFilename());
        return identityProviderCrudService.patch(VitamUIUtils.convertObjectFromJson(provider, Map.class), keystore, null, id, ProviderPatchType.KEYSTORE);
    }

    @PatchMapping(value = "/{id}/idpMetadata")
    @ApiOperation(value = "Update idpMetadata provider")
    @ResponseStatus(HttpStatus.OK)
    @ApiIgnore // FXME MDI - Ignore with Failed to execute goal 'convertSwagger2markup': Type of parameter 'provider' must not be blank
    public IdentityProviderDto patchProviderIdpMetadata(final @RequestPart("idpMetadata") MultipartFile idpMetadata,
                                                        final @RequestPart("provider") String provider, final @PathVariable String id) throws IOException {
        LOGGER.debug("Update idpMetadata provider id={} with partialDto", id, provider);
        ParameterChecker.checkParameter("Parameters are mandatory : ", provider, idpMetadata, id);
        SanityChecker.isValidFileName(idpMetadata.getOriginalFilename());
        return identityProviderCrudService.patch(VitamUIUtils.convertObjectFromJson(provider, Map.class), null, idpMetadata, id, ProviderPatchType.IDPMETADATA);
    }
}
