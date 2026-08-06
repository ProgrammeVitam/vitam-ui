package fr.gouv.vitamui.iam.server.cas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.auth.contract.HrdEntryDto;
import fr.gouv.vitamui.iam.server.config.ConverterConfig;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.logbook.config.LogbookConfiguration;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Home Realm Discovery confronted with the reference data, in a real MongoDB.
 *
 * The neighbouring unit cases check the rule against providers built for it. These ones confront it with
 * the data set shipped with the product, where patterns overlap, where a single address carries accounts
 * in two customers, and where identifiers are not all of the same type. Two divergences from the historic
 * webflow were only visible this way: an internal provider was added alongside a delegation for the same
 * customer, and an unknown address on an internal domain resolved to nothing, which disclosed the absence
 * of an account.
 *
 * The data set is an extract of the development database, stripped of keystores, metadata and client
 * secrets: HRD reads none of them.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({ ConverterConfig.class, LogbookConfiguration.class, VitamClientTestConfig.class })
class CasServiceHrdIntegrationTest extends AbstractMongoTests {

    private static final String SYSTEM_CUSTOMER_CODE = "000000";
    private static final String CLIENT1_CODE = "654852";
    private static final String CLIENT2_CODE = "659845";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdentityProviderRepository identityProviderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private CasService casService;

    @BeforeEach
    void loadReferenceData() throws IOException {
        mongoTemplate.getCollection("providers").drop();
        mongoTemplate.getCollection("users").drop();
        mongoTemplate.getCollection("customers").drop();
        insert("hrd/providers.json", "providers");
        insert("hrd/users.json", "users");
        insert("hrd/customers.json", "customers");

        casService = new CasService();
        casService.setIdentityProviderRepository(identityProviderRepository);
        casService.setUserRepository(userRepository);
        casService.setCustomerRepository(customerRepository);
    }

    @SuppressWarnings("unchecked")
    private void insert(final String resource, final String collection) throws IOException {
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            final List<Map<String, Object>> documents = new ObjectMapper().readValue(in, List.class);
            documents.forEach(document -> mongoTemplate.getCollection(collection).insertOne(new Document(document)));
        }
    }

    @Nested
    @DisplayName("Existing accounts have the final say")
    class ExistingAccountsDecide {

        @Test
        @DisplayName("a single account points at its own customer, and only that one")
        void aSingleAccountResolvesToItsOwnCustomer() {
            // Client2's catch-all pattern ".*@change-it.fr" matches this address too, but no account
            // there carries it: attaching it to Client2 would be a false positive.
            final List<HrdEntryDto> entries = casService.resolveHrdEntries("admin@change-it.fr");

            assertThat(entries)
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getCustomerCode()).isEqualTo(SYSTEM_CUSTOMER_CODE);
                    assertThat(entry.getIdentityProviderId()).isEqualTo("system_idp");
                    assertThat(entry.isInternal()).isTrue();
                    assertThat(entry.getUserStatus()).isEqualTo("ENABLED");
                });
        }

        @Test
        @DisplayName("an address carried by two accounts offers both customers, sorted by code")
        void twoAccountsResolveToTwoCustomers() {
            // The case the webflow handles with its customer selection page.
            final List<HrdEntryDto> entries = casService.resolveHrdEntries("demo@change-it.fr");

            assertThat(entries)
                .extracting(HrdEntryDto::getCustomerCode)
                .containsExactly(SYSTEM_CUSTOMER_CODE, CLIENT2_CODE);
            assertThat(entries)
                .extracting(HrdEntryDto::getIdentityProviderId)
                .containsExactly("system_idp", "5c7928337884583d1ebb6ebd3b672beb39d04beda4c55d70d5352184d926ed31");
        }

        @Test
        @DisplayName("the case of the address makes no difference")
        void emailCaseIsIgnored() {
            assertThat(casService.resolveHrdEntries("ADMIN@GMAIL.COM")).isEqualTo(
                casService.resolveHrdEntries("admin@gmail.com")
            );
            assertThat(casService.resolveHrdEntries("ADMIN@GMAIL.COM"))
                .singleElement()
                .extracting(HrdEntryDto::getCustomerCode)
                .isEqualTo(CLIENT1_CODE);
        }
    }

    @Nested
    @DisplayName("A customer is offered only once")
    class OneEntryPerCustomer {

        @Test
        @DisplayName("a federated account is not also offered its customer's password")
        void aFederatedAccountIsNotAlsoOfferedTheInternalProvider() {
            // Client1 carries an internal provider and two delegations. The webflow keeps the first
            // provider of the customer whose pattern matches: here the OIDC delegation alone, the
            // internal provider only covering ".*@gmail.com".
            final List<HrdEntryDto> entries = casService.resolveHrdEntries("demo.oidc@keycloak-oidc.fr");

            assertThat(entries)
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getCustomerCode()).isEqualTo(CLIENT1_CODE);
                    assertThat(entry.getIdentityProviderId()).isEqualTo("keycloak_test_idp_oidc");
                    assertThat(entry.isInternal()).isFalse();
                    assertThat(entry.getProtocoleType()).isEqualTo("OIDC");
                });
        }

        @Test
        @DisplayName("the protocol of the delegation kept is carried over as-is")
        void theDelegationProtocolIsCarried() {
            final List<HrdEntryDto> entries = casService.resolveHrdEntries("demo.saml@keycloak-saml.fr");

            assertThat(entries)
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getIdentityProviderId()).isEqualTo("keycloak_test_idp_saml");
                    assertThat(entry.getProtocoleType()).isEqualTo("SAML");
                });
        }
    }

    @Nested
    @DisplayName("Non-disclosure of account existence")
    class AccountExistenceIsNotDisclosed {

        @Test
        @DisplayName("an unknown address on an internal domain is routed like a known one")
        void anUnknownAddressOnAnInternalDomainIsStillRouted() {
            // With no account, patterns alone decide. Returning an empty list here would tell a missing
            // account apart from an existing one before any password is entered.
            final List<HrdEntryDto> unknown = casService.resolveHrdEntries("inconnu@change-it.fr");
            final List<HrdEntryDto> known = casService.resolveHrdEntries("admin@client2.fr");

            assertThat(unknown)
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getCustomerCode()).isEqualTo(CLIENT2_CODE);
                    assertThat(entry.isInternal()).isTrue();
                });
            // Same customer, same provider: nothing in the routing betrays the absence of an account.
            assertThat(unknown.getFirst().getCustomerCode()).isEqualTo(known.getFirst().getCustomerCode());
            assertThat(unknown.getFirst().getIdentityProviderId()).isEqualTo(known.getFirst().getIdentityProviderId());
        }

        @Test
        @DisplayName("an unknown address on a delegated domain is routed to that delegation")
        void anUnknownAddressOnADelegatedDomainIsRouted() {
            // Just-in-time provisioning depends on it: the account does not exist yet.
            final List<HrdEntryDto> entries = casService.resolveHrdEntries("inconnu@keycloak-oidc.fr");

            assertThat(entries)
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getIdentityProviderId()).isEqualTo("keycloak_test_idp_oidc");
                    assertThat(entry.getUserStatus()).isNull();
                });
        }

        @Test
        @DisplayName("an address no pattern covers resolves to nothing")
        void anAddressMatchedByNoPatternResolvesNothing() {
            assertThat(casService.resolveHrdEntries("inconnu@nowhere.xyz")).isEmpty();
        }
    }
}
