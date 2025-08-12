package fr.gouv.vitamui.commons.api.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
public class Facet implements Serializable {

    protected String name;
    protected FacetType facetType;

    public Facet(String name, FacetType facetType) {
        this.name = name;
        this.facetType = facetType;
    }
}
