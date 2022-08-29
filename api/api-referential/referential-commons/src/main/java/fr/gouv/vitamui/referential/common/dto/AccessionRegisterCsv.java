package fr.gouv.vitamui.referential.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitam.common.model.administration.AccessionRegisterDetailModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccessionRegisterCsv extends AccessionRegisterDetailModel implements Serializable {

    @JsonProperty("agency_name")
    private String originatingAgencyName;
}
