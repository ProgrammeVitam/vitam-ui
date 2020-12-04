package fr.gouv.vitamui.identity.rest;

import com.google.common.collect.ImmutableMap;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.identity.service.CustomerService;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { CustomerController.class })
public class CustomerControllerTest extends UiIdentityRestControllerTest<CustomerDto> {

    @Value("${ui-identity.prefix}")
    protected String apiUrl;

    @MockBean
    private CustomerService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerControllerTest.class);

    private static final String PREFIX = "/customers";

    @MockBean
    private BuildProperties buildProperties;


    @Test
    public void testGetCustomer() throws Exception {
        super.testGetEntityById();
    }

    @Test
    public void testCreateCustomer() {
        super.testCreateEntity();
    }

    @Test
    public void testUpdateCustomer() {
        super.testUpdateEntity();
    }

    @Test
    public void testGetCustomersByOrder() {
        LOGGER.debug("testGetCustomersByOrder");

        final QueryDto criteria = QueryDto.criteria("customerId", 1, CriterionOperator.EQUALS);
        final ImmutableMap<String, Object> params = new ImmutableMap.Builder<String, Object>().put("page", "1").put("size", "20").put("orderBy", "id")
                .put("direction", "ASC").put("criteria", criteria.toJson()).build();

        final ResultActions result = super.performGet(StringUtils.EMPTY, params);
    }

    @Test
    public void testGetCustomersByOrderWithBadKeyArgumentSanitizingThenReturnBadRequest() {
        LOGGER.debug("testGetCustomersByOrderWithBadKeyArgumentSanitizingThenReturnBadRequest");

        final QueryDto criteria = QueryDto.criteria("customerId<script>", 1, CriterionOperator.EQUALS);
        final ImmutableMap<String, Object> params =
            new ImmutableMap.Builder<String, Object>().put("page", "1").put("size", "20").put("orderBy", "id")
                .put("direction", "ASC").put("criteria", criteria.toJson()).build();

        final ResultActions result = super.performGet(StringUtils.EMPTY, params, status().isBadRequest());
    }

    @Test
    public void testCheckExistByCode() {
        LOGGER.debug("testCheckExistByCode");
        Mockito.when(service.checkExist(any(), any())).thenReturn(true);

        final QueryDto criteria = QueryDto.criteria().addCriterion("code", 1, CriterionOperator.EQUALS);
        final ResultActions result = super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()), status().isOk());
    }

    @Test
    public void testCheckExistByBadCodeCriteriaWhenSanitizingThenReturnBadRequest() {
        LOGGER.debug("testCheckExistByBadCodeCriteriaWhenSanitizingThenReturnBadRequest");
        Mockito.when(service.checkExist(any(), any())).thenReturn(true);

        final QueryDto criteria = QueryDto.criteria().addCriterion("code<script>", 1, CriterionOperator.EQUALS);
        final ResultActions result = super
            .performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()),
                status().isBadRequest());
    }

    @Test
    public void testCheckExistByCodeNotFound() {
        LOGGER.debug("testCheckExistByCode");
        Mockito.when(service.checkExist(any(), any())).thenReturn(false);

        final QueryDto criteria = QueryDto.criteria().addCriterion("code", 1, CriterionOperator.EQUALS);
        final ResultActions result = super.performHead(CommonConstants.PATH_CHECK, ImmutableMap.of("criteria", criteria.toJson()), status().isNoContent());
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected Class<CustomerDto> getDtoClass() {
        return CustomerDto.class;
    }

    @Override
    protected CustomerDto buildDto() {
        return new CustomerDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.create(any(), any(CustomerCreationFormData.class))).thenReturn(buildDto());
    }
}
