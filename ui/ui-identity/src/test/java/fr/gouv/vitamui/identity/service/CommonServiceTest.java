package fr.gouv.vitamui.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.identity.config.IdentityApplicationProperties;
import fr.gouv.vitamui.ui.commons.service.CommonService;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { "spring.config.name=ui-identity-application" })
public class CommonServiceTest {

    private static IdentityApplicationProperties applicationProperties;

    private static CommonService service;

    private static final int PAGINATIONLIMIT = 20;

    @BeforeClass
    public static void init() {
        applicationProperties = new IdentityApplicationProperties();
        applicationProperties.setLimitPagination(PAGINATIONLIMIT);
        service = new CommonService(applicationProperties);
    }

    @Test
    public void testPagination() {
        final int pagination = service.checkPagination(0, 1);
        assertThat(pagination).isEqualTo(1);
    }

    @Test
    public void testPaginationGreaterThanLimit() {
        final int pagination = service.checkPagination(0, 21);
        assertThat(pagination).isEqualTo(PAGINATIONLIMIT);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadPagination() {
        service.checkPagination(1, -1);
        fail();
    }
}
