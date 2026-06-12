package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.iam.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link OwnerController}.
 */
final class OwnerControllerTest {

    private AutoCloseable mocks;

    @InjectMocks
    private OwnerController controller;

    @Mock
    private OwnerService service;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    private void prepareServices() {}

    @Test
    void testUpdateFailsAsDtoIdAndPathIdAreDifferent() throws Exception {
        prepareServices();

        try {
            final OwnerDto dto = buildOwnerDto();
            controller.update("badId", dto);
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must match the path identifier for update.", e.getMessage());
        }
    }

    @Test
    void testCannotDelete() {
        assertThrows(UnsupportedOperationException.class, () -> {
            prepareServices();
            controller.delete("Id");
        });
    }

    private OwnerDto buildOwnerDto() {
        return IamServerUtilsTest.buildOwnerDto();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
