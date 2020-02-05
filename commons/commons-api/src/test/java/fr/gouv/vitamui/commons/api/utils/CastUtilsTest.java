package fr.gouv.vitamui.commons.api.utils;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import fr.gouv.vitamui.commons.api.exception.InvalidTypeException;

public class CastUtilsTest {

    @Test
    public void testCastToInteger() {
        Integer a = CastUtils.toInteger(new Integer(5));
        assertThat(a).isEqualTo(5);

        a = CastUtils.toInteger(new Float(5));
        assertThat(a).isEqualTo(5);

        a = CastUtils.toInteger(new Double(5));
        assertThat(a).isEqualTo(5);


        a = CastUtils.toInteger(null);
        assertThat(a).isEqualTo(null);
    }

    @Test(expected = InvalidTypeException.class)
    public void testCastToIntegerFail() throws IOException {
        Integer a = CastUtils.toInteger("abcd");
    }
}
