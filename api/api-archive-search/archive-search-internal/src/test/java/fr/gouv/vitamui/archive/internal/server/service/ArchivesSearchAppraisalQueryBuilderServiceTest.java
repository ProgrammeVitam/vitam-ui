/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
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

package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class ArchivesSearchAppraisalQueryBuilderServiceTest {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchAppraisalQueryBuilderServiceTest.class);

    public static String SEARCH_QUERY_WITH_UNIT_TYPE = "data/queries/search_query_with_unit_type.json";
    public static String SEARCH_QUERY_WITH_OBJECT_PARAMETER = "data/queries/search_query_with_object_parameter.json";
    public static String SEARCH_QUERY_WITH_OBJECT_PARAMETER_AND_UNIT_TYPE =
        "data/queries/search_query_with_object_and_unit_type.json";
    public static String ARCHIVE_UNIT_WITH_OBJECTS = "ARCHIVE_UNIT_WITH_OBJECTS";
    public static String ARCHIVE_UNIT_WITHOUT_OBJECTS = "ARCHIVE_UNIT_WITHOUT_OBJECTS";
    public static String ARCHIVE_UNIT_HOLDING_UNIT = "ARCHIVE_UNIT_HOLDING_UNIT";
    public static String ARCHIVE_UNIT_FILING_UNIT = "ARCHIVE_UNIT_FILING_UNIT";


    @InjectMocks
    private ArchivesSearchAppraisalQueryBuilderService archivesSearchAppraisalQueryBuilderService;

    @InjectMocks
    private ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService;


    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archivesSearchAppraisalQueryBuilderService = new ArchivesSearchAppraisalQueryBuilderService();
        archivesSearchFieldsQueryBuilderService = new ArchivesSearchFieldsQueryBuilderService();
    }


    @Test
    void testFillQueryFromCriteriaListWhenNullCriteriaList() throws InvalidCreateOperationException {
        //Given
        //When
        BooleanQuery query = or();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, null);

        //then
        Assertions.assertTrue(query.getQueries().isEmpty());
    }

    @Test
    void testFillQueryFromCriteriaListWhenEmptyCriteriaList() throws InvalidCreateOperationException {
        //Given
        //When
        BooleanQuery query = or();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, List.of());

        //then
        Assertions.assertTrue(query.getQueries().isEmpty());
    }

    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesSimpleCriteriaOnDate()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_END_DATE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("2021-11-02T02:50:12.208Z", null)));
        searchCriteriaEltDto.setDataType(ArchiveSearchConsts.CriteriaDataType.INTERVAL.name());
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.IN.name());
        criteriaList.add(searchCriteriaEltDto);
        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        String queryFileStr = loadFileContent("one-date-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }


    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesSimpleCriteriaIntervalDate()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_END_DATE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto
            .setValues(List.of(new CriteriaValue("2011-11-02T02:50:12.208Z", "2021-11-02T02:50:12.208Z")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.GTE.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("interval-date-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }


    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesSimpleCriteriaRuleCode()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("identifier-rule-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesWithOnlyInheritedRules()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto
            .setCriteria(
                ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name());
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("identifier-inherited-only-rule-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }


    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesWithOnlyInheritedOrScopedRules()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name());
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE.name());
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("identifier-inherited-or-scoped-rule-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }


    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesInWaitingToCalculate()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name());
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE.name());
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("identifier-waiting-to-recalculate-rule-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesHasNoRule()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name());
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_NO_ONE.name());
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("identifier-no-rules-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesHasFinalActionElimination()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name());
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION_TYPE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto
            .setValues(List.of(new CriteriaValue(ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("identifier-final-action-elimination-rules-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesHasFinalActionKeep()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION_TYPE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto
            .setValues(List.of(new CriteriaValue(ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchAppraisalQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        assertThat(query.getQueries().size()).isEqualTo(2);
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("identifier-final-action-keep-rules-query.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    void testFillQueryFromCriteriaListWhenEliminationAnalysisTechnicalIdentifierThenReturnTheExactQuery() throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ELIMINATION_TECHNICAL_ID);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("guid")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/elimination_analysis_search.json"));
        JSONAssert.assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(), true);

    }

    @Test
    void testFillQueryFromCriteriaListWhenAppraisalMgtRulesHasStartDateForControlThenReturnTheExactQuery()
        throws InvalidCreateOperationException, IOException  {

        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_START_DATE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("2021-11-08T23:00:00.000Z")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        //assertEquals(query.getQueries().size(), 1);
        assertThat(query.getQueries().size()).isEqualTo(1);
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("vitam_query_with_start_date.txt");
        assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    void testFillQueryFromCriteriaListWhenArchiveUnitTypeIsPresentThenReturnTheExactQuery() throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ARCHIVE_UNIT_HOLDING_UNIT), new CriteriaValue(ARCHIVE_UNIT_FILING_UNIT)));
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_QUERY_WITH_UNIT_TYPE));
        JSONAssert.assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(), true);

    }

    @Test
    void testFillQueryFromCriteriaListWhenObjectParameterIsPresentThenReturnTheExactQuery() throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ARCHIVE_UNIT_WITH_OBJECTS), new CriteriaValue(ARCHIVE_UNIT_HOLDING_UNIT)));
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_QUERY_WITH_OBJECT_PARAMETER));
        JSONAssert.assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(), true);

    }

    @Test
    void testFillQueryFromCriteriaListWhenObjectParameterAndUnitTypeArePresentThenReturnTheExactQuery() throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue(ARCHIVE_UNIT_WITH_OBJECTS), new CriteriaValue(ARCHIVE_UNIT_WITHOUT_OBJECTS)));
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_QUERY_WITH_OBJECT_PARAMETER_AND_UNIT_TYPE));
        JSONAssert.assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(), true);

    }

    private String loadFileContent(String filename) throws IOException {
        InputStream inputStream = ArchivesSearchAppraisalQueryBuilderServiceTest.class.getClassLoader()
            .getResourceAsStream("data/queries/" + filename);
        String fileContent = readFromInputStream(inputStream);
        return fileContent;
    }

    private String readFromInputStream(InputStream inputStream)
        throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
            = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
