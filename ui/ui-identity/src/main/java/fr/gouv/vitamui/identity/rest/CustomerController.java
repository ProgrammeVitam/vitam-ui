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

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.enums.ContentDispositionType;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import fr.gouv.vitamui.iam.common.utils.CustomerDtoEditor;
import fr.gouv.vitamui.iam.common.utils.MapEditor;
import fr.gouv.vitamui.identity.service.CustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Api(tags = "customers")
@RestController
@RequestMapping("${ui-identity.prefix}/customers")
@Consumes("application/json")
@Produces("application/json")
public class CustomerController extends AbstractUiRestController {

    private final CustomerService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerController.class);

    @Autowired
    public CustomerController(final CustomerService service) {
        this.service = service;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(CustomerDto.class, new CustomerDtoEditor());
        binder.registerCustomEditor(Map.class, new MapEditor());
    }

    @ApiOperation(value = "Create entity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto create(@ModelAttribute final CustomerCreationFormData customerCreationFormData) {
        LOGGER.debug("create customer={}", customerCreationFormData);
        return service.create(buildUiHttpContext(), customerCreationFormData);
    }

    @ApiOperation(value = "Get entity")
    @GetMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto getOne(final @PathVariable String id) {
        LOGGER.debug("Get customer={}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return service.getOne(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "Get entities paginated")
    @GetMapping(params = { "page", "size" })
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<CustomerDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam final Optional<String> criteria, @RequestParam final Optional<String> orderBy, @RequestParam final Optional<DirectionDto> direction) {
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        RestUtils.checkCriteria(criteria);
        return service.getAllPaginated(page, size, criteria, orderBy, direction, buildUiHttpContext());
    }

    @ApiOperation(value = "Get all entities")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<CustomerDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("Get all with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return service.getAll(buildUiHttpContext(), criteria);
    }

    @ApiOperation(value = "Check entity exist by criteria")
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(@RequestParam final String criteria) {
        LOGGER.debug("check exist criteria={}", criteria);
        RestUtils.checkCriteria(Optional.of(criteria));
        final boolean exist = service.checkExist(buildUiHttpContext(), criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    @ApiOperation(value = "Update entity")
    @PutMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto update(final @PathVariable("id") String id, @RequestBody final CustomerDto entityDto) {
        LOGGER.debug("update class={}", entityDto.getClass().getName());
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, entityDto.getId()), "The DTO identifier must match the path identifier for update.");
        return service.update(buildUiHttpContext(), entityDto);
    }

    /**
     * Retrieve Authenticated User Customer.
     * @return
     */
    @GetMapping(path = CommonConstants.PATH_ME)
    public CustomerDto getMyCustomer() {
        LOGGER.debug("Get MyCustomer");
        return service.getMyCustomer(buildUiHttpContext());
    }

    @ApiOperation(value = "Patch entity")
    @PatchMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public CustomerDto patch(final @PathVariable("id") String id, @ModelAttribute final CustomerPatchFormData customerPatchFormData) {
        LOGGER.debug("Patch Customer {} with {}", id, customerPatchFormData);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        Assert.isTrue(StringUtils.equals(id, (String) customerPatchFormData.getPartialCustomerDto().get("id")),
                "Unable to patch customer : the DTO id must match the path id");
        return service.patch(buildUiHttpContext(), id, customerPatchFormData);
    }

    @ApiOperation(value = "get history by customer's id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable String id) {
        LOGGER.debug("get logbook for customer with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return service.findHistoryById(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "Get entity logo")
    @GetMapping(CommonConstants.PATH_ID + "/logo")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> getLogo(final @PathVariable String id, final @RequestParam(value = "type") AttachmentType type) {
        LOGGER.debug("Get customer logos={}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        final ResponseEntity<Resource> response = service.getLogo(buildUiHttpContext(), id, type);
        if(HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
            return response;
        } else {
            return RestUtils.buildFileResponse(response, Optional.ofNullable(ContentDispositionType.INLINE), Optional.empty());
        }
    }

    /**
     * Retrieve settings for GPDR.
     *
     * @return
     */
    @GetMapping(path = CommonConstants.GDPR_STATUS)
    public boolean getGdprSettingStatus() {
        LOGGER.debug("Get Gdpr Setting Status");
        return service.getGdprSettingStatus(buildUiHttpContext());
    }
}
