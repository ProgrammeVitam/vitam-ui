/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.archives.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.archives.search.common.dto.ObjectData;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalRestClient;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalWebClient;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchStreamingExternalRestClient;
import fr.gouv.vitamui.commons.api.dtos.OntologyDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierType;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ArchivesSearchServiceTest {

    public final String ARCHIVE_UNITS_RESULTS_CSV = "data/vitam_archive_units_response.csv";
    public final String GOT_PHYSICAL = "data/vitam_got_physical.json";
    public final String GOT_DISSEMINATION = "data/vitam_got_dissemination.json";
    public final String GOT_TEXTCONTENT = "data/vitam_got_textcontent.json";
    public final String GOT_BINARYMASTER_MULTI_QUALIFIERS = "data/vitam_got_binarymaster_multiple_versions.json";
    public final String GOT_BINARYMASTER = "data/vitam_got_binarymaster.json";

    private ArchivesSearchService archivesSearchService;

    @Mock
    private ArchiveSearchExternalRestClient archiveSearchExternalRestClient;

    @Mock
    private ArchiveSearchExternalWebClient archiveSearchExternalWebClient;

    @Mock
    private ArchiveSearchStreamingExternalRestClient archiveSearchStreamingExternalRestClient;

    @Mock
    private CommonService commonService;

    @Mock
    private UnitService unitService;

    @Before
    public void init() {
        archivesSearchService = new ArchivesSearchService(
            commonService,
            archiveSearchExternalRestClient,
            archiveSearchExternalWebClient,
            archiveSearchStreamingExternalRestClient
        );
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void testIngest() {
        Assert.assertNotNull(archivesSearchService);
    }

    @Test
    public void testGetFilingHolding() {
        when(archiveSearchExternalRestClient.getFilingHoldingScheme(ArgumentMatchers.any())).thenReturn(
            new VitamUISearchResponseDto()
        );
        Assert.assertNotNull(archivesSearchService.findFilingHoldingScheme(null));
    }

    @Test
    public void testExportCsv() throws IOException {
        Resource resource = new ByteArrayResource(
            ArchivesSearchServiceTest.class.getClassLoader()
                .getResourceAsStream(ARCHIVE_UNITS_RESULTS_CSV)
                .readAllBytes()
        );
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        SearchCriteriaDto query = new SearchCriteriaDto();

        when(
            archiveSearchExternalRestClient.exportCsvArchiveUnitsByCriteria(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn((new ResponseEntity<>(resource, HttpStatus.OK)));
        Assert.assertNotNull(archivesSearchService.exportCsvArchiveUnitsByCriteria(query, context));
    }

    @Test
    public void should_return_binary_master_file_info()
        throws IOException, InvalidParseOperationException, VitamClientException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(GOT_BINARYMASTER);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        // When
        when(unitService.findObjectMetadataById(any(), any())).thenReturn(jsonNodeRequestResponse);
        when(archivesSearchService.findObjectById(any(), any())).thenReturn(ResponseEntity.of(Optional.of(resultsDto)));
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(ObjectQualifierType.BINARYMASTER.getValue());
        assertThat(objectData.getVersion()).isEqualTo(1);
        assertThat(objectData.getMimeType()).isEqualTo("text/plain");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahl2zz5ab23malq4gw2cnqaaaaq.txt");
    }

    @Test
    public void should_return_version() throws VitamClientException, IOException, InvalidParseOperationException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(GOT_BINARYMASTER);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        // When
        when(unitService.findObjectMetadataById(any(), any())).thenReturn(jsonNodeRequestResponse);
        when(archivesSearchService.findObjectById(any(), any())).thenReturn(ResponseEntity.of(Optional.of(resultsDto)));
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(ObjectQualifierType.BINARYMASTER.getValue());
        assertThat(objectData.getVersion()).isEqualTo(1);
        assertThat(objectData.getMimeType()).isEqualTo("text/plain");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahl2zz5ab23malq4gw2cnqaaaaq.txt");
    }

    @Test
    public void should_return_multi_version() throws VitamClientException, IOException, InvalidParseOperationException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(GOT_BINARYMASTER_MULTI_QUALIFIERS);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        // When
        when(unitService.findObjectMetadataById(any(), any())).thenReturn(jsonNodeRequestResponse);
        when(archivesSearchService.findObjectById(any(), any())).thenReturn(ResponseEntity.of(Optional.of(resultsDto)));
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(ObjectQualifierType.BINARYMASTER.getValue());
        assertThat(objectData.getVersion()).isEqualTo(2);
        assertThat(objectData.getMimeType()).isEqualTo("text/plain");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahl2zz5ab23malq4gw2cnqaaaaq.txt");
    }

    @Test
    public void should_return_dissemination() throws VitamClientException, IOException, InvalidParseOperationException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(GOT_DISSEMINATION);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        // When
        when(unitService.findObjectMetadataById(any(), any())).thenReturn(jsonNodeRequestResponse);
        when(archivesSearchService.findObjectById(any(), any())).thenReturn(ResponseEntity.of(Optional.of(resultsDto)));
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(ObjectQualifierType.DISSEMINATION.getValue());
        assertThat(objectData.getVersion()).isEqualTo(1);
        assertThat(objectData.getMimeType()).isEqualTo("image/png");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahlju6xaayh6alycfih5ziaaaba.png");
    }

    @Test
    public void should_return_dissemination_file_info()
        throws IOException, InvalidParseOperationException, VitamClientException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(GOT_DISSEMINATION);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        // When
        when(unitService.findObjectMetadataById(any(), any())).thenReturn(jsonNodeRequestResponse);
        when(archivesSearchService.findObjectById(any(), any())).thenReturn(ResponseEntity.of(Optional.of(resultsDto)));
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(ObjectQualifierType.DISSEMINATION.getValue());
        assertThat(objectData.getVersion()).isEqualTo(1);
        assertThat(objectData.getMimeType()).isEqualTo("image/png");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahlju6xaayh6alycfih5ziaaaba.png");
    }

    @Test
    public void should_return_null_file_info_when_physical_master()
        throws IOException, InvalidParseOperationException, VitamClientException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(GOT_PHYSICAL);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        // When
        when(unitService.findObjectMetadataById(any(), any())).thenReturn(jsonNodeRequestResponse);
        when(archivesSearchService.findObjectById(any(), any())).thenReturn(ResponseEntity.of(Optional.of(resultsDto)));
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertNull(objectData.getQualifier());
        assertNull(objectData.getVersion());
        assertNull(objectData.getMimeType());
        assertNull(objectData.getFilename());
    }

    @Test
    public void should_return_file_name_of_thumbnail_when_absent_in_binary_and_dissemination()
        throws IOException, InvalidParseOperationException, VitamClientException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot("data/vitam_got_full_with_thumbs.json");
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        // When
        when(unitService.findObjectMetadataById(any(), any())).thenReturn(jsonNodeRequestResponse);
        when(archivesSearchService.findObjectById(any(), any())).thenReturn(ResponseEntity.of(Optional.of(resultsDto)));
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(ObjectQualifierType.THUMBNAIL.getValue());
        assertThat(objectData.getVersion()).isEqualTo(1);
        assertThat(objectData.getMimeType()).isEqualTo("image/jpeg");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahlju6xaayh6alycfih54qaaaba.jpeg");
    }

    @Test
    public void should_return_null_when_filemodel_is_absent_from_all_qualifiers()
        throws IOException, InvalidParseOperationException, VitamClientException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(
            "data/vitam_got_full_qualifiers_without_filemodel.json"
        );
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);
        // When
        when(unitService.findObjectMetadataById(any(), any())).thenReturn(jsonNodeRequestResponse);
        when(archivesSearchService.findObjectById(any(), any())).thenReturn(ResponseEntity.of(Optional.of(resultsDto)));
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertEquals(objectData.getQualifier(), ObjectQualifierType.BINARYMASTER.getValue());
        assertThat(objectData.getVersion()).isEqualTo(1);
        assertThat(objectData.getMimeType()).isEqualTo("text/plain");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahly3l5ab7vwalzlvsew3aaaaaq.txt");
    }

    private ResultsDto buildResults(RequestResponse<JsonNode> jsonNodeRequestResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String re = StringUtils.chop(jsonNodeRequestResponse.toJsonNode().get("$results").toString().substring(1));
        return objectMapper.readValue(re, ResultsDto.class);
    }

    private RequestResponse<JsonNode> buildGot(String filename) throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ArchivesSearchServiceTest.class.getClassLoader().getResourceAsStream(filename);
        return RequestResponseOK.getFromJsonNode(
            objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class)
        );
    }

    @Test
    public void should_return_textcontent() throws VitamClientException, IOException, InvalidParseOperationException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(GOT_TEXTCONTENT);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

        // When
        unitService.findObjectMetadataById(any(), any());
        archivesSearchService.findObjectById(any(), any());
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(ObjectQualifierType.TEXTCONTENT.getValue());
        assertThat(objectData.getVersion()).isEqualTo(3);
        assertThat(objectData.getMimeType()).isEqualTo("image/png");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahlju6xaayh6alycfih5ziaaaba.png");
    }

    @Test
    public void should_return_textcontent_file_info()
        throws IOException, InvalidParseOperationException, VitamClientException {
        // Given
        ObjectData objectData = new ObjectData();
        RequestResponse<JsonNode> jsonNodeRequestResponse = buildGot(GOT_TEXTCONTENT);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

        // When
        unitService.findObjectMetadataById(any(), any());
        archivesSearchService.findObjectById(any(), any());
        archivesSearchService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(ObjectQualifierType.TEXTCONTENT.getValue());
        assertThat(objectData.getVersion()).isEqualTo(3);
        assertThat(objectData.getMimeType()).isEqualTo("image/png");
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahlju6xaayh6alycfih5ziaaaba.png");
    }

    @Test
    public void update_archive_units_rules_should_call_appropriate_rest_client_once() {
        // Given
        Mockito.when(
            archiveSearchExternalRestClient.updateArchiveUnitsRules(
                any(RuleSearchCriteriaDto.class),
                ArgumentMatchers.any()
            )
        ).thenReturn(new ResponseEntity<>(new String(), HttpStatus.OK));
        // When
        archivesSearchService.updateArchiveUnitsRules(new RuleSearchCriteriaDto(), null);

        // Then
        verify(archiveSearchExternalRestClient, Mockito.times(1)).updateArchiveUnitsRules(
            any(RuleSearchCriteriaDto.class),
            ArgumentMatchers.any()
        );
    }

    @Test
    public void launch_computed_inherited_rules_should_call_appropriate_rest_client() {
        // Given
        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        Mockito.when(
            archiveSearchExternalRestClient.computedInheritedRules(any(SearchCriteriaDto.class), ArgumentMatchers.any())
        ).thenReturn(new ResponseEntity<>(new String(), HttpStatus.OK));

        // When
        archivesSearchService.computedInheritedRules(searchCriteriaDto, context);

        // Then
        verify(archiveSearchExternalRestClient, Mockito.times(1)).computedInheritedRules(
            any(SearchCriteriaDto.class),
            ArgumentMatchers.any()
        );
    }

    @Test
    public void select_unit_with_inherited_rules_should_call_appropriate_rest_client() {
        // Given
        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        Mockito.when(
            archiveSearchExternalRestClient.selectUnitWithInheritedRules(
                ArgumentMatchers.any(),
                any(SearchCriteriaDto.class)
            )
        ).thenReturn(new ResponseEntity<>(new ResultsDto(), HttpStatus.OK));

        // When
        archivesSearchService.selectUnitsWithInheritedRules(searchCriteriaDto, context);

        // Then
        verify(archiveSearchExternalRestClient, Mockito.times(1)).selectUnitWithInheritedRules(
            ArgumentMatchers.any(),
            any(SearchCriteriaDto.class)
        );
    }

    @Test
    public void launch_reclassification_should_call_appropriate_rest_client_one_time() {
        // Given
        ReclassificationCriteriaDto reclassificationCriteriaDto = new ReclassificationCriteriaDto();
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        Mockito.when(
            archiveSearchExternalRestClient.reclassification(
                any(ReclassificationCriteriaDto.class),
                ArgumentMatchers.any()
            )
        ).thenReturn(new ResponseEntity<>(new String(), HttpStatus.OK));

        // When
        archivesSearchService.reclassification(reclassificationCriteriaDto, context);

        // Then
        verify(archiveSearchExternalRestClient, Mockito.times(1)).reclassification(
            any(ReclassificationCriteriaDto.class),
            ArgumentMatchers.any()
        );
    }

    @Test
    public void launch_transfer_reply_should_call_appropriate_rest_client_one_time() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        Mockito.when(
            archiveSearchStreamingExternalRestClient.transferAcknowledgment(
                ArgumentMatchers.any(),
                any(String.class),
                any(InputStream.class)
            )
        ).thenReturn(new ResponseEntity<>("OperationId", HttpStatus.OK));

        // When
        archivesSearchService.transferAcknowledgment(eq(context), eq("fileName"), ArgumentMatchers.any());

        // Then
        verify(archiveSearchStreamingExternalRestClient, Mockito.times(1)).transferAcknowledgment(
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        );
    }

    @Test
    public void get_external_ontologies_list_should_call_appropriate_rest_client_one_time() {
        // Given
        List<OntologyDto> ontologiesList = new ArrayList<>();
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        Mockito.when(archiveSearchExternalRestClient.getExternalOntologiesList(ArgumentMatchers.any())).thenReturn(
            ontologiesList
        );

        // When
        archivesSearchService.getExternalOntologiesList(eq(context));

        // Then
        verify(archiveSearchExternalRestClient, Mockito.times(1)).getExternalOntologiesList(ArgumentMatchers.any());
    }
}
