/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
package fr.gouv.vitamui.pastis.server;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.test.rest.AbstractSwaggerJsonFileGenerationTest;
import fr.gouv.vitamui.iam.internal.client.IamInternalRestClientFactory;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.ExternalAuthentificationService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.client.SecurityRestClientFactory;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Swagger JSON Generation.
 * With this test class, we can generate the swagger json file without launching a full SpringBoot app.
 */
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(value = { SwaggerConfiguration.class })
@ActiveProfiles("dev, swagger")
public class SwaggerJsonFileGenerationTest extends AbstractSwaggerJsonFileGenerationTest {

    @MockBean
    public ContextRestClient contextCrudRestClient;

    @MockBean
    public ExternalApiAuthenticationProvider apiAuthenticationProvider;

    @MockBean
    public ExternalSecurityService externalSecurityService;

    @MockBean
    public ExternalAuthentificationService externalAuthentificationService;

    @MockBean
    public IamInternalRestClientFactory iamInternalRestClientFactory;

    @MockBean
    public UserInternalRestClient userInternalRestClient;

    @MockBean
    private RestExceptionHandler restExceptionHandler;

    @MockBean
    private AdminExternalClient adminExternalClient;

    @MockBean(name = "accessExternalClient")
    private AccessExternalClient accessExternalClient;

    @MockBean
    private InternalSecurityService internalSecurityService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private SecurityRestClientFactory securityRestClientFactory;
}
