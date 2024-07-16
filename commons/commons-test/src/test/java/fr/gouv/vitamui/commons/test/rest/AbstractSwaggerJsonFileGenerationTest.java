package fr.gouv.vitamui.commons.test.rest;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.ws.rs.core.MediaType;
import java.io.FileWriter;
import java.io.Writer;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Swagger JSON Generation.
 * With this test class, we can generate the swagger json file without launching a full SpringBoot app.
 *
 */
@Deprecated
public abstract class AbstractSwaggerJsonFileGenerationTest {

    @Autowired
    protected MockMvc mockMvc;

    /**
     * Call to get and save the Swagger JSON file.
     * @throws Exception
     */
    @Test
    @Ignore //Voir Bug #8364 -- Commentaire à enlever après correction de ce bug
    public void swaggerJsonExists() throws Exception {
        final String contentAsString = mockMvc
            .perform(MockMvcRequestBuilders.get("/v2/api-docs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        System.out.println(contentAsString);

        Writer writer = new FileWriter("target/generated-sources/swagger.json");
        try (writer) {
            IOUtils.write(contentAsString, writer);
        }
    }
}
