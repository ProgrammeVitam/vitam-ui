/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.iam.server.user.password.reset;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class ResetPasswordValidationServiceTest {

    private ResetPasswordNotifier<UserDto> delegate;
    private IdentityProviderHelper helper;
    private IdentityProviderService idpService;

    @BeforeEach
    void setUp() {
        delegate = mock(ResetPasswordNotifier.class);
        helper = mock(IdentityProviderHelper.class);
        idpService = mock(IdentityProviderService.class);

        when(idpService.getAll(any(), any())).thenReturn(List.of());
    }

    // -----------------------
    // STRICT MODE
    // -----------------------

    @Test
    @DisplayName("Strict mode: should throw exception when user is disabled")
    void strict_should_throw_when_user_disabled() {
        ResetPasswordValidationService service = new ResetPasswordValidationService(
            delegate,
            helper,
            idpService,
            strictHandler()
        );

        UserDto user = buildUser();
        user.setStatus(UserStatusEnum.DISABLED);

        assertThrows(IllegalStateException.class, () -> service.notify(user));

        verifyNoInteractions(delegate);
        verifyNoInteractions(helper);
        verifyNoInteractions(idpService);
    }

    @Test
    @DisplayName("Strict mode: should throw exception when user is not nominative")
    void strict_should_throw_when_user_not_nominative() {
        ResetPasswordValidationService service = new ResetPasswordValidationService(
            delegate,
            helper,
            idpService,
            strictHandler()
        );

        UserDto user = buildUser();
        user.setType(UserTypeEnum.GENERIC);

        assertThrows(IllegalStateException.class, () -> service.notify(user));

        verifyNoInteractions(delegate);
        verifyNoInteractions(helper);
        verifyNoInteractions(idpService);
    }

    @Test
    @DisplayName("Strict mode: should throw exception when identifier does not match provider pattern")
    void strict_should_throw_when_pattern_invalid() {
        when(helper.identifierMatchProviderPattern(any(), any(), any())).thenReturn(false);

        ResetPasswordValidationService service = new ResetPasswordValidationService(
            delegate,
            helper,
            idpService,
            strictHandler()
        );

        UserDto user = buildUser();

        assertThrows(IllegalStateException.class, () -> service.notify(user));

        verify(helper).identifierMatchProviderPattern(any(), eq(user.getEmail()), eq(user.getCustomerId()));
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Strict mode: should call delegate when user is valid")
    void strict_should_call_delegate_when_valid() {
        when(helper.identifierMatchProviderPattern(any(), any(), any())).thenReturn(true);

        ResetPasswordValidationService service = new ResetPasswordValidationService(
            delegate,
            helper,
            idpService,
            strictHandler()
        );

        UserDto user = buildUser();

        service.notify(user);

        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(delegate).notify(captor.capture());

        UserDto sent = captor.getValue();

        assertThat(sent.getEmail()).isEqualTo(user.getEmail());
        assertThat(sent.getCustomerId()).isEqualTo(user.getCustomerId());
    }

    // -----------------------
    // LAX MODE
    // -----------------------

    @Test
    @DisplayName("Lax mode: should not call delegate when identifier does not match provider pattern")
    void lax_should_not_call_delegate_when_pattern_invalid() {
        when(helper.identifierMatchProviderPattern(any(), any(), any())).thenReturn(false);

        ResetPasswordValidationService service = new ResetPasswordValidationService(
            delegate,
            helper,
            idpService,
            laxHandler()
        );

        UserDto user = buildUser();

        service.notify(user);

        verify(helper).identifierMatchProviderPattern(any(), eq(user.getEmail()), eq(user.getCustomerId()));
        verifyNoInteractions(delegate);
    }

    @Test
    @DisplayName("Lax mode: should call delegate when user is valid")
    void lax_should_call_delegate_when_valid() {
        when(helper.identifierMatchProviderPattern(any(), any(), any())).thenReturn(true);

        ResetPasswordValidationService service = new ResetPasswordValidationService(
            delegate,
            helper,
            idpService,
            laxHandler()
        );

        UserDto user = buildUser();

        service.notify(user);

        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(delegate).notify(captor.capture());

        UserDto sent = captor.getValue();

        assertThat(sent.getEmail()).isEqualTo(user.getEmail());
        assertThat(sent.getCustomerId()).isEqualTo(user.getCustomerId());
    }

    // -----------------------
    // HELPERS
    // -----------------------

    private ValidationFailureHandler strictHandler() {
        return new StrictFailureHandler();
    }

    private ValidationFailureHandler laxHandler() {
        return new LaxFailureHandler();
    }

    private UserDto buildUser() {
        UserDto user = new UserDto();
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setEmail("john.doe@vitamui.com");
        user.setCustomerId("CustomerId");
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setUserInfoId("userInfoId");
        return user;
    }
}
