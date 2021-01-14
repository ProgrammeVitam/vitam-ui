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

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.IdentityProviderExternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
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
@Api(tags = "identityproviders", value = "Identity Providers Management", description = "Identity Providers Management")
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
        LOGGER.debug("Get all criteria={} embedded={}", criteria, embedded);
        RestUtils.checkCriteria(criteria);
        EnumUtils.checkValidEnum(ProviderEmbeddedOptions.class, embedded);
        return identityProviderCrudService.getAll(criteria, embedded);
    }

    @GetMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_PROVIDERS)
    public IdentityProviderDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> embedded) {
        LOGGER.debug("Get {}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return identityProviderCrudService.getOne(id, embedded);
    }

    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(final String criteria) {
        throw new UnsupportedOperationException("checkExist not implemented");
    }

    @Override
    @PostMapping
    @Secured(ServicesData.ROLE_CREATE_PROVIDERS)
    public IdentityProviderDto create(final @Valid @RequestBody IdentityProviderDto dto) {
        LOGGER.debug("Create {}", dto);
        return identityProviderCrudService.create(dto);
    }

    @Override
    public IdentityProviderDto update(final @PathVariable("id") String id, final @Valid @RequestBody IdentityProviderDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_PROVIDERS)
    public IdentityProviderDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return identityProviderCrudService.patch(partialDto);
    }
}
