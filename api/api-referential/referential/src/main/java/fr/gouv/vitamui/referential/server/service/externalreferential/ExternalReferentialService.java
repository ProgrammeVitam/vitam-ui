package fr.gouv.vitamui.referential.server.service.externalreferential;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.administration.profile.ProfileModel;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.ExternalAgencyDto;
import fr.gouv.vitamui.referential.common.dto.ExternalIngestContractDto;
import fr.gouv.vitamui.referential.common.dto.ExternalProfileDto;
import fr.gouv.vitamui.referential.common.dto.ExternalReferentialConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class ExternalReferentialService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalReferentialService.class);

    private final SecurityService securityService;
    private final ExternalArchivingSystemConfigurationManager configurationManager;

    public ExternalReferentialService(
        SecurityService securityService,
        ExternalArchivingSystemConfigurationManager configurationManager
    ) {
        this.securityService = securityService;
        this.configurationManager = configurationManager;
    }

    public Collection<ExternalReferentialConfigDto> listExternalReferentialConfig() {
        int tenantIdentifier = this.securityService.getTenantIdentifier();
        return this.configurationManager.listExternalReferentialConfig(tenantIdentifier);
    }

    public List<ExternalAgencyDto> listAgencies(String archivingSystemId, Integer targetTenantIdentifier) {
        this.configurationManager.validateExternalReferential(
                this.securityService.getTenantIdentifier(),
                archivingSystemId,
                targetTenantIdentifier
            );
        VitamContext vitamContext = buildVitamContext(targetTenantIdentifier);
        LOGGER.info(
            "List agencies from external archiving system '{}' - EvIdAppSession : {} ",
            archivingSystemId,
            vitamContext.getApplicationSessionId()
        );
        try (AdminExternalClient adminExternalClient = getAdminExternalClient(archivingSystemId)) {
            Select select = new Select();
            select.addUsedProjection(
                AgenciesModel.TAG_IDENTIFIER,
                AgenciesModel.TAG_NAME,
                AgenciesModel.TAG_DESCRIPTION
            );
            select.addOrderByAscFilter(AgenciesModel.TAG_IDENTIFIER);

            final RequestResponse<AgenciesModel> response = adminExternalClient.findAgencies(
                vitamContext,
                select.getFinalSelect()
            );
            VitamRestUtils.checkResponse(response);

            return ((RequestResponseOK<AgenciesModel>) response).getResults()
                .stream()
                .map(
                    p ->
                        new ExternalAgencyDto()
                            .setIdentifier(p.getIdentifier())
                            .setName(p.getName())
                            .setDescription(p.getDescription())
                )
                .toList();
        } catch (InvalidParseOperationException | VitamClientException e) {
            throw new InternalServerException(
                "Unable to find ingest contracts from external archiving system '" + archivingSystemId + "'",
                e
            );
        }
    }

    public List<ExternalIngestContractDto> listIngestContracts(
        String archivingSystemId,
        Integer targetTenantIdentifier
    ) {
        this.configurationManager.validateExternalReferential(
                this.securityService.getTenantIdentifier(),
                archivingSystemId,
                targetTenantIdentifier
            );
        VitamContext vitamContext = buildVitamContext(targetTenantIdentifier);
        LOGGER.info(
            "List ingest contracts from external archiving system '{}' - EvIdAppSession : {} ",
            archivingSystemId,
            vitamContext.getApplicationSessionId()
        );
        try (AdminExternalClient adminExternalClient = getAdminExternalClient(archivingSystemId)) {
            Select select = new Select();
            select.addUsedProjection(
                IngestContractModel.TAG_IDENTIFIER,
                IngestContractModel.TAG_NAME,
                IngestContractModel.TAG_DESCRIPTION
            );
            select.addOrderByAscFilter(IngestContractModel.TAG_IDENTIFIER);

            final RequestResponse<IngestContractModel> response = adminExternalClient.findIngestContracts(
                vitamContext,
                select.getFinalSelect()
            );
            VitamRestUtils.checkResponse(response);

            return ((RequestResponseOK<IngestContractModel>) response).getResults()
                .stream()
                .map(
                    p ->
                        new ExternalIngestContractDto()
                            .setIdentifier(p.getIdentifier())
                            .setName(p.getName())
                            .setDescription(p.getDescription())
                )
                .toList();
        } catch (InvalidParseOperationException | VitamClientException e) {
            throw new InternalServerException(
                "Unable to find ingest contracts from external archiving system '" + archivingSystemId + "'",
                e
            );
        }
    }

    public List<ExternalProfileDto> listArchiveProfiles(String externalSystemId, Integer targetTenantIdentifier) {
        this.configurationManager.validateExternalReferential(
                this.securityService.getTenantIdentifier(),
                externalSystemId,
                targetTenantIdentifier
            );
        VitamContext vitamContext = buildVitamContext(targetTenantIdentifier);
        LOGGER.info(
            "List profiles from external archiving system '{}' - EvIdAppSession : {} ",
            externalSystemId,
            vitamContext.getApplicationSessionId()
        );
        try (AdminExternalClient adminExternalClient = getAdminExternalClient(externalSystemId)) {
            Select select = new Select();
            select.addUsedProjection(ProfileModel.TAG_IDENTIFIER, ProfileModel.TAG_NAME, ProfileModel.TAG_DESCRIPTION);
            select.addOrderByAscFilter(ProfileModel.TAG_IDENTIFIER);

            final RequestResponse<ProfileModel> response = adminExternalClient.findProfiles(
                vitamContext,
                select.getFinalSelect()
            );
            VitamRestUtils.checkResponse(response);

            return ((RequestResponseOK<ProfileModel>) response).getResults()
                .stream()
                .map(
                    p ->
                        new ExternalProfileDto()
                            .setIdentifier(p.getIdentifier())
                            .setName(p.getName())
                            .setDescription(p.getDescription())
                )
                .toList();
        } catch (InvalidParseOperationException | VitamClientException e) {
            throw new InternalServerException(
                "Unable to list profiles from external archiving system '" + externalSystemId + "'",
                e
            );
        }
    }

    private AdminExternalClient getAdminExternalClient(String archivingSystemId) {
        return this.configurationManager.getClient(archivingSystemId);
    }

    private VitamContext buildVitamContext(Integer tenantIdentifier) {
        return new VitamContext(tenantIdentifier).setApplicationSessionId(this.securityService.getApplicationId());
    }
}
