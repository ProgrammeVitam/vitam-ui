package fr.gouv.vitamui.iam.commons.utils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.common.utils.DtoFactory;

public class IamDtoBuilder {

    public static UserDto buildUserDto(final String id, final String email, final String groupId, final String customerId, final String level) {
        final UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setEmail(email);
        userDto.setFirstname("Jean");
        userDto.setLastname("DUPONT");
        userDto.setCustomerId(customerId);
        userDto.setGroupId(groupId);
        userDto.setOtp(true);
        userDto.setIdentifier("code");
        userDto.setStatus(UserStatusEnum.ENABLED);
        userDto.setType(UserTypeEnum.NOMINATIVE);
        userDto.setLanguage(LanguageDto.FRENCH.toString());
        userDto.setLevel(level);
        userDto.setMobile("+33671270699");
        userDto.setPhone("+33134237766");
        userDto.setAddress(buildAddressDto());
        return userDto;
    }

    public static AuthUserDto buildAuthUserDto(final String id, final String email) {
        final AuthUserDto userDto = new AuthUserDto();
        userDto.setId(id);
        userDto.setEmail(email);
        userDto.setSubrogeable(true);
        userDto.setStatus(UserStatusEnum.ENABLED);
        userDto.setFirstname("Jean");
        userDto.setLastname("DUPONT");
        userDto.setCustomerId("customerId");
        userDto.setGroupId("groupId");
        userDto.setOtp(true);
        userDto.setIdentifier("code");
        userDto.setType(UserTypeEnum.NOMINATIVE);
        userDto.setLanguage(LanguageDto.FRENCH.toString());
        userDto.setLevel("level");
        userDto.setMobile("+33671270699");
        userDto.setPhone("+33134237766");
        userDto.setPasswordExpirationDate(OffsetDateTime.now().plusDays(1));
        userDto.setIdentifier("identifier");
        return userDto;
    }

    public static ProfileDto buildProfileDto(final String id, final String name, final String customerId, final Integer tenantId, final String applicationName,
            final String level, final List<Role> roles) {
        final ProfileDto profileDto = DtoFactory.buildProfileDto(name, "description", false, level, tenantId, applicationName, new ArrayList<String>(),
                customerId);
        profileDto.setId(id);
        profileDto.setRoles(roles);
        return profileDto;
    }

    public static ApplicationDto buildApplicationDto(final String id, final String identifier, final String url) {
        final ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(id);
        applicationDto.setIdentifier(identifier);
        applicationDto.setUrl(url);
        return applicationDto;
    }

    public static GroupDto buildGroupDto(final String id, final String name, final String customerId, final List<String> profileIds, final String level) {
        final GroupDto groupDto = new GroupDto();
        groupDto.setId(id);
        groupDto.setCustomerId(customerId);
        groupDto.setProfileIds(new ArrayList<>(profileIds));
        groupDto.setName(name);
        groupDto.setDescription("description");
        groupDto.setLevel(level);
        groupDto.setEnabled(true);
        groupDto.setReadonly(false);
        return groupDto;
    }

    public static CustomerDto buildCustomerDto(final String id, final String name, final String code, final String emailDomain) {
        final CustomerDto customer = new CustomerDto();
        customer.setId(id);
        customer.setName(name);
        customer.setCode(code);
        customer.setEnabled(true);
        customer.setCompanyName("companyName");
        customer.setLanguage(LanguageDto.FRENCH);
        customer.setPasswordRevocationDelay(6);
        customer.setOtp(OtpEnum.OPTIONAL);
        customer.setOwners(Arrays.asList(buildOwnerDto(null, name, id)));
        customer.setEmailDomains(Arrays.asList(emailDomain.toLowerCase()));
        customer.setDefaultEmailDomain(emailDomain.toLowerCase());
        customer.setAddress(buildAddressDto());
        customer.setHasCustomGraphicIdentity(false);
        return customer;
    }

    public static TenantDto buildTenantDto(final String id, final String name, final Integer tenantIdentifier, final String ownerId, final String customerId) {
        final TenantDto tenantDto = new TenantDto();
        tenantDto.setId(id);
        tenantDto.setEnabled(true);
        tenantDto.setName(name);
        tenantDto.setOwnerId(ownerId);
        tenantDto.setIdentifier(tenantIdentifier);
        tenantDto.setProof(false);
        tenantDto.setCustomerId(customerId);
        return tenantDto;
    }

    public static OwnerDto buildOwnerDto(final String id, final String name, final String customerId) {
        final OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(id);
        ownerDto.setName(name);
        ownerDto.setCompanyName(name);
        ownerDto.setCode(Integer.toString(ThreadLocalRandom.current().nextInt(1000000, 10000000)));
        ownerDto.setReadonly(false);
        ownerDto.setAddress(buildAddressDto());
        ownerDto.setCustomerId(customerId);
        return ownerDto;
    }

    public static SubrogationDto buildSubrogationDto(final String id, final String surrogate, final String superUser) {
        final SubrogationDto subrogation = new SubrogationDto();
        subrogation.setId(id);
        subrogation.setDate(OffsetDateTime.now());
        subrogation.setStatus(SubrogationStatusEnum.CREATED);
        subrogation.setSurrogate(surrogate);
        subrogation.setSuperUser(superUser);
        return subrogation;
    }

    public static IdentityProviderDto buildIdentityProviderDto(final String idpId, final String idpName, final String customerId, final List<String> patterns,
            final boolean internal) {
        final IdentityProviderDto idpDto = new IdentityProviderDto();
        idpDto.setId(idpId);
        idpDto.setName(idpName);
        idpDto.setCustomerId(customerId);
        idpDto.setEnabled(true);
        idpDto.setInternal(internal);
        idpDto.setPatterns(patterns);
        return idpDto;
    }

    public static AddressDto buildAddressDto() {
        final AddressDto address = new AddressDto();
        address.setCity("paris");
        address.setCountry("france");
        address.setZipCode("75009");
        address.setStreet("rue faubourg poissoniére");
        return address;
    }

}
