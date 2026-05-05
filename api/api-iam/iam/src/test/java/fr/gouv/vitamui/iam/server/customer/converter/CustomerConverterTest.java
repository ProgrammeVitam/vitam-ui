package fr.gouv.vitamui.iam.server.customer.converter;

import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.server.owner.dao.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerConverterTest {

    @Mock
    private OwnerConverter ownerConverter;

    @Mock
    private OwnerRepository ownerRepository;

    private final AddressConverter addressConverter = new AddressConverter();

    private CustomerConverter customerConverter;

    @BeforeEach
    public void setup() {
        customerConverter = new CustomerConverter(addressConverter, ownerRepository, ownerConverter);
    }

    @Test
    void convertToLogbookTest() {
        CustomerDto customer = new CustomerDto();
        customer.setEmailDomains(new ArrayList<>());
        String json = customerConverter.convertToLogbook(customer);
        assertThat(json).isNotBlank();
    }
}
