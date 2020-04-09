package fr.gouv.vitamui.utils;

import static fr.gouv.vitamui.utils.TestConstants.ADMIN_LEVEL;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_CUSTOMER_ID;
import static fr.gouv.vitamui.utils.TestConstants.SYSTEM_USER_PROFILE_ID;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.InvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;

public class FactoryDto {
    
    private static Integer proofTenantIdentitfier = 10;
    
    @Autowired
    public FactoryDto(Environment env) {
        proofTenantIdentitfier = Integer.valueOf(env.getProperty("vitamui_platform_informations.proof_tenant"));
    }

    public static <T> T buildDto(final Class<T> clazz) {
        T dto = null;
        if (clazz.equals(CustomerDto.class)) {
            dto = (T) buildBasicCustomerDto();
        }
        else if (clazz.equals(OwnerDto.class)) {
            dto = (T) buildOwnerDto();
        }
        else if (clazz.equals(TenantDto.class)) {
            dto = (T) buildTenantDto();
        }
        else if (clazz.equals(ProfileDto.class)) {
            dto = (T) buildProfileDto();
        }
        else if (clazz.equals(GroupDto.class)) {
            dto = (T) buildGroupDto();
        }
        else if (clazz.equals(UserDto.class)) {
            dto = (T) buildBasicUserDto();
        }
        else if (clazz.equals(IdentityProviderDto.class)) {
            dto = (T) buildIdentityProviderDto();
        }
        else {
            throw new InvalidArgumentException("build method not implemented for class " + clazz);
        }
        return dto;
    }

    private static String randomString() {
        return RandomStringUtils.random(30, true, false);
    }

    private static IdentityProviderDto buildIdentityProviderDto() {
        return IamDtoBuilder.buildIdentityProviderDto(null, "idp-" + randomString(), SYSTEM_CUSTOMER_ID, Arrays.asList(randomString()), false);
    }

    private static GroupDto buildGroupDto() {
        return IamDtoBuilder.buildGroupDto(null, randomString(), SYSTEM_CUSTOMER_ID, Arrays.asList(SYSTEM_USER_PROFILE_ID), ADMIN_LEVEL);
    }

    private static ProfileDto buildProfileDto() {
        return IamDtoBuilder.buildProfileDto(null, randomString(), SYSTEM_CUSTOMER_ID, proofTenantIdentitfier, "applicationName", ADMIN_LEVEL,
                Arrays.asList(new Role(ServicesData.ROLE_CREATE_PROFILES)));
    }

    private static TenantDto buildTenantDto() {
        return IamDtoBuilder.buildTenantDto(null, randomString(), null, null, SYSTEM_CUSTOMER_ID);
    }

    private static OwnerDto buildOwnerDto() {
        return IamDtoBuilder.buildOwnerDto(null, randomString(), SYSTEM_CUSTOMER_ID);
    }

    private static CustomerDto buildBasicCustomerDto() {
        return IamDtoBuilder.buildCustomerDto(null, randomString(), Integer.toString(ThreadLocalRandom.current().nextInt(1000000, 10000000)),
                randomString() + ".com");
    }

    private static UserDto buildBasicUserDto() {
        final UserDto user = IamDtoBuilder.buildUserDto(null, randomString().toLowerCase() + "@test.com",
                "5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363", SYSTEM_CUSTOMER_ID, ADMIN_LEVEL);
        user.setIdentifier(null);
        return user;
    }

}
