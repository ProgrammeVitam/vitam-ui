package fr.gouv.vitamui.portal;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.portal.config.PortalApplicationProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PortalApplicationTest {

    @Autowired
    PortalApplicationProperties portalProperties;

    @MockBean
    private CasLogoutUrl casLogoutUrl;

    @MockBean
    private BuildProperties buildProperties;

    @Test
    public void testContextLoads() {
        assertThat(portalProperties).isNotNull();
    }

}
