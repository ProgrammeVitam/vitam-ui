package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesRequestDto;
import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesResponseDto;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.idp.domain.IdentityProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

/**
 * The shape of the authentication attributes the IAM hands to the authentication server.
 *
 * The response keeps the types the token needs: scalars stay typed, the two enumerations travel as
 * their name (which is what they serialize to) and the composite attributes travel as their
 * already-serialized JSON. That is what lets the authentication server rebuild the exact same
 * principal without depending on the administration model.
 */
@ExtendWith(MockitoExtension.class)
class CasServicePrincipalAttributesTest {

    private static final String EMAIL = "jean.dupont@organisation-a.fr";
    private static final String CUSTOMER_ID = "customerA";

    @InjectMocks
    private CasService casService;

    @Mock
    private IdentityProviderRepository identityProviderRepository;

    @Test
    void booleansStayBooleans() {
        final UserDto user = user();
        user.setOtp(true);
        user.setSubrogeable(false);

        final PrincipalAttributesResponseDto attributes = casService.toPrincipalAttributes(user, request(), null);

        assertThat(attributes.isOtp()).isTrue();
        assertThat(attributes.isSubrogeable()).isFalse();
    }

    @Test
    void numbersAndDatesStayTyped() {
        final OffsetDateTime lastConnection = OffsetDateTime.parse("2026-01-15T10:30:00Z");
        final UserDto user = user();
        user.setNbFailedAttempts(3);
        user.setLastConnection(lastConnection);

        final PrincipalAttributesResponseDto attributes = casService.toPrincipalAttributes(user, request(), null);

        assertThat(attributes.getNbFailedAttempts()).isEqualTo(3);
        assertThat(attributes.getLastConnection()).isEqualTo(lastConnection);
    }

    @Test
    void enumsTravelAsTheirName() {
        final UserDto user = user();
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);

        final PrincipalAttributesResponseDto attributes = casService.toPrincipalAttributes(user, request(), null);

        assertThat(attributes.getStatus()).isEqualTo("ENABLED");
        assertThat(attributes.getType()).isEqualTo("NOMINATIVE");
    }

    @Test
    void compositesTravelAsJson() {
        final AddressDto address = new AddressDto();
        address.setStreet("1 rue de la Paix");
        address.setCity("Paris");
        final UserDto user = user();
        user.setAddress(address);

        final PrincipalAttributesResponseDto attributes = casService.toPrincipalAttributes(user, request(), null);

        assertThat(attributes.getAddressJson())
            .contains("\"street\":\"1 rue de la Paix\"")
            .contains("\"city\":\"Paris\"");
    }

    @Test
    void nullValuesAreCarried() {
        final UserDto user = user();
        user.setPhone(null);
        user.setAddress(null);

        final PrincipalAttributesResponseDto attributes = casService.toPrincipalAttributes(user, request(), null);

        assertThat(attributes.getPhone()).isNull();
        assertThat(attributes.getAddressJson()).isEqualTo("null");
    }

    @Test
    void computedOtpAppliesOnInternalProvider() {
        final UserDto user = user();
        user.setOtp(true);
        givenProviders(internalProvider());

        final PrincipalAttributesResponseDto attributes = casService.toPrincipalAttributes(user, request(), null);

        assertThat(attributes.isComputedOtp()).isTrue();
    }

    @Test
    void computedOtpDoesNotApplyOnExternalProvider() {
        final UserDto user = user();
        user.setOtp(true);
        givenProviders(externalProvider());

        final PrincipalAttributesResponseDto attributes = casService.toPrincipalAttributes(user, request(), null);

        assertThat(attributes.isComputedOtp()).isFalse();
    }

    @Test
    void computedOtpDoesNotApplyWhenDisabledOnTheUser() {
        final UserDto user = user();
        user.setOtp(false);

        final PrincipalAttributesResponseDto attributes = casService.toPrincipalAttributes(user, request(), null);

        assertThat(attributes.isComputedOtp()).isFalse();
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
