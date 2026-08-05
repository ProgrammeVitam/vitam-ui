package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesRequestDto;
import fr.gouv.vitamui.iam.auth.contract.UserPrincipalAttributes;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.idp.domain.IdentityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

/**
 * La forme des attributs d'authentification.
 *
 * Ces cas ne décrivent pas seulement ce que porte la table, mais sous quelle forme. C'est ce qui compte
 * pour l'iso-fonctionnel : les applications relisent ces attributs avec
 * {@code Boolean.parseBoolean((String) value)}, {@code OffsetDateTime.parse((String) value)} ou un
 * {@code parseJson} qui ne fait rien si la valeur n'est pas une chaîne. Une valeur laissée sous forme de
 * booléen ou d'objet ne provoquerait pas une erreur visible ici, mais un échec de lecture chez le
 * consommateur.
 */
@ExtendWith(MockitoExtension.class)
class CasServicePrincipalAttributesTest {

    private static final String EMAIL = "jean.dupont@organisation-a.fr";
    private static final String CUSTOMER_ID = "customerA";

    @InjectMocks
    private CasService casService;

    @Mock
    private IdentityProviderRepository identityProviderRepository;

    @Nested
    @DisplayName("Toute valeur est transmise comme une chaîne")
    class EverythingIsAString {

        @Test
        @DisplayName("un booléen devient sa représentation textuelle, pas un booléen")
        void booleansBecomeText() {
            final UserDto user = user();
            user.setOtp(true);
            user.setSubrogeable(false);

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(CommonConstants.OTP_ATTRIBUTE)).containsExactly("true");
            assertThat(attributes.get(CommonConstants.SUBROGEABLE_ATTRIBUTE)).containsExactly("false");
        }

        @Test
        @DisplayName("un entier et une date deviennent du texte relisible par le consommateur")
        void numbersAndDatesBecomeText() {
            final OffsetDateTime lastConnection = OffsetDateTime.parse("2026-01-15T10:30:00Z");
            final UserDto user = user();
            user.setNbFailedAttempts(3);
            user.setLastConnection(lastConnection);

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(CommonConstants.NB_FAILED_ATTEMPTS_ATTRIBUTE)).containsExactly("3");
            assertThat(attributes.get(CommonConstants.LAST_CONNECTION_ATTRIBUTE)).containsExactly(
                lastConnection.toString()
            );
            // La forme doit survivre à la relecture que fait AuthUserDto.
            assertThat(
                OffsetDateTime.parse(attributes.get(CommonConstants.LAST_CONNECTION_ATTRIBUTE).getFirst())
            ).isEqualTo(lastConnection);
        }

        @Test
        @DisplayName("une énumération devient son nom")
        void enumsBecomeTheirName() {
            final UserDto user = user();
            user.setStatus(UserStatusEnum.ENABLED);
            user.setType(UserTypeEnum.NOMINATIVE);

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(CommonConstants.STATUS_ATTRIBUTE)).containsExactly("ENABLED");
            assertThat(attributes.get(CommonConstants.TYPE_ATTRIBUTE)).containsExactly("NOMINATIVE");
        }

        @Test
        @DisplayName("un objet devient du JSON, que le consommateur sait reparser")
        void compositesBecomeJson() {
            final AddressDto address = new AddressDto();
            address.setStreet("1 rue de la Paix");
            address.setCity("Paris");
            final UserDto user = user();
            user.setAddress(address);

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(CommonConstants.ADDRESS_ATTRIBUTE).getFirst())
                .contains("\"street\":\"1 rue de la Paix\"")
                .contains("\"city\":\"Paris\"");
        }
    }

    @Nested
    @DisplayName("Attributs absents")
    class MissingValues {

        @Test
        @DisplayName("une valeur nulle est omise plutôt que portée à la chaîne \"null\"")
        void nullValuesAreOmitted() {
            final UserDto user = user();
            user.setPhone(null);
            user.setAddress(null);

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes).doesNotContainKey(CommonConstants.PHONE_ATTRIBUTE);
            assertThat(attributes).doesNotContainKey(CommonConstants.ADDRESS_ATTRIBUTE);
        }
    }

    @Nested
    @DisplayName("Authentification renforcée")
    class ComputedOtp {

        @Test
        @DisplayName("elle s'applique quand l'utilisateur l'a activée et se connecte par mot de passe")
        void appliesOnInternalProvider() {
            final UserDto user = user();
            user.setOtp(true);
            givenProviders(internalProvider());

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(UserPrincipalAttributes.COMPUTED_OTP)).containsExactly("true");
        }

        @Test
        @DisplayName("elle ne s'applique pas en authentification déléguée, où l'IdP décide")
        void doesNotApplyOnExternalProvider() {
            final UserDto user = user();
            user.setOtp(true);
            givenProviders(externalProvider());

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(UserPrincipalAttributes.COMPUTED_OTP)).containsExactly("false");
        }

        @Test
        @DisplayName("elle ne s'applique pas si l'utilisateur ne l'a pas activée")
        void doesNotApplyWhenDisabledOnTheUser() {
            final UserDto user = user();
            user.setOtp(false);

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(UserPrincipalAttributes.COMPUTED_OTP)).containsExactly("false");
        }
    }

    private void givenProviders(final IdentityProvider... providers) {
        lenient().when(identityProviderRepository.findAll()).thenReturn(List.of(providers));
    }

    private static PrincipalAttributesRequestDto request() {
        final PrincipalAttributesRequestDto request = new PrincipalAttributesRequestDto();
        request.setLoginEmail(EMAIL);
        request.setLoginCustomerId(CUSTOMER_ID);
        return request;
    }

    private static UserDto user() {
        final UserDto user = new UserDto();
        user.setId("userId");
        user.setCustomerId(CUSTOMER_ID);
        user.setEmail(EMAIL);
        user.setFirstname("Jean");
        user.setLastname("Dupont");
        return user;
    }

    private static IdentityProvider internalProvider() {
        final IdentityProvider provider = new IdentityProvider();
        provider.setId("idpA");
        provider.setCustomerId(CUSTOMER_ID);
        provider.setInternal(true);
        provider.setEnabled(true);
        provider.setPatterns(List.of(".*@organisation-a\\.fr"));
        return provider;
    }

    private static IdentityProvider externalProvider() {
        final IdentityProvider provider = internalProvider();
        provider.setInternal(false);
        return provider;
    }
}
