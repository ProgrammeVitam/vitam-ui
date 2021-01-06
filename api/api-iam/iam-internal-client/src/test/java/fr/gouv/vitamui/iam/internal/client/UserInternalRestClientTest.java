package fr.gouv.vitamui.iam.internal.client;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.commons.test.utils.UserBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserInternalRestClientTest extends AbstractServerIdentityBuilder {

    private final String BASE_URL = "http://localhost:8083";

    private UserInternalRestClient userInternalRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        userInternalRestClient = new UserInternalRestClient(restTemplate, BASE_URL);
    }

    @Test
    public void patchAnalyticsShouldReturnUserWithNewData() {
        UserDto user = UserBuilder.buildWithAnalytics();
        InternalHttpContext context = new InternalHttpContext(9, "", "", "", "", "", "", "");

        String PATCH_ANALYTIC_URL = BASE_URL + "/iam/v1/users/analytics";
        when(restTemplate.exchange(eq(URI.create(PATCH_ANALYTIC_URL)), eq(HttpMethod.POST), any(), eq(UserDto.class)))
                .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));
        Map<String, Object> analytics = Map.of(APPLICATION_ID, "INGEST_SUPERVISION_APP");

        UserDto response = userInternalRestClient.patchAnalytics(context, analytics);

        assertThat(response).isEqualTo(user);
        verify(restTemplate).exchange(any(), any(), any(), eq(UserDto.class));
    }
}
