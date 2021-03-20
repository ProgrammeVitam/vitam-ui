package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.customer.service.CustomerInternalService;
import fr.gouv.vitamui.iam.internal.server.customer.service.InitCustomerService;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CustomerInternalController}.
 */

public final class CustomerCrudControllerTest {

    private CustomerInternalController controller;

    private CustomerInternalService internalCustomerService;

    @InjectMocks
    private InitCustomerService initCustomerService;

    @Mock
    private UserRepository userRepository;

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
    private UserInternalService userInternalService;

    @Mock
    private OwnerInternalService internalOwnerService;

    @Mock
    private TenantInternalService internalTenantService;

    @Mock
    private IdentityProviderInternalService internalIdentityProviderService;

    @Mock
    private UserInternalService internalUserService;

    @Mock
    private GroupInternalService internalGroupService;

    @Mock
    private ProfileInternalService internalProfileService;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private AddressService addressService;

    @Mock
    private CustomSequenceRepository customSequenceRepository;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private InitVitamTenantService initVitamTenantService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private CustomerInitConfig customerInitConfig;



    @Mock
    private ExternalParametersInternalService externalParametersInternalService;


    private final AddressConverter addressConverter = new AddressConverter();

    private final IdentityProviderConverter identityProviderConverter =
        new IdentityProviderConverter(new SpMetadataGenerator());

    private final OwnerConverter ownerConverter = new OwnerConverter(addressConverter);

    private CustomerConverter customerConverter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        customerConverter = new CustomerConverter(addressConverter, ownerRepository, ownerConverter);
        initCustomerService.setOwnerConverter(ownerConverter);
        initCustomerService.setIdpConverter(identityProviderConverter);
        initCustomerService.setExternalParametersInternalService(externalParametersInternalService);
        internalCustomerService =
            new CustomerInternalService(customSequenceRepository, customerRepository, internalOwnerService,
                userInternalService,
                internalSecurityService, addressService, initCustomerService, iamLogbookService, customerConverter,
                logbookService);
        controller = new CustomerInternalController(internalCustomerService);
        Mockito.when(ownerRepository.generateSuperId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(ownerRepository.save(ArgumentMatchers.any(Owner.class)))
            .thenAnswer(AdditionalAnswers.returnsFirstArg());
        Mockito.when(initVitamTenantService.init(ArgumentMatchers.any(Tenant.class), ArgumentMatchers.any(
            ExternalParametersDto.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);


    }

    protected void prepareServices() {
        final CustomerDto customerDto = buildCustomerDto();

        when(customSequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(new CustomSequence()));
        when(customerRepository.save(any())).thenReturn(buildCustomer());
        when(customerRepository.exists(any(Query.class))).thenReturn(true);
        when(customerRepository.existsById(any())).thenReturn(true);
        when(customerRepository.findByCode(customerDto.getCode())).thenReturn(Optional.empty());
        when(customerRepository.findById(customerDto.getId())).thenReturn(Optional.of(buildCustomer()));
        when(customerRepository.findByEmailDomainsContainsIgnoreCase(anyString())).thenReturn(Optional.empty());

        when(internalOwnerService.findByCustomerId(customerDto.getId())).thenReturn(Arrays.asList(new OwnerDto()));
        when(internalOwnerService.create(any())).thenReturn(new OwnerDto());

        when(tenantRepository.save(any())).thenReturn(buildTenant());
        when(internalUserService.create(any())).thenReturn(buildUserDto());

        when(groupRepository.save(any())).thenReturn(buildGroup());

        when(profileRepository.save(any()))
            .thenAnswer(invocation -> {
                final Object[] args = invocation.getArguments();
                return args[0];
            });
        when(identityProviderRepository.save(any())).thenReturn(buildIdp());
        when(internalProfileService.getAll(any(QueryDto.class))).thenReturn(Arrays.asList(buildProfileDto()));
        when(internalTenantService.getDefaultProfiles(any(), any())).thenReturn(new ArrayList<>());
    }

    @Test
    public void testCreationOK() throws Exception {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();

        final CustomerDto createdCustomer = controller.create(buildCustomerData(customerDto));
        Assert.assertNotNull("Customer should be created.", createdCustomer.getId());
    }

    @Test
    public void testCreationWithoutTenantOK() throws Exception {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();

        final CustomerDto createdCustomer = controller.create(buildCustomerData(customerDto));
        Assert.assertNotNull("Customer should be created.", createdCustomer.getId());
    }

    @Test
    public void testCreationWithoutIdpOK() throws Exception {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();

        final CustomerDto createdCustomer = controller.create(buildCustomerData(customerDto));
        Assert.assertNotNull("Customer should be created.", createdCustomer.getId());
    }

    @Test
    public void testCreationFailsAsOwnersIsNull() {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setOwners(null);

        prepareServices();
        try {
            controller.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException ex) {
            assertEquals("Unable to create customer " + customerDto.getName() + ": a customer must have owners.",
                ex.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsOwnersIsEmpty() {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setOwners(Collections.emptyList());

        prepareServices();
        try {
            controller.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException ex) {
            assertEquals("Unable to create customer " + customerDto.getName() + ": a customer must have owners.",
                ex.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsIdIsProvided() throws Exception {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setId("customerId");

        try {
            controller.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheCodeIsAlreadyUsed() {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setId(null);

        prepareServices();
        when(customerRepository.findByCode(customerDto.getCode())).thenReturn(Optional.of(buildCustomer()));

        try {
            controller.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Integrity constraint error on the customer [Undefined] : the new code is already used by another customer.",
                e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheDomainIsAlreadyUsed() {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setId(null);

        prepareServices();
        when(customerRepository.findByEmailDomainsContainsIgnoreCase(anyString()))
            .thenReturn(Optional.of(buildCustomer()));

        try {
            controller.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create customer " + customerDto.getName() + ": a customer has already the email domain " +
                    customerDto.getDefaultEmailDomain(),
                e.getMessage());
        }
    }

    @Test(expected = InternalServerException.class)
    public void testRollbackOnIdpError() {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();
        when(identityProviderRepository.save(any())).thenThrow(new InternalServerException("IDP Creation error"));

        controller.create(buildCustomerData(customerDto));

        fail("should fail");
    }

    @Test(expected = InternalServerException.class)
    public void testRollbackOnOwnerError() {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();
        when(ownerRepository.save(any())).thenThrow(new InternalServerException("Owner Creation error"));

        controller.create(buildCustomerData(customerDto));
        fail("should fail");
    }

    @Test(expected = InternalServerException.class)
    public void testRollbackOnTenantError() {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();
        when(tenantRepository.save(any())).thenThrow(new InternalServerException("Tenant Creation error"));

        controller.create(buildCustomerData(customerDto));
        fail("should fail");
    }

    @Test(expected = InternalServerException.class)
    public void testRollbackOnGroupError() {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();
        when(groupRepository.save(any())).thenThrow(new InternalServerException("Group Creation error"));

        controller.create(buildCustomerData(customerDto));
        fail("should fail");
    }

    @Test(expected = InternalServerException.class)
    public void testRollbackOnProfileError() {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();
        when(profileRepository.save(any())).thenThrow(new InternalServerException("Profile Creation error"));

        controller.create(buildCustomerData(customerDto));
        fail("should fail");
    }

    @Test(expected = InternalServerException.class)
    public void testRollbackOnUserError() {
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();
        when(internalUserService.create(any())).thenThrow(new InternalServerException("User Creation error"));

        controller.create(buildCustomerData(customerDto));
        fail("should fail");
    }

    private CustomerCreationFormData buildCustomerData(final CustomerDto customerDto) {
        final CustomerCreationFormData customerCreationFormData = new CustomerCreationFormData(customerDto);
        customerCreationFormData.setTenantName("tenantName");
        return customerCreationFormData;
    }

    @Test
    public void testUpdateOK() throws Exception {
        final CustomerDto customerDto = buildCustomerDto();

        prepareServices();
        controller.update(customerDto.getId(), customerDto);
    }

    @Test
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() throws Exception {
        final CustomerDto customerDto = buildCustomerDto();

        try {
            prepareServices();

            controller.update(customerDto.getId() + "_BAD", customerDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must match the path identifier for update.", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsTheNewCodeIsAlreadyUsed() {
        final CustomerDto customerDto = buildCustomerDto();
        final Customer conlictedCustomerDto = new Customer();
        conlictedCustomerDto.setId("conflict");

        final Customer customer = buildCustomer();
        customer.setCode(customerDto.getCode() + "_");

        try {
            prepareServices();
            when(customerRepository.findByCode(customerDto.getCode())).thenReturn(Optional.of(conlictedCustomerDto));
            when(customerRepository.findById(customerDto.getId())).thenReturn(Optional.of(customer));

            controller.update(customerDto.getId(), customerDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Integrity constraint error on the customer customerId : the new code is already used by another customer.",
                e.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotDelete() {
        prepareServices();
        controller.delete("id");
    }

    @Test
    public void testCheckExist() throws Exception {
        prepareServices();
        final ResponseEntity<Void> result = controller.checkExist(QueryDto.criteria().toJson());
        Assert.assertNotNull("Customers should be returned.", result);
        Assert.assertEquals("Status Code should be returned.", HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void testGetOne() throws Exception {
        final CustomerDto dto = buildCustomerDto();
        prepareServices();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(dto, customerCreated);
        customerCreated.setId("id");
        when(customerRepository.findOne(any(Query.class))).thenReturn(Optional.of(customerCreated));

        final CustomerDto result = controller.getOne(customerCreated.getId(), Optional.empty());
        Assert.assertNotNull("Customers should be returned.", result);
        Assert.assertEquals("Customes size should be returned.", customerCreated.getId(), result.getId());
    }

    @Test
    public void testGetPaginatedValues() throws Exception {
        final CustomerDto dto = buildCustomerDto();
        prepareServices();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(dto, customerCreated);
        customerCreated.setId("id");
        final PaginatedValuesDto<Customer> data = new PaginatedValuesDto<>(Arrays.asList(customerCreated), 0, 5, false);
        when(customerRepository.getPaginatedValues(any(), any(), any(), any(), any())).thenReturn(data);

        final PaginatedValuesDto<CustomerDto> result =
            controller.getAllPaginated(Integer.valueOf(0), Integer.valueOf(5), Optional.empty(), Optional.empty(),
                Optional.of(DirectionDto.ASC));
        Assert.assertNotNull("Customer should be created.", result);
    }

    private CustomerDto buildFullCustomerDto() {
        final OwnerDto ownerDto = buildOwnerDto();
        ownerDto.setId(null);

        final CustomerDto customerDto = buildCustomerDto();
        customerDto.setId(null);
        customerDto.setOwners(Arrays.asList(ownerDto));
        return customerDto;
    }

    private Customer buildCustomer() {
        return IamServerUtilsTest.buildCustomer();
    }

    private CustomerDto buildCustomerDto() {
        return IamServerUtilsTest.buildCustomerDto();
    }

    private OwnerDto buildOwnerDto() {
        return IamServerUtilsTest.buildOwnerDto();
    }

    private UserDto buildUserDto() {
        return IamServerUtilsTest.buildUserDto();
    }

    private Tenant buildTenant() {
        return IamServerUtilsTest.buildTenant();
    }

    private Group buildGroup() {
        return IamServerUtilsTest.buildGroup();
    }

    private IdentityProvider buildIdp() {
        return IamServerUtilsTest.buildIdentityProvider();
    }

    private ProfileDto buildProfileDto() {
        return IamServerUtilsTest.buildProfileDto();
    }
}
