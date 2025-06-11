package fr.gouv.vitamui.commons.api.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * Class.
 *
 *
 */
public class CriterionTest {

    private Criterion criteria;

    @BeforeEach
    public void setup() {
        criteria = new Criterion();
    }

    @Test
    public void testToString() {
        criteria = new Criterion("latsName", "nole", CriterionOperator.EQUALS);
    }
}
