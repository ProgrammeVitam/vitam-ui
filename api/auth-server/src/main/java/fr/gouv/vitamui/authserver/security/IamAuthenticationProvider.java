/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link AuthenticationProvider} delegating password validation to IAM through {@link IamClient}.
 *
 * <p>The {@link Authentication} is expected to carry:
 * <ul>
 *   <li>{@code principal} = user email</li>
 *   <li>{@code credentials} = password</li>
 *   <li>{@code details} = {@link CustomerIdAuthenticationDetails} carrying the resolved {@code customerId}</li>
 * </ul>
 *
 * <p>On success the returned {@link Authentication} carries the {@link UserDto} as principal (used later by
 * {@link OpaqueVitamTokenGenerator} to mint the opaque access token).
 */
@Component
public class IamAuthenticationProvider implements AuthenticationProvider {

    private final IamClient iamClient;

    public IamAuthenticationProvider(IamClient iamClient) {
        this.iamClient = iamClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        String customerId = extractCustomerId(authentication);

        try {
            UserDto user = iamClient.login(email, password, customerId);
            if (user == null) {
                throw new BadCredentialsException("IAM returned no user");
            }
            UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            result.setDetails(authentication.getDetails());
            return result;
        } catch (IamClient.BadCredentialsClientException e) {
            throw new BadCredentialsException("Bad credentials", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String extractCustomerId(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof CustomerIdAuthenticationDetails customerDetails) {
            return customerDetails.getCustomerId();
        }
        throw new BadCredentialsException("customerId is required in Authentication details");
    }
}
