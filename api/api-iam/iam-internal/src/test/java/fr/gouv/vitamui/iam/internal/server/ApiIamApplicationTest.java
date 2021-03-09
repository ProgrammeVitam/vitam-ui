package fr.gouv.vitamui.iam.internal.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@TestPropertySource(properties = { "spring.config.name=iam-internal-application" })
@ActiveProfiles("test")
public class ApiIamApplicationTest {

    @Autowired
    private Environment env;

    @BeforeClass
    public static void init() {
        System.setProperty("vitam.config.folder", "src/main/config");
    }

    @Test
    public void testContextLoads() {
        assertThat(env).isNotNull();
        assertThat(env.getProperty("spring.config.name")).isEqualTo("iam-internal-application");
    }

}
