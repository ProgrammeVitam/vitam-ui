package fr.gouv.vitamui.referential.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.referential.service.TenantService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { TenantController.class })
public class TenantControllerTest extends UiReferentialRestControllerTest<TenantDto> {

	@Value("${ui-referential.prefix}")
	protected String apiUrl;

	@MockBean
	private TenantService service;

	private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantControllerTest.class);

	private static final String PREFIX = "/tenants";

	@Test
	public void testGetAllTenants() {
		super.testGetAllEntityWithCriteria();
	}

	@Override
	protected String getRessourcePrefix() {
		return "/" + apiUrl + PREFIX;
	}

	@Override
	protected Class<TenantDto> getDtoClass() {
		return TenantDto.class;
	}

	@Override
	protected TenantDto buildDto() {
		final TenantDto tenant = new TenantDto();
		return tenant;
	}

	@Override
	protected VitamUILogger getLog() {
		return LOGGER;
	}

	@Override
	protected void preparedServices() {
	}
}
