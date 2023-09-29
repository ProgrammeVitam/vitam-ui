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

package fr.gouv.vitamui.referential.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitam.common.model.administration.OntologyOrigin;
import fr.gouv.vitam.common.model.administration.SchemaType;
import fr.gouv.vitamui.referential.common.model.Cardinality;
import fr.gouv.vitamui.referential.common.model.Collection;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ExtendedOntologyCreateDto {
    @JsonProperty("Tenant") private Integer tenant;
    @JsonProperty("Version") private Integer version;
    @JsonProperty("Depth") private Integer depth; // TODO: Useless, easy to compute
    @JsonProperty("path") private String path;
    @JsonProperty("DataType") private String dataType;
    @JsonProperty("DataSize") private String dataSize; // TODO: Useless, don't know how to use this
    @JsonProperty("Cardinality") private Cardinality cardinality;
    @JsonProperty("Identifier") private String identifier;
    @JsonProperty("ShortName") private String shortName;
    @JsonProperty("Description") private String description;
    @JsonProperty("CreationDate") private String creationDate;
    @JsonProperty("LastUpdate") private String lastUpdate;
    @JsonProperty("SedaField") private String sedaField;
    @JsonProperty("ApiField") private String apiField;
    @JsonProperty("Type") private SchemaType type;
    @JsonProperty("Origin") private OntologyOrigin origin;
    @JsonProperty("Collections") private List<Collection> collections;
    @JsonProperty("SedaVersions") private List<String> sedaVersions;
    @JsonProperty("RootPaths") private List<String> rootPaths;
}
