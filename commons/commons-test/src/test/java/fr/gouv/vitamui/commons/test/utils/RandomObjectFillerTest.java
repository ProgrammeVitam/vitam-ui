package fr.gouv.vitamui.commons.test.utils;

import fr.gouv.vitamui.commons.api.domain.SingleValueDto;
import org.junit.Test;

import static org.junit.Assert.fail;

public class RandomObjectFillerTest {

	@Test
	public void testRandomObjectFiller() {
		try {
			SingleValueDto classTest = RandomObjectFiller.createAndFill(SingleValueDto.class);
			//Assert.assertTrue();
		} catch (Exception e) {
			fail();
		}
	}

}
