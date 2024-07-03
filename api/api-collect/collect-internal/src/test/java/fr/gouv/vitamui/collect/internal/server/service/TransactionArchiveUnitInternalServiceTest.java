/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.internal.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TransactionArchiveUnitInternalServiceTest {

    TransactionArchiveUnitInternalService transactionArchiveUnitInternalService;

    @Mock
    CollectService collectService;

    @Mock
    AgencyService agencyService;

    final PodamFactory factory = new PodamFactoryImpl();
    final VitamContext vitamContext = new VitamContext(1);
    ObjectMapper objectMapper = new ObjectMapper();
    public final String TRANSACTION_ID = "TRANSACTION_ID_FOR_LIFE";
    public final String VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT =
        "data/updateCollectArchiveUnits/vitam_units_response.json";
    public final String VITAM_UNIT_ONE_RESULTS = "data/updateCollectArchiveUnits/vitam_units_one_response.json";

    @BeforeEach
    public void beforeEach() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        transactionArchiveUnitInternalService = new TransactionArchiveUnitInternalService(
            collectService,
            agencyService,
            objectMapper
        );
    }

    @Test
    void shouldSearchArchiveUnitsByCriteriaWithSuccess()
        throws VitamClientException, InvalidParseOperationException, InvalidCreateOperationException, IOException {
        // GIVEN
        final VitamUIArchiveUnitResponseDto vitamUIArchiveUnitResponseDto = factory.manufacturePojo(
            VitamUIArchiveUnitResponseDto.class
        );
        RequestResponseOK<VitamUIArchiveUnitResponseDto> responseFromVitam = new RequestResponseOK<>();
        responseFromVitam.setHttpCode(200);
        responseFromVitam.setHits(1, 1, 1, 1);
        responseFromVitam.addResult(vitamUIArchiveUnitResponseDto);

        when(collectService.searchUnitsByTransactionId(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT)
        );
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        transactionArchiveUnitInternalService = new TransactionArchiveUnitInternalService(
            collectService,
            agencyService,
            objectMapper
        );
        SearchCriteriaDto searchQuery = new SearchCriteriaDto();

        // WHEN
        ArchiveUnitsDto archiveUnitsDto = transactionArchiveUnitInternalService.searchArchiveUnitsByCriteria(
            TRANSACTION_ID,
            searchQuery,
            vitamContext
        );

        // THEN
        assertNotNull(archiveUnitsDto);
        assertNotNull(archiveUnitsDto.getArchives());
        assertThat(archiveUnitsDto.getArchives().getResults().size()).isGreaterThan(0);
    }

    @Test
    void shouldExportToCsvSearchArchiveUnitsByCriteriaWithSuccess()
        throws VitamClientException, InvalidParseOperationException, IOException {
        // GIVEN
        final VitamUIArchiveUnitResponseDto vitamUIArchiveUnitResponseDto = factory.manufacturePojo(
            VitamUIArchiveUnitResponseDto.class
        );
        RequestResponseOK<VitamUIArchiveUnitResponseDto> responseFromVitam = new RequestResponseOK<>();
        responseFromVitam.setHttpCode(200);
        responseFromVitam.setHits(1, 1, 1, 1);
        responseFromVitam.addResult(vitamUIArchiveUnitResponseDto);

        when(collectService.searchUnitsByTransactionId(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT)
        );
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        transactionArchiveUnitInternalService = new TransactionArchiveUnitInternalService(
            collectService,
            agencyService,
            objectMapper
        );

        ArrayList agencies = factory.manufacturePojo(ArrayList.class, AgenciesModel.class);
        when(agencyService.findAgencies(any(), any())).thenReturn(
            new RequestResponseOK<>(null, agencies, agencies.size()).setHttpCode(400)
        );

        final SearchCriteriaDto searchQuery = new SearchCriteriaDto();

        // THEN
        assertDoesNotThrow(
            () ->
                transactionArchiveUnitInternalService.exportToCsvSearchArchiveUnitsByCriteria(
                    TRANSACTION_ID,
                    searchQuery,
                    vitamContext
                )
        );
    }

    @Test
    void shouldFindArchiveUnitByIdWithSuccess()
        throws VitamClientException, InvalidParseOperationException, IOException {
        // GIVEN
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_ONE_RESULTS);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        String unitId = "id";
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(collectService.findUnitById(unitId, vitamContext)).thenReturn(
            new RequestResponseOK<JsonNode>().setHttpCode(200).addResult(JsonHandler.toJsonNode(resultsDto))
        );
        transactionArchiveUnitInternalService = new TransactionArchiveUnitInternalService(
            collectService,
            agencyService,
            objectMapper
        );
        // THEN
        assertThatCode(
            () -> transactionArchiveUnitInternalService.findArchiveUnitById(unitId, vitamContext)
        ).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenFindArchiveUnitById() throws VitamClientException {
        // GIVEN
        String unitId = "id";
        when(collectService.findUnitById(unitId, vitamContext)).thenThrow(new VitamClientException("FAKE EXCEPTION!"));
        // THEN
        assertThrows(
            VitamClientException.class,
            () -> transactionArchiveUnitInternalService.findArchiveUnitById(unitId, vitamContext)
        );
    }

    @Test
    void test_find_object_group_by_id() throws VitamClientException {
        String resultStringValue =
            "{\n" +
            "  \"httpCode\": 200,\n" +
            "  \"$hits\": {\n" +
            "    \"total\": 1,\n" +
            "    \"offset\": 0,\n" +
            "    \"limit\": 0,\n" +
            "    \"size\": 1\n" +
            "  },\n" +
            "  \"$results\": [\n" +
            "    {\n" +
            "      \"_id\": \"aebaaaaaaehjuynkaa3goamemgtl6wiaaaba\",\n" +
            "      \"_tenant\": 1,\n" +
            "          \"_nbc\": 1,\n" +
            "      \"FileInfo\": {\n" +
            "        \"Filename\": \"file1.pem\"\n" +
            "      },\n" +
            "      \"_opi\": \"aeeaaaaaaghjuynkaa3goamemgtj73yaaaaq\",\n" +
            "      \"_qualifiers\": [\n" +
            "        {\n" +
            "          \"qualifier\": \"BinaryMaster\",\n" +
            "          \"_nbc\": 1,\n" +
            "          \"versions\": [\n" +
            "            {\n" +
            "              \"_id\": \"aebqaaaaaghjuynkaa3goamemgtl6wiaaaaq\",\n" +
            "              \"DataObjectVersion\": \"BinaryMaster_1\",\n" +
            "              \"FormatIdentification\": {\n" +
            "                \"FormatLitteral\": \"Plain Text File\",\n" +
            "                \"MimeType\": \"text/plain\",\n" +
            "                \"FormatId\": \"x-fmt/111\"\n" +
            "              },\n" +
            "              \"FileInfo\": {\n" +
            "                \"Filename\": \"file1.pem\"\n" +
            "              },\n" +
            "              \"Size\": 2313,\n" +
            "              \"Uri\": \"Content/aebqaaaaaghjuynkaa3goamemgtl6wiaaaaq.pem\",\n" +
            "              \"MessageDigest\": \"5391974484dbd1a3a9c4d3892f4bfc19c4b79fd4b27e8059ce92ca742e7f627b9f6dfa7a9c27484254214615210e796eeb29440da97251388a942a3d581c594e\",\n" +
            "              \"Algorithm\": \"SHA-512\",\n" +
            "              \"_opi\": \"aeeaaaaaaghjuynkaa3goamemgtj73yaaaaq\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"_acd\": \"2022-11-10T13:07:08.784\",\n" +
            "      \"_aud\": \"2022-11-10T13:07:09.039\",\n" +
            "      \"_v\": 1,\n" +
            "      \"_av\": 1\n" +
            "    }\n" +
            "  ],\n" +
            "  \"$facetResults\": [],\n" +
            "  \"$context\": {}\n" +
            "}";
        RequestResponse<JsonNode> mockResponse = RequestResponse.parseFromResponse(
            Response.ok(resultStringValue).build()
        );
        Mockito.when(collectService.getObjectById(any(), eq("aebaaaaaaehjuynkaa3goamemgtl6wiaaaba"))).thenReturn(
            mockResponse
        );

        ResultsDto resultsDto = transactionArchiveUnitInternalService.findObjectGroupById(
            "aebaaaaaaehjuynkaa3goamemgtl6wiaaaba",
            null
        );

        org.junit.jupiter.api.Assertions.assertEquals(resultsDto.getId(), "aebaaaaaaehjuynkaa3goamemgtl6wiaaaba");
        org.junit.jupiter.api.Assertions.assertEquals(resultsDto.getOpi(), "aeeaaaaaaghjuynkaa3goamemgtj73yaaaaq");
        org.junit.jupiter.api.Assertions.assertEquals(resultsDto.getQualifiers().size(), 1);
        org.junit.jupiter.api.Assertions.assertEquals(resultsDto.getQualifiers().get(0).getQualifier(), "BinaryMaster");
        org.junit.jupiter.api.Assertions.assertEquals(resultsDto.getQualifiers().get(0).getVersions().size(), 1);
        org.junit.jupiter.api.Assertions.assertEquals(
            resultsDto.getQualifiers().get(0).getVersions().get(0).getFileInfoModel().getFilename(),
            "file1.pem"
        );
        org.junit.jupiter.api.Assertions.assertEquals(
            resultsDto.getQualifiers().get(0).getVersions().get(0).getFormatIdentification().getMimeType(),
            "text/plain"
        );
        org.junit.jupiter.api.Assertions.assertEquals(
            resultsDto.getQualifiers().get(0).getVersions().get(0).getDataObjectVersion(),
            "BinaryMaster_1"
        );
    }

    private ResultsDto buildResults(RequestResponse<JsonNode> jsonNodeRequestResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String re = StringUtils.chop(jsonNodeRequestResponse.toJsonNode().get("$results").toString().substring(1));
        return objectMapper.readValue(re, ResultsDto.class);
    }

    private RequestResponseOK<JsonNode> buildUnitMetadataResponse(String filename)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream =
            TransactionArchiveUnitInternalServiceTest.class.getClassLoader().getResourceAsStream(filename);
        assertThat(inputStream).isNotNull();
        return RequestResponseOK.getFromJsonNode(
            objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class)
        );
    }

    private RequestResponse<JsonNode> buildArchiveUnit(String filename)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream =
            TransactionArchiveUnitInternalServiceTest.class.getClassLoader().getResourceAsStream(filename);
        Assertions.assertThat(inputStream).isNotNull();
        return RequestResponseOK.getFromJsonNode(
            objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class)
        );
    }
}
