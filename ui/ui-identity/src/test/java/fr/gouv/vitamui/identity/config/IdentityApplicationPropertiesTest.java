package fr.gouv.vitamui.identity.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class IdentityApplicationPropertiesTest {

    @MockBean
    private CasLogoutUrl casLogoutUrl;

    @MockBean
    private BuildProperties buildProperties;

    @Autowired
    private IdentityApplicationProperties applicationProperties;

    @Test
    public void testApplicationProperties() {
        assertThat(applicationProperties).isNotNull();
        assertThat(applicationProperties.getLimitPagination()).isNotNull();
        assertThat(applicationProperties.getPrefix()).isNotNull();
    }
}
