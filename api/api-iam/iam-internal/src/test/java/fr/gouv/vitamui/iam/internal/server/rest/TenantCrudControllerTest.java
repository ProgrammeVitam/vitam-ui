package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
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
import fr.gouv.vitamui.iam.internal.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TenantInternalController}.
 */
public final class TenantCrudControllerTest implements InternalCrudControllerTest {

    private static final String TENANT_ID = "tenantId";
    private static final String POFILE_NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String LEVEL = "level";
    private static final String APP_NAME = "application";
    private static final String ROLE = "role";

    private TenantInternalController controller;

    @InjectMocks
    private TenantInternalService internalTenantService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    protected CustomSequenceRepository customSequenceRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserInternalService internalUserService;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    protected CustomerInternalService internalCustomerService;

    @Mock
    protected OwnerInternalService internalOwnerService;

    @Mock
    protected ProfileInternalService internalProfileService;

    @Mock
    private GroupInternalService internalGroupService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private TenantConverter tenantConverter;

    @Mock
    private InitVitamTenantService initVitamTenantService;

    @Mock
    protected CustomerInitConfig customerInitConfig;
    @Mock
    protected ExternalParametersRepository externalParametersRepository;

    @Mock
    protected ExternalParametersInternalService externalParametersInternalService;


    @Before
    public void setup() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        MockitoAnnotations.initMocks(this);
        Mockito.when(tenantConverter.convertDtoToEntity(ArgumentMatchers.any())).thenCallRealMethod();
        Mockito.when(tenantConverter.convertEntityToDto(ArgumentMatchers.any())).thenCallRealMethod();
        controller = new TenantInternalController(internalTenantService);
    }

    protected void prepareServices() {
        final TenantDto tenantDto = buildTenantDto();

        final Tenant proofTenant = buildTenant();
        proofTenant.setProof(true);

        final ProfileDto profileDto = buildProfileDto();
        final UserDto userProfile = new UserDto();
        userProfile.setId("userId");

        when(tenantRepository.findByIdentifier(tenantDto.getIdentifier())).thenReturn(null);
        when(tenantRepository.findByCustomerIdAndProofIsTrue(tenantDto.getCustomerId()))
            .thenReturn(Optional.of(proofTenant));
        when(tenantRepository.existsById(any())).thenReturn(true);
        when(tenantRepository.save(any())).thenReturn(buildTenant());

        when(tenantRepository.findByIdAndCustomerId(tenantDto.getId(), tenantDto.getCustomerId()))
            .thenReturn(Optional.of(buildTenant()));

        when(customerRepository.findById(tenantDto.getCustomerId()))
            .thenReturn(Optional.of(IamServerUtilsTest.buildCustomer()));

        when(internalCustomerService.getMany(tenantDto.getCustomerId())).thenReturn(Arrays.asList(new CustomerDto()));

        when(customSequenceRepository.incrementSequence(anyString(), anyInt()))
            .thenReturn(Optional.of(new CustomSequence()));

        when(internalProfileService.create(any())).thenReturn(profileDto);
        when(internalProfileService.internalConvertFromEntityToDto(any())).thenReturn(profileDto);

        when(internalOwnerService.getOne(tenantDto.getOwnerId(), Optional.empty())).thenReturn(buildOwnerDto());

        when(internalGroupService.getOne(buildUserDto().getGroupId(), Optional.empty(), Optional.empty()))
            .thenReturn(buildGroupDto());
        when(ownerRepository.findById(tenantDto.getOwnerId())).thenReturn(Optional.of(buildOwner()));

        when(internalOwnerService.getMany(any(String.class))).thenReturn(Arrays.asList(buildOwnerDto()));
        when(tenantRepository.findById(tenantDto.getId())).thenReturn(Optional.of(buildTenant()));

        when(internalUserService.getDefaultAdminUser(proofTenant.getCustomerId())).thenReturn(buildUserDto());
        when(internalUserService.getAll(any(QueryDto.class))).thenReturn(Arrays.asList(buildUserDto()));
        when(externalParametersRepository.findByIdentifier(anyString()))
            .thenReturn(Optional.of(buildExternalParameter()));

    }

    public ExternalParameters buildExternalParameter() {
        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setIdentifier("identifierdefault_ac_customerId");
        externalParameters.setName("identifierdefault_ac_customerId");
        return externalParameters;
    }

    @Test
    @Override
    public void testCreationOK() throws Exception {
        final TenantDto dto = buildTenantDto();
        dto.setId(null);
        when(customerInitConfig.getTenantProfiles()).thenReturn(Arrays.asList(
            new CustomerInitConfig.ProfileInitConfig[] {
                new CustomerInitConfig.ProfileInitConfig(APP_NAME, DESCRIPTION, LEVEL, APP_NAME,
                    Arrays.asList(new String[] {ROLE}))}));
        when(profileRepository.save(any())).thenReturn(IamServerUtilsTest.buildProfile());

        prepareServices();
        controller.create(dto);
        verify(profileRepository, times(2)).save(any());
    }

    @Test
    @Override
    public void testCreationFailsAsIdIsProvided() throws Exception {
        final TenantDto dto = buildTenantDto();
        prepareServices();

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Test
    @Override
    public void testCreationFailsAsCustomerDoesNotExist() throws Exception {
        final TenantDto dto = buildTenantDto();
        dto.setId(null);
        dto.setCustomerId("Bad customerId");

        prepareServices();

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to create tenant " + dto.getName() + ": customer does not exist", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsOwnerDoesNotExist() throws Exception {
        final TenantDto dto = buildTenantDto();
        dto.setId(null);

        prepareServices();
        when(ownerRepository.findById(dto.getOwnerId())).thenReturn(Optional.empty());

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to create tenant " + dto.getName() + ": owner " + dto.getOwnerId() + " does not exist",
                e.getMessage());
        }
    }

    @Override
    @Test
    public void testUpdateOK() throws Exception {
        final TenantDto dto = buildTenantDto();

        prepareServices();
        controller.update(dto.getId(), dto);
    }

    @Test
    @Override
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() throws Exception {
        final TenantDto dto = buildTenantDto();

        try {
            controller.update(dto.getId() + "x", dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must match the path identifier for update.", e.getMessage());
        }

    }

    @Test
    public void testUpdateFailsAsIdentifierIsDifferent() {
        final TenantDto dto = buildTenantDto();
        dto.setIdentifier(8435455);

        final Tenant tenant = buildTenant();

        prepareServices();
        when(tenantRepository.findByIdentifier(dto.getIdentifier())).thenReturn(tenant);
        when(tenantRepository.findByIdAndCustomerId(dto.getId(), dto.getCustomerId())).thenReturn(Optional.of(tenant));

        try {
            controller.update(dto.getId(), dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to update tenant " + dto.getId() + ": tenant identifiers " + tenant.getIdentifier() + " and " +
                    dto.getIdentifier()
                    + " are not equals", e.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotDelete() {
        prepareServices();
        controller.delete(TENANT_ID);
    }

    @Test
    @Override
    public void testUpdateFailsAsCustomerDoesNotExist() throws Exception {
        final TenantDto dto = buildTenantDto();
        dto.setCustomerId("Bad customerId");

        prepareServices();

        try {
            controller.update(dto.getId(), dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to update tenant tenantId: customer does not exist", e.getMessage());
        }

    }

    private TenantDto buildTenantDto() {
        return IamServerUtilsTest.buildTenantDto();
    }

    private ProfileDto buildProfileDto() {
        return IamServerUtilsTest.buildProfileDto();
    }

    private GroupDto buildGroupDto() {
        return IamServerUtilsTest.buildGroupDto();
    }

    private UserDto buildUserDto() {
        return IamServerUtilsTest.buildUserDto();
    }

    private Owner buildOwner() {
        return IamServerUtilsTest.buildOwner();
    }

    private OwnerDto buildOwnerDto() {
        return IamServerUtilsTest.buildOwnerDto();
    }

    private Tenant buildTenant() {
        return IamServerUtilsTest.buildTenant();
    }

}
