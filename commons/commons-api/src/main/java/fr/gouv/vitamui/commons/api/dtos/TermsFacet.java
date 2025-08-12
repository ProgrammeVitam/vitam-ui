package fr.gouv.vitamui.commons.api.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
public class TermsFacet extends Facet {

    private String field;
    private String nestedPath;
    private Integer size;
    private FacetOrder order;

    public TermsFacet(String facetName, String field, Integer size, FacetOrder order) {
        super(facetName, FacetType.TERMS);
        this.field = field;
        this.size = size;
        this.order = order == null ? FacetOrder.ASC : order;
        this.facetType = FacetType.TERMS;
    }
}
