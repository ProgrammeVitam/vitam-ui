package fr.gouv.vitamui.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.external.client.CustomerExternalRestClient;
import fr.gouv.vitamui.iam.external.client.CustomerExternalWebClient;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import fr.gouv.vitamui.ui.commons.service.AbstractCrudService;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public class CustomerServiceTest extends UIIdentityServiceTest<CustomerDto> {

    private CustomerService service;

    @Mock
    protected static IamExternalWebClientFactory iamExternalWebClientFactory;

    @Mock
    private static CustomerExternalRestClient client;

    @Mock
    private static CustomerExternalWebClient webClient;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerServiceTest.class);

    @Before
    public void setup() {
        service = new CustomerService(commonService, factory, iamExternalWebClientFactory);
        Mockito.when(factory.getCustomerExternalRestClient()).thenReturn(client);
        Mockito.when(iamExternalWebClientFactory.getCustomerWebClient()).thenReturn(webClient);
    }

    @Test
    public void testCreate() {
        super.createEntite();
    }

    @Test
    public void testUpdate() {
        super.updateEntite();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithIdEmpty() {
        super.updateWithIdEmpty();
    }

    public void testGetCustomerById() {
        Mockito.when(client.getOne(any(), any(), any())).thenReturn(new CustomerDto());
        final CustomerDto customer = service.getOne(null, "1", Optional.empty());
        assertThat(customer).isNotNull();
        assertThat(customer);
    }

    @Test(expected = NotFoundException.class)
    public void testGetCustomerByIdFailed() {
        Mockito.when(client.getOne(any(), any(), any())).thenReturn(null);
        final CustomerDto customer = service.getOne(null, "1", Optional.empty());

    }

    @Test
    public void testGetsCustomerByCriteria() {
        final PaginatedValuesDto<CustomerDto> values = service.getAllPaginated(0, 20, Optional.empty(), Optional.of("id"), Optional.of(DirectionDto.ASC), null,
                null);
    }

    @Test
    public void testCreateCustomerBadValidation() {
        Mockito.when(webClient.create(any(), any(CustomerCreationFormData.class))).thenReturn(new CustomerDto());
        CustomerDto customer = new CustomerDto();
        customer.setName("test");
        customer.setCode("01425");
        try {
            customer = service.create(null, customer);
            fail();
        }
        catch (final ValidationException e) {
        }
    }

    @Override
    protected CustomerExternalRestClient getClient() {
        return client;
    }

    @Override
    protected CustomerDto buildDto(final String id) {
        final CustomerDto customer = new CustomerDto();
        customer.setCompanyName("reason social");
        customer.setName("test");
        customer.setCode("014256");
        customer.setOtp(OtpEnum.OPTIONAL);
        customer.setLanguage(LanguageDto.FRENCH);
        customer.setOwners(Arrays.asList(new OwnerDto()));
        customer.setAddress(new AddressDto("street", "zipCode", "city", "country"));
        customer.setDefaultEmailDomain("@vitamui.com");
        customer.setPasswordRevocationDelay(1);
        customer.setEmailDomains(Arrays.asList("@com"));
        return customer;
    }

    @Override
    protected AbstractCrudService<CustomerDto> getService() {
        return service;
    }
}
