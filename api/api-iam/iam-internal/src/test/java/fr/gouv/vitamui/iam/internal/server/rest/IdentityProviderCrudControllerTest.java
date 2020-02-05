package fr.gouv.vitamui.iam.internal.server.rest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

/**
 * Tests the {@link IdentityProviderInternalController}.
 *
 *
 */
public final class IdentityProviderCrudControllerTest {

    @InjectMocks
    private IdentityProviderInternalController controller;

    @Mock
    private IdentityProviderInternalService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private void prepareServices() {
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotUpdate() {
        final IdentityProviderDto dto = buildIdentityProviderDto();
        controller.update("id", dto);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotDelete() {
        prepareServices();
        controller.delete("Id");
    }

    private IdentityProviderDto buildIdentityProviderDto() {
        return IamServerUtilsTest.buildIdentityProviderDto();
    }

}
