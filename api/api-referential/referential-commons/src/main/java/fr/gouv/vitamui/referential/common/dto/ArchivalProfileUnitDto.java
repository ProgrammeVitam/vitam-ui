package fr.gouv.vitamui.referential.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileStatus;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ArchivalProfileUnitDto extends IdDto implements Serializable {

    private Integer tenant;
    private Integer version;
    private String identifier;
    private String name;
    private String description;
    private ArchiveUnitProfileStatus status;
    private String creationDate;
    private String lastUpdate;
    private String activationDate;
    private String deactivationDate;
    private String controlSchema;
    private List<String> fields;
}
