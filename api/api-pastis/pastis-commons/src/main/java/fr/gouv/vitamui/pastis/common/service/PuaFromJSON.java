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

import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class PuaFromJSON {
    private static final String SCHEMA = "http://json-schema.org/draft-04/schema";
    private static final String TYPE = "object";

    private final PuaPastisValidator puaPastisValidator;

    @Autowired
    public PuaFromJSON(final PuaPastisValidator puaPastisValidator) {
        this.puaPastisValidator = puaPastisValidator;
    }

    public String getControlSchemaFromElementProperties(ElementProperties elementProperties) throws IOException {
        // We use a JSONObject instead of POJO, since Jackson and Gson will add unnecessary
        // backslashes during mapping string object values back to string
        final JSONObject controlSchema = puaPastisValidator.sortedJSON();
        // 1. Add Schema
        controlSchema.put("$schema", SCHEMA);
        // 2. Add  type
        controlSchema.put("type", TYPE);
        // 3. Add additionProperties
        controlSchema.put("additionalProperties", this.additionalProperties(elementProperties));
        // 4. Check if tree contains Management metadata
        addPatternPropertiesForManagement(elementProperties, controlSchema);
        final List<ElementProperties> elementsForTree = puaPastisValidator.ignoreMetadata(elementProperties);
        final List<String> requiredElements = puaPastisValidator.getHeadRequired(elementsForTree);
        if (CollectionUtils.isNotEmpty(requiredElements)) {
            controlSchema.put(PuaPastisValidator.REQUIRED, puaPastisValidator.getHeadRequired(elementsForTree));
        }

        // 5. Add definitions _ not used actually
          /*  JSONObject definitionsFromBasePua = puaPastisValidator.getDefinitionsFromExpectedProfile();
            controlSchema.put("definitions", definitionsFromBasePua);*/
        // 6. Add ArchiveUnitProfile and the rest of the tree

        final JSONArray allElements = puaPastisValidator.getJSONObjectFromAllTree(elementsForTree);
        final JSONObject sortedElements = getJSONObjectsFromJSonArray(allElements);
        controlSchema.put("properties", sortedElements);
        // 7. Remove excessive backslashes from mapping strings to objects and vice-versa
        return controlSchema.toString().replaceAll("\\\\+", "");
    }

    public String getDefinitions() {
        return puaPastisValidator.getDefinitionsFromExpectedProfile().toString();
    }

    private JSONObject getJSONObjectsFromJSonArray(JSONArray array) {
        JSONObject sortedJSONObject = puaPastisValidator.sortedJSON();
        for (Object o : array) {
            JSONObject jsonObject = (JSONObject) o;
            for (String key : jsonObject.keySet()) {
                sortedJSONObject.put(key, jsonObject.get(key));
            }
        }
        return sortedJSONObject;
    }

    private void addPatternPropertiesForManagement(ElementProperties elementProperties, JSONObject controlSchema) {
        final ElementProperties managementElementProperties = elementProperties.find("Management");

        // set when it's null ?
        if (Objects.isNull(managementElementProperties) || !managementElementProperties.getChildren().isEmpty()) {
            return;
        }

        final JSONObject management = new JSONObject().put("additionalProperties", this.additionalProperties(managementElementProperties));
        final JSONObject patternProperties = new JSONObject().put("#management", management);

        controlSchema.put("patternProperties", patternProperties);
    }

    /**
     * Compute a value for additionalProperties from an ElementProperties instance.
     *
     * @param elementProperties
     * @return
     */
    private boolean additionalProperties(final ElementProperties elementProperties) {
        if (elementProperties.getPuaData() == null) {
            return false;
        }
        if (elementProperties.getPuaData().getAdditionalProperties() == null) {
            return false;
        }

        return elementProperties.getPuaData().getAdditionalProperties() || elementProperties.isAdditionalProperties();
    }
}
