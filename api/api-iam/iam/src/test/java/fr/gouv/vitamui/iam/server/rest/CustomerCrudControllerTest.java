package fr.gouv.vitamui.iam.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.server.common.service.AddressService;
import fr.gouv.vitamui.iam.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.customer.service.CustomerService;
import fr.gouv.vitamui.iam.server.customer.service.InitCustomerService;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.group.domain.Group;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CustomerController}.
 */

public final class CustomerCrudControllerTest {

    private AutoCloseable mocks;

    private CustomerController customerController;

    private CustomerService customerService;

    @InjectMocks
    private InitCustomerService initCustomerService;

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
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnerService ownerService;

    @Mock
    private TenantService tenantService;

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private UserInfoService userInfoService;

    @Mock
    private GroupService groupService;

    @Mock
    private ProfileService profileService;

    @Mock
    private SecurityService securityService;

    @Mock
    private AddressService addressService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private InitVitamTenantService initVitamTenantService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private CustomerInitConfig customerInitConfig;

    @Mock
    private ExternalParametersService externalParametersService;

    private final AddressConverter addressConverter = new AddressConverter();

    private final IdentityProviderConverter identityProviderConverter = new IdentityProviderConverter(
        new SpMetadataGenerator()
    );

    private final OwnerConverter ownerConverter = new OwnerConverter(addressConverter);

    private CustomerConverter customerConverter;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        customerConverter = new CustomerConverter(addressConverter, ownerRepository, ownerConverter);
        customerService = new CustomerService(
            sequenceGeneratorService,
            customerRepository,
            ownerService,
            userService,
            userRepository,
            securityService,
            addressService,
            initCustomerService,
            iamLogbookService,
            customerConverter,
            logbookService
        );
        customerController = new CustomerController(customerService);
        initCustomerService.setOwnerConverter(ownerConverter);
        initCustomerService.setIdpConverter(identityProviderConverter);
        initCustomerService.setExternalParametersService(externalParametersService);
        customerService = new CustomerService(
            sequenceGeneratorService,
            customerRepository,
            ownerService,
            userService,
            userRepository,
            securityService,
            addressService,
            initCustomerService,
            iamLogbookService,
            customerConverter,
            logbookService
        );
        customerController = new CustomerController(customerService);
        Mockito.when(ownerRepository.generateSuperId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(ownerRepository.save(ArgumentMatchers.any(Owner.class))).thenAnswer(
            AdditionalAnswers.returnsFirstArg()
        );
        Mockito.when(
            initVitamTenantService.init(
                ArgumentMatchers.any(Tenant.class),
                ArgumentMatchers.any(ExternalParametersDto.class)
            )
        ).thenAnswer(AdditionalAnswers.returnsFirstArg());
    }

    protected void prepareServices() {
        final CustomerDto customerDto = buildCustomerDto();

        when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
        when(customerRepository.save(any())).thenReturn(buildCustomer());
        when(customerRepository.exists(any(Query.class))).thenReturn(true);
        when(customerRepository.existsById(any())).thenReturn(true);
        when(customerRepository.findByCode(customerDto.getCode())).thenReturn(Optional.empty());
        when(customerRepository.findById(customerDto.getId())).thenReturn(Optional.of(buildCustomer()));
        when(customerRepository.findByEmailDomainsContainsIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(customerRepository.findByIdAndEmailDomainsIgnoreCase(anyString(), anyString())).thenReturn(
            Optional.empty()
        );

        when(ownerService.findByCustomerId(customerDto.getId())).thenReturn(Arrays.asList(new OwnerDto()));
        when(ownerService.create(any())).thenReturn(new OwnerDto());

        when(tenantRepository.save(any())).thenReturn(buildTenant());
        when(userService.create(any())).thenReturn(buildUserDto());

        when(groupRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        when(profileRepository.save(any())).thenAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            return args[0];
        });
        when(identityProviderRepository.save(any())).thenReturn(buildIdp());
        when(profileService.getAll(any(QueryDto.class))).thenReturn(Arrays.asList(buildProfileDto()));
        when(tenantService.getDefaultProfiles(any(), any())).thenReturn(new ArrayList<>());
        CustomerInitConfig.ProfileInitConfig restrictedProfile = createRestrictedProfile();

        when(customerInitConfig.getProfiles()).thenReturn(List.of(restrictedProfile));
    }

    @Test
    public void testCreationOK() throws InvalidParseOperationException, PreconditionFailedException {
        when(userInfoService.create(any())).thenReturn(buildUserInfoDto());

        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();

        final CustomerDto createdCustomer = customerController.create(buildCustomerData(customerDto));
        Assertions.assertNotNull(createdCustomer.getId(), "Customer should be created.");
    }

    @Test
    public void testCreationWithoutTenantOK() throws InvalidParseOperationException, PreconditionFailedException {
        when(userInfoService.create(any())).thenReturn(buildUserInfoDto());
        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();

        final CustomerDto createdCustomer = customerController.create(buildCustomerData(customerDto));
        Assertions.assertNotNull(createdCustomer.getId(), "Customer should be created.");
    }

    @Test
    public void testCreationWithoutIdpOK() throws InvalidParseOperationException, PreconditionFailedException {
        when(userInfoService.create(any())).thenReturn(buildUserInfoDto());

        final CustomerDto customerDto = buildFullCustomerDto();

        prepareServices();

        final CustomerDto createdCustomer = customerController.create(buildCustomerData(customerDto));
        Assertions.assertNotNull(createdCustomer.getId(), "Customer should be created.");
    }

    @Test
    public void testCreationFailsAsOwnersIsNull() throws InvalidParseOperationException, PreconditionFailedException {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setOwners(null);

        prepareServices();
        try {
            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException ex) {
            assertEquals(
                "Unable to create customer " + customerDto.getName() + ": a customer must have owners.",
                ex.getMessage()
            );
        }
    }

    @Test
    public void testCreationFailsAsOwnersIsEmpty() throws InvalidParseOperationException, PreconditionFailedException {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setOwners(Collections.emptyList());

        prepareServices();
        try {
            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException ex) {
            assertEquals(
                "Unable to create customer " + customerDto.getName() + ": a customer must have owners.",
                ex.getMessage()
            );
        }
    }

    @Test
    public void testCreationFailsAsIdIsProvided() throws Exception {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setId("customerId");

        try {
            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Test
    public void testCreationFailsAsTheCodeIsAlreadyUsed()
        throws InvalidParseOperationException, PreconditionFailedException {
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setId(null);

        prepareServices();
        when(customerRepository.findByCode(customerDto.getCode())).thenReturn(Optional.of(buildCustomer()));

        try {
            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Integrity constraint error on the customer [Undefined] : the new code is already used by another customer.",
                e.getMessage()
            );
        }
    }

    @Test
    public void testRollbackOnIdpError() {
        assertThrows(InternalServerException.class, () -> {
            final CustomerDto customerDto = buildFullCustomerDto();

            prepareServices();
            when(identityProviderRepository.save(any())).thenThrow(new InternalServerException("IDP Creation error"));

            customerController.create(buildCustomerData(customerDto));

            fail("should fail");
        });
    }

    @Test
    public void testRollbackOnOwnerError() {
        assertThrows(InternalServerException.class, () -> {
            final CustomerDto customerDto = buildFullCustomerDto();

            prepareServices();
            when(ownerRepository.save(any())).thenThrow(new InternalServerException("Owner Creation error"));

            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        });
    }

    @Test
    public void testRollbackOnTenantError() {
        assertThrows(InternalServerException.class, () -> {
            final CustomerDto customerDto = buildFullCustomerDto();

            prepareServices();
            when(tenantRepository.save(any())).thenThrow(new InternalServerException("Tenant Creation error"));

            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        });
    }

    @Test
    public void testRollbackOnGroupError() {
        assertThrows(InternalServerException.class, () -> {
            final CustomerDto customerDto = buildFullCustomerDto();

            prepareServices();
            when(groupRepository.save(any())).thenThrow(new InternalServerException("Group Creation error"));

            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        });
    }

    @Test
    public void testRollbackOnProfileError() {
        assertThrows(InternalServerException.class, () -> {
            final CustomerDto customerDto = buildFullCustomerDto();

            prepareServices();
            when(profileRepository.save(any())).thenThrow(new InternalServerException("Profile Creation error"));

            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        });
    }

    @Test
    public void testRollbackOnUserError() {
        assertThrows(InternalServerException.class, () -> {
            when(userInfoService.create(any())).thenReturn(buildUserInfoDto());
            final CustomerDto customerDto = buildFullCustomerDto();

            prepareServices();
            when(userService.create(any())).thenThrow(new InternalServerException("User Creation error"));

            customerController.create(buildCustomerData(customerDto));
            fail("should fail");
        });
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
        customerController.update(customerDto.getId(), customerDto);
    }

    @Test
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() throws Exception {
        final CustomerDto customerDto = buildCustomerDto();

        try {
            prepareServices();

            customerController.update(customerDto.getId() + "_BAD", customerDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must match the path identifier for update.", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsTheNewCodeIsAlreadyUsed()
        throws InvalidParseOperationException, PreconditionFailedException {
        final CustomerDto customerDto = buildCustomerDto();
        final Customer conlictedCustomerDto = new Customer();
        conlictedCustomerDto.setId("conflict");

        final Customer customer = buildCustomer();
        customer.setCode(customerDto.getCode() + "_");

        try {
            prepareServices();
            when(customerRepository.findByCode(customerDto.getCode())).thenReturn(Optional.of(conlictedCustomerDto));
            when(customerRepository.findById(customerDto.getId())).thenReturn(Optional.of(customer));

            customerController.update(customerDto.getId(), customerDto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Integrity constraint error on the customer customerId : the new code is already used by another customer.",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCannotDelete() {
        assertThrows(UnsupportedOperationException.class, () -> {
            prepareServices();
            customerController.delete("id");
        });
    }

    @Test
    public void testCheckExist() throws Exception {
        prepareServices();
        final ResponseEntity<Void> result = customerController.checkExist(QueryDto.criteria().toJson());
        Assertions.assertNotNull(result, "Customers should be returned.");
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode(), "Status Code should be returned.");
    }

    @Test
    public void testGetOne() throws Exception {
        final CustomerDto dto = buildCustomerDto();
        prepareServices();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(dto, customerCreated);
        customerCreated.setId("id");
        when(customerRepository.findOne(any(Query.class))).thenReturn(Optional.of(customerCreated));

        final CustomerDto result = customerController.getOne(customerCreated.getId());
        Assertions.assertNotNull(result, "Customers should be returned.");
        Assertions.assertEquals(customerCreated.getId(), result.getId(), "Customes size should be returned.");
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

        final PaginatedValuesDto<CustomerDto> result = customerController.getAllPaginated(
            Integer.valueOf(0),
            Integer.valueOf(5),
            Optional.empty(),
            Optional.empty(),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertNotNull(result, "Customer should be created.");
    }

    @Test
    public void testCreationSuccessDespiteTheDomainMailIsAlreadyUsed()
        throws InvalidParseOperationException, PreconditionFailedException {
        when(userInfoService.create(any())).thenReturn(buildUserInfoDto());
        final CustomerDto customerDto = buildFullCustomerDto();
        customerDto.setId(null);

        prepareServices();
        when(customerRepository.findByIdAndEmailDomainsIgnoreCase(anyString(), anyString())).thenReturn(
            Optional.of(buildCustomer())
        );

        customerController.create(buildCustomerData(customerDto));
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

    private UserInfoDto buildUserInfoDto() {
        return IamServerUtilsTest.buildUserInfoDto();
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

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    private CustomerInitConfig.ProfileInitConfig createRestrictedProfile() {
        CustomerInitConfig.ProfileInitConfig profile = new CustomerInitConfig.ProfileInitConfig();
        profile.setAppName("USERS_APP");
        profile.setName("Profil restreint pour la gestion des utilisateurs");
        profile.setDescription("Profil restreint pour la gestion des utilisateurs");
        profile.setRoles(
            List.of(
                "ROLE_GET_USERS",
                "ROLE_CREATE_USERS",
                "ROLE_UPDATE_USERS",
                "ROLE_UPDATE_STANDARD_USERS",
                "ROLE_MFA_USERS",
                "ROLE_ANONYMIZATION_USERS",
                "ROLE_GET_GROUPS",
                "ROLE_GET_USER_INFOS",
                "ROLE_CREATE_USER_INFOS",
                "ROLE_UPDATE_USER_INFOS"
            )
        );
        return profile;
    }
}
