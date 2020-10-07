package fr.gouv.vitamui.commons.test.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.ws.rs.core.MediaType;

/**
 * Swagger JSON Generation.
 * Extend this class for generate the swagger json file without launching a full SpringBoot app.
 *
 */
public abstract class AbstractSwaggerJsonFileGenerationJunit5 {

    @Autowired
    protected MockMvc mockMvc;

    /**
     * Call to get and save the Swagger JSON file.
     * @throws Exception
     */
    protected void swaggerJsonExists() throws Exception {
        final String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get("/v2/api-docs").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Writer writer = new FileWriter(new File("target/generated-sources/swagger.json"));
        try (writer) {
            IOUtils.write(contentAsString, writer);
        }
    }
}
