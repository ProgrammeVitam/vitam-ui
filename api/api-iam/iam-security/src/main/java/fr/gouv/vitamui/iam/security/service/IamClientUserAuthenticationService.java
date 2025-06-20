package fr.gouv.vitamui.iam.security.service;

import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.client.UserRestClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IamClientUserAuthenticationService implements UserAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IamClientUserAuthenticationService.class);

    private final UserRestClient userRestClient;

    @Autowired
    public IamClientUserAuthenticationService(final UserRestClient userRestClient) {
        this.userRestClient = userRestClient;
    }

    @Override
    public AuthUserDto getUserFromHttpContext(final PreAuthenticatedAuthenticationToken token) {
        final AuthUserDto userDto = getAuthenticatedUser(token);

        HttpContext httpContext = (HttpContext) token.getPrincipal();
        final List<Integer> userTenants = userDto
            .getProfileGroup()
            .getProfiles()
            .stream()
            .filter(ProfileDto::isEnabled)
            .map(ProfileDto::getTenantIdentifier)
            .collect(Collectors.toList());

        final Integer tenantIdentifier = httpContext.getTenantIdentifier();
        if (!userTenants.contains(tenantIdentifier)) {
            LOGGER.debug("Tenant id [{}] not in user tenants [{}]", tenantIdentifier, userTenants);
            throw new BadCredentialsException(
                "This tenant: " +
                httpContext.getTenantIdentifier() +
                " is not allowed for this user: " +
                userDto.getId()
            );
        }
        return userDto;
    }

    private AuthUserDto getAuthenticatedUser(PreAuthenticatedAuthenticationToken token) {
        final String userToken = ((HttpContext) token.getPrincipal()).getUserToken();
        if (StringUtils.isBlank(userToken)) {
            throw new BadCredentialsException("User token is empty");
        }

        final AuthUserDto userDto = userRestClient.getMe((HttpContext) token.getPrincipal());
        if (userDto == null) {
            throw new NotFoundException("User not found for token: " + userToken);
        }
        return userDto;
    }
}
