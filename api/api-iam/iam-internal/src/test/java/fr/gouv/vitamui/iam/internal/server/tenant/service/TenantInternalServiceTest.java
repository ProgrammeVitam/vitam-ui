package fr.gouv.vitamui.iam.internal.server.tenant.service;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.service.CustomerInternalService;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TenantInternalServiceTest {

    @InjectMocks
    private TenantInternalService internalTenantService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerInternalService internalCustomerService;

    @Mock
    private UserInternalService internalUserService;

    @Mock
    private OwnerInternalService internalOwnerService;

    @Mock
    private GroupInternalService internalGroupService;

    @Mock
    private ProfileInternalService internalProfileService;

    @Mock
    private CustomSequenceRepository customSequenceRepository;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private TenantConverter tenantConverter;

    @Mock
    private AccessContractService accessContractService;

    @Mock
    private InitVitamTenantService initVitamTenantService;

    @Mock
    private CustomerInitConfig customerInitConfig;

    @Mock
    private ExternalParametersRepository externalParametersRepository;

    @Mock
    private ExternalParametersInternalService externalParametersInternalService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(tenantConverter.convertEntityToDto(ArgumentMatchers.any())).thenCallRealMethod();
        Mockito.when(tenantConverter.convertDtoToEntity(ArgumentMatchers.any())).thenCallRealMethod();
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        when(externalParametersRepository.findByIdentifier(Mockito.any(String.class)))
            .thenReturn(Optional.of(buildExternalParameter()));
    }

    protected void prepareServices() {
        final TenantDto tenantDto = buildTenantDto();
        tenantDto.setId(null);

        final Tenant proofTenant = buildTenant();
        proofTenant.setProof(true);

        final ProfileDto profileDto = buildProfileDto();
        final UserDto userProfile = new UserDto();
        userProfile.setId("userId");

        when(customerRepository.findById(tenantDto.getCustomerId()))
            .thenReturn(Optional.of(IamServerUtilsTest.buildCustomer()));

        when(ownerRepository.findById(tenantDto.getOwnerId())).thenReturn(Optional.of(buildOwner()));

        when(tenantRepository.findByIdentifier(tenantDto.getIdentifier())).thenReturn(null);
        when(tenantRepository.findByCustomerIdAndProofIsTrue(tenantDto.getCustomerId()))
            .thenReturn(Optional.of(proofTenant));
        when(tenantRepository.generateSuperId()).thenReturn(tenantDto.getId());
        when(tenantRepository.save(any())).thenReturn(buildTenant());
        when(customSequenceRepository.incrementSequence(anyString(), anyInt()))
            .thenReturn(Optional.of(new CustomSequence()));

        when(internalProfileService.create(any())).thenReturn(profileDto);
        when(internalProfileService.internalConvertFromEntityToDto(any())).thenReturn(profileDto);

        when(internalOwnerService.getOne(tenantDto.getOwnerId(), Optional.empty())).thenReturn(buildOwnerDto());

        when(internalUserService.getDefaultAdminUser(proofTenant.getCustomerId())).thenReturn(buildUserDto());

        when(internalGroupService.getOne(buildUserDto().getGroupId(), Optional.empty(), Optional.empty()))
            .thenReturn(buildGroupDto());
        when(internalUserService.getAll(any(QueryDto.class))).thenReturn(Arrays.asList(buildUserDto()));


    }

    @Test
    public void createTenant_searchProfileCreated() {
        final TenantDto tenantDto = buildTenantDto();
        tenantDto.setId(null);

        when(profileRepository.save(any())).thenReturn(IamServerUtilsTest.buildProfile());

        prepareServices();
        internalTenantService.create(tenantDto);
    }

    @Test
    public void testProcessPatchSuccess() {
        final Tenant entity = new Tenant();
        final Tenant other = IamServerUtilsTest.buildTenant();

        final Map<String, Object> partialDto = TestUtils.getMapFromObject(other);

        when(internalOwnerService.getOne(any(), eq(Optional.empty()))).thenReturn(IamServerUtilsTest.buildOwnerDto());

        internalTenantService.processPatch(entity, partialDto);

        entity.setId(other.getId());
        entity.setCustomerId(other.getCustomerId());
        entity.setReadonly(other.isReadonly());
        entity.setIdentifier(other.getIdentifier());

        assertThat(entity).isEqualToComparingFieldByField(other);
    }

    private GroupDto buildGroupDto() {
        return IamServerUtilsTest.buildGroupDto();
    }

    private UserDto buildUserDto() {
        return IamServerUtilsTest.buildUserDto();
    }

    private OwnerDto buildOwnerDto() {
        return IamServerUtilsTest.buildOwnerDto();
    }

    private TenantDto buildTenantDto() {
        return IamServerUtilsTest.buildTenantDto();
    }

    private Tenant buildTenant() {
        return IamServerUtilsTest.buildTenant();
    }

    private ProfileDto buildProfileDto() {
        return IamServerUtilsTest.buildProfileDto();
    }

    private Owner buildOwner() {
        return IamServerUtilsTest.buildOwner();
    }

    public ExternalParameters buildExternalParameter() {
        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setIdentifier("identifierdefault_ac_customerId");
        externalParameters.setName("identifierdefault_ac_customerId");
        return externalParameters;
    }

}
