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
package fr.gouv.vitamui.referential.external.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.AccessionRegisterSearchDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.external.server.service.AccessionRegisterDetailExternalService;
import fr.gouv.vitamui.referential.external.server.service.AccessionRegisterSummaryExternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.ACCESSION_REGISTER_URL)
public class AccessionRegisterExternalController {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(AccessionRegisterExternalController.class);

    private final AccessionRegisterSummaryExternalService accessionRegisterSummaryExternalService;
    private final AccessionRegisterDetailExternalService accessionRegisterDetailExternalService;

    @Autowired
    public AccessionRegisterExternalController(
        AccessionRegisterSummaryExternalService accessionRegisterSummaryExternalService,
        AccessionRegisterDetailExternalService accessionRegisterDetailExternalService) {
        this.accessionRegisterSummaryExternalService = accessionRegisterSummaryExternalService;
        this.accessionRegisterDetailExternalService = accessionRegisterDetailExternalService;
    }

    @GetMapping("/summary")
    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    public Collection<AccessionRegisterSummaryDto> getAccessionRegisterSummaries(
        @RequestParam final Optional<String> criteria) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("get all accessionRegister criteria={}", criteria);
        return accessionRegisterSummaryExternalService.getAll(criteria);
    }

    @GetMapping(value = RestApi.DETAILS, params = {"page", "size"})
    @Secured(ServicesData.ROLE_GET_ACCESSION_REGISTER_DETAIL)
    public PaginatedValuesDto<AccessionRegisterDetailDto> getAccessionRegisterDetails(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction) {
        SanityChecker.sanitizeCriteria(criteria);
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page, size, criteria, orderBy, direction);
        return accessionRegisterDetailExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @PostMapping(RestApi.DETAILS_EXPORT_CSV)
    @Secured(ServicesData.ROLE_GET_ACCESSION_REGISTER_DETAIL)
    public Resource exportCsvArchiveUnitsByCriteria(final @RequestBody AccessionRegisterSearchDto query)
        throws InvalidParseOperationException, PreconditionFailedException{
        ParameterChecker.checkParameter("The query is a mandatory parameter: ", query);
        SanityChecker.sanitizeCriteria(query);
        LOGGER.info("Calling export to csv search archive Units By Criteria {} ", query);
        return accessionRegisterDetailExternalService.exportCsvArchiveUnitsByCriteria(query);
    }

}
