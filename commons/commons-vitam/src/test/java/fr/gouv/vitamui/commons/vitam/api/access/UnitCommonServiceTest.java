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
package fr.gouv.vitamui.commons.vitam.api.access;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.commons.api.utils.NonSortableFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitCommonServiceTest {

    @Mock
    private AccessExternalClient accessExternalClient;

    @InjectMocks
    private UnitService unitCommonService;

    private final ObjectMapper mapper = new ObjectMapper();

    @AfterEach
    void resetHolder() {
        NonSortableFields.setNonSortableFields(Map.of());
    }

    @Test
    void searchUnits_stripsBlocklistedOrderByBeforeReachingVitam() throws Exception {
        NonSortableFields.setNonSortableFields(Map.of(NonSortableFields.UNIT_COLLECTION, List.of("Title")));
        when(accessExternalClient.selectUnits(any(), any())).thenReturn(okResponse());
        JsonNode dsl = mapper.readTree("{\"$filter\":{\"$orderby\":{\"Title\":1}}}");

        unitCommonService.searchUnits(dsl, new VitamContext(1));

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(accessExternalClient).selectUnits(any(), captor.capture());
        assertThat(captor.getValue().get("$filter").has("$orderby")).isFalse();
    }

    @Test
    void searchUnits_keepsSortableOrderBy() throws Exception {
        NonSortableFields.setNonSortableFields(Map.of(NonSortableFields.UNIT_COLLECTION, List.of("Title")));
        when(accessExternalClient.selectUnits(any(), any())).thenReturn(okResponse());
        JsonNode dsl = mapper.readTree("{\"$filter\":{\"$orderby\":{\"StartDate\":1}}}");

        unitCommonService.searchUnits(dsl, new VitamContext(1));

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(accessExternalClient).selectUnits(any(), captor.capture());
        assertThat(captor.getValue().get("$filter").get("$orderby").has("StartDate")).isTrue();
    }

    private RequestResponseOK<JsonNode> okResponse() {
        RequestResponseOK<JsonNode> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        return response;
    }
}
