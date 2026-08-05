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
 * Table de vérité du Home Realm Discovery.
 *
 * Ces cas décrivent le comportement que le serveur d'authentification produit aujourd'hui dans son
 * webflow, réparti entre {@code ListCustomersAction} (combien d'organisations proposer) et
 * {@code DispatcherAction} (mot de passe, délégation, ou refus). Les porter ici fige ce comportement
 * avant que la décision ne se déplace, et permet de démontrer qu'elle n'a pas changé.
 *
 * Correspondance avec les tests d'origine :
 * <ul>
 *   <li>{@code testLoginWithEmailMatchingASingleUser} → une entrée, l'organisation est déjà choisie</li>
 *   <li>{@code testLoginWithEmailMatchingMultipleUsers} → plusieurs entrées, il faut faire choisir</li>
 *   <li>{@code testLoginWithUnknownUserMatchingASingleCustomerMailDomain} → une entrée sans compte</li>
 *   <li>{@code testLoginWithUnknownUserMatchingMultipleCustomerMailDomain} → plusieurs entrées sans compte</li>
 *   <li>{@code testLoginWithUnknownUserMatchingNoValidCustomerMailDomain} → aucune entrée</li>
 *   <li>{@code testInternalAuthnOK} / {@code testExternal} → {@code internal} tranche le parcours</li>
 *   <li>{@code testInternalAuthnDisabled} / {@code testExternalDisabled} → {@code userStatus} refuse</li>
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
    @DisplayName("Cardinalité : combien d'organisations proposer")
    class Cardinality {

        @Test
        @DisplayName("un compte unique donne une entrée, l'organisation est déjà déterminée")
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
        @DisplayName("des comptes dans plusieurs organisations imposent un choix")
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
        @DisplayName("aucun fournisseur correspondant ne donne aucune entrée")
        void noMatch() {
            givenProviders(externalProvider("idpC", "customerC", ".*@autre-domaine\\.fr"));
            givenUsers();

            assertThat(casService.resolveHrdEntries(EMAIL)).isEmpty();
        }

        @Test
        @DisplayName("les entrées sont triées par code d'organisation")
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
    @DisplayName("Non-divulgation : un compte inconnu ne se distingue pas d'un compte connu")
    class AccountExistenceDisclosure {

        @Test
        @DisplayName("un email sans compte reste résolu lorsqu'un fournisseur externe le couvre")
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
        @DisplayName("plusieurs fournisseurs externes couvrant l'email imposent un choix, sans compte")
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
    @DisplayName("Nature du parcours et statut du compte")
    class JourneyAndStatus {

        @Test
        @DisplayName("un fournisseur interne mène au mot de passe")
        void internalProviderLeadsToPassword() {
            givenProviders(internalProvider("idpA", CUSTOMER_A));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));

            assertThat(casService.resolveHrdEntries(EMAIL).getFirst().isInternal()).isTrue();
        }

        @Test
        @DisplayName("un fournisseur externe mène à la délégation")
        void externalProviderLeadsToDelegation() {
            givenProviders(externalProvider("idpA", CUSTOMER_A, ".*@organisation-a\\.fr"));
            givenUsers();

            final HrdEntryDto entry = casService.resolveHrdEntries(EMAIL).getFirst();
            assertThat(entry.isInternal()).isFalse();
            assertThat(entry.getProtocoleType()).isEqualTo("SAML");
        }

        @Test
        @DisplayName("un compte actif est signalé comme tel")
        void enabledUser() {
            givenProviders(internalProvider("idpA", CUSTOMER_A));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));

            assertThat(casService.resolveHrdEntries(EMAIL).getFirst().getUserStatus()).isEqualTo("ENABLED");
        }

        @Test
        @DisplayName("un compte désactivé est signalé, et c'est ce qui fera refuser la connexion")
        void disabledUser() {
            givenProviders(internalProvider("idpA", CUSTOMER_A));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.DISABLED));

            assertThat(casService.resolveHrdEntries(EMAIL).getFirst().getUserStatus()).isEqualTo("DISABLED");
        }
    }

    @Nested
    @DisplayName("Filtrage des fournisseurs")
    class ProviderFiltering {

        @Test
        @DisplayName("un fournisseur interne sans compte dans son organisation est écarté")
        void internalProviderWithoutUserIsDropped() {
            // Le pattern attrape-tout rattacherait l'adresse à une organisation où elle n'a pas de compte.
            givenProviders(internalProviderWithPattern("idpA", CUSTOMER_A, ".*@organisation-a\\.fr"));
            givenUsers();

            assertThat(casService.resolveHrdEntries(EMAIL)).isEmpty();
        }

        @Test
        @DisplayName("un fournisseur interne désactivé ne résout pas un compte existant")
        void disabledInternalProviderIsIgnored() {
            final IdentityProvider disabled = internalProvider("idpA", CUSTOMER_A);
            disabled.setEnabled(false);
            givenProviders(disabled);
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));

            assertThat(casService.resolveHrdEntries(EMAIL)).isEmpty();
        }

        @Test
        @DisplayName("un fournisseur retenu deux fois n'apparaît qu'une fois")
        void deduplicatesProviderMatchedTwice() {
            // Retenu par son pattern et par le compte existant de son organisation.
            givenProviders(internalProviderWithPattern("idpA", CUSTOMER_A, ".*@organisation-a\\.fr"));
            givenUsers(user(CUSTOMER_A, UserStatusEnum.ENABLED));

            assertThat(casService.resolveHrdEntries(EMAIL)).hasSize(1);
        }

        @Test
        @DisplayName("la correspondance de pattern ignore la casse")
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
        final IdentityProvider provider = internalProvider(id, customerId);
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
