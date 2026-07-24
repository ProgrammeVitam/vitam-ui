/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Wraps a {@link UserDto} so that {@code Authentication.getName()} returns a stable identifier
 * (the user id) instead of falling back to {@code UserDto.toString()} — the Lombok-generated
 * {@code toString()} embeds memory hashCodes (e.g. {@code AnalyticsDto@1bd121ef}) that differ
 * between instances, which breaks OIDC checks such as the {@code sub} comparison in
 * {@code OidcLogoutAuthenticationProvider}.
 *
 * <p>Also used by {@link OpaqueVitamTokenGenerator} and the JWT customizer to reach the UserDto
 * via {@link #getUserDto()}.
 */
public class VitamuiPrincipal implements AuthenticatedPrincipal {

    private final UserDto userDto;

    public VitamuiPrincipal(UserDto userDto) {
        if (userDto == null || userDto.getId() == null) {
            throw new IllegalArgumentException("UserDto (with id) must not be null");
        }
        this.userDto = userDto;
    }

    public UserDto getUserDto() {
        return userDto;
    }

    @Override
    public String getName() {
        return userDto.getId();
    }

    @Override
    public String toString() {
        return "VitamuiPrincipal[id=" + userDto.getId() + ", email=" + userDto.getEmail() + "]";
    }
}
