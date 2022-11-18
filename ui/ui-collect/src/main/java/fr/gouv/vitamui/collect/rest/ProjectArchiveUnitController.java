/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.collect.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.collect.service.SearchCriteriaHistoryService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.List;

import static fr.gouv.vitamui.collect.common.rest.RestApi.ARCHIVE_UNITS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.PROJECTS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.SEARCH_CRITERIA_HISTORY;
import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ID;

@Api(tags = "Collect")
@RequestMapping("${ui-collect.prefix}/" + PROJECTS + ARCHIVE_UNITS)
@RestController
@Consumes("application/json")
@Produces("application/json")
public class ProjectArchiveUnitController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectArchiveUnitController.class);

    private final SearchCriteriaHistoryService searchCriteriaHistoryService;

    @Autowired
    public ProjectArchiveUnitController(
        final SearchCriteriaHistoryService searchCriteriaHistoryService) {
        this.searchCriteriaHistoryService = searchCriteriaHistoryService;
    }

    @ApiOperation(value = "Create search criteria history for collect")
    @PostMapping(SEARCH_CRITERIA_HISTORY)
    @ResponseStatus(HttpStatus.CREATED)
    public SearchCriteriaHistoryDto create(@RequestBody final SearchCriteriaHistoryDto entityDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(entityDto);
        LOGGER.debug("create class={}", entityDto.getClass().getName());
        return searchCriteriaHistoryService.create(buildUiHttpContext(), entityDto);
    }

    @ApiOperation(value = "Get the search criteria history")
    @GetMapping(SEARCH_CRITERIA_HISTORY)
    @Produces("application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<SearchCriteriaHistoryDto> getSearchCritriaHistory() throws InvalidParseOperationException,
        PreconditionFailedException {
        LOGGER.debug("Get the search criteria history");

        List<SearchCriteriaHistoryDto> searchCriteriaHistoryDtoList =
            searchCriteriaHistoryService.getSearchCritriaHistory(buildUiHttpContext());
        LOGGER.debug("Ui commons controller : {}", searchCriteriaHistoryDtoList.toString());
        return searchCriteriaHistoryDtoList;
    }

    @ApiOperation(value = "delete Search criteria history")
    @DeleteMapping(SEARCH_CRITERIA_HISTORY + PATH_ID)
    public void delete(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Delete SearchCriteriaHistory by id :{}", id);
        searchCriteriaHistoryService.delete(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "Update Search criteria history")
    @PutMapping(SEARCH_CRITERIA_HISTORY + PATH_ID)
    public SearchCriteriaHistoryDto update(@RequestBody final SearchCriteriaHistoryDto entity)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(entity);
        LOGGER.debug("Update SearchCriteriaHistory with id :{}", entity.getId());
        return searchCriteriaHistoryService.update(buildUiHttpContext(), entity);
    }
}
