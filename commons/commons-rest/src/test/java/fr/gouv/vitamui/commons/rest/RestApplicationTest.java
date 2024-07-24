package fr.gouv.vitamui.commons.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests if the context is successfully loaded
 */

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RestApplicationTest {

    @Autowired
    RestTestApplicationConfiguration applicationConfiguration;

    @Test
    public void testContextLoads() {
        assertThat(applicationConfiguration).isNotNull();
    }
}
