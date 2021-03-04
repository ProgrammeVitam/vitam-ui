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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import fr.gouv.vitamui.ui.commons.service.AccountService;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import fr.gouv.vitamui.ui.commons.service.ExternalParametersService;
import fr.gouv.vitamui.ui.commons.service.LogbookService;
import fr.gouv.vitamui.ui.commons.service.SubrogationService;
import fr.gouv.vitamui.ui.commons.service.UserService;

@Configuration
public class AutoConfigurationService {

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("uiProperties")
    public CommonService commonService(final UIProperties uiProperties) {
        return new CommonService(uiProperties);
    }

    @Bean
    @DependsOn("iamRestClientFactory")
    @ConditionalOnMissingBean
    public ApplicationService applicationService(final UIProperties uiProperties, final CasLogoutUrl casLogoutUrl, final IamExternalRestClientFactory factory, final BuildProperties buildProperties) {
        return new ApplicationService(uiProperties, casLogoutUrl, factory, buildProperties);
    }

    @Bean
    @DependsOn("iamRestClientFactory")
    @ConditionalOnMissingBean
    public LogbookService logbookService(final IamExternalRestClientFactory factory) {
        return new LogbookService(factory.getLogbookExternalRestClient());
    }
    
    @Bean
    @DependsOn("iamRestClientFactory")
    @ConditionalOnMissingBean
    public ExternalParametersService externalParametersService(final IamExternalRestClientFactory factory) {
        return new ExternalParametersService(factory.getExternalParametersExternalRestClient());
    }

    @Bean
    @DependsOn(value = { "iamRestClientFactory", "commonService" })
    @ConditionalOnMissingBean
    public SubrogationService subrogationService(final IamExternalRestClientFactory factory) {
        return new SubrogationService(factory);
    }

    @Bean("accountService")
    @DependsOn(value = { "iamRestClientFactory", "commonService" })
    @ConditionalOnMissingBean
    public AccountService accountService(final IamExternalRestClientFactory factory) {
        return new AccountService(factory);
    }

    @Bean("commonUserService")
    @DependsOn("iamRestClientFactory")
    @ConditionalOnMissingBean
    public UserService userService(final IamExternalRestClientFactory factory) {
        return new UserService(factory);
    }

}
