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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import fr.gouv.vitamui.ui.commons.rest.AccountController;
import fr.gouv.vitamui.ui.commons.rest.ApplicationController;
import fr.gouv.vitamui.ui.commons.rest.ExternalParametersController;
import fr.gouv.vitamui.ui.commons.rest.LogbookController;
import fr.gouv.vitamui.ui.commons.rest.SecurityController;
import fr.gouv.vitamui.ui.commons.rest.SubrogationController;
import fr.gouv.vitamui.ui.commons.rest.UserController;
import fr.gouv.vitamui.ui.commons.service.AccountService;
import fr.gouv.vitamui.ui.commons.service.ApplicationService;
import fr.gouv.vitamui.ui.commons.service.ExternalParametersService;
import fr.gouv.vitamui.ui.commons.service.LogbookService;
import fr.gouv.vitamui.ui.commons.service.SubrogationService;
import fr.gouv.vitamui.ui.commons.service.UserService;

@Configuration
public class AutoConfigurationRestController {

    @Bean("subrogationController")
    @DependsOn(value = "subrogationService")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "controller.subrogation", value = "enabled", matchIfMissing = false)
    public SubrogationController subrogationController(final SubrogationService subrogationService) {
        return new SubrogationController(subrogationService);
    }

    @Bean("applicationController")
    @DependsOn("applicationService")
    @ConditionalOnProperty(prefix = "controller.application", value = "enabled", matchIfMissing = true)
    public ApplicationController applicationController(final ApplicationService applicationService) {
        return new ApplicationController(applicationService);
    }

    @Bean("accountController")
    @DependsOn("accountService")
    @ConditionalOnProperty(prefix = "controller.account", value = "enabled", matchIfMissing = true)
    public AccountController accountController(final AccountService accountService) {
        return new AccountController(accountService);
    }

    @Bean("securityController")
    @ConditionalOnProperty(prefix = "controller.security", value = "enabled", matchIfMissing = true)
    public SecurityController securityController() {
        return new SecurityController();
    }

    @Bean("logbookController")
    @DependsOn("logbookService")
    public LogbookController logbookController(final LogbookService logbookService) {
        return new LogbookController(logbookService);
    }
    
    @Bean("externalParametersController")
    @DependsOn("externalParametersService")
    public ExternalParametersController externalParametersController(final ExternalParametersService externalParametersService) {
        return new ExternalParametersController(externalParametersService);
    }

    @Bean("commonUserController")
    @DependsOn("commonUserService")
    @ConditionalOnProperty(prefix = "controller.user", value = "enabled", matchIfMissing = true)
    public UserController userController(final UserService userService) {
        return new UserController(userService);
    }

}
