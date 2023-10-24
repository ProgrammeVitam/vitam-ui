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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ManagementContractModelDto implements Serializable {

    private StorageDetailDto storage;

    private VersionRetentionPolicyDto versionRetentionPolicy;

    private List<PersistentIdentifierPolicyDto> persistentIdentifierPolicyList;

    private String id;

    private Integer tenant;

    private Integer version;

    private String name;

    private String identifier;

    private String description;

    private String status;

    private String creationDate;

    private String lastUpdate;

    private String deactivationDate;

    private String activationDate;



    @JsonProperty("versionRetentionPolicy")
    public void setVersionRetentionPolicy(VersionRetentionPolicyDto versionRetentionPolicy) {
        this.versionRetentionPolicy = versionRetentionPolicy;
    }

    @JsonProperty("#id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("#tenant")
    public void setTenant(Integer tenant) {
        this.tenant = tenant;
    }

    @JsonProperty("#version")
    public void setVersion(Integer version) {
        this.version = version;
    }

    @JsonProperty("Name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("Identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonProperty("Description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("Status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("CreationDate")
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    @JsonProperty("LastUpdate")
    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @JsonProperty("ActivationDate")
    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }

    @JsonProperty("DeactivationDate")
    public void setDeactivationDate(String deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    @JsonProperty("Storage")
    public void setStorage(StorageDetailDto storage) {
        this.storage = storage;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("tenant")
    public Integer getTenant() {
        return tenant;
    }

    @JsonProperty("version")
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

    @JsonProperty("Status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("CreationDate")
    public String getCreationDate() {
        return creationDate;
    }

    @JsonProperty("lastUpdate")
    public String getLastUpdate() {
        return lastUpdate;
    }

    @JsonProperty("ActivationDate")
    public String getActivationDate() {
        return activationDate;
    }

    @JsonProperty("Storage")
    public StorageDetailDto getStorage() {
        return storage;
    }

    @JsonProperty("VersionRetentionPolicy")
    public VersionRetentionPolicyDto getVersionRetentionPolicy() {
        return versionRetentionPolicy;
    }

    @JsonProperty("DeactivationDate")
    public String getDeactivationDate() {
        return deactivationDate;
    }


    @JsonProperty("PersistentIdentifierPolicy")
    public void setPersistentIdentifierPolicyList(
        List<PersistentIdentifierPolicyDto> persistentIdentifierPolicyList) {
        this.persistentIdentifierPolicyList = persistentIdentifierPolicyList;
    }

    @JsonProperty("PersistentIdentifierPolicy")
    public List<PersistentIdentifierPolicyDto> getPersistentIdentifierPolicyList() {
        return persistentIdentifierPolicyList;
    }
}
