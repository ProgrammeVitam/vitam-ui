package fr.gouv.vitamui.pastis.standalone;

import fr.gouv.vitamui.pastis.common.rest.RestApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ProfileControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileControllerTest.class);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockHttpServletResponse mockHttpServletResponse;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void getProfiles() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(RestApi.PASTIS_GET_ALL_PROFILES).header(
            "X-Tenant-Id",
            "1"
        );
        mockHttpServletResponse = mockMvc.perform(requestBuilder).andReturn().getResponse();
        String resBody = mockHttpServletResponse.getContentAsString();
        LOGGER.info("response = {}", resBody);
        Assertions.assertEquals(200, mockHttpServletResponse.getStatus());
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void getFile() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(RestApi.PASTIS_GET_PROFILE_FILE)
            .param("name", "PA_Exemple")
            .header("X-Tenant-Id", "1");
        mockHttpServletResponse = mockMvc.perform(requestBuilder).andReturn().getResponse();
        String resBody = mockHttpServletResponse.getContentAsString();
        LOGGER.info("response = {}", resBody);
        Assertions.assertEquals(200, mockHttpServletResponse.getStatus());
    }
}
