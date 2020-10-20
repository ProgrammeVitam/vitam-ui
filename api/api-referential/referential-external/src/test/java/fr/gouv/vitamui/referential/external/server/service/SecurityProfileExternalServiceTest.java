package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.SecurityProfileDto;
import fr.gouv.vitamui.referential.internal.client.SecurityProfileInternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityProfileExternalServiceTest extends ExternalServiceTest {

    @Mock
    private SecurityProfileInternalRestClient securityProfileInternalRestClient;
    @Mock
    private ExternalSecurityService externalSecurityService;

    private SecurityProfileExternalService securityProfileExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        securityProfileExternalService = new SecurityProfileExternalService(externalSecurityService, securityProfileInternalRestClient);
    }

    @Test
    public void getAll_should_return_SecurityProfileDtoList_when_profileInternalRestClient_return_SecurityProfileDtoList() {

        List<SecurityProfileDto> list = new ArrayList<>();
        SecurityProfileDto securityProfileDto = new SecurityProfileDto();
        securityProfileDto.setId("1");
        list.add(securityProfileDto);

        when(securityProfileInternalRestClient.getAll(any(InternalHttpContext.class), any(Optional.class)))
            .thenReturn(list);

        assertThatCode(() -> {
            securityProfileExternalService.getAll(Optional.empty());
        }).doesNotThrowAnyException();
    }

    @Test
    public void create_should_return_SecurityProfileDto_when_profileInternalRestClient_return_SecurityProfileDto() {

        SecurityProfileDto securityProfileDto = new SecurityProfileDto();
        securityProfileDto.setId("1");

        when(securityProfileInternalRestClient.create(any(InternalHttpContext.class), any(SecurityProfileDto.class)))
            .thenReturn(securityProfileDto);

        assertThatCode(() -> {
            securityProfileExternalService.create(new SecurityProfileDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void check_should_return_boolean_when_profileInternalRestClient_return_boolean() {

        when(securityProfileInternalRestClient.check(any(InternalHttpContext.class), any(SecurityProfileDto.class)))
            .thenReturn(true);

        assertThatCode(() -> {
            securityProfileExternalService.check(new SecurityProfileDto());
        }).doesNotThrowAnyException();
    }

    @Test
    public void delete_should_return_ok_when_profileInternalRestClient_return_ok() {

        doNothing().when(securityProfileInternalRestClient).delete(any(InternalHttpContext.class), any(String.class));
        String id = "1";

        assertThatCode(() -> {
            securityProfileExternalService.delete(id);
        }).doesNotThrowAnyException();
    }
}
