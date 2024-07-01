/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.archives.search.common.common.RulesUpdateCommonService;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("unchecked")
public class ArchiveSearchArchiveSearchUnitExportCsvInternalServiceTest {

    @MockBean(name = "objectMapper")
    private ObjectMapper objectMapper;

    @MockBean(name = "unitService")
    private UnitService unitService;

    @MockBean(name = "agencyService")
    private AgencyService agencyService;

    @MockBean(name = "archiveSearchAgenciesInternalService")
    private ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;

    @MockBean(name = "archiveSearchInternalService")
    private ArchiveSearchInternalService archiveSearchInternalService;

    @InjectMocks
    private ArchiveSearchUnitExportCsvInternalService archiveSearchUnitExportCsvInternalService;

    // CSV Export data tests
    public final String FR_ARCHIVE_UNITS_RESULTS_CSV = "data/export-csv/vitam_archive_units_response_fr.csv";
    public final String EN_ARCHIVE_UNITS_RESULTS_CSV = "data/export-csv/vitam_archive_units_response_en.csv";
    public final String VITAM_UNIT_RESULTS = "data/export-csv/vitam_units_response.json";
    public final String VITAM_UNIT_ONE_RESULTS = "data/export-csv/vitam_units_one_response.json";

    // Tests encode special weired chars
    public final String VITAM_UNIT_ONE_RESULT_TO_ENCODE =
        "data/export-csv/vitam_units_one_response_to_encode_end_to_end.json";
    public final String ARCHIVE_UNITS_WITH_CONTENT_TO_ENCODE_CORRECTLY =
        "data/export-csv/vitam_archive_units_response_correctly_encoded.csv";
    public final String VITAM_UNIT_RESULTS_TO_ENCODE = "data/export-csv/vitam_units_response_to_encode.json";

    // Filing Unit
    public final String ARCHIVE_UNITS_WITH_CONTENT_FILING_UNIT =
        "data/export-csv/filing-unit/vitam_archive_units_response_fr.csv";
    public final String VITAM_UNIT_RESULTS_FILING_UNIT = "data/export-csv/filing-unit/vitam_units_one_response.json";
    public final String VITAM_UNIT_ONE_RESULT_FILING_UNIT = "data/export-csv/filing-unit/vitam_units_response.json";

    // Holding Unit
    public final String ARCHIVE_UNITS_WITH_CONTENT_HOLDING_UNIT =
        "data/export-csv/holding-unit/vitam_archive_units_response_fr.csv";
    public final String VITAM_UNIT_RESULTS_HOLDING_UNIT = "data/export-csv/holding-unit/vitam_units_one_response.json";
    public final String VITAM_UNIT_ONE_RESULT_HOLDING_UNIT = "data/export-csv/holding-unit/vitam_units_response.json";

    // Unit With Object
    public final String ARCHIVE_UNITS_WITH_CONTENT_UNIT_WITH_OBJECT =
        "data/export-csv/unit-with-object/vitam_archive_units_response_fr.csv";
    public final String VITAM_UNIT_RESULTS_UNIT_WITH_OBJECT =
        "data/export-csv/unit-with-object/vitam_units_one_response.json";
    public final String VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT =
        "data/export-csv/unit-with-object/vitam_units_response.json";

    // Unit Without Object
    public final String ARCHIVE_UNITS_WITH_CONTENT_UNIT_WITHOUT_OBJECT =
        "data/export-csv/unit-without-object/vitam_archive_units_response_fr.csv";
    public final String VITAM_UNIT_RESULTS_UNIT_WITHOUT_OBJECT =
        "data/export-csv/unit-without-object/vitam_units_one_response.json";
    public final String VITAM_UNIT_ONE_RESULT_UNIT_WITHOUT_OBJECT =
        "data/export-csv/unit-without-object/vitam_units_response.json";

    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archiveSearchUnitExportCsvInternalService = new ArchiveSearchUnitExportCsvInternalService(
            archiveSearchInternalService,
            archiveSearchAgenciesInternalService,
            objectMapper
        );
    }

    private RequestResponse<JsonNode> buildUnitMetadataResponse(String filename)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ArchiveSearchInternalServiceTest.class.getClassLoader().getResourceAsStream(filename);
        Assertions.assertThat(inputStream).isNotNull();
        return RequestResponseOK.getFromJsonNode(
            objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class)
        );
    }

    @Test
    void testExportCSVWithFrThenReturnTheExactExpectedFile() throws Exception {
        // Given
        setUpData();
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS"))
        );
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(
            Objects.requireNonNull(
                ArchiveSearchInternalService.class.getClassLoader().getResourceAsStream(FR_ARCHIVE_UNITS_RESULTS_CSV)
            ).readAllBytes()
        );
        // When
        Resource responseCsv = archiveSearchUnitExportCsvInternalService.exportToCsvSearchArchiveUnitsByCriteria(
            query,
            new VitamContext(1)
        );

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();

        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.size()).isEqualTo(resultsExpected.size());
        Assertions.assertThat(responseCsv).isNotNull();
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFile() throws Exception {
        // Given
        setUpData();
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS"))
        );
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.ENGLISH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(
            Objects.requireNonNull(
                ArchiveSearchInternalService.class.getClassLoader().getResourceAsStream(EN_ARCHIVE_UNITS_RESULTS_CSV)
            ).readAllBytes()
        );
        // When
        Resource responseCsv = archiveSearchUnitExportCsvInternalService.exportToCsvSearchArchiveUnitsByCriteria(
            query,
            new VitamContext(1)
        );

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();

        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.size()).isEqualTo(resultsExpected.size());
        Assertions.assertThat(responseCsv).isNotNull();
    }

    @Test
    public void testExportCSVWithFrAndSpecialCharsThenReturnTheExactExpectedContentCorrectlyEncoded() throws Exception {
        try (MockedStatic mockRulesUpdateCommonService = Mockito.mockStatic(RulesUpdateCommonService.class)) {
            // Given
            when(unitService.searchUnits(any(), any())).thenReturn(
                buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_TO_ENCODE)
            );
            when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

            when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any())).thenReturn(
                List.of(buildAgencyModelDtos())
            );

            RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_TO_ENCODE);
            ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

            mockRulesUpdateCommonService
                .when(() -> RulesUpdateCommonService.fillOriginatingAgencyName(any(), any()))
                .thenReturn(buildArchiveUnits(resultsDto));

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            when(objectMapper.treeToValue(any(), (Class<Object>) any())).thenReturn(
                buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_TO_ENCODE)
            );
            // query
            List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
            SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
            searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
            searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
            searchCriteriaEltDto.setValues(
                List.of(
                    new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"),
                    new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")
                )
            );
            criteriaList.add(searchCriteriaEltDto);

            SearchCriteriaDto query = new SearchCriteriaDto();
            query.setLanguage(Locale.FRENCH.getLanguage());
            query.setSize(20);
            query.setPageNumber(20);
            query.setCriteriaList(criteriaList);

            Resource GivenResourceCsv = new ByteArrayResource(
                Objects.requireNonNull(
                    ArchiveSearchInternalService.class.getClassLoader()
                        .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_TO_ENCODE_CORRECTLY)
                ).readAllBytes()
            );
            // When
            Resource responseCsv = archiveSearchUnitExportCsvInternalService.exportToCsvSearchArchiveUnitsByCriteria(
                query,
                new VitamContext(1)
            );

            // Then
            Assertions.assertThat(responseCsv).isNotNull();
            InputStream inputStream = responseCsv.getInputStream();

            // The exact match of available bytes number
            Assertions.assertThat(GivenResourceCsv.getInputStream().available()).isEqualTo(
                responseCsv.getInputStream().available()
            );
            Assertions.assertThat(inputStream.available()).isPositive();
            // Read the CSV InputStream
            List<String[]> results = readCSVFromInputStream(inputStream);
            List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

            // Two expecting entries here
            //1: The CSV Header
            Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
            //2: The first line content
            Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
            Assertions.assertThat(responseCsv).isNotNull();
        }
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFileAsFilingUnit() throws Exception {
        try (MockedStatic mockRulesUpdateCommonService = Mockito.mockStatic(RulesUpdateCommonService.class)) {
            // Given
            when(unitService.searchUnits(any(), any())).thenReturn(
                buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_FILING_UNIT)
            );
            when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

            when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any())).thenReturn(
                List.of(buildAgencyModelDtos())
            );

            RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_FILING_UNIT);
            ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

            mockRulesUpdateCommonService
                .when(() -> RulesUpdateCommonService.fillOriginatingAgencyName(any(), any()))
                .thenReturn(buildArchiveUnits(resultsDto));

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            when(objectMapper.treeToValue(any(), (Class<Object>) any())).thenReturn(
                buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_FILING_UNIT)
            );
            // query
            List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
            SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
            searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
            searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
            searchCriteriaEltDto.setValues(
                List.of(
                    new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"),
                    new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")
                )
            );
            criteriaList.add(searchCriteriaEltDto);

            SearchCriteriaDto query = new SearchCriteriaDto();
            query.setLanguage(Locale.FRENCH.getLanguage());
            query.setSize(20);
            query.setPageNumber(20);
            query.setCriteriaList(criteriaList);

            Resource GivenResourceCsv = new ByteArrayResource(
                Objects.requireNonNull(
                    ArchiveSearchInternalService.class.getClassLoader()
                        .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_FILING_UNIT)
                ).readAllBytes()
            );
            // When
            Resource responseCsv = archiveSearchUnitExportCsvInternalService.exportToCsvSearchArchiveUnitsByCriteria(
                query,
                new VitamContext(1)
            );

            // Then
            Assertions.assertThat(responseCsv).isNotNull();
            InputStream inputStream = responseCsv.getInputStream();

            // The exact match of available bytes number
            Assertions.assertThat(GivenResourceCsv.getInputStream().available()).isEqualTo(
                responseCsv.getInputStream().available()
            );
            Assertions.assertThat(inputStream.available()).isPositive();
            // Read the CSV InputStream
            List<String[]> results = readCSVFromInputStream(inputStream);
            List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

            // Two expecting entries here
            //1: The CSV Header
            Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
            //2: The first line content
            Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
            Assertions.assertThat(responseCsv).isNotNull();
        }
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFileAsHoldingUnit() throws Exception {
        try (MockedStatic mockRulesUpdateCommonService = Mockito.mockStatic(RulesUpdateCommonService.class)) {
            // Given
            when(unitService.searchUnits(any(), any())).thenReturn(
                buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_HOLDING_UNIT)
            );
            when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

            when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any())).thenReturn(
                List.of(buildAgencyModelDtos())
            );

            RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_HOLDING_UNIT);
            ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

            mockRulesUpdateCommonService
                .when(() -> RulesUpdateCommonService.fillOriginatingAgencyName(any(), any()))
                .thenReturn(buildArchiveUnits(resultsDto));

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            when(objectMapper.treeToValue(any(), (Class<Object>) any())).thenReturn(
                buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_HOLDING_UNIT)
            );
            // query
            List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
            SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
            searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
            searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
            searchCriteriaEltDto.setValues(
                List.of(
                    new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"),
                    new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")
                )
            );
            criteriaList.add(searchCriteriaEltDto);

            SearchCriteriaDto query = new SearchCriteriaDto();
            query.setLanguage(Locale.FRENCH.getLanguage());
            query.setSize(20);
            query.setPageNumber(20);
            query.setCriteriaList(criteriaList);

            Resource GivenResourceCsv = new ByteArrayResource(
                Objects.requireNonNull(
                    ArchiveSearchInternalService.class.getClassLoader()
                        .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_HOLDING_UNIT)
                ).readAllBytes()
            );
            // When
            Resource responseCsv = archiveSearchUnitExportCsvInternalService.exportToCsvSearchArchiveUnitsByCriteria(
                query,
                new VitamContext(1)
            );

            // Then
            Assertions.assertThat(responseCsv).isNotNull();
            InputStream inputStream = responseCsv.getInputStream();

            // The exact match of available bytes number
            Assertions.assertThat(GivenResourceCsv.getInputStream().available()).isEqualTo(
                responseCsv.getInputStream().available()
            );
            Assertions.assertThat(inputStream.available()).isPositive();
            // Read the CSV InputStream
            List<String[]> results = readCSVFromInputStream(inputStream);
            List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

            // Two expecting entries here
            //1: The CSV Header
            Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
            //2: The first line content
            Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
            Assertions.assertThat(responseCsv).isNotNull();
        }
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFileAsUnitWithObject() throws Exception {
        // Given
        try (MockedStatic mockRulesUpdateCommonService = Mockito.mockStatic(RulesUpdateCommonService.class)) {
            when(unitService.searchUnits(any(), any())).thenReturn(
                buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT)
            );
            when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

            when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any())).thenReturn(
                List.of(buildAgencyModelDtos())
            );

            RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_UNIT_WITH_OBJECT);
            ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

            mockRulesUpdateCommonService
                .when(() -> RulesUpdateCommonService.fillOriginatingAgencyName(any(), any()))
                .thenReturn(buildArchiveUnits(resultsDto));

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            when(objectMapper.treeToValue(any(), (Class<Object>) any())).thenReturn(
                buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT)
            );
            // query
            List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
            SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
            searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
            searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
            searchCriteriaEltDto.setValues(
                List.of(
                    new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"),
                    new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")
                )
            );
            criteriaList.add(searchCriteriaEltDto);

            SearchCriteriaDto query = new SearchCriteriaDto();
            query.setLanguage(Locale.FRENCH.getLanguage());
            query.setSize(20);
            query.setPageNumber(20);
            query.setCriteriaList(criteriaList);

            Resource GivenResourceCsv = new ByteArrayResource(
                Objects.requireNonNull(
                    ArchiveSearchInternalService.class.getClassLoader()
                        .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_UNIT_WITH_OBJECT)
                ).readAllBytes()
            );
            // When
            Resource responseCsv = archiveSearchUnitExportCsvInternalService.exportToCsvSearchArchiveUnitsByCriteria(
                query,
                new VitamContext(1)
            );

            // Then
            Assertions.assertThat(responseCsv).isNotNull();
            InputStream inputStream = responseCsv.getInputStream();

            // The exact match of available bytes number
            Assertions.assertThat(GivenResourceCsv.getInputStream().available()).isEqualTo(
                responseCsv.getInputStream().available()
            );
            Assertions.assertThat(inputStream.available()).isPositive();
            // Read the CSV InputStream
            List<String[]> results = readCSVFromInputStream(inputStream);
            List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

            // Two expecting entries here
            //1: The CSV Header
            Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
            //2: The first line content
            Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
            Assertions.assertThat(responseCsv).isNotNull();
        }
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFileAsUnitWithoutObject() throws Exception {
        try (MockedStatic mockRulesUpdateCommonService = Mockito.mockStatic(RulesUpdateCommonService.class)) {
            // Given
            when(unitService.searchUnits(any(), any())).thenReturn(
                buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITHOUT_OBJECT)
            );
            when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

            when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any())).thenReturn(
                List.of(buildAgencyModelDtos())
            );

            RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(
                VITAM_UNIT_RESULTS_UNIT_WITHOUT_OBJECT
            );
            ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

            mockRulesUpdateCommonService
                .when(() -> RulesUpdateCommonService.fillOriginatingAgencyName(any(), any()))
                .thenReturn(buildArchiveUnits(resultsDto));

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            when(objectMapper.treeToValue(any(), (Class<Object>) any())).thenReturn(
                buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_UNIT_WITHOUT_OBJECT)
            );
            // query
            List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
            SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
            searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
            searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
            searchCriteriaEltDto.setValues(
                List.of(
                    new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"),
                    new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")
                )
            );
            criteriaList.add(searchCriteriaEltDto);

            SearchCriteriaDto query = new SearchCriteriaDto();
            query.setLanguage(Locale.FRENCH.getLanguage());
            query.setSize(20);
            query.setPageNumber(20);
            query.setCriteriaList(criteriaList);

            Resource GivenResourceCsv = new ByteArrayResource(
                Objects.requireNonNull(
                    ArchiveSearchInternalService.class.getClassLoader()
                        .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_UNIT_WITHOUT_OBJECT)
                ).readAllBytes()
            );
            // When
            Resource responseCsv = archiveSearchUnitExportCsvInternalService.exportToCsvSearchArchiveUnitsByCriteria(
                query,
                new VitamContext(1)
            );

            // Then
            Assertions.assertThat(responseCsv).isNotNull();
            InputStream inputStream = responseCsv.getInputStream();

            // The exact match of available bytes number
            Assertions.assertThat(GivenResourceCsv.getInputStream().available()).isEqualTo(
                responseCsv.getInputStream().available()
            );
            Assertions.assertThat(inputStream.available()).isPositive();
            // Read the CSV InputStream
            List<String[]> results = readCSVFromInputStream(inputStream);
            List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

            // Two expecting entries here
            //1: The CSV Header
            Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
            //2: The first line content
            Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
            Assertions.assertThat(responseCsv).isNotNull();
        }
    }

    private void setUpData() throws Exception {
        try (MockedStatic mockRulesUpdateCommonService = Mockito.mockStatic(RulesUpdateCommonService.class)) {
            when(unitService.searchUnits(any(), any())).thenReturn(buildUnitMetadataResponse(VITAM_UNIT_RESULTS));
            when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

            when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any())).thenReturn(
                List.of(buildAgencyModelDtos())
            );

            RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_ONE_RESULTS);
            ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

            mockRulesUpdateCommonService
                .when(() -> RulesUpdateCommonService.fillOriginatingAgencyName(any(), any()))
                .thenReturn(buildArchiveUnits(resultsDto));

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            when(objectMapper.treeToValue(any(), (Class<Object>) any())).thenReturn(
                buildVitamUISearchResponseDto(VITAM_UNIT_RESULTS)
            );
        }
    }

    @SneakyThrows
    private List<String[]> readCSVFromInputStream(InputStream inputStream) {
        List<String[]> results;
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            results = reader.readAll();
        } catch (IOException | CsvException e) {
            throw e;
        }
        return results;
    }

    private ArchiveUnit buildArchiveUnits(ResultsDto resultsDto) {
        ArchiveUnit archiveUnit = new ArchiveUnit();
        BeanUtils.copyProperties(resultsDto, archiveUnit);
        return archiveUnit;
    }

    private RequestResponse<JsonNode> buildArchiveUnit(String filename)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream =
            ArchiveSearchArchiveSearchUnitExportCsvInternalServiceTest.class.getClassLoader()
                .getResourceAsStream(filename);
        Assertions.assertThat(inputStream).isNotNull();
        return RequestResponseOK.getFromJsonNode(
            objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class)
        );
    }

    private ResultsDto buildResults(RequestResponse<JsonNode> jsonNodeRequestResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String re = StringUtils.chop(jsonNodeRequestResponse.toJsonNode().get("$results").toString().substring(1));
        return objectMapper.readValue(re, ResultsDto.class);
    }

    private AgencyModelDto buildAgencyModelDtos() {
        AgencyModelDto agencyModelDto = new AgencyModelDto();
        agencyModelDto.setIdentifier("FRAN_NP_009915");
        agencyModelDto.setName("name");
        agencyModelDto.setTenant(1);
        return agencyModelDto;
    }

    AgenciesModel createAgencyModel(String identifier, String name, Integer tenant) {
        AgenciesModel agency = new AgenciesModel();
        agency.setIdentifier(identifier);
        agency.setName(name);
        agency.setTenant(tenant);
        return agency;
    }

    private VitamUISearchResponseDto buildVitamUISearchResponseDto(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream =
            ArchiveSearchArchiveSearchUnitExportCsvInternalServiceTest.class.getClassLoader()
                .getResourceAsStream(filename);
        return objectMapper.readValue(ByteStreams.toByteArray(inputStream), VitamUISearchResponseDto.class);
    }

    private RequestResponse<AgenciesModel> buildAgenciesResponse() {
        List<AgenciesModel> agenciesModelList = List.of(createAgencyModel("FRAN_NP_009915", "Service producteur1", 0));

        return new RequestResponseOK<>(null, agenciesModelList, agenciesModelList.size()).setHttpCode(400);
    }
}
