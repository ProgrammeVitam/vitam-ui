package fr.gouv.vitamui;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;

@RunWith(SpringRunner.class)
@Import(ServerIdentityConfiguration.class)
public abstract class AbstractIntegrationTest {

    public static final OffsetDateTime start = OffsetDateTime.now();
}
