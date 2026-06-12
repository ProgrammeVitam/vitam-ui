package fr.gouv.vitamui.commons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class VitamUIStringUtilsTest {

    @Test
    void TestIsBoolean() throws IOException {
        Assertions.assertTrue(VitamUIStringUtils.isBoolean("y"));
        Assertions.assertTrue(VitamUIStringUtils.isBoolean("true"));
        Assertions.assertTrue(VitamUIStringUtils.isBoolean("false"));
        Assertions.assertFalse(VitamUIStringUtils.isBoolean(null));
        Assertions.assertFalse(VitamUIStringUtils.isBoolean("test"));
    }
}
