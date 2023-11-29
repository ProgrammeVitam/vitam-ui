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

package fr.gouv.vitamui.collect.internal.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.collect.common.dto.ProjectDto;
import fr.gouv.vitam.collect.common.dto.TransactionDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.dto.GetorixDepositDto;
import fr.gouv.vitamui.collect.internal.server.dao.GetorixDepositRepository;
import fr.gouv.vitamui.collect.internal.server.domain.GetorixDepositModel;
import fr.gouv.vitamui.collect.internal.server.service.converters.GetorixDepositConverter;
import fr.gouv.vitamui.collect.internal.server.service.converters.ProjectConverter;
import fr.gouv.vitamui.collect.internal.server.service.converters.TransactionConverter;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Objects;

import static fr.gouv.vitamui.collect.internal.server.service.converters.ProjectConverter.toVitamuiCollectProjectDto;

/**
 * The service to manage all Getorix Deposits
 */
@Service
public class GetorixDepositInternalService  extends
    VitamUICrudService<GetorixDepositDto, GetorixDepositModel> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(GetorixDepositInternalService.class);

    private static final String NOT_ALLOWED_TO_CREATE_DEPOSIT = "You can not create the deposit";
    public static final String UNABLE_TO_CREATE_PROJECT = "Unable to create project";
    public static final String UNABLE_TO_CREATE_TRANSACTION = "Unable to create transaction";
    public static final String UNABLE_TO_PROCESS_RESPONSE = "Unable to process response";

    private final GetorixDepositRepository getorixDepositRepository;

    private final GetorixDepositConverter getorixDepositConverter;

    private final InternalSecurityService internalSecurityService;

    private final CollectService collectService;

    @Autowired
    public GetorixDepositInternalService(final CustomSequenceRepository sequenceRepository,
        GetorixDepositRepository getorixDepositRepository, GetorixDepositConverter getorixDepositConverter,
        InternalSecurityService internalSecurityService, CollectService collectService) {
        super(sequenceRepository);
        this.getorixDepositRepository = getorixDepositRepository;
        this.getorixDepositConverter = getorixDepositConverter;
        this.internalSecurityService = internalSecurityService;
        this.collectService = collectService;
    }

    public GetorixDepositDto createGetorixDeposit(GetorixDepositDto getorixDepositDto, VitamContext vitamContext) {

        LOGGER.debug("[Internal] : Create new Getorix Deposit");
        LOGGER.debug("[Internal] : Create new Getorix Deposit : Vitam context", vitamContext);
        AuthUserDto authUserDto = internalSecurityService.getUser();
        if(Objects.isNull(authUserDto)) {
            LOGGER.error("You are not authorized to create the deposit");
            throw new UnAuthorizedException("You are not authorized to create the deposit ");
        }
        if(Objects.isNull(getorixDepositDto)) {
            LOGGER.error("The Getorix deposit information are not provided");
            throw new BadRequestException("The Getorix deposit information are not provided ");
        }
        if(!authUserDto.getId().equals(getorixDepositDto.getUserId())) {
            LOGGER.error(NOT_ALLOWED_TO_CREATE_DEPOSIT);
            throw new ForbiddenException(NOT_ALLOWED_TO_CREATE_DEPOSIT);
        }
        if(!Objects.equals(vitamContext.getTenantId(), getorixDepositDto.getTenantIdentifier())) {
            LOGGER.error(NOT_ALLOWED_TO_CREATE_DEPOSIT);
            throw new ForbiddenException(NOT_ALLOWED_TO_CREATE_DEPOSIT);
        }

        manageDepositDates(getorixDepositDto);

        CollectProjectDto collectProjectDto = new CollectProjectDto();
        collectProjectDto.setStatus("OPEN");
        collectProjectDto.setMessageIdentifier(getorixDepositDto.getOperationName());
        collectProjectDto.setOriginatingAgencyIdentifier(getorixDepositDto.getOriginatingAgency());
        collectProjectDto.setSubmissionAgencyIdentifier(getorixDepositDto.getVersatileService());

        CollectTransactionDto collectTransactionDto = new CollectTransactionDto();
        collectTransactionDto.setStatus("OPEN");

        final CollectProjectDto projectResult = createProject(collectProjectDto, vitamContext);

        CollectTransactionDto transactionResult = createTransactionForProject(collectTransactionDto,
            projectResult.getId(), vitamContext);

        getorixDepositDto.setTransactionId(transactionResult.getId());

        getorixDepositDto.setCreationDate(OffsetDateTime.now());
        getorixDepositDto.setProjectId(projectResult.getId());

        return this.create(getorixDepositDto);
    }

    public GetorixDepositDto getGetorixDepositById(String id) {
        AuthUserDto authUserDto = internalSecurityService.getUser();
        if(Objects.isNull(authUserDto)) {
            LOGGER.error("You are not authorized to create the deposit");
            throw new UnAuthorizedException("You are not authorized to create the deposit ");
        }
        LOGGER.debug("[Internal] : get the GetorixDeposit details by id : {}", id);
        return this.getOne(id);
    }

    private CollectProjectDto createProject(CollectProjectDto collectProjectDto, VitamContext vitamContext) {
        LOGGER.debug("CollectProjectDto: {}", collectProjectDto);
        try {
            ProjectDto projectDto = ProjectConverter.toVitamProjectDto(collectProjectDto);
            RequestResponse<JsonNode> requestResponse = collectService.initProject(vitamContext, projectDto);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when retrieving projects!");
            }
            return toVitamuiCollectProjectDto(
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    ProjectDto.class));
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_CREATE_PROJECT + ": {}", e);
            throw new InternalServerException(UNABLE_TO_CREATE_PROJECT, e);
        } catch (InvalidParseOperationException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    private CollectTransactionDto createTransactionForProject(
        CollectTransactionDto collectTransactionDto, String projectId, VitamContext vitamContext) {
        LOGGER.debug("CollectTransactionDto: ", collectTransactionDto);
        try {
            SanityChecker.checkSecureParameter(projectId);
            TransactionDto transactionDto = TransactionConverter.toVitamDto(collectTransactionDto);
            RequestResponse<JsonNode> requestResponse =
                collectService.initTransaction(vitamContext, transactionDto, projectId);
            if (!requestResponse.isOk()) {
                LOGGER.error("Error occurs when creating transaction");
                throw new VitamClientException("Error occurs when creating transaction");
            }
            return TransactionConverter.toVitamUiDto(
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    TransactionDto.class));
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_CREATE_TRANSACTION + ": {}", e);
            throw new InternalServerException(UNABLE_TO_CREATE_TRANSACTION, e);
        } catch (InvalidParseOperationException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

     @Override
    protected VitamUIRepository<GetorixDepositModel, String> getRepository() {
        return getorixDepositRepository;
    }

    @Override
    protected Class<GetorixDepositModel> getEntityClass() {
        return GetorixDepositModel.class;
    }

    @Override
    protected Converter<GetorixDepositDto, GetorixDepositModel> getConverter() {
        return getorixDepositConverter;
    }

    private void manageDepositDates(GetorixDepositDto getorixDepositDto) {
        LOGGER.debug("manage dates for the getorix deposit");
        if(getorixDepositDto.getOperationStartDate() != null) {
            getorixDepositDto.setOperationStartDate(getorixDepositDto.getOperationStartDate().plusDays(1));
        }

        if(getorixDepositDto.getOperationEndDate() != null) {
            getorixDepositDto.setOperationEndDate(getorixDepositDto.getOperationEndDate().plusDays(1));
        }

        if(getorixDepositDto.getDocumentStartDate() != null) {
            getorixDepositDto.setDocumentStartDate(getorixDepositDto.getDocumentStartDate().plusDays(1));
        }

        if(getorixDepositDto.getDocumentEndDate() != null) {
            getorixDepositDto.setDocumentEndDate(getorixDepositDto.getDocumentEndDate().plusDays(1));
        }
    }
}
