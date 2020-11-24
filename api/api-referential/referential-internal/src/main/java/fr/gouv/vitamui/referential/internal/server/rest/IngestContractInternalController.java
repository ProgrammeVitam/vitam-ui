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
package fr.gouv.vitamui.referential.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.internal.server.ingestcontract.IngestContractInternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.INGEST_CONTRACTS_URL)
@Getter
@Setter
public class IngestContractInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestContractInternalController.class);

    @Autowired
    private IngestContractInternalService ingestContractInternalService;

    @Autowired
    private InternalSecurityService securityService;

    @GetMapping()
    public Collection<IngestContractDto> getAll(@RequestParam final Optional<String> criteria) {
        LOGGER.debug("get all ingestContract criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return ingestContractInternalService.getAll(vitamContext);
    }

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<IngestContractDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
                                                                 @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
                                                                 @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities ingestContract page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return ingestContractInternalService.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public IngestContractDto getOne(final @PathVariable("identifier") String identifier) throws UnsupportedEncodingException {
        LOGGER.debug("get ingestContract identifier={} / {}", identifier, URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString()));
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return ingestContractInternalService.getOne(vitamContext, URLDecoder.decode(identifier, StandardCharsets.UTF_8.toString()));
    }

    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> checkExist(@RequestBody IngestContractDto ingestContractDto, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("check exist ingestContract={}", ingestContractDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        ingestContractDto.setTenant(tenant);
        final boolean exist = ingestContractInternalService.check(vitamContext, ingestContractDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @PostMapping
    public IngestContractDto create(@Valid @RequestBody IngestContractDto ingestContractDto, @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant) {
        LOGGER.debug("create ingestContract={}", ingestContractDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        ingestContractDto.setTenant(tenant);
        return ingestContractInternalService.create(vitamContext, ingestContractDto);
    }

    /* TODO: Implement this */
    @PutMapping(CommonConstants.PATH_ID)
    public IngestContractDto update(final @PathVariable("id") String id, final @Valid @RequestBody IngestContractDto dto) {
        /*LOGGER.debug("Update {} with {}", id, dto);
        Assert.isTrue(StringUtils.equals(id, dto.getId()), "The DTO identifier must match the path identifier for update.");
        return ingestContractInternalService.update(dto);*/
        return null;
    }

    @PatchMapping(CommonConstants.PATH_ID)
    public IngestContractDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", id, partialDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "The DTO identifier must match the path identifier for update.");
        return ingestContractInternalService.patch(vitamContext, partialDto);
    }

    @GetMapping("/{id}/history")
    public JsonNode findHistoryById(final @PathVariable("id") String id) throws VitamClientException {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        LOGGER.debug("get logbook for ingestContract with id :{}", id);
        return ingestContractInternalService.findHistoryByIdentifier(vitamContext, id);
    }
}
