package fr.gouv.vitamui.iam.server.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The debug logs of the identity provider endpoints do not contain the keystore password nor the client secret.
 */
class IdentityProviderControllerLogTest {

    private static final String PASSWORD = "keystore-pass-value";
    private static final String CLIENT_SECRET = "client-secret-value";
    private static final String PROVIDER_JSON =
        "{\"id\":\"idp\",\"keystorePassword\":\"" + PASSWORD + "\",\"clientSecret\":\"" + CLIENT_SECRET + "\"}";

    private final IdentityProviderService identityProviderService = mock(IdentityProviderService.class);
    private final IdentityProviderController controller = new IdentityProviderController(identityProviderService);

    private final Logger logger = (Logger) LoggerFactory.getLogger(IdentityProviderController.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        previousLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        logger.setLevel(previousLevel);
    }

    @Test
    void patch_does_not_log_the_values() throws Exception {
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("id", "idp");
        partialDto.put("keystorePassword", PASSWORD);
        partialDto.put("clientSecret", CLIENT_SECRET);

        controller.patch("idp", partialDto);

        assertNoSecretLogged();
    }

    @Test
    void keystore_patch_does_not_log_the_provider() throws Exception {
        controller.patchProviderKeystore(
            new MockMultipartFile("keystore", "keystore.jks", null, new byte[] { 1 }),
            PROVIDER_JSON,
            "idp"
        );

        assertNoSecretLogged();
    }

    @Test
    void create_does_not_log_the_secrets() throws Exception {
        final IdentityProviderDto dto = new IdentityProviderDto();
        dto.setKeystorePassword(PASSWORD);
        dto.setClientSecret(CLIENT_SECRET);
        when(identityProviderService.mapToIdentityProviderDto(any(), any(), any())).thenReturn(dto);

        controller.create(PROVIDER_JSON, null, null);

        assertThat(appender.list).isNotEmpty();
        assertNoSecretLogged();
    }

    private void assertNoSecretLogged() {
        assertThat(appender.list)
            .extracting(ILoggingEvent::getFormattedMessage)
            .noneMatch(message -> message.contains(PASSWORD) || message.contains(CLIENT_SECRET));
    }
}
