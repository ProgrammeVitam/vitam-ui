/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.ingest.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractCommonService;
import fr.gouv.vitamui.iam.security.authentication.AuthenticationToken;
import fr.gouv.vitamui.iam.security.service.ExternalParametersService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestServiceTest {

    @Mock
    private ExternalParametersService externalParametersService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private AccessContractCommonService accessContractCommonService;

    @Mock
    private IngestAccessContractService ingestAccessContractService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SecurityService securityService;

    @Mock
    private IngestExternalClient ingestExternalClient;

    @InjectMocks
    private IngestService ingestService;

    @BeforeEach
    public void beforeEach() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ingestAccessContractService = new IngestAccessContractService(accessContractCommonService, objectMapper);
        VitamContext vitamContext = new VitamContext(1).setAccessContract("ContratTNR");
        lenient().doReturn(vitamContext).when(externalParametersService).buildVitamContextFromExternalParam();
    }

    /**
     * Test for <a href="https://assistance.programmevitam.fr/plugins/tracker/?aid=13172">#13172 bug</a>
     */
    @Test
    void getAllPaginatedWhenEmptyOriginatingAgenciesAndEveryOriginatingAgencyIsFalse() throws VitamClientException {
        final VitamContext vitamContext = new VitamContext(1);
        final String accessContract = "AccessContract42";
        final String criteria = "{\"evTypeProc\":\"INGEST\"}";
        vitamContext.setAccessContract(accessContract);
        when(externalParametersService.buildVitamContextFromExternalParam()).thenReturn(vitamContext);
        final AccessContractModel accessContractModel = new AccessContractModel();
        accessContractModel.setEveryOriginatingAgency(false);
        accessContractModel.setOriginatingAgencies(Collections.emptySet());

        final LogbookOperation logbookOperation = new LogbookOperation();
        logbookOperation.setEvId("1");
        logbookOperation.setEvents(Collections.emptyList());
        when(logbookService.selectOperations(any(JsonNode.class), any(VitamContext.class))).thenReturn(
            new RequestResponseOK<LogbookOperation>().addResult(logbookOperation).setHttpCode(200)
        );

        Assertions.assertDoesNotThrow(
            () -> ingestService.getAllPaginated(0, 10, Optional.empty(), Optional.empty(), Optional.of(criteria))
        );
    }

    @Test
    void streamingUpload_should_require_scheme_import_role_for_scheme_workflows() {
        authenticateWith(ServicesData.ROLE_CREATE_INGEST);

        for (final String workflow : List.of("HOLDING_SCHEME", "FILING_SCHEME")) {
            Assertions.assertThrows(
                ForbiddenException.class,
                () -> ingestService.streamingUpload(InputStream.nullInputStream(), workflow, "RESUME")
            );
        }
        verifyNoInteractions(ingestExternalClient);
    }

    @Test
    void streamingUpload_should_require_ingest_role_for_other_workflows() {
        authenticateWith(ServicesData.ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE);

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> ingestService.streamingUpload(InputStream.nullInputStream(), "DEFAULT_WORKFLOW", "RESUME")
        );
        verifyNoInteractions(ingestExternalClient);
    }

    @Test
    void streamingUpload_should_ignore_roles_granted_on_other_tenants() {
        // The user has ROLE_CREATE_INGEST on another tenant, but only the scheme import role on the current one
        final AuthUserDto user = new AuthUserDto();
        final GroupDto profileGroup = new GroupDto();
        profileGroup.setProfiles(
            List.of(
                profile(1, ServicesData.ROLE_CREATE_INGEST),
                profile(2, ServicesData.ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE)
            )
        );
        user.setProfileGroup(profileGroup);
        when(securityService.getAuthentication()).thenReturn(
            new AuthenticationToken(user, null, null, List.of(ServicesData.ROLE_CREATE_HOLDING_FILLING_SCHEME_ROLE))
        );

        Assertions.assertThrows(
            ForbiddenException.class,
            () -> ingestService.streamingUpload(InputStream.nullInputStream(), "DEFAULT_WORKFLOW", "RESUME")
        );
        verifyNoInteractions(ingestExternalClient);
    }

    @Test
    void streamingUpload_should_send_the_upload_when_the_role_is_granted() throws Exception {
        authenticateWith(ServicesData.ROLE_CREATE_INGEST);
        when(securityService.getTenantIdentifier()).thenReturn(1);
        when(ingestExternalClient.ingest(any(VitamContext.class), any(InputStream.class), any(), any())).thenReturn(
            new RequestResponseOK<Void>()
        );

        ingestService.streamingUpload(InputStream.nullInputStream(), "DEFAULT_WORKFLOW", "RESUME");

        verify(ingestExternalClient).ingest(any(VitamContext.class), any(InputStream.class), any(), any());
    }

    private void authenticateWith(final String... roles) {
        when(securityService.getAuthentication()).thenReturn(
            new AuthenticationToken(new AuthUserDto(), null, null, List.of(roles))
        );
    }

    private static ProfileDto profile(final int tenantIdentifier, final String roleName) {
        final ProfileDto profile = new ProfileDto();
        profile.setTenantIdentifier(tenantIdentifier);
        profile.setEnabled(true);
        profile.setRoles(List.of(new Role(roleName)));
        return profile;
    }
}
