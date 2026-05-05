package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link IdentityProviderController}.
 */
final class IdentityProviderCrudControllerTest {

    private AutoCloseable mocks;

    @InjectMocks
    private IdentityProviderController controller;

    @Mock
    private IdentityProviderService service;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    private void prepareServices() {}

    @Test
    void testCannotUpdate() {
        assertThrows(UnsupportedOperationException.class, () -> {
            final IdentityProviderDto dto = buildIdentityProviderDto();
            controller.update("id", dto);
        });
    }

    @Test
    void testCannotDelete() {
        assertThrows(UnsupportedOperationException.class, () -> {
            prepareServices();
            controller.delete("Id");
        });
    }

    private IdentityProviderDto buildIdentityProviderDto() {
        return IamServerUtilsTest.buildIdentityProviderDto();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
