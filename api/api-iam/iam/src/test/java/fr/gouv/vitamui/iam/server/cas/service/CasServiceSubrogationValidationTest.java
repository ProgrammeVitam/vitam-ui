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
 * Ce que valide une subrogation, et ce qu'elle refuse.
 *
 * Le serveur d'authentification demande aujourd'hui toutes les subrogations du super-utilisateur puis
 * cherche celle qui correspond exactement aux quatre valeurs. Ces cas figent cette règle, et le refus
 * supplémentaire des subrogations expirées, que le filtrage actuel ne fait pas.
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
    @DisplayName("une subrogation acceptée et valide résout les deux identifiants")
    void acceptedSubrogationResolvesBothUsers() {
        givenSubrogation(SubrogationStatusEnum.ACCEPTED, inOneHour());
        givenBothUsersExist();

        final SubrogationValidateResponseDto response = casService.validateSubrogation(request());

        assertThat(response.getSuperUserId()).isEqualTo("superUserId");
        assertThat(response.getSurrogateUserId()).isEqualTo("surrogateUserId");
    }

    @Test
    @DisplayName("l'absence de subrogation est un refus, pas une réponse vide")
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
    @DisplayName("une subrogation seulement créée, non acceptée, est refusée")
    void pendingSubrogationIsRefused() {
        givenSubrogation(SubrogationStatusEnum.CREATED, inOneHour());

        assertThatThrownBy(() -> casService.validateSubrogation(request())).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("une subrogation expirée est refusée, sans attendre la purge de Mongo")
    void expiredSubrogationIsRefused() {
        // L'index TTL ne passe qu'une fois par minute, et certains déploiements le désactivent.
        givenSubrogation(SubrogationStatusEnum.ACCEPTED, anHourAgo());

        assertThatThrownBy(() -> casService.validateSubrogation(request()))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("une subrogation dont un des comptes a disparu est refusée")
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
