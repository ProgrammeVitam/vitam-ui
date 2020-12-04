package fr.gouv.vitamui.identity.rest;

import static org.mockito.ArgumentMatchers.any;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.identity.service.OwnerService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = { OwnerController.class })
public class OwnerControllerTest extends UiIdentityRestControllerTest<OwnerDto> {

    @Value("${ui-identity.prefix}")
    protected String apiUrl;

    @MockBean
    private OwnerService service;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OwnerControllerTest.class);

    private static final String PREFIX = "/owners";

    @MockBean
    private BuildProperties buildProperties;

    @Test
    public void testCreateOwner() {
        super.testCreateEntity();
    }

    @Test
    public void testUpdateOwner() {
        super.testUpdateEntity();
    }

    @Override
    protected void preparedServices() {
        Mockito.when(service.create(any(), any(OwnerDto.class))).thenReturn(new OwnerDto());
        Mockito.when(service.update(any(), any(OwnerDto.class))).thenReturn(new OwnerDto());
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + PREFIX;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected Class<OwnerDto> getDtoClass() {
        return OwnerDto.class;
    }

    @Override
    protected OwnerDto buildDto() {
        final OwnerDto owner = new OwnerDto();
        return owner;
    }

}
