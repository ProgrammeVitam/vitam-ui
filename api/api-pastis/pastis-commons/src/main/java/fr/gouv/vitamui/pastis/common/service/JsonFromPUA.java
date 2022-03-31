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

package fr.gouv.vitamui.pastis.common.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.PuaData;
import fr.gouv.vitamui.pastis.common.dto.seda.SedaNode;
import fr.gouv.vitamui.pastis.common.util.RNGConstants;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JsonFromPUA {

    private Long idCounter = 0L;
    private static final String CONTENT = "Content";
    private static final String PROPERTIES = "properties";

    /**
     * Generates a Profile from a PUA file
     *
     * @param jsonPUA the JSON Object representing the PUA
     * @return
     */
    public ElementProperties getProfileFromPUA(JSONObject jsonPUA) throws IOException {
        String controlSchemaString = (String) jsonPUA.get("controlSchema");
        JSONObject controlSchema = new JSONObject(controlSchemaString);

        idCounter = 0L;
        // Adding root element DescriptiveMetadata
        ElementProperties root = new ElementProperties();
        root.setAdditionalProperties(controlSchema.getBoolean("additionalProperties"));
        root.setName("DescriptiveMetadata");
        root.setId(idCounter++);
        root.setLevel(0);
        root.setType(String.valueOf(RNGConstants.MetadaDataType.ELEMENT.getLabel()));

        // Adding ArchiveUnit Element
        ElementProperties archiveUnit = createChildren(root, "ArchiveUnit");
        archiveUnit.setType(String.valueOf(RNGConstants.MetadaDataType.ELEMENT.getLabel()));

        // Adding id element
        ElementProperties id = createChildren(archiveUnit, "id");
        id.setType(String.valueOf(RNGConstants.MetadaDataType.ATTRIBUTE.getLabel()));
        id.setValueOrData("data");
        id.setDataType(String.valueOf(RNGConstants.DataType.ID));

        SedaNode sedaNode = getArchiveUnitSedaNode();

        buildProfile(controlSchema, sedaNode, archiveUnit);

        sortTreeWithSeda(archiveUnit, sedaNode);

        return root;
    }

    /**
     * Sort the ElementProperties tree based on the Seda
     *
     * @param tree
     * @param sedaNode
     */
    public void sortTreeWithSeda(ElementProperties tree, SedaNode sedaNode) {
        tree.getChildren().sort(Comparator.comparing(
            c -> sedaNode.getChildren().stream().map(SedaNode::getName).collect(Collectors.toList())
                .indexOf(c.getName())));
        for (ElementProperties e : tree.getChildren()) {
            Optional<SedaNode> optionalSedaNode = sedaNode.getChildren().stream().filter(s -> s.getName().equals(e.getName())).findFirst();
            optionalSedaNode.ifPresent(node -> sortTreeWithSeda(e, node));
        }
    }

    private List<String> getRequiredFields(JSONObject controlSchema) {
        List<String> required = new ArrayList<>();
        if (controlSchema.has("required")) {
            required.addAll(controlSchema.getJSONArray("required").toList().stream().map(String.class::cast)
                .collect(Collectors.toList()));
        }
        return required;
    }

    private SedaNode getArchiveUnitSedaNode() throws IOException {
        InputStream inputStream =
            getClass().getClassLoader().getResourceAsStream("pua_validation/archiveUnitSeda.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        return objectMapper.readValue(inputStream, SedaNode.class);
    }

    /**
     * Get children definition of node by name
     *
     * @param sedaNode node to look for children
     * @param name name of children to look for
     * @return
     */
    private SedaNode getChildrenSedaNode(SedaNode sedaNode, String name) {
        String realName = sanitizeNodeName(name);
        return sedaNode.getChildren().stream().filter(c -> c.getName().equals(realName)).findAny().orElse(null);
    }

    private String sanitizeNodeName(String name) {
        String realName = name.replace("_", "");
        if (realName.equals("#management")) {
            realName = "Management";
        }
        return realName;
    }

    /**
     * Build the profile based on the PUA JSON file and the definition of an ArchiveUnit
     * Recursive
     *
     * @param jsonPUA
     * @param sedaNode
     * @param parent
     */
    private void buildProfile(JSONObject jsonPUA, SedaNode sedaNode, ElementProperties parent) {
        List<String> requiredFields = getRequiredFields(jsonPUA);
        if (jsonPUA.has(PROPERTIES)) {
            JSONObject properties = jsonPUA.getJSONObject(PROPERTIES);
            if (properties.length() != 0) {
                for (String propertyName : properties.keySet()) {
                    Set<String> childrensNames;
                    JSONObject propertiesNew;
                    List<String> requiredFieldsActual;
                    // If property's name equal 'Rules'
                    // Then we have to retrieve all the the sub-childrens in the Rules->items property
                    if (propertyName.equals("Rules")) {
                        requiredFieldsActual =
                            getRequiredFields(properties.getJSONObject(propertyName).getJSONObject("items"));
                        propertiesNew =
                            properties.getJSONObject(propertyName).getJSONObject("items").getJSONObject(PROPERTIES);
                        childrensNames = propertiesNew.keySet();
                    } else {
                        requiredFieldsActual = requiredFields;
                        propertiesNew = properties;
                        childrensNames = Collections.singleton(propertyName);
                    }
                    buildChildrenProfile(parent, sedaNode, requiredFieldsActual, childrensNames, propertiesNew);
                }
            }
        }
    }

    private void buildChildrenDefinition(SedaNode childrenSedaNode, JSONObject childPua, ElementProperties childrenParent,
        String childName, List<String> requiredFieldsActual){
        if (childrenSedaNode != null) {
            ElementProperties childProfile =
                getElementProperties(childrenSedaNode, childrenParent, childName, childPua,
                    requiredFieldsActual.contains(childName));

            buildProfile(childPua, childrenSedaNode, childProfile);
        }
    }

    private void buildChildrenProfile(ElementProperties parent, SedaNode sedaNode, List<String> requiredFieldsActual,
        Set<String> childrensNames, JSONObject propertiesNew){
        childrensNames.forEach(childName -> {
            JSONObject childPua = propertiesNew.getJSONObject(childName);
            SedaNode childrenSedaNode = getChildrenSedaNode(sedaNode, childName);

            ElementProperties childrenParent;
            // In a PUA the Content node in ArchiveUnit node is omitted.
            // So if we are in the ArchiveUnit Node, then we must check for the children in Content Node as well
            if (childrenSedaNode == null && parent.getName().equals("ArchiveUnit")) {
                childrenSedaNode = getChildrenSedaNode(getChildrenSedaNode(sedaNode, CONTENT), childName);

                ElementProperties content =
                    parent.getChildren().stream().filter(c -> c.getName().equals(CONTENT)).findAny()
                        .orElse(null);
                // Create "Content" ElementProperties if not created yet
                if (content == null) {
                    content = createChildren(parent, CONTENT);
                    content.setType(String.valueOf(RNGConstants.MetadaDataType.ELEMENT.getLabel()));
                }
                childrenParent = content;
            } else {
                childrenParent = parent;
            }
            // If the childrenDefinition is found then process the childPua and add it to the childProfile
            buildChildrenDefinition(childrenSedaNode, childPua, childrenParent, childName, requiredFieldsActual);
        });
    }

    /**
     * Build and retrieve an ElementProperties node
     *
     * @param sedaNode
     * @param parent
     * @param key
     * @param childPua
     * @return
     */
    private ElementProperties getElementProperties(SedaNode sedaNode, ElementProperties parent, String key,
        JSONObject childPua, Boolean required) {
        ElementProperties childProfile = createChildren(parent, key);
        childProfile.setType(RNGConstants.getTypeElement().get(sedaNode.getElement()));
        childProfile.setDataType(sedaNode.getType());

        Integer minItems = null;
        Integer maxItems = null;

        for (String k : childPua.keySet()) {
            switch (k) {
                case "$ref":
                    addPuaDataToElementIfNotPresent(childProfile);
                    addRefToElement(childProfile, childPua.getString(k));
                    break;
                case "enum":
                    addPuaDataToElementIfNotPresent(childProfile);
                    List<String> enume =
                        childPua.getJSONArray(k).toList().stream().map(String.class::cast).collect(Collectors.toList());
                    childProfile.getPuaData().setEnum(enume);
                    break;
                case "pattern":
                    addPuaDataToElementIfNotPresent(childProfile);
                    childProfile.getPuaData().setPattern(childPua.getString(k));
                    break;
                case "minLength":
                    addPuaDataToElementIfNotPresent(childProfile);
                    childProfile.getPuaData().setMinLenght(childPua.getInt(k));
                    break;
                case "maxLength":
                    addPuaDataToElementIfNotPresent(childProfile);
                    childProfile.getPuaData().setMaxLenght(childPua.getInt(k));
                    break;
                case "minimum":
                    addPuaDataToElementIfNotPresent(childProfile);
                    childProfile.getPuaData().setMinimum(childPua.getInt(k));
                    break;
                case "maximum":
                    addPuaDataToElementIfNotPresent(childProfile);
                    childProfile.getPuaData().setMaximum(childPua.getInt(k));
                    break;
                case "additionalProperties":
                    addPuaDataToElementIfNotPresent(childProfile);
                    childProfile.getPuaData().setAdditionalProperties(childPua.getBoolean(k));
                    break;
                case "exclusiveMinimum":
                    addPuaDataToElementIfNotPresent(childProfile);
                    childProfile.getPuaData().setExclusiveMinimum(childPua.getBoolean(k));
                    break;
                case "exclusiveMaximum":
                    addPuaDataToElementIfNotPresent(childProfile);
                    childProfile.getPuaData().setExclusiveMaximum(childPua.getBoolean(k));
                    break;
                case "description":
                    childProfile.setDocumentation(childPua.getString(k));
                    break;
                case "minItems":
                    minItems = childPua.getInt(k);
                    break;
                case "maxItems":
                    maxItems = childPua.getInt(k);
                    break;
                default:
                    break;
            }
        }
        childProfile.setCardinality(getCardinality(minItems, maxItems, required, sedaNode));

        return childProfile;
    }

    private String getCardinality(Integer minItems, Integer maxItems, Boolean required, SedaNode sedaNode) {
        if (Boolean.TRUE.equals(required)) {
            switch (sedaNode.getCardinality()) {
                case "1-N":
                case "0-N":
                    return "1-N";
                case "1":
                case "0-1":
                    return "1";
                default:
            }
            return "1";
        } else if (minItems != null && maxItems != null) {
            return minItems + "-" + maxItems;
        } else {
            return sedaNode.getCardinality();
        }
    }

    private void addPuaDataToElementIfNotPresent(ElementProperties childProfile) {
        if (childProfile.getPuaData() == null) {
            childProfile.setPuaData(new PuaData());
        }
    }

    private void addRefToElement(ElementProperties el, String ref) {
        ref = ref.substring(ref.lastIndexOf('/') + 1);
        el.getPuaData().setPattern(ref);
    }

    /**
     * @param parent tree of {@link ElementProperties}
     * @param name name of new {@link ElementProperties}
     * @return new child of {@link ElementProperties}
     */
    private ElementProperties createChildren(ElementProperties parent, String name) {
        String realName = sanitizeNodeName(name);
        ElementProperties children = new ElementProperties();
        children.setName(realName);
        children.setId(idCounter++);
        children.setParent(parent);
        children.setParentId(parent.getId());
        children.setLevel(parent.getLevel() + 1);
        parent.getChildren().add(children);
        return children;
    }
}
