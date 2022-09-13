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

package fr.gouv.vitamui.archive.internal.server.rulesupdate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class RulesUpdateCommonServiceTest {

    public final String SEARCH_UA_QUERY = "data/rulesUpdateQueries/search_UA_query.json";
    public final String EXPECTED_SEARCH_UA_QUERY_WITHOUT_FILTER_AND_PROJECTION = "data/rulesUpdateQueries/expected_search_UA_query_without_filter_and_projection.json";
    public final String EXPECTED_SEARCH_UA_QUERY_WITHOUT_FILTER =
        "data/rulesUpdateQueries/expected_search_UA_query_without_filter.json";
    private final RulesUpdateCommonService rulesUpdateCommonService = new RulesUpdateCommonService();

    @Test
    void search_query_without_filter_field() throws FileNotFoundException, InvalidParseOperationException {
        JsonNode search_query_json = JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_UA_QUERY));
        JsonNode expected_search_query_json = JsonHandler.getFromFile(PropertiesUtils
            .findFile(EXPECTED_SEARCH_UA_QUERY_WITHOUT_FILTER));
        rulesUpdateCommonService.deleteAttributesFromObjectNode((ObjectNode) search_query_json, "$filter");
        assertThat(search_query_json).isEqualTo(expected_search_query_json);
    }

    @Test
    void search_query_without_filter_and_projection_fields() throws FileNotFoundException, InvalidParseOperationException {
        JsonNode search_query_json = JsonHandler.getFromFile(PropertiesUtils.findFile(SEARCH_UA_QUERY));
        JsonNode expected_search_query_json = JsonHandler.getFromFile(PropertiesUtils
            .findFile(EXPECTED_SEARCH_UA_QUERY_WITHOUT_FILTER_AND_PROJECTION));
        rulesUpdateCommonService.deleteAttributesFromObjectNode((ObjectNode) search_query_json, "$filter", "$projection");
        assertThat(search_query_json).isEqualTo(expected_search_query_json);
    }
}
