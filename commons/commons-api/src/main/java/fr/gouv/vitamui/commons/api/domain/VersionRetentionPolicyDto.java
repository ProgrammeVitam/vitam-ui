/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.commons.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitamui.commons.api.enums.IntermediaryVersionEnum;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@NoArgsConstructor
public class VersionRetentionPolicyDto implements Serializable {

    private boolean initialVersion;
    private IntermediaryVersionEnum intermediaryVersion;
    Set<VersionUsageDto> usages;

    public VersionRetentionPolicyDto (VersionRetentionPolicyDto versionRetentionPolicyDto){
        if (versionRetentionPolicyDto != null) {
            this.initialVersion = versionRetentionPolicyDto.initialVersion;
            this.intermediaryVersion = versionRetentionPolicyDto.intermediaryVersion;
            this.usages = versionRetentionPolicyDto.usages;
        } else {
            this.usages = new HashSet<>();
        }
    }

    @JsonCreator
    public VersionRetentionPolicyDto(@JsonProperty("initialVersion") boolean initialVersion,
                                     @JsonProperty("intermediaryVersion") IntermediaryVersionEnum intermediaryVersion,
                                     @JsonProperty("usages") Set<VersionUsageDto> usages) {
        this.initialVersion = initialVersion;
        this.intermediaryVersion = intermediaryVersion;
        this.usages = usages;
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
    public IntermediaryVersionEnum getIntermediaryVersion() {
        return intermediaryVersion;
    }

    @JsonProperty("IntermediaryVersion")
    public void setIntermediaryVersion(IntermediaryVersionEnum intermediaryVersion) {
        this.intermediaryVersion = intermediaryVersion;
    }
}
