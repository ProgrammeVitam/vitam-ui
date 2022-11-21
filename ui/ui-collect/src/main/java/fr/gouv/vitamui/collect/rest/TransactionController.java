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
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.service.TransactionService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.ARCHIVE_UNIT_INFO;
import static fr.gouv.vitamui.archives.search.common.rest.RestApi.EXPORT_CSV_SEARCH_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.*;
import static fr.gouv.vitamui.commons.api.CommonConstants.IDENTIFIER_MANDATORY_PARAMETER;
import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ID;

@Api(tags = "Collect")
@RestController
@RequestMapping("${ui-collect.prefix}/transactions")
@Consumes("application/json")
@Produces("application/json")
public class TransactionController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TransactionController.class);


    private final TransactionService transactionService;

    @Autowired
    public TransactionController(final TransactionService service) {

        this.transactionService = service;
    }



    @ApiOperation(value = "Get AU collect paginated")
    @PostMapping(ARCHIVE_UNITS + "/{transactionId}" + SEARCH)
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public VitamUIArchiveUnitResponseDto searchArchiveUnits(final @PathVariable("transactionId") String transactionId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Query  and the transactionId are mandatories parameters: ",
            searchQuery, transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("search archives Units by criteria = {}", searchQuery);
        VitamUIArchiveUnitResponseDto archiveResponseDtos = new VitamUIArchiveUnitResponseDto();
        ArchiveUnitsDto archiveUnits =
            transactionService.searchArchiveUnitsByTransactionAndSearchQuery(buildUiHttpContext(), transactionId,
                searchQuery);

        if (archiveUnits != null) {
            archiveResponseDtos = archiveUnits.getArchives();
        }
        return archiveResponseDtos;

    }

    @ApiOperation(value = "export into csv format archive units by criteria")
    @PostMapping(ARCHIVE_UNITS + "/{transactionId}" + EXPORT_CSV_SEARCH_PATH)
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> exportCsvArchiveUnitsByCriteria(final @PathVariable("transactionId") String transactionId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Query is a mandatory parameter: ", searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        LOGGER.debug("Export search archives Units by criteria into csv format = {}", searchQuery);
        Resource exportedCsvResult =
            transactionService.exportCsvArchiveUnitsByCriteria(transactionId, searchQuery, buildUiHttpContext()).getBody();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment")
            .body(exportedCsvResult);
    }

    @ApiOperation(value = "Find the Archive Unit Details")
    @GetMapping(ARCHIVE_UNITS + ARCHIVE_UNIT_INFO + PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResultsDto> findUnitById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Query is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the Archive Unit with ID {}", id);
        return transactionService.findUnitById(id, buildUiHttpContext());
    }

    @ApiOperation(value = "Send transaction operation")
    @PutMapping(CommonConstants.PATH_ID + SEND_PATH)
    public void sendTransaction(@PathVariable("id") String transactionId) throws InvalidParseOperationException {
        SanityChecker.checkSecureParameter(transactionId);
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        LOGGER.debug("Send the Transaction with ID {}", transactionId);
        transactionService.sendTransaction(buildUiHttpContext(), transactionId);
    }

    @PutMapping(CommonConstants.PATH_ID)
    public CollectTransactionDto updateTransaction(final @PathVariable("id") String id,
        @RequestBody CollectTransactionDto collectTransactionDto)
        throws InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(collectTransactionDto);
        LOGGER.debug("[Internal] Transaction to update : {}", collectTransactionDto);
        return transactionService.updateTransaction(buildUiHttpContext(), collectTransactionDto);
    }

    @ApiOperation(value = "Validate transaction operation")
    @PutMapping(CommonConstants.PATH_ID + VALIDATE_PATH)
    public void validateTransaction(@PathVariable("id") String transactionId) throws InvalidParseOperationException {
        SanityChecker.checkSecureParameter(transactionId);
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        LOGGER.debug("Validate the Transaction with ID {}", transactionId);
        transactionService.validateTransaction(buildUiHttpContext(), transactionId);
    }

    @ApiOperation(value = "Reopen transaction operation")
    @PutMapping(CommonConstants.PATH_ID + REOPEN_PATH)
    public void reopenTransaction(@PathVariable("id") String transactionId) throws InvalidParseOperationException {
        SanityChecker.checkSecureParameter(transactionId);
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, transactionId);
        LOGGER.debug("Reopen the Transaction with ID {}", transactionId);
        transactionService.reopenTransaction(buildUiHttpContext(), transactionId);
    }

    @ApiOperation(value = "Abort transaction operation")
    @PutMapping(CommonConstants.PATH_ID + ABORT_PATH)
    public void abortTransaction(@PathVariable("id") String transactionId) throws InvalidParseOperationException {
        SanityChecker.checkSecureParameter(transactionId);
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, transactionId);
        LOGGER.debug("Abort the Transaction with ID {}", transactionId);
        transactionService.abortTransaction(buildUiHttpContext(), transactionId);
    }


    @ApiOperation(value = "Get transaction by project")
    @GetMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public CollectTransactionDto getTransactionById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the transaction with ID {}", id);
        final HttpServletRequest request = getCurrentHttpRequest();
        return transactionService.getTransactionById(buildUiHttpContext(), id);
    }



}
