package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.auth.contract.HrdEntryDto;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Truth table of Home Realm Discovery.
 *
 * These cases describe the behaviour the authentication server produces today in its webflow, split
 * between {@code ListCustomersAction} (how many customers to offer) and {@code DispatcherAction}
 * (password, delegation, or refusal). Bringing them here pins that behaviour down before the decision
 * moves, and makes it possible to show it has not changed.
 *
 * Mapping to the original tests:
 * <ul>
 *   <li>{@code testLoginWithEmailMatchingASingleUser} -> one entry, the customer is already settled</li>
 *   <li>{@code testLoginWithEmailMatchingMultipleUsers} -> several entries, a choice is needed</li>
 *   <li>{@code testLoginWithUnknownUserMatchingASingleCustomerMailDomain} -> one entry, no account</li>
 *   <li>{@code testLoginWithUnknownUserMatchingMultipleCustomerMailDomain} -> several entries, no account</li>
 *   <li>{@code testLoginWithUnknownUserMatchingNoValidCustomerMailDomain} -> no entry at all</li>
 *   <li>{@code testInternalAuthnOK} / {@code testExternal} -> {@code internal} settles the journey</li>
 *   <li>{@code testInternalAuthnDisabled} / {@code testExternalDisabled} -> {@code userStatus} refuses</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class CasServiceHrdTest {

    private static final String EMAIL = "jean.dupont@organisation-a.fr";

    private static final String CUSTOMER_A = "customerA";
    private static final String CUSTOMER_B = "customerB";

    @InjectMocks
    private CasService casService;

    @Mock
    private IdentityProviderRepository identityProviderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @BeforeEach
    void noCustomerByDefault() {
        lenient().when(customerRepository.findAllById(any())).thenReturn(List.of());
    }

    @Nested
    @DisplayName("Cardinality: how many customers to offer")
    class Cardinality {

        @Test
        @DisplayName("a single account gives one entry, the customer is already settled")
        void singleUser() {
            givenProviders(internalProvider("idpA", CUSTOMER_A));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));
            givenCustomers(customer(CUSTOMER_A, "AAA", "Organisation A"));

            final List<HrdEntryDto> entries = casService.resolveHrdEntries(EMAIL);

            assertThat(entries).hasSize(1);
            assertThat(entries.getFirst().getCustomerId()).isEqualTo(CUSTOMER_A);
            assertThat(entries.getFirst().getCustomerName()).isEqualTo("Organisation A");
        }

        @Test
        @DisplayName("accounts in several customers force a choice")
        void multipleUsers() {
            givenProviders(internalProvider("idpA", CUSTOMER_A), internalProvider("idpB", CUSTOMER_B));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED), user(CUSTOMER_B, UserStatusEnum.ENABLED));
            givenCustomers(
                customer(CUSTOMER_A, "AAA", "Organisation A"),
                customer(CUSTOMER_B, "BBB", "Organisation B")
            );

            final List<HrdEntryDto> entries = casService.resolveHrdEntries(EMAIL);

            assertThat(entries).hasSize(2);
            assertThat(entries).extracting(HrdEntryDto::getCustomerId).containsExactly(CUSTOMER_A, CUSTOMER_B);
        }

        @Test
        @DisplayName("no matching provider gives no entry")
        void noMatch() {
            givenProviders(externalProvider("idpC", "customerC", ".*@autre-domaine\\.fr"));
            givenUsers();

            assertThat(casService.resolveHrdEntries(EMAIL)).isEmpty();
        }

        @Test
        @DisplayName("entries are sorted by customer code")
        void sortedByCustomerCode() {
            givenProviders(internalProvider("idpA", CUSTOMER_A), internalProvider("idpB", CUSTOMER_B));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED), user(CUSTOMER_B, UserStatusEnum.ENABLED));
            givenCustomers(
                customer(CUSTOMER_A, "ZZZ", "Organisation A"),
                customer(CUSTOMER_B, "AAA", "Organisation B")
            );

            final List<HrdEntryDto> entries = casService.resolveHrdEntries(EMAIL);

            assertThat(entries).extracting(HrdEntryDto::getCustomerCode).containsExactly("AAA", "ZZZ");
        }
    }

    @Nested
    @DisplayName("Non-disclosure: an unknown account is not told apart from a known one")
    class AccountExistenceDisclosure {

        @Test
        @DisplayName("an email with no account is still resolved when an external provider covers it")
        void unknownUserOnExternalProvider() {
            givenProviders(externalProvider("idpA", CUSTOMER_A, ".*@organisation-a\\.fr"));
            givenUsers();
            givenCustomers(customer(CUSTOMER_A, "AAA", "Organisation A"));

            final List<HrdEntryDto> entries = casService.resolveHrdEntries(EMAIL);

            assertThat(entries).hasSize(1);
            assertThat(entries.getFirst().getUserStatus()).isNull();
            assertThat(entries.getFirst().getIdentityProviderId()).isEqualTo("idpA");
        }

        @Test
        @DisplayName("several external providers covering the email force a choice, with no account")
        void unknownUserOnSeveralExternalProviders() {
            givenProviders(
                externalProvider("idpA", CUSTOMER_A, ".*@organisation-a\\.fr"),
                externalProvider("idpB", CUSTOMER_B, ".*@organisation-a\\.fr")
            );
            givenUsers();
            givenCustomers(
                customer(CUSTOMER_A, "AAA", "Organisation A"),
                customer(CUSTOMER_B, "BBB", "Organisation B")
            );

            assertThat(casService.resolveHrdEntries(EMAIL)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Nature of the journey and status of the account")
    class JourneyAndStatus {

        @Test
        @DisplayName("an internal provider leads to the password")
        void internalProviderLeadsToPassword() {
            givenProviders(internalProvider("idpA", CUSTOMER_A));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));

            assertThat(casService.resolveHrdEntries(EMAIL).getFirst().isInternal()).isTrue();
        }

        @Test
        @DisplayName("an external provider leads to the delegation")
        void externalProviderLeadsToDelegation() {
            givenProviders(externalProvider("idpA", CUSTOMER_A, ".*@organisation-a\\.fr"));
            givenUsers();

            final HrdEntryDto entry = casService.resolveHrdEntries(EMAIL).getFirst();
            assertThat(entry.isInternal()).isFalse();
            assertThat(entry.getProtocoleType()).isEqualTo("SAML");
        }

        @Test
        @DisplayName("an active account is reported as such")
        void enabledUser() {
            givenProviders(internalProvider("idpA", CUSTOMER_A));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));

            assertThat(casService.resolveHrdEntries(EMAIL).getFirst().getUserStatus()).isEqualTo("ENABLED");
        }

        @Test
        @DisplayName("a disabled account is reported, and that is what will have the login refused")
        void disabledUser() {
            givenProviders(internalProvider("idpA", CUSTOMER_A));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.DISABLED));

            assertThat(casService.resolveHrdEntries(EMAIL).getFirst().getUserStatus()).isEqualTo("DISABLED");
        }
    }

    @Nested
    @DisplayName("Provider filtering")
    class ProviderFiltering {

        @Test
        @DisplayName("an internal provider with no account in its customer is still offered")
        void internalProviderWithoutUserIsStillOffered() {
            // Answering "nothing" would tell an unknown address apart from a known one before any
            // password is entered. The webflow routes both alike and lets the failure happen at
            // authentication, in a generic form.
            givenProviders(internalProviderWithPattern("idpA", CUSTOMER_A, ".*@organisation-a\\.fr"));
            givenUsers();

            assertThat(casService.resolveHrdEntries(EMAIL))
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getIdentityProviderId()).isEqualTo("idpA");
                    assertThat(entry.getUserStatus()).isNull();
                });
        }

        @Test
        @DisplayName("a disabled internal provider is still offered, just as in the webflow")
        void disabledInternalProviderIsStillOffered() {
            // An anomaly reproduced as-is: neither ProvidersService nor IdentityProviderHelper looks at
            // "enabled", so a disabled provider keeps being offered. Fixing it here alone would move IAM
            // away from the behaviour it has to reproduce.
            final IdentityProvider disabled = internalProvider("idpA", CUSTOMER_A);
            disabled.setEnabled(false);
            givenProviders(disabled);
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));

            assertThat(casService.resolveHrdEntries(EMAIL))
                .singleElement()
                .extracting(HrdEntryDto::getIdentityProviderId)
                .isEqualTo("idpA");
        }

        @Test
        @DisplayName("a provider matched twice appears only once")
        void deduplicatesProviderMatchedTwice() {
            // Matched both by its pattern and by the existing account of its customer.
            givenProviders(internalProviderWithPattern("idpA", CUSTOMER_A, ".*@organisation-a\\.fr"));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));

            assertThat(casService.resolveHrdEntries(EMAIL)).hasSize(1);
        }

        @Test
        @DisplayName("pattern matching ignores case")
        void patternMatchingIsCaseInsensitive() {
            givenProviders(externalProvider("idpA", CUSTOMER_A, ".*@ORGANISATION-A\\.FR"));
            givenUsers();

            assertThat(casService.resolveHrdEntries(EMAIL)).hasSize(1);
        }
    }

    private void givenProviders(final IdentityProvider... providers) {
        when(identityProviderRepository.findAll()).thenReturn(List.of(providers));
    }

    private void givenUsers(final User... users) {
        when(userRepository.findAllByEmailIgnoreCase(EMAIL)).thenReturn(List.of(users));
    }

    private void givenCustomers(final Customer... customers) {
        when(customerRepository.findAllById(any())).thenReturn(List.of(customers));
    }

    private static IdentityProvider internalProvider(final String id, final String customerId) {
        // A provider with no pattern is kept by neither of the webflow's two paths: building one this
        // way would describe an unreachable customer rather than a nominal case.
        return internalProviderWithPattern(id, customerId, ".*@organisation-a\\.fr");
    }

    private static IdentityProvider bareInternalProvider(final String id, final String customerId) {
        final IdentityProvider provider = new IdentityProvider();
        provider.setId(id);
        provider.setName("Fournisseur " + id);
        provider.setCustomerId(customerId);
        provider.setInternal(true);
        provider.setEnabled(true);
        return provider;
    }

    private static IdentityProvider internalProviderWithPattern(
        final String id,
        final String customerId,
        final String pattern
    ) {
        final IdentityProvider provider = bareInternalProvider(id, customerId);
        provider.setPatterns(List.of(pattern));
        return provider;
    }

    private static IdentityProvider externalProvider(final String id, final String customerId, final String pattern) {
        final IdentityProvider provider = new IdentityProvider();
        provider.setId(id);
        provider.setName("Fournisseur " + id);
        provider.setCustomerId(customerId);
        provider.setInternal(false);
        provider.setEnabled(true);
        provider.setPatterns(List.of(pattern));
        provider.setProtocoleType("SAML");
        return provider;
    }

    private static User user(final String customerId, final UserStatusEnum status) {
        final User user = new User();
        user.setEmail(EMAIL);
        user.setCustomerId(customerId);
        user.setStatus(status);
        return user;
    }

    private static Customer customer(final String id, final String code, final String name) {
        final Customer customer = new Customer();
        customer.setId(id);
        customer.setCode(code);
        customer.setName(name);
        return customer;
    }
}
