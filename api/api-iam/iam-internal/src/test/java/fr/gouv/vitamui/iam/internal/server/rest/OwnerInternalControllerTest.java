package fr.gouv.vitamui.iam.internal.server.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;


/**
 * Tests the {@link OwnerInternalController}.
 *
 *
 */
public final class OwnerInternalControllerTest extends AbstractServerIdentityBuilder {

    @InjectMocks
    private OwnerInternalController controller;

    @Mock
    private OwnerInternalService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private void prepareServices() {
    }

    @Test
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferent() throws Exception {
        prepareServices();

        try {
            final OwnerDto dto = buildOwnerDto();
            controller.update("badId", dto);
        }
        catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must match the path identifier for update.", e.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotDelete() {
        prepareServices();
        controller.delete("Id");
    }

    private OwnerDto buildOwnerDto() {
        return IamServerUtilsTest.buildOwnerDto();
    }

}
