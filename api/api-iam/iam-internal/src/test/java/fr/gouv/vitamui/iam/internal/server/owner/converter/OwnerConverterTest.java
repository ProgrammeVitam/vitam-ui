package fr.gouv.vitamui.iam.internal.server.owner.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;

public class OwnerConverterTest {

    private final OwnerConverter ownerConverter = new OwnerConverter(new AddressConverter());

    @Test
    public void testConvertEntityToDto() {
        Owner owner = new Owner();
        owner.setAddress(new Address());
        owner.setCode("12345687");
        owner.setCompanyName("companyName");
        owner.setCustomerId("customerId");
        owner.setId("id");
        owner.setIdentifier("identifier");
        owner.setName("name");
        OwnerDto res = ownerConverter.convertEntityToDto(owner);
        assertThat(owner).isEqualToIgnoringGivenFields(res, "address");

        owner.setAddress(null);
        res = ownerConverter.convertEntityToDto(owner);
        assertThat(res.getAddress()).isNull();
    }

    @Test
    public void testConvertDtoToEntity() {
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setAddress(new AddressDto());
        ownerDto.setCode("code");
        ownerDto.setCompanyName("companyName");
        ownerDto.setCustomerId("customerId");
        ownerDto.setId("id");
        ownerDto.setIdentifier("identifier");
        ownerDto.setName("name");

        Owner res = ownerConverter.convertDtoToEntity(ownerDto);
        assertThat(res).isEqualToIgnoringGivenFields(ownerDto, "address");
    }

    @Test
    public void testConvertDtoToLogbook() throws InvalidParseOperationException {
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setAddress(new AddressDto());
        ownerDto.setCode("code");
        ownerDto.setCompanyName("companyName");
        ownerDto.setCustomerId("customerId");
        ownerDto.setId("id");
        ownerDto.setIdentifier("identifier");
        ownerDto.setName("name");

        String json = ownerConverter.convertToLogbook(ownerDto);
        assertThat(json).isNotBlank();
        JsonNode jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(OwnerConverter.CODE_KEY)).isNotNull();
        assertThat(jsonNode.get(OwnerConverter.NAME_KEY)).isNotNull();
        assertThat(jsonNode.get(OwnerConverter.COMPANY_NAME_KEY)).isNotNull();
    }

}
