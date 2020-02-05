package fr.gouv.vitamui.iam.internal.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.test.utils.FieldUtils;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Criteria;

import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Tests the {@link ProfileInternalService}.
 *
 *
 */
public final class ProfileCrudControllerTest extends AbstractServerIdentityBuilder implements InternalCrudControllerTest {

    private static final String PROFILE_ID = "profileId";

    private static final Integer TENANT_IDENTIFIER = 1000;

    private ProfileInternalController controller;

    @InjectMocks
    private ProfileInternalService internalProfileService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantInternalService internalTenantService;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private CustomSequenceRepository sequenceRepository;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private ProfileConverter profileConverter;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        controller = new ProfileInternalController(internalProfileService);

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setSequence(1);
        when(sequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(customSequence));
        FieldUtils.setFinalStatic(CustomerInitConfig.class.getDeclaredField("allRoles"), ServicesData.getAllRoles());
    }

    protected void prepareServices() {
        final ProfileDto profileDto = buildProfileDto();

        when(customerRepository.findById(profileDto.getCustomerId())).thenReturn(Optional.of(IamServerUtilsTest.buildCustomer()));
        when(internalTenantService.findByIdentifier(profileDto.getTenantIdentifier())).thenReturn(new TenantDto());
        when(tenantRepository.findByIdentifier(profileDto.getTenantIdentifier())).thenReturn(buildTenant());
        when(internalSecurityService.getUser()).thenReturn(buildAuthUserDto());
        when(internalSecurityService.getLevel()).thenReturn(buildAuthUserDto().getLevel());
        when(internalSecurityService.getCustomerId()).thenReturn(buildAuthUserDto().getCustomerId());
        when(internalSecurityService.isLevelAllowed(any())).thenReturn(true);
        when(internalSecurityService.getCustomerId()).thenReturn(buildCustomerDto().getId());
        when(profileRepository.findAll(anyList())).thenReturn(Arrays.asList(buildProfile()));
        when(profileRepository.save(any())).thenReturn(buildProfile());
        when(profileRepository.exists(any(Criteria.class))).thenReturn(false);
    }

    @Override
    @Test
    public void testCreationOK() throws Exception {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);

        prepareServices();
        controller.create(dto);
    }

    @Override
    @Test
    public void testCreationFailsAsIdIsProvided() throws Exception {
        final ProfileDto dto = buildProfileDto();
        prepareServices();

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }

    }

    @Override
    @Test
    public void testCreationFailsAsCustomerDoesNotExist() throws Exception {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);
        dto.setCustomerId("Bad Customer Id");
        prepareServices();

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create profile " + dto.getName() + ": customerId " + dto.getCustomerId() + " is not allowed", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheTenantDoesNotExist() {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);

        prepareServices();
        when(tenantRepository.findByIdentifier(TENANT_IDENTIFIER)).thenReturn(null);

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create profile profileName: The tenant " + TENANT_IDENTIFIER + " does not exist", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheRoleDoesNotExist() {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);
        dto.setRoles(null);

        prepareServices();

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create profile " + dto.getName() + ": no roles", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheRolesAreNotAllowed() {
        final Role role = new Role("Bad Role");
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);
        dto.setRoles(Arrays.asList(role));

        prepareServices();

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create profile " + dto.getName() + ": role " + role.getName() + " does not exist", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheNameAlreadyExists() {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);

        prepareServices();
        when(profileRepository.exists(any(Criteria.class))).thenReturn(true);

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create profile " + dto.getName() + ": profile already exists", e.getMessage());
        }
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateOK() {
        final ProfileDto dto = buildProfileDto();
        controller.update(dto.getId(), dto);
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() {
        final ProfileDto dto = buildProfileDto();
        controller.update("Bad Id", dto);
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateFailsAsCustomerDoesNotExist() throws Exception {
        final ProfileDto dto = buildProfileDto();
        controller.update("Bad Id", dto);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotDelete() {
        prepareServices();
        controller.delete(PROFILE_ID);
    }

    private AuthUserDto buildAuthUserDto() {
        return IamServerUtilsTest.buildAuthUserDto();
    }

    private Profile buildProfile() {
        return IamServerUtilsTest.buildProfile();
    }

    private ProfileDto buildProfileDto() {
        return IamServerUtilsTest.buildProfileDto();
    }

    private Tenant buildTenant() {
        return IamServerUtilsTest.buildTenant();
    }

    private CustomerDto buildCustomerDto() {
        return IamServerUtilsTest.buildCustomerDto();
    }

}
