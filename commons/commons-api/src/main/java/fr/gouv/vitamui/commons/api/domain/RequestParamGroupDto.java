package fr.gouv.vitamui.commons.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class RequestParamGroupDto implements Serializable {

    /**
     * aggregation fields list
     */
    @NotNull
    @NonNull
    private List<String> fields;

    /**
     * aggregation operator
     */
    @NotNull
    @NonNull
    private AggregationRequestOperator operator;

    /**
     * field on which we want the operator
     */
    private String fieldOperator;
}
