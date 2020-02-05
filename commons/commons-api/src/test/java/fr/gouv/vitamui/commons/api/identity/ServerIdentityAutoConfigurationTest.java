package fr.gouv.vitamui.commons.api.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class ServerIdentityAutoConfigurationTest {

    ConfigurableApplicationContext context;

    /**
     * @throws java.lang.Exception
     *             Exception.
     */
    @Before
    public void setUp() throws Exception {
        System.clearProperty("spring.profiles.active");
    }

    @After
    public void tearDown() {
        SpringApplication.exit(context);
    }

    @Test
    public void configurationBeanExists() {
        context = new AnnotationConfigApplicationContext(PropertyPlaceholderAutoConfiguration.class,
                ServerIdentityAutoConfiguration.class);
        assertThat(context.getBeanNamesForType(ServerIdentityConfiguration.class)).hasSize(1);
    }

}
