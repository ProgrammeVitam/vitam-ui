package fr.gouv.vitamui.cucumber.common;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import fr.gouv.vitamui.RegisterRestQueryInterceptor;
import fr.gouv.vitamui.TestContextConfiguration;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import fr.gouv.vitamui.commons.rest.client.logbook.LogbookExternalRestClient;
import fr.gouv.vitamui.iam.external.client.ApplicationExternalRestClient;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import fr.gouv.vitamui.iam.external.client.CustomerExternalRestClient;
import fr.gouv.vitamui.iam.external.client.CustomerExternalWebClient;
import fr.gouv.vitamui.iam.external.client.ExternalParametersExternalRestClient;
import fr.gouv.vitamui.iam.external.client.GroupExternalRestClient;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import fr.gouv.vitamui.iam.external.client.IdentityProviderExternalRestClient;
import fr.gouv.vitamui.iam.external.client.OwnerExternalRestClient;
import fr.gouv.vitamui.iam.external.client.ProfileExternalRestClient;
import fr.gouv.vitamui.iam.external.client.*;
import fr.gouv.vitamui.referential.external.client.*;
import fr.gouv.vitamui.utils.TestConstants;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.mongodb.client.model.Filters.eq;

@ContextConfiguration(classes = TestContextConfiguration.class)
public abstract class BaseIntegration {

    protected static final String TESTS_CONTEXT_ID = "integration-tests_context";

    protected static final String TESTS_CERTIFICATE_ID = "integration-tests_cert";

    protected static final String GENERIC_CERTIFICATE = "generic-it";

    protected static final String TESTS_USER_ADMIN = "tokenadmin";

    protected static final String SYSTEM_USER_ID = "admin_user";

    protected static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(BaseIntegration.class);

    protected static final String TEST_USER_EMAIL = "testuser@test.com";

    public static final String ACCESS_CONTRACT = "AC-000001";

    public static final String UNIT_CONTRACT = "ContratTNR";

    public static final String ADMIN_USER = "admin_user";

    public static final String ADMIN_USER_GROUP = "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363";

    private IamExternalRestClientFactory restClientFactory;

    private IamExternalWebClientFactory iamExternalWebClientFactory;

    private ReferentialExternalRestClientFactory restReferentialClientFactory;

    private ReferentialExternalWebClientFactory webReferentialClientFactory;

    private CustomerExternalRestClient customerClient;

    private CustomerExternalWebClient customerWebClient;

    private AgencyExternalWebClient agencyWebClient;

    private OntologyExternalWebClient ontologyWebClient;

    private FileFormatExternalWebClient fileFormatWebClient;

    private IdentityProviderExternalRestClient identityProviderRestClient;

    private TenantExternalRestClient tenantRestClient;

    private UserExternalRestClient userRestClient;

    private GroupExternalRestClient groupRestClient;

    private ApplicationExternalRestClient applicationRestClient;

    private ProfileExternalRestClient profileRestClient;

    private CasExternalRestClient casRestClient;

    private SubrogationExternalRestClient subrogationRestClient;

    private OwnerExternalRestClient ownerRestClient;

    private ContextExternalRestClient contextRestClient;

    private RuleExternalRestClient ruleRestClient;
    
    private ExternalParametersExternalRestClient externalParametersExternalRestClient;

    private UnitExternalRestClient unitRestClient;

    private AccessContractExternalRestClient accessContractRestClient;

    private IngestContractExternalRestClient ingestContractRestClient;

    private SecurityProfileExternalRestClient securityProfileRestClient;

    private static MongoClient mongoClientIam;

    private static MongoClient mongoClientSecurity;

    private MongoDatabase iamDatabase;

    private MongoDatabase securityDatabase;

    private MongoCollection<Document> contextsCollection;

    private MongoCollection<Document> certificatesCollection;

    private MongoCollection<Document> profilesCollection;

    private MongoCollection<Document> groupsCollection;

    private MongoCollection<Document> usersCollection;

    private MongoCollection<Document> tokensCollection;

    private MongoCollection<Document> subrogationsCollection;

    @Value("${certs-folder}")
    protected String certsFolder;

    @Value("${generic-cert}")
    private String genericCert;

    @Value("${jks-password}")
    private String jksPassword;

    @Value("${mongo.iam.uri}")
    private String mongoIamUri;

    @Value("${mongo.security.uri}")
    private String mongoSecurityUri;

    @Value("${mongo.host}")
    private String mongoHost;

    @Value("${mongo.port}")
    private Integer mongoPort;

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

    @Value("${iam-client.ssl.truststore.password}")
    protected String iamTruststorePassword;

    @Value("${referential-client.host}")
    protected String referentialServerHost;

    @Value("${referential-client.port}")
    protected Integer referentialServerPort;

    @Value("${referential-client.ssl.keystore.path}")
    private String referentialKeystoreFilePath;

    @Value("${referential-client.ssl.truststore.path}")
    protected String referentialTrustStoreFilePath;

    @Value("${referential-client.ssl.keystore.password}")
    protected String referentialKeystorePassword;

    @Value("${referential-client.ssl.truststore.password}")
    protected String referentialTruststorePassword;

    @Value("${user.admin.password}")
    protected String adminPassword;

    @Value("${trace.timeOutInInSeconds:60000}")
    protected Long timeOutInSeconds;

    @Value("${trace.enabled:false}")
    protected Boolean traceEnabled;

    @Value("${flow.sleep.duration}")
    protected int FLOW_SLEEP_DURATION;

    @Value("${flow.timeout}")
    protected long FLOW_TIMEOUT;

    @Value("${vitamui_platform_informations.default_email_domain}")
    protected String defaultEmailDomain;

    @Value("${vitamui_platform_informations.system_archive_tenant_identifier}")
    protected int systemArchiveTenantIdentifier;

    @Value("${vitamui_platform_informations.proof_tenant}")
    protected int proofTenantIdentifier;

    @Value("${vitamui_platform_informations.cas_tenant}")
    protected int casTenantIdentifier;

    @Value("${vitamui_platform_informations.system_archive_tenant_identifier}")
    protected int client1TenantIdentifier;

    @Autowired
    protected RestTemplateBuilder restTemplateBuilder;

    @Autowired
    protected WebClient.Builder webClientBuilder;

    private void buildSystemTenantUserAdminContext() {
        getUsersCollection().updateOne(new BsonDocument("_id", new BsonString(ADMIN_USER)),
                new BsonDocument("$set", new BsonDocument("groupId", new BsonString(ADMIN_USER_GROUP))));
        tokenUserAdmin();
    }

    protected ExternalHttpContext getSystemTenantUserAdminContext() {
        buildSystemTenantUserAdminContext();
        return new ExternalHttpContext(proofTenantIdentifier, TestConstants.TOKEN_USER_ADMIN, TESTS_CONTEXT_ID, "admincaller", "requestId");

    }

    protected ExternalHttpContext getArchiveTenantUserAdminContext() {
        buildSystemTenantUserAdminContext();
        return new ExternalHttpContext(systemArchiveTenantIdentifier, TestConstants.TOKEN_USER_ADMIN, TESTS_CONTEXT_ID, "admincaller", "requestId",
                ACCESS_CONTRACT);
    }

    protected ExternalHttpContext getArchiveTenantUserAdminContext(final Integer tenantIdentifier) {
        buildSystemTenantUserAdminContext();
        return new ExternalHttpContext(tenantIdentifier, TestConstants.TOKEN_USER_ADMIN, TESTS_CONTEXT_ID, "admincaller", "requestId", ACCESS_CONTRACT);
    }

    protected ExternalHttpContext getUnitAdminContext() {
        buildSystemTenantUserAdminContext();
        return new ExternalHttpContext(proofTenantIdentifier, TestConstants.TOKEN_USER_ADMIN, TESTS_CONTEXT_ID, "admincaller", "requestId", UNIT_CONTRACT);
    }

    protected ExternalHttpContext getContext(final int tenant, final String user) {
        return new ExternalHttpContext(tenant, user, "appId", "identity");
    }

    protected SSLConfiguration getSSLConfiguration(final String keystorePathname, final String iamKeystorePassword, final String trustStorePathname,
            final String iamTruststorePassword) {
        final String keystorePath = getClass().getClassLoader().getResource(keystorePathname).getPath();
        final String trustStorePath = getClass().getClassLoader().getResource(trustStorePathname).getPath();
        final SSLConfiguration.CertificateStoreConfiguration keyStore = new SSLConfiguration.CertificateStoreConfiguration();
        keyStore.setKeyPath(keystorePath);
        keyStore.setKeyPassword(iamKeystorePassword);
        keyStore.setType("JKS");
        final SSLConfiguration.CertificateStoreConfiguration trustStore = new SSLConfiguration.CertificateStoreConfiguration();
        trustStore.setKeyPath(trustStorePath);
        trustStore.setKeyPassword(iamTruststorePassword);
        trustStore.setType("JKS");

        final SSLConfiguration sslConfig = new SSLConfiguration();
        sslConfig.setKeystore(keyStore);
        sslConfig.setTruststore(trustStore);

        return sslConfig;
    }

    protected RestClientConfiguration getRestClientConfiguration(final String host, final int port, final boolean secure, final SSLConfiguration sslConfig) {
        final RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setServerHost(host);
        restClientConfiguration.setServerPort(port);
        restClientConfiguration.setSecure(secure);
        if (sslConfig != null) {
            restClientConfiguration.setSslConfiguration(sslConfig);
        }

        return restClientConfiguration;
    }

    private IamExternalRestClientFactory getIamRestClientFactory() {
        if (restClientFactory == null) {
            LOGGER.debug("Instantiating iam rest client [host={}, port:{}, iamKeystoreFilePath:{}]", iamServerHost, iamServerPort, iamKeystoreFilePath);
            restClientFactory = new IamExternalRestClientFactory(getRestClientConfiguration(iamServerHost, iamServerPort, true,
                    getSSLConfiguration(iamKeystoreFilePath, iamKeystorePassword, iamTrustStoreFilePath, iamTruststorePassword)), restTemplateBuilder);
            final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            interceptors.add(new RegisterRestQueryInterceptor());
            restClientFactory.setRestClientInterceptor(interceptors);
        }
        return restClientFactory;
    }

    protected IamExternalRestClientFactory getIamRestClientFactory(final String keystorePrefix) {
        final IamExternalRestClientFactory restClientFactory = new IamExternalRestClientFactory(
                getRestClientConfiguration(iamServerHost, iamServerPort, true,
                        getSSLConfiguration(certsFolder + keystorePrefix + ".jks", iamKeystorePassword, iamTrustStoreFilePath, iamTruststorePassword)),
                restTemplateBuilder);
        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RegisterRestQueryInterceptor());
        restClientFactory.setRestClientInterceptor(interceptors);
        return restClientFactory;
    }

    private IamExternalWebClientFactory getIamExternalWebClientFactory() {
        if (iamExternalWebClientFactory == null) {
            LOGGER.debug("Instantiating IAM webclient [host={}, port:{}, iamKeystoreFilePath:{}]", iamServerHost, iamServerPort, iamKeystoreFilePath);
            iamExternalWebClientFactory = new IamExternalWebClientFactory(getRestClientConfiguration(iamServerHost, iamServerPort, true,
                    getSSLConfiguration(iamKeystoreFilePath, iamKeystorePassword, iamTrustStoreFilePath, iamTruststorePassword)), webClientBuilder);
        }
        return iamExternalWebClientFactory;
    }

    protected IamExternalWebClientFactory getIamWebClientFactory(final String keystorePrefix) {
        final IamExternalWebClientFactory webClientFactory = new IamExternalWebClientFactory(getRestClientConfiguration(iamServerHost, iamServerPort, true,
                getSSLConfiguration(certsFolder + keystorePrefix + ".jks", iamKeystorePassword, iamTrustStoreFilePath, iamTruststorePassword)), webClientBuilder);
        return webClientFactory;
    }
    private ReferentialExternalRestClientFactory getReferentialRestClientFactory() {
        if (restReferentialClientFactory == null) {
            LOGGER.debug("Instantiating referential rest client [host={}, port:{}, referentialKeystoreFilePath:{}]", referentialServerHost, referentialServerPort, referentialKeystoreFilePath);
            restReferentialClientFactory = new ReferentialExternalRestClientFactory(getRestClientConfiguration(referentialServerHost, referentialServerPort, true,
                    getSSLConfiguration(referentialKeystoreFilePath, referentialKeystorePassword, referentialTrustStoreFilePath, referentialTruststorePassword)), restTemplateBuilder);
            final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            interceptors.add(new RegisterRestQueryInterceptor());
            restReferentialClientFactory.setRestClientInterceptor(interceptors);
        }
        return restReferentialClientFactory;
    }

    private ReferentialExternalWebClientFactory getReferentialWebClientFactory() {
        if (webReferentialClientFactory == null) {
            LOGGER.debug("Instantiating referential web client [host={}, port:{}, referentialKeystoreFilePath:{}]", referentialServerHost, referentialServerPort, referentialKeystoreFilePath);
            webReferentialClientFactory = new ReferentialExternalWebClientFactory(
            	getRestClientConfiguration(referentialServerHost, referentialServerPort, true,
            		getSSLConfiguration(referentialKeystoreFilePath, referentialKeystorePassword, referentialTrustStoreFilePath, referentialTruststorePassword)
            	)
            );
        }
        return webReferentialClientFactory;
    }

    protected CustomerExternalRestClient getCustomerRestClient() {
        if (customerClient == null) {
            customerClient = getIamRestClientFactory().getCustomerExternalRestClient();
        }
        return customerClient;
    }

    protected CustomerExternalWebClient getCustomerWebClient() {
        if (customerWebClient == null) {
            customerWebClient = getIamExternalWebClientFactory().getCustomerWebClient();
        }
        return customerWebClient;
    }

    protected AgencyExternalWebClient getAgencyWebClient() {
        if (agencyWebClient == null) {
            agencyWebClient = getReferentialWebClientFactory().getAgencyExternalWebClient();
        }
        return agencyWebClient;
    }

    protected OntologyExternalWebClient getOntologyWebClient() {
        if (ontologyWebClient == null) {
            ontologyWebClient = getReferentialWebClientFactory().getOntologyExternalWebClient();
        }
        return ontologyWebClient;
    }

    protected FileFormatExternalWebClient getFileFormatWebClient() {
        if (fileFormatWebClient == null) {
            fileFormatWebClient = getReferentialWebClientFactory().getFileFormatExternalWebClient();
        }
        return fileFormatWebClient;
    }

    protected IamExternalWebClientFactory getIamWebClientFactory(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        final IamExternalWebClientFactory restClientFactory = new IamExternalWebClientFactory(getRestClientConfiguration(iamServerHost, iamServerPort, true,
                getSSLConfiguration(certsFolder + GENERIC_CERTIFICATE + ".jks", iamKeystorePassword, iamTrustStoreFilePath, iamTruststorePassword)), webClientBuilder);
        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RegisterRestQueryInterceptor());
        return restClientFactory;
    }

    protected MongoClient getMongoIam() {
        if (BaseIntegration.mongoClientIam == null) {
            BaseIntegration.mongoClientIam = new MongoClient(new MongoClientURI(mongoIamUri));
        }
        return BaseIntegration.mongoClientIam;
    }

    protected MongoClient getMongoSecurity() {
        if (BaseIntegration.mongoClientSecurity == null) {
            BaseIntegration.mongoClientSecurity = new MongoClient(new MongoClientURI(mongoSecurityUri));
        }
        return BaseIntegration.mongoClientSecurity;
    }

    protected MongoDatabase getIamDatabase() {
        if (iamDatabase == null) {
            iamDatabase = getMongoIam().getDatabase("iam");
        }
        return iamDatabase;
    }

    protected MongoDatabase getSecurityDatabase() {
        if (securityDatabase == null) {
            securityDatabase = getMongoSecurity().getDatabase("security");
        }
        return securityDatabase;
    }

    protected MongoCollection<Document> getContextsCollection() {
        if (contextsCollection == null) {
            contextsCollection = getSecurityDatabase().getCollection("contexts");
        }
        return contextsCollection;
    }

    protected MongoCollection<Document> getCertificatesCollection() {
        if (certificatesCollection == null) {
            certificatesCollection = getSecurityDatabase().getCollection("certificates");
        }
        return certificatesCollection;
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
        }
        else {
            itContext.append("tenants", Arrays.asList(new Integer[] { -1 }));
        }
        getContextsCollection().insertOne(itContext);

        // recreate generic certificate
        getCertificatesCollection().deleteOne(eq("_id", TESTS_CERTIFICATE_ID));
        //@formatter:off
        try {
            final String certificate = getCertificate("JKS", genericCert, jksPassword.toCharArray());

            final Document itCertificate = new Document("_id", TESTS_CERTIFICATE_ID)
                    .append("contextId", TESTS_CONTEXT_ID)
                    .append("subjectDN", "subjectDN")
                    .append("issuerDN", "issuerDN")
                    .append("serialNumber", "serialNumberAdmin")
                    .append("data", certificate);
            getCertificatesCollection().insertOne(itCertificate);

        }
        catch (final Exception e) {
            LOGGER.error("Retrieving generic certificate failed [cert={}, password:{}, exception :{}]", genericCert, jksPassword, e);
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

    protected CustomerExternalRestClient getCustomerRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getCustomerExternalRestClient();
    }

    protected CustomerExternalWebClient getCustomerWebClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamWebClientFactory(GENERIC_CERTIFICATE).getCustomerWebClient();
    }

    protected IdentityProviderExternalRestClient getIdentityProviderRestClient() {
        if (identityProviderRestClient == null) {
            identityProviderRestClient = getIamRestClientFactory().getIdentityProviderExternalRestClient();
        }
        return identityProviderRestClient;
    }

    protected IdentityProviderExternalRestClient getIdentityProviderRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getIdentityProviderExternalRestClient();
    }

    protected TenantExternalRestClient getTenantRestClient() {
        if (tenantRestClient == null) {
            tenantRestClient = getIamRestClientFactory().getTenantExternalRestClient();
        }
        return tenantRestClient;
    }

    protected TenantExternalRestClient getTenantRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getTenantExternalRestClient();
    }

    protected UserExternalRestClient getUserRestClient() {
        if (userRestClient == null) {
            userRestClient = getIamRestClientFactory().getUserExternalRestClient();
        }
        return userRestClient;
    }

    protected UserExternalRestClient getUserRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getUserExternalRestClient();
    }

    protected GroupExternalRestClient getGroupRestClient() {
        if (groupRestClient == null) {
            groupRestClient = getIamRestClientFactory().getGroupExternalRestClient();
        }
        return groupRestClient;
    }

    protected GroupExternalRestClient getGroupRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getGroupExternalRestClient();
    }

    protected ApplicationExternalRestClient getApplicationRestClient() {
        if (applicationRestClient == null) {
            applicationRestClient = getIamRestClientFactory().getApplicationExternalRestClient();
        }
        return applicationRestClient;
    }

    protected ApplicationExternalRestClient getApplicationRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getApplicationExternalRestClient();
    }

    protected ProfileExternalRestClient getProfileRestClient() {
        if (profileRestClient == null) {
            profileRestClient = getIamRestClientFactory().getProfileExternalRestClient();
        }
        return profileRestClient;
    }

    protected ProfileExternalRestClient getProfileRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getProfileExternalRestClient();
    }

    protected CasExternalRestClient getCasRestClient() {
        if (casRestClient == null) {
            casRestClient = getIamRestClientFactory().getCasExternalRestClient();
        }
        return casRestClient;
    }

    protected CasExternalRestClient getCasRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getCasExternalRestClient();
    }

    protected SubrogationExternalRestClient getSubrogationRestClient() {
        if (subrogationRestClient == null) {
            subrogationRestClient = getIamRestClientFactory().getSubrogationExternalRestClient();
        }
        return subrogationRestClient;
    }

    protected SubrogationExternalRestClient getSubrogationRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getSubrogationExternalRestClient();
    }

    protected OwnerExternalRestClient getOwnerRestClient() {
        if (ownerRestClient == null) {
            ownerRestClient = getIamRestClientFactory().getOwnerExternalRestClient();
        }
        return ownerRestClient;
    }

    protected OwnerExternalRestClient getOwnerRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getOwnerExternalRestClient();
    }

    protected LogbookExternalRestClient getLogbookRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getLogbookExternalRestClient();
    }

    protected ContextExternalRestClient getContextRestClient() {
        if (contextRestClient == null) {
            contextRestClient = getReferentialRestClientFactory().getContextExternalRestClient();
        }
        return contextRestClient;
    }

    protected RuleExternalRestClient getRuleRestClient() {
        if (ruleRestClient == null) {
            ruleRestClient = getReferentialRestClientFactory().getRuleExternalRestClient();
        }
        return ruleRestClient;
    }

    protected AccessContractExternalRestClient getAccessContractRestClient() {
        if (accessContractRestClient == null) {
            accessContractRestClient = getReferentialRestClientFactory().getAccessContractExternalRestClient();
        }
        return accessContractRestClient;
    }

    protected IngestContractExternalRestClient getIngestContractRestClient() {
        if (ingestContractRestClient == null) {
            ingestContractRestClient = getReferentialRestClientFactory().getIngestContractExternalRestClient();
        }
        return ingestContractRestClient;
    }

    protected SecurityProfileExternalRestClient getSecurityProfileRestClient() {
        if (securityProfileRestClient == null) {
            securityProfileRestClient = getReferentialRestClientFactory().getSecurityProfileExternalRestClient();
        }
        return securityProfileRestClient;
    }

    protected UnitExternalRestClient getUnitRestClient() {
        if (unitRestClient == null) {
            unitRestClient = getReferentialRestClientFactory().getUnitExternalRestClient();
        }
        return unitRestClient;
    }

    protected ExternalParametersExternalRestClient getExternalParametersExternalRestClient() {
        if (externalParametersExternalRestClient == null) {
        	externalParametersExternalRestClient = getIamRestClientFactory().getExternalParametersExternalRestClient();
        }
        return externalParametersExternalRestClient;
    }

    protected ExternalParametersExternalRestClient getExternalParametersExternalRestClient(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
        prepareGenericContext(fullAccess, tenants, roles);
        return getIamRestClientFactory(GENERIC_CERTIFICATE).getExternalParametersExternalRestClient();
    }


    protected MongoCollection<Document> getProfilesCollection() {
        if (profilesCollection == null) {
            profilesCollection = getIamDatabase().getCollection("profiles");
        }
        return profilesCollection;
    }

    protected MongoCollection<Document> getSubrogationsCollection() {
        if (subrogationsCollection == null) {
            subrogationsCollection = getIamDatabase().getCollection("subrogations");
        }
        return subrogationsCollection;
    }

    protected void writeProfile(final String id, final String level, final int tenantId, final String[] roles, final String customerId) {
        final List<Document> rolesList = new ArrayList<>();
        for (final String role : roles) {
            rolesList.add(new Document("name", role));
        }
        getProfilesCollection().deleteOne(eq("_id", id));
        //@formatter:off
        final Document profile = new Document("_id", id)
                .append("name", "Test Tenant Profile " + id)
                .append("identifier", generateRandomInteger())
                .append("level", level)
                .append("description", "Test Tenant Profile " + id)
                .append("tenantIdentifier", tenantId)
                .append("applicationName", "TEST")
                .append("enabled", true)
                .append("readonly", false)
                .append("roles", rolesList)
                .append("customerId", customerId);
        //@formatter:on
        getProfilesCollection().insertOne(profile);
    }

    protected String generateRandomInteger() {
        return Integer.toString(ThreadLocalRandom.current().nextInt(1000000, 10000000));
    }

    protected MongoCollection<Document> getGroupsCollection() {
        if (groupsCollection == null) {
            groupsCollection = getIamDatabase().getCollection("groups");
        }
        return groupsCollection;
    }

    protected void writeGroup(final String id, final String level, final String profileId, final String customerId) {
        writeGroup(id, level, new String[] { profileId }, customerId);
    }

    protected void writeGroup(final String id, final String level, final String[] profileIds, final String customerId) {
        getGroupsCollection().deleteOne(eq("_id", id));
        //@formatter:off
        final Document group = new Document("_id", id)
                .append("level", level)
                .append("enabled", true)
                .append("readonly", true)
                .append("description", "Test Tenant Group")
                .append("profileIds", Arrays.asList(profileIds))
                .append("customerId", customerId);
        //@formatter:on
        getGroupsCollection().insertOne(group);
    }

    protected MongoCollection<Document> getUsersCollection() {
        if (usersCollection == null) {
            usersCollection = getIamDatabase().getCollection("users");
        }
        return usersCollection;
    }

    protected void writeUser(final String id, final String level, final String identifier, final String groupId, final String customerId, final String email) {
        getUsersCollection().deleteOne(eq("_id", id));
        //@formatter:off
        final Document user = new Document("_id", id)
                .append("level", level)
                .append("enabled", true)
                .append("readonly", true)
                .append("identifier", identifier)
                .append("password", "password")
                .append("email", email)
                .append("firstname", "FIR" + identifier)
                .append("lastname", "LAS" + identifier)
                .append("phone", "+33178956321")
                .append("mobile", "+33674892746")
                .append("otp", false)
                .append("language", LanguageDto.FRENCH.toString())
                .append("type", UserTypeEnum.NOMINATIVE.toString())
                .append("status", UserStatusEnum.ENABLED.toString())
                .append("subrogeable", true)
                .append("groupId", groupId)
                .append("customerId", customerId);
        //@formatter:on
        getUsersCollection().insertOne(user);
    }

    protected MongoCollection<Document> getTokensCollection() {
        if (tokensCollection == null) {
            tokensCollection = getIamDatabase().getCollection("tokens");
        }
        return tokensCollection;
    }

    protected void tokenUserAdmin() {
        writeToken(TESTS_USER_ADMIN, SYSTEM_USER_ID);
    }

    protected void writeToken(final String id, final String userId) {
        getTokensCollection().deleteOne(eq("_id", id));
        final Document token = new Document("_id", id).append("updatedDate", DateUtils.addDays(new Date(), -10)).append("refId", userId);
        getTokensCollection().insertOne(token);
    }
}
