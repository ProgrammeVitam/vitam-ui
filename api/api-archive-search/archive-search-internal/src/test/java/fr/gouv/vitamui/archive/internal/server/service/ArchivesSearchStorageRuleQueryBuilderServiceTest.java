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

package fr.gouv.vitamui.archive.internal.server.service;

import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitamui.archive.internal.server.utils.FileReader;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.fillQueryFromMgtRulesCriteriaList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ArchivesSearchStorageRuleQueryBuilderServiceTest {

    public static String SEARCH_QUERY_WITH_RULE_IDENTIFIER =
        "storage/expected-search-query-with-rule-identifier.txt";
    public static String SEARCH_QUERY_WITH_RULE_IDENTIFIER_AND_RULE_START_DATE =
        "storage/expected-search-query-with-rule-identifier-and-rule-startDate.txt";

    public static String SEARCH_QUERY_WITH_RULES_IDENTIFIERS =
        "storage/expected-search-query-with-rules-identifiers.txt";

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchStorageRuleQueryBuilderServiceTest.class);

    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void testFillQueryFromCriteriaListRuleIdentifierIsPresentThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.MANAGEMENT_RULE_IDENTIFIER_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.STORAGE_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("StorageRuleIdentifier")));
        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        fillQueryFromMgtRulesCriteriaList(query, criteriaList);


        //then
        assertThat(query.getQueries()).isNotEmpty();
        assertThat(query.getQueries()).hasSize(1);
        String queryStr = query.getQueries().toString();
        String queryFileStr = FileReader.loadFileContent(SEARCH_QUERY_WITH_RULE_IDENTIFIER);
        assertThat(queryStr.trim()).isEqualTo(queryFileStr.trim());

    }

    @Test
    void testFillQueryFromCriteriaListRuleIdentifierAndRuleStartDateArePresentThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.MANAGEMENT_RULE_IDENTIFIER_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.STORAGE_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("StorageRuleIdentifier")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaEltDto searchCriteriaWithDateEltDto = new SearchCriteriaEltDto();
        searchCriteriaWithDateEltDto.setCriteria(ArchiveSearchConsts.MANAGEMENT_RULE_START_DATE);
        searchCriteriaWithDateEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.ACCESS_RULE);
        searchCriteriaWithDateEltDto.setValues(List.of(new CriteriaValue("2022-05-24T23:00:00.000Z")));
        searchCriteriaWithDateEltDto.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());

        criteriaList.add(searchCriteriaWithDateEltDto);

        //When
        BooleanQuery query = and();
        fillQueryFromMgtRulesCriteriaList(query, criteriaList);


        //then
        assertThat(query.getQueries()).isNotEmpty();
        assertThat(query.getQueries()).hasSize(2);
        String queryStr = query.getQueries().toString();
        String queryFileStr = FileReader.loadFileContent(SEARCH_QUERY_WITH_RULE_IDENTIFIER_AND_RULE_START_DATE);
        assertThat(queryStr.trim()).isEqualTo(queryFileStr.trim());

    }

    @Test
    void testFillQueryFromCriteriaListOfRulesIdentifiersArePresentThenReturnTheExactQuery()
        throws Exception {
        //Given
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.MANAGEMENT_RULE_IDENTIFIER_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.STORAGE_RULE);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("StorageRuleFirstIdentifier"), new CriteriaValue("StorageRuleSecondIdentifier")));

        criteriaList.add(searchCriteriaEltDto);

        //When
        BooleanQuery query = and();
        fillQueryFromMgtRulesCriteriaList(query, criteriaList);


        //then
        assertThat(query.getQueries()).isNotEmpty();
        assertThat(query.getQueries()).hasSize(1);
        String queryStr = query.getQueries().toString();
        String queryFileStr = FileReader.loadFileContent(SEARCH_QUERY_WITH_RULES_IDENTIFIERS);
        assertThat(queryStr.trim()).isEqualTo(queryFileStr.trim());

    }
}
