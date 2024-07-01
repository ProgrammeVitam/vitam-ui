package fr.gouv.vitamui.iam.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.gouv.vitamui.commons.api.deserializer.ToLowerCaseConverter;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ProvidedUserDto {

    @NotNull
    @Length(min = 2, max = 50)
    private String lastname;

    @NotNull
    @Length(min = 2, max = 50)
    private String firstname;

    @NotNull
    @Length(min = 4, max = 100)
    @Email
    @JsonDeserialize(converter = ToLowerCaseConverter.class)
    private String email;

    @NotNull
    private String unit;

    private AddressDto address;

    private String siteCode;

    private String internalCode;
}
