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
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class PuaPastisValidator {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(PuaPastisValidator.class);

    private static JSONObject profileJsonExpected;

    private static SedaNode archiveUnitSeda;

    private JSONObject getProfileJsonExpected() {
        if (profileJsonExpected == null) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("pua_validation/valid_pua.json");

            assert inputStream != null;
            JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStream));
            profileJsonExpected = new JSONObject(tokener);
        }
        return profileJsonExpected;
    }

    private SedaNode getArchiveUnitSeda() throws IOException {
        if (archiveUnitSeda == null) {
            InputStream inputStream =
                getClass().getClassLoader().getResourceAsStream("pua_validation/archiveUnitSeda.json");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            archiveUnitSeda = objectMapper.readValue(inputStream, SedaNode.class);
        }
        return archiveUnitSeda;
    }

    /**
     * Validate a PUA JSON file against a template file
     * using LENIENT comparison mode
     *
     * @param pua The string containing the JSON file to be validated
     * @throws IOException
     * @throws AssertionError
     */
    public void validatePUA(JSONObject pua) throws IOException, AssertionError {
        JSONObject profileJsonExpected = getProfileJsonExpected();

        // Compare list of field at the root level
        Set<String> actualFieldList = pua.keySet().stream().collect(toSet());
        Set<String> expectedFieldList = profileJsonExpected.keySet().stream().collect(Collectors.toSet());
        if (!actualFieldList.equals(expectedFieldList)) {
            throw new AssertionError("PUA field list does not contains the expected values");
        }

        // Next tests are controlling the ControlSchema
        String controlSchemaString = pua.getString("controlSchema");
        JSONObject controlSchemaActual = new JSONObject(controlSchemaString);
        controlSchemaString = profileJsonExpected.getString("controlSchema");
        JSONObject controlSchemaExpected = new JSONObject(controlSchemaString);

        // Checking that the whole structure is respected. Doesn't care that the pua contains extended fields.
        JSONAssert.assertEquals(controlSchemaExpected, controlSchemaActual, JSONCompareMode.LENIENT);

        // Checking that the definitions list is exactly the same as expected
        JSONAssert.assertEquals(controlSchemaExpected.getJSONObject("definitions"),
            controlSchemaActual.getJSONObject("definitions"), JSONCompareMode.STRICT);

        // Checking that #management object is present and at the correct position
        if (controlSchemaActual.has("patternProperties")) {
            JSONObject patternProperties = controlSchemaActual.getJSONObject("patternProperties");
            if (patternProperties.has("#management")) {
                JSONAssert.assertEquals(new JSONObject(), patternProperties.getJSONObject("#management"),
                    JSONCompareMode.STRICT);

                // Check that #management is not in both header and 'properties' object
                JSONObject properties = controlSchemaActual.getJSONObject("properties");
                if (properties.has("#management")) {
                    throw new AssertionError("Can't have both '#management' key in header and in 'properties' object");
                }
            }
        } else {
            JSONObject properties = controlSchemaActual.getJSONObject("properties");
            if (!properties.has("#management")) {
                throw new AssertionError("Missing '#management' key in 'properties' object");
            }
            // TODO Verify #management rules structure
            // #HAVEFUN
        }
    }

    public JSONObject getDefinitionsFromExpectedProfile() {

        JSONObject baseProfile = getProfileJsonExpected();
        String controlSchema = baseProfile.get("controlSchema").toString();
        JSONObject controlSchemaAsJSON = new JSONObject(controlSchema);

        return controlSchemaAsJSON.getJSONObject("definitions");
    }

    /**
     * <p>Finds the seda type of a element based on his name</p>
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
     * <p>Resolve the Pua element type based on VITAM given rules</p>
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
        if ((sedaElement.getElement().equals("Complex") &&
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
     * <p>Find and create a single JSONObject based on a given ElementProperties tree if it contains a Management metadata</p>
     *
     * @param elementProperties an ElementProperties object containing Management as root element
     * @return a JSONObject containing a PUA representation of a Management metadata
     */
    private JSONObject getJSONObjectFromMetadata(ElementProperties elementProperties) throws IOException {

        JSONObject puaJSONObject = new JSONObject();
        SedaNode sedaElement = getSedaMetadata("Management");

        ElementProperties elementFound = elementProperties.flattened()
            .filter(childName -> childName.getName().equals("Management")
                && childName.getType().equals("element")).findAny().orElse(null);

        if (elementFound != null && sedaElement != null) {
            PuaMetadataDetails puaMetadataDetails = new PuaMetadataDetails();
            // get pua type;
            puaMetadataDetails.setType(resolvePuaType(sedaElement));
            puaMetadataDetails.setDescription((elementFound.getDocumentation()));
            // Create a Map<PuaElementName,PuaElementDetails>
            Map<String, PuaMetadataDetails> puaMap = new HashMap<>();
            puaMap.put("Management", puaMetadataDetails);
            puaJSONObject.put("properties", puaMap);
            return puaJSONObject;
        }
        return puaJSONObject;

    }

    /**
     * <p>Recursively generates a tree of JSON objects based on a given ElementProperties object type</p>
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
                if (el.getName().equals("Management")) {
                    JSONObject management = getJSONFromManagement(el);
                    jsonArray.put(management);
                }
                if (jsonArray.length() > 0 &&
                    jsonArray.toString().contains(el.getName())) {
                    ElementProperties element = getElementById(elementsFromTree, el.getParentId());
                    if (element != null && element.getName().equals("Content")) {
                        JSONObject notManagementMapElement = getJSONObjectFromElement(el);
                        jsonArray.put(notManagementMapElement);
                    } else {
                        continue;
                    }
                } else if (!rulesToIgnore.contains(el.getName()) && !el.getName().equals("Content") &&
                    !el.getName().equals("Management")) {
                    JSONObject notManagementMapElement = getJSONObjectFromElement(el);
                    jsonArray.put(notManagementMapElement);
                }

            } catch (IOException e) {
                LOGGER.info(e.getMessage());
            }
        }
        return jsonArray;
    }

    /**
     * <p>Recursively generates a tree of JSON objects starting from the Management metadata</p>
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

        JSONObject pua = sortedJSONObject();
        if (element.getChildren().size() > 0) {
            for (ElementProperties childElement : element.getChildren()) {
                JSONObject childrenOfRule = sortedJSONObject();
                JSONObject grandChildrenOfRule = sortedJSONObject();
                JSONObject propertiesRules = sortedJSONObject();
                // 1. Check special cases
                if (rulesMetadata.contains(childElement.getName())) {
                    PuaMetadataDetails ruleTypeMetadataDetails = new PuaMetadataDetails();
                    PuaMetadataDetails nonSpecialChildOfRuleDetails = new PuaMetadataDetails();
                    SedaNode sedaElement = getSedaMetadata(childElement.getName());
                    if (childElement.getCardinality().equals("1") && sedaElement.getElement().equals("Complex"))
                        rulesFound.add(childElement.getName());
                    getMetaDataFromSeda(childElement, ruleTypeMetadataDetails, sedaElement);
                    Map ruleTypeMetadataMap = new HashMap<String, PuaMetadataDetails>();
                    Map nonSpecialChildOfRule = new HashMap<String, PuaMetadataDetails>();

                    List<String> requiredNonSpecialChildren = new ArrayList<>();
                    List<String> requiredChildren = new ArrayList<>();
                    // 2. If special cases have children, encapsulate them into "Rules : { items : {childName : { ..."
                    if (childElement.getChildren().size() > 0) {
                        for (ElementProperties grandChild : childElement.getChildren()) {
                            SedaNode node = getSedaMetadata(grandChild.getName());
                            if (childrenToEncapsulate.contains(grandChild.getName())) {
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
                            } else {
                                getMetaDataFromSeda(grandChild, nonSpecialChildOfRuleDetails, node);
                                nonSpecialChildOfRule.put(grandChild.getName(), nonSpecialChildOfRuleDetails);
                                //Required field
                                requiredNonSpecialChildren.add(grandChild.getName());
                            }
                            ruleTypeMetadataMap.put(childElement.getName(), ruleTypeMetadataDetails);
                        }
                    }
                    // 2. Once the children of special cases are processed, we put them into Rules -> items
                    if (!grandChildrenOfRule.isEmpty()) {
                        JSONObject propretyOfItems = new JSONObject().put("properties", grandChildrenOfRule);
                        propretyOfItems.put("required", requiredChildren);
                        childrenOfRule.put("items", propretyOfItems);
                        propertiesRules.put("Rules", childrenOfRule);
                    }

                    // 3. Convert to jsonobject via map and update its property
                    JSONObject ruleTypeMetadata = new JSONObject(ruleTypeMetadataMap);
                    ruleTypeMetadata.getJSONObject(childElement.getName()).put("properties", propertiesRules);
                    if (!requiredNonSpecialChildren.isEmpty()) {
                        ruleTypeMetadata.getJSONObject(childElement.getName())
                            .put("required", requiredNonSpecialChildren);
                    }
                    nonSpecialChildOfRule.keySet().forEach(e -> {
                        Object details = nonSpecialChildOfRule.get(e);
                        ruleTypeMetadata.getJSONObject(childElement.getName()).getJSONObject("properties")
                            .put(e.toString(), details);
                    });
                    // 5. We retrieve parent properties and add more elements to root element properties
                    pua.accumulate("properties", ruleTypeMetadata.toMap());
                    if (!rulesFound.isEmpty())
                        pua.put("required", rulesFound);
                }
            }
        }
        return retrieveAccumulatedJsonManagementProperties(pua);
    }

    /**
     * Retrieve the accumulated JSONArray properties from pua
     *
     * @param pua is JSONObjet that contains Management section of PUA
     * Convert it into a JSONObject and put it into a #mangagement key
     */
    public JSONObject retrieveAccumulatedJsonManagementProperties(JSONObject pua) {

        JSONObject managementAsJSONObject = new JSONObject();
        if (pua.keySet().contains("properties")) {
            JSONObject properties = pua;
            try {
                JSONArray accumulatedProperties = pua.getJSONArray("properties");
                String propertiesAsString = accumulatedProperties.toString()
                    .substring(1, accumulatedProperties.toString().length() - 1)
                    .replaceAll("(},\\{)", ",");
                properties = new JSONObject(propertiesAsString);
                JSONObject propertiesRequiredJson = new JSONObject();
                propertiesRequiredJson.put("properties", properties);
                if (pua.keySet().contains("required"))
                    propertiesRequiredJson.put("required", pua.getJSONArray("required"));
                managementAsJSONObject.put("#management", propertiesRequiredJson);
            } catch (JSONException e) {
                LOGGER.info(e.getMessage());
                managementAsJSONObject.put("#management", pua);
            }
        } else {
            managementAsJSONObject.put("#management", new JSONObject());
        }
        return managementAsJSONObject;
    }

    /**
     * <p>Recursively searches for a seda node metadata based on a name</p>
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
     * <p>Order a JSONObject</p>
     *
     * @return an ordered JSONObject
     */
    public JSONObject sortedJSONObject() {
        JSONObject jsonObj = new JSONObject();
        try {
            Field changeMap = jsonObj.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonObj, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
        }
        return jsonObj;
    }

    /**
     * <p>Order a JSONOArray</p>
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
            LOGGER.info(e.getMessage());
        }
        return jsonArray;
    }

    /**
     * <p>Checks if an object of type ElementProperties contains, and its children, contains a Management</p>
     *
     * @return true if an given ElementProperties object contains a Management metadata
     */
    public boolean containsManagement(ElementProperties elementProperties) throws IOException {
        return getJSONObjectFromMetadata(elementProperties).length() > 0;
    }

    /**
     * <p>Recursively converts an ElementProperty tree and its children, into a Map</p>
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
        json.put(elementProperties.getName(), new JSONObject(puaMetadataDetails));
        if (!elementProperties.getChildren().isEmpty()) {
            json.getJSONObject(elementProperties.getName()).put("properties", new JSONObject());
            getJSONObjectFromElement(elementProperties,
                json.getJSONObject(elementProperties.getName()).getJSONObject("properties"));
        }
        return json;
    }

    public void getJSONObjectFromElement(ElementProperties elementProperties, JSONObject json)
        throws IOException {
        if (elementProperties.getChildren().size() > 0) {
            for (ElementProperties el : elementProperties.getChildren()) {
                PuaMetadataDetails puaMetadataDetails = new PuaMetadataDetails();
                puaMetadataDetails.setType(getPUAMetadataType(el.getName()));
                puaMetadataDetails.setDescription(el.getDocumentation());
                json.put(el.getName(), new JSONObject(puaMetadataDetails));
                if (!el.getChildren().isEmpty()) {
                    json.getJSONObject(el.getName()).put("properties", new JSONObject());
                    getJSONObjectFromElement(el, json.getJSONObject(el.getName()).getJSONObject("properties"));
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
                LOGGER.info(e.getMessage());
            }
        });
        return listRequired;
    }

    public List<ElementProperties> ignoreMetadata(ElementProperties elementProperties) {
        List<String> metadataToIgnore = Arrays.asList("DescriptiveMetadata", "ArchiveUnit");
        List<ElementProperties> elementsFromTree =
            elementProperties.flattened()
                .filter(child -> !metadataToIgnore.contains(child.getName())
                    && child.getType().equals("element")).collect(toList());
        return elementsFromTree;
    }

    public List<String> getHeadRequired(List<ElementProperties> elementsFromTree) {
        List<String> list = new ArrayList<>();
        elementsFromTree.forEach((element) -> {
            try {
                SedaNode sedaElement = getSedaMetadata(element.getName());
                ElementProperties parent = getElementById(elementsFromTree, element.getParentId());
                if ((parent != null &&
                    (parent.getName().equals("Content") || element.getName().equals("ArchiveUnitProfile")))) {
                    if ((element.getCardinality().equals("1-N") && sedaElement.getCardinality().equals("0-N"))
                        || (element.getCardinality().equals("1") && !sedaElement.getCardinality().equals("1"))
                        || sedaElement.getCardinality().equals("1")) {
                        list.add(element.getName());
                    }
                }
            } catch (IOException e) {
                LOGGER.info(e.getMessage());
            }
        });
        return list;
    }

    public void getMetaDataFromSeda(ElementProperties el, PuaMetadataDetails puaMetadataDetails, SedaNode sedaElement) {
        // get pua type;
        puaMetadataDetails.setType(resolvePuaType(sedaElement));
        puaMetadataDetails.setDescription((el.getDocumentation()));
        if (sedaElement.getElement().equals("Complex") && el.getPuaData() != null) {
            if (el.getPuaData().getAdditionalProperties() != null) {
                puaMetadataDetails.setAdditionalProperties(el.getPuaData().getAdditionalProperties());
            }
        }
        if ((el.getCardinality() != null &&
            el.getCardinality().equals("0-1") && sedaElement.getCardinality().equals("0-N"))) {
            puaMetadataDetails.setMinItems(0);
            puaMetadataDetails.setMaxItems(1);
        }
        if (!sedaElement.getEnumeration().isEmpty() && el.getValue() == null) {
            puaMetadataDetails.setEnums(sedaElement.getEnumeration());
        }
        if (el.getValue() != null) {
            ArrayList list = new ArrayList();
            list.add(el.getValue());
            puaMetadataDetails.setEnums(list);
        }

    }

    public ElementProperties getElementById(List<ElementProperties> elementProperties, Long id) {

        for (ElementProperties el : elementProperties) {
            if (el.getId() == id)
                return el;
        }
        return null;
    }
}
