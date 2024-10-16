package fr.gouv.vitamui.iam.internal.server.common.service;

import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressServiceTest {

    private AddressService service;

    @Before
    public void setup() {
        service = new AddressService();
    }

    @Test
    public void processPatch() {
        final Address entity = new Address();
        final Address other = IamServerUtilsTest.buildAddress();
        final Map<String, Object> partialDto = TestUtils.getMapFromObject(other);

        service.processPatch(entity, partialDto, new ArrayList<>(), false);
        assertThat(entity).isEqualToComparingFieldByField(other);
    }
}
