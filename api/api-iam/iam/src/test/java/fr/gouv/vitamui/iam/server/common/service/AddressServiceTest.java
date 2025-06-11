package fr.gouv.vitamui.iam.server.common.service;

import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.iam.server.common.domain.Address;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressServiceTest {

    private AddressService service;

    @BeforeEach
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
