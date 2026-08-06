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
 * Le Home Realm Discovery confronté aux données de référence, dans un vrai MongoDB.
 *
 * Les cas unitaires voisins valident la règle sur des fournisseurs construits pour elle. Ceux-ci la
 * confrontent au jeu de données livré avec le produit, où les patterns se chevauchent, où une même adresse
 * porte des comptes dans deux organisations, et où les identifiants ne sont pas tous du même type. Deux
 * écarts avec le webflow historique n'étaient visibles que par ce chemin : un fournisseur interne
 * s'ajoutait à une délégation pour la même organisation, et une adresse inconnue sur un domaine interne ne
 * renvoyait rien, ce qui révélait l'absence de compte.
 *
 * Le jeu de données est un extrait de la base de développement, amputé des keystores, métadonnées et
 * secrets clients : le HRD n'en lit aucun.
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
    @DisplayName("Les comptes existants font autorité")
    class ExistingAccountsDecide {

        @Test
        @DisplayName("un compte unique désigne son organisation, et elle seule")
        void aSingleAccountResolvesToItsOwnCustomer() {
            // Le pattern attrape-tout « .*@change-it.fr » de Client2 correspond aussi à cette adresse, mais
            // aucun compte ne la porte chez Client2 : la rattacher là serait un faux positif.
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
        @DisplayName("une adresse portée par deux comptes propose les deux organisations, triées par code")
        void twoAccountsResolveToTwoCustomers() {
            // Le cas que le webflow traite par la page de sélection d'organisation.
            final List<HrdEntryDto> entries = casService.resolveHrdEntries("demo@change-it.fr");

            assertThat(entries)
                .extracting(HrdEntryDto::getCustomerCode)
                .containsExactly(SYSTEM_CUSTOMER_CODE, CLIENT2_CODE);
            assertThat(entries)
                .extracting(HrdEntryDto::getIdentityProviderId)
                .containsExactly("system_idp", "5c7928337884583d1ebb6ebd3b672beb39d04beda4c55d70d5352184d926ed31");
        }

        @Test
        @DisplayName("la casse de l'adresse est indifférente")
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
    @DisplayName("Une organisation n'est proposée qu'une fois")
    class OneEntryPerCustomer {

        @Test
        @DisplayName("un compte fédéré ne se voit pas aussi proposer le mot de passe de son organisation")
        void aFederatedAccountIsNotAlsoOfferedTheInternalProvider() {
            // Client1 porte un fournisseur interne et deux délégations. Le webflow retient le premier
            // fournisseur de l'organisation dont un pattern correspond : ici la délégation OIDC seule, le
            // fournisseur interne ne couvrant que « .*@gmail.com ».
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
        @DisplayName("le protocole de la délégation retenue est transmis tel quel")
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
    @DisplayName("Non-divulgation de l'existence d'un compte")
    class AccountExistenceIsNotDisclosed {

        @Test
        @DisplayName("une adresse inconnue sur un domaine interne est routée comme une adresse connue")
        void anUnknownAddressOnAnInternalDomainIsStillRouted() {
            // Sans compte, seuls les patterns décident. Renvoyer une liste vide ici distinguerait un compte
            // absent d'un compte présent avant toute saisie de mot de passe.
            final List<HrdEntryDto> unknown = casService.resolveHrdEntries("inconnu@change-it.fr");
            final List<HrdEntryDto> known = casService.resolveHrdEntries("admin@client2.fr");

            assertThat(unknown)
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getCustomerCode()).isEqualTo(CLIENT2_CODE);
                    assertThat(entry.isInternal()).isTrue();
                });
            // Même organisation, même fournisseur : rien dans le routage ne trahit l'absence de compte.
            assertThat(unknown.getFirst().getCustomerCode()).isEqualTo(known.getFirst().getCustomerCode());
            assertThat(unknown.getFirst().getIdentityProviderId()).isEqualTo(known.getFirst().getIdentityProviderId());
        }

        @Test
        @DisplayName("une adresse inconnue sur un domaine délégué est routée vers cette délégation")
        void anUnknownAddressOnADelegatedDomainIsRouted() {
            // Le provisionnement à la première connexion en dépend : le compte n'existe pas encore.
            final List<HrdEntryDto> entries = casService.resolveHrdEntries("inconnu@keycloak-oidc.fr");

            assertThat(entries)
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getIdentityProviderId()).isEqualTo("keycloak_test_idp_oidc");
                    assertThat(entry.getUserStatus()).isNull();
                });
        }

        @Test
        @DisplayName("une adresse qu'aucun pattern ne couvre ne résout rien")
        void anAddressMatchedByNoPatternResolvesNothing() {
            assertThat(casService.resolveHrdEntries("inconnu@nowhere.xyz")).isEmpty();
        }
    }
}
