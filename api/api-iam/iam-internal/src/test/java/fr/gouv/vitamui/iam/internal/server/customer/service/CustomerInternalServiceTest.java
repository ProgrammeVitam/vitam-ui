package fr.gouv.vitamui.iam.internal.server.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Query;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.customer.domain.GraphicIdentity;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

public class CustomerInternalServiceTest {

    private CustomerInternalService internalCustomerService;

    @InjectMocks
    private InitCustomerService initCustomerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private IdentityProviderRepository identityProviderRepository;

    private final OwnerRepository ownerRepository = mock(OwnerRepository.class);

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
    private GroupInternalService internalGroupService;

    @Mock
    private ProfileInternalService internalProfileService;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private AddressService addressService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private CustomSequenceRepository sequenceRepository;

    private final AddressConverter addressConverter = new AddressConverter();

    private final OwnerConverter ownerConverter = new OwnerConverter(addressConverter);

    private final CustomerConverter customerConverter = new CustomerConverter(addressConverter, ownerRepository, ownerConverter);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        internalCustomerService = new CustomerInternalService(sequenceRepository, customerRepository, internalOwnerService, userInternalService,
                internalSecurityService, addressService, initCustomerService, iamLogbookService, customerConverter, logbookService);
    }

    @Test
    public void testGetPaginatedValues() {
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

        final PaginatedValuesDto<CustomerDto> result = internalCustomerService.getAllPaginated(Integer.valueOf(0), Integer.valueOf(5), Optional.empty(),
                Optional.empty(), Optional.of(DirectionDto.ASC));
        Assert.assertNotNull("Customers should be returned.", result);
        Assert.assertNotNull("Customers should be returned.", result.getValues());
        Assert.assertEquals("Customes size should be returned.", 1, result.getValues().size());
        Assert.assertEquals("Customes size should be returned.", 0, result.getPageNum());
        Assert.assertEquals("Customes size should be returned.", 5, result.getPageSize());
        Assert.assertEquals("Customes size should be returned.", false, result.isHasMore());
    }

    @Test
    public void testCheckExistByCode() {
        final CustomerDto customer = buildDto();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(customer, customerCreated);
        customerCreated.setId("id");
        customerCreated.setOtp(OtpEnum.OPTIONAL);
        customerCreated.setLanguage("FRENCH");

        when(customerRepository.exists(any(Query.class))).thenReturn(true);

        final boolean result = internalCustomerService.checkExist(null);
        Assert.assertTrue("Customers should be found.", result);
        Assert.assertTrue("Customers should be found.", internalCustomerService.checkExist(null));
    }

    @Test
    public void testCheckNotExistByCode() {
        when(customerRepository.exists(any(Query.class))).thenReturn(false);

        final boolean result = internalCustomerService.checkExist(null);
        Assert.assertFalse("Customers should be found.", result);
    }

    @Test
    public void testCheckExistByDomain() {
        final CustomerDto customer = buildDto();

        final Customer customerCreated = new Customer();
        VitamUIUtils.copyProperties(customer, customerCreated);
        customerCreated.setId("id");
        customerCreated.setOtp(OtpEnum.OPTIONAL);
        customerCreated.setLanguage("FRENCH");

        when(customerRepository.exists(any(Query.class))).thenReturn(true);

        final boolean result = internalCustomerService.checkExist(null);
        Assert.assertTrue("Customers should be found.", result);
        Assert.assertTrue("Customers should be found.", internalCustomerService.checkExist(null));
    }

    @Test
    public void testCheckNotExistByDomain() {
        when(customerRepository.findByEmailDomainsContainsIgnoreCase(any())).thenReturn(null);

        final boolean result = internalCustomerService.checkExist(null);
        Assert.assertFalse("Customers shouldn't be found.", result);
    }

    @Test
    public void testCreateDefaultIdp() {
        List<String> emailsDomain = Arrays.asList("@vitamui.com", "vitamui.fr");
        emailsDomain = emailsDomain.stream().map(s -> ".*" + s).collect(Collectors.toList());
        assertThat(emailsDomain).isEqualTo(Arrays.asList(".*@vitamui.com", ".*vitamui.fr"));
    }

    @Test
    public void testUpdateCustomerCode() {
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

        final CustomerDto customerDtoUpdated = internalCustomerService.update(customerToUpdate);
        Assert.assertNotNull("Customer should be returned.", customerDtoUpdated);
        Assert.assertEquals("Customer code should be returned.", customerToUpdate.getCode(), customerDtoUpdated.getCode());
        Assert.assertEquals("Customer id should be returned.", customerToUpdate.getId(), customerDtoUpdated.getId());
    }

    @Ignore
    @Test
    public void testProcessPatch() {
        final Customer entity = new Customer();
        final List<String> emailDomains = Arrays.asList("julien@vitamui.com", "pierre@vitamui.com");
        final Customer other = IamServerUtilsTest.buildCustomer("id", "name", "0123456", emailDomains);

        final Map<String, Object> partialDto = TestUtils.getMapFromObject(other);
        partialDto.put("address", TestUtils.getMapFromObject(other.getAddress()));
        final List<String> fieldNotModifiable = Arrays.asList("id", "readonly");
        fieldNotModifiable.forEach(key -> partialDto.remove(key));

        internalCustomerService.processPatch(entity, new CustomerPatchFormData());
        entity.setId(other.getId());
        assertThat(entity).isEqualToComparingFieldByField(other);
    }

    @Test
    public void testCheckCodeNoConflict() {
        when(customerRepository.findByCode("0123456")).thenReturn(Optional.empty());
        internalCustomerService.checkCode(Optional.empty(), "0123456");
    }

    @Test
    public void testCheckCodeExistingCustomerOk() {
        final Customer customer = IamServerUtilsTest.buildCustomer("id", "name", "0123456", Arrays.asList("@vitamui.com"));

        when(customerRepository.findByCode("0123456")).thenReturn(Optional.of(customer));
        internalCustomerService.checkCode(Optional.of("id"), "0123456");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckCodeExistingCustomerKO() {
        final Customer customer = IamServerUtilsTest.buildCustomer("id", "name", "0123456", Arrays.asList("@vitamui.com"));

        when(customerRepository.findByCode("0123456")).thenReturn(Optional.of(customer));
        internalCustomerService.checkCode(Optional.of("diffId"), "0123456");
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
}
