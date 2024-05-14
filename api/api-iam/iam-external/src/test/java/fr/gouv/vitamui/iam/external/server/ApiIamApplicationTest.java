package fr.gouv.vitamui.iam.external.server;

import fr.gouv.vitamui.iam.external.server.config.ApiIamApplicationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ApiIamApplicationTest {

    @Autowired
    private Environment env;

    @Autowired
    private ApiIamApplicationProperties iamProperties;

    @MockBean
    BuildProperties buildProperties;

    @Test
    public void testContextLoads() {
        assertThat(env).isNotNull();

        assertThat(iamProperties).isNotNull();
        assertThat(iamProperties.getIamInternalClient()).isNotNull();
    }
}
