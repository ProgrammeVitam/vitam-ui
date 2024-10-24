package fr.gouv.vitamui.commons.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class VitamUIStringUtilsTest {

    @Test
    public void TestIsBoolean() throws IOException {
        Assert.assertTrue(VitamUIStringUtils.isBoolean("y"));
        Assert.assertTrue(VitamUIStringUtils.isBoolean("true"));
        Assert.assertTrue(VitamUIStringUtils.isBoolean("false"));
        Assert.assertFalse(VitamUIStringUtils.isBoolean(null));
        Assert.assertFalse(VitamUIStringUtils.isBoolean("test"));
    }
}
