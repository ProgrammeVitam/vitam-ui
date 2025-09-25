package fr.gouv.vitamui.commons.vitam.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ObjectGroupInfoDto {

    @JsonProperty("#errors")
    @JsonAlias({ "_errors" })
    private List<ValidationErrorsDto> errors;
}
