package fr.gouv.vitamui.cucumber.common;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.CompareQuery;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.AbstractIntegrationTest;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookEventDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.cucumber.common.context.Context;
import fr.gouv.vitamui.cucumber.common.context.TestContext;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.utils.FactoryDto;
import fr.gouv.vitamui.utils.TestConstants;

@ContextConfiguration(classes = Context.class)
public abstract class CommonSteps extends BaseIntegration {

    private static boolean defaultSubrogationInitialized = false;

    protected List<TenantDto> tenantDtos;

    protected SubrogationDto subrogationDto;

    protected AuthUserDto surrogateUser;

    protected UserDto superUser;

    protected List<CustomerDto> basicCustomerDtos;

    @Autowired
    protected TestContext testContext;

    protected String getOrInitializeDefaultSubrogationId() {
        if (!defaultSubrogationInitialized) {
            writeSubrogation(IamDtoBuilder.buildSubrogationDto("juliensurrogatespierre",
                    TestConstants.PIERRE_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain,
                    TestConstants.JULIEN_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain));
        }

        return "juliensurrogatespierre";
    }

    protected void buildSubrogation(final boolean clientSubrogeable, final boolean surrogateSubrogeable, final UserStatusEnum surrogateStatus,
            final UserStatusEnum superUserStatus) {

        CustomerDto customer = FactoryDto.buildDto(CustomerDto.class);
        final String ownerName = customer.getOwners().get(0).getName();
        customer.setSubrogeable(clientSubrogeable);
        customer = getCustomerWebClient().create(getSystemTenantUserAdminContext(), customer, Optional.empty());
        final String customerId = customer.getId();

        final String adminEmail = "admin@" + customer.getDefaultEmailDomain();
        final AuthUserDto adminUser = (AuthUserDto) getCasRestClient(false, new Integer[] { casTenantIdentifier },
                new String[] { ServicesData.ROLE_CAS_USERS }).getUserByEmail(getContext(casTenantIdentifier, TestConstants.TOKEN_USER_CAS),
                        adminEmail, Optional.of(CommonConstants.AUTH_TOKEN_PARAMETER));

        final QueryDto criteria = QueryDto.criteria("name", ownerName, CriterionOperator.CONTAINSIGNORECASE);

        testContext.tenantDto = getTenantRestClient().getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson(), Optional.empty()).get(0);

        final ExternalHttpContext customerAdminContext = getContext(testContext.tenantDto.getIdentifier(), adminUser.getAuthToken());

        // retrieve admin group in order to create the right user
        final List<GroupDto> groups = getGroupRestClient().getAll(customerAdminContext, Optional.empty());
        if (CollectionUtils.isEmpty(groups)) {
            throw new ApplicationServerException("Can't find groups for customer : " + customerId);
        }
        final Optional<GroupDto> optionalGroup = groups.stream()
                .filter(group -> BooleanUtils.isTrue(group.isEnabled()) && StringUtils.isEmpty(group.getLevel())).findFirst();
        if (!optionalGroup.isPresent()) {
            throw new ApplicationServerException("Can't find existing admin group for customer : " + customerId);
        }

        UserDto basicUserDto = FactoryDto.buildDto(UserDto.class);
        basicUserDto.setSubrogeable(surrogateSubrogeable);
        basicUserDto.setCustomerId(customerId);
        basicUserDto.setGroupId(optionalGroup.get().getId());
        if (surrogateStatus != null) {
            basicUserDto.setStatus(surrogateStatus);
        }
        basicUserDto = getUserRestClient().create(customerAdminContext, basicUserDto);
        surrogateUser = new AuthUserDto(basicUserDto);

        subrogationDto = IamDtoBuilder.buildSubrogationDto(null, TestConstants.JULIEN_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain,
                TestConstants.SYSTEM_USER_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR + defaultEmailDomain);
        subrogationDto.setSurrogateCustomerId(surrogateUser.getCustomerId());
        subrogationDto.setSuperUserCustomerId(customerId);

        if (testContext.superUserEmail != null) {
            subrogationDto.setSuperUser(testContext.superUserEmail);
        }
        else {
            if (superUserStatus != null) {
                superUser = FactoryDto.buildDto(UserDto.class);
                superUser.setCustomerId(customerId);
                superUser.setStatus(superUserStatus);
                superUser.setGroupId(optionalGroup.get().getId());
                superUser = getUserRestClient().create(customerAdminContext, superUser);
                subrogationDto.setSuperUser(superUser.getEmail());
            }
            else {
                superUser = null;
            }
        }

        subrogationDto.setSurrogate(surrogateUser.getEmail());
    }

    protected SubrogationDto buildGoodSubrogation() {
        buildSubrogation(true, true, null, null);
        return subrogationDto;
    }

    protected void createBasicUser() {
        UserDto basicUserDto = FactoryDto.buildDto(UserDto.class);
        basicUserDto = getUserRestClient().create(getSystemTenantUserAdminContext(), basicUserDto);
        testContext.authUserDto = new AuthUserDto(basicUserDto);
    }

    protected TenantDto createOwnerAndBuildTenant() {
        final TenantDto tenantDto = FactoryDto.buildDto(TenantDto.class);
        final OwnerDto ownerDto = FactoryDto.buildDto(OwnerDto.class);
        final OwnerDto rownerDto = getOwnerRestClient().create(getSystemTenantUserAdminContext(), ownerDto);
        tenantDto.setOwnerId(rownerDto.getId());
        return tenantDto;
    }

    protected void setMainTenant(final int mainTenant) {
        testContext.mainTenant = mainTenant;
    }

    protected void setSecondTenant(final int secondTenant) {
        testContext.otherTenant = secondTenant;
    }

    protected String tokenUser(final String[] roles, final String customerId, final String email, final String level, final int tenant, final String globalId) {
        final String PROFILE_ID = globalId;
        final String GROUP_ID = globalId;
        final String USER_ID = globalId;
        final String TOKEN_ID = globalId;
        final long t0 = new Date().getTime();
        writeProfile(PROFILE_ID, level, tenant, roles, customerId);
        writeGroup(GROUP_ID, level, PROFILE_ID, customerId);
        writeUser(USER_ID, level, "" + t0, GROUP_ID, customerId, email);
        writeToken(TOKEN_ID, USER_ID);
        return TOKEN_ID;
    }

    protected String tokenUserTest(final String[] roles, final int tenant, final String customerId, final String level) {
        final long t0 = new Date().getTime();
        writeProfile(TestConstants.TESTS_PROFILE_ID, level, tenant, roles, customerId);
        writeGroup(TestConstants.TESTS_GROUP_ID, level, TestConstants.TESTS_PROFILE_ID, customerId);
        writeUser(TestConstants.TESTS_USER_ID, level, "" + t0, TestConstants.TESTS_GROUP_ID, customerId, TEST_USER_EMAIL);
        writeToken(TestConstants.TESTS_TOKEN_ID, TestConstants.TESTS_USER_ID);
        return TestConstants.TESTS_TOKEN_ID;
    }

    protected String tokenUserTestSystemCustomer(final String role, final int tenant) {
        return tokenUserTest(new String[] { role }, tenant, TestConstants.SYSTEM_CUSTOMER_ID, testContext.level);
    }

    protected String tokenUserTestSystemCustomer(final String[] role, final int tenant) {
        return tokenUserTest(role, tenant, TestConstants.SYSTEM_CUSTOMER_ID, testContext.level);
    }

    protected String tokenUserTestTenantSystem() {
        return tokenUserTestSystemCustomer(ServicesData.ROLE_GET_TENANTS, proofTenantIdentifier);
    }

    protected String tokenUserNoRole(final int tenant) {
        tokenUserTestSystemCustomer((String) null, tenant);
        writeProfile(TestConstants.TESTS_PROFILE_ID, testContext.level, tenant, new String[] {}, TestConstants.SYSTEM_CUSTOMER_ID);
        return TestConstants.TESTS_TOKEN_ID;
    }

    protected void writeSubrogation(final SubrogationDto subrogationDto) {
        final String id = subrogationDto.getId() != null ? subrogationDto.getId() : "subrogationId";
        subrogationDto.setId(id);
        final Date sDate = Date.from(subrogationDto.getDate().toInstant());
        getSubrogationsCollection().deleteOne(eq("_id", id));
        final Document subrogation = new Document("_id", id).append("status", subrogationDto.getStatus().toString()).append("date", sDate)
                .append("surrogate", subrogationDto.getSurrogate()).append("superUser", subrogationDto.getSuperUser())
                .append("surrogateCustomerId", subrogationDto.getSurrogateCustomerId()).append("superUserCustomerId", subrogationDto.getSuperUserCustomerId());
        deleteSubrogation(subrogationDto);
        getSubrogationsCollection().insertOne(subrogation);
    }

    protected void deleteAllSubrogations(final SubrogationDto subrogationDto) {
        final BsonDocument document = new BsonDocument("superUser", new BsonString(subrogationDto.getSuperUser()));
        getSubrogationsCollection().deleteMany(document);
    }

    protected void deleteSubrogation(final SubrogationDto subrogationDto) {
        final BsonDocument document = new BsonDocument("superUser", new BsonString(subrogationDto.getSuperUser()));
        document.append("surrogate", new BsonString(subrogationDto.getSurrogate()));
        getSubrogationsCollection().deleteOne(document);
    }

    protected void createSubrogationByUserStatus(final boolean isSuperUserDisabled) {
        if (isSuperUserDisabled) {
            buildSubrogation(true, true, null, UserStatusEnum.DISABLED);
        }
        else {
            buildSubrogation(true, true, null, UserStatusEnum.ENABLED);
        }
        writeSubrogation(subrogationDto);
    }

    protected ObjectNode buildOperationQuery(final String obId, final String obIdReq, final String evType) {
        final Select select = new Select();
        final BooleanQuery andQuery;
        final CompareQuery obIdQuery;
        final CompareQuery obIdReqQuery;
        final CompareQuery eventTypeQuery;
        final CompareQuery evDateTime;
        try {
            andQuery = QueryHelper.and();
            obIdQuery = QueryHelper.eq("events.obId", obId);
            obIdReqQuery = QueryHelper.eq("events.obIdReq", obIdReq);
            eventTypeQuery = QueryHelper.eq("events.evType", evType);
            evDateTime = QueryHelper.gte("evDateTime", AbstractIntegrationTest.start.toLocalDate().toString());
            andQuery.add(obIdQuery, obIdReqQuery, eventTypeQuery, evDateTime);
            select.setQuery(andQuery);
            select.addUsedProjection("events");
            select.addOrderByDescFilter("evDateTime");
            select.setLimitFilter(0, 5);
        }
        catch (final InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new ApplicationServerException("An error occured while creating vitam query", e);
        }

        return select.getFinalSelect();
    }

    protected Optional<LogbookEventDto> testTrace(final String customerId, final String identifier, final String collectionNames, final String eventType) {
        Optional<LogbookEventDto> event = Optional.empty();
        if (traceEnabled) {
            final QueryDto criteria = QueryDto.criteria("customerId", customerId, CriterionOperator.EQUALS).addCriterion("proof", true,
                    CriterionOperator.EQUALS);
            final List<TenantDto> tenantDto = getTenantRestClient(true, null, new String[] { ServicesData.ROLE_GET_ALL_TENANTS, ServicesData.ROLE_GET_TENANTS })
                    .getAll(getSystemTenantUserAdminContext(), criteria.toOptionalJson());
            assertThat(tenantDto).hasSize(1);
            final Integer tenantIdentifier = tenantDto.stream().findFirst().get().getIdentifier();

            final OffsetDateTime timeOut = OffsetDateTime.now().plusSeconds(timeOutInSeconds);
            while (!event.isPresent() && OffsetDateTime.now().isBefore(timeOut)) {
                try {
                    // Retry every 5 secondes
                    Thread.sleep(5000L);
                    event = retrieveTraceFromVitam(identifier, collectionNames, eventType, tenantIdentifier);
                }
                catch (final InterruptedException e) {
                    e.printStackTrace();
                }

            }
            assertThat(event).isPresent();
        }
        return event;
    }

    private Optional<LogbookEventDto> retrieveTraceFromVitam(final String identifier, final String collectionNames, final String eventType,
            final Integer tenantIdentifier) {
        Optional<LogbookEventDto> event = Optional.empty();
        final JsonNode responseJson = getLogbookRestClient(true, null, new String[] { ServicesData.ROLE_LOGBOOKS })
                .findOperations(getArchiveTenantUserAdminContext(tenantIdentifier), buildOperationQuery(identifier, collectionNames, eventType));

        final LogbookOperationsResponseDto response;
        try {
            response = JsonUtils.treeToValue(responseJson, LogbookOperationsResponseDto.class, false);
        }
        catch (final JsonProcessingException e) {
            throw new InternalServerException(VitamRestUtils.PARSING_ERROR_MSG, e);
        }
        if (CollectionUtils.isNotEmpty(response.getResults())) {
            final Predicate<LogbookEventDto> predicate = (l) -> {
                JsonNode evDetData = null;
                try {
                    evDetData = JsonUtils.readTree(l.getEvDetData());
                }
                catch (final IOException e) {
                    e.printStackTrace();
                }
                final JsonNode dateOp = evDetData.get(TestConstants.EVENT_DATE_TIME_KEY);
                String evDateOperation = null;
                if (dateOp != null) {
                    evDateOperation = dateOp.asText();
                }
                OffsetDateTime dateOperation = null;
                if (evDateOperation != null) {
                    dateOperation = OffsetDateTime.parse(evDateOperation);
                }
                return dateOperation != null && dateOperation.isAfter(AbstractIntegrationTest.start) && StringUtils.equals(l.getEvType(), eventType);
            };

            event = response.getResults().stream().flatMap(l -> l.getEvents().stream()).filter(predicate).findFirst();
        }
        return event;
    }

}
