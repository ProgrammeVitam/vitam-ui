package fr.gouv.vitamui.identity.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

@RunWith(SpringRunner.class)
@AutoConfigureJsonTesters
public class MyJsonTests {
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(MyJsonTests.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    public void testSerialize() throws Exception {
        final UserDto user = new UserDto();
        final OffsetDateTime now = OffsetDateTime.now();
        final String nowString = now.toString();
        user.setLastConnection(now);
        final JsonContent<UserDto> jsonContent = json.write(user);
        assertThat(jsonContent).hasJsonPathStringValue("@.lastConnection");
        LOGGER.debug("OffsetDateTime serialize format : " + nowString);
    }

}
