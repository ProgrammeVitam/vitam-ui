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

package fr.gouv.vitamui.collect.internal.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.internal.server.service.TransactionInternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static fr.gouv.vitamui.collect.common.rest.RestApi.ABORT_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.REOPEN_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.SEND_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.VALIDATE_PATH;

@RestController
@RequestMapping(RestApi.COLLECT_TRANSACTION_PATH)
@Api(tags = "collect", value = "Préparation de versements")
public class TransactionInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TransactionInternalController.class);
    private final InternalSecurityService securityService;
    private final TransactionInternalService transactionInternalService;

    @Autowired
    public TransactionInternalController(final TransactionInternalService transactionInternalService,
                                         final InternalSecurityService securityService) {
        this.securityService = securityService;
        this.transactionInternalService = transactionInternalService;
    }

    @PutMapping(CommonConstants.PATH_ID + SEND_PATH)
    public void sendTransaction(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Transaction to send  {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        transactionInternalService.sendTransaction(id, vitamContext);
    }

    @PutMapping(CommonConstants.PATH_ID + VALIDATE_PATH)
    public void validateTransaction(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Transaction to close  {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        transactionInternalService.validateTransaction(id, vitamContext);
    }

    @PutMapping(CommonConstants.PATH_ID + REOPEN_PATH)
    public void reopenTransaction(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Transaction to reopen  {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        transactionInternalService.reopenTransaction(id, vitamContext);
    }

    @PutMapping(CommonConstants.PATH_ID + ABORT_PATH)
    public void abortTransaction(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Transaction to abort  {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        transactionInternalService.abortTransaction(id, vitamContext);
    }

    @GetMapping(CommonConstants.PATH_ID )
    public CollectTransactionDto getTransactionById(final @PathVariable("id") String id) throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Project Id  {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return transactionInternalService.getTransactionById(id, vitamContext);
    }

    @PutMapping
    public CollectTransactionDto updateTransaction(@RequestBody CollectTransactionDto transactionDto)
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", transactionDto.getId());
        SanityChecker.checkSecureParameter(transactionDto.getId());
        LOGGER.debug("[External] Transaction to update : {}", transactionDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return transactionInternalService.updateTransaction(transactionDto, vitamContext);
    }

}
