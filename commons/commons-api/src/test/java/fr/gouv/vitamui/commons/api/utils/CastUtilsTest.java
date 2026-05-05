package fr.gouv.vitamui.commons.api.utils;

import fr.gouv.vitamui.commons.api.exception.InvalidTypeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CastUtilsTest {

    @Test
    void testCastToInteger() {
        Integer a = CastUtils.toInteger(Integer.valueOf(5));
        assertThat(a).isEqualTo(5);

        a = CastUtils.toInteger(Float.valueOf(5));
        assertThat(a).isEqualTo(5);

        a = CastUtils.toInteger(Double.valueOf(5));
        assertThat(a).isEqualTo(5);

        a = CastUtils.toInteger(null);
        assertThat(a).isEqualTo(null);
    }

    @Test
    void testCastToIntegerFail() {
        assertThrows(InvalidTypeException.class, () -> {
            Integer a = CastUtils.toInteger("abcd");
        });
    }
}
