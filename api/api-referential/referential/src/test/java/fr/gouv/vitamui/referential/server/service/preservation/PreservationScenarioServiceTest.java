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

package fr.gouv.vitamui.referential.server.service.preservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.preservation.scenario.PreservationScenario;
import fr.gouv.vitamui.referential.server.security.TenantQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class PreservationScenarioServiceTest {

    private static final PreservationScenario SCENARIO_1 = scenario("PSC-000001");
    private static final PreservationScenario SCENARIO_2 = scenario("PSC-000002");
    private static final PreservationScenario SCENARIO_3 = scenario("PSC-000003");

    private PreservationScenarioService service;

    private static PreservationScenario scenario(String identifier) {
        return new PreservationScenario(identifier, "name-" + identifier, null, null, null, null, null, null);
    }

    @BeforeEach
    void setUp() {
        service = spy(
            new PreservationScenarioService(
                mock(SecurityService.class),
                mock(AdminExternalClient.class),
                new ObjectMapper(),
                mock(TenantQueryService.class)
            )
        );
    }

    @Test
    void delete_should_reimport_every_scenario_except_the_deleted_one()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        doReturn(List.of(SCENARIO_1, SCENARIO_2, SCENARIO_3)).when(service).getAll();
        doReturn(ResponseEntity.accepted().build()).when(service).put(anyList());

        service.delete(SCENARIO_2);

        ArgumentCaptor<List<PreservationScenario>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).put(captor.capture());
        assertThat(captor.getValue()).containsExactly(SCENARIO_1, SCENARIO_3);
    }

    @Test
    void delete_should_reimport_the_whole_referential_when_the_scenario_is_unknown()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        doReturn(List.of(SCENARIO_1, SCENARIO_2)).when(service).getAll();
        doReturn(ResponseEntity.accepted().build()).when(service).put(anyList());

        service.delete(scenario("PSC-999999"));

        ArgumentCaptor<List<PreservationScenario>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).put(captor.capture());
        assertThat(captor.getValue()).containsExactly(SCENARIO_1, SCENARIO_2);
    }

    @Test
    void delete_should_reimport_an_empty_referential_when_the_last_scenario_is_deleted()
        throws VitamClientException, AccessExternalClientException, IOException, InvalidCreateOperationException {
        doReturn(List.of(SCENARIO_1)).when(service).getAll();
        doReturn(ResponseEntity.accepted().build()).when(service).put(anyList());

        service.delete(SCENARIO_1);

        ArgumentCaptor<List<PreservationScenario>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).put(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }
}
