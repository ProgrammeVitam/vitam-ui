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
package fr.gouv.vitamui.identity.rest;

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.utils.IamUtils;
import fr.gouv.vitamui.identity.domain.dto.ProviderPatchType;
import fr.gouv.vitamui.identity.service.ProviderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Api(tags = "providers")
@RestController
@RequestMapping("${ui-identity.prefix}/providers")
@Consumes("application/json")
@Produces("application/json")
public class ProviderController extends AbstractUiRestController {

    private final ProviderService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProviderController.class);

    @Autowired
    public ProviderController(final ProviderService service) {
        this.service = service;
    }

    @ApiOperation(value = "Get entity")
    @GetMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public IdentityProviderDto getOne(final @PathVariable String id,
            @ApiParam(defaultValue = "KEYSTORE,IDPMETADATA") @RequestParam final Optional<String> embedded) {
        LOGGER.debug("Get provider={}, embedded={}", id, embedded);
        EnumUtils.checkValidEnum(ProviderEmbeddedOptions.class, embedded);
        return service.getOne(buildUiHttpContext(), id, embedded);
    }

    @ApiOperation(value = "Get all entities")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<IdentityProviderDto> getAll(final Optional<String> criteria, @RequestParam final Optional<String> embedded) {
        LOGGER.debug("Get all with criteria={}, embedded={}", criteria, embedded);
        RestUtils.checkCriteria(criteria);
        EnumUtils.checkValidEnum(ProviderEmbeddedOptions.class, embedded);
        return service.getAll(buildUiHttpContext(), criteria, embedded);
    }

    /**
     * Retrieve an identity provider metadata.
     * The tenant identifier is provided as a query param instead of a header because the link to retrieve the file must be self sufficient.
     * @param tenantId
     * @param id
     * @return
     */
    @ApiOperation(value = "Get metadata provider")
    @GetMapping("/{id}/idpMetadata")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> getIdpMetadataProviderByProviderId(@RequestParam(required = true) final Integer tenantId, final @PathVariable String id) {
        LOGGER.debug("Get keystore provider ={}");
        ParameterChecker.checkParameter("Parameters are mandatory : ", tenantId, id);
        final IdentityProviderDto dto = service.getOne(buildUiHttpContext(tenantId), id, IamUtils.buildOptionalEmbedded(ProviderEmbeddedOptions.IDPMETADATA));
        final HttpHeaders headers = new HttpHeaders();
        final ByteArrayResource resource = new ByteArrayResource(dto.getIdpMetadata().getBytes());
        headers.add("Content-Disposition", "attachment; filename=" + "idpmetadata.xml");
        return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @PostMapping
    @ApiOperation(value = "Create entity request to upload the file", produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiIgnore // FXME MDI - Ignore with Failed to execute goal 'convertSwagger2markup': Type of parameter 'keystore' must not be blank
    public IdentityProviderDto create(@RequestPart final String provider, @RequestPart("keystore") final MultipartFile keystore,
            @RequestPart("idpMetadata") final MultipartFile idpMetadata) throws Exception {
        LOGGER.debug("Create provider");
        ParameterChecker.checkParameter("Parameters are mandatory : ", keystore, idpMetadata);
        SanityChecker.isValidFileName(keystore.getOriginalFilename());
        SanityChecker.isValidFileName(idpMetadata.getOriginalFilename());
        return service.create(buildUiHttpContext(), keystore, idpMetadata, provider);
    }

    @PatchMapping(value = CommonConstants.PATH_ID)
    @ApiOperation(value = "Update partially provider")
    @ResponseStatus(HttpStatus.OK)
    public IdentityProviderDto patchProvider(final @RequestBody Map<String, Object> provider, final @PathVariable String id) {
        LOGGER.debug("Update partially provider id={} with partialDto={}", id, provider);
        ParameterChecker.checkParameter("Parameters are mandatory : ", provider, id);
        return service.patch(buildUiHttpContext(), provider, null, null, id, ProviderPatchType.JSON);
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
        return service.patch(buildUiHttpContext(), VitamUIUtils.convertObjectFromJson(provider, Map.class), keystore, null, id, ProviderPatchType.KEYSTORE);
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
        return service.patch(buildUiHttpContext(), VitamUIUtils.convertObjectFromJson(provider, Map.class), null, idpMetadata, id, ProviderPatchType.IDPMETADATA);
    }
}
