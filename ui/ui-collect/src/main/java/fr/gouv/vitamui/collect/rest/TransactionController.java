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
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.service.TransactionService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import static fr.gouv.vitamui.collect.common.rest.RestApi.SEND_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.VALIDATE_PATH;

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


    @ApiOperation(value = "Send transaction operation")
    @PutMapping(CommonConstants.PATH_ID + SEND_PATH)
    public void sendTransaction(@PathVariable("id") String transactionId) throws InvalidParseOperationException {
        SanityChecker.checkSecureParameter(transactionId);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        LOGGER.debug("Send the Transaction with ID {}", transactionId);
        transactionService.sendTransaction(buildUiHttpContext(), transactionId);
    }

    @ApiOperation(value = "Validate transaction operation")
    @PutMapping(CommonConstants.PATH_ID + VALIDATE_PATH)
    public void validateTransaction(@PathVariable("id") String transactionId) throws InvalidParseOperationException {
        SanityChecker.checkSecureParameter(transactionId);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        LOGGER.debug("Validate the Transaction with ID {}", transactionId);
        transactionService.validateTransaction(buildUiHttpContext(), transactionId);
    }


    @ApiOperation(value = "Get transaction by project")
    @GetMapping(CommonConstants.PATH_ID )
    @ResponseStatus(HttpStatus.OK)
    public CollectTransactionDto getTransactionById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the transaction with ID {}", id);
        return transactionService.getTransactionById(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "Delete project")
    @PutMapping( CommonConstants.PATH_ID + "/update-units-metadata")
    @ResponseStatus(HttpStatus.OK)
    public String update(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Delete the Project with ID {}", id);
        return transactionService.update(id, buildUiHttpContext()).getBody();
    }


}
