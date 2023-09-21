/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.util.NoticeUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/application-test.yml")
public class JsonFromPuaTest {

    final JsonFromPua jsonFromPua = new JsonFromPua();

    @Test
    public void testImportOK() throws IOException {
        InputStream inputStreamPua = getClass().getClassLoader().getResourceAsStream("pua/pua_OK.json");
        JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStreamPua));
        JSONObject profileJson = new JSONObject(tokener);
        ElementProperties profileActual = jsonFromPua.toElementProperties(profileJson);
        ObjectMapper mapper = new ObjectMapper();
        String fileNodeActual = mapper.writeValueAsString(profileActual);
        JSONObject fileNodeJSONActual = new JSONObject(fileNodeActual);
        InputStream inputStreamExpected = getClass().getClassLoader().getResourceAsStream("pua/profile_Expected.json");
        tokener = new JSONTokener(inputStreamExpected);
        JSONObject fileNodeJSONExpected = new JSONObject(tokener);
        JSONAssert.assertEquals(fileNodeJSONActual, fileNodeJSONExpected, JSONCompareMode.STRICT);
    }

    @Test
    public void testImportOK_with_management() throws IOException {
        InputStream inputStreamPua = getClass().getClassLoader().getResourceAsStream("pua/pua_OK_with_management.json");
        JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStreamPua));
        JSONObject profileJson = new JSONObject(tokener);
        ElementProperties profileActual = jsonFromPua.toElementProperties(profileJson);
        ObjectMapper mapper = new ObjectMapper();
        String fileNodeActual = mapper.writeValueAsString(profileActual);
        JSONObject fileNodeJSONActual = new JSONObject(fileNodeActual);
        InputStream inputStreamExpected =
            getClass().getClassLoader().getResourceAsStream("pua/profile_Expected_with_management.json");
        tokener = new JSONTokener(inputStreamExpected);
        JSONObject fileNodeJSONExpected = new JSONObject(tokener);
        JSONAssert.assertEquals(fileNodeJSONActual, fileNodeJSONExpected, JSONCompareMode.STRICT);
    }

    @Test
    public void testImportNOK_missing_definitions() throws IOException {
        InputStream inputStreamPua =
            getClass().getClassLoader().getResourceAsStream("pua/pua_NOK_missing_definitions.json");
        JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStreamPua));
        JSONObject profileJson = new JSONObject(tokener);
        ElementProperties profile = jsonFromPua.toElementProperties(profileJson);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(profile);
        NoticeUtils.getNoticeFromPUA(profileJson);
    }

    @Test
    public void testImportNOK_missing_management() throws IOException {
        InputStream inputStreamPua =
            getClass().getClassLoader().getResourceAsStream("pua/pua_NOK_missing_management.json");
        JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStreamPua));
        JSONObject profileJson = new JSONObject(tokener);
        ElementProperties profile = jsonFromPua.toElementProperties(profileJson);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(profile);
        NoticeUtils.getNoticeFromPUA(profileJson);
    }

    @Test
    public void testImportNOK_missing_properties() throws IOException {
        InputStream inputStreamPua =
            getClass().getClassLoader().getResourceAsStream("pua/pua_NOK_missing_properties.json");
        JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStreamPua));
        JSONObject profileJson = new JSONObject(tokener);
        ElementProperties profile = jsonFromPua.toElementProperties(profileJson);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(profile);
        NoticeUtils.getNoticeFromPUA(profileJson);
    }

    @Test
    public void testImportNOK_both_management_present() throws IOException {
        InputStream inputStreamPua =
            getClass().getClassLoader().getResourceAsStream("pua/pua_NOK_both_management_present.json");
        JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStreamPua));
        JSONObject profileJson = new JSONObject(tokener);
        ElementProperties profile = jsonFromPua.toElementProperties(profileJson);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(profile);
        NoticeUtils.getNoticeFromPUA(profileJson);
    }
}
