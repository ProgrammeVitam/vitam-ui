package fr.gouv.vitamui.iam.internal.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Tests the {@link GroupInternalController}.
 *
 *
 */
public final class GroupInternalControllerTest extends AbstractServerIdentityBuilder implements InternalCrudControllerTest {

    private GroupInternalController controller;

    private GroupInternalService internalGroupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProfileInternalService internalProfileService;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private CustomSequenceRepository sequenceRepository;

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
        internalGroupService = new GroupInternalService(sequenceRepository, groupRepository, customerRepository, internalProfileService, userRepository,
                internalSecurityService, tenantRepository, iamLogbookService, groupConverter, null);

        controller = new GroupInternalController(internalGroupService);
        controller.setInternalGroupService(internalGroupService);
    }

    protected void prepareServices() {
        final GroupDto dto = buildGroupDto();

        when(groupRepository.exists(any(CriteriaDefinition.class))).thenReturn(false);
        when(groupRepository.existsById(any())).thenReturn(true);

        when(internalSecurityService.isLevelAllowed(dto.getLevel())).thenCallRealMethod();
        when(internalSecurityService.getLevel()).thenReturn("");
        when(internalSecurityService.getCustomerId()).thenReturn(dto.getCustomerId());
        when(internalProfileService.getMany(anyList(), any())).thenReturn(Arrays.asList(buildProfileDto()));

        when(customerRepository.findById(any())).thenReturn(Optional.of(buildCustomer()));

        final Tenant tenant = new Tenant();
        tenant.setCustomerId("customerId");
        when(tenantRepository.findByIdentifier(any())).thenReturn(tenant);

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setId(UUID.randomUUID().toString());
        customSequence.setSequence(1);
        Mockito.when(sequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(customSequence));
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
        }
        catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Override
    @Test
    public void testCreationFailsAsCustomerDoesNotExist() throws Exception {
        final GroupDto dto = buildGroupDto();
        dto.setId(null);

        prepareServices();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create group " + dto.getName() + ": customer " + dto.getCustomerId() + " does not exist", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheProfileDoesNotExist() {
        final GroupDto dto = buildGroupDto();
        dto.setId(null);

        prepareServices();
        when(internalProfileService.getMany(anyList(), any())).thenReturn(Collections.emptyList());

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create group " + dto.getName() + ": no profiles", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheNameIsAlreadyUsed() {
        final GroupDto dto = buildGroupDto();
        dto.setId(null);

        prepareServices();
        when(groupRepository.exists(Criteria.where("customerId").is(dto.getCustomerId()).and("name").is(dto.getName()))).thenReturn(true);

        try {
            controller.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
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
    @Override
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
