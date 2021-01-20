package fr.gouv.vitamui.commons.api.utils;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;

import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;

public class CriteriaUtilsTest {

    @Test
    public void testTransformCriteriaToJsonString() {
        final QueryDto criteria = new QueryDto();
        final Criterion criterion1 = new Criterion("creationDate", "2018-08-24T17:15:33.790+02:00", CriterionOperator.LOWERTHANOREQUALS);
        final Criterion criterion2 = new Criterion("creationDate", "2018-08-24T17:15:33.790+02:00", CriterionOperator.GREATERTHANOREQUALS);
        criteria.addCriterion(criterion1);
        criteria.addCriterion(criterion2);

        final String json = CriteriaUtils.toJson(criteria);

        final QueryDto criteriafromJson = CriteriaUtils.fromJson(json);
        assertThat(criteria).isEqualTo(criteriafromJson);
    }

    @Test
    public void test_checkFormat_with_invalid_subquery() {
        final QueryDto criteria = new QueryDto();
        final Criterion criterion1 = new Criterion("creationDate", "2018-08-24T17:15:33.790+02:00", CriterionOperator.LOWERTHANOREQUALS);
        final Criterion criterion2 = new Criterion("updatedDate", "2018-08-24T17:15:33.790+02:00", CriterionOperator.GREATERTHANOREQUALS);
        criteria.addCriterion(criterion1);
        criteria.addCriterion(criterion2);
        final QueryDto subQuery = new QueryDto();
        criteria.addQuery(subQuery);
        final Criterion criterion3 = new Criterion("lastname", "nole", null);
        subQuery.addCriterion(criterion3);

        try {
            CriteriaUtils.checkFormat(criteria);
            Assert.fail();
        } catch (final BadRequestException e) {
            assertThat(e.getMessage()).contains("lastname");
        }
    }

    @Test
    public void test_criterion_equals_null() {
        final QueryDto criteria = new QueryDto();
        final Criterion criterion1 = new Criterion("externalParamId", null, CriterionOperator.EQUALS);
        criteria.addCriterion(criterion1);

        try {
            CriteriaUtils.checkFormat(criteria);
            Assert.assertTrue(true);
        } catch (final BadRequestException e) {
            Assert.fail();
        }
    }

    @Test
    public void test_criterion_other_than_equals_null() {
        final QueryDto criteria = new QueryDto();
        final Criterion criterion1 = new Criterion("externalParamId", null, CriterionOperator.NOTEQUALS);
        criteria.addCriterion(criterion1);

        try {
            CriteriaUtils.checkFormat(criteria);
            Assert.fail();
        } catch (final BadRequestException e) {
            assertThat(e.getMessage()).contains("externalParamId");
        }
    }

}
