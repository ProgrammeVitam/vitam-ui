package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.commons.test.rest.CrudControllerTest;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ProfileService}.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public final class ProfileCrudControllerTest extends AbstractMongoTests implements CrudControllerTest {

    private static final String PROFILE_ID = "profileId";

    private static final Integer TENANT_IDENTIFIER = 1000;

    private ProfileController controller;

    private ProfileService profileService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private SecurityService securityService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Autowired
    private CustomerInitConfig customerInitConfig;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        profileService = new ProfileService(
            sequenceGeneratorService,
            profileRepository,
            customerRepository,
            null,
            tenantRepository,
            null,
            securityService,
            iamLogbookService,
            new ProfileConverter(),
            null,
            customerInitConfig
        );
        controller = new ProfileController(profileService, securityService);

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setSequence(1);
        when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
    }

    private void prepareServices() {
        final ProfileDto profileDto = buildProfileDto();

        when(customerRepository.findById(profileDto.getCustomerId())).thenReturn(
            Optional.of(IamServerUtilsTest.buildCustomer())
        );
        when(tenantService.findByIdentifier(profileDto.getTenantIdentifier())).thenReturn(new TenantDto());
        when(tenantRepository.findByIdentifier(profileDto.getTenantIdentifier())).thenReturn(buildTenant());
        when(securityService.getUser()).thenReturn(buildAuthUserDto());
        when(securityService.getLevel()).thenReturn(buildAuthUserDto().getLevel());
        when(securityService.getCustomerId()).thenReturn(buildAuthUserDto().getCustomerId());
        when(securityService.isLevelAllowed(any())).thenReturn(true);
        when(securityService.getTenantIdentifier()).thenReturn(profileDto.getTenantIdentifier());
        when(profileRepository.findAll(anyList())).thenReturn(List.of(buildProfile()));
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
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException e) {
            Assertions.assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsCustomerDoesNotExist() throws Exception {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);
        dto.setCustomerId("Bad Customer Id");
        prepareServices();

        try {
            controller.create(dto);
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException e) {
            Assertions.assertEquals(
                "Unable to create profile " + dto.getName() + ": customerId " + dto.getCustomerId() + " is not allowed",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreationFailsAsTheTenantDoesNotExist()
        throws InvalidParseOperationException, PreconditionFailedException {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);

        prepareServices();
        when(tenantRepository.findByIdentifier(TENANT_IDENTIFIER)).thenReturn(null);

        try {
            controller.create(dto);
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException e) {
            Assertions.assertEquals(
                "Unable to create profile profileName: The tenant " + TENANT_IDENTIFIER + " does not exist",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreationFailsAsTheRoleDoesNotExist()
        throws InvalidParseOperationException, PreconditionFailedException {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);
        dto.setRoles(null);

        prepareServices();

        try {
            controller.create(dto);
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException e) {
            Assertions.assertEquals("Unable to create profile " + dto.getName() + ": no roles", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheRolesAreNotAllowed()
        throws InvalidParseOperationException, PreconditionFailedException {
        final Role role = new Role("Bad Role");
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);
        dto.setRoles(Arrays.asList(role));

        prepareServices();

        try {
            controller.create(dto);
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException e) {
            Assertions.assertEquals(
                "Unable to create profile " + dto.getName() + ": role " + role.getName() + " does not exist",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreationFailsAsTheNameAlreadyExists()
        throws InvalidParseOperationException, PreconditionFailedException {
        final ProfileDto dto = buildProfileDto();
        dto.setId(null);

        prepareServices();
        when(profileRepository.exists(any(Criteria.class))).thenReturn(true);
        when(securityService.getCustomerId()).thenReturn("customerId");
        Assertions.assertEquals(securityService.getCustomerId(), "customerId");

        try {
            controller.create(dto);
            Assertions.fail("should fail");
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            Assertions.assertEquals(
                "Unable to create profile " + dto.getName() + ": profile already exists",
                e.getMessage()
            );
        }
    }

    @Override
    @Test
    public void testUpdateOK() {
        final ProfileDto dto = buildProfileDto();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> controller.update(dto.getId(), dto));
    }

    @Override
    @Test
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() {
        final ProfileDto dto = buildProfileDto();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> controller.update("Bad Id", dto));
    }

    @Test
    public void testUpdateFailsAsCustomerDoesNotExist() {
        final ProfileDto dto = buildProfileDto();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> controller.update("Bad Id", dto));
    }

    @Test
    public void testCannotDelete() throws PreconditionFailedException {
        prepareServices();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> controller.delete(PROFILE_ID));
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
