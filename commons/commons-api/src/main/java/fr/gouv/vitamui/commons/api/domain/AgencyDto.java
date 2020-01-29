package fr.gouv.vitamui.commons.api.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgencyDto implements Serializable {

    private Integer tenant;

    private Integer version;

    private String name;

    private String identifier;

    private String description;

    @JsonProperty("tenant")
    public void setTenant(Integer tenant) {
        this.tenant = tenant;
    }

    @JsonProperty("version")
    public void setVersion(Integer version) {
        this.version = version;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("#tenant")
    public Integer getTenant() {
        return tenant;
    }

    @JsonProperty("#version")
    public Integer getVersion() {
        return version;
    }

    @JsonProperty("Name")
    public String getName() {
        return name;
    }

    @JsonProperty("Identifier")
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty("Description")
    public String getDescription() {
        return description;
    }
}
