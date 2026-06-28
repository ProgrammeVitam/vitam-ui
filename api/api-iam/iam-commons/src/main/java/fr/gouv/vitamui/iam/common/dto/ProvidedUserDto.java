package fr.gouv.vitamui.iam.common.dto;

import fr.gouv.vitamui.commons.api.deserializer.ToLowerCaseConverter;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProvidedUserDto {

    @NotNull
    @Size(min = 2, max = 50)
    private String lastname;

    @NotNull
    @Size(min = 2, max = 50)
    private String firstname;

    @NotNull
    @Size(min = 4, max = 100)
    @Email
    @JsonDeserialize(converter = ToLowerCaseConverter.class)
    private String email;

    @NotNull
    private String unit;

    private AddressDto address;

    private String siteCode;

    private String internalCode;
}
