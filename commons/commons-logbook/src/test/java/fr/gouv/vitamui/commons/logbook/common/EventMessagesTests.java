package fr.gouv.vitamui.commons.logbook.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.TestMongoConfig;
import fr.gouv.vitamui.commons.logbook.config.LogbookAutoConfiguration;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class, LogbookAutoConfiguration.class })
public class EventMessagesTests {

    @MockBean
    private AdminExternalClient adminExternalClient;

    @Autowired
    private EventMessages msg;

    @BeforeClass
    public static void setup() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void testLogbookMessages() {
        assertThat(msg).isNotNull();
        assertThat(msg.getOutMessg()).isNotNull();

        Condition<String> condition = new Condition<>(s -> StringUtils.isNotBlank(s), "is not empty");
        assertThat(msg.getOutMessg()).hasValueSatisfying(condition);
    }
}
