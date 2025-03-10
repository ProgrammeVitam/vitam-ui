/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 * <p>
 * contact@programmevitam.fr
 * <p>
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 * <p>
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
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
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.UnitCommonService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyCommonService;
import fr.gouv.vitamui.commons.vitam.api.administration.VitamOperationCommonService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.iam.client.ApplicationRestClient;
import fr.gouv.vitamui.iam.client.ExternalParametersRestClient;
import fr.gouv.vitamui.iam.client.IamRestClientFactory;
import fr.gouv.vitamui.iam.client.UserRestClient;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.AuthentificationService;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.service.AccessionRegisterCommonService;
import fr.gouv.vitamui.referential.common.service.ImportSchemaCommonService;
import fr.gouv.vitamui.referential.common.service.IngestContractCommonService;
import fr.gouv.vitamui.referential.common.service.OntologyCommonService;
import fr.gouv.vitamui.referential.common.service.OperationCommonService;
import fr.gouv.vitamui.referential.common.service.VitamAgencyCommonService;
import fr.gouv.vitamui.referential.common.service.VitamArchivalProfileUnitCommonService;
import fr.gouv.vitamui.referential.common.service.VitamBatchReportCommonService;
import fr.gouv.vitamui.referential.common.service.VitamContextCommonService;
import fr.gouv.vitamui.referential.common.service.VitamFileFormatCommonService;
import fr.gouv.vitamui.referential.common.service.VitamRuleCommonService;
import fr.gouv.vitamui.referential.common.service.VitamSecurityProfileCommonService;
import fr.gouv.vitamui.referential.common.service.VitamUIAccessContractCommonService;
import fr.gouv.vitamui.referential.common.service.VitamUIManagementContractCommonService;
import fr.gouv.vitamui.referential.server.security.WebSecurityConfig;
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.client.SecurityRestClientFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

@Configuration
@Import(
    {
        RestExceptionHandler.class,
        SwaggerConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        WebSecurityConfig.class,
        VitamAccessConfig.class,
        VitamAdministrationConfig.class,
        ConverterConfig.class,
    }
)
public class ApiReferentialServerConfig extends AbstractContextConfiguration {

    @Bean
    public MultipartResolver multipartResolver() {
        final MultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        return commonsMultipartResolver;
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        final MultipartFilter multipartFilter = new MultipartFilter();
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(multipartFilter);
        filterRegistrationBean.addInitParameter("multipartResolverBeanName", "commonsMultipartResolver");
        return filterRegistrationBean;
    }

    @Bean
    public SecurityRestClientFactory securityRestClientFactory(
        final ApiReferentialApplicationProperties apiReferentialApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new SecurityRestClientFactory(
            apiReferentialApplicationProperties.getSecurityClient(),
            restTemplateBuilder
        );
    }

    @Bean
    public ContextRestClient contextCrudRestClient(final SecurityRestClientFactory securityRestClientFactory) {
        return securityRestClientFactory.getContextRestClient();
    }

    @Bean
    public SecurityService securityService() {
        return new SecurityService();
    }

    @Bean
    public VitamUIAccessContractCommonService vitamUIAccessContractCommonService(
        final AdminExternalClient adminExternalClient
    ) {
        return new VitamUIAccessContractCommonService(adminExternalClient);
    }

    @Bean
    public AccessionRegisterCommonService accessionRegisterCommonService(
        final AdminExternalClient adminExternalClient
    ) {
        return new AccessionRegisterCommonService(adminExternalClient);
    }

    @Bean
    public VitamAgencyCommonService vitamAgencyCommonService(
        final AdminExternalClient adminClient,
        final AgencyCommonService agencyCommonService,
        ObjectMapper objectMapper,
        final AccessExternalClient accessClient
    ) {
        return new VitamAgencyCommonService(adminClient, agencyCommonService, objectMapper, accessClient);
    }

    @Bean
    public VitamArchivalProfileUnitCommonService vitamArchivalProfileUnitCommonService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper,
        final AccessExternalClient accessClient
    ) {
        return new VitamArchivalProfileUnitCommonService(adminClient, objectMapper, accessClient);
    }

    @Bean
    public VitamContextCommonService vitamContextCommonService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper
    ) {
        return new VitamContextCommonService(adminClient, objectMapper);
    }

    @Bean
    public VitamFileFormatCommonService vitamFileFormatCommonService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper,
        final AccessExternalClient accessClient
    ) {
        return new VitamFileFormatCommonService(adminClient, objectMapper, accessClient);
    }

    @Bean
    public VitamUIManagementContractCommonService vitamUIManagementContractCommonService(
        final AdminExternalClient adminClient
    ) {
        return new VitamUIManagementContractCommonService(adminClient);
    }

    @Bean
    public OntologyCommonService ontologyCommonService(final AdminExternalClient adminExternalClient) {
        return new OntologyCommonService(adminExternalClient);
    }

    @Bean
    public OperationCommonService operationCommonService(final AdminExternalClient adminExternalClient) {
        return new OperationCommonService(adminExternalClient);
    }

    @Bean
    public VitamRuleCommonService vitamRuleCommonService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper,
        final AccessExternalClient accessClient
    ) {
        return new VitamRuleCommonService(adminClient, objectMapper, accessClient);
    }

    @Bean
    public VitamSecurityProfileCommonService vitamSecurityProfileCommonService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper
    ) {
        return new VitamSecurityProfileCommonService(adminClient, objectMapper);
    }

    @Bean
    public IngestContractCommonService ingestContractCommonService(final AdminExternalClient adminExternalClient) {
        return new IngestContractCommonService(adminExternalClient);
    }

    @Bean
    public VitamBatchReportCommonService vitamBatchReportCommonService(final AdminExternalClient adminExternalClient) {
        return new VitamBatchReportCommonService(adminExternalClient);
    }

    @Bean
    public IamRestClientFactory iamRestClientFactory(
        final ApiReferentialApplicationProperties apiReferentialApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new IamRestClientFactory(apiReferentialApplicationProperties.getIamClient(), restTemplateBuilder);
    }

    @Bean
    public ApplicationRestClient applicationRestClient(final IamRestClientFactory iamRestClientFactory) {
        return iamRestClientFactory.getApplicationExternalRestClient();
    }

    @Bean
    public UnitCommonService unitCommonService(final AccessExternalClient client) {
        return new UnitCommonService(client);
    }

    @Bean
    public VitamOperationCommonService vitamOperationCommonService(final AdminExternalClient adminExternalClient) {
        return new VitamOperationCommonService(adminExternalClient);
    }

    @Bean
    public ExternalParametersRestClient externalParametersRestClient(final IamRestClientFactory iamRestClientFactory) {
        return iamRestClientFactory.getExternalParametersExternalRestClient();
    }

    @Bean
    public UserRestClient userRestClient(final IamRestClientFactory iamRestClientFactory) {
        return iamRestClientFactory.getUserExternalRestClient();
    }

    @Bean
    public AuthentificationService authentificationService(
        final ContextRestClient contextRestClient,
        final UserRestClient userRestClient
    ) {
        return new AuthentificationService(contextRestClient, userRestClient);
    }

    @Bean
    public ApiAuthenticationProvider apiAuthenticationProvider(final AuthentificationService authentificationService) {
        return new ApiAuthenticationProvider(authentificationService);
    }

    @Bean
    public ImportSchemaCommonService importSchemaCommonService(final AdminExternalClient adminExternalClient) {
        return new ImportSchemaCommonService(adminExternalClient);
    }
}
