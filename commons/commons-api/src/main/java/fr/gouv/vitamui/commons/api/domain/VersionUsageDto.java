package fr.gouv.vitamui.commons.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.io.Serializable;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersionUsageDto implements Serializable {

    private String usageName;
    private boolean initialVersion;
    private IntermediaryVersionEnum intermediaryVersion;

    @JsonProperty("initialVersion")
    public boolean getInitialVersion() {
        return initialVersion;
    }

    @JsonProperty("InitialVersion")
    public void setInitialVersion(boolean initialVersion) {
        this.initialVersion = initialVersion;
    }

    @JsonProperty("intermediaryVersion")
    public IntermediaryVersionEnum getIntermediaryVersion() {
        return intermediaryVersion;
    }

    @JsonProperty("IntermediaryVersion")
    public void setIntermediaryVersion(IntermediaryVersionEnum intermediaryVersion) {
        this.intermediaryVersion = intermediaryVersion;
    }

    @JsonProperty("usageName")
    public String getUsageName() {
        return usageName;
    }

    @JsonProperty("UsageName")
    public void setUsageName(String usageName) {
        this.usageName = usageName;
    }

    public enum IntermediaryVersionEnum {
        ALL,
        LAST,
        NONE
    }
}
