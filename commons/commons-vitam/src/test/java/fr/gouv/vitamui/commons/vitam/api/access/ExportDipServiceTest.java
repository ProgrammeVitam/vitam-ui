package fr.gouv.vitamui.commons.vitam.api.access;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.test.utils.FieldUtils;

@RunWith(MockitoJUnitRunner.class)
public class ExportDipServiceTest {

    @InjectMocks
    private ExportDipService exportDipService;

    @Mock
    private AccessExternalClient accessExternalClient;

    @Mock
    private VitamUILogger logger;

    @Before
    public void setUp() throws NoSuchFieldException, SecurityException, Exception {
        FieldUtils.setFinalStatic(ExportDipService.class.getDeclaredField("LOGGER"), logger);
    }

    @Test
    public void toImplement() {
        // TODO implement
    }

}
