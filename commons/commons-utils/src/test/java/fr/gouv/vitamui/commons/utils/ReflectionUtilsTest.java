package fr.gouv.vitamui.commons.utils;

import org.junit.Assert;
import org.junit.Test;

public class ReflectionUtilsTest {

    class MainDto {
        private String field;
        private SubDto neestedObject = new SubDto();
    }
    class SubDto{
        private String subField;
    }

    @Test
    public void testBuildPath() {
        Assert.assertTrue(ReflectionUtils.hasField(MainDto.class, "field"));
        Assert.assertFalse(ReflectionUtils.hasField(MainDto.class, "unknownField"));
        Assert.assertFalse(ReflectionUtils.hasField(MainDto.class, "field.unknownSubField"));
        Assert.assertFalse(ReflectionUtils.hasField(MainDto.class, "field.subField"));
        Assert.assertTrue(ReflectionUtils.hasField(MainDto.class, "neestedObject.subField"));

    }

}
