package fr.gouv.vitamui.pastis.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.pastis.config.PastisApplicationProperties;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.rest.UIControllerTest;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;

@ImportAutoConfiguration(classes= {UICommonsAutoSpringMockConfiguration.class, UICommonsAutoConfiguration.class})
@Import(value = {PastisApplicationProperties.class, SecurityConfig.class, ServerIdentityConfiguration.class,
    RestExceptionHandler.class})
public abstract class UIPastisRestControllerTest<T extends IdDto> extends UIControllerTest<T> {
}
