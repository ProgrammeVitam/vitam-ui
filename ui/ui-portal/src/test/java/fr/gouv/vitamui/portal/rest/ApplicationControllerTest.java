/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitamui.portal.rest;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.ResultActions;

import com.google.common.collect.ImmutableMap;

import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.portal.config.PortalApplicationProperties;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoConfiguration;
import fr.gouv.vitamui.ui.commons.config.UICommonsAutoSpringMockConfiguration;
import fr.gouv.vitamui.ui.commons.rest.ApplicationController;
import fr.gouv.vitamui.ui.commons.rest.UIControllerTest;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(classes = { UICommonsAutoSpringMockConfiguration.class, UICommonsAutoConfiguration.class, ServerIdentityAutoConfiguration.class })
@Import(value = { PortalApplicationProperties.class })
@WebMvcTest(controllers = { ApplicationController.class })
public class ApplicationControllerTest extends UIControllerTest<ApplicationDto> {

    @Value("${ui-prefix}")
    protected String apiUrl;

    @MockBean
    private ApplicationService applicationService;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationControllerTest.class);

    private static final String PREFIX = "/ui/applications";

    @Test
    public void testGetApplications() throws Exception {
        super.testGetAllEntity();
    }

    @Test
    public void testfindApplicationsByNames() throws Exception {
        final ResultActions result = performGet(StringUtils.EMPTY, ImmutableMap.of("term", "ab", "lang", "ENGLISH"));
    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + apiUrl + ApplicationControllerTest.PREFIX;
    }

    @Override
    protected Class<ApplicationDto> getDtoClass() {
        return ApplicationDto.class;
    }

    @Override
    protected ApplicationDto buildDto() {
        final ApplicationDto application = new ApplicationDto();
        return application;
    }

    @Override
    protected VitamUILogger getLog() {
        return ApplicationControllerTest.LOGGER;
    }

    @Override
    protected void preparedServices() {
    }
}
