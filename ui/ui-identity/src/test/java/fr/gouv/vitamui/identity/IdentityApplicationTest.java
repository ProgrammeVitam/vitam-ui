package fr.gouv.vitamui.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import fr.gouv.vitamui.identity.config.IdentityApplicationProperties;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IdentityApplicationTest {

    @Autowired
    private Environment env;

    @Autowired
    IdentityApplicationProperties identityProperties;

    @MockBean
    BuildProperties buildProperties;

    @Test
    public void testContextLoads() {
        assertThat(env).isNotNull();

        assertThat(identityProperties).isNotNull();
        assertThat(identityProperties.getIamExternalClient()).isNotNull();
    }

}
