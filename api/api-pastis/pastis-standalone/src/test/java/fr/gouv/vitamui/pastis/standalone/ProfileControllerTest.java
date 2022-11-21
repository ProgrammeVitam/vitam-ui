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

package fr.gouv.vitamui.pastis.standalone;

import fr.gouv.vitamui.pastis.common.rest.RestApi;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class ProfileControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    private MockHttpServletResponse mockHttpServletResponse;

    @MockBean
    private ApiPastisStandaloneApplication apiPastisStandaloneApplication;

    @Test
    public void getProfiles() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Tenant-Id", "1");
        RequestBuilder requestBuilder =
            MockMvcRequestBuilders.get(RestApi.PASTIS_GET_ALL_PROFILES).header("X-Tenant-Id", "1");
        mockHttpServletResponse = mockMvc.perform(requestBuilder).andReturn().getResponse();
        String resBody = mockHttpServletResponse.getContentAsString();
        LOGGER.info("response = {}", resBody);
        Assertions.assertEquals(200, mockHttpServletResponse.getStatus());
    }

    @Test
    public void getFile() throws Exception {
        RequestBuilder requestBuilder =
            MockMvcRequestBuilders.get(RestApi.PASTIS_GET_PROFILE_FILE).param("name", "PA_Exemple")
                .header("X-Tenant-Id", "1");
        mockHttpServletResponse = mockMvc.perform(requestBuilder).andReturn().getResponse();
        String resBody = mockHttpServletResponse.getContentAsString();
        LOGGER.info("response = {}", resBody);
        Assertions.assertEquals(200, mockHttpServletResponse.getStatus());
    }

}
