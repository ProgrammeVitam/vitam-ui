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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.PuaData;
import fr.gouv.vitamui.pastis.common.dto.pua.PuaMetadataDetails;
import fr.gouv.vitamui.pastis.common.dto.seda.SedaNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
public class PuaPastisValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PuaPastisValidator.class);
    public static final String CARDINALITY_0_N = "0-N";
    public static final String CARDINALITY_1 = "1";
    public static final String CARDINALITY_0_1 = "0-1";
    public static final String CARDINALITY_1_N = "1-N";
    private static final String CONTROL_SCHEMA = "controlSchema";
    private static final String DEFINITIONS = "definitions";
    private static final String PROPERTIES = "properties";
    private static final String MANAGEMENT_CONTROL = "#management";
    private static final String MANAGEMENT = "Management";
    private static final String CONTENT = "Content";
    private static final String SCHEMA = "$schema";
    private static final String ITEMS = "items";
    private static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    private static final String TYPE = "type";
    private static final String OBJECT = "object";
    private static final String ID = "ID";
    private static final String STRING = "string";
    private static final String STANDALONE_PUA_RESOURCE = "pua_validation/valid_pua.json";
    private static final String ONLINE_PUA_RESOURCE = "pua_validation/valid_pua_vitam.json";
    private static final String ARCHIVE_UNIT_SEDA_RESOURCE = "pua_validation/archiveUnitSeda.json";

    private static JSONObject profileJsonExpected;
    private static SedaNode archiveUnitSeda;

    public static final String REQUIRED = "required";

    private JSONObject getProfileJsonExpected(final boolean standalone) {
        if (profileJsonExpected != null) {
            return profileJsonExpected;
        }

        final String puaValidationResource = standalone ? STANDALONE_PUA_RESOURCE : ONLINE_PUA_RESOURCE;
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(puaValidationResource);

        assert inputStream != null;

        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final JSONTokener jsonTokener = new JSONTokener(inputStreamReader);

        profileJsonExpected = new JSONObject(jsonTokener);

        return profileJsonExpected;
    }

    private SedaNode getArchiveUnitSeda() throws IOException {
        if (Objects.nonNull(archiveUnitSeda)) {
            return archiveUnitSeda;
        }

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(ARCHIVE_UNIT_SEDA_RESOURCE);

        archiveUnitSeda = new ObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .readValue(inputStream, SedaNode.class);

        return archiveUnitSeda;
    }

    /**
     * Validate a PUA JSON file against a template file
     * using LENIENT comparison mode
     *
     * @param pua The string containing the JSON file to be validated
     * @throws AssertionError
     */
    public void validatePUA(JSONObject pua, boolean standalone) throws AssertionError {
        final JSONObject expectedJsonProfile = getProfileJsonExpected(standalone);

        // Compare list of field at the root level
        if (!standalone) {
            final Set<String> actualFieldList = new HashSet<>(pua.keySet());

            if (!actualFieldList.contains("name") && !actualFieldList.contains(CONTROL_SCHEMA)) {
                throw new AssertionError("Notice not contains the expected keys 'name' and 'controlSchema'");
            }
        }

        // Next tests are controlling the ControlSchema
        final JSONObject controlSchemaActual = new JSONObject(pua.getString(CONTROL_SCHEMA));
        final JSONObject controlSchemaExpected = new JSONObject(expectedJsonProfile.getString(CONTROL_SCHEMA));
        LOGGER.debug("Current control schema {}", controlSchemaActual);
        LOGGER.debug("Expected control schema {}", controlSchemaExpected);
        if (standalone) {
            // Checking that additionalProperties is present and is boolean
            if (controlSchemaActual.has(ADDITIONAL_PROPERTIES) &&
                !(controlSchemaActual.get(ADDITIONAL_PROPERTIES) instanceof Boolean)) {
                throw new AssertionError("PUA additionalProperties field does not contains a boolean value");
            }
            // Checking that #management object is present and at the correct position
            if (controlSchemaActual.has("patternProperties")) {
                JSONObject patternProperties = controlSchemaActual.getJSONObject("patternProperties");
                if (patternProperties.has(MANAGEMENT_CONTROL)) {
                    // Check that #management is not in both header and 'properties' object
                    JSONObject properties = controlSchemaActual.getJSONObject(PROPERTIES);
                    if (properties.has(MANAGEMENT_CONTROL)) {
                        throw new AssertionError(
                            "Can't have both '#management' key in header and in 'properties' object");
                    }
                }
            } else {
                if (controlSchemaActual.has(PROPERTIES)) {
                    JSONObject properties = controlSchemaActual.getJSONObject(PROPERTIES);
                    if (!properties.has(MANAGEMENT_CONTROL)) {
                        throw new AssertionError("Missing '#management' key in 'properties' object");
                    }
                } else {
                    throw new AssertionError("Missing 'properties' key in controlSchema");
                }

                // #HAVEFUN
            }
        } else {
            if (!controlSchemaActual.has(SCHEMA)) {
                throw new AssertionError("Missing '$schema' key in controlSchema' object");
            }
        }
    }

    public JSONObject getDefinitionsFromExpectedProfile() {
        final JSONObject baseProfile = getProfileJsonExpected(true);
        final String controlSchema = baseProfile.get(CONTROL_SCHEMA).toString();
        LOGGER.debug(" control schema {}", controlSchema);
        final JSONObject controlSchemaAsJSON = new JSONObject(controlSchema);

        return controlSchemaAsJSON.getJSONObject(DEFINITIONS);
    }

    /**
     * Finds the seda type of element based on his name
     *
     * @param elementName the name of the element to search on the archiveUnitSeda.json file
     * @param name
     * @return the seda type of element
     */
    private String getPUAMetadataType(final String elementName, final String name) throws IOException {
        final SedaNode sedaElement = getSedaMetadata(elementName, name);

        if (sedaElement != null) {
            return resolvePuaType(sedaElement);
        }
        return elementName.equals("MessageDigest") ? STRING : "undefined";
    }

    /**
     * Resolve the Pua element type based on VITAM given rules
     *
     * @param sedaElement the seda element type of the metadata
     * @return The type of pua element
     */
    private String resolvePuaType(final SedaNode sedaElement) {
        final String sedaType = sedaElement.getType();
        final String sedaElementType = sedaElement.getElement();
        final String sedaName = sedaElement.getName();
        final String sedaCardinality = sedaElement.getCardinality();

        if (sedaName.equals("Title") || sedaName.equals("Description") || sedaName.equals("algorithm")) {
            return STRING;
        }

        if (sedaName.equals("SignedObjectDigest")) {
            return OBJECT;
        }

        if (sedaElementType.equals("Simple") && !sedaElement.getType().equals("boolean") &&
            !sedaElement.getType().equals("integer") &&
            (sedaCardinality.equals(CARDINALITY_0_1) || sedaCardinality.equals(CARDINALITY_1))) {
            return STRING;
        }
        if ((sedaElement.isComplex() &&
            (sedaCardinality.equals(CARDINALITY_0_1) || sedaCardinality.equals(CARDINALITY_1)))) {
            return OBJECT;
        }
        if (sedaType.equals("boolean") &&
            (sedaCardinality.equals(CARDINALITY_0_1) || sedaCardinality.equals(CARDINALITY_1))) {
            return "boolean";
        }
        if (sedaType.equals("integer") &&
            (sedaCardinality.equals(CARDINALITY_0_1) || sedaCardinality.equals(CARDINALITY_1))) {
            return "integer";
        }
        if (sedaCardinality.equals(CARDINALITY_1_N) || sedaCardinality.equals(CARDINALITY_0_N)) {
            return "array";
        }
        if (sedaType.equals(ID)) {
            return STRING;
        }
        return "undefined";
    }

    /**
     * Recursively generates a tree of JSON objects based on a given ElementProperties object type
     *
     * @param elementsFromTree an ElementProperties List
     * @return a JSONArray representing all PUA elements of an ArchiveUnitProfile and its siblings
     */
    public JSONArray getJSONObjectFromAllTree(List<ElementProperties> elementsFromTree) {
        final JSONArray jsonArray = sortedJSONArray();
        final List<String> rulesToIgnore =
            Arrays.asList("StorageRule", "AppraisalRule", "AccessRule", "DisseminationRule", "ReuseRule",
                "ClassificationRule");
        final List<String> managementMetadata = Arrays.asList("LogBook", "NeedAuthorization");

        for (ElementProperties el : elementsFromTree) {
            setMetadataName(el);
            try {
                if (el.getName().equals(MANAGEMENT) && !el.getChildren().isEmpty()) {
                    JSONObject management = getJSONFromManagement(el);
                    jsonArray.put(management);
                }
                if (jsonArray.length() > 0 && jsonArray.toString().contains(el.getName())) {
                    ElementProperties element = getElementById(elementsFromTree, el.getParentId());
                    if (element != null && element.getName().equals(CONTENT)) {
                        JSONObject notManagementMapElement = getJSONObjectFromElement(el);
                        jsonArray.put(notManagementMapElement);
                    }
                } else if (!rulesToIgnore.contains(el.getName()) && !el.getName().equals(CONTENT) &&
                    !el.getName().equals(MANAGEMENT) && !managementMetadata.contains(el.getName())) {
                    JSONObject notManagementMapElement = getJSONObjectFromElement(el);
                    jsonArray.put(notManagementMapElement);
                }

            } catch (IOException e) {
                LOGGER.debug(e.getMessage());
            }
        }
        return jsonArray;
    }

    /**
     * Recursively generates a tree of JSON objects starting from the Management metadata
     *
     * @param element an ElementProperties object
     * @return a JSONArray representing all PUA elements of a Management metadata and its specific rules
     */
    public JSONObject getJSONFromManagement(ElementProperties element) throws IOException {
        final List<String> rulesMetadata =
            Arrays.asList("StorageRule", "AppraisalRule", "AccessRule", "DisseminationRule", "ReuseRule",
                "ClassificationRule");
        final List<String> childrenToEncapsulate = Arrays.asList("Rule", "StartDate");
        final List<String> rulesFound = new ArrayList<>();
        final JSONObject pua = sortedJSON();

        if (Objects.nonNull(element.getPuaData()) && Objects.nonNull(element.getPuaData().getAdditionalProperties())) {
            pua.put(ADDITIONAL_PROPERTIES, element.getPuaData().getAdditionalProperties());
        }
        if (element.getChildren().isEmpty()) {
            return retrieveAccumulatedJsonManagementProperties(pua);
        }

        // JSON Object representing all PUA elements of a Management metadata and its specific rules
        retrieveAccumulatedJsonManagementProperties(element, rulesMetadata, childrenToEncapsulate, rulesFound, pua);

        final List<String> requiredProperties = getRequiredProperties(element);

        if (isNotEmpty(requiredProperties)) {
            pua.put(REQUIRED, requiredProperties);
        }

        return retrieveAccumulatedJsonManagementProperties(pua);
    }

    /**
     * 1. Check special cases
     * 2. If special cases have children, encapsulate them into "Rules : { items : {childName : { ..."
     * 3. Once the children of special cases are processed, we put them into Rules -> items
     * 4. Convert to json object via map and update its property
     * 5. Retrieve parent properties and add more elements to root element properties
     *
     * @param element
     * @param rulesMetadata
     * @param childrenToEncapsulate
     * @param rulesFound
     * @param pua
     * @throws IOException
     */
    private void retrieveAccumulatedJsonManagementProperties(
        final ElementProperties element,
        final List<String> rulesMetadata,
        final List<String> childrenToEncapsulate,
        final List<String> rulesFound,
        final JSONObject pua
    ) throws IOException {
        for (ElementProperties childElement : element.getChildren()) {
            final JSONObject childrenOfRule = sortedJSON();
            final JSONObject grandChildrenOfRule = sortedJSON();
            final JSONObject propertiesRules = sortedJSON();

            // add endDate by default cardinality 0-1
            final PuaMetadataDetails endDateDetails = new PuaMetadataDetails();
            endDateDetails.setType(STRING);
            final ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            grandChildrenOfRule.put("EndDate", new JSONObject(mapper.writeValueAsString(endDateDetails)));

            final PuaMetadataDetails ruleTypeMetadataDetails = new PuaMetadataDetails();
            final SedaNode sedaElement = getSedaMetadata(childElement.getName(), null);

            // 1. Check special cases
            if (checkSpecialCases(rulesMetadata, rulesFound, childElement, sedaElement)) {
                pua.accumulate(PROPERTIES, getJSONObjectFromElement(childElement));
                continue;
            }

            getMetaDataFromSeda(childElement, ruleTypeMetadataDetails, sedaElement);
            final Map<String, PuaMetadataDetails> ruleTypeMetadataMap = new HashMap<>();
            final Map<String, PuaMetadataDetails> nonSpecialChildOfRule = new HashMap<>();
            final List<String> requiredNonSpecialChildren = new ArrayList<>();
            final List<String> requiredChildren = new ArrayList<>();
            // 2. If special cases have children, encapsulate them into "Rules : { items : {childName : { ..."
            if (!childElement.getChildren().isEmpty()) {
                for (ElementProperties grandChild : childElement.getChildren()) {
                    final SedaNode node = getSedaMetadata(grandChild.getName(), childElement.getName());
                    if (childrenToEncapsulate.contains(grandChild.getName())) {
                        childrenContainsGrandChildName(grandChildrenOfRule, ruleTypeMetadataDetails, requiredChildren,
                            grandChild, node);
                    } else {
                        final PuaMetadataDetails nonSpecialChildOfRuleDetails = new PuaMetadataDetails();
                        getMetaDataFromSeda(grandChild, nonSpecialChildOfRuleDetails, node);
                        nonSpecialChildOfRule.put(grandChild.getName(), nonSpecialChildOfRuleDetails);
                        //Required field
                        if (grandChild.getCardinality().equals(CARDINALITY_1))
                            requiredNonSpecialChildren.add(grandChild.getName());
                    }
                    ruleTypeMetadataMap.put(childElement.getName(), ruleTypeMetadataDetails);
                }
            }
            // 3. Once the children of special cases are processed, we put them into Rules -> items
            putChildrenIntoRules(childrenOfRule, grandChildrenOfRule, propertiesRules, requiredChildren);

            // 4. Convert to json object via map and update its property
            final ObjectMapper mapper2 = new ObjectMapper();
            mapper2.registerModule(new AfterburnerModule());
            final JSONObject ruleTypeMetadata = new JSONObject(ruleTypeMetadataMap);
            ruleTypeMetadata.getJSONObject(childElement.getName()).put(PROPERTIES, propertiesRules);
            putRequiredNonSpecialChildren(childElement, requiredNonSpecialChildren, ruleTypeMetadata, requiredChildren);
            for (Map.Entry<String, PuaMetadataDetails> entry : nonSpecialChildOfRule.entrySet()) {
                final PuaMetadataDetails details = entry.getValue();

                if (entry.getKey().equals("PreventInheritance") || entry.getKey().equals("PreventRulesId")) {
                    final JSONObject inheritance = new JSONObject();
                    final PuaMetadataDetails preventRulesId = new PuaMetadataDetails();

                    if (entry.getKey().equals("PreventInheritance")) {
                        preventRulesId.setType("boolean");
                    } else {
                        preventRulesId.setType("array");
                    }

                    final Optional<ElementProperties> elOpt = childElement.getChildren().stream()
                        .filter(e -> e.getName().equals(entry.getKey()))
                        .findFirst();

                    if (elOpt.isPresent()) {
                        final ElementProperties el = elOpt.get();

                        preventRulesId.setDescription(el.getDocumentation());
                        if (el.getCardinality().equals(CARDINALITY_0_1)) {
                            getMinAndMAxItems(el, preventRulesId);
                        } else if (el.getCardinality().equals(CARDINALITY_1)) {
                            getMinAndMAxItems(el, preventRulesId);
                        }
                        if (null != el.getPuaData()) {
                            if (null != el.getPuaData().getPattern()) {
                                preventRulesId.setPattern(el.getPuaData().getPattern());
                            }
                            if (null != el.getPuaData().getEnum()) {
                                preventRulesId.setEnums(el.getPuaData().getEnum());
                            }
                        }

                    }
                    inheritance.put(TYPE, OBJECT).put(ADDITIONAL_PROPERTIES, false).put(PROPERTIES,
                        new JSONObject().put(entry.getKey(), new JSONObject(details.serialiseString()))
                            .put(entry.getKey(), new JSONObject(preventRulesId.serialiseString())));
                    if (!childElement.getChildren().isEmpty()) {
                        final String cardinality =
                            childElement.getChildren().stream().filter(e -> e.getName().equals("PreventInheritance"))
                                .map(ElementProperties::getCardinality).collect(Collectors.joining());
                        if (cardinality.equals(CARDINALITY_1)) {
                            inheritance.accumulate(REQUIRED, Collections.singletonList(entry.getKey()));
                        }
                    }
                    ruleTypeMetadata.getJSONObject(childElement.getName()).getJSONObject(PROPERTIES)
                        .put("Inheritance", inheritance);
                } else {
                    ruleTypeMetadata.getJSONObject(childElement.getName()).getJSONObject(PROPERTIES)
                        .put(entry.getKey(), new JSONObject(details.serialiseString()));
                }
            }

            // 5. We retrieve parent properties and add more elements to root element properties
            pua.accumulate(PROPERTIES, ruleTypeMetadata);
            if (!rulesFound.isEmpty())
                pua.put(REQUIRED, rulesFound);
        }
    }

    /**
     * Check specials cases
     *
     * @param rulesMetadata
     * @param rulesFound
     * @param childElement
     * @param sedaElement
     * @return
     */
    private boolean checkSpecialCases(
        final List<String> rulesMetadata,
        final List<String> rulesFound,
        final ElementProperties childElement,
        final SedaNode sedaElement
    ) {
        if (!rulesMetadata.contains(childElement.getName()) || sedaElement == null) {
            return true;
        }

        if (childElement.getCardinality().equals(CARDINALITY_1) && sedaElement.isComplex()) {
            rulesFound.add(childElement.getName());
        }

        return false;
    }

    /**
     * put Required Non-Special Children
     *
     * @param childElement
     * @param requiredNonSpecialChildren
     * @param ruleTypeMetadata
     * @param requiredChildren
     */
    private void putRequiredNonSpecialChildren(ElementProperties childElement, List<String> requiredNonSpecialChildren,
        JSONObject ruleTypeMetadata, List<String> requiredChildren) {
        if (!requiredNonSpecialChildren.isEmpty()) {
            requiredNonSpecialChildren.removeIf(e -> e.equals("PreventInheritance"));

        }
        if (!childElement.getChildren().isEmpty() &&
            childElement.getChildren().stream().anyMatch(e -> e.getName().equals("PreventInheritance"))) {
            if (childElement.getChildren().stream().filter(e -> e.getName().equals("PreventInheritance"))
                .collect(Collectors.toList()).get(0).getCardinality().equals(CARDINALITY_1)) {
                requiredNonSpecialChildren.add("Inheritance");
            }
        }
        if (!requiredChildren.isEmpty()) {
            requiredNonSpecialChildren.add("Rules");
        }

        if (!requiredNonSpecialChildren.isEmpty()) {
            ruleTypeMetadata.getJSONObject(childElement.getName()).put(REQUIRED, requiredNonSpecialChildren);
        }
    }

    /**
     * Put Children into rules
     *
     * @param childrenOfRule
     * @param grandChildrenOfRule
     * @param propertiesRules
     * @param requiredChildren
     */
    private void putChildrenIntoRules(JSONObject childrenOfRule, JSONObject grandChildrenOfRule,
        JSONObject propertiesRules, List<String> requiredChildren) {
        if (!grandChildrenOfRule.isEmpty()) {
            JSONObject propertyOfItems = new JSONObject().put(TYPE, OBJECT);
            propertyOfItems.put(ADDITIONAL_PROPERTIES, false);
            propertyOfItems.put(PROPERTIES, grandChildrenOfRule);
            if (requiredChildren.isEmpty()) {
                childrenOfRule.put("maxItems", 1);
            } else {
                childrenOfRule.put("minItems", 1);
                childrenOfRule.put("maxItems", 1);
                propertyOfItems.put(REQUIRED, requiredChildren);
            }
            childrenOfRule.put("type", "array");
            childrenOfRule.put(ITEMS, propertyOfItems);
            propertiesRules.put("Rules", childrenOfRule);
        }
    }

    /**
     * If children contain grand child name
     *
     * @param grandChildrenOfRule
     * @param ruleTypeMetadataDetails
     * @param requiredChildren
     * @param grandChild
     * @param node
     * @throws JsonProcessingException
     */
    private void childrenContainsGrandChildName(
        final JSONObject grandChildrenOfRule,
        final PuaMetadataDetails ruleTypeMetadataDetails,
        final List<String> requiredChildren,
        final ElementProperties grandChild,
        final SedaNode node
    ) throws JsonProcessingException {
        final PuaMetadataDetails childOfRuleDetails = new PuaMetadataDetails();

        getMetaDataFromSeda(grandChild, childOfRuleDetails, node);
        if (grandChild.getCardinality().startsWith(CARDINALITY_1)) {
            requiredChildren.add(grandChild.getName());
        }
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final JSONObject childProperties = new JSONObject(mapper.writeValueAsString(childOfRuleDetails));
        grandChildrenOfRule.put(grandChild.getName(), childProperties);
        ruleTypeMetadataDetails.setProperties(grandChildrenOfRule);
    }

    /**
     * Retrieve the accumulated JSONArray properties from pua
     *
     * @param pua is JSONObjet that contains Management section of PUA
     * Convert it into a JSONObject and put it into a #mangagement key
     */
    public JSONObject retrieveAccumulatedJsonManagementProperties(final JSONObject pua) {
        final JSONObject managementAsJSONObject = new JSONObject();

        if (pua.keySet().contains(PROPERTIES)) {
            try {
                final JSONArray accumulatedProperties = pua.getJSONArray(PROPERTIES);
                final String propertiesAsString =
                    accumulatedProperties.toString().substring(1, accumulatedProperties.toString().length() - 1)
                        .replaceAll("(},\\{)", ",");
                final JSONObject properties = new JSONObject(propertiesAsString);
                final JSONObject propertiesRequiredJson = new JSONObject();

                propertiesRequiredJson.put(PROPERTIES, properties);
                if (pua.keySet().contains(REQUIRED)) {
                    propertiesRequiredJson.put(REQUIRED, pua.getJSONArray(REQUIRED));
                }
                if (pua.has(ADDITIONAL_PROPERTIES)) {
                    propertiesRequiredJson.put(ADDITIONAL_PROPERTIES, pua.get(ADDITIONAL_PROPERTIES));
                }
                managementAsJSONObject.put(MANAGEMENT_CONTROL, propertiesRequiredJson);
            } catch (JSONException e) {
                LOGGER.debug(e.getMessage());
                managementAsJSONObject.put(MANAGEMENT_CONTROL, pua);
            }
        } else {
            managementAsJSONObject.put(MANAGEMENT_CONTROL, pua);
        }

        return managementAsJSONObject;
    }

    /**
     * Recursively searches for a seda node metadata based on a name
     *
     * @param elementName the name of the seda element to be found
     * @return a SedaNode object representation of a given seda element
     */

    private SedaNode getSedaMetadata(String elementName, String parentName) throws IOException {
        SedaNode sedaTree = getArchiveUnitSeda();
        SedaNode result;
        if (null != parentName) {
            result = Objects.requireNonNull(
                    sedaTree.flattened().filter(childName -> childName.getName().equals(parentName)).findAny().orElse(null))
                .getChildren().stream().filter(childName -> childName.getName().equals(elementName)).findAny()
                .orElse(null);
        } else {
            result = sedaTree.flattened().filter(e -> e.getName().equals(elementName)).findAny().orElse(null);
        }
        return result;
    }

    /**
     * Recursively converts an ElementProperty tree and its children, into a Map
     *
     * @return a HashMap containing a tree of Pua metadata and its children
     */
    public JSONObject getJSONObjectFromElement(ElementProperties elementProperties) throws IOException {
        final SedaNode sedaElement = getSedaMetadata(elementProperties.getName(), null);
        final PuaMetadataDetails puaMetadataDetails = new PuaMetadataDetails();
        final JSONObject json = new JSONObject();

        getMetaDataFromSeda(elementProperties, puaMetadataDetails, sedaElement);

        if (isNotEmpty(elementProperties.getChildren())) {
            List<String> requiredProperties = getRequiredProperties(elementProperties);
            if (isNotEmpty(requiredProperties)) {
                puaMetadataDetails.setRequired(requiredProperties);
            }
        }

        json.put(elementProperties.getName(), new JSONObject(puaMetadataDetails.serialiseString()));

        if (elementProperties.getChildren().isEmpty()) {
            return json;
        }

        if (puaMetadataDetails.getType().equals("array")) {
            final JSONObject items = new JSONObject();

            items.put("type", OBJECT);
            items.put(ADDITIONAL_PROPERTIES, elementProperties.getPuaData().getAdditionalProperties());

            if (null != json.getJSONObject(elementProperties.getName()).opt(REQUIRED)) {
                json.getJSONObject(elementProperties.getName()).remove(REQUIRED);
            }

            json.getJSONObject(elementProperties.getName()).put(ITEMS, items);
            json.getJSONObject(elementProperties.getName()).remove(ADDITIONAL_PROPERTIES);
            json.getJSONObject(elementProperties.getName()).getJSONObject(ITEMS).put(PROPERTIES, new JSONObject());

            if (null != puaMetadataDetails.getRequired() && !puaMetadataDetails.getRequired().isEmpty()) {
                json.getJSONObject(elementProperties.getName()).getJSONObject(ITEMS)
                    .put(REQUIRED, puaMetadataDetails.getRequired());
            }

            getJSONObjectFromElement(elementProperties,
                json.getJSONObject(elementProperties.getName()).getJSONObject(ITEMS).getJSONObject(PROPERTIES));
        } else {
            json.getJSONObject(elementProperties.getName()).put(PROPERTIES, new JSONObject());
            getJSONObjectFromElement(elementProperties,
                json.getJSONObject(elementProperties.getName()).getJSONObject(PROPERTIES));
        }

        return json;
    }

    public void getJSONObjectFromElement(ElementProperties elementProperties, JSONObject json) throws IOException {
        if (elementProperties.getChildren().isEmpty()) {
            return;
        }

        for (ElementProperties el : elementProperties.getChildren()) {
            final PuaMetadataDetails puaMetadataDetails = new PuaMetadataDetails();

            puaMetadataDetails.setType(getPUAMetadataType(el.getName(), elementProperties.getName()));
            puaMetadataDetails.setDescription(el.getDocumentation());
            if (null != el.getPuaData() && null != el.getPuaData().getEnum()) {
                puaMetadataDetails.setEnums(el.getPuaData().getEnum());
            }
            if (null != el.getPuaData() && null != el.getPuaData().getPattern()) {
                puaMetadataDetails.setPattern(el.getPuaData().getPattern());
            }
            if (puaMetadataDetails.getType().equals("array")) {
                getMinAndMAxItems(el, puaMetadataDetails);
            }
            setMetadataName(el);
            if (isNotEmpty(el.getChildren())) {
                final List<String> requiredProperties = getRequiredProperties(el);
                if (isNotEmpty(requiredProperties)) {
                    puaMetadataDetails.setRequired(requiredProperties);
                }
            }
            if (el.getName().equals("SignedObjectDigest")) {
                setElementIfSignedObjectDigest(el);
                final List<String> required = puaMetadataDetails.getRequired();
                required.add("MessageDigest");
                puaMetadataDetails.setRequired(required);
            }
            setChildName(json, el, puaMetadataDetails);
            if (!el.getChildren().isEmpty()) {
                if (null != el.getPuaData() && null != el.getPuaData().getAdditionalProperties()) {
                    json.getJSONObject(el.getName())
                        .put(ADDITIONAL_PROPERTIES, el.getPuaData().getAdditionalProperties());
                }

                if (puaMetadataDetails.getType().equals("array")) {
                    final JSONObject items = new JSONObject();
                    items.put("type", OBJECT);
                    items.put(ADDITIONAL_PROPERTIES, el.getPuaData().getAdditionalProperties());
                    json.getJSONObject(el.getName()).remove(REQUIRED);
                    json.getJSONObject(el.getName()).put(ITEMS, items);
                    json.getJSONObject(el.getName()).remove(ADDITIONAL_PROPERTIES);
                    json.getJSONObject(el.getName()).getJSONObject(ITEMS)
                        .put(PROPERTIES, new JSONObject(new PuaData()));
                    if (null != puaMetadataDetails.getRequired() && !puaMetadataDetails.getRequired().isEmpty()) {
                        json.getJSONObject(el.getName()).getJSONObject(ITEMS)
                            .put(REQUIRED, puaMetadataDetails.getRequired());
                    }

                    getJSONObjectFromElement(el,
                        json.getJSONObject(el.getName()).getJSONObject(ITEMS).getJSONObject(PROPERTIES));
                } else {
                    json.getJSONObject(el.getName()).put(PROPERTIES, new JSONObject());
                    getJSONObjectFromElement(el, json.getJSONObject(el.getName()).getJSONObject(PROPERTIES));
                }

            } else {
                if (puaMetadataDetails.getType().equals("array")) {
                    final JSONObject items = new JSONObject();
                    items.put("type", OBJECT);
                    if (null != el.getPuaData() && null != el.getPuaData().getAdditionalProperties()) {
                        items.put(ADDITIONAL_PROPERTIES, el.getPuaData().getAdditionalProperties());
                    }
                    List<String> keyToDelete = new ArrayList<>();
                    json.getJSONObject(el.getName()).keySet().forEach(key -> {
                        if (!key.equals(TYPE)) {
                            keyToDelete.add(key);
                        }
                    });
                    keyToDelete.forEach(e -> json.getJSONObject(el.getName()).remove(e)
                    );
                    puaMetadataDetails.setType(STRING);

                    json.getJSONObject(el.getName()).remove(ADDITIONAL_PROPERTIES);
                    if (null != puaMetadataDetails.getMinItems()) {
                        json.getJSONObject(el.getName()).put("minItems", puaMetadataDetails.getMinItems());
                        puaMetadataDetails.setMinItems(null);
                    }
                    if (null != puaMetadataDetails.getMaxItems()) {
                        json.getJSONObject(el.getName()).put("maxItems", puaMetadataDetails.getMaxItems());
                        puaMetadataDetails.setMaxItems(null);
                    }
                    json.getJSONObject(el.getName())
                        .put(ITEMS, new JSONObject(puaMetadataDetails.serialiseString()));
                }
            }
        }
    }

    private void setElementIfSignedObjectDigest(ElementProperties el) {
        final List<ElementProperties> children = el.getChildren();
        final ElementProperties messageDigest = new ElementProperties();

        messageDigest.setName("MessageDigest");
        messageDigest.setCardinality(CARDINALITY_1);
        children.add(messageDigest);
        el.setChildren(children);
    }

    private void setMetadataName(ElementProperties child) {
        switch (child.getName()) {
            case "EventIdentifier":
                child.setName("evId");
                break;
            case "EventTypeCode":
                child.setName("evTypeProc");
                break;
            case "EventType":
                child.setName("evType");
                break;
            case "EventDateTime":
                child.setName("evDateTime");
                break;
            case "EventDetail":
                child.setName("evTypeDetail");
                break;
            case "Outcome":
                child.setName("outcome");
                break;
            case "OutcomeDetail":
                child.setName("outDetail");
                break;
            case "OutcomeDetailMessage":
                child.setName("outMessg");
                break;
            case "EventDetailData":
                child.setName("evDetData");
                break;
            case "algorithm":
                child.setName("Algorithm");
                break;
            default:
                break;
        }
    }

    private void setChildName(JSONObject json, ElementProperties el, PuaMetadataDetails puaMetadataDetails)
        throws JsonProcessingException {
        json.put(el.getName(), new JSONObject(puaMetadataDetails.serialiseString()));
    }

    public List<String> getRequiredProperties(ElementProperties elementProperties) {
        final List<String> listRequired = new ArrayList<>();
        elementProperties.getChildren().forEach(child -> {
            try {
                final SedaNode sedaElement = getSedaMetadata(child.getName(), elementProperties.getName());

                if ((child.getCardinality().equals(CARDINALITY_1_N) && isMultiple(sedaElement))
                    || (child.getCardinality().equals(CARDINALITY_1) &&
                    !sedaElement.getCardinality().equals(CARDINALITY_1))
                    || sedaElement.getCardinality().equals(CARDINALITY_1)
                ) {
                    setMetadataName(child);
                    listRequired.add(child.getName());
                    child.setName(sedaElement.getName());
                }
            } catch (IOException e) {
                LOGGER.debug(e.getMessage());
            }
        });

        return listRequired;
    }

    public boolean isMultiple(SedaNode sedaNode) {
        return sedaNode.getCardinality().equals(CARDINALITY_0_N)
            || sedaNode.getCardinality().equals(CARDINALITY_1_N);
    }

    public boolean isMultiple(ElementProperties elementProperties) {
        return elementProperties.getCardinality().equals(CARDINALITY_0_N)
            || elementProperties.getCardinality().equals(CARDINALITY_1_N);
    }

    public boolean isRequired(SedaNode sedaNode) {
        return sedaNode.getCardinality().equals(CARDINALITY_1)
            || sedaNode.getCardinality().equals(CARDINALITY_1_N);
    }

    public boolean isRequired(ElementProperties elementProperties) {
        return elementProperties.getCardinality().equals(CARDINALITY_1)
            || elementProperties.getCardinality().equals(CARDINALITY_1_N);
    }

    public List<ElementProperties> ignoreMetadata(ElementProperties elementProperties) {
        final List<String> metadataToIgnore = Arrays.asList("DescriptiveMetadata", "ArchiveUnit");

        return elementProperties.flattened()
            .filter(child -> !metadataToIgnore.contains(child.getName()) && child.getType().equals("element"))
            .collect(toList());
    }

    public List<String> getHeadRequired(final List<ElementProperties> elementsFromTree) {
        final List<String> list = new ArrayList<>();

        for (final ElementProperties element : elementsFromTree) {
            try {
                final SedaNode sedaElement = getSedaMetadata(element.getName(), null);
                final ElementProperties parent = getElementById(elementsFromTree, element.getParentId());
                boolean isCardinalityValid = false;

                if (parent != null &&
                    (parent.getName().equals(CONTENT) || element.getName().equals("ArchiveUnitProfile"))) {
                    if (element.getCardinality().equals(CARDINALITY_1_N) &&
                        sedaElement.getCardinality().equals(CARDINALITY_0_N)) {
                        isCardinalityValid = true;
                    } else if (element.getCardinality().equals(CARDINALITY_1) &&
                        !sedaElement.getCardinality().equals(CARDINALITY_1)) {
                        isCardinalityValid = true;
                    } else if (sedaElement.getCardinality().equals(CARDINALITY_1)) {
                        isCardinalityValid = true;
                    }
                } else if (element.getName().equals(MANAGEMENT) && element.getCardinality().equals(CARDINALITY_1)) {
                    isCardinalityValid = true;
                } else if (element.getName().equals("ArchiveUnitProfile") &&
                    element.getCardinality().equals(CARDINALITY_1)) {
                    isCardinalityValid = true;
                }

                if (isCardinalityValid) {
                    if (element.getName().equals(MANAGEMENT) && !element.getChildren().isEmpty()) {
                        list.add(MANAGEMENT_CONTROL);
                    } else {
                        list.add(element.getName());
                    }
                }
            } catch (IOException e) {
                LOGGER.debug(e.getMessage());
            }
        }
        return list;
    }

    public void getMetaDataFromSeda(ElementProperties el, PuaMetadataDetails puaMetadataDetails, SedaNode sedaElement) {
        final String puaType = resolvePuaType(sedaElement);
        final boolean hasAdditionalProperties = el.getPuaData() != null
            && el.getPuaData().getAdditionalProperties() != null;
        final boolean hasPattern = el.getPuaData() != null
            && el.getPuaData().getPattern() != null;
        final boolean hasEnum = el.getPuaData() != null
            && el.getPuaData().getEnum() != null;

        // get pua type
        puaMetadataDetails.setType(puaType);
        puaMetadataDetails.setDescription((el.getDocumentation()));

        // Only complex seda nodes seems extensible.
        // We try to apply additionalProperties rules only on it.
        if (hasAdditionalProperties && sedaElement.isComplex() && sedaElement.isExtensible()) {
            puaMetadataDetails.setAdditionalProperties(el.getPuaData().getAdditionalProperties());
        } else {
            // For other case, only seda rules should be applied.
            puaMetadataDetails.setAdditionalProperties(sedaElement.isExtensible());
        }

        if (el.getCardinality() != null && puaMetadataDetails.getType().equals("array")) {
            getMinAndMAxItems(el, puaMetadataDetails);
        }

        if (hasPattern) {
            puaMetadataDetails.setPattern(el.getPuaData().getPattern());
        }

        if (hasEnum) {
            puaMetadataDetails.setEnums(el.getPuaData().getEnum());
        }

        if (el.getValue() != null) {
            puaMetadataDetails.setEnums(Collections.singletonList(el.getValue()));
        }
    }

    private void getMinAndMAxItems(ElementProperties el, PuaMetadataDetails puaMetadataDetails) {
        switch (el.getCardinality()) {
            case CARDINALITY_1: {
                puaMetadataDetails.setMinItems(1);
                puaMetadataDetails.setMaxItems(1);
                break;
            }
            case CARDINALITY_0_1: {
                puaMetadataDetails.setMinItems(0);
                puaMetadataDetails.setMaxItems(1);
                break;
            }
            case CARDINALITY_1_N: {
                puaMetadataDetails.setMinItems(1);
                break;
            }
            default:
                break;

        }
    }

    public ElementProperties getElementById(List<ElementProperties> elementProperties, Long id) {
        for (ElementProperties el : elementProperties) {
            final Long elementId = el.getId();

            if (elementId != null && elementId.equals(id)) {
                return el;
            }
        }

        return null;
    }

    /**
     * Order a JSONObject
     *
     * @return an ordered JSONObject
     */
    public JSONObject sortedJSON() {
        final JSONObject jsonObj = new JSONObject();

        try {
            final Field changeMap = jsonObj.getClass().getDeclaredField("map");

            changeMap.setAccessible(true);
            changeMap.set(jsonObj, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.error(e.getMessage());
        }

        return jsonObj;
    }

    /**
     * Order a JSONOArray
     *
     * @return an ordered JSONArray
     */
    public JSONArray sortedJSONArray() {
        final JSONArray jsonArray = new JSONArray();

        try {
            final Field changeMap = jsonArray.getClass().getDeclaredField("map");

            changeMap.setAccessible(true);
            changeMap.set(jsonArray, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.debug(e.getMessage());
        }

        return jsonArray;
    }
}
