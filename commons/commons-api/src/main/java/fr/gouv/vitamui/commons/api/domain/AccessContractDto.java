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

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessContractDto implements Serializable {

    private Integer tenant;

    private Integer version;

    private String name;

    private String identifier;

    private String description;

    private String status;

    private String creationDate;

    private String lastUpdate;

    private String activationDate;
    
    private String deactivationDate;

    private Boolean writingPermission;

    private Boolean everyOriginatingAgency;

    private Boolean everyDataObjectVersion;

    private String accessLog;

    private Set<String> ruleCategoryToFilter;

    private Set<String> originatingAgencies;

    private Set<String> rootUnits;

    private Set<String> excludedRootUnits;

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

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("creationDate")
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    @JsonProperty("lastUpdate")
    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @JsonProperty("activationDate")
    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }
    
    @JsonProperty("deactivationDate")
    public void setDeactivationDate(String deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    @JsonProperty("writingPermission")
    public void setWritingPermission(Boolean writingPermission) {
        this.writingPermission = writingPermission;
    }

    @JsonProperty("everyOriginatingAgency")
    public void setEveryOriginatingAgency(Boolean everyOriginatingAgency) {
        this.everyOriginatingAgency = everyOriginatingAgency;
    }

    @JsonProperty("everyDataObjectVersion")
    public void setEveryDataObjectVersion(Boolean everyDataObjectVersion) {
        this.everyDataObjectVersion = everyDataObjectVersion;
    }

    @JsonProperty("accessLog ")
    public void setAccessLog(String accessLog) {
        this.accessLog = accessLog;
    }

    @JsonProperty("ruleCategoryToFilter")
    public void setRuleCategoryToFilter(Set<String> ruleCategoryToFilter) {
        this.ruleCategoryToFilter = ruleCategoryToFilter;
    }

    @JsonProperty("originatingAgencies")
    public void setOriginatingAgencies(Set<String> originatingAgencies) {
        this.originatingAgencies = originatingAgencies;
    }

    @JsonProperty("rootUnits")
    public void setRootUnits(Set<String> rootUnits) {
        this.rootUnits = rootUnits;
    }

    @JsonProperty("excludedRootUnits")
    public void setExcludedRootUnits(Set<String> excludedRootUnits) {
        this.excludedRootUnits = excludedRootUnits;
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

    @JsonProperty("Status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("CreationDate")
    public String getCreationDate() {
        return creationDate;
    }

    @JsonProperty("LastUpdate")
    public String getLastUpdate() {
        return lastUpdate;
    }

    @JsonProperty("ActivationDate")
    public String getActivationDate() {
        return activationDate;
    }
    
    @JsonProperty("DeactivationDate")
    public String getDeactivationDate() {
        return deactivationDate;
    }

    @JsonProperty("WritingPermission")
    public Boolean getWritingPermission() {
        return writingPermission;
    }

    @JsonProperty("EveryOriginatingAgency")
    public Boolean getEveryOriginatingAgency() {
        return everyOriginatingAgency;
    }

    @JsonProperty("EveryDataObjectVersion")
    public Boolean getEveryDataObjectVersion() {
        return everyDataObjectVersion;
    }

    @JsonProperty("AccessLog")
    public String getAccessLog() {
        return accessLog;
    }

    @JsonProperty("RuleCategoryToFilter")
    public Set<String> getRuleCategoryToFilter() {
        return ruleCategoryToFilter;
    }

    @JsonProperty("OriginatingAgencies")
    public Set<String> getOriginatingAgencies() {
        return originatingAgencies;
    }

    @JsonProperty("RootUnits")
    public Set<String> getRootUnits() {
        return rootUnits;
    }

    @JsonProperty("ExcludedRootUnits")
    public Set<String> getExcludedRootUnits() {
        return excludedRootUnits;
    }

}
