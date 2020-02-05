package fr.gouv.vitamui.iam.internal.server.common.converter;

import org.junit.Test;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;

public class AddressConverterTest {

    private final AddressConverter converter = new AddressConverter();

    @Test
    public void testConvertToEntity() {
        AddressDto dto = new AddressDto();
        dto.setCity("city");
        dto.setCountry("country");
        dto.setStreet("street");
        dto.setZipCode("zipcode");

        Address entity = converter.convertDtoToEntity(dto);

    }
}
