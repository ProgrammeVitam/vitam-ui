package fr.gouv.vitamui.commons.api.domain;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * VITAMUI DTO.
 *
 *
 */
public class QueryDtoTest {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(QueryDto.class);

    @Before
    public void setup() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void testToString() {
        QueryDto criteria = new QueryDto();
        criteria.addCriterion(new Criterion("lastName", "nole", CriterionOperator.EQUALS));
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        QueryDto criteria = new QueryDto();
        String exceptedQueryString = "{\"queryOperator\":\"AND\",\"criteria\":[{\"key\":\"lastname\",\"value\":\"nole\",\"operator\":\"EQUALSIGNORECASE\"},{\"queryOperator\":\"AND\",\"criteria\":[{\"key\":\"firstname\",\"value\":\"Pierre\",\"operator\":\"EQUALS\"}]}]}";
        criteria.addCriterion(new Criterion("lastname", "nole", CriterionOperator.EQUALSIGNORECASE));
        QueryDto subQuery = new QueryDto();
        subQuery.addCriterion(new Criterion("firstname", "Pierre", CriterionOperator.EQUALS));
        criteria.addQuery(subQuery);
        LOGGER.debug(JsonUtils.toJson(criteria));
        String queryAsString = JsonUtils.toJson(criteria);
        Assertions.assertThat(queryAsString).isEqualTo(exceptedQueryString);
    }

    @Test
    public void testDeserialization() throws JsonParseException, JsonMappingException, IOException {
        String queryAsJson = "{\"queryOperator\":\"OR\",\"criteria\":[{\"key\":\"lastName\",\"value\":\"nole\",\"operator\":\"EQUALS\"}]}";
        QueryDto query = JsonUtils.fromJson(queryAsJson, QueryDto.class);
        Assertions.assertThat(query.getQueryOperator()).isEqualTo(QueryOperator.OR);
        Assertions.assertThat(query.getCriterionList()).hasSize(1);
        Criterion criterion = query.getCriterionList().get(0);
        Assertions.assertThat(criterion.getKey()).isEqualTo("lastName");
        Assertions.assertThat(criterion.getValue()).isEqualTo("nole");
        Assertions.assertThat(criterion.getOperator()).isEqualTo(CriterionOperator.EQUALS);
    }

    @Test
    public void testDeserializationWithSubquery() throws JsonParseException, JsonMappingException, IOException {
        String queryAsJson = "{\"queryOperator\":\"OR\",\"criteria\":[{\"key\":\"lastName\",\"value\":\"nole\",\"operator\":\"EQUALS\"},{\"queryOperator\":\"NOR\",\"criteria\":[{\"key\":\"firstname\",\"value\":\"Pierre\",\"operator\":\"EQUALS\"}]}]}";
        QueryDto query = JsonUtils.fromJson(queryAsJson, QueryDto.class);
        Assertions.assertThat(query.getQueryOperator()).isEqualTo(QueryOperator.OR);
        Assertions.assertThat(query.getCriterionList()).hasSize(1);
        Assertions.assertThat(query.getSubQueries()).hasSize(1);
        QueryDto subQuery = query.getSubQueries().get(0);
        Assertions.assertThat(subQuery.getCriterionList()).hasSize(1);
        Criterion criterionFromSubquery = subQuery.getCriterionList().get(0);
        Assertions.assertThat(criterionFromSubquery.getKey()).isEqualTo("firstname");
        Assertions.assertThat(criterionFromSubquery.getValue()).isEqualTo("Pierre");
        Assertions.assertThat(criterionFromSubquery.getOperator()).isEqualTo(CriterionOperator.EQUALS);

    }

}
