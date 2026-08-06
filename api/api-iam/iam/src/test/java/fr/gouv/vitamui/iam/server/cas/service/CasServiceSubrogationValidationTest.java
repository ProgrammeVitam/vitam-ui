package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.iam.auth.contract.SubrogationValidateRequestDto;
import fr.gouv.vitamui.iam.auth.contract.SubrogationValidateResponseDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * What a subrogation validates, and what it refuses.
 *
 * The authentication server today asks for every subrogation of the super user, then looks for the one
 * matching all four values exactly. These cases pin that rule down, along with the additional refusal of
 * expired subrogations, which the current filtering does not perform.
 */
@ExtendWith(MockitoExtension.class)
class CasServiceSubrogationValidationTest {

    private static final String SUPER_USER_EMAIL = "super@organisation-a.fr";
    private static final String SUPER_USER_CUSTOMER = "customerA";
    private static final String SURROGATE_EMAIL = "agent@organisation-b.fr";
    private static final String SURROGATE_CUSTOMER = "customerB";

    @InjectMocks
    private CasService casService;

    @Mock
    private SubrogationRepository subrogationRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("an accepted and still valid subrogation resolves both identifiers")
    void acceptedSubrogationResolvesBothUsers() {
        givenSubrogation(SubrogationStatusEnum.ACCEPTED, inOneHour());
        givenBothUsersExist();

        final SubrogationValidateResponseDto response = casService.validateSubrogation(request());

        assertThat(response.getSuperUserId()).isEqualTo("superUserId");
        assertThat(response.getSurrogateUserId()).isEqualTo("surrogateUserId");
    }

    @Test
    @DisplayName("a missing subrogation is a refusal, not an empty response")
    void missingSubrogationIsRefused() {
        when(
            subrogationRepository.findBySuperUserAndSuperUserCustomerIdAndSurrogateAndSurrogateCustomerId(
                SUPER_USER_EMAIL,
                SUPER_USER_CUSTOMER,
                SURROGATE_EMAIL,
                SURROGATE_CUSTOMER
            )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() -> casService.validateSubrogation(request())).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("a subrogation merely created, not accepted, is refused")
    void pendingSubrogationIsRefused() {
        givenSubrogation(SubrogationStatusEnum.CREATED, inOneHour());

        assertThatThrownBy(() -> casService.validateSubrogation(request())).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("an expired subrogation is refused, without waiting for Mongo to purge it")
    void expiredSubrogationIsRefused() {
        // The TTL index only runs once a minute, and some deployments disable it altogether.
        givenSubrogation(SubrogationStatusEnum.ACCEPTED, anHourAgo());

        assertThatThrownBy(() -> casService.validateSubrogation(request()))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("a subrogation whose accounts no longer both exist is refused")
    void unresolvableUserIsRefused() {
        givenSubrogation(SubrogationStatusEnum.ACCEPTED, inOneHour());
        lenient()
            .when(userRepository.findByEmailIgnoreCaseAndCustomerId(SUPER_USER_EMAIL, SUPER_USER_CUSTOMER))
            .thenReturn(user("superUserId"));
        lenient()
            .when(userRepository.findByEmailIgnoreCaseAndCustomerId(SURROGATE_EMAIL, SURROGATE_CUSTOMER))
            .thenReturn(null);

        assertThatThrownBy(() -> casService.validateSubrogation(request())).isInstanceOf(NotFoundException.class);
    }

    private void givenSubrogation(final SubrogationStatusEnum status, final Date date) {
        final Subrogation subrogation = new Subrogation();
        subrogation.setStatus(status);
        subrogation.setDate(date);
        subrogation.setSuperUser(SUPER_USER_EMAIL);
        subrogation.setSuperUserCustomerId(SUPER_USER_CUSTOMER);
        subrogation.setSurrogate(SURROGATE_EMAIL);
        subrogation.setSurrogateCustomerId(SURROGATE_CUSTOMER);
        when(
            subrogationRepository.findBySuperUserAndSuperUserCustomerIdAndSurrogateAndSurrogateCustomerId(
                SUPER_USER_EMAIL,
                SUPER_USER_CUSTOMER,
                SURROGATE_EMAIL,
                SURROGATE_CUSTOMER
            )
        ).thenReturn(Optional.of(subrogation));
    }

    private void givenBothUsersExist() {
        when(userRepository.findByEmailIgnoreCaseAndCustomerId(SUPER_USER_EMAIL, SUPER_USER_CUSTOMER)).thenReturn(
            user("superUserId")
        );
        when(userRepository.findByEmailIgnoreCaseAndCustomerId(SURROGATE_EMAIL, SURROGATE_CUSTOMER)).thenReturn(
            user("surrogateUserId")
        );
    }

    private static SubrogationValidateRequestDto request() {
        return new SubrogationValidateRequestDto(
            SUPER_USER_EMAIL,
            SUPER_USER_CUSTOMER,
            SURROGATE_EMAIL,
            SURROGATE_CUSTOMER
        );
    }

    private static User user(final String id) {
        final User user = new User();
        user.setId(id);
        return user;
    }

    private static Date inOneHour() {
        return new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
    }

    private static Date anHourAgo() {
        return new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
    }
}
