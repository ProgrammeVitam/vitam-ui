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
package fr.gouv.vitamui.referential.external.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.administration.VitamOperationService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.iam.internal.client.IamInternalRestClientFactory;
import fr.gouv.vitamui.iam.internal.client.IamInternalWebClientFactory;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.service.AccessionRegisterService;
import fr.gouv.vitamui.referential.common.service.IngestContractService;
import fr.gouv.vitamui.referential.common.service.OntologyService;
import fr.gouv.vitamui.referential.common.service.OperationService;
import fr.gouv.vitamui.referential.common.service.VitamAgencyService;
import fr.gouv.vitamui.referential.common.service.VitamArchivalProfileUnitService;
import fr.gouv.vitamui.referential.common.service.VitamBatchReportService;
import fr.gouv.vitamui.referential.common.service.VitamContextService;
import fr.gouv.vitamui.referential.common.service.VitamFileFormatService;
import fr.gouv.vitamui.referential.common.service.VitamProfileService;
import fr.gouv.vitamui.referential.common.service.VitamRuleService;
import fr.gouv.vitamui.referential.common.service.VitamSecurityProfileService;
import fr.gouv.vitamui.referential.common.service.VitamUIAccessContractService;
import fr.gouv.vitamui.referential.common.service.VitamUIManagementContractService;
import fr.gouv.vitamui.referential.external.server.security.WebSecurityConfig;
import fr.gouv.vitamui.referential.internal.client.AccessContractInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.AccessContractInternalWebClient;
import fr.gouv.vitamui.referential.internal.client.AccessionRegisterDetailInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.AccessionRegisterSummaryInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.AgencyInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.AgencyInternalWebClient;
import fr.gouv.vitamui.referential.internal.client.ArchivalProfileInternalWebClient;
import fr.gouv.vitamui.referential.internal.client.ArchivalProfileUnitInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.ContextInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.FileFormatInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.FileFormatInternalWebClient;
import fr.gouv.vitamui.referential.internal.client.IngestContractInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.IngestContractInternalWebClient;
import fr.gouv.vitamui.referential.internal.client.LogbookManagementOperationInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.ManagementContractInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.OntologyInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.OntologyInternalWebClient;
import fr.gouv.vitamui.referential.internal.client.OperationInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.ProfileInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.ProfileInternalWebClient;
import fr.gouv.vitamui.referential.internal.client.ReferentialInternalRestClientFactory;
import fr.gouv.vitamui.referential.internal.client.ReferentialInternalWebClientFactory;
import fr.gouv.vitamui.referential.internal.client.RuleInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.RuleInternalWebClient;
import fr.gouv.vitamui.referential.internal.client.SchemaClient;
import fr.gouv.vitamui.referential.internal.client.SecurityProfileInternalRestClient;
import fr.gouv.vitamui.referential.internal.client.UnitInternalRestClient;
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.client.SecurityRestClientFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
        VitamAdministrationConfig.class,
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
    public FilterRegistrationBean multipartFilterRegistrationBean() {
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
    public ExternalSecurityService externalSecurityService() {
        return new ExternalSecurityService();
    }

    @Bean
    public IamInternalRestClientFactory iamInternalRestClientFactory(
        final ApiReferentialApplicationProperties apiReferentialApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new IamInternalRestClientFactory(
            apiReferentialApplicationProperties.getIamInternalClient(),
            restTemplateBuilder
        );
    }

    @Bean
    public IamInternalWebClientFactory internalWebClientFactory(
        final ApiReferentialApplicationProperties apiReferentialApplicationProperties
    ) {
        return new IamInternalWebClientFactory(apiReferentialApplicationProperties.getIamInternalClient());
    }

    @Bean
    public ReferentialInternalRestClientFactory referentialInternalRestClientFactory(
        final ApiReferentialApplicationProperties apiReferentialApplicationProperties,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new ReferentialInternalRestClientFactory(
            apiReferentialApplicationProperties.getReferentialInternalClient(),
            restTemplateBuilder
        );
    }

    @Bean
    public ReferentialInternalWebClientFactory referentialInternalWebClientFactory(
        final ApiReferentialApplicationProperties apiReferentialApplicationProperties
    ) {
        return new ReferentialInternalWebClientFactory(
            apiReferentialApplicationProperties.getReferentialInternalClient()
        );
    }

    @Bean
    public AccessContractInternalRestClient accessContractInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getAccessContractInternalRestClient();
    }

    @Bean
    public IngestContractInternalRestClient ingestContractInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getIngestContractInternalRestClient();
    }

    @Bean
    public AgencyInternalRestClient agencyInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getAgencyInternalRestClient();
    }

    @Bean
    public FileFormatInternalRestClient fileFormatInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getFileFormatInternalRestClient();
    }

    @Bean
    public ArchivalProfileUnitInternalRestClient archivalProfileInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getArchivalProfileInternalRestClient();
    }

    @Bean
    public ContextInternalRestClient contextInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getContextInternalRestClient();
    }

    @Bean
    public SecurityProfileInternalRestClient securityProfileInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getSecurityProfileInternalRestClient();
    }

    @Bean
    public OntologyInternalRestClient ontologyInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getOntologyInternalRestClient();
    }

    @Bean
    public OperationInternalRestClient operationInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getOperationInternalRestClient();
    }

    @Bean
    public AccessionRegisterSummaryInternalRestClient accessionRegisterInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getAccessionRegisterInternalRestClient();
    }

    @Bean
    public UnitInternalRestClient unitInternalRestClient(final ReferentialInternalRestClientFactory factory) {
        return factory.getUnitInternalRestClient();
    }

    @Bean
    public ManagementContractInternalRestClient managementContractInternalRestClient(
        final ReferentialInternalRestClientFactory factory
    ) {
        return factory.getManagementContractInternalRestClient();
    }

    @Bean
    public ProfileInternalRestClient profileInternalRestClient(final ReferentialInternalRestClientFactory factory) {
        return factory.getProfileInternalRestClient();
    }

    @Bean
    public RuleInternalRestClient ruleInternalRestClient(final ReferentialInternalRestClientFactory factory) {
        return factory.getRuleInternalRestClient();
    }

    @Bean
    public AgencyInternalWebClient agencyInternalWebClient(
        final ReferentialInternalWebClientFactory referentialInternalWebClientFactory
    ) {
        return referentialInternalWebClientFactory.getAgencyInternalWebClient();
    }

    @Bean
    public FileFormatInternalWebClient fileFormatInternalWebClient(
        final ReferentialInternalWebClientFactory referentialInternalWebClientFactory
    ) {
        return referentialInternalWebClientFactory.getFileFormatInternalWebClient();
    }

    @Bean
    public ArchivalProfileInternalWebClient archivalProfileInternalWebClient(
        final ReferentialInternalWebClientFactory referentialInternalWebClientFactory
    ) {
        return referentialInternalWebClientFactory.getArchivalProfileInternalWebClient();
    }

    @Bean
    public ProfileInternalWebClient profileInternalWebClient(
        final ReferentialInternalWebClientFactory referentialInternalWebClientFactory
    ) {
        return referentialInternalWebClientFactory.getProfileInternalWebClient();
    }

    @Bean
    public OntologyInternalWebClient ontologyInternalWebClient(
        final ReferentialInternalWebClientFactory referentialInternalWebClientFactory
    ) {
        return referentialInternalWebClientFactory.getOntologyInternalWebClient();
    }

    @Bean
    public LogbookManagementOperationInternalRestClient logbookManagementOperationInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getLogbookManagementOperationInternalRestClient();
    }

    @Bean
    public RuleInternalWebClient ruleInternalWebClient(
        final ReferentialInternalWebClientFactory referentialInternalWebClientFactory
    ) {
        return referentialInternalWebClientFactory.getRuleInternalWebClient();
    }

    @Bean
    public AccessionRegisterDetailInternalRestClient accessionRegisterDetailInternalRestClient(
        final ReferentialInternalRestClientFactory referentialInternalRestClientFactory
    ) {
        return referentialInternalRestClientFactory.getAccessionRegisterDetailInternalRestClient();
    }

    @Bean
    public SchemaClient schemaClient(final ReferentialInternalRestClientFactory referentialInternalRestClientFactory) {
        return referentialInternalRestClientFactory.getSchemaClient();
    }

    @Bean
    public AccessContractInternalWebClient accessContractInternalWebClient(
        final ReferentialInternalWebClientFactory referentialInternalWebClientFactory
    ) {
        return referentialInternalWebClientFactory.getAccessContractInternalWebClient();
    }

    @Bean
    public IngestContractInternalWebClient ingestContractInternalWebClient(
        final ReferentialInternalWebClientFactory referentialInternalWebClientFactory
    ) {
        return referentialInternalWebClientFactory.getIngestContractInternalWebClient();
    }

    @Bean
    public InternalSecurityService securityService() {
        return new InternalSecurityService();
    }

    @Bean
    public VitamUIAccessContractService vitamUIAccessContractService(final AdminExternalClient adminExternalClient) {
        return new VitamUIAccessContractService(adminExternalClient);
    }

    @Bean
    public AccessionRegisterService accessionRegisterService(final AdminExternalClient adminExternalClient) {
        return new AccessionRegisterService(adminExternalClient);
    }

    @Bean
    public VitamAgencyService vitamAgencyService(
        final AdminExternalClient adminClient,
        final AgencyService agencyService,
        ObjectMapper objectMapper,
        final AccessExternalClient accessClient
    ) {
        return new VitamAgencyService(adminClient, agencyService, objectMapper, accessClient);
    }

    @Bean
    public VitamArchivalProfileUnitService vitamArchivalProfileService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper,
        final AccessExternalClient accessClient
    ) {
        return new VitamArchivalProfileUnitService(adminClient, objectMapper, accessClient);
    }

    @Bean
    public VitamContextService vitamContextService(final AdminExternalClient adminClient, ObjectMapper objectMapper) {
        return new VitamContextService(adminClient, objectMapper);
    }

    @Bean
    public VitamFileFormatService vitamFileFormatService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper,
        final AccessExternalClient accessClient
    ) {
        return new VitamFileFormatService(adminClient, objectMapper, accessClient);
    }

    @Bean
    public VitamUIManagementContractService getVitamUIManagementContractService(final AdminExternalClient adminClient) {
        return new VitamUIManagementContractService(adminClient);
    }

    @Bean
    public OntologyService ontologyService(final AdminExternalClient adminExternalClient) {
        return new OntologyService(adminExternalClient);
    }

    @Bean
    public OperationService operationService(final AdminExternalClient adminExternalClient) {
        return new OperationService(adminExternalClient);
    }

    @Bean
    public VitamProfileService vitamProfileService(final AdminExternalClient adminClient, ObjectMapper objectMapper) {
        return new VitamProfileService(adminClient, objectMapper);
    }

    @Bean
    public VitamRuleService vitamRuleService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper,
        final AccessExternalClient accessClient
    ) {
        return new VitamRuleService(adminClient, objectMapper, accessClient);
    }

    @Bean
    public VitamSecurityProfileService vitamSecurityProfileService(
        final AdminExternalClient adminClient,
        ObjectMapper objectMapper
    ) {
        return new VitamSecurityProfileService(adminClient, objectMapper);
    }

    @Bean
    public IngestContractService ingestContractService(final AdminExternalClient adminExternalClient) {
        return new IngestContractService(adminExternalClient);
    }

    @Bean
    public VitamBatchReportService vitamBatchReportService(final AdminExternalClient adminExternalClient) {
        return new VitamBatchReportService(adminExternalClient);
    }

    @Bean
    @ConfigurationProperties(prefix = "clients.iam-internal")
    public RestClientConfiguration IamInternalRestClientConfiguration() {
        return new RestClientConfiguration();
    }

    @Bean
    public IamInternalRestClientFactory iamInternalRestClientFactory(
        final RestClientConfiguration IamInternalRestClientConfiguration,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        return new IamInternalRestClientFactory(IamInternalRestClientConfiguration, restTemplateBuilder);
    }

    @Bean
    public UnitService unitService(final AccessExternalClient client) {
        return new UnitService(client);
    }

    @Bean
    public VitamOperationService vitamOperationService(final AdminExternalClient adminExternalClient) {
        return new VitamOperationService(adminExternalClient);
    }
}
