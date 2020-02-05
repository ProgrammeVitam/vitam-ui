package fr.gouv.vitamui;

import java.time.OffsetDateTime;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

@RunWith(SpringRunner.class)
@Import(ServerIdentityConfiguration.class)
public abstract class AbstractIntegrationTest {

    public static final OffsetDateTime start = OffsetDateTime.now();

}
