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
package fr.gouv.vitamui.collect.external.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.external.server.service.TransactionExternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static fr.gouv.vitamui.collect.common.rest.RestApi.*;

/**
 * Transaction External controller
 */
@Api(tags = "Collect")
@RequestMapping(RestApi.COLLECT_TRANSACTION_PATH)
@RestController
@ResponseBody
public class TransactionExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TransactionExternalController.class);

    private final TransactionExternalService transactionExternalService;

    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";

    @Autowired
    public TransactionExternalController(TransactionExternalService transactionExternalService) {
        this.transactionExternalService = transactionExternalService;
    }

    @Secured(ServicesData.ROLE_SEND_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + SEND_PATH)
    public void sendTransaction(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("The transaction id {} ", id);
        transactionExternalService.sendTransaction(id);
    }

    @Secured(ServicesData.ROLE_CLOSE_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + VALIDATE_PATH)
    public void validateTransaction(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("The transaction id {} ", id);
        transactionExternalService.validateTransaction(id);
    }

    @ApiOperation(value = "Get transaction by id")
    @GetMapping(CommonConstants.PATH_ID)
    public CollectTransactionDto getTransactionById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the Transactions with project Id {}", id);
        return transactionExternalService.getTransactionById(id);
    }


}