package fr.gouv.vitamui.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import fr.gouv.vitamui.identity.config.IdentityApplicationProperties;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public class IdentityApplicationTest {

    @Autowired
    private Environment env;

    @Autowired
    IdentityApplicationProperties identityProperties;

    @Test
    public void testContextLoads() {
        assertThat(env).isNotNull();
        assertThat(env.getProperty("spring.config.name") ).isEqualTo("ui-identity-application");

        assertThat(identityProperties).isNotNull();
        assertThat(identityProperties.getIamExternalClient()).isNotNull();
    }

}
