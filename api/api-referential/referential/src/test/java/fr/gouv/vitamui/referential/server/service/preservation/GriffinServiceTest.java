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
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.preservation.griffin.Griffin;
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

class GriffinServiceTest {

    private static final Griffin GRIFFIN_1 = griffin("id-1", "GRI-000001");
    private static final Griffin GRIFFIN_2 = griffin("id-2", "GRI-000002");
    private static final Griffin GRIFFIN_3 = griffin("id-3", "GRI-000003");

    private GriffinService service;

    private static Griffin griffin(String id, String identifier) {
        return new Griffin(id, 1, 0, identifier, "name-" + id, null, null, null, null, null);
    }

    @BeforeEach
    void setUp() {
        service = spy(
            new GriffinService(
                mock(SecurityService.class),
                mock(AdminExternalClient.class),
                new ObjectMapper(),
                mock(LogbookService.class)
            )
        );
    }

    @Test
    void delete_should_reimport_every_griffin_except_the_deleted_one()
        throws VitamClientException, AccessExternalClientException, IOException {
        doReturn(List.of(GRIFFIN_1, GRIFFIN_2, GRIFFIN_3)).when(service).getAll();
        doReturn(ResponseEntity.accepted().build()).when(service).put(anyList());

        service.delete(GRIFFIN_2);

        ArgumentCaptor<List<Griffin>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).put(captor.capture());
        assertThat(captor.getValue()).containsExactly(GRIFFIN_1, GRIFFIN_3);
    }

    @Test
    void delete_should_reimport_the_whole_referential_when_the_griffin_is_unknown()
        throws VitamClientException, AccessExternalClientException, IOException {
        doReturn(List.of(GRIFFIN_1, GRIFFIN_2)).when(service).getAll();
        doReturn(ResponseEntity.accepted().build()).when(service).put(anyList());

        service.delete(griffin("unknown-id", "GRI-999999"));

        ArgumentCaptor<List<Griffin>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).put(captor.capture());
        assertThat(captor.getValue()).containsExactly(GRIFFIN_1, GRIFFIN_2);
    }

    @Test
    void delete_should_reimport_an_empty_referential_when_the_last_griffin_is_deleted()
        throws VitamClientException, AccessExternalClientException, IOException {
        doReturn(List.of(GRIFFIN_1)).when(service).getAll();
        doReturn(ResponseEntity.accepted().build()).when(service).put(anyList());

        service.delete(GRIFFIN_1);

        ArgumentCaptor<List<Griffin>> captor = ArgumentCaptor.forClass(List.class);
        verify(service).put(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }
}
