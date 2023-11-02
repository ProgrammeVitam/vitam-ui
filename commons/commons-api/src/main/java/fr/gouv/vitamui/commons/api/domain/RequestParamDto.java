package fr.gouv.vitamui.commons.api.domain;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;

/**
 * Request Param Dto.
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(callSuper = true)
public class RequestParamDto implements Serializable {

    /**
     * page number
     */
    @NotNull
    private Integer page;

    /**
     * result set size
     */
    @NotNull
    private Integer size;

    /**
     * filter criteria
     */
    private String criteria;

    /**
     * order by fields separated by ','
     */
    private String orderBy;

    /**
     * orderby direction
     */
    private DirectionDto direction;

    /**
     * aggregation groups
     */
    private RequestParamGroupDto groups;

    /**
     * fields to be excluded from each dto result, separated by ','
     */
    private List<String> excludeFields;

    /**
     * embed external dependency in the response
     */
    String embedded;

}
