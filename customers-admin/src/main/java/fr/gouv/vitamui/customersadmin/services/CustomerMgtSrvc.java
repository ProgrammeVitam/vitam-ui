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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.external.client.CustomerExternalWebClient;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

@Configuration
@Service
//@Import(value = {ServerIdentityConfiguration.class})
public class CustomerMgtSrvc {

    protected static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerMgtSrvc.class);

    protected static final String GENERIC_CERTIFICATE = "generic-it";
    public static final String ADMIN_USER = "superadmin_user";
    public static final String TOKEN_USER_ADMIN = "tokenadmin";
    protected static final String TESTS_CONTEXT_ID = "integration-tests_context";
    private MongoCollection<Document> tokensCollection;
    private MongoDatabase iamDatabase;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> usersInfosCollection;
    private MongoDatabase securityDatabase;
    private static MongoClient mongoClientSecurity;

    private MongoCollection<Document> contextsCollection;
    private MongoCollection<Document> groupsCollection;
    private MongoCollection<Document> tenantsCollection;
    private MongoCollection<Document> customersCollection;
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

    @Value("classpath:data/users.json")
    private Resource usersFile;

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

    //  @Autowired
    //  private IamExternalWebClientFactory iamWebClientFactory;


    //  @Autowired
    //  private IamExternalRestClientFactory iamRestClientFactory;

    List<String> tokensId = new ArrayList<>();

    public ExternalHttpContext createCustomerContext(String customerId, String userId, int tenant) {
        return new ExternalHttpContext(tenant, "token-" + userId, TESTS_CONTEXT_ID,
            "admincaller", "requestId", "ContratTNR");
    }

    protected ExternalHttpContext getSystemTenantUserAdminContext() {
        buildSystemTenantUserAdminContext();
        return new ExternalHttpContext(proofTenantIdentifier, TOKEN_USER_ADMIN, TESTS_CONTEXT_ID,
            "admincaller", "requestId", "ContratTNR");
    }

    private void buildSystemTenantUserAdminContext() {
        getUsersCollection().updateOne(new BsonDocument("_id", new BsonString(ADMIN_USER)),
            new BsonDocument("$set", new BsonDocument("groupId", new BsonString(ADMIN_USER_GROUP))));
        tokenUserAdmin();
    }

    private Integer getTenantInfosByCustomer(String customerId) {
        Integer tenantId = null;
        Query query = new Query();
        query.addCriteria(Criteria.where("customerId").is(customerId));

        BsonDocument bsonDocument = new BsonDocument("customerId", new BsonString(customerId));
        FindIterable<Document> documents = getTenantsCollection().find(bsonDocument);
        try (MongoCursor<Document> cursor = documents.iterator()) {
            while (cursor.hasNext()) {
                tenantId = cursor.next().get("identifier", Integer.class);
            }
        }
        return tenantId;
    }


    private String getGenericgetGenericUserAdminByCustomerUserAdminIdByCustomer(String customerId) {
        String userId = null;
        Query query = new Query();
        query.addCriteria(Criteria.where("customerId").is(customerId).and("type").is("GENERIC"));

        BsonDocument bsonDocument = new BsonDocument("customerId", new BsonString(customerId));
        FindIterable<Document> documents = getUsersCollection().find(bsonDocument);
        try (MongoCursor<Document> cursor = documents.iterator()) {
            while (cursor.hasNext()) {
                userId = cursor.next().get("_id", String.class);
            }
        }
        return userId;
    }

    private String getGroupIdByCustomer(String customerId) {
        String groupId = null;
        Query query = new Query();
        query.addCriteria(Criteria.where("customerId").is(customerId));

        BsonDocument bsonDocument = new BsonDocument("customerId", new BsonString(customerId));
        FindIterable<Document> documents = getGroupsCollection().find(bsonDocument);
        try (MongoCursor<Document> cursor = documents.iterator()) {
            while (cursor.hasNext()) {
                groupId = cursor.next().get("_id", String.class);
            }
        }
        return groupId;
    }

    private List<CustomerDto> getCustomersList() {
        List<CustomerDto> customers = new ArrayList<>();

        FindIterable<Document> documents = getCustomersCollection().find();
        try (MongoCursor<Document> cursor = documents.iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                CustomerDto customerDto = new CustomerDto();
                customerDto.setId(document.get("_id", String.class));
                customerDto.setName(document.get("name", String.class));
                customerDto.setDefaultEmailDomain(document.get("defaultEmailDomain", String.class));
                customerDto.setEmailDomains(document.get("emailDomains", List.class));
                customers.add(customerDto);
            }
        }
        return customers;
    }

    protected String tokenUserAdmin() {
        return writeToken(TESTS_USER_ADMIN, SYSTEM_USER_ID);
    }

    protected String writeToken(final String tokenId, final String userId) {

        final Document token =
            new Document("_id", tokenId).append("updatedDate", DateUtils.addDays(new Date(), -10))
                .append("refId", userId);
        getTokensCollection().insertOne(token);
        return tokenId;
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

    protected MongoCollection<Document> getGroupsCollection() {
        if (groupsCollection == null) {
            groupsCollection = getIamDatabase().getCollection("groups");
        }
        return groupsCollection;
    }

    protected MongoCollection<Document> getTenantsCollection() {
        if (tenantsCollection == null) {
            tenantsCollection = getIamDatabase().getCollection("tenants");
        }
        return tenantsCollection;
    }

    protected MongoCollection<Document> getCustomersCollection() {
        if (customersCollection == null) {
            customersCollection = getIamDatabase().getCollection("customers");
        }
        return customersCollection;
    }

    protected MongoCollection<Document> getUsersCollection() {
        if (usersCollection == null) {
            usersCollection = getIamDatabase().getCollection("users");
        }
        return usersCollection;
    }

    protected MongoCollection<Document> getUserInfosCollection() {
        if (usersInfosCollection == null) {
            usersInfosCollection = getIamDatabase().getCollection("usersInfos");
        }
        return usersInfosCollection;
    }

    protected String generatedUserInfo() {
        String generatedId = UUID.randomUUID().toString();
        final Document userInfoEntry =
            new Document("_id", generatedId).append("language", "FRENCH")
                .append("_class", "fr.gouv.vitamui.iam.internal.server.user.domain.UserInfo");
        getUserInfosCollection().insertOne(userInfoEntry);
        return generatedId;
    }

    protected void prepareGenericContext(final boolean fullAccess, final Integer[] tenants, final String[] roles) {
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
                genericCert, jksPassword, e);
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
/*
    private void parseAndCreateUsers() {

        LOGGER.info("Start parsing users file");
        List<UserDto> usersListToCreate = null;
        try {
            usersListToCreate = readFromUsersFile();
            if (!CollectionUtils.isEmpty(usersListToCreate)) {
                LOGGER.info("Start creating users from file, users count in file is {} ",
                    usersListToCreate.size());
                try {
                    List<CustomerDto> customerDtoList = getCustomersList();
                    if (!CollectionUtils.isEmpty(customerDtoList)) {
                        for (UserDto userDto : usersListToCreate) {
                            Optional<CustomerDto> customerDtoOpt = customerDtoList.stream()
                                .filter(customerDto -> customerDto.getDefaultEmailDomain() != null &&
                                    userDto.getEmail().contains(customerDto.getDefaultEmailDomain()))
                                .findAny();
                            if (customerDtoOpt.isPresent()) {
                                CustomerDto customerDto = customerDtoOpt.get();
                                Integer tenant = getTenantInfosByCustomer(customerDto.getId());
                                String groupId = getGroupIdByCustomer(customerDto.getId());
                                LOGGER.info("Start preparing context for user {} - {} ", userDto.getFirstname(),
                                    userDto.getLastname());
                                String genericAdminUserId =
                                    getGenericgetGenericUserAdminByCustomerUserAdminIdByCustomer(customerDto.getId());
                                writeToken("token-" + genericAdminUserId, genericAdminUserId);
                                ExternalHttpContext customerContext =
                                    createCustomerContext(customerDto.getId(), genericAdminUserId, tenant);
                                String userInfoId = generatedUserInfo();
                                userDto.setUserInfoId(userInfoId);
                                userDto.setCustomerId(customerDto.getId());
                                userDto.setGroupId(groupId);
                                UserDto createdUser =
                                    iamRestClientFactory.getUserExternalRestClient().create(customerContext, userDto);
                                LOGGER.info("User {}-}{} created with success with id {} ", userDto.getFirstname(),
                                    userDto.getLastname(), createdUser.getIdentifier());
                            } else {
                                throw new NotFoundException("Wrong user / company email {} " + userDto.getEmail());
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error creating user due to error {} ", e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error parsing users files {} ", e.getMessage());
        }
    }*/

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

    public SSLConfiguration sSLConfiguration() {

        final String keystorePath =
            getClass().getClassLoader().getResource(certsFolder + GENERIC_CERTIFICATE + ".jks").getPath();
        final String trustStorePath = getClass().getClassLoader().getResource(iamTrustStoreFilePath).getPath();
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

    private void parseAndCreateCustomers() {
        LOGGER.info("Start init customers ");
        RestClientConfiguration restClientConfiguration =
            getRestClientConfiguration(iamServerHost, iamServerPort, true, sSLConfiguration());

        LOGGER.info("restClientConfiguration {}  ", restClientConfiguration);

        LOGGER.info("webClientBuilder {}  ", webClientBuilder);

        IamExternalWebClientFactory iamExternalWebClientFactory =
            new IamExternalWebClientFactory(restClientConfiguration, webClientBuilder);

        LOGGER.info("iamExternalWebClientFactory {}  ", iamExternalWebClientFactory);
        CustomerExternalWebClient customerExternalWebClient = iamExternalWebClientFactory.getCustomerWebClient();
        LOGGER.info("customerExternalWebClient {}  ", customerExternalWebClient);
        LOGGER.info("End init settings -------------- ");


        //read json file
        List<CustomerDto> customersListToCreate;
        List<CustomerDto> customerDtoList = new ArrayList<>();
        Map<String, String> customerDtoNotCreatedWithErrors = new HashMap<>();
        try {
            LOGGER.info("Start parsing customers file");
            customersListToCreate = readFromCustomersFile();


            if (customersListToCreate != null) {

                LOGGER.info("Start creating customers from file, customers count in file is {} ",
                    customersListToCreate.size());
                LOGGER.info("Start preparing context");
                prepareGenericContext(true, null, new String[] {ServicesData.ROLE_CREATE_CUSTOMERS});
                ExternalHttpContext externalContext = getSystemTenantUserAdminContext();
                CustomerDto customerDto = null;
                for (CustomerDto customerDtoToCreate : customersListToCreate) {
                    try {
                        LOGGER.debug("Start creating customer with code {} ", customerDtoToCreate.getCode());

                        CustomerCreationFormData customerCreationFormData = new CustomerCreationFormData();
                        customerCreationFormData.setCustomerDto(customerDtoToCreate);
                        customerCreationFormData.setTenantName(customerDtoToCreate.getCompanyName());

                        List<OwnerDto> ownerDtos = new ArrayList<>();
                        OwnerDto ownerDto = new OwnerDto();
                        ownerDto.setCode(customerDtoToCreate.getCode());
                        ownerDto.setCompanyName(customerDtoToCreate.getCompanyName());
                        ownerDto.setName(customerDtoToCreate.getName());
                        ownerDto.setAddress(customerDtoToCreate.getAddress());
                        ownerDtos.add(ownerDto);

                        customerDtoToCreate.setOwners(ownerDtos);
                        //boolean existCode = isExistCode(externalContext, customerCreationFormData);
                        customerDto = customerExternalWebClient.create(externalContext, customerCreationFormData);
                        customerDtoList.add(customerDto);
                        LOGGER.info("Customer with name {} and id {} is created with identifier ",
                            customerDto.getName(),
                            customerDto.getIdentifier());
                    } catch (Exception e) {
                        LOGGER.error("Error creating customer due to error {} ", e);
                        customerDtoNotCreatedWithErrors
                            .put(customerDto.getCode() + "-" + customerDto.getCompanyName(), e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error parsing customer files {} ", e.getMessage());
        } finally {
            LOGGER.info("Customer created count : {} ", customerDtoList.size());
            LOGGER.info("Customer not created count : {} ", customerDtoNotCreatedWithErrors.keySet().size());
            for (Map.Entry<String, String> entryError : customerDtoNotCreatedWithErrors.entrySet()) {
                LOGGER.info("Customer not created {} du to error {} ", entryError.getKey(),
                    entryError.getValue());
            }
        }
    }

    /*@TODO add check code after
     */
    /*
    private boolean isExistCode(ExternalHttpContext externalContext,
        CustomerCreationFormData customerCreationFormData) {
        boolean existCode =
            iamRestClientFactory.getUserExternalRestClient().checkExist(externalContext,
                "{\"queryOperator\":\"AND\",\"criteria\":[{\"queryOperator\":\"AND\",\"criteria\":[{\"key\":\"code\",\"value\":\"" +
                    customerCreationFormData.getCustomerDto().getCode() +
                    "\",\"operator\":\"EQUALS\"}]}]}");
        return existCode;
    }
*/
    public void createCustomersWithUsers() {
        try {
            tokensId.add(TESTS_USER_ADMIN);
            parseAndCreateCustomers();
            //parseAndCreateUsers();
        } finally {
            dropGenericContext();
        }
    }

    public void dropGenericContext() {
        // recreate generic context
        getContextsCollection().deleteOne(eq("_id", TESTS_CONTEXT_ID));
        // recreate generic certificate
        getCertificatesCollection().deleteOne(eq("_id", TESTS_CERTIFICATE_ID));
        if (!CollectionUtils.isEmpty(tokensId)) {
            tokensId.stream().forEach(tokenId -> getTokensCollection().deleteOne(eq("_id", TESTS_USER_ADMIN)));
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

    private List<UserDto> readFromUsersFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<UserDto> usersList =
            mapper.readValue(usersFile.getFile(), new TypeReference<List<UserDto>>() {
            });
        return usersList;
    }



}
