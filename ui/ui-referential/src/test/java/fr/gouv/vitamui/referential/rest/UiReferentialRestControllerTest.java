package fr.gouv.vitamui.referential.rest;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.referential.config.ReferentialApplicationProperties;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.rest.UIControllerTest;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;

@ImportAutoConfiguration(classes = { UICommonsAutoSpringMockConfiguration.class, UICommonsAutoConfiguration.class })
@Import(value = { ReferentialApplicationProperties.class, SecurityConfig.class, ServerIdentityConfiguration.class,
        RestExceptionHandler.class })
@TestPropertySource(properties = { "spring.config.name=ui-referential-application" })
public abstract class UiReferentialRestControllerTest<T extends IdDto> extends UIControllerTest<T> {

}
