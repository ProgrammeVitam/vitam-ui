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
package fr.gouv.vitamui.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorWebflowConfigurer;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.view.CasProtocolView;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.thymeleaf.spring5.SpringTemplateEngine;

import fr.gouv.vitamui.cas.pm.PmTransientSessionTicketExpirationPolicyBuilder;
import fr.gouv.vitamui.cas.pm.ResetPasswordController;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.webflow.actions.CheckMfaTokenAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomDelegatedClientAuthenticationAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomInitialFlowSetupAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomSendTokenAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomVerifyPasswordResetRequestAction;
import fr.gouv.vitamui.cas.webflow.actions.DispatcherAction;
import fr.gouv.vitamui.cas.webflow.actions.GeneralTerminateSessionAction;
import fr.gouv.vitamui.cas.webflow.actions.I18NSendPasswordResetInstructionsAction;
import fr.gouv.vitamui.cas.webflow.actions.NoOpAction;
import fr.gouv.vitamui.cas.webflow.actions.SelectRedirectAction;
import fr.gouv.vitamui.cas.webflow.actions.TriggerChangePasswordAction;
import fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.val;

/**
 * Web(flow) customizations.
 *
 *
 */
@Configuration
public class WebflowConfig {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private ObjectProvider<AuthenticationEventExecutionPlan> AuthenticationEventExecutionStrategies;

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordManagementService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ProvidersService providersService;

    @Autowired
    private IdentityProviderHelper identityProviderHelper;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasExternalRestClient casRestClient;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("builtClients")
    private ObjectProvider<Clients> builtClients;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private ObjectProvider<SingleSignOnParticipationStrategy> webflowSingleSignOnParticipationStrategy;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionStore")
    private ObjectProvider<SessionStore> delegatedClientDistributedSessionStore;

    @Autowired
    private Utils utils;

    @Autowired
    private DelegatedClientWebflowManager delegatedClientWebflowManager;

    @Autowired
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("messageSource")
    private HierarchicalMessageSource messageSource;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationTicketFactory")
    private TransientSessionTicketFactory casSimpleMultifactorAuthenticationTicketFactory;

    @Value("${vitamui.portal.url}")
    private String vitamuiPortalUrl;

    @Value("${cas.authn.surrogate.separator}")
    private String surrogationSeparator;

    @Bean
    public DispatcherAction dispatcherAction() {
        return new DispatcherAction(providersService, identityProviderHelper, casRestClient,
            surrogationSeparator, utils, delegatedClientDistributedSessionStore.getObject());
    }

    @RefreshScope
    @Bean
    public Action initialFlowSetupAction() {
        return new CustomInitialFlowSetupAction(CollectionUtils.wrap(argumentExtractor.getObject()), servicesManager.getObject(),
                authenticationRequestServiceSelectionStrategies.getObject(), ticketGrantingTicketCookieGenerator.getObject(), warnCookieGenerator.getObject(),
                casProperties, AuthenticationEventExecutionStrategies.getObject(), webflowSingleSignOnParticipationStrategy.getObject(), ticketRegistrySupport);
    }

    @Bean
    public DefaultTransientSessionTicketFactory pmTicketFactory() {
        return new DefaultTransientSessionTicketFactory(new PmTransientSessionTicketExpirationPolicyBuilder(casProperties));
    }

    @Bean
    @RefreshScope
    public Action sendPasswordResetInstructionsAction() {
        val pmTicketFactory = new DefaultTicketFactory();
        pmTicketFactory.addTicketFactory(TransientSessionTicket.class, pmTicketFactory());

        return new I18NSendPasswordResetInstructionsAction(casProperties, communicationsManager, passwordManagementService,
            ticketRegistry, pmTicketFactory, messageSource, providersService, identityProviderHelper, utils);
    }

    @Bean
    public SelectRedirectAction selectRedirectAction() {
        return new SelectRedirectAction(centralAuthenticationService.getObject());
    }

    @Bean
    public TriggerChangePasswordAction triggerChangePasswordAction() {
        return new TriggerChangePasswordAction(ticketRegistrySupport, utils);
    }

    @Bean
    @Order(0)
    @RefreshScope
    public CasWebflowConfigurer defaultWebflowConfigurer() {
        final CustomLoginWebflowConfigurer c = new CustomLoginWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @RefreshScope
    @Bean
    @Lazy
    public Action delegatedAuthenticationAction() {
        return new CustomDelegatedClientAuthenticationAction(
            initialAuthenticationAttemptWebflowEventResolver.getObject(),
            serviceTicketRequestWebflowEventResolver.getObject(),
            adaptiveAuthenticationPolicy.getObject(),
            builtClients.getObject(),
            servicesManager.getObject(),
            registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer.getObject(),
            delegatedClientWebflowManager,
            authenticationSystemSupport.getObject(),
            casProperties,
            authenticationRequestServiceSelectionStrategies.getObject(),
            centralAuthenticationService.getObject(),
            webflowSingleSignOnParticipationStrategy.getObject(),
            delegatedClientDistributedSessionStore.getObject(),
            CollectionUtils.wrap(argumentExtractor.getObject()),
            identityProviderHelper,
            providersService,
            utils,
            ticketRegistry,
            vitamuiPortalUrl,
            surrogationSeparator);
    }

    @Bean
    @RefreshScope
    public Action terminateSessionAction() {
        return new GeneralTerminateSessionAction(centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(),
            warnCookieGenerator.getObject(),
            casProperties.getLogout());
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CasProtocolView casGetResponseView() {
        return new CasProtocolView("protocol/casGetResponseView",
            applicationContext, springTemplateEngine, thymeleafProperties);
    }

    @Bean
    public ResetPasswordController resetPasswordController() {
        return new ResetPasswordController(casProperties, passwordManagementService, communicationsManager, ticketRegistry,
            messageSource, utils, pmTicketFactory());
    }

    @Bean
    public Action loadSurrogatesListAction() {
        return new NoOpAction("success");
    }

    @Bean
    @RefreshScope
    public Action mfaSimpleMultifactorSendTokenAction() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new CustomSendTokenAction(ticketRegistry, communicationsManager,
            casSimpleMultifactorAuthenticationTicketFactory, simple, utils);
    }

    @Bean
    public FlowDefinitionRegistry customMfaSimpleAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath(CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/mfa-simple/mfa-simple-custom-webflow.xml");
        return builder.build();
    }

    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer() {
        return new CasSimpleMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
            customMfaSimpleAuthenticatorFlowRegistry(), applicationContext, casProperties);
    }

    @Bean
    public Action checkMfaTokenAction() {
        return new CheckMfaTokenAction(ticketRegistry);
    }

    @Bean
    @Lazy
    @RefreshScope
    public Action delegatedAuthenticationClientLogoutAction() {
        return new NoOpAction(null);
    }

    @Bean
    @RefreshScope
    public Action verifyPasswordResetRequestAction() {
        return new CustomVerifyPasswordResetRequestAction(casProperties, passwordManagementService, centralAuthenticationService.getObject());
    }
}
