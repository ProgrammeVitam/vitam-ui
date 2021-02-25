package fr.gouv.vitamui.iam.internal.server.customer.config;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;

public class CustomerInitConfigTest {

    private static final String PROFILE_NAME_1 = "profile1";
    private static final String PROFILE_NAME_2 = "profile2";
    private static final String PROFILE_NAME_3 = "profile3";
    private static final String GROUP_NAME_1 = "group1";
    private static final String GROUP_NAME_2 = "group2";
    private static final String LEVEL_1 = "1";
    private static final String LEVEL_2 = "2";
    private static final String DESCRIPTION_1 = "desc1";
    private static final String DESCRIPTION_2 = "desc2";
    private static final String DESCRIPTION_3 = "desc3";
    private static final String APP_NAME_1 = "app1";
    private static final String ROLE_1 = "role_1";
    private static final String ROLE_2 = "role_2";
    private static final String ROLE_3 = "role_3";
    private static final String LAST_NAME = "LASTNAME";
    private static final String FIRST_NAME = "FirstName";
    private static final String EMAIl = "a@mail.com";

    private CustomerInitConfig customerInitConfig;

    @Before
    public void initCustomerInitConfig(){
        customerInitConfig = new CustomerInitConfig();
    }

    @Test
    public void testAfterPropertiesWithEmptyConfig() throws Exception {
        customerInitConfig.afterPropertiesSet();
    }

    @Test
    public void testAfterPropertiesWithValidConfig() throws Exception {
        setValidGroupAndProfile();
        customerInitConfig.setUsers(Arrays.asList(new CustomerInitConfig.UserInitConfig(LAST_NAME,FIRST_NAME,GROUP_NAME_1,EMAIl,LEVEL_2)));
        customerInitConfig.afterPropertiesSet();
    }


    @Test
    public void testAfterPropertiesWithEmptyProfileNameForProfile(){
        customerInitConfig.setProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig("",null,null,null,null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name cannot be empty for profile config");
    }


    @Test
    public void testAfterPropertiesWithEmptyAppNameForProfile(){
        customerInitConfig.setProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,"",null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("app-name cannot be empty for profile config");
    }

    @Test
    public void testAfterPropertiesWithEmptyRolesForProfile(){
        customerInitConfig.setProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,new ArrayList<>())));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("roles list cannot be empty for profile config");
    }

    @Test
    public void testAfterPropertiesWithDuplicateRolesForProfile(){
        customerInitConfig.setProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_1, ROLE_1))));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("roles list must contains distinct roles for profile config");
    }

    @Test
    public void testAfterPropertiesWithDuplicateProfileName(){
        customerInitConfig.setProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_1, ROLE_2)),
            new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_1, ROLE_2))));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("profiles list contains duplicate name for profile config");
    }



    @Test
    public void testAfterPropertiesWithEmptyProfileNameForTenantProfile(){
        customerInitConfig.setTenantProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig("",null,null,null,null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name cannot be empty for profile config");
    }

    @Test
    public void testAfterPropertiesWithEmptyAppNameForTenantProfile(){
        customerInitConfig.setTenantProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,"",null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("app-name cannot be empty for profile config");
    }

    @Test
    public void testAfterPropertiesWithEmptyRolesForTenantProfile(){
        customerInitConfig.setTenantProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,new ArrayList<>())));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("roles list cannot be empty for profile config");
    }

    @Test
    public void testAfterPropertiesWithDuplicateRolesForTenantProfile(){
        customerInitConfig.setTenantProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_1, ROLE_1))));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("roles list must contains distinct roles for profile config");
    }

    @Test
    public void testAfterPropertiesWithDuplicateTenantProfileName(){
        customerInitConfig.setTenantProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_1, ROLE_2)),
            new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_1, ROLE_2))));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("profiles list contains duplicate name for profile config");
    }

    @Test
    public void testAfterPropertiesWithEmptyProfileNameForGroup(){
        setValidProfiles();
        customerInitConfig.setProfilesGroups(Arrays.asList(new CustomerInitConfig.ProfilesGroupInitConfig("",null,null,null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name cannot be empty for profiles-groups config");
    }


    @Test
    public void testAfterPropertiesWithEmptyProfilesForGroup(){
        setValidProfiles();
        customerInitConfig.setProfilesGroups(Arrays.asList(new CustomerInitConfig.ProfilesGroupInitConfig(GROUP_NAME_1,DESCRIPTION_2,LEVEL_2,null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("profiles list cannot be empty for profiles-groups config");
    }

    @Test
    public void testAfterPropertiesWithUndefinedProfileForGroup(){
        setValidProfiles();
        customerInitConfig.setProfilesGroups(Arrays.asList(new CustomerInitConfig.ProfilesGroupInitConfig(GROUP_NAME_1,DESCRIPTION_2,LEVEL_2,Arrays.asList(PROFILE_NAME_3))));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("profile '" + PROFILE_NAME_3 + "' is not defined in profile config");
    }

    @Test
    public void testAfterPropertiesWithDuplicateProfilesForGroup(){
       setValidProfiles();
        customerInitConfig.setProfilesGroups(Arrays.asList(new CustomerInitConfig.ProfilesGroupInitConfig(GROUP_NAME_1,DESCRIPTION_2,LEVEL_2,Arrays.asList(PROFILE_NAME_1, PROFILE_NAME_1))));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("profiles list contains duplicate name for profiles-groups config");
    }

    @Test
    public void testAfterPropertiesWithDuplicateProfilesGroups(){
        setValidProfiles();
        customerInitConfig.setProfilesGroups(Arrays.asList(new CustomerInitConfig.ProfilesGroupInitConfig(GROUP_NAME_1,DESCRIPTION_2,LEVEL_2,Arrays.asList(PROFILE_NAME_1)),new CustomerInitConfig.ProfilesGroupInitConfig(GROUP_NAME_1,DESCRIPTION_2,LEVEL_2,Arrays.asList(PROFILE_NAME_1))));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("profiles group list contains duplicate name for profiles-groups config");
    }

    private void setValidProfiles(){
        customerInitConfig.setProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_1,DESCRIPTION_1,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_2))));
        customerInitConfig.setTenantProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_2,DESCRIPTION_1,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_1))));
    }


    @Test
    public void testAfterPropertiesWithEmptyLastNameForUser(){
        setValidGroupAndProfile();
        customerInitConfig.setUsers(Arrays.asList(new CustomerInitConfig.UserInitConfig("",null,null,null,null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("last-name cannot be empty for users config");
    }

    @Test
    public void testAfterPropertiesWithEmptyFirstNameForUser(){
        setValidGroupAndProfile();
        customerInitConfig.setUsers(Arrays.asList(new CustomerInitConfig.UserInitConfig(LAST_NAME,"",null,null,null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("first-name cannot be empty for users config");
    }

    @Test
    public void testAfterPropertiesWithEmptyGroupNameForUser(){
        setValidGroupAndProfile();
        customerInitConfig.setUsers(Arrays.asList(new CustomerInitConfig.UserInitConfig(LAST_NAME,FIRST_NAME,"",null,null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("profiles-group-name cannot be null for users config");
    }

    @Test
    public void testAfterPropertiesWithUndefinedProfilesGroupForUser(){
        setValidGroupAndProfile();
        customerInitConfig.setUsers(Arrays.asList(new CustomerInitConfig.UserInitConfig(LAST_NAME,FIRST_NAME,GROUP_NAME_2,"",null)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("profiles group name is not defined in profile-groups config");
    }

    @Test
    public void testAfterPropertiesWithEmptyEmailForUser(){
        setValidGroupAndProfile();
        customerInitConfig.setUsers(Arrays.asList(new CustomerInitConfig.UserInitConfig(LAST_NAME,FIRST_NAME,GROUP_NAME_1,"",LEVEL_2)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("email cannot be empty for users config");
    }


    @Test
    public void testAfterPropertiesWithDuplicateEmailForUser(){
        setValidGroupAndProfile();
        customerInitConfig.setUsers(Arrays.asList(new CustomerInitConfig.UserInitConfig(LAST_NAME,FIRST_NAME,GROUP_NAME_1,EMAIl,LEVEL_2), new CustomerInitConfig.UserInitConfig(LAST_NAME,FIRST_NAME,GROUP_NAME_1,EMAIl,LEVEL_2)));
        assertThatThrownBy(() -> customerInitConfig.afterPropertiesSet())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("users list contains duplicate email for users config");
    }

    @Test
    public void testAfterPropertiesWithUnfoundOtherRoles() throws Exception {
        setValidProfiles();
        customerInitConfig.setAdminProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_3,DESCRIPTION_3,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_3))));
        customerInitConfig.setOtherRoles(null);
        int adminRolesSize = ServicesData.getAllRoles().size();

        customerInitConfig.afterPropertiesSet();

        assertThat(CustomerInitConfig.getAllRoles()).isNotNull();
        assertThat(CustomerInitConfig.getAllRoles().size()).isEqualTo(3 + adminRolesSize);
    }

    @Test
    public void testAfterPropertiesWithOtherRoles() throws Exception {
        setValidProfiles();
        customerInitConfig.setAdminProfiles(Arrays.asList(new CustomerInitConfig.ProfileInitConfig(PROFILE_NAME_3,DESCRIPTION_3,LEVEL_1,APP_NAME_1,Arrays.asList(ROLE_3))));
        customerInitConfig.setOtherRoles(Arrays.asList("role_4", "role_5"));
        int adminRolesSize = ServicesData.getAllRoles().size();

        customerInitConfig.afterPropertiesSet();

        assertThat(CustomerInitConfig.getAllRoles()).isNotNull();
        assertThat(CustomerInitConfig.getAllRoles().size()).isEqualTo(5 + adminRolesSize);
    }

    private void setValidGroupAndProfile(){
        setValidProfiles();
        customerInitConfig.setProfilesGroups(Arrays.asList(new CustomerInitConfig.ProfilesGroupInitConfig(GROUP_NAME_1,DESCRIPTION_2,LEVEL_2,Arrays.asList(PROFILE_NAME_1))));

    }

}
