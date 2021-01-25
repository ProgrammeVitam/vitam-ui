/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential;

import fr.gouv.vitamui.referential.service.AccessContractService;
import fr.gouv.vitamui.referential.service.AccessionRegisterService;
import fr.gouv.vitamui.referential.service.AgencyService;
import fr.gouv.vitamui.referential.service.ContextService;
import fr.gouv.vitamui.referential.service.CustomerService;
import fr.gouv.vitamui.referential.service.FileFormatService;
import fr.gouv.vitamui.referential.service.IngestContractService;
import fr.gouv.vitamui.referential.service.ManagementContractService;
import fr.gouv.vitamui.referential.service.OntologyService;
import fr.gouv.vitamui.referential.service.OperationService;
import fr.gouv.vitamui.referential.service.ProfileService;
import fr.gouv.vitamui.referential.service.RuleService;
import fr.gouv.vitamui.referential.service.SecurityProfileService;
import fr.gouv.vitamui.referential.service.TenantService;
import fr.gouv.vitamui.referential.service.UnitService;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.test.rest.AbstractSwaggerJsonFileGenerationTest;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;

/**
 * Swagger JSON Generation.
 * With this test class, we can generate the swagger json file without launching a full SpringBoot app.
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(value = { SecurityConfig.class, ServerIdentityConfiguration.class, SwaggerConfiguration.class })
@TestPropertySource(properties = { "spring.config.name=ui-referential-application" })
@ActiveProfiles("test, swagger")
public class SwaggerJsonFileGenerationTest extends AbstractSwaggerJsonFileGenerationTest {

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private AccessContractService accessContractService;

    @MockBean
    private AccessionRegisterService accessionRegisterService;

    @MockBean
    private AgencyService agencyService;

    @MockBean
    private ContextService contextService;

    @MockBean
    private FileFormatService fileFormatService;

    @MockBean
    private IngestContractService ingestContractService;

    @MockBean
    private ManagementContractService managementContractService;

    @MockBean
    private OntologyService ontologyService;

    @MockBean
    private OperationService operationService;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private SecurityProfileService securityProfileService;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private UnitService unitService;

    @MockBean
    private RuleService ruleService;

    @MockBean
    private CustomerService customerService;
}
