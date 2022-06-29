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
package fr.gouv.vitamui.archives.search.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.archives.search.service.SearchCriteriaHistoryService;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;
import java.util.List;

@Api(tags = "searchcriteriahistory")
@RequestMapping("${ui-prefix}/archive-search/searchcriteriahistory")
@RestController
@ResponseBody
public class SearchCriteriaHistoryController extends AbstractUiRestController {

    protected final SearchCriteriaHistoryService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SearchCriteriaHistoryController.class);

    @Autowired
    public SearchCriteriaHistoryController(final SearchCriteriaHistoryService service) {
        this.service = service;
    }

    @ApiOperation(value = "Get the search criteria history")
    @GetMapping
    @Produces("application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<SearchCriteriaHistoryDto> getSearchCritriaHistory() throws InvalidParseOperationException,
        PreconditionFailedException {
        LOGGER.debug("Get the search criteria history");

        List<SearchCriteriaHistoryDto> searchCriteriaHistoryDtoList = service.getSearchCritriaHistory(buildUiHttpContext());
        LOGGER.debug("Ui commons controller : {}", searchCriteriaHistoryDtoList.toString());
        return searchCriteriaHistoryDtoList;
    }

    @ApiOperation(value = "Create search criteria history")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SearchCriteriaHistoryDto create(@RequestBody final SearchCriteriaHistoryDto entityDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(entityDto);
        LOGGER.debug("create class={}", entityDto.getClass().getName());
        return service.create(buildUiHttpContext(), entityDto);
    }

    @ApiOperation(value = "delete Search criteria history")
    @DeleteMapping(CommonConstants.PATH_ID)
    public void delete(final @PathVariable("id") String id) throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Delete SearchCriteriaHistory by id :{}", id);
        service.delete(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "Update Search criteria history")
    @PutMapping(CommonConstants.PATH_ID)
    public SearchCriteriaHistoryDto update(@RequestBody final SearchCriteriaHistoryDto entity)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(entity);
        LOGGER.debug("Update SearchCriteriaHistory with id :{}", entity.getId());
        return service.update(buildUiHttpContext(), entity);
    }
}
