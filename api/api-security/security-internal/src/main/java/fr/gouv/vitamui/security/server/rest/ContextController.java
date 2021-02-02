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
package fr.gouv.vitamui.security.server.rest;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import fr.gouv.vitamui.security.common.rest.RestApi;
import fr.gouv.vitamui.security.server.context.service.ContextService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.security.common.rest.RestApi.ADD_TENANT_TO_CONTEXT_PATH;

/**
 * The controller to check existence, create, read, update and delete the application contexts.
 */
@RestController
@RequestMapping(RestApi.V1_CONTEXTS_URL)
@Getter
@Setter
@Api(tags = "contexts", value = "Contexts Management", description = "Contexts Management")
public class ContextController implements CrudController<ContextDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ContextController.class);

    @Autowired
    private ContextService contextService;

    @Override
    @GetMapping
    public List<ContextDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("Get ALL");
        return contextService.getAll(criteria).stream().collect(Collectors.toList());
    }

    @Override
    @RequestMapping(path = CommonConstants.PATH_ID, method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkExist(final @PathVariable("id") String id) {
        LOGGER.debug("Check exists {}", id);
        final List<ContextDto> dto = contextService.getMany(id);
        return RestUtils.buildBooleanResponse(dto != null && !dto.isEmpty());
    }

    @GetMapping(CommonConstants.PATH_ID)
    public ContextDto getOne(final @PathVariable("id") String id, final @RequestParam Optional<String> criteria) {
        LOGGER.debug("Get {} criteria={}", id, criteria);
        return contextService.getOne(id, criteria);
    }

    @PostMapping(value = RestApi.FINDBYCERTIFICATE_PATH)
    public ContextDto findByCertificate(final @Valid @RequestBody String data) {
        LOGGER.info("Request data {} ", data);
        return contextService.findByCertificate(data);
    }

    @Override
    @PostMapping
    public ContextDto create(final @Valid @RequestBody ContextDto dto) {
        LOGGER.debug("Create {}", dto);
        return contextService.create(dto);
    }

    @Override
    @PutMapping(CommonConstants.PATH_ID)
    public ContextDto update(final @PathVariable("id") String id, final @Valid @RequestBody ContextDto dto) {
        LOGGER.debug("Update {} with {}", id, dto);
        Assert.isTrue(StringUtils.equals(id, dto.getId()),
            "The DTO identifier must match the path identifier for update.");
        return contextService.update(dto);
    }

    @PutMapping(ADD_TENANT_TO_CONTEXT_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public ContextDto addTenant(final @PathVariable("id") String id,
        final @PathVariable("tenantIdentifier") Integer tenantIdentifier) {
        LOGGER.debug("Update {} with {}", id, tenantIdentifier);
        final ContextDto contextDto = contextService.addTenant(id, tenantIdentifier);

        return contextDto;
    }

    @Override
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(final @PathVariable("id") String id) {
        LOGGER.debug("Delete {}", id);
        contextService.delete(id);
    }
}
