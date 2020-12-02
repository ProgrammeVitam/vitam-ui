package fr.gouv.vitamui.commons.api.domain;

import java.io.Serializable;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Request Param Dto.
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class RequestParamDto implements Serializable {

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

    private Optional<String> criteria;

    private Optional<String> orderBy;

    private Optional<DirectionDto> direction;

    private Optional<RequestParamGroupDto> groups;

}
