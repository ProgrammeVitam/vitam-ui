package fr.gouv.vitamui.iam.server.tenant.service;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.VitamConfigurationDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractCommonService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.configuration.ConfigurationService;
import fr.gouv.vitamui.iam.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.service.CustomerService;
import fr.gouv.vitamui.iam.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TenantServiceTest {

    private AutoCloseable mocks;

    @InjectMocks
    private TenantService tenantService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private UserService userService;

    @Mock
    private OwnerService ownerService;

    @Mock
    private GroupService groupService;

    @Mock
    private ProfileService profileService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private SecurityService securityService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private TenantConverter tenantConverter;

    @Mock
    private AccessContractCommonService accessContractCommonService;

    @Mock
    private InitVitamTenantService initVitamTenantService;

    @Mock
    private CustomerInitConfig customerInitConfig;

    @Mock
    private ExternalParametersRepository externalParametersRepository;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private ExternalParametersService externalParametersService;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        Mockito.when(tenantConverter.convertEntityToDto(ArgumentMatchers.any())).thenCallRealMethod();
        Mockito.when(tenantConverter.convertDtoToEntity(ArgumentMatchers.any())).thenCallRealMethod();

        when(externalParametersRepository.findByIdentifier(Mockito.any(String.class))).thenReturn(
            Optional.of(buildExternalParameter())
        );
    }

    protected void prepareServices() {
        final TenantDto tenantDto = buildTenantDto();
        tenantDto.setId(null);

        final Tenant proofTenant = buildTenant();
        proofTenant.setProof(true);

        final ProfileDto profileDto = buildProfileDto();
        final UserDto userProfile = new UserDto();
        userProfile.setId("userId");

        when(customerRepository.findById(tenantDto.getCustomerId())).thenReturn(
            Optional.of(IamServerUtilsTest.buildCustomer())
        );

        when(ownerRepository.findById(tenantDto.getOwnerId())).thenReturn(Optional.of(buildOwner()));

        when(tenantRepository.findByIdentifier(tenantDto.getIdentifier())).thenReturn(null);
        when(tenantRepository.findByCustomerIdAndProofIsTrue(tenantDto.getCustomerId())).thenReturn(
            Optional.of(proofTenant)
        );
        when(tenantRepository.generateSuperId()).thenReturn(tenantDto.getId());
        when(tenantRepository.save(any())).thenReturn(buildTenant());
        when(sequenceGeneratorService.getNextSequenceId(anyString(), anyInt())).thenReturn(1);

        when(profileService.create(any())).thenReturn(profileDto);
        when(profileService.internalConvertFromEntityToDto(any())).thenReturn(profileDto);

        when(ownerService.getOne(tenantDto.getOwnerId(), Optional.empty())).thenReturn(buildOwnerDto());

        when(userService.getDefaultAdminUser(proofTenant.getCustomerId())).thenReturn(buildUserDto());

        when(groupService.getOne(buildUserDto().getGroupId(), Optional.empty(), Optional.empty())).thenReturn(
            buildGroupDto()
        );
        when(userService.getAll(any(QueryDto.class))).thenReturn(Arrays.asList(buildUserDto()));
    }

    @Test
    public void createTenant_searchProfileCreated() {
        final TenantDto tenantDto = buildTenantDto();
        Integer someTenantId = 2;
        tenantDto.setId(null);
        tenantDto.setIdentifier(someTenantId);
        when(profileRepository.save(any())).thenReturn(IamServerUtilsTest.buildProfile());

        VitamConfigurationDto vitamConfigurationDto = new VitamConfigurationDto();
        vitamConfigurationDto.setTenants(List.of(someTenantId));

        when(profileRepository.save(any())).thenReturn(IamServerUtilsTest.buildProfile());
        Mockito.when(configurationService.getVitamPublicConfigurations()).thenReturn(vitamConfigurationDto);
        Mockito.when(securityService.getTenant(ArgumentMatchers.any())).thenReturn(tenantDto);
        prepareServices();
        tenantService.create(tenantDto);
    }

    @Test
    public void testProcessPatchSuccess() {
        final Tenant entity = new Tenant();
        final Tenant other = IamServerUtilsTest.buildTenant();

        final Map<String, Object> partialDto = TestUtils.getMapFromObject(other);

        when(ownerService.getOne(any(), eq(Optional.empty()))).thenReturn(IamServerUtilsTest.buildOwnerDto());

        tenantService.processPatch(entity, partialDto);

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

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
