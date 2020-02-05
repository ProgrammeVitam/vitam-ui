/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.api.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
public class IngestContractDto {

    private String id;

    private Integer tenant;

    private Integer version;

    private String name;

    private String identifier;

    private String description;

    private String status;

    private String creationDate;

    private String lastUpdate;

    private String activationDate;

    private Boolean writingPermission;

    private Boolean everyOriginatingAgency;

    private Boolean everyDataObjectVersion;

    private List<String> archiveProfiles = new ArrayList<>();

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

    @JsonProperty("WritingPermission")
    public void setWritingPermission(Boolean writingPermission) {
        this.writingPermission = writingPermission;
    }

    @JsonProperty("EveryOriginatingAgency")
    public void setEveryOriginatingAgency(Boolean everyOriginatingAgency) {
        this.everyOriginatingAgency = everyOriginatingAgency;
    }

    @JsonProperty("EveryDataObjectVersion")
    public void setEveryDataObjectVersion(Boolean everyDataObjectVersion) {
        this.everyDataObjectVersion = everyDataObjectVersion;
    }

    @JsonProperty("ArchiveProfiles")
    public void setArchiveProfiles(List<String> archiveProfiles) {
        this.archiveProfiles = archiveProfiles;
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

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("creationDate")
    public String getCreationDate() {
        return creationDate;
    }

    @JsonProperty("lastUpdate")
    public String getLastUpdate() {
        return lastUpdate;
    }

    @JsonProperty("activationDate")
    public String getActivationDate() {
        return activationDate;
    }

    @JsonProperty("writingPermission")
    public Boolean getWritingPermission() {
        return writingPermission;
    }

    @JsonProperty("everyOriginatingAgency")
    public Boolean getEveryOriginatingAgency() {
        return everyOriginatingAgency;
    }

    @JsonProperty("everyDataObjectVersion")
    public Boolean getEveryDataObjectVersion() {
        return everyDataObjectVersion;
    }

    @JsonProperty("ArchiveProfiles")
    public List<String> getArchiveProfiles() {
        return archiveProfiles;
    }

}
