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

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.domain.AccessionRegisterSearchDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryDto;
import fr.gouv.vitamui.referential.service.AccessionRegisterDetailService;
import fr.gouv.vitamui.referential.service.AccessionRegisterSummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.Optional;

@Api(tags = "accession-register")
@RestController
@RequestMapping("${ui-referential.prefix}/accession-register")
@Consumes("application/json")
@Produces("application/json")
public class AccessionRegisterController extends AbstractUiRestController {

    protected final AccessionRegisterSummaryService summaryService;
    protected final AccessionRegisterDetailService detailsService;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AccessionRegisterController.class);

    public AccessionRegisterController(
        AccessionRegisterSummaryService summaryService,
        AccessionRegisterDetailService detailsService
    ) {
        this.summaryService = summaryService;
        this.detailsService = detailsService;
    }

    @ApiOperation(value = "Get accession register summary entities")
    @GetMapping("/summary")
    @ResponseStatus(HttpStatus.OK)
    public Collection<AccessionRegisterSummaryDto> getAll(final Optional<String> criteria)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("Get all with criteria={}", criteria);
        return summaryService.getAll(buildUiHttpContext(), criteria);
    }

    @ApiOperation(value = "Get accession register details entities paginated")
    @GetMapping(value = "/details", params = { "page", "size" })
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<AccessionRegisterDetailDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam final Optional<String> criteria,
        @RequestParam final Optional<String> orderBy,
        @RequestParam final Optional<DirectionDto> direction
    ) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(criteria);
        if (orderBy.isPresent()) {
            SanityChecker.checkSecureParameter(orderBy.get());
        }
        LOGGER.debug(
            "getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            criteria,
            orderBy,
            direction
        );
        return detailsService.getAllPaginated(page, size, criteria, orderBy, direction, buildUiHttpContext());
    }

    @ApiOperation(value = "Export accession register by criteria into csv format")
    @PostMapping("/details/export-csv")
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(
        @RequestBody final AccessionRegisterSearchDto searchQuery
    ) throws InvalidParseOperationException, PreconditionFailedException {
        LOGGER.debug("Export accession register by criteria into csv format = {}", searchQuery);
        Resource exportedCsvResult = detailsService
            .exportAccessionRegisterCsv(searchQuery, buildUiHttpContext())
            .getBody();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment")
            .body(exportedCsvResult);
    }
}
