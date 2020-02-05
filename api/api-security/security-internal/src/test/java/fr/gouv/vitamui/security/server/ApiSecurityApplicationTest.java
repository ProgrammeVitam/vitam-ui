package fr.gouv.vitamui.security.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "spring.config.name=security-internal-application" })
@ActiveProfiles("test")
public class ApiSecurityApplicationTest {

    @Autowired
    private Environment env;

    @Test
    public void testContextLoads() {
        assertThat(env).isNotNull();
        assertThat(env.getProperty("spring.config.name") ).isEqualTo("security-internal-application");
    }

}
