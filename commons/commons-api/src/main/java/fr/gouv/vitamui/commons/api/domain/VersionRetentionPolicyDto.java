package fr.gouv.vitamui.commons.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class VersionRetentionPolicyDto implements Serializable {

    private boolean initialVersion;
    private VersionUsageDto.IntermediaryVersionEnum intermediaryVersion;
    Set<VersionUsageDto> usages;

    public VersionRetentionPolicyDto (VersionRetentionPolicyDto versionRetentionPolicyDto){
        if (versionRetentionPolicyDto != null) {
            this.initialVersion = versionRetentionPolicyDto.initialVersion;
            this.intermediaryVersion = versionRetentionPolicyDto.intermediaryVersion;
            this.usages = versionRetentionPolicyDto.usages;
        }
    }

    @JsonProperty("usages")
    public Set<VersionUsageDto> getUsages() {
        return usages;
    }

    @JsonProperty("Usages")
    public void setUsages(Set<VersionUsageDto> usages) {
        this.usages = usages;
    }

    @JsonProperty("initialVersion")
    public boolean getInitialVersion() {
        return initialVersion;
    }

    @JsonProperty("InitialVersion")
    public void setInitialVersion(boolean initialVersion) {
        this.initialVersion = initialVersion;
    }

    @JsonProperty("intermediaryVersion")
    public VersionUsageDto.IntermediaryVersionEnum getIntermediaryVersion() {
        return intermediaryVersion;
    }

    @JsonProperty("IntermediaryVersion")
    public void setIntermediaryVersion(VersionUsageDto.IntermediaryVersionEnum intermediaryVersion) {
        this.intermediaryVersion = intermediaryVersion;
    }
}
