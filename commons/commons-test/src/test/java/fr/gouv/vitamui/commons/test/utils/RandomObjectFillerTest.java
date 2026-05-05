package fr.gouv.vitamui.commons.test.utils;

import fr.gouv.vitamui.commons.api.domain.SingleValueDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class RandomObjectFillerTest {

    @Test
    void testRandomObjectFiller() {
        try {
            SingleValueDto classTest = RandomObjectFiller.createAndFill(SingleValueDto.class);
            //Assert.assertTrue();
        } catch (Exception e) {
            fail();
        }
    }
}
