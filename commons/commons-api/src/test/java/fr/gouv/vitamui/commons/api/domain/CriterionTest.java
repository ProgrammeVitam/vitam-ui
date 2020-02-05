package fr.gouv.vitamui.commons.api.domain;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * Class.
 *
 *
 */
public class CriterionTest {

    private Criterion criteria;

    @Before
    public void setup() {
        criteria = new Criterion();
    }

    @Test
    public void testToString() {
        criteria = new Criterion("latsName", "nole", CriterionOperator.EQUALS);
    }
}
