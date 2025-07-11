package fr.gouv.vitamui.iam.server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = { "spring.config.name=iam-application" })
@ActiveProfiles("test")
public class ApiIamApplicationTest {

    @Autowired
    private Environment env;

    @BeforeAll
    public static void init() {
        System.setProperty("vitam.config.folder", "src/main/config");
    }

    @Test
    public void testContextLoads() {
        assertThat(env).isNotNull();
        assertThat(env.getProperty("spring.config.name")).isEqualTo("iam-application");
    }
}
