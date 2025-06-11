package fr.gouv.vitamui.commons.logbook.common;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EventMessagesTests {

    @MockitoBean
    private AdminExternalClient adminExternalClient;

    @Autowired
    private EventMessages msg;

    @Test
    public void testLogbookMessages() {
        assertThat(msg).isNotNull();
        assertThat(msg.getOutMessg()).isNotNull();

        Condition<String> condition = new Condition<>(StringUtils::isNotBlank, "is not empty");
        assertThat(msg.getOutMessg()).hasValueSatisfying(condition);
    }
}
