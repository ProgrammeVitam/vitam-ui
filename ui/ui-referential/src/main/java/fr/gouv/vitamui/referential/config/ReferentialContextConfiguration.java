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
package fr.gouv.vitamui.referential.config;

import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.iam.external.client.CustomerExternalRestClient;
import fr.gouv.vitamui.iam.external.client.ExternalParametersExternalRestClient;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import fr.gouv.vitamui.iam.external.client.TenantExternalRestClient;
import fr.gouv.vitamui.referential.external.client.*;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.security.SecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties
@Import(value = {SecurityConfig.class, SwaggerConfiguration.class, RestExceptionHandler.class})
public class ReferentialContextConfiguration extends AbstractContextConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public IamExternalWebClientFactory iamWebClientFactory(final UIProperties uiProperties) {
        return new IamExternalWebClientFactory(uiProperties.getIamExternalClient());
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public ReferentialExternalRestClientFactory referentialRestClientFactory(final ReferentialApplicationProperties uiProperties, RestTemplateBuilder restTemplateBuilder) {
        return new ReferentialExternalRestClientFactory(uiProperties.getReferentialExternalClient(), restTemplateBuilder);
    }
    
    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public ReferentialExternalWebClientFactory referentialWebClientFactory(final ReferentialApplicationProperties uiProperties) {
    	return new ReferentialExternalWebClientFactory(uiProperties.getReferentialExternalClient());
    }


    @Bean
    public AccessContractExternalRestClient accessContractExternalRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getAccessContractExternalRestClient();
    }

    @Bean
    public IngestContractExternalRestClient ingestContractExternalRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getIngestContractExternalRestClient();
    }

    @Bean
    public AgencyExternalRestClient agencyCrudRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getAgencyExternalRestClient();
    }

    @Bean
    public FileFormatExternalRestClient fileFormatCrudRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getFileFormatExternalRestClient();
    }

    @Bean
    public ContextExternalRestClient contextCrudRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getContextExternalRestClient();
    }

    @Bean
    public SecurityProfileExternalRestClient securityProfileCrudRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getSecurityProfileExternalRestClient();
    }

    @Bean
    public OntologyExternalRestClient ontologyCrudRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getOntologyRestClient();
    }

    @Bean
    public TenantExternalRestClient tenantCrudRestClient(final IamExternalRestClientFactory iamExternalRestClientFactory) {
        return iamExternalRestClientFactory.getTenantExternalRestClient();
    }
    
    @Bean
    public CustomerExternalRestClient customerCrudRestClient(final IamExternalRestClientFactory iamExternalRestClientFactory) {
        return iamExternalRestClientFactory.getCustomerExternalRestClient();
    }

    @Bean
    public OperationExternalRestClient auditCrudRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getOperationExternalRestClient();
    }

    @Bean
    public AccessionRegisterExternalRestClient accessionRegisterExternalRestClient(final ReferentialExternalRestClientFactory referentialExternalRestClientFactory) {
        return referentialExternalRestClientFactory.getAccessionRegisterExternalRestClient();
    }

    @Bean
    public UnitExternalRestClient unitExternalRestClient(final ReferentialExternalRestClientFactory factory) {
        return factory.getUnitExternalRestClient();
    }

    @Bean
    public ManagementContractExternalRestClient managementContractExternalRestClient(final ReferentialExternalRestClientFactory factory) {
        return factory.getManagementContractRestClient();
    }

    @Bean
    public ProfileExternalRestClient profileExternalRestClient(final ReferentialExternalRestClientFactory factory) {
        return factory.getProfileExternalRestClient();
    }

    @Bean
    public RuleExternalRestClient ruleExternalRestClient(final ReferentialExternalRestClientFactory factory) {
        return factory.getRuleExternalRestClient();
    }
    
    @Bean
    public AgencyExternalWebClient agencyWebRestClient(final ReferentialExternalWebClientFactory referentialExternalWebClientFactory) {
    	return referentialExternalWebClientFactory.getAgencyExternalWebClient();
    }
    
    @Bean
    public FileFormatExternalWebClient fileFormatWebRestClient(final ReferentialExternalWebClientFactory referentialExternalWebClientFactory) {
    	return referentialExternalWebClientFactory.getFileFormatExternalWebClient();
    }
    
    @Bean
    public OntologyExternalWebClient ontologyWebRestClient(final ReferentialExternalWebClientFactory referentialExternalWebClientFactory) {
    	return referentialExternalWebClientFactory.getOntologyExternalWebClient();
    }
    
    @Bean
    public ExternalParametersExternalRestClient externalParametersExternalRestClient(final IamExternalRestClientFactory factory) {
        return factory.getExternalParametersExternalRestClient();
    }
}
