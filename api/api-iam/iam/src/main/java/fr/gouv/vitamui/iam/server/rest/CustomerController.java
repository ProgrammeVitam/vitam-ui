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
package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.enums.ContentDispositionType;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsCommonResponseDto;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.common.utils.CustomerDtoEditor;
import fr.gouv.vitamui.iam.common.utils.MapEditor;
import fr.gouv.vitamui.iam.server.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Customers", description = "Customers Management")
public class CustomerController implements CrudController<CustomerDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    @Autowired
    public CustomerController(final CustomerService customerService) {
        this.customerService = customerService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(CustomerDto.class, new CustomerDtoEditor());
        binder.registerCustomEditor(Map.class, new MapEditor());
    }

    @Override
    @GetMapping
    @Operation(operationId = "getAll", summary = "Get all customer objects")
    @Secured(ServicesData.ROLE_GET_CUSTOMERS)
    public Collection<CustomerDto> getAll(final Optional<String> criteria) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("Get all with criteria={}", criteria);
        return customerService.getAll(criteria);
    }

    @Override
    @Operation(operationId = "checkExist", summary = "Check that a customer exists")
    @Secured(ServicesData.ROLE_GET_CUSTOMERS)
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        SanityChecker.sanitizeCriteria(Optional.of(criteria));
        LOGGER.debug("check exist by criteria={}", criteria);
        final boolean exist = customerService.checkExist(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Override
    @Operation(operationId = "getOne", summary = "Get a customer by its id")
    @Secured({ ServicesData.ROLE_GET_CUSTOMERS })
    @GetMapping(CommonConstants.PATH_ID)
    public CustomerDto getOne(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.checkSecureParameter(id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        LOGGER.debug("Get {}", id);
        return customerService.getOne(id);
    }

    /**
     * Retrieve Authenticated User Customer.
     * Everyone has a right to get his customer informations.
     * @return
     */
    @Operation(operationId = "getMyCustomer", summary = "Get the customer for the authenticated user")
    @GetMapping(path = CommonConstants.PATH_ME)
    public CustomerDto getMyCustomer() {
        return customerService.getMyCustomer();
    }

    @Secured(ServicesData.ROLE_GET_CUSTOMERS)
    @GetMapping(value = "/paginated", params = { "page", "size" })
    @Operation(operationId = "getAllPaginated", summary = "Get all customers using pagination")
    public PaginatedValuesDto<CustomerDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(criteria);
        SanityChecker.checkSecureParameter(String.valueOf(page), String.valueOf(size));
        direction.ifPresent(SanityChecker::sanitizeCriteria);
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            criteria,
            orderBy,
            direction
        );
        return customerService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Operation(operationId = "create", summary = "Create a customer")
    @Secured(ServicesData.ROLE_CREATE_CUSTOMERS)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto create(@ModelAttribute final CustomerCreationFormData customerData)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(customerData);
        LOGGER.debug("Create {}", customerData);
        return customerService.create(customerData);
    }

    @Override
    @Operation(operationId = "update", summary = "Update a customer")
    @Secured(ServicesData.ROLE_UPDATE_CUSTOMERS)
    @PutMapping(CommonConstants.PATH_ID)
    public CustomerDto update(final @PathVariable("id") String id, final @Valid @RequestBody CustomerDto dto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(dto);
        LOGGER.debug("Update {} with {}", id, dto);
        Assert.isTrue(
            StringUtils.equals(id, dto.getId()),
            "The DTO identifier must match the path identifier for update."
        );
        return customerService.update(dto);
    }

    @Operation(operationId = "patch", summary = "Patch a customer entity")
    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_CUSTOMERS)
    public CustomerDto patch(
        final @PathVariable("id") String id,
        @ModelAttribute final CustomerPatchFormData customerData
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(customerData.getPartialCustomerDto());
        Assert.isTrue(
            StringUtils.equals(id, (String) customerData.getPartialCustomerDto().get("id")),
            "The DTO identifier must match the path identifier for update."
        );
        LOGGER.debug("Patch customer with {}", customerData.getPartialCustomerDto().get("id"));
        return customerService.patch(customerData);
    }

    @Operation(operationId = "findHistoryById", summary = "Find history for a customer by its id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsCommonResponseDto findHistoryById(final @PathVariable("id") String id)
        throws VitamClientException {
        LOGGER.debug("get logbook for customer with id :{}", id);
        SanityChecker.checkSecureParameter(id);
        return customerService.findHistoryById(id);
    }

    @Override
    public CustomerDto create(final CustomerDto dto) {
        throw new NotImplementedException("Method is not implemented");
    }

    @Override
    public CustomerDto patch(final String id, final Map<String, Object> partialCustomerDto) {
        throw new NotImplementedException("Method is not implemented");
    }

    @Operation(operationId = "getLogo", summary = "Get customer logo")
    @GetMapping(CommonConstants.PATH_ID + "/logo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> getLogo(
        final @PathVariable String id,
        final @RequestParam(value = "type") AttachmentType type
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logo for customer with id :{}, type : {}", id, type);
        final ResponseEntity<Resource> response = customerService.getLogo(id, type);
        if (HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
            return response;
        }
        return RestUtils.buildFileResponse(response, Optional.of(ContentDispositionType.INLINE), Optional.empty());
    }

    /**
     * Retrieve settings for GPDR.
     *
     * @return boolean
     */
    @Operation(operationId = "getGdprSettingStatus", summary = "Get Gdpr Setting Status")
    @GetMapping(CommonConstants.GDPR_STATUS)
    @ResponseStatus(HttpStatus.OK)
    public boolean getGdprSettingStatus() {
        LOGGER.debug("Get Gdpr Setting Status");
        return customerService.getGdprSettingStatus();
    }
}
