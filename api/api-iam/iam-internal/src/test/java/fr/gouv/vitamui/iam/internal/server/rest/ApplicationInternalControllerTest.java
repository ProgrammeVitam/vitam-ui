package fr.gouv.vitamui.iam.internal.server.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.iam.internal.server.application.service.ApplicationInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

/**
 * Tests the {@link ApplicationInternalController}.
 *
 *
 */
public final class ApplicationInternalControllerTest {

    @InjectMocks
    private ApplicationInternalController controller;

    @Mock
    private ApplicationInternalService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void testGetAll() {
        List<ApplicationDto> apps = Arrays.asList(buildApplicationDto());

        when(service.getAll(any(Optional.class), any())).thenReturn(apps);

        try {
            controller.getAll(Optional.empty(), Optional.empty());
        }
        catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must match the path identifier for update.", e.getMessage());
        }
    }

    private ApplicationDto buildApplicationDto() {
        return IamServerUtilsTest.buildApplicationDto();
    }

}
