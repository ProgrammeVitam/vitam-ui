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
package fr.gouv.vitamui.iam.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
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
import fr.gouv.vitamui.iam.internal.server.customer.service.CustomerInternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 *
 */
@RestController
@RequestMapping(RestApi.V1_CUSTOMERS_URL)
@Getter
@Setter
@Api(tags = "customers", value = "Customers Management", description = "Customers Management")
public class CustomerInternalController implements CrudController<CustomerDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerInternalController.class);

    private CustomerInternalService internalCustomerService;

    @Autowired
    public CustomerInternalController(final CustomerInternalService internalCustomerService) {
        this.internalCustomerService = internalCustomerService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(CustomerDto.class, new CustomerDtoEditor());
        binder.registerCustomEditor(Map.class, new MapEditor());
    }

    /**
     * Get All with criteria.
     * @param criteria
     * @return
     */
    @Override
    @GetMapping
    public Collection<CustomerDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("Get all with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return internalCustomerService.getAll(criteria);
    }

    /**
     * Get paginated items with criteria.
     * @param page
     * @param size
     * @param criteria
     * @param orderBy
     * @param direction
     * @return
     */
    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<CustomerDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        RestUtils.checkCriteria(criteria);
        return internalCustomerService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    /**
     * GetOne with criteria and item id.
     * @param id
     * @param criteria
     * @return
     */
    @GetMapping(CommonConstants.PATH_ID)
    public CustomerDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria) {
        LOGGER.debug("Get One {}, criteria={}", id, criteria);
        ParameterChecker.checkParameter("The identifier is mandatory : ", id);
        RestUtils.checkCriteria(criteria);
        return internalCustomerService.getOne(id, criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        LOGGER.debug("check exist criteria={}", criteria);
        RestUtils.checkCriteria(Optional.of(criteria));
        final boolean exist = internalCustomerService.checkExist(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    /**
     * Retrieve Authenticated User Customer.
     * Everyone has a right to get his customer informations.
     * @return
     */
    @GetMapping(path = CommonConstants.PATH_ME)
    public CustomerDto getMyCustomer() {
        LOGGER.debug("Get MyCustomer");
        return internalCustomerService.getMyCustomer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping(CommonConstants.PATH_ID)
    public CustomerDto update(final @PathVariable("id") String id, final @Valid @RequestBody CustomerDto dto) {
        LOGGER.debug("Update {} with {}", id, dto);
        ParameterChecker.checkParameter("The identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        return internalCustomerService.update(dto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto create(@ModelAttribute final CustomerCreationFormData customerData) {
        LOGGER.debug("Create {}", customerData);
        return internalCustomerService.create(customerData);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    public CustomerDto patch(final @PathVariable("id") String id,
                             @ModelAttribute final CustomerPatchFormData customerData) {
        LOGGER.debug("Patch customer {}", customerData);
        ParameterChecker.checkParameter("The identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, (String) customerData.getPartialCustomerDto().get("id")), "The DTO identifier must match the path identifier for update.");
        return internalCustomerService.patch(customerData);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) throws VitamClientException {
        LOGGER.debug("get logbook for customer with id :{}", id);
        return internalCustomerService.findHistoryById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CustomerDto create(final @Valid @RequestBody CustomerDto dto) {
        throw new NotImplementedException("Method is not implemented");
    }

    @ApiOperation(value = "Get entity logo")
    @GetMapping(CommonConstants.PATH_ID + "/logo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> getLogo(final @PathVariable String id, final @RequestParam(value = "type") AttachmentType type) {
        LOGGER.debug("get logo for customer with id :{}, type : {}", id, type);
        return internalCustomerService.getLogo(id, type);
    }

    /**
     * get GDPR status (readonly/editable)
     *
     * @return (readonly / editable)
     */
    @GetMapping(path = CommonConstants.GDPR_STATUS)
    public boolean getGdprSettingStatus() {
        LOGGER.debug("Get Gdpr Setting Status");
        return internalCustomerService.getGdprSettingStatus();
    }

}
