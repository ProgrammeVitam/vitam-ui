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

import fr.gouv.vitamui.cas.pm.PmTransientSessionTicketExpirationPolicyBuilder;
import fr.gouv.vitamui.cas.pm.ResetPasswordController;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.webflow.actions.CheckMfaTokenAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomDelegatedClientAuthenticationAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomSendTokenAction;
import fr.gouv.vitamui.cas.webflow.actions.DispatcherAction;
import fr.gouv.vitamui.cas.webflow.actions.GeneralTerminateSessionAction;
import fr.gouv.vitamui.cas.webflow.actions.I18NSendPasswordResetInstructionsAction;
import fr.gouv.vitamui.cas.webflow.actions.TriggerChangePasswordAction;
import fr.gouv.vitamui.cas.webflow.configurer.CustomCasSimpleMultifactorWebflowConfigurer;
import fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer;
import fr.gouv.vitamui.cas.x509.CustomRequestHeaderX509CertificateExtractor;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.factory.DefaultTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.X509CertificateCredentialsRequestHeaderAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * Web(flow) customizations.
 */
@Configuration
public class WebflowConfig {

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

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
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasExternalRestClient casRestClient;

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionStore")
    private ObjectProvider<SessionStore> delegatedClientDistributedSessionStore;

    @Autowired
    private Utils utils;

    @Autowired
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("messageSource")
    private HierarchicalMessageSource messageSource;

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationTicketFactory")
    private CasSimpleMultifactorAuthenticationTicketFactory casSimpleMultifactorAuthenticationTicketFactory;

    @Autowired
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier(DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
    private DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext;

    @Autowired
    @Qualifier("mfaSimpleMultifactorTokenCommunicationStrategy")
    private CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy;

    @Autowired
    @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
    private FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("frontChannelLogoutAction")
    private Action frontChannelLogoutAction;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Value("${vitamui.portal.url}")
    private String vitamuiPortalUrl;

    @Value("${cas.authn.surrogate.separator}")
    private String surrogationSeparator;

    @Value("${theme.vitamui-platform-name:VITAM-UI}")
    private String vitamuiPlatformName;

    @Value("${vitamui.authn.x509.enabled:false}")
    private boolean x509AuthnEnabled;

    @Value("${vitamui.authn.x509.mandatory:false}")
    private boolean x509AuthnMandatory;

    @Bean
    public DispatcherAction dispatcherAction() {
        return new DispatcherAction(
            providersService,
            identityProviderHelper,
            casRestClient,
            surrogationSeparator,
            utils,
            delegatedClientDistributedSessionStore.getObject()
        );
    }

    @Bean
    public DefaultTransientSessionTicketFactory pmTicketFactory() {
        return new DefaultTransientSessionTicketFactory(
            new PmTransientSessionTicketExpirationPolicyBuilder(casProperties)
        );
    }

    @Bean
    @RefreshScope
    public Action sendPasswordResetInstructionsAction() {
        val pmTicketFactory = new DefaultTicketFactory();
        pmTicketFactory.addTicketFactory(TransientSessionTicket.class, pmTicketFactory());

        return new I18NSendPasswordResetInstructionsAction(
            casProperties,
            communicationsManager,
            passwordManagementService,
            ticketRegistry,
            pmTicketFactory,
            messageSource,
            providersService,
            identityProviderHelper,
            utils,
            vitamuiPlatformName
        );
    }

    @Bean
    public TriggerChangePasswordAction triggerChangePasswordAction() {
        return new TriggerChangePasswordAction(ticketRegistrySupport, utils);
    }

    @Bean
    @Order(0)
    @RefreshScope
    public CasWebflowConfigurer defaultWebflowConfigurer() {
        final CustomLoginWebflowConfigurer c = new CustomLoginWebflowConfigurer(
            flowBuilderServices,
            loginFlowDefinitionRegistry,
            applicationContext,
            casProperties
        );
        c.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @RefreshScope
    @Bean
    @Lazy
    public Action delegatedAuthenticationAction() {
        return new CustomDelegatedClientAuthenticationAction(
            delegatedClientAuthenticationConfigurationContext,
            identityProviderHelper,
            providersService,
            utils,
            ticketRegistry,
            vitamuiPortalUrl,
            surrogationSeparator
        );
    }

    @Bean
    @RefreshScope
    public Action terminateSessionAction() {
        return new GeneralTerminateSessionAction(
            centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(),
            warnCookieGenerator.getObject(),
            logoutManager,
            applicationContext,
            utils,
            casRestClient,
            servicesManager,
            casProperties,
            frontChannelLogoutAction
        );
    }

    @Bean
    public ResetPasswordController resetPasswordController() {
        return new ResetPasswordController(
            casProperties,
            passwordManagementService,
            communicationsManager,
            ticketRegistry,
            messageSource,
            utils,
            pmTicketFactory()
        );
    }

    @Bean
    public Action loadSurrogatesListAction() {
        return StaticEventExecutionAction.SUCCESS;
    }

    @Bean
    @RefreshScope
    public Action mfaSimpleMultifactorSendTokenAction() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new CustomSendTokenAction(
            ticketRegistry,
            communicationsManager,
            casSimpleMultifactorAuthenticationTicketFactory,
            simple,
            mfaSimpleMultifactorTokenCommunicationStrategy,
            utils
        );
    }

    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer() {
        val cfg = new CustomCasSimpleMultifactorWebflowConfigurer(
            flowBuilderServices,
            loginFlowDefinitionRegistry,
            mfaSimpleAuthenticatorFlowRegistry,
            applicationContext,
            casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext)
        );
        cfg.setOrder(100);
        return cfg;
    }

    @Bean
    public Action checkMfaTokenAction() {
        return new CheckMfaTokenAction(ticketRegistry);
    }

    @Bean
    @Lazy
    @RefreshScope
    public Action delegatedAuthenticationClientLogoutAction() {
        return new ConsumerExecutionAction(ctx -> {});
    }

    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientFinishLogoutAction() {
        return new ConsumerExecutionAction(ctx -> {});
    }

    @Bean
    @RefreshScope
    public Action x509Check() {
        if (x509AuthnEnabled) {
            val sslHeaderName = casProperties.getAuthn().getX509().getSslHeaderName();
            val certificateExtractor = new CustomRequestHeaderX509CertificateExtractor(
                sslHeaderName,
                x509AuthnMandatory
            );

            return new X509CertificateCredentialsRequestHeaderAction(
                initialAuthenticationAttemptWebflowEventResolver.getObject(),
                serviceTicketRequestWebflowEventResolver.getObject(),
                adaptiveAuthenticationPolicy.getObject(),
                certificateExtractor,
                casProperties
            );
        } else {
            return new StaticEventExecutionAction("error");
        }
    }
}
