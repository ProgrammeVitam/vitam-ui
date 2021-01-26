package fr.gouv.vitamui.ui.commons.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.rest.AccountController;
import fr.gouv.vitamui.ui.commons.rest.ApplicationController;
import fr.gouv.vitamui.ui.commons.rest.SubrogationController;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;

@RunWith(SpringJUnit4ClassRunner.class)
public class UICommonsAutoConfigurationTest {


    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(UICommonsAutoConfiguration.class))
                    .withConfiguration(AutoConfigurations.of(UICommonsAutoSpringMockConfiguration.class))
                    .withSystemProperties("controller.subrogation.enabled=true", "controller.user.enabled=true");

    @Test
    public void serviceNameCanBeConfigured() {
        contextRunner.withUserConfiguration(UserConfiguration.class).run((context) -> {
            assertThat(context).hasSingleBean(ApplicationService.class);
            assertThat(context).hasSingleBean(ApplicationController.class);
            assertThat(context).hasSingleBean(IamExternalRestClientFactory.class);
            assertThat(context).hasSingleBean(SubrogationController.class);
            assertThat(context).hasSingleBean(AccountController.class);
        });
    }

    @Configuration
    static class UserConfiguration {


        @Bean
        public UIProperties uiProperties() {
            final UIPropertiesImpl properties = new UIPropertiesImpl();
            properties.setIamExternalClient(new RestClientConfiguration());
            return properties;
        }

        @Bean
        public CasLogoutUrl caLogoutUrl() {
            final CasLogoutUrl casBean = new CasLogoutUrl("url");
            return casBean;
        }

    }

}
