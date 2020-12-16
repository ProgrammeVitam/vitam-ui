package fr.gouv.vitamui.ingest.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {"spring.config.name=ui-ingest-application"})
public class IngestApplicationPropertiesTest {

    @Autowired
    private IngestApplicationProperties ingestApplicationProperties;

    @Test
    public void testApplicationProperties() {
        assertNotNull(ingestApplicationProperties);
        assertNotNull(ingestApplicationProperties.getLimitPagination());
        assertNotNull(ingestApplicationProperties.getPrefix());
        assertEquals(ingestApplicationProperties.getPrefix(), "ingest-api");
    }
}
