package fr.gouv.vitamui.commons.api.domain;

import fr.gouv.vitamui.commons.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import java.io.IOException;

/**
 * VITAMUI DTO.
 */
public class QueryDtoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDto.class);

    @Test
    void testToString() {
        QueryDto criteria = new QueryDto();
        criteria.addCriterion(new Criterion("lastName", "nole", CriterionOperator.EQUALS));
    }

    @Test
    void testSerialization() throws JacksonException {
        QueryDto criteria = new QueryDto();
        String exceptedQueryString =
            "{\"queryOperator\":\"AND\",\"criteria\":[{\"key\":\"lastname\",\"value\":\"nole\",\"operator\":\"EQUALSIGNORECASE\"},{\"queryOperator\":\"AND\",\"criteria\":[{\"key\":\"firstname\",\"value\":\"Pierre\",\"operator\":\"EQUALS\"}]}]}";
        criteria.addCriterion(new Criterion("lastname", "nole", CriterionOperator.EQUALSIGNORECASE));
        QueryDto subQuery = new QueryDto();
        subQuery.addCriterion(new Criterion("firstname", "Pierre", CriterionOperator.EQUALS));
        criteria.addQuery(subQuery);
        LOGGER.debug(JsonUtils.toJson(criteria));
        String queryAsString = JsonUtils.toJson(criteria);
        try {
            Assertions.assertThat(JsonUtils.readTree(queryAsString)).isEqualTo(JsonUtils.readTree(exceptedQueryString));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDeserialization() throws StreamReadException, DatabindException, IOException {
        String queryAsJson =
            "{\"queryOperator\":\"OR\",\"criteria\":[{\"key\":\"lastName\",\"value\":\"nole\",\"operator\":\"EQUALS\"}]}";
        QueryDto query = JsonUtils.fromJson(queryAsJson, QueryDto.class);
        Assertions.assertThat(query.getQueryOperator()).isEqualTo(QueryOperator.OR);
        Assertions.assertThat(query.getCriterionList()).hasSize(1);
        Criterion criterion = query.getCriterionList().get(0);
        Assertions.assertThat(criterion.getKey()).isEqualTo("lastName");
        Assertions.assertThat(criterion.getValue()).isEqualTo("nole");
        Assertions.assertThat(criterion.getOperator()).isEqualTo(CriterionOperator.EQUALS);
    }

    @Test
    void testDeserializationWithSubquery() throws StreamReadException, DatabindException, IOException {
        String queryAsJson =
            "{\"queryOperator\":\"OR\",\"criteria\":[{\"key\":\"lastName\",\"value\":\"nole\",\"operator\":\"EQUALS\"},{\"queryOperator\":\"NOR\",\"criteria\":[{\"key\":\"firstname\",\"value\":\"Pierre\",\"operator\":\"EQUALS\"}]}]}";
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
