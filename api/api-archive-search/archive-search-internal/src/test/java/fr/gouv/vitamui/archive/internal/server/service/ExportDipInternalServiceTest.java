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
 *
 *
 */

package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.DataObjectVersionType;
import fr.gouv.vitam.common.model.dip.QualifierVersion;
import fr.gouv.vitam.common.model.export.dip.DipRequest;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Set;

import static fr.gouv.vitam.common.model.administration.DataObjectVersionType.BINARY_MASTER;
import static fr.gouv.vitam.common.model.dip.QualifierVersion.FIRST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class ExportDipInternalServiceTest {

    @Mock
    private ExportDipV2Service exportDipV2Service;

    @Mock
    private ArchiveSearchInternalService archiveSearchInternalService;

    @InjectMocks
    ExportDipInternalService exportDipInternalService;

    @Test
    void dipExport_should_pass() throws Exception {
        //Given
        final Map<DataObjectVersionType, Set<QualifierVersion>> dataObjectVersionsPatterns = Map.of(
            BINARY_MASTER,
            Set.of(FIRST)
        );
        final ExportDipCriteriaDto exportDipCriteriaDto = newExportDipCriteriaDto(
            true,
            "2.2",
            dataObjectVersionsPatterns
        );
        VitamContext vitamContext = newVitamContext();
        String jsonDslQuery =
            "{\"$roots\":[],\"$query\":[{\"$and\":[{\"$eq\":{\"#id\":\"aeaqaaaaaehmay6yaaqhual6ysiaariaaaba\"}}]}],\"$filter\":{\"$limit\":10},\"$projection\":{},\"$facets\":[]}";
        JsonNode dslQuery = newJsonNode(jsonDslQuery);

        Mockito.when(
            archiveSearchInternalService.prepareDslQuery(
                exportDipCriteriaDto.getExportDIPSearchCriteria(),
                vitamContext
            )
        ).thenReturn(dslQuery);
        String requestResponseOKJson =
            "{\"httpCode\":202,\"$hits\":{\"total\":1,\"offset\":0,\"limit\":0,\"size\":1},\"$results\":[{\"itemId\":\"aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq\",\"message\":\"toutestOK\",\"globalStatus\":\"STARTED\",\"globalState\":\"RUNNING\",\"lifecycleEnable\":true}]}";
        RequestResponse<JsonNode> responseReturned = RequestResponseOK.getFromJsonNode(
            newJsonNode(requestResponseOKJson)
        );
        Mockito.when(exportDipV2Service.exportDip(eq(vitamContext), any())).thenReturn(responseReturned);

        //When
        String response = exportDipInternalService.requestToExportDIP(exportDipCriteriaDto, vitamContext);

        //then
        assertThat(response).isEqualTo("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");

        ArgumentCaptor<DipRequest> argumentCaptor = ArgumentCaptor.forClass(DipRequest.class);
        verify(exportDipV2Service).exportDip(any(VitamContext.class), argumentCaptor.capture());
        final DipRequest dipRequest = argumentCaptor.getValue();
        assertThat(dipRequest.isExportWithLogBookLFC()).isTrue();
        assertThat(dipRequest.isExportWithoutObjects()).isFalse();
        assertThat(dipRequest.getSedaVersion()).isEqualTo("2.2");
        assertThat(dipRequest.getDataObjectVersionToExport().getDataObjectVersionsPatterns()).isEqualTo(
            dataObjectVersionsPatterns
        );
    }

    private JsonNode newJsonNode(String json) throws JsonProcessingException {
        return new ObjectMapper().readTree(json);
    }

    private VitamContext newVitamContext() {
        return new VitamContext(1);
    }

    private ExportDipCriteriaDto newExportDipCriteriaDto(
        boolean lifeCycleLogs,
        String sedaVersion,
        Map<DataObjectVersionType, Set<QualifierVersion>> dataObjectVersionsPatterns
    ) {
        final ExportDipCriteriaDto exportDipCriteriaDto = new ExportDipCriteriaDto();
        exportDipCriteriaDto.setLifeCycleLogs(lifeCycleLogs);
        exportDipCriteriaDto.setSedaVersion(sedaVersion);
        exportDipCriteriaDto.setWithoutObjects(false);
        exportDipCriteriaDto.setDataObjectVersionsPatterns(dataObjectVersionsPatterns);
        return exportDipCriteriaDto;
    }
}
