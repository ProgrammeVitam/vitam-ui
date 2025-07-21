package fr.gouv.vitamui.referential.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Getter
@Setter
@Accessors(chain = true)
public class ExternalAgencyDto {

    private String identifier;
    private String name;
    private String description;
}
