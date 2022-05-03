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
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

@Configuration
@Service
//@Import(value = {ServerIdentityConfiguration.class})
public class CustomerMgtSrvc {

    protected static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerMgtSrvc.class);

    protected static final String GENERIC_CERTIFICATE = "generic-it";
    public static final String ADMIN_USER = "admin_user";
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

    @Autowired
    private IamExternalWebClientFactory iamWebClientFactory;


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



    /*
        @Bean
        public IamExternalRestClientFactory iamExternalRestClientFactory(final RestTemplateBuilder restTemplateBuilder,
            final RestClientConfiguration restClientConfiguration) {
            final IamExternalRestClientFactory restClientFactory =
                new IamExternalRestClientFactory(restClientConfiguration, restTemplateBuilder);
            return restClientFactory;
        }
    */
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
        getTokensCollection().insertOne(userInfoEntry);
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

    public void createCustomers() throws IOException {

        LOGGER.info(getGroupIdByCustomer("626bf4c1a21df47ee9027a005f521a6f27fd49e1b4dac65a45c65eef1ff172f6"));
        //read json file
        List<CustomerCreationFormData> customersListToCreate = readFromCustomersFile();
        List<CustomerDto> customerDtoList = new ArrayList<>();
        if (customersListToCreate != null) {
            try {
                prepareGenericContext(true, null, new String[] {ServicesData.ROLE_CREATE_CUSTOMERS});
                ExternalHttpContext externalContext = getSystemTenantUserAdminContext();
                for (CustomerCreationFormData customerCreationFormData : customersListToCreate) {
                    LOGGER.debug("Start creating customer  {} ", customerCreationFormData);

                    boolean existCode = false;
                    /*
                    iamExternalRestClientFactory.getCustomerExternalRestClient().checkExist(externalContext,
                            "{\"queryOperator\":\"AND\",\"criteria\":[{\"queryOperator\":\"AND\",\"criteria\":[{\"key\":\"code\",\"value\":\"" +
                                customerCreationFormData.getCustomerDto().getCode() +
                                "\",\"operator\":\"EQUALS\"}]}]}");

                     */
                    if (existCode) {
                        throw new IllegalArgumentException(String.format("Customer with code exists %S",
                            customerCreationFormData.getCustomerDto().getCode()));
                    }
                    CustomerDto customerDto = iamWebClientFactory.getCustomerWebClient()
                        .create(externalContext, customerCreationFormData);
                    customerDtoList.add(customerDto);
                    LOGGER.info("Customer with name {} and id {} is created ", customerDto.getName(),
                        customerDto.getIdentifier());
                }
                List<UserDto> usersListToCreate = readFromUsersFile();
                if (!CollectionUtils.isEmpty(usersListToCreate)) {
                    for (CustomerDto customerDto : customerDtoList) {
                        Optional<UserDto> userOpt = usersListToCreate.stream()
                            .filter(userDto -> userDto.getEmail().contains(customerDto.getDefaultEmailDomain()))
                            .findAny();
                        /*
                        if (userOpt.isPresent()) {
                            UserDto userDto = userOpt.get();
                            String userInfoId = generatedUserInfo();
                            userDto.setUserInfoId(userInfoId);
                            UserDto createdUser =
                                getIamRestClientFactory(GENERIC_CERTIFICATE).getUserExternalRestClient()
                                    .create(externalContext, userDto);
                            LOGGER.info("User created with id {} ", createdUser.getIdentifier());
                        } else {
                            throw new NotFoundException(
                                "No user found for company email " + customerDto.getDefaultEmailDomain());
                        }
                        */
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error creating customer due to error {} ", e);
            } finally {
                dropGenericContext();
            }
        }
    }

    public void dropGenericContext() {
        // recreate generic context
        getContextsCollection().deleteOne(eq("_id", TESTS_CONTEXT_ID));
        // recreate generic certificate
        getCertificatesCollection().deleteOne(eq("_id", TESTS_CERTIFICATE_ID));
        getTokensCollection().deleteOne(eq("_id", TESTS_USER_ADMIN));
    }

    /**
     * @return
     */
    private List<CustomerCreationFormData> readFromCustomersFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<CustomerCreationFormData> customerList =
            mapper.readValue(customersFile.getFile(), new TypeReference<List<CustomerCreationFormData>>() {
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
