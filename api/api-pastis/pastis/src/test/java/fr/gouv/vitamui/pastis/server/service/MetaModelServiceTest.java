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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileType;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileVersion;
import fr.gouv.vitamui.pastis.common.dto.seda.SedaNode;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@ExtendWith(MockitoExtension.class)
class MetaModelServiceTest {

    @InjectMocks
    MetaModelService metaModelService;

    @Mock
    ExternalSchemaService externalSchemaService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnCorrectMetaModelForPAWhenNoExternalSchema() throws Exception {
        // Return empty schema on calling
        doReturn(new ArrayList<>()).when(externalSchemaService).getExternalSchemaModels();
        // Call service
        SedaNode sedaNode = metaModelService.getMetaModelForVersion(ProfileVersion.VERSION_2_2, ProfileType.PA);
        JSONObject actual = new JSONObject(objectMapper.writeValueAsString(sedaNode));
        InputStream inputStreamExpected = getClass()
            .getClassLoader()
            .getResourceAsStream("metamodel/expected_pa_2.2.json");
        JSONObject expected = new JSONObject(new JSONTokener(inputStreamExpected));
        assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void shouldReturnCorrectMetaModelForPUAWhenNoExternalSchema() throws Exception {
        // Return empty schema on calling
        doReturn(new ArrayList<>()).when(externalSchemaService).getExternalSchemaModels();
        // Call service
        SedaNode sedaNode = metaModelService.getArchiveUnitMetaModelForVersion(
            ProfileVersion.VERSION_2_3,
            ProfileType.PUA
        );
        JSONObject actual = new JSONObject(objectMapper.writeValueAsString(sedaNode));
        InputStream inputStreamExpected = getClass()
            .getClassLoader()
            .getResourceAsStream("metamodel/expected_pua_2.3.json");
        JSONObject expected = new JSONObject(new JSONTokener(inputStreamExpected));
        assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void shouldReturnCorrectMetaModelForPAWhenExternalSchema() throws Exception {
        // Return empty schema on calling
        List<SchemaResponse> schemaResponses = getSchemaResponses("metamodel/external_schema.json");
        doReturn(schemaResponses).when(externalSchemaService).getExternalSchemaModels();
        // Call service
        SedaNode sedaNode = metaModelService.getMetaModelForVersion(ProfileVersion.VERSION_2_2, ProfileType.PA);
        JSONObject actual = new JSONObject(objectMapper.writeValueAsString(sedaNode));
        InputStream inputStreamExpected = getClass()
            .getClassLoader()
            .getResourceAsStream("metamodel/expected_pa_2.2_with_external_schema.json");
        JSONObject expected = new JSONObject(new JSONTokener(inputStreamExpected));
        assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void shouldReturnCorrectMetaModelForPUAWhenExternalSchema() throws Exception {
        // Return empty schema on calling
        List<SchemaResponse> schemaResponses = getSchemaResponses("metamodel/external_schema.json");
        doReturn(schemaResponses).when(externalSchemaService).getExternalSchemaModels();
        // Call service
        SedaNode sedaNode = metaModelService.getArchiveUnitMetaModelForVersion(
            ProfileVersion.VERSION_2_3,
            ProfileType.PUA
        );
        JSONObject actual = new JSONObject(objectMapper.writeValueAsString(sedaNode));
        InputStream inputStreamExpected = getClass()
            .getClassLoader()
            .getResourceAsStream("metamodel/expected_pua_2.3_with_external_schema.json");
        JSONObject expected = new JSONObject(new JSONTokener(inputStreamExpected));
        assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    private List<SchemaResponse> getSchemaResponses(String path) throws IOException {
        return objectMapper.readValue(
            getClass().getClassLoader().getResourceAsStream(path),
            objectMapper.getTypeFactory().constructCollectionType(List.class, SchemaResponse.class)
        );
    }
}
