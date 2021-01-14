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
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

/**
 * The controller to check existence, create, read, update and delete the owners.
 *
 *
 */
@RestController
@RequestMapping(RestApi.V1_OWNERS_URL)
@Getter
@Setter
@Api(tags = "owners", value = "Owners Management", description = "Owners Management")
public class OwnerInternalController implements CrudController<OwnerDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OwnerInternalController.class);

    @Autowired
    private OwnerInternalService internalOwnerService;

    /**
     * {@inheritDoc}
     */
    @Override
    @RequestMapping(path = CommonConstants.PATH_CHECK, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(final @RequestParam String criteria) {
        LOGGER.debug("Check exists by criteria {}", criteria);
        RestUtils.checkCriteria(Optional.of(criteria));
        final boolean exist = internalOwnerService.checkExist(criteria);
        return RestUtils.buildBooleanResponse(exist);
    }

    /**
     * GetOne with criteria, item id.
     * @param id
     * @param criteria
     * @return
     */
    @GetMapping(CommonConstants.PATH_ID)
    public OwnerDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria) {
        LOGGER.debug("Get one {} criteria={}", id, criteria);
        ParameterChecker.checkParameter("The identifier is mandatory : ", id);
        return internalOwnerService.getOne(id, criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    public OwnerDto create(@Valid final @RequestBody OwnerDto dto) {
        LOGGER.debug("Create {}", dto);
        return internalOwnerService.create(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping(CommonConstants.PATH_ID)
    public OwnerDto update(final @PathVariable("id") String id, final @Valid @RequestBody OwnerDto dto) {
        LOGGER.debug("Update {} with {}", id, dto);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        return internalOwnerService.update(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PatchMapping(CommonConstants.PATH_ID)
    public OwnerDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return internalOwnerService.patch(partialDto);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) throws VitamClientException {
        LOGGER.debug("get logbook for owner with id :{}", id);
        return internalOwnerService.findHistoryById(id);
    }
}
