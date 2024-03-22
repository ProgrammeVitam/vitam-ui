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

package fr.gouv.vitamui.archives.search.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitamui.commons.api.dtos.JsonPatch;
import fr.gouv.vitamui.commons.api.dtos.JsonPatchDto;
import fr.gouv.vitamui.commons.api.dtos.MultiJsonPatchDto;
import fr.gouv.vitamui.commons.api.dtos.PatchCommand;
import fr.gouv.vitamui.commons.api.dtos.PatchOperation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiJsonPatchDtoTest {
    @Test
    public void testJsonPatchSerializationDeserialization() throws JsonProcessingException {
        // Création d'un exemple de MultiJsonPatchDto
        ObjectMapper objectMapper = new ObjectMapper();
        JsonPatch jsonPatch = new JsonPatch();
        jsonPatch.add(new PatchCommand().setOp(PatchOperation.ADD).setPath("/name")
            .setValue(objectMapper.readValue("{\"field\":\"John\"}", ObjectNode.class)));
        MultiJsonPatchDto multiJsonPatchDto = new MultiJsonPatchDto();
        multiJsonPatchDto.add(new JsonPatchDto().setId("1").setJsonPatch(new JsonPatch()));
        multiJsonPatchDto.add(new JsonPatchDto().setId("2").setJsonPatch(jsonPatch));

        // Conversion de l'objet en JSON
        String jsonString = objectMapper.writeValueAsString(multiJsonPatchDto);

        // Vérification de la sérialisation
        assertEquals(
            "[{\"id\":\"1\",\"jsonPatch\":[]},{\"id\":\"2\",\"jsonPatch\":[{\"op\":\"add\",\"path\":\"/name\",\"value\":{\"field\":\"John\"}}]}]",
            jsonString);

        // Conversion du JSON en objet MultiJsonPatchDto
        MultiJsonPatchDto deserializedDto = objectMapper.readValue(jsonString, MultiJsonPatchDto.class);

        // Vérification de la désérialisation
        assertEquals(2, deserializedDto.size());
        assertEquals("1", deserializedDto.get(0).getId());
        assertEquals(0, deserializedDto.get(0).getJsonPatch().size());
        assertEquals("2", deserializedDto.get(1).getId());
        assertEquals(1, deserializedDto.get(1).getJsonPatch().size());
    }

    @Test
    public void testMultiJsonPatchWithAddInstruction() throws JsonProcessingException {
        // Création d'un exemple de MultiJsonPatchDto avec une instruction ADD
        MultiJsonPatchDto multiJsonPatchDto = new MultiJsonPatchDto();
        JsonPatch jsonPatch = new JsonPatch();
        jsonPatch.add(new PatchCommand().setOp(PatchOperation.ADD).setPath("/name")
            .setValue(JsonNodeFactory.instance.textNode("bonjour")));
        multiJsonPatchDto.add(new JsonPatchDto().setId("1").setJsonPatch(jsonPatch));

        // Conversion de l'objet en JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(multiJsonPatchDto);

        // Vérification de la sérialisation
        String expectedJson =
            "[{\"id\":\"1\",\"jsonPatch\":[{\"op\":\"add\",\"path\":\"/name\",\"value\":\"bonjour\"}]}]";
        assertEquals(expectedJson, jsonString);

        // Conversion du JSON en objet MultiJsonPatchDto
        MultiJsonPatchDto deserializedDto = objectMapper.readValue(jsonString, MultiJsonPatchDto.class);

        // Vérification de la désérialisation
        assertEquals(1, deserializedDto.size());
        JsonPatch deserializedJsonPatch = deserializedDto.get(0).getJsonPatch();
        assertEquals(1, deserializedJsonPatch.size());
        PatchCommand deserializedPatchCommand = deserializedJsonPatch.get(0);
        assertEquals(PatchOperation.ADD, deserializedPatchCommand.getOp());
        assertEquals("/name", deserializedPatchCommand.getPath());
        assertEquals(JsonNodeFactory.instance.textNode("bonjour"), deserializedPatchCommand.getValue());
    }

}
