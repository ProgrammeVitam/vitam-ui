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
package fr.gouv.vitamui.iam.server.security;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.UserAuthenticationService;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.server.token.domain.Token;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * External authentication service
 */
@Service
@Slf4j
public class IamUserAuthentificationService implements UserAuthenticationService {

    public static final String INTERNAL_CAS_USER_NAME = "casuser";
    private final TokenRepository tokenRepository;

    private final UserService userService;

    private final SubrogationRepository subrogationRepository;

    @Value("${token.max-ttl:480}")
    @NotNull
    private Integer tokenMaxTtl;

    @Value("${token.additional.ttl}")
    @NotNull
    private Integer tokenAdditionalTtl;

    @Value("${cas_secret_token}")
    @NotNull
    private String casSecretToken;

    public IamUserAuthentificationService(
        final UserService userService,
        final TokenRepository tokenRepository,
        final SubrogationRepository subrogationRepository
    ) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.subrogationRepository = subrogationRepository;
    }

    @Override
    public AuthUserDto getUserFromHttpContext(
        final PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken
    ) {
        final String userToken = ((HttpContext) preAuthenticatedAuthenticationToken.getPrincipal()).getUserToken();
        if (userToken == null) {
            throw new BadCredentialsException("Usertoken not found in header");
        }

        final Token token = getToken(userToken);

        return getUserByToken(token);
    }

    private Token getToken(String userToken) {
        if (this.casSecretToken.equals(userToken)) {
            log.debug("Granted access to CAS token");
            Token token = new Token();
            token.setRefId(INTERNAL_CAS_USER_NAME);
            return token;
        }

        log.debug("Checking access for user token: '{}'", userToken);
        Token token = tokenRepository
            .findById(userToken)
            .orElseThrow(() -> new BadCredentialsException("No user found for usertoken: " + userToken));

        log.debug("Found valid token: {}", token);
        if (!token.isSurrogation()) {
            Date tokenMaxExpirationDate = DateUtils.addMinutes(token.getCreatedDate(), tokenMaxTtl);
            Date currentTokenExpirationDate = token.getUpdatedDate();
            Date newTokenExpirationDate = DateUtils.addMinutes(new Date(), tokenAdditionalTtl);

            if (currentTokenExpirationDate.before(new Date())) {
                throw new BadCredentialsException("Expired token  usertoken: " + userToken);
            }
            if (
                currentTokenExpirationDate.before(newTokenExpirationDate) &&
                newTokenExpirationDate.before(tokenMaxExpirationDate)
            ) {
                token.setUpdatedDate(newTokenExpirationDate);
                tokenRepository.save(token);
            }
        }
        return token;
    }

    private AuthUserDto getUserByToken(final Token token) {
        final UserDto userDto = userService.findUserById(token.getRefId());
        final AuthUserDto authUserDto = userService.loadGroupAndProfiles(userDto);
        if (token.isSurrogation()) {
            final String surrogateEmail = userDto.getEmail();
            final Subrogation subrogation = subrogationRepository.findOneBySurrogateAndSurrogateCustomerId(
                surrogateEmail,
                userDto.getCustomerId()
            );
            final String superUserEmail = subrogation.getSuperUser();
            authUserDto.setSuperUser(superUserEmail);
            authUserDto.setSuperUserCustomerId(subrogation.getSuperUserCustomerId());
            final UserDto superUserDto = userService.findUserByEmailAndCustomerId(
                superUserEmail,
                subrogation.getSuperUserCustomerId()
            );
            authUserDto.setSuperUserIdentifier(superUserDto.getIdentifier());
        }
        userService.addBasicCustomerAndProofTenantIdentifierInformation(authUserDto);
        userService.addTenantsByAppInformation(authUserDto);
        authUserDto.setAuthToken(token.getRefId());
        return authUserDto;
    }
}
