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

import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.referential.common.dto.AgencyDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.referential.service.UnitService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("${ui-referential.prefix}" + RestApi.SEARCH_PATH)
@Consumes("application/json")
@Produces("application/json")
public class UnitController extends AbstractUiRestController {

    private static final VitamUILogger LOG = VitamUILoggerFactory.getInstance(UnitController.class);

    private final UnitService searchService;

    @Autowired
    public UnitController(final UnitService searchService) {
        this.searchService = searchService;
    }
    
    @ApiOperation(value = "search unit by id")
    @GetMapping(RestApi.UNITS_PATH + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public VitamUISearchResponseDto searchUnitById(@PathVariable final String id) {
        LOG.debug("searchUnits by id = {}", id);
        return searchService.searchById(id, buildUiHttpContext());
    }

    @ApiOperation(value = "find units by custom dsl")
    @PostMapping({RestApi.UNITS_PATH + RestApi.DSL_PATH, RestApi.UNITS_PATH + RestApi.DSL_PATH + CommonConstants.PATH_ID})
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public JsonNode findByDsl(final @PathVariable Optional<String> id, @RequestBody final JsonNode dsl) {
        LOG.debug("searchUnits by dsl = {}", dsl);
        LOG.debug("id = {}", id);
        return searchService.findByDsl(id, dsl, buildUiHttpContext());
    }
    
    @ApiOperation(value = "find unit objects by custom dsl")
    @PostMapping(RestApi.UNITS_PATH + CommonConstants.PATH_ID + RestApi.OBJECTS_PATH)
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public JsonNode findObjectMetadataById(final @PathVariable String id, @RequestBody final JsonNode dsl) {
        LOG.debug("searchUnitObjects by id {} and dsl = {}", id, dsl);
        return searchService.findObjectMetadataById(id, dsl, buildUiHttpContext());
    }

    @ApiOperation(value = "Get filing plan")
    @GetMapping(RestApi.FILING_PLAN_PATH)
    @ResponseStatus(HttpStatus.OK)
    public VitamUISearchResponseDto findFilingPlan() {
        LOG.debug("find filing plan");
        return searchService.findFilingPlan(buildUiHttpContext());
    }
}
