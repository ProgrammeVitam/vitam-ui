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
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.common.utils.CustomerDtoEditor;
import fr.gouv.vitamui.iam.common.utils.MapEditor;
import fr.gouv.vitamui.iam.external.server.service.CustomerExternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
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
 * The controller to check existence, create, read, update and delete the customers.
 *
 * Endpoints of this controller have cross-customers and cross-tenant capacities. Only instance
 * administrators should be allowed to use this controller.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_CUSTOMERS_URL)
@Getter
@Setter
@Api(tags = "customers", value = "Customers Management", description = "Customers Management")
public class CustomerExternalController implements CrudController<CustomerDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerExternalController.class);

    private final CustomerExternalService customerExternalService;

    @Autowired
    public CustomerExternalController(final CustomerExternalService customerExternalService) {
        this.customerExternalService = customerExternalService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(CustomerDto.class, new CustomerDtoEditor());
        binder.registerCustomEditor(Map.class, new MapEditor());
    }

    @Override
    @GetMapping
    @Secured(ServicesData.ROLE_GET_CUSTOMERS)
    public Collection<CustomerDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all customer criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return customerExternalService.getAll(criteria);
    }

    @Override
    @Secured(ServicesData.ROLE_GET_CUSTOMERS)
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        RestUtils.checkCriteria(Optional.of(criteria));
        LOGGER.debug("check exist by criteria={}", criteria);
        final boolean exist = customerExternalService.checkExists(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Override
    @Secured({ ServicesData.ROLE_GET_CUSTOMERS })
    @GetMapping(CommonConstants.PATH_ID)
    public CustomerDto getOne(final @PathVariable("id") String id) {
        LOGGER.debug("Get {}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : " , id);
        return customerExternalService.getOne(id);
    }

    /**
     * Retrieve Authenticated User Customer.
     * Everyone has a right to get his customer informations.
     * @return
     */
    @GetMapping(path = CommonConstants.PATH_ME)
    public CustomerDto getMyCustomer() {
        return customerExternalService.getMyCustomer();
    }

    @Secured(ServicesData.ROLE_GET_CUSTOMERS)
    @GetMapping(params = { "page", "size" })
    public PaginatedValuesDto<CustomerDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        RestUtils.checkCriteria(criteria);
        return customerExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_CREATE_CUSTOMERS)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto create(@ModelAttribute final CustomerCreationFormData customerData) {
        LOGGER.debug("Create {}", customerData);
        return customerExternalService.create(customerData);
    }

    @Override
    @Secured(ServicesData.ROLE_UPDATE_CUSTOMERS)
    @PutMapping(CommonConstants.PATH_ID)
    public CustomerDto update(final @PathVariable("id") String id, final @Valid @RequestBody CustomerDto dto) {
        LOGGER.debug("Update {} with {}", id, dto);
        ParameterChecker.checkParameter("Identifier is mandatory : " , id);
        SanityChecker.check(id);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        return customerExternalService.update(dto);
    }

    @ApiOperation(value = "Patch customer entity")
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_CUSTOMERS)
    public CustomerDto patch(final @PathVariable("id") String id,
                             @ModelAttribute final CustomerPatchFormData customerData) {
        LOGGER.debug("Patch customer with {}", customerData.getPartialCustomerDto().get("id"));
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.check(id);
        return customerExternalService.patch(customerData);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) {
        LOGGER.debug("get logbook for customer with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return customerExternalService.findHistoryById(id);
    }

    @Override
    public CustomerDto create(final CustomerDto dto) {
        throw new NotImplementedException("Method is not implemented");
    }

    @Override
    public CustomerDto patch(final String id, final Map<String, Object> partialCustomerDto) {
        throw new NotImplementedException("Method is not implemented");
    }

    @ApiOperation(value = "Get entity logo")
    @GetMapping(CommonConstants.PATH_ID + "/logo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> getLogo(final @PathVariable String id, final @RequestParam(value = "type") AttachmentType type) {
        LOGGER.debug("get logo for customer with id :{}, type : {}", id, type);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.check(id);
        final ResponseEntity<Resource> response = customerExternalService.getLogo(id, type);
        return RestUtils.buildFileResponse(response, Optional.empty(), Optional.empty());
    }

    /**
     * Retrieve settings for GPDR.
     *
     * @return boolean
     */
    @ApiOperation(value = "Get Gdpr Setting Status")
    @GetMapping(CommonConstants.GDPR_STATUS)
    @ResponseStatus(HttpStatus.OK)
    public boolean getGdprSettingStatus() {
        LOGGER.debug("Get Gdpr Setting Status");
        return customerExternalService.getGdprSettingStatus();
    }
}
