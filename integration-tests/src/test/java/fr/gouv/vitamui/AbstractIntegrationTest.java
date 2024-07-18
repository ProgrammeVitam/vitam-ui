package fr.gouv.vitamui;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;

@RunWith(SpringRunner.class)
public abstract class AbstractIntegrationTest {

    public static final OffsetDateTime start = OffsetDateTime.now();
}
