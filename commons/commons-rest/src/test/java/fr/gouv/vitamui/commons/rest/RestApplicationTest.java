package fr.gouv.vitamui.commons.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests if the context is successfully loaded
 */

@SpringBootTest
public class RestApplicationTest {

    @Autowired
    RestTestApplicationConfiguration applicationConfiguration;

    @Test
    public void testContextLoads() {
        assertThat(applicationConfiguration).isNotNull();
    }
}
