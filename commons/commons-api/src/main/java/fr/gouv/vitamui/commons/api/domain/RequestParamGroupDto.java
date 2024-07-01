package fr.gouv.vitamui.commons.api.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Request Param Group Dto.
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class RequestParamGroupDto implements Serializable {

    @NotNull
    private List<String> fields;

    @NotNull
    private AggregationRequestOperator operator;
}
