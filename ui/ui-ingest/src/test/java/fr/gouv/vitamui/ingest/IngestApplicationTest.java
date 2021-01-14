package fr.gouv.vitamui.ingest;

import fr.gouv.vitamui.ingest.config.IngestApplicationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = { "spring.config.name=ui-ingest-application" })
public class IngestApplicationTest {

    @Autowired
    IngestApplicationProperties ingestApplicationProperties;

    @Test
    public void testPropertiesLoading() {
        assertThat(ingestApplicationProperties).isNotNull();
    }

}
