package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.test.rest.CrudControllerTest;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.group.domain.Group;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link GroupController}.
 *
 *
 */
public final class GroupControllerTest implements CrudControllerTest {

    private GroupController controller;

    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private SecurityService securityService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private GroupConverter groupConverter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(groupConverter.convertDtoToEntity(ArgumentMatchers.any())).thenCallRealMethod();
        Mockito.when(groupConverter.convertEntityToDto(ArgumentMatchers.any())).thenCallRealMethod();
        groupService = new GroupService(
            sequenceGeneratorService,
            groupRepository,
            customerRepository,
            profileService,
            userRepository,
            securityService,
            tenantRepository,
            iamLogbookService,
            groupConverter,
            null,
            null
        );

        controller = new GroupController(groupService, securityService);
    }

    protected void prepareServices() {
        final GroupDto dto = buildGroupDto();

        when(groupRepository.exists(any(CriteriaDefinition.class))).thenReturn(false);
        when(groupRepository.existsById(any())).thenReturn(true);

        when(securityService.isLevelAllowed(dto.getLevel())).thenCallRealMethod();
        when(securityService.getLevel()).thenReturn("");
        when(securityService.getCustomerId()).thenReturn(dto.getCustomerId());
        when(profileService.getMany(anyList(), any())).thenReturn(Arrays.asList(buildProfileDto()));

        when(customerRepository.findById(any())).thenReturn(Optional.of(buildCustomer()));

        final Tenant tenant = new Tenant();
        tenant.setCustomerId("customerId");
        when(tenantRepository.findByIdentifier(any())).thenReturn(tenant);

        when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
    }

    @Override
    @Test
    public void testCreationOK() throws Exception {
        final GroupDto dto = buildGroupDto();
        dto.setId(null);

        prepareServices();
        controller.create(dto);
    }

    @Override
    public void testCreationFailsAsIdIsProvided() throws Exception {
        final GroupDto dto = buildGroupDto();

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsCustomerDoesNotExist() throws Exception {
        final GroupDto dto = buildGroupDto();
        dto.setId(null);

        prepareServices();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create group " + dto.getName() + ": customer " + dto.getCustomerId() + " does not exist",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreationFailsAsTheProfileDoesNotExist()
        throws InvalidParseOperationException, PreconditionFailedException {
        final GroupDto dto = buildGroupDto();
        dto.setId(null);

        prepareServices();
        when(profileService.getMany(anyList(), any())).thenReturn(Collections.emptyList());

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to create group " + dto.getName() + ": no profiles", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheNameIsAlreadyUsed()
        throws InvalidParseOperationException, PreconditionFailedException {
        final GroupDto dto = buildGroupDto();
        dto.setId(null);

        prepareServices();
        when(
            groupRepository.exists(Criteria.where("customerId").is(dto.getCustomerId()).and("name").is(dto.getName()))
        ).thenReturn(true);

        try {
            controller.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to create group " + dto.getName() + ": group already exists", e.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    @Override
    public void testUpdateOK() {
        final GroupDto dto = buildGroupDto();

        prepareServices();
        when(groupRepository.findByIdAndCustomerId(anyString(), anyString())).thenReturn(Optional.of(buildGroup()));

        controller.update(dto.getId(), dto);
    }

    @Test(expected = UnsupportedOperationException.class)
    @Override
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() {
        final GroupDto dto = buildGroupDto();

        prepareServices();

        controller.update("Bad Id" + dto.getId(), dto);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateFailsAsCustomerDoesNotExist() {
        final GroupDto dto = buildGroupDto();

        prepareServices();
        when(groupRepository.findByIdAndCustomerId(anyString(), anyString())).thenReturn(Optional.empty());

        controller.update(dto.getId(), dto);
    }

    private Group buildGroup() {
        final Group group = IamServerUtilsTest.buildGroup();
        return group;
    }

    private GroupDto buildGroupDto() {
        final GroupDto groupDto = IamServerUtilsTest.buildGroupDto();
        return groupDto;
    }

    private Customer buildCustomer() {
        final Customer customer = IamServerUtilsTest.buildCustomer();
        return customer;
    }

    private ProfileDto buildProfileDto() {
        final TenantDto tenantDto = IamServerUtilsTest.buildTenantDto();

        final ProfileDto dto = IamServerUtilsTest.buildProfileDto();
        dto.setTenantIdentifier(tenantDto.getIdentifier());
        dto.setTenantName(tenantDto.getName());
        return dto;
    }
}
