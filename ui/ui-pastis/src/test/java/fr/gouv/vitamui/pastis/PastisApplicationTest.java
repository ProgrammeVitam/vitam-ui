package fr.gouv.vitamui.pastis;

import fr.gouv.vitamui.pastis.config.PastisApplicationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PastisApplicationTest {

    @Autowired
    private Environment env;

    @Autowired
    PastisApplicationProperties pastisApplicationProperties;

    @MockBean
    BuildProperties buildProperties;

    @Test
    public void testContextLoad() {
        assertThat(env).isNotNull();

        assertThat(pastisApplicationProperties).isNotNull();
        assertThat(pastisApplicationProperties.getIamExternalClient()).isNotNull();
        assertThat(pastisApplicationProperties.getReferentialExternalClient()).isNotNull();
    }
}
