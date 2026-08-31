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

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.server.token.domain.Token;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import fr.gouv.vitamui.security.common.dto.ContextDto;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IamUserAuthentificationServiceTest {

    private static final String SECRET = "tokcas_secret_for_tests";

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private SubrogationRepository subrogationRepository;

    @InjectMocks
    private IamUserAuthentificationService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "casSecretToken", SECRET);
        ReflectionTestUtils.setField(service, "tokenMaxTtl", 480);
        ReflectionTestUtils.setField(service, "tokenAdditionalTtl", 240);
    }

    @Test
    void the_shared_secret_resolves_the_cas_service_account_without_any_database_lookup() {
        givenTheCasServiceAccount();

        final AuthUserDto user = service.getUserFromHttpContext(callFromTheCasApplication(SECRET));

        assertThat(user.getAuthToken()).isEqualTo(IamUserAuthentificationService.INTERNAL_CAS_USER_NAME);
        verify(tokenRepository, never()).findById(any());
    }

    @Test
    void the_shared_secret_is_refused_when_nothing_proves_the_caller_is_the_cas_application() {
        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken(SECRET))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    @Test
    void the_shared_secret_is_refused_when_the_caller_is_another_certified_application() {
        final PreAuthenticatedAuthenticationToken call = callWithToken(SECRET);
        final ContextDto anotherContext = new ContextDto();
        anotherContext.setRoleNames(List.of(ServicesData.ROLE_GET_USERS));
        call.setDetails(anotherContext);

        assertThatThrownBy(() -> service.getUserFromHttpContext(call)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void a_missing_token_is_refused() {
        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken(null))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    @Test
    void an_unknown_token_is_refused() {
        when(tokenRepository.findById("TOK-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken("TOK-unknown"))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    @Test
    void an_expired_token_is_refused() {
        final Token token = new Token();
        token.setRefId("userId");
        token.setCreatedDate(DateUtils.addMinutes(new Date(), -100));
        token.setUpdatedDate(DateUtils.addMinutes(new Date(), -1));
        when(tokenRepository.findById("TOK-expired")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.getUserFromHttpContext(callWithToken("TOK-expired"))).isInstanceOf(
            BadCredentialsException.class
        );
    }

    @Test
    void a_valid_token_close_to_expiry_is_extended() {
        final Token token = new Token();
        token.setId("TOK-valid");
        token.setRefId("userId");
        token.setCreatedDate(new Date());
        token.setUpdatedDate(DateUtils.addMinutes(new Date(), 5));
        when(tokenRepository.findById("TOK-valid")).thenReturn(Optional.of(token));
        givenAResolvableUser("userId");

        service.getUserFromHttpContext(callWithToken("TOK-valid"));

        verify(tokenRepository).save(token);
    }

    private void givenTheCasServiceAccount() {
        givenAResolvableUser(IamUserAuthentificationService.INTERNAL_CAS_USER_NAME);
    }

    private void givenAResolvableUser(final String userId) {
        final UserDto userDto = new UserDto();
        userDto.setId(userId);
        when(userService.findUserById(userId)).thenReturn(userDto);
        when(userService.loadGroupAndProfiles(userDto)).thenReturn(new AuthUserDto(userDto));
    }

    private PreAuthenticatedAuthenticationToken callFromTheCasApplication(final String userToken) {
        final PreAuthenticatedAuthenticationToken call = callWithToken(userToken);
        final ContextDto casContext = new ContextDto();
        casContext.setRoleNames(List.of(ServicesData.ROLE_CAS_LOGIN));
        call.setDetails(casContext);
        return call;
    }

    private PreAuthenticatedAuthenticationToken callWithToken(final String userToken) {
        final HttpContext httpContext = mock(HttpContext.class);
        when(httpContext.getUserToken()).thenReturn(userToken);
        return new PreAuthenticatedAuthenticationToken(httpContext, null);
    }
}
