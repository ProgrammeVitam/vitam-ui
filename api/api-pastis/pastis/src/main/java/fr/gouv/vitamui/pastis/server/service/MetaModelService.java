/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

package fr.gouv.vitamui.pastis.server.service;

import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitam.common.model.administration.schema.SchemaType;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileVersion;
import fr.gouv.vitamui.pastis.common.dto.seda.SedaNode;
import fr.gouv.vitamui.pastis.common.util.SedaNodeUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MetaModelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaModelService.class);

    private final ExternalSchemaService externalSchemaService;

    public SedaNode getMetaModelForVersion(ProfileVersion profileVersion) throws IOException {
        // Get metamodel for wanted profile version
        String resourcePath = "metamodel/metamodel-" + profileVersion.getVersion() + ".json";
        SedaNode sedaNode = SedaNodeUtils.parseSedaNodeFromResource(resourcePath);
        // Get metamodel from external schema properties
        List<SedaNode> externalSedaNodes = getMetaModelFromExternalSchema();
        // Merge metamodel and external schema model
        return mergeSedaNodesIntoContent(sedaNode, externalSedaNodes);
    }

    public SedaNode getArchiveUnitMetaModelForVersion(ProfileVersion profileVersion) throws IOException {
        SedaNode sedaNode = getMetaModelForVersion(profileVersion);
        return sedaNode.getChild("DataObjectPackage").getChild("DescriptiveMetadata").getChild("ArchiveUnit");
    }

    private List<SedaNode> getMetaModelFromExternalSchema() {
        List<SchemaResponse> schemaResponsesList = externalSchemaService.getExternalSchemaModels();
        return convertSchemaModelsToSedaNodes(schemaResponsesList);
    }

    private List<SedaNode> convertSchemaModelsToSedaNodes(List<SchemaResponse> externalSchemaModels) {
        // Used to map schema Path (a.b.c) to SedaNode
        Map<String, SedaNode> schemaPathToSedaNode = new HashMap<>();
        // Convert SchemaResponse to SedaNode, without taking account the children
        for (SchemaResponse schemaResponse : externalSchemaModels) {
            String schemaPath = schemaResponse.getPath();
            String schemaName = schemaResponse.getFieldName();
            SedaNode sedaNode = convertSchemaModelToSedaNode(schemaName, schemaResponse);
            schemaPathToSedaNode.put(schemaPath, sedaNode);
        }
        // Rebuild the SedaNode hierarchy (needs to be done in two steps, to account for the case when the child
        // appears before the parent
        List<SedaNode> result = new ArrayList<>();
        for (Map.Entry<String, SedaNode> entry : schemaPathToSedaNode.entrySet()) {
            String schemaPath = entry.getKey();
            SedaNode sedaNode = entry.getValue();
            int lastDotPointer = schemaPath.lastIndexOf('.');
            if (lastDotPointer != -1) {
                String parentSchemaPath = schemaPath.substring(0, lastDotPointer);
                SedaNode parentSedaNode = schemaPathToSedaNode.get(parentSchemaPath);
                if (parentSedaNode == null) {
                    LOGGER.error("Schema item {} does not have its parent {}", schemaPath, parentSchemaPath);
                    continue; // Skip the item
                } else {
                    parentSedaNode.getChildren().add(sedaNode);
                }
            } else {
                // We only return root elements
                result.add(sedaNode);
            }
        }
        return result;
    }

    private SedaNode convertSchemaModelToSedaNode(String schemaName, SchemaResponse schemaResponse) {
        SedaNode result = new SedaNode();
        result.setName(schemaName);
        result.setNameFr(schemaResponse.getShortName());
        result.setElement(schemaResponse.getType() == SchemaType.OBJECT ? "Complex" : "Simple");
        result.setCardinality(
            switch (schemaResponse.getCardinality()) {
                case ZERO, ONE -> "0-1"; // We should NEVER receive the ZERO cardinality from the schema API
                case ONE_REQUIRED -> "1";
                case MANY -> "0-N";
                case MANY_REQUIRED -> "1-N";
            }
        );
        result.setType(
            switch (schemaResponse.getType()) {
                case OBJECT -> "null";
                case DATE -> "date";
                case ENUM, KEYWORD -> "token";
                case LONG -> "int";
                case TEXT -> "string";
                case DOUBLE -> "decimal";
                case BOOLEAN -> "boolean";
            }
        );
        result.setChoice("no"); // Not used
        result.setExtensible("no"); // Not used
        result.setEnumeration(new ArrayList<>());
        // Special case for boolean values. Is this really useful?
        if (schemaResponse.getType() == SchemaType.BOOLEAN) {
            result.getEnumeration().addAll(List.of("false", "true"));
        }
        result.setDefinition(schemaResponse.getDescription());
        result.setCollection("Unités d'archives");
        result.setChildren(new ArrayList<>());
        result.setExternal(true); // Mark the SedaNode as coming from an external schema
        return result;
    }

    private SedaNode mergeSedaNodesIntoContent(SedaNode main, List<SedaNode> otherNodes) {
        // Get Content node
        SedaNode content = main
            .getChild("DataObjectPackage")
            .getChild("DescriptiveMetadata")
            .getChild("ArchiveUnit")
            .getChild("Content");
        // Append the other nodes to its children
        content.getChildren().addAll(otherNodes);
        // Return initial node
        return main;
    }
}
