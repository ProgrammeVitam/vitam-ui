package fr.gouv.vitamui.ui.commons.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.info.BuildProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import fr.gouv.vitamui.iam.external.client.ApplicationExternalRestClient;
import fr.gouv.vitamui.ui.commons.config.UIPropertiesImpl;
import fr.gouv.vitamui.ui.commons.property.BaseUrl;
import fr.gouv.vitamui.ui.commons.property.PortalCategoryConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServerIdentityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:/application-test.properties")
public class ApplicationServiceTest extends ServiceTest<ApplicationDto> {

    private ApplicationService service;

    @Mock
    private UIPropertiesImpl properties;

    @Mock
    private BaseUrl baseUrl;

    @Mock
    private Map<String, PortalCategoryConfig> categoriesConfig;

    @Mock
    private CasLogoutUrl casLogoutUrl;

    @Mock
    private ApplicationExternalRestClient client;

    @Mock
    private BuildProperties buildProperties;

    @Before
    public void setup() {
        Mockito.when(properties.getBaseUrl()).thenReturn(baseUrl);
        Mockito.when(baseUrl.getPortal()).thenReturn("http://portal.vitamui.com");
        Mockito.when(baseUrl.getAdminIdentity()).thenReturn("http://identity.vitamui.com");
        Mockito.when(baseUrl.getIdentity()).thenReturn("http://identity.vitamui.com");
        Mockito.when(properties.getPortalCategories()).thenReturn(categoriesConfig);
        Mockito.when(categoriesConfig.size()).thenReturn(2);
        Mockito.when(casLogoutUrl.getValueWithRedirection(any())).thenReturn("http://identity.vitamui.com");
        Mockito.when(factory.getApplicationExternalRestClient()).thenReturn(client);
        service = new ApplicationService(properties, casLogoutUrl, factory, buildProperties);
    }

    @Test
    public void testApplicationService() {
        Assert.assertNotNull(service);
    }

    @Test
    public void testGetApplications() {
        final List<ApplicationDto> applicationsMock = buildCollectionDto("USERS", "CUSTOMERS");
        Mockito.when(client.getAll(isNull(), any(Optional.class))).thenReturn(applicationsMock);

        Map<String, Object> config = service.getApplications(null, true);
        Assert.assertNotNull(config);

        final Collection<ApplicationDto> applications = (Collection<ApplicationDto>) config.get(CommonConstants.APPLICATION_CONFIGURATION);
        Assert.assertNotNull(applications);
        final Collection<String> applicationsId = applications.stream().map(a -> a.getId()).collect(Collectors.toList());
        assertThat(applicationsId).containsExactly("USERS", "CUSTOMERS");

        final Map<String, PortalCategoryConfig> categories = (Map<String, PortalCategoryConfig>) config.get(CommonConstants.CATEGORY_CONFIGURATION);
        Assert.assertNotNull(categories);
        assertThat(categories.size()).isEqualTo(2);
    }

    @Test
    public void testGetConfiguration() {
        Mockito.when(buildProperties.get("version.release")).thenReturn("0.0.0");

        final Map<String, Object> map = service.getConf();
        Assert.assertNotNull(map);
        Assert.assertEquals("http://portal.vitamui.com", map.get(CommonConstants.PORTAL_URL));
        Assert.assertEquals("0.0", map.get(CommonConstants.VERSION_RELEASE));
    }

    @Test
    public void baseUrlPortal() {
        Assert.assertEquals("http://portal.vitamui.com", service.getBaseUrlPortal());
    }

    private List<ApplicationDto> buildCollectionDto(final String... ids) {
        final List<ApplicationDto> apps = new ArrayList<>();
        for (final String id : ids) {
            apps.add(buildDto(id));
        }
        return apps;
    }

    protected ApplicationDto buildDto(final String id) {
        final ApplicationDto app = new ApplicationDto();
        app.setId(id);
        app.setUrl("${BASE_URL_IDENTITY}/customer");
        return app;
    }


    @Override
    public ApplicationExternalRestClient getClient() {
        return client;
    }

    @Override
    protected AbstractCrudService<ApplicationDto> getService() {
        return service;
    }
}
