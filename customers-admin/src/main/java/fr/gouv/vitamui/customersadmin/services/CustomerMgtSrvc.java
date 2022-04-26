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
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.customersadmin.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Service
public class CustomerMgtSrvc {

    protected static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerMgtSrvc.class);

    protected static final String GENERIC_CERTIFICATE = "generic-it";
    public static final String ADMIN_USER = "admin_user";
    public static final String TOKEN_USER_ADMIN = "tokenadmin";
    protected static final String TESTS_CONTEXT_ID = "integration-tests_context";
    private MongoCollection<Document> tokensCollection;
    private MongoDatabase iamDatabase;
    private MongoCollection<Document> usersCollection;
    private MongoDatabase securityDatabase;
    private static MongoClient mongoClientSecurity;

    private MongoCollection<Document> contextsCollection;

    private MongoCollection<Document> certificatesCollection;
    protected static final String TESTS_CERTIFICATE_ID = "integration-tests_cert";

    private MongoCollection<Document> profilesCollection;

    private static MongoClient mongoClientIam;

    protected static final String TESTS_USER_ADMIN = "tokenadmin";

    protected static final String SYSTEM_USER_ID = "admin_user";

    public static final String ADMIN_USER_GROUP = "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363";

    @Value("${jks-password}")
    private String jksPassword;

    @Value("classpath:data/customers.json")
    private Resource customersFile;

    @Value("${vitamui_platform_informations.proof_tenant}")
    protected int proofTenantIdentifier;


    @Value("${generic-cert}")
    private String genericCert;

    @Value("${mongo.security.uri}")
    private String mongoSecurityUri;

    @Value("${iam-client.ssl.truststore.password}")
    protected String iamTruststorePassword;

    @Value("${certs-folder}")
    protected String certsFolder;

    @Value("${iam-client.host}")
    protected String iamServerHost;

    @Value("${iam-client.port}")
    protected Integer iamServerPort;

    @Value("${iam-client.ssl.keystore.path}")
    private String iamKeystoreFilePath;

    @Value("${iam-client.ssl.truststore.path}")
    protected String iamTrustStoreFilePath;

    @Value("${iam-client.ssl.keystore.password}")
    protected String iamKeystorePassword;
    @Autowired
    protected WebClient.Builder webClientBuilder;

    @Value("${mongo.iam.uri}")
    private String mongoIamUri;



    protected ExternalHttpContext getSystemTenantUserAdminContext() {
        buildSystemTenantUserAdminContext();
        return new ExternalHttpContext(proofTenantIdentifier, TOKEN_USER_ADMIN, TESTS_CONTEXT_ID,
            "admincaller",
            "requestId");

    }

    private void buildSystemTenantUserAdminContext() {
        getUsersCollection().updateOne(new BsonDocument("_id", new BsonString(ADMIN_USER)),
            new BsonDocument("$set", new BsonDocument("groupId", new BsonString(ADMIN_USER_GROUP))));
        tokenUserAdmin();
    }

    public CustomerDto create(final ExternalHttpContext context, final CustomerDto customerDto,
        final Optional<Path> logoPath) {
        final IamExternalWebClientFactory
            iamExternalWebClientFactory =
            getIamWebClientFactory(true, null, new String[] {ServicesData.ROLE_CREATE_CUSTOMERS});

        LOGGER.debug("Create {} with logo : {}", customerDto, logoPath);

        return iamExternalWebClientFactory.getCustomerWebClient().create(context, customerDto, logoPath);
    }

    protected IamExternalWebClientFactory getIamWebClientFactory(final String keystorePrefix) {
        final IamExternalWebClientFactory webClientFactory =
            new IamExternalWebClientFactory(
                getRestClientConfiguration(iamServerHost, iamServerPort, true,
                    getSSLConfiguration(certsFolder + keystorePrefix + ".jks",
                        iamKeystorePassword, iamTrustStoreFilePath,
                        iamTruststorePassword)), webClientBuilder);
        return webClientFactory;
    }

    protected IamExternalWebClientFactory getIamWebClientFactory(final boolean fullAccess, final Integer[] tenants,
        final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        final IamExternalWebClientFactory restClientFactory =
            new IamExternalWebClientFactory(
                getRestClientConfiguration(iamServerHost, iamServerPort, true,
                    getSSLConfiguration(certsFolder + GENERIC_CERTIFICATE + ".jks",
                        iamKeystorePassword,
                        iamTrustStoreFilePath, iamTruststorePassword)),
                webClientBuilder);
        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        //interceptors.add(new RegisterRestQueryInterceptor());
        return restClientFactory;
    }

    protected void tokenUserAdmin() {
        writeToken(TESTS_USER_ADMIN, SYSTEM_USER_ID);
    }

    protected SSLConfiguration getSSLConfiguration(final String keystorePathname, final String iamKeystorePassword,
        final String trustStorePathname,
        final String iamTruststorePassword) {
        final String keystorePath = getClass().getClassLoader().getResource(keystorePathname).getPath();
        final String trustStorePath = getClass().getClassLoader().getResource(trustStorePathname).getPath();
        final SSLConfiguration.CertificateStoreConfiguration keyStore =
            new SSLConfiguration.CertificateStoreConfiguration();
        keyStore.setKeyPath(keystorePath);
        keyStore.setKeyPassword(iamKeystorePassword);
        keyStore.setType("JKS");
        final SSLConfiguration.CertificateStoreConfiguration trustStore =
            new SSLConfiguration.CertificateStoreConfiguration();
        trustStore.setKeyPath(trustStorePath);
        trustStore.setKeyPassword(iamTruststorePassword);
        trustStore.setType("JKS");

        final SSLConfiguration sslConfig = new SSLConfiguration();
        sslConfig.setKeystore(keyStore);
        sslConfig.setTruststore(trustStore);

        return sslConfig;
    }

    protected RestClientConfiguration getRestClientConfiguration(final String host, final int port,
        final boolean secure, final SSLConfiguration sslConfig) {
        final RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setServerHost(host);
        restClientConfiguration.setServerPort(port);
        restClientConfiguration.setSecure(secure);
        if (sslConfig != null) {
            restClientConfiguration.setSslConfiguration(sslConfig);
        }

        return restClientConfiguration;
    }

    protected void writeToken(final String id, final String userId) {
        getTokensCollection().deleteOne(eq("_id", id));
        final Document token =
            new Document("_id", id).append("updatedDate", DateUtils.addDays(new Date(), -10)).append("refId", userId);
        getTokensCollection().insertOne(token);
    }

    protected MongoCollection<Document> getTokensCollection() {
        if (tokensCollection == null) {
            tokensCollection = getIamDatabase().getCollection("tokens");
        }
        return tokensCollection;
    }

    protected MongoClient getMongoIam() {
        if (mongoClientIam == null) {
            mongoClientIam = MongoClients.create(mongoIamUri);
        }
        return mongoClientIam;
    }

    protected MongoDatabase getIamDatabase() {
        if (iamDatabase == null) {
            iamDatabase = getMongoIam().getDatabase("iam");
        }
        return iamDatabase;
    }

    protected MongoCollection<Document> getUsersCollection() {
        if (usersCollection == null) {
            usersCollection = getIamDatabase().getCollection("users");
        }
        return usersCollection;
    }

    protected MongoDatabase getSecurityDatabase() {
        if (securityDatabase == null) {
            securityDatabase = getMongoSecurity().getDatabase("security");
        }
        return securityDatabase;
    }

    protected MongoClient getMongoSecurity() {
        if (mongoClientSecurity == null) {
            mongoClientSecurity = MongoClients.create(mongoSecurityUri);
        }
        return mongoClientSecurity;
    }

    protected MongoCollection<Document> getCertificatesCollection() {
        if (certificatesCollection == null) {
            certificatesCollection = getSecurityDatabase().getCollection("certificates");
        }
        return certificatesCollection;
    }

    protected MongoCollection<Document> getContextsCollection() {
        if (contextsCollection == null) {
            contextsCollection = getSecurityDatabase().getCollection("contexts");
        }
        return contextsCollection;
    }

    protected void prepareGenericContext(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        // recreate generic context
        getContextsCollection().deleteOne(eq("_id", TESTS_CONTEXT_ID));
        //@formatter:off
        final Document itContext = new Document("_id", TESTS_CONTEXT_ID)
            .append("name", "" + new Date())
            .append("fullAccess", fullAccess)
            .append("roleNames", Arrays.asList(roles));
        //@formatter:on
        if (tenants != null) {
            itContext.append("tenants", Arrays.asList(tenants));
        } else {
            itContext.append("tenants", Arrays.asList(new Integer[] {-1}));
        }
        getContextsCollection().insertOne(itContext);

        // recreate generic certificate
        getCertificatesCollection().deleteOne(eq("_id", TESTS_CERTIFICATE_ID));
        //@formatter:off
        try {
            final String certificate =
                getCertificate("JKS", genericCert, jksPassword.toCharArray());

            final Document itCertificate = new Document("_id", TESTS_CERTIFICATE_ID)
                .append("contextId", TESTS_CONTEXT_ID)
                .append("subjectDN", "subjectDN")
                .append("issuerDN", "issuerDN")
                .append("serialNumber", "serialNumberAdmin")
                .append("data", certificate);
            getCertificatesCollection().insertOne(itCertificate);

        } catch (final Exception e) {
            LOGGER.error("Retrieving generic certificate failed [cert={}, password:{}, exception :{}]",
                genericCert,
                jksPassword, e);
        }
        //@formatter:on
    }

    private String getCertificate(final String type, final String filename, final char[] password)
        throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        String result = "";
        final KeyStore keyStore = KeyStore.getInstance(type);
        final File key = new ClassPathResource(filename).getFile();
        try (InputStream in = new FileInputStream(key)) {
            keyStore.load(in, password);
        }
        final Enumeration<?> enumeration = keyStore.aliases();
        while (enumeration.hasMoreElements()) {
            final String alias = (String) enumeration.nextElement();
            final Certificate certificate = keyStore.getCertificate(alias);
            final byte[] encodedCertKey = certificate.getEncoded();
            result = Base64.getEncoder().encodeToString(encodedCertKey);
        }

        return result;
    }

    public void createCustomers() throws IOException {
        //read json file
        List<CustomerDto> customersListToCreate = readFromCustomersFile();
        if (customersListToCreate != null) {
            for (CustomerDto customerDto : customersListToCreate) {
                create(getSystemTenantUserAdminContext(), customerDto, Optional.empty());
            }
        }
    }

    /**
     * @return
     */
    private List<CustomerDto> readFromCustomersFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<CustomerDto> customerList =
            mapper.readValue(customersFile.getFile(), new TypeReference<List<CustomerDto>>() {
            });
        return customerList;
    }
}
