package fr.gouv.vitamui.iam.internal.server.utils;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.application.domain.Application;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.customer.domain.GraphicIdentity;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;

/**
 * Class Utils for test
 */

public final class IamServerUtilsTest {

    public static final String USER_ID = "userId";

    public static final String USER_MAIL = "user@supermail.fr";

    public static final String CUSTOMER_ID = "customerId";

    public static final String CUSTOMER_NAME = "customerName";

    public static final String CUSTOMER_CODE = "customerCode";

    public static final List<String> EMAIL_DOMAINS = Arrays.asList("example.fr", "example.com");

    public static final String GROUP_ID = "groupId";

    public static final String GROUP_NAME = "groupName";

    public static final String GROUP_IDENTIFIER = "1000";

    public static final String PROFILE_ID = "profileId";

    public static final String PROFILE_NAME = "profileName";

    public static final String PROFILE_IDENTIFIER = "1000";

    public static final String APPLICATION_IDENTIFIER = "applicationIdentifier";

    public static final String APPLICATION_URL = "applicationUrl";

    public static final String TENANT_ID = "tenantId";

    public static final String TENANT_NAME = "tenantName";

    public static final int TENANT_IDENTIFIER = 1000;

    public static final String IDP_ID = "idpId";

    public static final String IDP_NAME = "idpName";

    public static final int IDP_IDENTIFIER = 1000;

    public static final String OWNER_ID = "ownerId";

    public static final String OWNER_IDENTIFIER = "ownerIdentifier";

    public static final String OWNER_NAME = "ownerName";

    public static final String LEVEL = "DSI";

    public static final String APP_NAME = "appName";

    public static final List<Role> ROLES =
        Arrays.asList(new Role(ServicesData.ROLE_CREATE_USERS), new Role(ServicesData.ROLE_GET_USERS));

    /**
     * User
     */
    public static User buildUser() {
        return buildUser(USER_ID, USER_MAIL, GROUP_ID);
    }

    public static User buildUser(final String id, final String email, final String groupId) {
        return buildUser(id, email, groupId, CUSTOMER_ID);
    }

    public static User buildUser(final String id, final String email, final String groupId, final String customerId) {
        return buildUser(id, email, groupId, customerId, LEVEL);
    }

    public static User buildUser(final String id, final String email, final String groupId, final String customerId,
        final String level) {
        final User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstname("Jean");
        user.setLastname("DUPONT");
        user.setCustomerId(customerId);
        user.setGroupId(groupId);
        user.setOtp(true);
        user.setIdentifier("identifier_" + id);
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setLanguage("FRENCH");
        user.setLevel(level);
        user.setMobile("+33671270699");
        user.setPhone("+33134237766");
        user.setPasswordExpirationDate(OffsetDateTime.now().plusDays(1));
        user.setAddress(buildAddress());
        return user;
    }

    /**
     * BasicUserDto
     */
    public static UserDto buildUserDto() {
        return buildUserDto(USER_ID, USER_MAIL, GROUP_ID);
    }

    public static UserDto buildUserDto(final String id, final String email, final String groupId) {
        return buildUserDto(id, email, groupId, CUSTOMER_ID);
    }

    public static UserDto buildUserDto(final String id, final String email, final String groupId,
        final String customerId) {
        return IamDtoBuilder.buildUserDto(id, email, groupId, customerId, LEVEL);
    }

    /**
     * ExtUserDto
     */
    public static AuthUserDto buildAuthUserDto() {
        final AuthUserDto extUserDto = new AuthUserDto(buildUserDto());
        final GroupDto group = new GroupDto();
        group.setProfiles(Arrays.asList(buildProfileDto()));
        extUserDto.setProfileGroup(group);
        return extUserDto;
    }

    /**
     * Profile
     */
    public static Profile buildProfile() {
        return buildProfile(PROFILE_ID, PROFILE_IDENTIFIER, PROFILE_NAME, CUSTOMER_ID, TENANT_IDENTIFIER, APP_NAME);
    }

    public static Profile buildProfile(final String id, final String identifier, final String name,
        final String customerId, final Integer tenantId,
        final String applicationName) {
        return buildProfile(id, identifier, name, customerId, tenantId, applicationName, LEVEL);
    }

    public static Profile buildProfile(final String id, final String identifier, final String name,
        final String customerId, final Integer tenantId,
        final String applicationName, final String level) {
        final Profile profile = new Profile();
        profile.setId(id);
        profile.setIdentifier(identifier);
        profile.setEnabled(true);
        profile.setName(name);
        profile.setCustomerId(customerId);
        profile.setTenantIdentifier(tenantId);
        profile.setDescription("description");
        profile.setApplicationName(applicationName);
        profile.setLevel(level);
        profile.setRoles(IamServerUtilsTest.ROLES);
        return profile;
    }

    /**
     * ProfileDto
     */
    public static ProfileDto buildProfileDto() {
        return buildProfileDto(PROFILE_ID, PROFILE_NAME, CUSTOMER_ID, TENANT_IDENTIFIER, APP_NAME);
    }

    public static ProfileDto buildProfileDto(final String id, final String name, final String customerId,
        final Integer tenantId,
        final String applicationName) {
        return IamDtoBuilder.buildProfileDto(id, name, customerId, tenantId, applicationName, LEVEL, ROLES);
    }

    /**
     * Application
     */
    public static Application buildApplication() {
        return buildApplication(APPLICATION_ID, APPLICATION_URL);
    }

    public static Application buildApplication(final String id, final String url) {
        final Application application = new Application();
        application.setId(id);
        application.setIdentifier(id);
        application.setUrl(url);
        return application;
    }

    public static ApplicationDto buildApplicationDto() {
        return IamDtoBuilder.buildApplicationDto(APPLICATION_ID, APPLICATION_IDENTIFIER, APPLICATION_URL);
    }

    public static ApplicationDto buildApplicationDto(final String id, final String url) {
        return IamDtoBuilder.buildApplicationDto(id, id, url);
    }

    /**
     * Group
     */
    public static Group buildGroup() {
        return buildGroup(GROUP_ID, GROUP_IDENTIFIER, GROUP_NAME, CUSTOMER_ID, LEVEL);
    }

    public static Group buildGroup(final String id, final String identifier, final String name,
        final String customerId) {
        return buildGroup(id, identifier, name, customerId, LEVEL);
    }

    public static Group buildGroup(final String id, final String identifier, final String name, final String customerId,
        final String level) {
        final Group group = new Group();
        final List<String> profileIds = new ArrayList<>();
        profileIds.add("1");
        group.setId(id);
        group.setName(name);
        group.setCustomerId(customerId);
        group.setProfileIds(profileIds);
        group.setDescription("description");
        group.setIdentifier(identifier);
        group.setEnabled(true);
        group.setLevel(level);
        return group;
    }

    /**
     * Group DTO
     */
    public static GroupDto buildGroupDto() {
        return IamDtoBuilder.buildGroupDto(GROUP_ID, GROUP_NAME, CUSTOMER_ID, Arrays.asList(PROFILE_ID), LEVEL);
    }

    public static GroupDto buildGroupDto(final String id, final String name, final String customerId,
        final List<String> profileIds) {
        return IamDtoBuilder.buildGroupDto(id, name, customerId, profileIds, LEVEL);
    }

    /**
     * Customer
     */
    public static Customer buildCustomer() {
        return buildCustomer(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_CODE, EMAIL_DOMAINS);
    }

    public static Customer buildCustomer(final String id, final String name, final String code,
        final List<String> emailDomains) {

        final Address address = buildAddress();

        final Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setEnabled(true);
        customer.setCode(code);
        customer.setCompanyName("companyName");
        customer.setLanguage(LanguageDto.FRENCH.toString());
        customer.setPasswordRevocationDelay(6);
        customer.setOtp(OtpEnum.OPTIONAL);
        customer.setEmailDomains(emailDomains);
        customer.setDefaultEmailDomain(emailDomains.get(0));
        customer.setAddress(address);
        customer.setGraphicIdentity(new GraphicIdentity());
        return customer;
    }

    /**
     * CustomerDto
     */
    public static CustomerDto buildCustomerDto() {
        return IamDtoBuilder.buildCustomerDto(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_CODE, USER_MAIL);
    }

    /**
     * Tenant
     */
    public static Tenant buildTenant() {
        return buildTenant(TENANT_ID, TENANT_NAME, TENANT_IDENTIFIER);
    }

    public static Tenant buildTenant(final String id, final String name, final Integer tenantIdentifier,
        final String customerId) {
        final Tenant tenant = buildTenant(id, name, tenantIdentifier);
        tenant.setCustomerId(customerId);
        return tenant;
    }

    public static Tenant buildTenant(final String id, final String name, final Integer tenantIdentifier) {
        final Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setEnabled(true);
        tenant.setName(name);
        tenant.setOwnerId("ownerId");
        tenant.setIdentifier(tenantIdentifier);
        tenant.setProof(false);
        tenant.setCustomerId("customerId");
        return tenant;
    }

    /**
     * TenantDto
     */
    public static TenantDto buildTenantDto() {
        return IamDtoBuilder.buildTenantDto(TENANT_ID, TENANT_NAME, TENANT_IDENTIFIER, OWNER_ID, CUSTOMER_ID);
    }

    /**
     * OwnerDto
     */
    public static Owner buildOwner() {
        return buildOwner(OWNER_ID, OWNER_NAME, CUSTOMER_ID);
    }

    /**
     * return new owner
     *
     * @param id
     * @return
     */
    public static Owner builOwner(final String id) {
        return buildOwner(id, OWNER_NAME, CUSTOMER_ID);
    }

    public static Owner buildOwner(final String id, final String name, final String customerId) {
        final Address address = buildAddress();

        final Owner owner = new Owner();
        owner.setId(id);
        owner.setName(name);
        owner.setCode("owner1");
        owner.setReadonly(false);
        owner.setAddress(address);
        owner.setCustomerId(customerId);
        owner.setCompanyName("companyName");
        return owner;
    }

    /**
     * OwnerDto
     */
    public static OwnerDto buildOwnerDto() {
        return IamDtoBuilder.buildOwnerDto(OWNER_ID, OWNER_NAME, CUSTOMER_ID);
    }

    /**
     * IdentityProviderDto
     */
    public static IdentityProviderDto buildIdentityProviderDto() {
        return IamDtoBuilder.buildIdentityProviderDto(IDP_ID, IDP_NAME, CUSTOMER_ID, EMAIL_DOMAINS, true);
    }

    public static IdentityProvider buildIdentityProvider() {
        final IdentityProvider idp = new IdentityProvider();
        idp.setId(IDP_ID);
        idp.setName(IDP_NAME);
        idp.setCustomerId(CUSTOMER_ID);
        idp.setEnabled(true);
        idp.setInternal(true);
        idp.setIdentifier(String.valueOf(IDP_IDENTIFIER));
        return idp;
    }

    public static Address buildAddress() {
        final Address address = new Address();
        address.setCity("paris");
        address.setCountry("france");
        address.setZipCode("75009");
        address.setStreet("rue faubourg poissoniére");
        return address;
    }

}
