/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.iam.internal.server.security;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.token.domain.Token;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import lombok.Getter;
import lombok.Setter;

/**
 * External authentication service
 *
 *
 */
@Getter
@Setter
public class IamAuthentificationService {

    private final TokenRepository tokenRepository;

    private final UserInternalService internalUserService;

    private final SubrogationRepository subrogationRepository;

    @Value("${token.additional.ttl}")
    @NotNull
    @Setter
    private Integer tokenAdditionalTtl;

    public IamAuthentificationService(final UserInternalService internalUserService, final TokenRepository tokenRepository,
            final SubrogationRepository subrogationRepository) {
        this.internalUserService = internalUserService;
        this.tokenRepository = tokenRepository;
        this.subrogationRepository = subrogationRepository;
    }

    /**
     * Return the User profile and check security data.
     * @param httpContext HTTP Context.
     * @return
     */
    public AuthUserDto getUserFromHttpContext(final InternalHttpContext httpContext) {

        final String userToken = httpContext.getUserToken();
        if (userToken == null) {
            throw new BadCredentialsException("Usertoken not found in header");
        }

        return getUserByToken(userToken);
    }

    private AuthUserDto getUserByToken(final String userToken) {
        final Token token = tokenRepository.findById(userToken).orElseThrow(() -> new BadCredentialsException("No user found for usertoken: " + userToken));

        if (!token.isSurrogation()) {
            final LocalDate date = convertToLocalDate(token.getUpdatedDate());
            if (date.isAfter(LocalDate.of(2018, 10, 1))) {
                token.setUpdatedDate(DateUtils.addMinutes(token.getUpdatedDate(), tokenAdditionalTtl));
                tokenRepository.save(token);
            }
        }

        final UserDto userDto = internalUserService.findUserById(token.getRefId());
        final AuthUserDto authUserDto = internalUserService.loadGroupAndProfiles(userDto);
        if (token.isSurrogation()) {
            final String surrogateEmail = userDto.getEmail();
            final Subrogation subrogation = subrogationRepository.findOneBySurrogate(surrogateEmail);
            final String superUserEmail = subrogation.getSuperUser();
            authUserDto.setSuperUser(superUserEmail);
            final UserDto superUserDto = internalUserService.findUserByEmail(superUserEmail);
            authUserDto.setSuperUserIdentifier(superUserDto.getIdentifier());
        }
        internalUserService.addBasicCustomerAndProofTenantIdentifierInformation(authUserDto);
        internalUserService.addTenantsByAppInformation(authUserDto);
        authUserDto.setAuthToken(userToken);
        return authUserDto;
    }

    private LocalDate convertToLocalDate(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
