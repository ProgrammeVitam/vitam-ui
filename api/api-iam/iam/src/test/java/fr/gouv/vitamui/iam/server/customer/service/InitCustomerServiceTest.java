package fr.gouv.vitamui.iam.server.customer.service;

import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.server.configuration.ConfigurationService;
import fr.gouv.vitamui.iam.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.group.domain.Group;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InitCustomerServiceTest {

    private static final String TENANT_NAME = "TENANT_NAME";
    private static final Integer TENANT_ID = 10;

    private static final String APP_NAME = "APP_NAME";

    private static final String FIRST_PROFILE_ID = "FIRST_PROFILE_ID";

    private static final String SECOND_PROFILE_ID = "SECOND_PROFILE_ID";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private IdentityProviderRepository identityProviderRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnerService ownerService;

    @Mock
    private TenantService tenantService;

    @Mock
    private UserService userService;

    @Mock
    private UserInfoService userInfoService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ProfileService profileService;

    @Mock
    private GroupService groupService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private OwnerConverter ownerConverter;

    @Mock
    private IdentityProviderConverter idpConverter;

    @Mock
    private InitVitamTenantService initVitamTenantService;

    @Mock
    private CustomerInitConfig customerInitConfig;

    @Mock
    private ExternalParametersService externalParametersService;

    @InjectMocks
    private InitCustomerService initCustomerService;

    @Test
    void shouldCreateProfiles_whenMultipleProfilesOnSameApp() {
        // Given
        CustomerDto customer = IamDtoBuilder.buildCustomerDto("id", "name", "code", "emailDomain");
        OwnerDto owner = IamDtoBuilder.buildOwnerDto("id", "name", "id");
        CustomerInitConfig.ProfileInitConfig restrictedProfile = createRestrictedProfile();
        when(customerInitConfig.getProfiles()).thenReturn(List.of(restrictedProfile));
        Profile profileCustomerInit1 = new Profile();
        profileCustomerInit1.setApplicationName(APP_NAME);
        profileCustomerInit1.setName(FIRST_PROFILE_ID);
        profileCustomerInit1.setRoles(new ArrayList<>());
        Profile profileCustomerInit2 = new Profile();
        profileCustomerInit2.setApplicationName(APP_NAME);
        profileCustomerInit2.setName(SECOND_PROFILE_ID);
        profileCustomerInit2.setRoles(new ArrayList<>());
        given(tenantService.getDefaultProfiles(any(), any())).willReturn(
            List.of(profileCustomerInit1, profileCustomerInit2)
        );

        given(profileRepository.save(any())).willAnswer(invocation -> {
            Profile profileSaved = invocation.getArgument(0, Profile.class);
            profileSaved.setId(profileSaved.getName());
            return profileSaved;
        });

        given(ownerConverter.convertDtoToEntity(any())).willReturn(
            new OwnerConverter(new AddressConverter()).convertDtoToEntity(owner)
        );
        given(ownerConverter.convertEntityToDto(any())).willReturn(owner);
        given(ownerRepository.generateSuperId()).willReturn("id");
        given(ownerRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0, Owner.class));

        given(tenantRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0, Tenant.class));
        given(initVitamTenantService.init(any(Tenant.class), ArgumentMatchers.any())).willReturn(new Tenant());

        given(groupRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0, Group.class));

        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);

        given(userInfoService.create(any())).willReturn(new UserInfoDto());

        // When
        initCustomerService.initCustomer(TENANT_ID, TENANT_NAME, customer, List.of(owner));

        // Then
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        Mockito.verify(profileRepository, times(8)).save(profileCaptor.capture());
        // Two profiles created on the same app
        Assertions.assertThat(profileCaptor.getAllValues())
            .filteredOn(profile -> profile.getApplicationName().equals(APP_NAME))
            .hasSize(2);

        // Only the first profile of the app in customer-init is affected to admin group

        verify(groupRepository, times(2)).save(groupCaptor.capture());

        List<Group> savedGroups = groupCaptor.getAllValues();

        Group firstSavedGroup = savedGroups.get(0);
        Group secondSavedGroup = savedGroups.get(1);

        Assertions.assertThat(firstSavedGroup.getName()).startsWith("ADMIN_CLIENT_ROOT");
        Assertions.assertThat(secondSavedGroup.getName()).startsWith("RESTRICTED_ADMIN_GROUP");

        Assertions.assertThat(firstSavedGroup.getProfileIds())
            .contains(FIRST_PROFILE_ID)
            .doesNotContain(SECOND_PROFILE_ID);
    }

    private CustomerInitConfig.ProfileInitConfig createRestrictedProfile() {
        CustomerInitConfig.ProfileInitConfig profile = new CustomerInitConfig.ProfileInitConfig();
        profile.setAppName("USERS_APP");
        profile.setName("Profil restreint pour la gestion des utilisateurs");
        profile.setDescription("Profil restreint pour la gestion des utilisateurs");
        profile.setRoles(
            List.of(
                "ROLE_GET_USERS",
                "ROLE_CREATE_USERS",
                "ROLE_UPDATE_USERS",
                "ROLE_UPDATE_STANDARD_USERS",
                "ROLE_MFA_USERS",
                "ROLE_ANONYMIZATION_USERS",
                "ROLE_GET_GROUPS",
                "ROLE_GET_USER_INFOS",
                "ROLE_CREATE_USER_INFOS",
                "ROLE_UPDATE_USER_INFOS"
            )
        );
        return profile;
    }
}
