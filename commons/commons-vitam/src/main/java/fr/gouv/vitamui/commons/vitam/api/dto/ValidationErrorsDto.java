package fr.gouv.vitamui.commons.vitam.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ValidationErrorsDto {

    @JsonProperty("evId")
    private String evId;

    @JsonProperty("evTypeProc")
    private String evTypeProc;

    @JsonProperty("outDetail")
    private String outDetail;

    @JsonProperty("outMessg")
    private String outMessg;

    @JsonProperty("evDetData")
    private String evDetData;

    @JsonProperty("obId")
    private String obId;
}
