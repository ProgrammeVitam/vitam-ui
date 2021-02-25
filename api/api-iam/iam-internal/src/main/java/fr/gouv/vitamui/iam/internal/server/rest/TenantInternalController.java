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

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 *
 */
@RestController
@RequestMapping(RestApi.V1_TENANTS_URL)
@Getter
@Setter
@Api(tags = "tenants", value = "Tenants Management", description = "Tenants Management")
public class TenantInternalController implements CrudController<TenantDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantInternalController.class);

    private TenantInternalService internalTenantService;

    @Autowired
    public TenantInternalController(final TenantInternalService internalTenantService) {
        this.internalTenantService = internalTenantService;
    }

    /**
     * {@inheritDoc}
     */
    @GetMapping
    @Override
    public Collection<TenantDto> getAll(@RequestParam final Optional<String> criteria) {
        LOGGER.debug("Get all criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return internalTenantService.getAll(criteria);
    }

    /**
     * GetOne with criteria, item id.
     * @param id
     * @param criteria
     * @return
     */
    @GetMapping(CommonConstants.PATH_ID)
    public TenantDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria) {
        LOGGER.debug("Get {}, criteria={}", id, criteria);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        RestUtils.checkCriteria(criteria);
        return internalTenantService.getOne(id, criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        RestUtils.checkCriteria(Optional.of(criteria));
        LOGGER.debug("checkExist criteria={}", criteria);
        final boolean exist = internalTenantService.checkExist(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    /**
     * {@inheritDoc}
     */
    @PostMapping
    @Override
    public TenantDto create(final @Valid @RequestBody TenantDto dto) {
        LOGGER.debug("Create {}", dto);
        return internalTenantService.create(dto);
    }

    /**
     * {@inheritDoc}
     */
    @PutMapping(CommonConstants.PATH_ID)
    @Override
    public TenantDto update(final @PathVariable("id") String id, final @Valid @RequestBody TenantDto dto) {
        LOGGER.debug("Update {} with {}", id, dto);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        return internalTenantService.update(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public TenantDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch tenant {} with {}", id, partialDto);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch tenant : the DTO id must match the path id");
        return internalTenantService.patch(partialDto);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) throws VitamClientException {
        LOGGER.debug("get logbook for tenant with id :{}", id);
        return internalTenantService.findHistoryById(id);
    }
}
