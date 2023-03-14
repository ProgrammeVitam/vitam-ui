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

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class ArchivesSearchAppraisalMgtRulesQueryBuilderServiceTest {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchAppraisalMgtRulesQueryBuilderServiceTest.class);

    public static String SEARCH_QUERY_WITH_UNIT_TYPE = "data/queries/appraisal/search_query_with_unit_type.json";
    public static String SEARCH_QUERY_WITH_ONTOLOGY_FIELD_TYPE_DATE = "data/search_query_with_ontology_field_type_date.json";
    public static String SEARCH_QUERY_WITH_ONTOLOGY_FIELD_TYPE_TEXT = "data/search_query_with_ontology_field_type_text.json";
    public static String SEARCH_QUERY_WITH_OBJECT_PARAMETER =
        "data/queries/appraisal/search_query_with_object_parameter.json";
    public static String SEARCH_QUERY_WITH_OBJECT_PARAMETER_AND_UNIT_TYPE =
        "data/queries/appraisal/search_query_with_object_and_unit_type.json";
    public static String SEARCH_QUERY_WITH_RULE_IDENTIFIER =
        "appraisal/expected-search-query-with-rule-identifier.txt";
    public static String SEARCH_QUERY_WITH_RULE_IDENTIFIER_AND_RULE_STARDATE =
        "appraisal/expected-search-query-with-rule-identifier-and-rule-startDate.txt";
    public static String ARCHIVE_UNIT_WITH_OBJECTS = "ARCHIVE_UNIT_WITH_OBJECTS";
    public static String ARCHIVE_UNIT_WITHOUT_OBJECTS = "ARCHIVE_UNIT_WITHOUT_OBJECTS";
    public static String ARCHIVE_UNIT_HOLDING_UNIT = "ARCHIVE_UNIT_HOLDING_UNIT";
    public static String ARCHIVE_UNIT_FILING_UNIT = "ARCHIVE_UNIT_FILING_UNIT";



    @InjectMocks
    private ArchivesSearchManagementRulesQueryBuilderService archivesSearchManagementRulesQueryBuilderService;

    @InjectMocks
    private ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService;


    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archivesSearchManagementRulesQueryBuilderService = new ArchivesSearchManagementRulesQueryBuilderService();
        archivesSearchFieldsQueryBuilderService = new ArchivesSearchFieldsQueryBuilderService();
    }


    @Test
    public void testFillQueryFromCriteriaListWhenNullCriteriaList() throws InvalidCreateOperationException {
        //Given
        //When
        BooleanQuery query = or();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, null);

        //then
        Assertions.assertTrue(query.getQueries().isEmpty());
    }

    @Test
    public void testFillQueryFromCriteriaListWhenEmptyCriteriaList() throws InvalidCreateOperationException {
        //Given
        //When
        BooleanQuery query = or();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, List.of());

        //then
        Assertions.assertTrue(query.getQueries().isEmpty());
    }

    @Test
    public void testFillQueryFromCriteriaListWhenAppraisalMgtRulesSimpleCriteriaOnDate()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_END_DATE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("2021-11-02T02:50:12.208Z", null)));
        searchCriteriaEltDto.setDataType(ArchiveSearchConsts.CriteriaDataType.INTERVAL.name());
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.IN.name());
        criteriaList.add(searchCriteriaEltDto);
        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        String queryFileStr = loadFileContent("appraisal/one-date-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }


    @Test
    public void testFillQueryFromCriteriaListWhenAppraisalMgtRulesSimpleCriteriaIntervalDate()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_END_DATE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto
            .setValues(List.of(new CriteriaValue("2011-11-02T02:50:12.208Z", "2021-11-02T02:50:12.208Z")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.GTE.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/interval-date-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }


    @Test
    public void testFillQueryFromCriteriaListWhenAppraisalMgtRulesSimpleCriteriaRuleCode()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/identifier-rule-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenAppraisalMgtRulesWithOnlyInheritedRules()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto
            .setCriteria(ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_INHERITE_AT_LEAST_ONE.name())));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/identifier-inherited-only-rule-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }


    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenAppraisalMgtRulesWithOnlyInheritedOrScopedRules()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_INHERITE_AT_LEAST_ONE.name())));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto
            .setValues(List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_AT_LEAST_ONE.name())));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/identifier-inherited-or-scoped-rule-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }


    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenAppraisalMgtRulesInWaitingToCalculate()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_INHERITE_AT_LEAST_ONE.name())));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_WAITING_RECALCULATE.name())));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/identifier-waiting-to-recalculate-rule-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenAppraisalMgtRulesHasNoRule()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_INHERITE_AT_LEAST_ONE.name())));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto
            .setValues(List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_NO_ONE.name())));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/identifier-no-rules-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenAppraisalMgtRulesHasFinalActionElimination()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_ORIGIN_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_INHERITE_AT_LEAST_ONE.name())));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_FINAL_ACTION_TYPE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto
            .setValues(List.of(new CriteriaValue(ArchiveSearchConsts.FINAL_ACTION_TYPE_ELIMINATION)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/identifier-final-action-elimination-rules-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenAppraisalMgtRulesHasFinalActionKeep()
        throws InvalidCreateOperationException, IOException {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.RULE_IDENTIFIER);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("APP-001")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(
            ArchiveSearchConsts.RULE_FINAL_ACTION_TYPE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto
            .setValues(List.of(new CriteriaValue(ArchiveSearchConsts.FINAL_ACTION_TYPE_KEEP)));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);
        //then

        Assertions.assertFalse(query.getQueries().isEmpty());
        Assertions.assertEquals(query.getQueries().size(), 2);
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/identifier-final-action-keep-rules-query.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenEliminationAnalysisTechnicalIdentifierThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE);
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
            JsonHandler
                .getFromFile(PropertiesUtils.findFile("data/queries/appraisal/elimination_analysis_search.json"));
        JSONAssert
            .assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(),
                true);

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenAppraisalMgtRulesHasStartDateForControlThenReturnTheExactQuery()
        throws InvalidCreateOperationException, IOException {

        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_START_DATE);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(List.of(new CriteriaValue("2021-11-08T23:00:00.000Z")));
        searchCriteriaEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        Assertions.assertEquals(query.getQueries().size(), 1);
        String queryStr = query.getQueries().toString();
        LOGGER.info(queryStr);
        String queryFileStr = loadFileContent("appraisal/vitam_query_with_start_date.txt");
        Assertions.assertEquals(queryStr.trim(), queryFileStr.trim());

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenArchiveUnitTypeIsPresentThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ARCHIVE_UNIT_HOLDING_UNIT), new CriteriaValue(ARCHIVE_UNIT_FILING_UNIT)));
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_QUERY_WITH_UNIT_TYPE));
        JSONAssert
            .assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(),
                true);

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenObjectParameterIsPresentThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ARCHIVE_UNIT_WITH_OBJECTS), new CriteriaValue(ARCHIVE_UNIT_HOLDING_UNIT)));
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_QUERY_WITH_OBJECT_PARAMETER));
        JSONAssert
            .assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(),
                true);

    }

    @Test
    public void testfillQueryFromAppraisalCriteriaListWhenObjectParameterAndUnitTypeArePresentThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ARCHIVE_UNIT_WITH_OBJECTS), new CriteriaValue(ARCHIVE_UNIT_WITHOUT_OBJECTS)));
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService
            .fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_QUERY_WITH_OBJECT_PARAMETER_AND_UNIT_TYPE));
        JSONAssert
            .assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(),
                true);

    }

    @Test
    public void testFillQueryFromCriteriaListRuleIdentifierIsPresentThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("Rule1")));
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);


        //then
        assertThat(query.getQueries().isEmpty()).isFalse();
        assertThat(query.getQueries().size()).isEqualTo(1);
        String queryStr = query.getQueries().toString();
        String queryFileStr = loadFileContent(SEARCH_QUERY_WITH_RULE_IDENTIFIER);
        assertThat(queryStr.trim()).isEqualTo(queryFileStr.trim());

    }


    @Test
    public void testFillQueryFromCriteriaListRuleIdentifierAndRuleStartDateArePresentThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("Rule1")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaEltDto searchCriteriaWithDateEltDto = new SearchCriteriaEltDto();
        searchCriteriaWithDateEltDto.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_START_DATE);
        searchCriteriaWithDateEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
        searchCriteriaWithDateEltDto.setValues(List.of(new CriteriaValue("2080-05-08T23:00:00.000Z")));
        searchCriteriaWithDateEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());

        criteriaList.add(searchCriteriaWithDateEltDto);

        //When
        BooleanQuery query = and();
        archivesSearchManagementRulesQueryBuilderService
            .fillQueryFromMgtRulesCriteriaList(query, criteriaList);


        //then
        assertThat(query.getQueries().isEmpty()).isFalse();
        assertThat(query.getQueries().size()).isEqualTo(2);
        String queryStr = query.getQueries().toString();
        String queryFileStr = loadFileContent(SEARCH_QUERY_WITH_RULE_IDENTIFIER_AND_RULE_STARDATE);
        assertThat(queryStr.trim()).isEqualTo(queryFileStr.trim());

    }

    private String loadFileContent(String filename) throws IOException {
        InputStream inputStream = ArchivesSearchAppraisalMgtRulesQueryBuilderServiceTest.class.getClassLoader()
            .getResourceAsStream("data/queries/" + filename);
        String fileContent = readFromInputStream(inputStream);
        return fileContent;
    }
    @Test
    void testFillQueryFromCriteriaListWithOntologyFieldTypeDateThenReturnTheExactQueryWithoutException()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ARCHIVE_UNIT_HOLDING_UNIT), new CriteriaValue(ARCHIVE_UNIT_FILING_UNIT)));

        SearchCriteriaEltDto searchCriteriaEltDtoWithOntology = new SearchCriteriaEltDto();
        searchCriteriaEltDtoWithOntology.setCriteria("ontologyField");
        searchCriteriaEltDtoWithOntology.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDtoWithOntology.setDataType(ArchiveSearchConsts.CriteriaDataType.DATE.name());
        searchCriteriaEltDtoWithOntology.setValues(List.of(new CriteriaValue("2080-05-08T23:00:00.000Z")));
        searchCriteriaEltDtoWithOntology.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());

        criteriaList.add(searchCriteriaEltDto);
        criteriaList.add(searchCriteriaEltDtoWithOntology);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService.
            fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_QUERY_WITH_ONTOLOGY_FIELD_TYPE_DATE));
        JSONAssert
            .assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(),
                true);

    }

    @Test
    void testFillQueryFromCriteriaListWithOntologyFieldTypeTextThenReturnTheExactQueryWithoutException()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue(ARCHIVE_UNIT_HOLDING_UNIT), new CriteriaValue(ARCHIVE_UNIT_FILING_UNIT)));

        SearchCriteriaEltDto searchCriteriaEltDtoWithOntology = new SearchCriteriaEltDto();
        searchCriteriaEltDtoWithOntology.setCriteria("ontologyFieldText");
        searchCriteriaEltDtoWithOntology.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDtoWithOntology.setDataType(ArchiveSearchConsts.CriteriaDataType.STRING.name());
        searchCriteriaEltDtoWithOntology.setValues(List.of(new CriteriaValue("test test value")));
        searchCriteriaEltDtoWithOntology.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());

        criteriaList.add(searchCriteriaEltDto);
        criteriaList.add(searchCriteriaEltDtoWithOntology);

        //When
        BooleanQuery query = and();
        archivesSearchFieldsQueryBuilderService.
            fillQueryFromCriteriaList(query, criteriaList);

        //then
        Assertions.assertFalse(query.getQueries().isEmpty());
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_QUERY_WITH_ONTOLOGY_FIELD_TYPE_TEXT));
        JSONAssert
            .assertEquals(expectedQuery.toPrettyString(), JsonHandler.getFromString(query.toString()).toPrettyString(),
                true);

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
