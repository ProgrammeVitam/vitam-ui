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

package fr.gouv.vitamui.referential.server.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.rest.AbstractRestControllerMockMvcTest;
import fr.gouv.vitamui.iam.security.authentication.AuthenticationToken;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.ExternalAgencyDto;
import fr.gouv.vitamui.referential.common.dto.ExternalIngestContractDto;
import fr.gouv.vitamui.referential.common.dto.ExternalProfileDto;
import fr.gouv.vitamui.referential.common.dto.ExternalReferentialConfigDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.server.security.WebSecurityConfig;
import fr.gouv.vitamui.referential.server.service.externalreferential.ExternalReferentialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@WebMvcTest(controllers = { ExternalReferentialController.class })
@Import(value = { WebSecurityConfig.class, RestExceptionHandler.class })
public class ExternalReferentialControllerTest extends AbstractRestControllerMockMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ExternalReferentialService externalReferentialService;

    @MockitoBean
    private ApiAuthenticationProvider apiAuthenticationProvider;

    @MockitoBean
    private SecurityService securityService;

    @Override
    protected String getRessourcePrefix() {
        return RestApi.EXTERNAL_REFERENTIAL_URL;
    }

    @Test
    void testListExternalReferentialConfig() throws Exception {
        // Given
        Mockito.when(externalReferentialService.listExternalReferentialConfig()).thenReturn(
            List.of(
                new ExternalReferentialConfigDto()
                    .setName("name")
                    .setArchivingSystemId("system_id")
                    .setTenantIds(List.of(1, 2, 3))
            )
        );

        // When
        ResultActions resultActions = this.performGet(getUriBuilder(RestApi.CONFIG_PATH));

        // Then
        List<ExternalReferentialConfigDto> response = objectMapper.readValue(
            resultActions.andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getName()).isEqualTo("name");
    }

    @Test
    void testListAgencies() throws Exception {
        // Given
        Mockito.when(externalReferentialService.listAgencies("system_id", 2)).thenReturn(
            List.of(new ExternalAgencyDto().setIdentifier("id").setName("name").setDescription("description"))
        );

        // When
        ResultActions resultActions =
            this.performGet(
                    getUriBuilder(RestApi.AGENCIES_PATH)
                        .queryParam(RestApi.ARCHIVING_SYSTEM_ID_PARAM, "system_id")
                        .queryParam(RestApi.TENANT_IDENTIFIER_PARAM, "2")
                );

        // Then
        List<ExternalAgencyDto> response = objectMapper.readValue(
            resultActions.andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getName()).isEqualTo("name");
    }

    @Test
    void testListIngestContracts() throws Exception {
        // Given
        Mockito.when(externalReferentialService.listIngestContracts("system_id", 2)).thenReturn(
            List.of(new ExternalIngestContractDto().setIdentifier("id").setName("name").setDescription("description"))
        );

        // When
        ResultActions resultActions =
            this.performGet(
                    getUriBuilder(RestApi.INGEST_CONTRACTS_PATH)
                        .queryParam(RestApi.ARCHIVING_SYSTEM_ID_PARAM, "system_id")
                        .queryParam(RestApi.TENANT_IDENTIFIER_PARAM, "2")
                );

        // Then
        List<ExternalIngestContractDto> response = objectMapper.readValue(
            resultActions.andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getName()).isEqualTo("name");
    }

    @Test
    void testListArchiveProfiles() throws Exception {
        // Given
        Mockito.when(externalReferentialService.listArchiveProfiles("system_id", 2)).thenReturn(
            List.of(new ExternalProfileDto().setIdentifier("id").setName("name").setDescription("description"))
        );

        // When
        ResultActions resultActions =
            this.performGet(
                    getUriBuilder(RestApi.ARCHIVE_PROFILES_PATH)
                        .queryParam(RestApi.ARCHIVING_SYSTEM_ID_PARAM, "system_id")
                        .queryParam(RestApi.TENANT_IDENTIFIER_PARAM, "2")
                );

        // Then
        List<ExternalProfileDto> response = objectMapper.readValue(
            resultActions.andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getName()).isEqualTo("name");
    }

    protected Authentication buildUserAuthenticated() {
        return new AuthenticationToken(
            new AuthUserDto(),
            null,
            null,
            ServicesData.getServicesByName(
                ServicesData.ROLE_GET_EXTERNAL_REFERENTIAL_CONFIG,
                ServicesData.ROLE_GET_EXTERNAL_REFERENTIAL_AGENCIES,
                ServicesData.ROLE_GET_EXTERNAL_REFERENTIAL_INGEST_CONTRACTS,
                ServicesData.ROLE_GET_EXTERNAL_REFERENTIAL_ARCHIVE_PROFILES
            )
        );
    }
}
