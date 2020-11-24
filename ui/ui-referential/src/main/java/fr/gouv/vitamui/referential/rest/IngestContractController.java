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
package fr.gouv.vitamui.referential.rest;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.service.IngestContractService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Api(tags = "ingestcontract")
@RestController
@RequestMapping("${ui-referential.prefix}/ingestcontract")
@Consumes("application/json")
@Produces("application/json")
public class IngestContractController extends AbstractUiRestController {

    protected final IngestContractService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestContractController.class);

    @Autowired
    public IngestContractController(final IngestContractService service) {
        this.service = service;
    }

    @ApiOperation(value = "Get entity")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<IngestContractDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("Get all with criteria={}", criteria);
        RestUtils.checkCriteria(criteria);
        return service.getAll(buildUiHttpContext(), criteria);
    }

    @ApiOperation(value = "Get entities paginated")
    @GetMapping(params = { "page", "size" })
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<IngestContractDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
                                                                 @RequestParam final Optional<String> criteria, @RequestParam final Optional<String> orderBy, @RequestParam final Optional<DirectionDto> direction) {
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        return service.getAllPaginated(page, size, criteria, orderBy, direction, buildUiHttpContext());
    }

    @ApiOperation(value = "Get ingest contract by ID")
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    @ResponseStatus(HttpStatus.OK)
    public IngestContractDto getById(final @PathVariable("identifier") String identifier) throws UnsupportedEncodingException {
        LOGGER.debug("getById {} / {}", identifier, URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString()));
        return service.getOne(buildUiHttpContext(), URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString()));
    }

    /**
     * Check ingest contract.
     *
     * @param ingestContractDto
     * @return
     */
    @ApiOperation(value = "Check ability to create entity")
    @PostMapping(path = CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(@RequestBody  IngestContractDto ingestContractDto) {
        LOGGER.debug("check ability to create ingestContract={}", ingestContractDto);
        final boolean exist = service.check(buildUiHttpContext(), ingestContractDto);
        LOGGER.debug("response value={}" + exist);
        return RestUtils.buildBooleanResponse(exist);
    }

    @ApiOperation(value = "Create entity")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngestContractDto create(@Valid @RequestBody IngestContractDto ingestContractDto) {
        LOGGER.debug("create ingestContract={}", ingestContractDto);
        return service.create(buildUiHttpContext(), ingestContractDto);
    }

    @ApiOperation(value = "Patch entity")
    @PatchMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public IngestContractDto patch(final @PathVariable("id") String id, @RequestBody final Map<String, Object> partialDto) {
        LOGGER.debug("Patch ingestContract {} with {}", id, partialDto);
        Assert.isTrue(StringUtils.equals(id, (String) partialDto.get("id")), "Unable to patch ingestContract : the DTO id must match the path id.");
        return service.patch(buildUiHttpContext(), partialDto, id);
    }

    @ApiOperation(value = "get history by ingestContract's id")
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public LogbookOperationsResponseDto findHistoryById(final @PathVariable String id) {
        LOGGER.debug("get logbook for ingestContract with id :{}", id);
        return service.findHistoryById(buildUiHttpContext(), id);
    }
}
