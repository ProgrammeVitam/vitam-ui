package fr.gouv.vitamui.commons.rest;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.util.AbstractServerIdentityBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests if the context is successfully loaded
 *
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = StartRestTestApplication.class)
public class RestApplicationTest extends AbstractServerIdentityBuilder {

    @Autowired
    ServerIdentityConfiguration serverIdentityConfiguration;

    @Autowired
    RestTestApplicationConfiguration applicationConfiguration;

    @Test
    public void testContextLoads() {
        assertThat(serverIdentityConfiguration).isNotNull();
        assertThat(applicationConfiguration).isNotNull();
    }

}
