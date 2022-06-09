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
package fr.gouv.vitamui.archives.search.external.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.external.server.service.SearchCriteriaHistoryExternalService;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.util.List;

/**
 * The controller to check existence, create, read and delete the search criterias
 *
 */
@RestController
@RequestMapping(RestApi.SEARCH_CRITERIA_HISTORY)
@Getter
@Setter
@Api(tags = "searchCriteriaHistory", value = "Search Criteria History")
public class SearchCriteriaHistoryExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        SearchCriteriaHistoryExternalController.class);

    private SearchCriteriaHistoryExternalService searchCriteriaHistoryExternalService;

    @Autowired
    public SearchCriteriaHistoryExternalController(final SearchCriteriaHistoryExternalService searchCriteriaHistoryExternalService) {
        this.searchCriteriaHistoryExternalService = searchCriteriaHistoryExternalService;
    }

    @GetMapping(CommonConstants.PATH_ME)
    @Secured(ServicesData.ROLE_GET_ALL_ARCHIVE)
    public List<SearchCriteriaHistoryDto> getSearchCriteriaHistory() {
        LOGGER.debug("getSearchCriteriaHistory archive external");
        return searchCriteriaHistoryExternalService.getSearchCriteriaHistory();
    }

    @PostMapping
    @Secured(ServicesData.ROLE_CREATE_ARCHIVE)
    public SearchCriteriaHistoryDto create(final @Valid @RequestBody SearchCriteriaHistoryDto dto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(dto);
        LOGGER.debug("Create SearchCriteriaHistory {}", dto);
        return searchCriteriaHistoryExternalService.create(dto);
    }

    @DeleteMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_GET_ARCHIVE)
    public void delete(final @PathVariable("id") String id) throws PreconditionFailedException, InvalidParseOperationException {
        SanityChecker.checkSecureParameter(id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Delete SearchCriteriaHistory with id :{}", id);
        searchCriteriaHistoryExternalService.delete(id);
    }

    @ApiOperation(value = "Update Search criteria history")
    @PutMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_CREATE_ARCHIVE)
    public void update(@RequestBody final SearchCriteriaHistoryDto entity) throws PreconditionFailedException, InvalidParseOperationException {
        SanityChecker.sanitizeCriteria(entity);
        ParameterChecker.checkParameter("Identifier is mandatory : " , entity.getId());
        LOGGER.debug("Update SearchCriteriaHistory with id :{}", entity.getId());
        searchCriteriaHistoryExternalService.update(entity);
    }
}
