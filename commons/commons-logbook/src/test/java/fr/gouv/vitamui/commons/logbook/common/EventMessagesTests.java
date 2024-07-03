package fr.gouv.vitamui.commons.logbook.common;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class EventMessagesTests {

    @MockBean
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
