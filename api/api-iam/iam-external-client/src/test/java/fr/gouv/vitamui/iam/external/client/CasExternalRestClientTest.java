package fr.gouv.vitamui.iam.external.client;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class CasExternalRestClientTest extends AbstractServerIdentityBuilder {

    private CasExternalRestClient client;

    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    private final ExternalHttpContext header = new ExternalHttpContext(1, "", "", "");

    @Before
    public void setup() {
        Mockito.reset(restTemplate);
        client = new CasExternalRestClient(restTemplate, "http://localhost:8080");
    }

    @Test
    public void testLogout() {
        final ArgumentCaptor<URI> argumentCaptor = ArgumentCaptor.forClass(URI.class);
        Mockito.when(restTemplate.exchange(argumentCaptor.capture(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        final String superUser = "julien@vitamui.com";
        final String authToken = "TOK-1-F8lEhVif0FWjgDF32ov73TtKhE6mflRu";
        client.logout(header, authToken, superUser);
        final String path = RestApi.CAS_LOGOUT_PATH + "?authToken=" + authToken + "&superUser=" + superUser;
        assertThat(argumentCaptor.getValue().toString()).endsWith(path.replaceAll(CommonConstants.EMAIL_SEPARATOR, "%40"));
    }
}
