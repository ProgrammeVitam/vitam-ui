package fr.gouv.vitamui.identity.rest;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.identity.config.IdentityApplicationProperties;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.rest.UIControllerTest;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;

@ImportAutoConfiguration(classes = { UICommonsAutoSpringMockConfiguration.class, UICommonsAutoConfiguration.class })
@Import(value = { IdentityApplicationProperties.class, SecurityConfig.class, ServerIdentityConfiguration.class,
        RestExceptionHandler.class })
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public abstract class UiIdentityRestControllerTest<T extends IdDto> extends UIControllerTest<T> {

}
