package fr.gouv.vitamui.commons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReflectionUtilsTest {

    class MainDto {

        private String field;
        private SubDto neestedObject = new SubDto();
    }

    class SubDto {

        private String subField;
    }

    @Test
    public void testBuildPath() {
        Assertions.assertTrue(ReflectionUtils.hasField(MainDto.class, "field"));
        Assertions.assertFalse(ReflectionUtils.hasField(MainDto.class, "unknownField"));
        Assertions.assertFalse(ReflectionUtils.hasField(MainDto.class, "field.unknownSubField"));
        Assertions.assertFalse(ReflectionUtils.hasField(MainDto.class, "field.subField"));
        Assertions.assertTrue(ReflectionUtils.hasField(MainDto.class, "neestedObject.subField"));
    }
}
