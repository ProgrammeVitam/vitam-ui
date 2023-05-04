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
package fr.gouv.vitamui.ui.commons.config;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.iam.external.client.IamExternalWebClientFactory;
import fr.gouv.vitamui.referential.external.client.OntologyExternalRestClient;
import fr.gouv.vitamui.referential.external.client.ReferentialExternalRestClientFactory;
import fr.gouv.vitamui.referential.external.client.ReferentialExternalWebClientFactory;
import fr.gouv.vitamui.referential.external.client.UnitExternalRestClient;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Import(value = {AutoConfigurationVitam.class, AutoConfigurationRestController.class, AutoConfigurationService.class,
    RestExceptionHandler.class})
@AutoConfigureAfter
@EnableScheduling
public class UICommonsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UIProperties uiProperties() {
        throw new InternalServerException("You must define ui properties");
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public IamExternalRestClientFactory iamRestClientFactory(final UIProperties uiProperties,
        final RestTemplateBuilder restTemplateBuilder) {
        return new IamExternalRestClientFactory(uiProperties.getIamExternalClient(), restTemplateBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public IamExternalWebClientFactory iamExternalWebClientFactory(final UIProperties uiProperties,
        final RestTemplateBuilder restTemplateBuilder) {
        return new IamExternalWebClientFactory(uiProperties.getIamExternalClient());
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public ReferentialExternalRestClientFactory referentialRestClientFactory(final UIProperties uiProperties,
        final RestTemplateBuilder restTemplateBuilder) {
        return new ReferentialExternalRestClientFactory(uiProperties.getReferentialExternalClient(),
            restTemplateBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public ReferentialExternalWebClientFactory referentialWebClientFactory(final UIProperties uiProperties) {
        return new ReferentialExternalWebClientFactory(uiProperties.getReferentialExternalClient());
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public UnitExternalRestClient unitExternalRestClient(
        final ReferentialExternalRestClientFactory referentialRestClientFactory) {
        return referentialRestClientFactory.getUnitExternalRestClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public OntologyExternalRestClient ontologyExternalRestClient(
        final ReferentialExternalRestClientFactory referentialRestClientFactory) {
        return referentialRestClientFactory.getOntologyRestClient();
    }
}
