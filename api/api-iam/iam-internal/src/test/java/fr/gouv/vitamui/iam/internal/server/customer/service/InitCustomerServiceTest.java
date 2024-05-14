package fr.gouv.vitamui.iam.internal.server.customer.service;

import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInfoInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
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

@ExtendWith(MockitoExtension.class)
public class InitCustomerServiceTest {

    private static final String TENANT_NAME = "TENANT_NAME";

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
    private OwnerInternalService internalOwnerService;

    @Mock
    private TenantInternalService internalTenantService;

    @Mock
    private UserInternalService internalUserService;

    @Mock
    private UserInfoInternalService userInfoInternalService;

    @Mock
    private ProfileInternalService internalProfileService;

    @Mock
    private GroupInternalService internalGroupService;

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
    private ExternalParametersInternalService externalParametersInternalService;

    @InjectMocks
    private InitCustomerService initCustomerService;

    @Test
    void shouldCreateProfiles_whenMultipleProfilesOnSameApp() {
        // Given
        CustomerDto customer = IamDtoBuilder.buildCustomerDto("id", "name", "code", "emailDomain");
        OwnerDto owner = IamDtoBuilder.buildOwnerDto("id", "name", "id");

        Profile profileCustomerInit1 = new Profile();
        profileCustomerInit1.setApplicationName(APP_NAME);
        profileCustomerInit1.setName(FIRST_PROFILE_ID);
        profileCustomerInit1.setRoles(new ArrayList<>());
        Profile profileCustomerInit2 = new Profile();
        profileCustomerInit2.setApplicationName(APP_NAME);
        profileCustomerInit2.setName(SECOND_PROFILE_ID);
        profileCustomerInit2.setRoles(new ArrayList<>());
        given(internalTenantService.getDefaultProfiles(any(), any())).willReturn(
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

        given(userInfoInternalService.create(any())).willReturn(new UserInfoDto());

        // When
        initCustomerService.initCustomer(TENANT_NAME, customer, List.of(owner));

        // Then
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        Mockito.verify(profileRepository, times(7)).save(profileCaptor.capture());
        // Two profiles created on the same app
        Assertions.assertThat(profileCaptor.getAllValues())
            .filteredOn(profile -> profile.getApplicationName().equals(APP_NAME))
            .hasSize(2);

        // Only the first profile of the app in customer-init is affected to admin group
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        Mockito.verify(groupRepository, times(1)).save(groupCaptor.capture());
        Assertions.assertThat(groupCaptor.getValue().getProfileIds()).contains(FIRST_PROFILE_ID);
        Assertions.assertThat(groupCaptor.getValue().getProfileIds()).doesNotContain(SECOND_PROFILE_ID);
    }
}
