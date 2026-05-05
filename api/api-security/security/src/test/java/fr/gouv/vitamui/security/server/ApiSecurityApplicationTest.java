package fr.gouv.vitamui.security.server;

import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ApiSecurityApplicationTest extends AbstractMongoTests {

    @Autowired
    private Environment env;

    @Test
    void testContextLoads() {
        assertThat(env).isNotNull();
    }
}
