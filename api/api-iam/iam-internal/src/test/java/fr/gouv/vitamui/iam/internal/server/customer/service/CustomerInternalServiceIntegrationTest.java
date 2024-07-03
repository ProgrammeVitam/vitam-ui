package fr.gouv.vitamui.iam.internal.server.customer.service;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.domain.Event;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.CustomerPatchFormData;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class CustomerInternalServiceIntegrationTest extends AbstractLogbookIntegrationTest {

    private CustomerInternalService service;

    @MockBean
    private InitCustomerService initCustomerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Mock
    private UserInternalService userInternalService;

    @Mock
    private OwnerInternalService internalOwnerService;

    @Mock
    private InternalHttpContext internalHttpContext;

    @Mock
    private AddressService addressService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private CustomerConverter customerConverter;

    @MockBean
    private TenantRepository tenantRepository;

    @MockBean
    private LogbookService logbookService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        service = new CustomerInternalService(
            sequenceGeneratorService,
            customerRepository,
            internalOwnerService,
            userInternalService,
            internalSecurityService,
            addressService,
            initCustomerService,
            iamLogbookService,
            customerConverter,
            logbookService
        );
        final Tenant tenant = new Tenant();
        tenant.setIdentifier(10);
        Mockito.when(tenantRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(tenant));

        customerRepository.deleteAll();

        when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
    }

    @Test
    public void testCheckExist() {
        customerRepository.save(
            IamServerUtilsTest.buildCustomer("id", "name", "0123456", Arrays.asList("vitamui.com", "gmail.com"))
        );
        customerRepository.save(IamServerUtilsTest.buildCustomer("id2", "name3", "01234567", List.of("toto.com")));

        QueryDto criteria = QueryDto.criteria()
            .addCriterion("code", "0123456", CriterionOperator.EQUALS)
            .addCriterion("emailDomains", "vitamui.com", CriterionOperator.CONTAINS);
        boolean exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = QueryDto.criteria()
            .addCriterion("code", "01234567", CriterionOperator.EQUALS)
            .addCriterion("emailDomains", List.of("toto.com"), CriterionOperator.IN);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = QueryDto.criteria()
            .addCriterion("code", "01234567", CriterionOperator.EQUALS)
            .addCriterion("emailDomains", List.of("toto.com"), CriterionOperator.EQUALS);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = QueryDto.criteria()
            .addCriterion("code", "01234567", CriterionOperator.EQUALS)
            .addCriterion("emailDomains", "TOTO.com", CriterionOperator.CONTAINSIGNORECASE);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isTrue();

        criteria = QueryDto.criteria()
            .addCriterion("code", "012345678", CriterionOperator.EQUALS)
            .addCriterion("emailDomains", List.of("toto.com"), CriterionOperator.IN);
        exist = service.checkExist(criteria.toJson());
        assertThat(exist).isFalse();
    }

    @Test
    public void testCreateCustomer() {
        final CustomerDto customer = createCustomer();
        assertThat(customer.getIdentifier()).isNotBlank();

        final Criteria criteria = Criteria.where("obId")
            .is(customer.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.CUSTOMERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_CREATE_CUSTOMER);
        final Optional<Event> ev = eventRepository.findOne(Query.query(criteria));
        assertThat(ev).isPresent();
    }

    private CustomerDto createCustomer() {
        final CustomerCreationFormData customerDta = new CustomerCreationFormData(buildDto());
        customerDta.setTenantName("tenantName");

        Mockito.when(internalSecurityService.getHttpContext()).thenReturn(internalHttpContext);
        return service.create(customerDta);
    }

    @Test
    public void testPatch() {
        final CustomerDto customer = createCustomer();
        final CustomerPatchFormData customerPatchFormData = new CustomerPatchFormData();
        final Map<String, Object> partialDtoInit = new HashMap<>();
        customerPatchFormData.setPartialCustomerDto(partialDtoInit);

        final Map<String, Object> partialDto = customerPatchFormData.getPartialCustomerDto();
        partialDto.put("id", customer.getId());

        partialDto.put("name", "nameTest");
        service.patch(customerPatchFormData);
        partialDto.remove("name");

        partialDto.put("enabled", false);
        service.patch(customerPatchFormData);
        partialDto.remove("enabled");

        partialDto.put("companyName", "company Name test");
        service.patch(customerPatchFormData);
        partialDto.remove("companyName");

        partialDto.put("language", LanguageDto.FRENCH.toString());
        service.patch(customerPatchFormData);
        partialDto.remove("language");

        partialDto.put("passwordRevocationDelay", 1);
        service.patch(customerPatchFormData);
        partialDto.remove("passwordRevocationDelay");

        partialDto.put("otp", OtpEnum.OPTIONAL.toString());
        service.patch(customerPatchFormData);
        partialDto.remove("otp");

        partialDto.put("emailDomains", Arrays.asList("vitamui.com", "test.com"));
        service.patch(customerPatchFormData);
        partialDto.remove("emailDomains");

        partialDto.put("defaultEmailDomain", "vitamui.com");
        service.patch(customerPatchFormData);
        partialDto.remove("defaultEmailDomain");

        partialDto.put("subrogeable", true);
        service.patch(customerPatchFormData);
        partialDto.remove("subrogeable");

        final Criteria criteria = Criteria.where("obId")
            .is(customer.getIdentifier())
            .and("obIdReq")
            .is(MongoDbCollections.CUSTOMERS)
            .and("evType")
            .is(EventType.EXT_VITAMUI_UPDATE_CUSTOMER);
        final Collection<Event> events = eventRepository.findAll(Query.query(criteria));
        assertThat(events).hasSize(9);
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
