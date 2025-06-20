package fr.gouv.vitamui.iam.security.service;

import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public interface UserAuthenticationService {
    AuthUserDto getUserFromHttpContext(PreAuthenticatedAuthenticationToken token);
}
