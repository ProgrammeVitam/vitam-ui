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
 * The shape of the authentication attributes.
 *
 * These cases describe not only what the map carries, but in which form. That is what keeps the
 * behaviour identical: applications read these attributes back with
 * {@code Boolean.parseBoolean((String) value)}, {@code OffsetDateTime.parse((String) value)} or a
 * {@code parseJson} that does nothing unless the value is a string. A value left as a boolean or as an
 * object would raise no visible error here, but would fail to be read by the consumer.
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
    @DisplayName("Every value is carried as a string")
    class EverythingIsAString {

        @Test
        @DisplayName("a boolean becomes its textual representation, not a boolean")
        void booleansBecomeText() {
            final UserDto user = user();
            user.setOtp(true);
            user.setSubrogeable(false);

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(CommonConstants.OTP_ATTRIBUTE)).containsExactly("true");
            assertThat(attributes.get(CommonConstants.SUBROGEABLE_ATTRIBUTE)).containsExactly("false");
        }

        @Test
        @DisplayName("an integer and a date become text the consumer can read back")
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
            // The shape must survive the round trip AuthUserDto performs.
            assertThat(
                OffsetDateTime.parse(attributes.get(CommonConstants.LAST_CONNECTION_ATTRIBUTE).getFirst())
            ).isEqualTo(lastConnection);
        }

        @Test
        @DisplayName("an enum becomes its name")
        void enumsBecomeTheirName() {
            final UserDto user = user();
            user.setStatus(UserStatusEnum.ENABLED);
            user.setType(UserTypeEnum.NOMINATIVE);

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(CommonConstants.STATUS_ATTRIBUTE)).containsExactly("ENABLED");
            assertThat(attributes.get(CommonConstants.TYPE_ATTRIBUTE)).containsExactly("NOMINATIVE");
        }

        @Test
        @DisplayName("an object becomes JSON, which the consumer knows how to parse back")
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
    @DisplayName("Missing attributes")
    class MissingValues {

        @Test
        @DisplayName("a null value is omitted rather than carried as the string \"null\"")
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
    @DisplayName("Strong authentication")
    class ComputedOtp {

        @Test
        @DisplayName("it applies when the user enabled it and signs in with a password")
        void appliesOnInternalProvider() {
            final UserDto user = user();
            user.setOtp(true);
            givenProviders(internalProvider());

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(UserPrincipalAttributes.COMPUTED_OTP)).containsExactly("true");
        }

        @Test
        @DisplayName("it does not apply on a delegated authentication, where the IdP decides")
        void doesNotApplyOnExternalProvider() {
            final UserDto user = user();
            user.setOtp(true);
            givenProviders(externalProvider());

            final Map<String, List<String>> attributes = casService.toPrincipalAttributes(user, request(), null);

            assertThat(attributes.get(UserPrincipalAttributes.COMPUTED_OTP)).containsExactly("false");
        }

        @Test
        @DisplayName("it does not apply when the user did not enable it")
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
