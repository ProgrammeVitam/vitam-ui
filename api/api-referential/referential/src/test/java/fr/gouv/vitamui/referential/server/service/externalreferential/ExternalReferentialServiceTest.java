package fr.gouv.vitamui.referential.server.service.externalreferential;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.administration.profile.ProfileModel;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.ExternalAgencyDto;
import fr.gouv.vitamui.referential.common.dto.ExternalIngestContractDto;
import fr.gouv.vitamui.referential.common.dto.ExternalProfileDto;
import fr.gouv.vitamui.referential.common.dto.ExternalReferentialConfigDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class ExternalReferentialServiceTest {

    private static final int CURRENT_TENANT = 2;
    private static final String ARCHIVING_SYSTEM_ID = "system_id";

    @Mock
    private AdminExternalClient adminExternalClient;

    @Mock
    private SecurityService securityService;

    @Mock
    private ExternalArchivingSystemConfigurationManager configurationManager;

    @InjectMocks
    private ExternalReferentialService externalReferentialService;

    @BeforeEach
    void setUp() {
        doReturn(CURRENT_TENANT).when(securityService).getTenantIdentifier();
        doReturn(adminExternalClient).when(configurationManager).getClient(ARCHIVING_SYSTEM_ID);
    }

    @Test
    public void testGetExternalReferential() {
        // Given
        doReturn(
            List.of(
                new ExternalReferentialConfigDto()
                    .setName("local")
                    .setArchivingSystemId("local")
                    .setTenantIds(List.of(2, 3)),
                new ExternalReferentialConfigDto()
                    .setName("name")
                    .setArchivingSystemId(ARCHIVING_SYSTEM_ID)
                    .setTenantIds(List.of(1, 2, 3))
            )
        )
            .when(configurationManager)
            .listExternalReferentialConfig(CURRENT_TENANT);

        // When
        Collection<ExternalReferentialConfigDto> response = externalReferentialService.listExternalReferentialConfig();

        // Then
        assertThat(response).hasSize(2);
    }

    @Test
    public void testListAgencies() throws VitamClientException {
        // Given
        AgenciesModel agency1 = new AgenciesModel();
        agency1.setIdentifier("identifier1");
        agency1.setName("agency1");
        agency1.setDescription("description1");

        AgenciesModel agency2 = new AgenciesModel();
        agency2.setIdentifier("identifier2");
        agency2.setName("agency2");
        agency2.setDescription("description2");

        doReturn(new RequestResponseOK<AgenciesModel>().addAllResults(List.of(agency1, agency2)).setHttpCode(200))
            .when(adminExternalClient)
            .findAgencies(any(), any());

        // When
        List<ExternalAgencyDto> response = externalReferentialService.listAgencies(ARCHIVING_SYSTEM_ID, 3);

        // Then
        assertThat(response).hasSize(2);
        assertThat(response)
            .extracting(ExternalAgencyDto::getIdentifier, ExternalAgencyDto::getName, ExternalAgencyDto::getDescription)
            .containsExactly(
                tuple("identifier1", "agency1", "description1"),
                tuple("identifier2", "agency2", "description2")
            );

        ArgumentCaptor<VitamContext> contextArgumentCaptor = ArgumentCaptor.forClass(VitamContext.class);
        verify(adminExternalClient).findAgencies(contextArgumentCaptor.capture(), any());
        assertThat(contextArgumentCaptor.getValue().getTenantId()).isEqualTo(3);

        verify(configurationManager).validateExternalReferential(CURRENT_TENANT, ARCHIVING_SYSTEM_ID, 3);
    }

    @Test
    public void testListIngestContracts() throws VitamClientException {
        // Given
        IngestContractModel ingestContract1 = new IngestContractModel();
        ingestContract1.setIdentifier("identifier1");
        ingestContract1.setName("agency1");
        ingestContract1.setDescription("description1");

        IngestContractModel ingestContract2 = new IngestContractModel();
        ingestContract2.setIdentifier("identifier2");
        ingestContract2.setName("agency2");
        ingestContract2.setDescription("description2");

        doReturn(
            new RequestResponseOK<IngestContractModel>()
                .addAllResults(List.of(ingestContract1, ingestContract2))
                .setHttpCode(200)
        )
            .when(adminExternalClient)
            .findIngestContracts(any(), any());

        // When
        List<ExternalIngestContractDto> response = externalReferentialService.listIngestContracts(
            ARCHIVING_SYSTEM_ID,
            3
        );

        // Then
        assertThat(response).hasSize(2);
        assertThat(response)
            .extracting(
                ExternalIngestContractDto::getIdentifier,
                ExternalIngestContractDto::getName,
                ExternalIngestContractDto::getDescription
            )
            .containsExactly(
                tuple("identifier1", "agency1", "description1"),
                tuple("identifier2", "agency2", "description2")
            );

        ArgumentCaptor<VitamContext> contextArgumentCaptor = ArgumentCaptor.forClass(VitamContext.class);
        verify(adminExternalClient).findIngestContracts(contextArgumentCaptor.capture(), any());
        assertThat(contextArgumentCaptor.getValue().getTenantId()).isEqualTo(3);

        verify(configurationManager).validateExternalReferential(CURRENT_TENANT, ARCHIVING_SYSTEM_ID, 3);
    }

    @Test
    public void testListArchiveProfiles() throws VitamClientException {
        // Given
        ProfileModel profileModel1 = new ProfileModel();
        profileModel1.setIdentifier("identifier1");
        profileModel1.setName("agency1");
        profileModel1.setDescription("description1");

        ProfileModel profileModel2 = new ProfileModel();
        profileModel2.setIdentifier("identifier2");
        profileModel2.setName("agency2");
        profileModel2.setDescription("description2");

        doReturn(
            new RequestResponseOK<ProfileModel>().addAllResults(List.of(profileModel1, profileModel2)).setHttpCode(200)
        )
            .when(adminExternalClient)
            .findProfiles(any(), any());

        // When
        List<ExternalProfileDto> response = externalReferentialService.listArchiveProfiles(ARCHIVING_SYSTEM_ID, 3);

        // Then
        assertThat(response).hasSize(2);
        assertThat(response)
            .extracting(
                ExternalProfileDto::getIdentifier,
                ExternalProfileDto::getName,
                ExternalProfileDto::getDescription
            )
            .containsExactly(
                tuple("identifier1", "agency1", "description1"),
                tuple("identifier2", "agency2", "description2")
            );

        ArgumentCaptor<VitamContext> contextArgumentCaptor = ArgumentCaptor.forClass(VitamContext.class);
        verify(adminExternalClient).findProfiles(contextArgumentCaptor.capture(), any());
        assertThat(contextArgumentCaptor.getValue().getTenantId()).isEqualTo(3);

        verify(configurationManager).validateExternalReferential(CURRENT_TENANT, ARCHIVING_SYSTEM_ID, 3);
    }
}
