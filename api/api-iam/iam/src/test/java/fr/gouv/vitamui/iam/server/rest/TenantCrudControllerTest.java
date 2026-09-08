package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.configuration.VitamConfigurationDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.test.rest.CrudControllerTest;
import fr.gouv.vitamui.commons.vitam.api.administration.ConfigurationService;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
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
import fr.gouv.vitamui.iam.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TenantController}.
 */
public final class TenantCrudControllerTest implements CrudControllerTest {

    private AutoCloseable mocks;

    private static final String TENANT_ID = "tenantId";
    private static final String POFILE_NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String LEVEL = "level";
    private static final String APP_NAME = "application";
    private static final String ROLE = "role";

    private static final Integer TENANT_IDENTIFIER = 10;

    private TenantController controller;

    @InjectMocks
    private TenantService tenantService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    protected SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserService userService;

    @Mock
    private SecurityService securityService;

    @Mock
    protected CustomerService customerService;

    @Mock
    protected OwnerService ownerService;

    @Mock
    protected ProfileService profileService;

    @Mock
    private GroupService groupService;

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
    protected ExternalParametersService externalParametersService;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        Mockito.when(tenantConverter.convertDtoToEntity(ArgumentMatchers.any())).thenCallRealMethod();
        Mockito.when(tenantConverter.convertEntityToDto(ArgumentMatchers.any())).thenCallRealMethod();
        controller = new TenantController(tenantService);
    }

    protected void prepareServices() {
        final TenantDto tenantDto = buildTenantDto();

        final Tenant proofTenant = buildTenant();
        proofTenant.setProof(true);

        final ProfileDto profileDto = buildProfileDto();
        final UserDto userProfile = new UserDto();
        userProfile.setId("userId");

        when(tenantRepository.findByIdentifier(tenantDto.getIdentifier())).thenReturn(null);
        when(tenantRepository.findByCustomerIdAndProofIsTrue(tenantDto.getCustomerId())).thenReturn(
            Optional.of(proofTenant)
        );
        when(tenantRepository.existsById(any())).thenReturn(true);
        when(tenantRepository.save(any())).thenReturn(buildTenant());

        when(tenantRepository.findByIdAndCustomerId(tenantDto.getId(), tenantDto.getCustomerId())).thenReturn(
            Optional.of(buildTenant())
        );

        when(customerRepository.findById(tenantDto.getCustomerId())).thenReturn(
            Optional.of(IamServerUtilsTest.buildCustomer())
        );

        when(customerService.getMany(tenantDto.getCustomerId())).thenReturn(Arrays.asList(new CustomerDto()));

        when(sequenceGeneratorService.getNextSequenceId(anyString(), anyInt())).thenReturn(1);

        when(profileService.create(any())).thenReturn(profileDto);
        when(profileService.internalConvertFromEntityToDto(any())).thenReturn(profileDto);

        when(ownerService.getOne(tenantDto.getOwnerId(), Optional.empty())).thenReturn(buildOwnerDto());

        when(groupService.getOneByPassSecurity(buildUserDto().getGroupId(), Optional.empty())).thenReturn(
            buildGroupDto()
        );
        when(ownerRepository.findById(tenantDto.getOwnerId())).thenReturn(Optional.of(buildOwner()));

        when(ownerService.getMany(any(String.class))).thenReturn(Arrays.asList(buildOwnerDto()));
        when(tenantRepository.findById(tenantDto.getId())).thenReturn(Optional.of(buildTenant()));

        when(userService.getDefaultAdminUser(proofTenant.getCustomerId())).thenReturn(buildUserDto());
        when(userService.getAll(any(QueryDto.class))).thenReturn(Arrays.asList(buildUserDto()));
        when(externalParametersRepository.findByIdentifier(anyString())).thenReturn(
            Optional.of(buildExternalParameter())
        );
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
        when(customerInitConfig.getTenantProfiles()).thenReturn(
            Arrays.asList(
                new CustomerInitConfig.ProfileInitConfig[] {
                    new CustomerInitConfig.ProfileInitConfig(
                        APP_NAME,
                        DESCRIPTION,
                        LEVEL,
                        APP_NAME,
                        Arrays.asList(new String[] { ROLE })
                    ),
                }
            )
        );

        VitamConfigurationDto vitamConfigurationDto = new VitamConfigurationDto();
        vitamConfigurationDto.setTenants(List.of(dto.getIdentifier()));

        when(profileRepository.save(any())).thenReturn(IamServerUtilsTest.buildProfile());
        Mockito.when(configurationService.getVitamPublicConfigurations()).thenReturn(vitamConfigurationDto);
        Mockito.when(securityService.getTenant(ArgumentMatchers.any())).thenReturn(dto);
        prepareServices();
        controller.create(dto);
        verify(profileRepository, times(3)).save(any());
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
    void testCreationFailsAsCustomerDoesNotExist() throws Exception {
        final TenantDto dto = buildTenantDto();
        dto.setId(null);
        dto.setCustomerId("Bad customerId");

        prepareServices();

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("No customer found with id Bad customerId", e.getMessage());
        }
    }

    @Test
    void testCreationFailsAsOwnerDoesNotExist() throws Exception {
        final TenantDto dto = buildTenantDto();
        dto.setId(null);

        prepareServices();
        when(ownerRepository.findById(dto.getOwnerId())).thenReturn(Optional.empty());

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create tenant " + dto.getName() + ": owner " + dto.getOwnerId() + " does not exist",
                e.getMessage()
            );
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
    void testUpdateFailsAsIdentifierIsDifferent() throws InvalidParseOperationException, PreconditionFailedException {
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
                "Unable to update tenant " +
                dto.getId() +
                ": tenant identifiers " +
                tenant.getIdentifier() +
                " and " +
                dto.getIdentifier() +
                " are not equals",
                e.getMessage()
            );
        }
    }

    @Test
    void testCannotDelete() {
        assertThrows(UnsupportedOperationException.class, () -> {
            prepareServices();
            controller.delete(TENANT_ID);
        });
    }

    @Test
    void testUpdateFailsAsCustomerDoesNotExist() throws Exception {
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

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
}
