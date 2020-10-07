package fr.gouv.vitamui.commons.test.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;

/**
 * Swagger JSON Generation.
 * With this test class, we can generate the swagger json file without launching a full SpringBoot app.
 *
 */
@Deprecated
public abstract class AbstractSwaggerJsonFileGenerationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Before
    public void setup() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    /**
     * Call to get and save the Swagger JSON file.
     * @throws Exception
     */
    @Test
    public void swaggerJsonExists() throws Exception {
        final String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get("/v2/api-docs").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Writer writer = new FileWriter(new File("target/generated-sources/swagger.json"));
        try (writer) {
            IOUtils.write(contentAsString, writer);
        }
    }
}
