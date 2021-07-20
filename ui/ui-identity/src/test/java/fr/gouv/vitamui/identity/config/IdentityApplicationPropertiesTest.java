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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class IdentityApplicationPropertiesTest {

    @Autowired
    private IdentityApplicationProperties applicationProperties;

    @MockBean
    BuildProperties buildProperties;

    @Test
    public void testApplicationProperties() {
        assertThat(applicationProperties).isNotNull();
        assertThat(applicationProperties.getLimitPagination()).isNotNull();
        assertThat(applicationProperties.getPrefix()).isNotNull();
    }
}
