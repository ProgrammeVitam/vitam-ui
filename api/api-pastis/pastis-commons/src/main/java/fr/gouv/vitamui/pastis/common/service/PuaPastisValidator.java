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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.pua.PuaMetadataDetails;
import fr.gouv.vitamui.pastis.common.dto.seda.SedaNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class PuaPastisValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PuaPastisValidator.class);

    private static JSONObject profileJsonExpected;

    private static SedaNode archiveUnitSeda;

    private static final String CONTROLSCHEMA = "controlSchema";
    private static final String DEFINITIONS = "definitions";
    private static final String PROPERTIES = "properties";
    private static final String MANAGEMENTCONTROL = "#management";
    private static final String MANAGEMENT = "Management";
    private static final String CONTENT = "Content";
    private static final String COMPLEX = "Complex";
    private static final String REQUIRED = "required";
    private static final String SCHEMA = "$schema";

    private JSONObject getProfileJsonExpected(boolean standalone) {
        if (profileJsonExpected == null) {
            InputStream inputStream;
            if(standalone)
                 inputStream = getClass().getClassLoader().getResourceAsStream("pua_validation/valid_pua.json");

            else{
                 inputStream = getClass().getClassLoader().getResourceAsStream("pua_validation/valid_pua_vitam.json");
            }
            assert inputStream != null;
            JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStream));
            setProfileJsonExpected(new JSONObject(tokener));
        }
        return profileJsonExpected;
    }

    private static void setProfileJsonExpected(JSONObject jsonObject) {
        profileJsonExpected = jsonObject;
    }

    private SedaNode getArchiveUnitSeda() throws IOException {
        if (PuaPastisValidator.getArchiveUnitSedaMember() == null) {
            InputStream inputStream =
                getClass().getClassLoader().getResourceAsStream("pua_validation/archiveUnitSeda.json");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            PuaPastisValidator.setArchiveUnitSedaMember(objectMapper.readValue(inputStream, SedaNode.class));
        }
        return PuaPastisValidator.getArchiveUnitSedaMember();
    }

    private static SedaNode getArchiveUnitSedaMember() {
        return archiveUnitSeda;
    }

    private static void setArchiveUnitSedaMember(SedaNode sedaNode) {
        archiveUnitSeda = sedaNode;
    }

    /**
     * Validate a PUA JSON file against a template file
     * using LENIENT comparison mode
     *
     * @param pua The string containing the JSON file to be validated
     * @throws AssertionError
     */
    public void validatePUA(JSONObject pua, boolean standalone) throws AssertionError {
        JSONObject profileJson = getProfileJsonExpected(standalone);

        // Compare list of field at the root level
        if (!standalone) {
            Set<String> actualFieldList = pua.keySet().stream().collect(toSet());
            if(!actualFieldList.contains("name") && !actualFieldList.contains("controlSchema")){
                throw new AssertionError("Notice not contains the expected keys 'name' and 'controlSchema'");
            }
        }


    // Next tests are controlling the ControlSchema
    String controlSchemaString = pua.getString(CONTROLSCHEMA);
    JSONObject controlSchemaActual = new JSONObject(controlSchemaString);
    controlSchemaString = profileJson.getString(CONTROLSCHEMA);
    JSONObject controlSchemaExpected = new JSONObject(controlSchemaString);
    LOGGER.error(controlSchemaActual.toString() + " control shema actuelle");
        LOGGER.error(controlSchemaExpected.toString() + " control shema Expected");
        if(standalone) {
            // Checking that the whole structure is respected. Doesn't care that the pua contains extended fields.
            JSONAssert.assertEquals(controlSchemaExpected, controlSchemaActual, JSONCompareMode.LENIENT);

            // Checking that the definitions list is exactly the same as expected
            JSONAssert.assertEquals(controlSchemaExpected.getJSONObject(DEFINITIONS),
                controlSchemaActual.getJSONObject(DEFINITIONS), JSONCompareMode.STRICT);

        // Checking that additionalProperties is present and is boolean
        if (controlSchemaActual.has("additionalProperties") &&
            !(controlSchemaActual.get("additionalProperties") instanceof Boolean)) {
            throw new AssertionError("PUA additionalProperties field does not contains a boolean value");
        }
        // Checking that #management object is present and at the correct position
        if (controlSchemaActual.has("patternProperties")) {
            JSONObject patternProperties = controlSchemaActual.getJSONObject("patternProperties");
            if (patternProperties.has(MANAGEMENTCONTROL)) {
                JSONAssert.assertEquals(new JSONObject(), patternProperties.getJSONObject(MANAGEMENTCONTROL),
                    JSONCompareMode.STRICT);

                // Check that #management is not in both header and 'properties' object
                JSONObject properties = controlSchemaActual.getJSONObject(PROPERTIES);
                if (properties.has(MANAGEMENTCONTROL)) {
                    throw new AssertionError("Can't have both '#management' key in header and in 'properties' object");
                }
            }
        } else {
            JSONObject properties = controlSchemaActual.getJSONObject(PROPERTIES);
            if (!properties.has(MANAGEMENTCONTROL)) {
                throw new AssertionError("Missing '#management' key in 'properties' object");
            }
            // #HAVEFUN
        }
        }
        else{
                if (!controlSchemaActual.has(SCHEMA)) {
                    throw new AssertionError("Missing '$schema' key in controlSchema' object");
                }
            }
    }

    public JSONObject getDefinitionsFromExpectedProfile() {

        JSONObject baseProfile = getProfileJsonExpected(true);
        String controlSchema = baseProfile.get(CONTROLSCHEMA).toString();
        LOGGER.error(controlSchema + " control shema");
        JSONObject controlSchemaAsJSON = new JSONObject(controlSchema);

        return controlSchemaAsJSON.getJSONObject(DEFINITIONS);
    }

    /**
     * Finds the seda type of a element based on his name
     *
     * @param elementName the name of the element to search on the archiveUnitSeda.json file
     * @return the seda type of an element
     */
    private String getPUAMetadataType(String elementName) throws IOException {
        SedaNode sedaElement = getSedaMetadata(elementName);
        return sedaElement != null ?
            resolvePuaType(sedaElement) :
            "undefined";
    }

    /**
     * Resolve the Pua element type based on VITAM given rules
     *
     * @param sedaElement the seda element type of the metadata
     * @return The type of a pua element
     */
    private String resolvePuaType(SedaNode sedaElement) {
        String sedaType = sedaElement.getType();
        String sedaElementType = sedaElement.getElement();
        String sedaName = sedaElement.getName();
        String sedaCardinality = sedaElement.getCardinality();


        if (sedaElementType.equals("Simple") &&
            (sedaCardinality.equals("0-1") || sedaCardinality.equals("1"))) {
            return "string";
        }
        if ((sedaElement.getElement().equals(COMPLEX) &&
            (sedaCardinality.equals("0-1") || sedaCardinality.equals("1"))) || sedaName.equals("Title") ||
            sedaName.equals("Description")) {
            return "object";
        }
        if (sedaType.equals("boolean") && (sedaCardinality.equals("0-1") || sedaCardinality.equals("1"))) {
            return "boolean";
        }
        if (sedaCardinality.equals("1-N") || sedaCardinality.equals("0-N")) {
            return "array";
        }
        return "undefined";
    }

    /**
     * Find and create a single JSONObject based on a given ElementProperties tree if it contains a Management metadata
     *
     * @param elementProperties an ElementProperties object containing Management as root element
     * @return a JSONObject containing a PUA representation of a Management metadata
     */
    private JSONObject getJSONObjectFromMetadata(ElementProperties elementProperties) throws IOException {

        JSONObject puaJSONObject = new JSONObject();
        SedaNode sedaElement = getSedaMetadata(MANAGEMENT);

        ElementProperties elementFound = elementProperties.flattened()
            .filter(childName -> childName.getName().equals(MANAGEMENT)
                && childName.getType().equals("element")).findAny().orElse(null);

        if (elementFound != null && sedaElement != null) {
            PuaMetadataDetails puaMetadataDetails = new PuaMetadataDetails();
            // get pua type
            puaMetadataDetails.setType(resolvePuaType(sedaElement));
            puaMetadataDetails.setDescription((elementFound.getDocumentation()));
            // Create a Map<PuaElementName,PuaElementDetails>
            Map<String, PuaMetadataDetails> puaMap = new HashMap<>();
            puaMap.put(MANAGEMENT, puaMetadataDetails);
            puaJSONObject.put(PROPERTIES, puaMap);
            return puaJSONObject;
        }
        return puaJSONObject;

    }

    /**
     * Recursively generates a tree of JSON objects based on a given ElementProperties object type
     *
     * @param elementsFromTree an ElementProperties List
     * @return a JSONArray representing all PUA elements of an ArchiveUnitProfile and its siblings
     */
    public JSONArray getJSONObjectFromAllTree(List<ElementProperties> elementsFromTree) {

        JSONArray jsonArray = sortedJSONArray();
        List<String> rulesToIgnore = Arrays.asList("StorageRule", "AppraisalRule", "AccessRule", "DisseminationRule",
            "ReuseRule", "ClassificationRule");

        for (ElementProperties el : elementsFromTree) {
            try {
                if (el.getName().equals(MANAGEMENT)) {
                    JSONObject management = getJSONFromManagement(el);
                    jsonArray.put(management);
                }
                if (jsonArray.length() > 0 &&
                    jsonArray.toString().contains(el.getName())) {
                    ElementProperties element = getElementById(elementsFromTree, el.getParentId());
                    if (element != null && element.getName().equals(CONTENT)) {
                        JSONObject notManagementMapElement = getJSONObjectFromElement(el);
                        jsonArray.put(notManagementMapElement);
                    }
                } else if (!rulesToIgnore.contains(el.getName()) && !el.getName().equals(CONTENT) &&
                    !el.getName().equals(MANAGEMENT)) {
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
     * @return a JSONArray representing all PUA elements of an Management metadata and its specific rules
     */
    public JSONObject getJSONFromManagement(ElementProperties element) throws IOException {
        List<String> rulesMetadata =
            Arrays.asList("StorageRule", "AppraisalRule", "AccessRule", "DisseminationRule", "ReuseRule",
                "ClassificationRule");
        List<String> childrenToEncapsulate = Arrays.asList("Rule", "StartDate");
        List<String> rulesFound = new ArrayList<>();

        JSONObject pua = sortedJSON();
        if (element.getChildren().isEmpty()) {
            return retrieveAccumulatedJsonManagementProperties(pua);
        }

        // JSON Object representing all PUA elements of an Management metadata and its specific rules
        retrieveAccumalatedJsonManagaementProperties(element, rulesMetadata, childrenToEncapsulate, rulesFound, pua);
        return retrieveAccumulatedJsonManagementProperties(pua);
    }

    /**
     * 1. Check special cases
     * 2. If special cases have children, encapsulate them into "Rules : { items : {childName : { ..."
     * 3. Once the children of special cases are processed, we put them into Rules -> items
     * 4. Convert to jsonobject via map and update its property
     * 5. Retrieve parent properties and add more elements to root element properties
     *
     * @param element
     * @param rulesMetadata
     * @param childrenToEncapsulate
     * @param rulesFound
     * @param pua
     * @throws IOException
     */
    private void retrieveAccumalatedJsonManagaementProperties(ElementProperties element, List<String> rulesMetadata,
        List<String> childrenToEncapsulate, List<String> rulesFound, JSONObject pua) throws IOException {
        for (ElementProperties childElement : element.getChildren()) {
            JSONObject childrenOfRule = sortedJSON();
            JSONObject grandChildrenOfRule = sortedJSON();
            JSONObject propertiesRules = sortedJSON();

            PuaMetadataDetails ruleTypeMetadataDetails = new PuaMetadataDetails();
            PuaMetadataDetails nonSpecialChildOfRuleDetails = new PuaMetadataDetails();
            SedaNode sedaElement = getSedaMetadata(childElement.getName());

            // 1. Check special cases
            if (checkSpecialCases(rulesMetadata, rulesFound, childElement, sedaElement))
                continue;
            getMetaDataFromSeda(childElement, ruleTypeMetadataDetails, sedaElement);
            Map<String, PuaMetadataDetails> ruleTypeMetadataMap = new HashMap<>();
            Map<String, PuaMetadataDetails> nonSpecialChildOfRule = new HashMap<>();

            List<String> requiredNonSpecialChildren = new ArrayList<>();
            List<String> requiredChildren = new ArrayList<>();
            // 2. If special cases have children, encapsulate them into "Rules : { items : {childName : { ..."
            if (!childElement.getChildren().isEmpty()) {
                for (ElementProperties grandChild : childElement.getChildren()) {
                    SedaNode node = getSedaMetadata(grandChild.getName());
                    if (childrenToEncapsulate.contains(grandChild.getName())) {
                        childrenContainsGrandChildName(grandChildrenOfRule, ruleTypeMetadataDetails, requiredChildren,
                            grandChild, node);
                    } else {
                        getMetaDataFromSeda(grandChild, nonSpecialChildOfRuleDetails, node);
                        nonSpecialChildOfRule.put(grandChild.getName(), nonSpecialChildOfRuleDetails);
                        //Required field
                        requiredNonSpecialChildren.add(grandChild.getName());
                    }
                    ruleTypeMetadataMap.put(childElement.getName(), ruleTypeMetadataDetails);
                }
            }
            // 3. Once the children of special cases are processed, we put them into Rules -> items
            putChildrenIntoRules(childrenOfRule, grandChildrenOfRule, propertiesRules, requiredChildren);

            // 4. Convert to jsonobject via map and update its property
            JSONObject ruleTypeMetadata = new JSONObject(ruleTypeMetadataMap);
            ruleTypeMetadata.getJSONObject(childElement.getName()).put(PROPERTIES, propertiesRules);
            putRequiredNonSpecialChildren(childElement, requiredNonSpecialChildren, ruleTypeMetadata);
            nonSpecialChildOfRule.keySet().forEach(e -> {
                Object details = nonSpecialChildOfRule.get(e);
                ruleTypeMetadata.getJSONObject(childElement.getName()).getJSONObject(PROPERTIES)
                    .put(e, details);
            });
            // 5. We retrieve parent properties and add more elements to root element properties
            pua.accumulate(PROPERTIES, ruleTypeMetadata.toMap());
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
    private boolean checkSpecialCases(List<String> rulesMetadata, List<String> rulesFound,
        ElementProperties childElement, SedaNode sedaElement) {
        if (!rulesMetadata.contains(childElement.getName()) || sedaElement == null) {
            return true;
        }

        if (childElement.getCardinality().equals("1") && sedaElement.getElement().equals(COMPLEX))
            rulesFound.add(childElement.getName());
        return false;
    }

    /**
     * put Required Non Special Children
     *
     * @param childElement
     * @param requiredNonSpecialChildren
     * @param ruleTypeMetadata
     */
    private void putRequiredNonSpecialChildren(ElementProperties childElement, List<String> requiredNonSpecialChildren,
        JSONObject ruleTypeMetadata) {
        if (!requiredNonSpecialChildren.isEmpty()) {
            ruleTypeMetadata.getJSONObject(childElement.getName())
                .put(REQUIRED, requiredNonSpecialChildren);
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
            JSONObject propretyOfItems = new JSONObject().put(PROPERTIES, grandChildrenOfRule);
            propretyOfItems.put(REQUIRED, requiredChildren);
            childrenOfRule.put("items", propretyOfItems);
            propertiesRules.put("Rules", childrenOfRule);
        }
    }

    /**
     * If children contrain grand child name
     *
     * @param grandChildrenOfRule
     * @param ruleTypeMetadataDetails
     * @param requiredChildren
     * @param grandChild
     * @param node
     * @throws JsonProcessingException
     */
    private void childrenContainsGrandChildName(JSONObject grandChildrenOfRule,
        PuaMetadataDetails ruleTypeMetadataDetails, List<String> requiredChildren, ElementProperties grandChild,
        SedaNode node) throws JsonProcessingException {
        PuaMetadataDetails childOfRuleDetails = new PuaMetadataDetails();
        getMetaDataFromSeda(grandChild, childOfRuleDetails, node);
        if (grandChild.getCardinality().equals("0-1") ||
            grandChild.getCardinality().equals("1"))
            requiredChildren.add(grandChild.getName());
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JSONObject childProperties =
            new JSONObject(mapper.writeValueAsString(childOfRuleDetails));
        grandChildrenOfRule.put(grandChild.getName(), childProperties);
        ruleTypeMetadataDetails.setProperties(grandChildrenOfRule);
    }

    /**
     * Retrieve the accumulated JSONArray properties from pua
     *
     * @param pua is JSONObjet that contains Management section of PUA
     * Convert it into a JSONObject and put it into a #mangagement key
     */
    public JSONObject retrieveAccumulatedJsonManagementProperties(JSONObject pua) {

        JSONObject managementAsJSONObject = new JSONObject();
        if (pua.keySet().contains(PROPERTIES)) {
            JSONObject properties = pua;
            try {
                JSONArray accumulatedProperties = pua.getJSONArray(PROPERTIES);
                String propertiesAsString = accumulatedProperties.toString()
                    .substring(1, accumulatedProperties.toString().length() - 1)
                    .replaceAll("(},\\{)", ",");
                properties = new JSONObject(propertiesAsString);
                JSONObject propertiesRequiredJson = new JSONObject();
                propertiesRequiredJson.put(PROPERTIES, properties);
                if (pua.keySet().contains(REQUIRED))
                    propertiesRequiredJson.put(REQUIRED, pua.getJSONArray(REQUIRED));
                managementAsJSONObject.put(MANAGEMENTCONTROL, propertiesRequiredJson);
            } catch (JSONException e) {
                LOGGER.debug(e.getMessage());
                managementAsJSONObject.put(MANAGEMENTCONTROL, pua);
            }
        } else {
            managementAsJSONObject.put(MANAGEMENTCONTROL, new JSONObject());
        }
        return managementAsJSONObject;
    }

    /**
     * Recursively searches for a seda node metadata based on a name
     *
     * @param elementName the name of the seda element to be found
     * @return a SedaNode object representation of a given seda element
     */

    private SedaNode getSedaMetadata(String elementName) throws IOException {
        SedaNode sedaTree = getArchiveUnitSeda();

        return sedaTree.flattened()
            .filter(childName -> childName.getName().equals(elementName)).findAny().orElse(null);
    }

    /**
     * Order a JSONObject
     *
     * @return an ordered JSONObject
     */
    public JSONObject sortedJSON() {
        JSONObject jsonObj = new JSONObject();
        try {
            Field changeMap = jsonObj.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonObj, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.debug(e.getMessage());
        }
        return jsonObj;
    }

    /**
     * Checks if an object of type ElementProperties contains, and its children, contains a Management
     *
     * @return true if an given ElementProperties object contains a Management metadata
     */
    public boolean containsManagement(ElementProperties elementProperties) throws IOException {
        return getJSONObjectFromMetadata(elementProperties).length() > 0;
    }

    /**
     * Recursively converts an ElementProperty tree and its children, into a Map
     *
     * @return a HashMap containing a tree of Pua metadata and its children
     */
    public JSONObject getJSONObjectFromElement(ElementProperties elementProperties)
        throws IOException {
        SedaNode sedaElement = getSedaMetadata(elementProperties.getName());
        PuaMetadataDetails puaMetadataDetails = new PuaMetadataDetails();
        getMetaDataFromSeda(elementProperties, puaMetadataDetails, sedaElement);
        if (!elementProperties.getChildren().isEmpty() && !getRequiredProperties(elementProperties).isEmpty()) {
            puaMetadataDetails.setRequired(getRequiredProperties(elementProperties));
        }
        JSONObject json = new JSONObject();
        json.put(elementProperties.getName(), new JSONObject(puaMetadataDetails.serialiseString()));
        if (!elementProperties.getChildren().isEmpty()) {
            json.getJSONObject(elementProperties.getName()).put(PROPERTIES, new JSONObject());
            getJSONObjectFromElement(elementProperties,
                json.getJSONObject(elementProperties.getName()).getJSONObject(PROPERTIES));
        }
        return json;
    }

    public void getJSONObjectFromElement(ElementProperties elementProperties, JSONObject json)
        throws IOException {
        if (!elementProperties.getChildren().isEmpty()) {
            for (ElementProperties el : elementProperties.getChildren()) {
                PuaMetadataDetails puaMetadataDetails = new PuaMetadataDetails();
                puaMetadataDetails.setType(getPUAMetadataType(el.getName()));
                puaMetadataDetails.setDescription(el.getDocumentation());
                json.put(el.getName(), new JSONObject(puaMetadataDetails));
                if (!el.getChildren().isEmpty()) {
                    json.getJSONObject(el.getName()).put(PROPERTIES, new JSONObject());
                    getJSONObjectFromElement(el, json.getJSONObject(el.getName()).getJSONObject(PROPERTIES));
                }
            }
        }
    }

    public List<String> getRequiredProperties(ElementProperties elementProperties) {
        List<String> listRequired = new ArrayList<>();
        elementProperties.getChildren().forEach(child -> {
            try {
                SedaNode sedaElement = getSedaMetadata(child.getName());
                if ((child.getCardinality().equals("1-N") && sedaElement.getCardinality().equals("0-N"))
                    || (child.getCardinality().equals("1") && !sedaElement.getCardinality().equals("1"))
                    || sedaElement.getCardinality().equals("1"))
                    listRequired.add(child.getName());
            } catch (IOException e) {
                LOGGER.debug(e.getMessage());
            }
        });
        return listRequired;
    }

    public List<ElementProperties> ignoreMetadata(ElementProperties elementProperties) {
        List<String> metadataToIgnore = Arrays.asList("DescriptiveMetadata", "ArchiveUnit");
        return elementProperties.flattened()
            .filter(child -> !metadataToIgnore.contains(child.getName())
                && child.getType().equals("element")).collect(toList());
    }

    public List<String> getHeadRequired(List<ElementProperties> elementsFromTree) {
        List<String> list = new ArrayList<>();
        elementsFromTree.forEach(element -> {
            try {
                SedaNode sedaElement = getSedaMetadata(element.getName());
                ElementProperties parent = getElementById(elementsFromTree, element.getParentId());
                if ((parent != null
                    && (parent.getName().equals(CONTENT) || element.getName().equals("ArchiveUnitProfile")))
                    && ((element.getCardinality().equals("1-N") && sedaElement.getCardinality().equals("0-N"))
                    || (element.getCardinality().equals("1") && !sedaElement.getCardinality().equals("1"))
                    || sedaElement.getCardinality().equals("1"))) {
                    list.add(element.getName());
                }
            } catch (IOException e) {
                LOGGER.debug(e.getMessage());
            }
        });
        return list;
    }

    public void getMetaDataFromSeda(ElementProperties el, PuaMetadataDetails puaMetadataDetails, SedaNode sedaElement) {
        // get pua type
        puaMetadataDetails.setType(resolvePuaType(sedaElement));
        puaMetadataDetails.setDescription((el.getDocumentation()));
        if (sedaElement.getElement().equals(COMPLEX)
            && el.getPuaData() != null
            && el.getPuaData().getAdditionalProperties() != null
        ) {
            puaMetadataDetails.setAdditionalProperties(el.getPuaData().getAdditionalProperties());
        }
        if ((el.getCardinality() != null &&
            el.getCardinality().equals("0-1") && sedaElement.getCardinality().equals("0-N"))) {
            puaMetadataDetails.setMinItems(0);
            puaMetadataDetails.setMaxItems(1);
        }
        if (!sedaElement.getElement().equals(COMPLEX) && el.getPuaData() != null &&
            el.getPuaData().getPattern() != null) {
            puaMetadataDetails.setPattern(el.getPuaData().getPattern());
        }
        if (el.getPuaData() != null && el.getPuaData().getEnum() != null) {
            puaMetadataDetails.setEnums(el.getPuaData().getEnum());
        } else {
            if (!sedaElement.getEnumeration().isEmpty() && el.getValue() == null) {
                puaMetadataDetails.setEnums(sedaElement.getEnumeration());
            }
            if (el.getValue() != null) {
                ArrayList<String> list = new ArrayList<>();
                list.add(el.getValue());
                puaMetadataDetails.setEnums(list);
            }
        }

    }

    public ElementProperties getElementById(List<ElementProperties> elementProperties, Long id) {

        for (ElementProperties el : elementProperties) {
            Long elementId = el.getId();
            if (elementId != null && elementId.equals(id))
                return el;
        }
        return null;
    }

    /**
     * Order a JSONOArray
     *
     * @return an ordered JSONArray
     */
    public JSONArray sortedJSONArray() {
        JSONArray jsonArray = new JSONArray();
        try {
            Field changeMap = jsonArray.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonArray, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.debug(e.getMessage());
        }
        return jsonArray;
    }
}
