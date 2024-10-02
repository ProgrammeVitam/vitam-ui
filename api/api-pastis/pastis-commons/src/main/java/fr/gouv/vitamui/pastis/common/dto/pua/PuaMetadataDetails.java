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

package fr.gouv.vitamui.pastis.common.dto.pua;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import lombok.Data;
import org.json.JSONObject;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PuaMetadataDetails {

    String type;
    String description;
    Integer minItems;
    Integer maxItems;
    Boolean additionalProperties;
    JSONObject properties;
    List<String> required;
    PuaMetadata items;

    @JsonProperty("enum")
    List<String> enums;

    String pattern;

    public String serialiseString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new AfterburnerModule());
        String result = mapper.writeValueAsString(this);
        // This whole method of generating the JSON Schema should really be completely rewritten. This is unreadable
        // and not maintainable. Here we manage the correct generation of the "array" type having enum or pattern...
        if ("array".equals(type) && (pattern != null || (enums != null && !enums.isEmpty()))) {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject items = new JSONObject();
            jsonObject.put("items", items);
            items.put("type", "string");
            if (enums != null && !enums.isEmpty()) {
                items.put("enum", jsonObject.getJSONArray("enum"));
                jsonObject.remove("enum");
            }
            if (pattern != null) {
                items.put("pattern", jsonObject.getString("pattern"));
                jsonObject.remove("pattern");
            }
            result = jsonObject.toString();
        }
        return result;
    }
}
