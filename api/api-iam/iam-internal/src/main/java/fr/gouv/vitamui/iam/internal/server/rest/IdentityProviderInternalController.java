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
package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
 *
 */
@RestController
@RequestMapping(RestApi.V1_PROVIDERS_URL)
@Getter
@Setter
@Api(tags = "identityproviders", value = "Identity Providers Management", description = "Identity Providers Management")
public class IdentityProviderInternalController implements CrudController<IdentityProviderDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IdentityProviderInternalController.class);

    @Autowired
    private IdentityProviderInternalService internalIdentityProviderService;

    /**
     * Get All with criteria and embedded request.
     * @param criteria
     * @param embedded
     * @return
     */
    @GetMapping
    public List<IdentityProviderDto> getAll(final Optional<String> criteria, @RequestParam final Optional<String> embedded) {
        LOGGER.debug("Get all criteria={}, embedded={}", criteria, embedded);
        RestUtils.checkCriteria(criteria);
        EnumUtils.checkValidEnum(ProviderEmbeddedOptions.class, embedded);
        return internalIdentityProviderService.getAll(criteria, embedded);
    }

    /**
     * GetOne with criteria, item id and embedded request.
     * @param id
     * @param criteria
     * @param embedded
     * @return
     */
    @GetMapping(CommonConstants.PATH_ID)
    public IdentityProviderDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria,
            final @RequestParam Optional<String> embedded) {
        LOGGER.debug("Get {} criteria={} embedded={}", id, criteria, embedded);
        EnumUtils.checkValidEnum(ProviderEmbeddedOptions.class, embedded);
        return internalIdentityProviderService.getOne(id, criteria, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(final String criteria) {
        throw new UnsupportedOperationException("checkExist not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    public IdentityProviderDto create(final @Valid @RequestBody IdentityProviderDto dto) {
        LOGGER.debug("Create {}", dto);
        return internalIdentityProviderService.create(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityProviderDto update(final @PathVariable("id") String id, final @Valid @RequestBody IdentityProviderDto dto) {
        throw new UnsupportedOperationException("update not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    public IdentityProviderDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return internalIdentityProviderService.patch(partialDto);
    }
}
