package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests the {@link IdentityProviderController}.
 *
 *
 */
public final class IdentityProviderCrudControllerTest {

    @InjectMocks
    private IdentityProviderController controller;

    @Mock
    private IdentityProviderService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private void prepareServices() {}

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotUpdate() {
        final IdentityProviderDto dto = buildIdentityProviderDto();
        controller.update("id", dto);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotDelete() throws InvalidParseOperationException, PreconditionFailedException {
        prepareServices();
        controller.delete("Id");
    }

    private IdentityProviderDto buildIdentityProviderDto() {
        return IamServerUtilsTest.buildIdentityProviderDto();
    }
}
