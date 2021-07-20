package fr.gouv.vitamui.portal.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PortalApplicationPropertiesTest {

    @Autowired
    private PortalApplicationProperties applicationProperties;

    @MockBean
    BuildProperties buildProperties;

    @Test
    public void testApplicationProperties() {
        assertThat(applicationProperties).isNotNull();
        assertThat(applicationProperties.getPrefix()).isNotNull();
    }
}
