/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.internal.server.rest;

import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.collect.internal.server.service.ExternalParametersService;
import fr.gouv.vitamui.collect.internal.server.service.TransactionArchiveUnitInternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import io.swagger.annotations.Api;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.ARCHIVE_UNIT_INFO;
import static fr.gouv.vitamui.archives.search.common.rest.RestApi.EXPORT_CSV_SEARCH_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.ARCHIVE_UNITS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_TRANSACTION_ARCHIVE_UNITS_PATH;

@RestController
@RequestMapping(COLLECT_TRANSACTION_ARCHIVE_UNITS_PATH)
@Api(tags = "collect", value = "Unités archivistiques d'un projet")
public class TransactionArchiveUnitInternalController {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(TransactionArchiveUnitInternalController.class);
    private final ExternalParametersService externalParametersService;
    private final TransactionArchiveUnitInternalService transactionArchiveUnitInternalService;

    public TransactionArchiveUnitInternalController(
        TransactionArchiveUnitInternalService transactionArchiveUnitInternalService,
        final ExternalParametersService externalParametersService) {
        this.transactionArchiveUnitInternalService = transactionArchiveUnitInternalService;
        this.externalParametersService = externalParametersService;
    }

    @PostMapping("/{transactionId}" + ARCHIVE_UNITS)
    public ArchiveUnitsDto searchArchiveUnitsByCriteria(@PathVariable("transactionId") final String transactionId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException, InvalidParseOperationException, PreconditionFailedException,
        InvalidCreateOperationException {

        SanityChecker.sanitizeCriteria(searchQuery);
        ParameterChecker
            .checkParameter("The SearchCriteria is mandatory parameter: ", searchQuery);
        SanityChecker.checkSecureParameter(transactionId);
        LOGGER.debug("Calling service searchArchiveUnits for tenantId {}, accessContractId {} By Criteria {} ",
            searchQuery);
        ArchiveUnitsDto archiveUnitsDto =
            transactionArchiveUnitInternalService.searchArchiveUnitsByCriteria(transactionId, searchQuery,
                externalParametersService.buildVitamContextFromExternalParam());
        return archiveUnitsDto;
    }

    @PostMapping("/{transactionId}" + ARCHIVE_UNITS + EXPORT_CSV_SEARCH_PATH)
    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(
        @PathVariable("transactionId") final String transactionId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Export to CSV file Archive Units by criteria {}", searchQuery);
        Resource exportedResult =
            transactionArchiveUnitInternalService.exportToCsvSearchArchiveUnitsByCriteria(transactionId, searchQuery,
                externalParametersService.buildVitamContextFromExternalParam());
        return new ResponseEntity<>(exportedResult, HttpStatus.OK);
    }

    @GetMapping(ARCHIVE_UNIT_INFO + CommonConstants.PATH_ID)
    public ResultsDto findUnitById(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Id is mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("UA Details  {}", id);

        return transactionArchiveUnitInternalService.findArchiveUnitById(id,
            externalParametersService.buildVitamContextFromExternalParam());
    }

}
