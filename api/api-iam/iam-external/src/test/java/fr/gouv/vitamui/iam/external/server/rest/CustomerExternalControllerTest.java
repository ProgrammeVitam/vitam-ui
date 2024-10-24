package fr.gouv.vitamui.iam.external.server.rest;

import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import fr.gouv.vitamui.iam.external.server.service.CustomerExternalService;
import fr.gouv.vitamui.iam.external.server.utils.ApiIamServerUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Optional;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = { CustomerExternalController.class })
public class CustomerExternalControllerTest extends ApiIamControllerTest<CustomerDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerExternalControllerTest.class);

    @MockBean
    private CustomerExternalService customerExternalService;

    @Test
    public void testCreateCustomer() {
        final CustomerCreationFormData creationFormData = new CustomerCreationFormData();
        creationFormData.setCustomerDto(buildDto());
        creationFormData.setHeader(Optional.empty());
        creationFormData.setFooter(Optional.empty());
        creationFormData.setPortal(Optional.empty());

        final MockMultipartFile customer = new MockMultipartFile(
            "customerData",
            "",
            "application/json",
            asJsonString(creationFormData).getBytes()
        );
        final ResultActions result = performPostMultipart(StringUtils.EMPTY, Arrays.asList(customer));
    }

    @Test
    public void testGetPaginatedCustomers() {
        super.testGetPaginatedEntities();
    }

    @Override
    protected CustomerDto buildDto() {
        return ApiIamServerUtils.buildCustomerDto("id");
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.V1_CUSTOMERS_URL;
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.SERVICE_CUSTOMERS };
    }

    @Override
    protected Class<CustomerDto> getDtoClass() {
        return CustomerDto.class;
    }
}
