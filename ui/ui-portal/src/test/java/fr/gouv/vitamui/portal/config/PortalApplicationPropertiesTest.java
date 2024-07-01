package fr.gouv.vitamui.portal.config;

import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PortalApplicationPropertiesTest {

    @MockBean
    private CasLogoutUrl casLogoutUrl;

    @MockBean
    private BuildProperties buildProperties;

    @Autowired
    private PortalApplicationProperties applicationProperties;

    @Test
    public void testApplicationProperties() {
        assertThat(applicationProperties).isNotNull();
        assertThat(applicationProperties.getPrefix()).isNotNull();
    }
}
