package fr.gouv.vitamui.iam.internal.server.customer.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;

public class CustomerConverterTest {

    @Mock
    private OwnerConverter ownerConverter;

    @Mock
    private OwnerRepository ownerRepository;

    private final AddressConverter addressConverter = new AddressConverter();

    private CustomerConverter customerConverter;

    @Before
    public void setup() {
        customerConverter = new CustomerConverter(addressConverter, ownerRepository, ownerConverter);
    }

    @Test
    public void convertToLogbookTest() {
        CustomerDto customer = new CustomerDto();
        customer.setEmailDomains(new ArrayList<>());
        String json = customerConverter.convertToLogbook(customer);
        assertThat(json).isNotBlank();

    }

}
