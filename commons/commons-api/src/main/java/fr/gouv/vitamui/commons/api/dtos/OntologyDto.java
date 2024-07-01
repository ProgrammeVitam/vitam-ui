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
 *
 *
 */

package fr.gouv.vitamui.commons.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyDto {

    private String identifier;
    private String apiField;
    private String description;
    private OntologyType type;
    private String origin;
    private String creationDate;
    private String lastUpdate;
    private String shortName;

    private List<Integer> tenantIds;

    @JsonProperty("Identifier")
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty("TenantIds")
    public List<Integer> getTenantIds() {
        return tenantIds;
    }

    @JsonProperty("ApiField")
    public String getApiField() {
        return apiField;
    }

    @JsonProperty("Description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("Type")
    public OntologyType getType() {
        return type;
    }

    @JsonProperty("Origin")
    public String getOrigin() {
        return origin;
    }

    @JsonProperty("CreationDate")
    public String getCreationDate() {
        return creationDate;
    }

    @JsonProperty("LastUpdate")
    public String getLastUpdate() {
        return lastUpdate;
    }

    @JsonProperty("ShortName")
    public String getShortName() {
        return shortName;
    }

    public void setApiField(String apiField) {
        this.apiField = apiField;
    }

    public void setType(OntologyType type) {
        this.type = type;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setTenantIds(List<Integer> tenantIds) {
        this.tenantIds = tenantIds;
    }
}
