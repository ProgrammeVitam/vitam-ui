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

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.TenantExternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the tenants.
 *
 * Endpoints of this controller have cross-customers  only if user has a GET_ALL_TENANTS role
 * and cross-tenant capacities when user has the GET_TENANTS role.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_TENANTS_URL)
@Getter
@Setter
@Api(tags = "tenants", value = "Tenants Management", description = "Tenants Management")
public class TenantExternalController implements CrudController<TenantDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantExternalController.class);

    private final TenantExternalService tenantExternalService;

    @Autowired
    public TenantExternalController(final TenantExternalService tenantExternalService) {
        this.tenantExternalService = tenantExternalService;
    }

    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    @Secured({ ServicesData.ROLE_GET_ALL_TENANTS })
    public ResponseEntity<Void> checkExist(final @RequestParam String criteria) {
        RestUtils.checkCriteria(Optional.of(criteria));
        LOGGER.debug("checkExist criteria={}", criteria);
        final boolean exist = tenantExternalService.checkExists(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @GetMapping(CommonConstants.PATH_ID)
    @Secured({ ServicesData.ROLE_GET_TENANTS, ServicesData.ROLE_GET_ALL_TENANTS })
    @Override
    public TenantDto getOne(final @PathVariable("id") String id) {
        LOGGER.debug("Get {}", id);
        SanityChecker.check(id);
        return tenantExternalService.getOne(id);
    }

    @PostMapping
    @Secured(ServicesData.ROLE_CREATE_TENANTS)
    @Override
    public TenantDto create(final @Valid @RequestBody TenantDto dto) {
        LOGGER.debug("Create {}", dto);
        return tenantExternalService.create(dto);
    }

    @PutMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_TENANTS)
    @Override
    public TenantDto update(final @PathVariable("id") String id, final @Valid @RequestBody TenantDto dto) {
        LOGGER.debug("Update {} with {}", id, dto);
        SanityChecker.check(id);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        return tenantExternalService.update(dto);
    }

    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    @Secured(ServicesData.ROLE_UPDATE_TENANTS)
    public TenantDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch tenant {} with {}", id, partialDto);
        SanityChecker.check(id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch tenant : the DTO id must match the path id");
        return tenantExternalService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_GET_TENANTS)
    @GetMapping
    @Override
    public Collection<TenantDto> getAll(@RequestParam final Optional<String> criteria) {
        LOGGER.debug("Get all criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return tenantExternalService.getAll(criteria);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for tenant with id :{}", id);
        SanityChecker.check(id);
        return tenantExternalService.findHistoryById(id);
    }
}
