package fr.gouv.vitamui.iam.internal.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import fr.gouv.vitamui.commons.api.domain.AnalyticsDto;
import fr.gouv.vitamui.commons.api.domain.ApplicationAnalyticsDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;

@RunWith(MockitoJUnitRunner.class)
public class UserInternalRestClientTest extends AbstractServerIdentityBuilder {

    private final String BASE_URL = "http://localhost:8083";

    private final String PATCH_ANALYTIC_URL = BASE_URL + "/iam/v1/users/analytics";

    private UserInternalRestClient userInternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        userInternalRestClient = new UserInternalRestClient(restTemplate, BASE_URL);
    }

    @Test
    public void patchAnalyticsShouldReturnUserWithNewData() {
        UserDto user = buildUser();
        InternalHttpContext context = new InternalHttpContext(9, "", "", "", "", "", "", "");

        when(restTemplate.exchange(eq(URI.create(PATCH_ANALYTIC_URL)), eq(HttpMethod.POST), any(), eq(UserDto.class)))
                .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));
        Map<String, Object> analytics = Map.of("applicationId", "INGEST_SUPERVISION_APP");

        UserDto response = userInternalRestClient.patchAnalytics(context, analytics);

        assertThat(response).isEqualTo(user);
        verify(restTemplate).exchange(any(), any(), any(), eq(UserDto.class));
    }

    private UserDto buildUser() {
        ApplicationAnalyticsDto applicationAnalytic = new ApplicationAnalyticsDto();
        applicationAnalytic.setAccessCounter(9546);
        applicationAnalytic.setLastAccess(OffsetDateTime.now());
        applicationAnalytic.setApplicationId("INGEST_SUPERVISION_APP");

        AnalyticsDto analytics = new AnalyticsDto();
        analytics.setApplications(List.of(applicationAnalytic));

        UserDto user = new UserDto();
        user.setId("854");
        user.setEmail("test@user.fr");
        user.setAnalytics(analytics);

        return user;
    }

}
