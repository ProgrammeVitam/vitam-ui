package fr.gouv.vitamui.iam.server.customer.service;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.server.common.service.AddressService;
import fr.gouv.vitamui.iam.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.customer.domain.GraphicIdentity;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

class CustomerServiceTest {

    @Mock
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
    private GroupService intergroupServicealGroupService;

    @Mock
    private ProfileService profileService;

    @Mock
    private SecurityService securityService;

    @Mock
    private AddressService addressService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CustomerService customerService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        AddressConverter addressConverter = new AddressConverter();
        OwnerConverter ownerConverter = new OwnerConverter(addressConverter);
        CustomerConverter customerConverter = new CustomerConverter(addressConverter, ownerRepository, ownerConverter);

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
    }

    @Test
    void testGetPaginatedValues() {
        final CustomerDto customer = buildDto();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(customer, customerCreated);
        customerCreated.setId("id");
        customerCreated.setOtp(OtpEnum.OPTIONAL);
        customerCreated.setLanguage("FRENCH");
        customerCreated.setGraphicIdentity(new GraphicIdentity());
        customerCreated.getGraphicIdentity().setHasCustomGraphicIdentity(false);

        final PaginatedValuesDto<Customer> data = new PaginatedValuesDto<>(Arrays.asList(customerCreated), 0, 5, false);
        when(customerRepository.getPaginatedValues(any(), any(), any(), any(), any())).thenReturn(data);

        final PaginatedValuesDto<CustomerDto> result = customerService.getAllPaginated(
            Integer.valueOf(0),
            Integer.valueOf(5),
            Optional.empty(),
            Optional.empty(),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertNotNull(result, "Customers should be returned.");
        Assertions.assertNotNull(result.getValues(), "Customers should be returned.");
        Assertions.assertEquals(1, result.getValues().size(), "Customes size should be returned.");
        Assertions.assertEquals(0, result.getPageNum(), "Customes size should be returned.");
        Assertions.assertEquals(5, result.getPageSize(), "Customes size should be returned.");
        Assertions.assertEquals(false, result.isHasMore(), "Customes size should be returned.");
    }

    @Test
    void testCheckExistByCode() {
        final CustomerDto customer = buildDto();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(customer, customerCreated);
        customerCreated.setId("id");
        customerCreated.setOtp(OtpEnum.OPTIONAL);
        customerCreated.setLanguage("FRENCH");

        when(customerRepository.exists(any(Query.class))).thenReturn(true);

        final boolean result = customerService.checkExist(null);
        Assertions.assertTrue(result, "Customers should be found.");
        Assertions.assertTrue(customerService.checkExist(null), "Customers should be found.");
    }

    @Test
    void testCheckNotExistByCode() {
        when(customerRepository.exists(any(Query.class))).thenReturn(false);

        final boolean result = customerService.checkExist(null);
        Assertions.assertFalse(result, "Customers should be found.");
    }

    @Test
    void testCheckExistByDomain() {
        final CustomerDto customer = buildDto();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(customer, customerCreated);
        customerCreated.setId("id");
        customerCreated.setOtp(OtpEnum.OPTIONAL);
        customerCreated.setLanguage("FRENCH");

        when(customerRepository.exists(any(Query.class))).thenReturn(true);

        final boolean result = customerService.checkExist(null);
        Assertions.assertTrue(result, "Customers should be found.");
        Assertions.assertTrue(customerService.checkExist(null), "Customers should be found.");
    }

    @Test
    void testCheckNotExistByDomain() {
        when(customerRepository.findByEmailDomainsContainsIgnoreCase(any())).thenReturn(null);

        final boolean result = customerService.checkExist(null);
        Assertions.assertFalse(result, "Customers shouldn't be found.");
    }

    @Test
    void testCreateDefaultIdp() {
        List<String> emailsDomain = Arrays.asList("@vitamui.com", "vitamui.fr");
        emailsDomain = emailsDomain.stream().map(s -> ".*" + s).collect(Collectors.toList());
        assertThat(emailsDomain).isEqualTo(Arrays.asList(".*@vitamui.com", ".*vitamui.fr"));
    }

    @Test
    void testUpdateCustomerCode() {
        final CustomerDto customer = buildDto();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(customer, customerCreated);
        customerCreated.setId("id");
        customerCreated.setOtp(customer.getOtp());
        VitamUIUtils.copyProperties(customer.getAddress(), customerCreated.getAddress());
        customerCreated.setLanguage(customer.getLanguage().toString());
        customerCreated.setGraphicIdentity(new GraphicIdentity());
        customerCreated.getGraphicIdentity().setHasCustomGraphicIdentity(false);

        final CustomerDto customerToUpdate = buildDto();
        customerToUpdate.setId(customerCreated.getId());
        customerToUpdate.setCode(customer.getCode());
        customerToUpdate.setHasCustomGraphicIdentity(false);

        final Customer customerV2 = new Customer();
        VitamUIUtils.copyProperties(customerToUpdate, customerV2);
        customerV2.setOtp(customerToUpdate.getOtp());
        VitamUIUtils.copyProperties(customerToUpdate.getAddress(), customerV2.getAddress());
        customerV2.setLanguage(customerToUpdate.getLanguage().toString());
        customerV2.setGraphicIdentity(new GraphicIdentity());
        VitamUIUtils.copyProperties(customerCreated.getGraphicIdentity(), customerV2.getGraphicIdentity());

        when(customerRepository.findById(any())).thenReturn(Optional.of(customerCreated));
        when(customerRepository.existsById(any())).thenReturn(true);
        when(customerRepository.save(any())).thenReturn(customerV2);
        when(ownerRepository.findAll(any(Query.class))).thenReturn(new ArrayList<>());

        final CustomerDto customerDtoUpdated = customerService.update(customerToUpdate);
        Assertions.assertNotNull(customerDtoUpdated, "Customer should be returned.");
        Assertions.assertEquals(
            customerToUpdate.getCode(),
            customerDtoUpdated.getCode(),
            "Customer code should be returned."
        );
        Assertions.assertEquals(
            customerToUpdate.getId(),
            customerDtoUpdated.getId(),
            "Customer id should be returned."
        );
    }

    @Test
    void should_patch_customer_address_and_name_when_changes_occured() {
        // Given
        final Customer customer = new Customer();
        final Customer anotherCustomer = IamServerUtilsTest.buildCustomer(
            "id",
            "name",
            "0123456",
            List.of("julien@vitamui.com", "pierre@vitamui.com")
        );

        final Map<String, Object> partialDto = TestUtils.getMapFromObject(anotherCustomer);
        partialDto.put("address", TestUtils.getMapFromObject(anotherCustomer.getAddress()));
        Arrays.asList("id", "graphicIdentity", "gdprAlertDelay").forEach(partialDto::remove); // remove not allows keys for patch

        CustomerPatchFormData customerFormData = new CustomerPatchFormData();
        customerFormData.setPartialCustomerDto(partialDto);
        doCallRealMethod().when(addressService).processPatch(any(), anyMap(), anyCollection(), anyBoolean());

        // When
        customerService.processPatch(customer, customerFormData);

        // Then
        assertThat(customer)
            .usingRecursiveComparison()
            .ignoringFields("id", "graphicIdentity", "gdprAlertDelay")
            .isEqualTo(anotherCustomer);
    }

    @Test
    void testCheckCodeNoConflict() {
        when(customerRepository.findByCode("0123456")).thenReturn(Optional.empty());
        customerService.checkCode(Optional.empty(), "0123456");
    }

    @Test
    void testCheckCodeExistingCustomerOk() {
        final Customer customer = IamServerUtilsTest.buildCustomer(
            "id",
            "name",
            "0123456",
            Arrays.asList("@vitamui.com")
        );

        when(customerRepository.findByCode("0123456")).thenReturn(Optional.of(customer));
        customerService.checkCode(Optional.of("id"), "0123456");
    }

    @Test
    void testCheckCodeExistingCustomerKO() {
        assertThrows(IllegalArgumentException.class, () -> {
            final Customer customer = IamServerUtilsTest.buildCustomer(
                "id",
                "name",
                "0123456",
                Arrays.asList("@vitamui.com")
            );

            when(customerRepository.findByCode("0123456")).thenReturn(Optional.of(customer));
            customerService.checkCode(Optional.of("diffId"), "0123456");
        });
    }

    protected CustomerDto buildDto() {
        final CustomerDto dto = new CustomerDto();
        dto.setEnabled(true);
        dto.setName("CustomerName");
        dto.setCode("0123456");
        dto.setOtp(OtpEnum.OPTIONAL);
        dto.setLanguage(LanguageDto.FRENCH);
        dto.setPasswordRevocationDelay(365 * 10);
        dto.setDefaultEmailDomain("vitamui.com");
        final List<String> domainsEmails = new ArrayList<>();
        domainsEmails.add("vitamui.com");
        dto.setEmailDomains(domainsEmails);
        final OwnerDto owner = new OwnerDto();
        owner.setName("The Boss");
        owner.setAddress(new AddressDto());
        final List<OwnerDto> owners = new ArrayList<>();
        owners.add(owner);
        dto.setOwners(owners);
        dto.setHasCustomGraphicIdentity(false);
        dto.setGdprAlert(false);
        dto.setGdprAlertDelay(72);
        return dto;
    }

    @Test
    void testCheckNotExistByDomainMail() {
        when(customerRepository.findByIdAndEmailDomainsIgnoreCase(anyString(), any())).thenReturn(null);

        final boolean result = customerService.checkExist(null);
        Assertions.assertFalse(result, "Customers shouldn't be found.");
    }

    @Test
    void testCreateFailsAsDuplicatePatterns() {
        final CustomerDto customerDto = new CustomerDto();
        customerDto.setName("name");
        customerDto.setCode("0123456");
        List<String> duplicatesDomains = List.of("@vitamui.com", "@vitamui.com");
        customerDto.setEmailDomains(duplicatesDomains);

        CustomerCreationFormData customerCreationFormData = new CustomerCreationFormData(customerDto);
        customerCreationFormData.setTenantName("Some tenant");
        when(customerRepository.findByCode("0123456")).thenReturn(Optional.empty());

        try {
            customerService.create(customerCreationFormData);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Duplicate email domain found @vitamui.com", e.getMessage());
        }
    }
}
