/*
 * *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 *  * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *  *
 *  * contact@programmevitam.fr
 *  *
 *  * This software is a computer program whose purpose is to implement
 *  * implement a digital archiving front-office system for the secure and
 *  * efficient high volumetry VITAM solution.
 *  *
 *  * This software is governed by the CeCILL-C license under French law and
 *  * abiding by the rules of distribution of free software.  You can  use,
 *  * modify and/ or redistribute the software under the terms of the CeCILL-C
 *  * license as circulated by CEA, CNRS and INRIA at the following URL
 *  * "http://www.cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and  rights to copy,
 *  * modify and redistribute granted by the license, users are provided only
 *  * with a limited warranty  and the software's author,  the holder of the
 *  * economic rights,  and the successive licensors  have only  limited
 *  * liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated
 *  * with loading,  using,  modifying and/or developing or reproducing the
 *  * software by the user in light of its specific status of free software,
 *  * that may mean  that it is complicated to manipulate,  and  that  also
 *  * therefore means  that it is reserved for developers  and  experienced
 *  * professionals having in-depth computer knowledge. Users are therefore
 *  * encouraged to load and test the software's suitability as regards their
 *  * requirements in conditions enabling the security of their systems and/or
 *  * data to be ensured and,  more generally, to use and operate it in the
 *  * same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had
 *  * knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.archive.search.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class VitamQueryHelperTest {

    public final String SEARCH_WITH_ONE_NAME_QUERY = "data/query_with_one_name_criteria.json";
    public final String SEARCH_WITH_LIST_OF_NAME_QUERY = "data/query_with_list_of_names_criteria.json";
    public final String SEARCH_WITH_IDENTIFIER_QUERY = "data/query_with_identifier_criteria.json";
    public final String SEARCH_WITH_NAME_AND_IDENTIFIER_QUERY = "data/query_with_identifier_name_criteria.json";

    @Test
    void search_query_with_one_originating_agency_name_as_criteria()
        throws FileNotFoundException, InvalidParseOperationException, InvalidCreateOperationException {
        Map<String, Object> searchCriteriaMap = new HashMap<>();
        List<String> originAgenciesNames = new ArrayList<>();
        originAgenciesNames.add("originating agency name");
        searchCriteriaMap.put("Name", originAgenciesNames);
        JsonNode search_query_json = JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_WITH_ONE_NAME_QUERY));
        JsonNode expected_search_query_json = VitamQueryHelper.createQueryDSL(
            searchCriteriaMap,
            Optional.empty(),
            Optional.empty()
        );

        assertThat(search_query_json).isEqualTo(expected_search_query_json);
    }

    @Test
    void search_query_with_list_of_originating_agency_names_as_criteria()
        throws FileNotFoundException, InvalidParseOperationException, InvalidCreateOperationException {
        Map<String, Object> searchCriteriaMap = new HashMap<>();
        List<String> originAgenciesNames = new ArrayList<>();
        originAgenciesNames.add("originating agency name 1");
        originAgenciesNames.add("originating agency name 2");
        originAgenciesNames.add("originating agency name 3");
        searchCriteriaMap.put("Name", originAgenciesNames);
        JsonNode search_query_json = JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_WITH_LIST_OF_NAME_QUERY));
        JsonNode expected_search_query_json = VitamQueryHelper.createQueryDSL(
            searchCriteriaMap,
            Optional.empty(),
            Optional.empty()
        );

        assertThat(search_query_json).isEqualTo(expected_search_query_json);
    }

    @Test
    void search_query_with_originating_agency_identifier_as_criteria()
        throws FileNotFoundException, InvalidParseOperationException, InvalidCreateOperationException {
        Map<String, Object> searchCriteriaMap = new HashMap<>();
        List<String> originAgenciesCodes = new ArrayList<>();
        originAgenciesCodes.add("originating agency identifier");
        searchCriteriaMap.put("Identifier", originAgenciesCodes);
        JsonNode search_query_json = JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_WITH_IDENTIFIER_QUERY));
        JsonNode expected_search_query_json = VitamQueryHelper.createQueryDSL(
            searchCriteriaMap,
            Optional.empty(),
            Optional.empty()
        );

        assertThat(search_query_json).isEqualTo(expected_search_query_json);
    }

    @Test
    void search_query_with_originating_agency_identifier_and_name_as_criterias()
        throws FileNotFoundException, InvalidParseOperationException, InvalidCreateOperationException {
        Map<String, Object> searchCriteriaMap = new HashMap<>();
        List<String> originAgenciesCodes = new ArrayList<>();
        List<String> originAgenciesNames = new ArrayList<>();
        originAgenciesCodes.add("originating agency identifier");
        originAgenciesNames.add("originating agency name");
        searchCriteriaMap.put("Identifier", originAgenciesCodes);
        searchCriteriaMap.put("Name", originAgenciesNames);
        JsonNode search_query_json = JsonHandler.getFromFile(
            PropertiesUtils.findFile(SEARCH_WITH_NAME_AND_IDENTIFIER_QUERY)
        );
        JsonNode expected_search_query_json = VitamQueryHelper.createQueryDSL(
            searchCriteriaMap,
            Optional.empty(),
            Optional.empty()
        );

        assertThat(search_query_json).isEqualTo(expected_search_query_json);
    }
}
