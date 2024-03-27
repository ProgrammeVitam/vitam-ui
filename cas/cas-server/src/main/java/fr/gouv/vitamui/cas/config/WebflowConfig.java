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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.cas.pm.PmTransientSessionTicketExpirationPolicyBuilder;
import fr.gouv.vitamui.cas.pm.ResetPasswordController;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.web.CustomOidcRevocationEndpointController;
import fr.gouv.vitamui.cas.webflow.actions.CheckMfaTokenAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomDelegatedAuthenticationClientLogoutAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomDelegatedClientAuthenticationAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomSendTokenAction;
import fr.gouv.vitamui.cas.webflow.actions.DispatcherAction;
import fr.gouv.vitamui.cas.webflow.actions.GeneralTerminateSessionAction;
import fr.gouv.vitamui.cas.webflow.actions.I18NSendPasswordResetInstructionsAction;
import fr.gouv.vitamui.cas.webflow.actions.ListCustomersAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomerSelectedAction;
import fr.gouv.vitamui.cas.webflow.actions.TriggerChangePasswordAction;
import fr.gouv.vitamui.cas.webflow.configurer.CustomCasSimpleMultifactorWebflowConfigurer;
import fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer;
import fr.gouv.vitamui.cas.webflow.resolver.CustomCasDelegatingWebflowEventResolver;
import fr.gouv.vitamui.cas.x509.CustomRequestHeaderX509CertificateExtractor;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.web.controllers.token.OidcRevocationEndpointController;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.factory.DefaultTicketFactory;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.X509CertificateCredentialsRequestHeaderAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * Webflow customizations.
 */
@Configuration
public class WebflowConfig {

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

    @Value("${theme.vitamui-platform-name:VITAM-UI}")
    private String vitamuiPlatformName;

    @Value("${vitamui.authn.x509.enabled:false}")
    private boolean x509AuthnEnabled;

    @Value("${vitamui.authn.x509.mandatory:false}")
    private boolean x509AuthnMandatory;

    @Bean
    public ListCustomersAction listCustomersAction() {
        return new ListCustomersAction(providersService, identityProviderHelper, casRestClient, utils);
    }

    @Bean
    public CustomerSelectedAction customerSelectedAction() {
        return new CustomerSelectedAction();
    }

    @Bean
    public DispatcherAction dispatcherAction() {
        return new DispatcherAction(providersService, identityProviderHelper, casRestClient, utils,
            delegatedClientDistributedSessionStore.getObject());
    }

    @Bean
    public DefaultTransientSessionTicketFactory pmTicketFactory() {
        return new DefaultTransientSessionTicketFactory(
            new PmTransientSessionTicketExpirationPolicyBuilder(casProperties));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action sendPasswordResetInstructionsAction(
        final CasConfigurationProperties casProperties,
        @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
        final PasswordManagementService passwordManagementService,
        @Qualifier(TicketRegistry.BEAN_NAME) final TicketRegistry ticketRegistry,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver,
        @Qualifier(CommunicationsManager.BEAN_NAME) final CommunicationsManager communicationsManager,
        @Qualifier(TicketFactory.BEAN_NAME) final TicketFactory ticketFactory,
        @Qualifier(PasswordResetUrlBuilder.BEAN_NAME) final PasswordResetUrlBuilder passwordResetUrlBuilder) {
        val pmTicketFactory = new DefaultTicketFactory();
        pmTicketFactory.addTicketFactory(TransientSessionTicket.class, pmTicketFactory());

        return new I18NSendPasswordResetInstructionsAction(casProperties, communicationsManager,
            passwordManagementService, ticketRegistry, pmTicketFactory,
            defaultPrincipalResolver, passwordResetUrlBuilder, messageSource, providersService,
            identityProviderHelper, utils, vitamuiPlatformName);
    }

    @Bean
    public TriggerChangePasswordAction triggerChangePasswordAction() {
        return new TriggerChangePasswordAction(ticketRegistrySupport, utils);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer defaultWebflowConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry logoutFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
        val c =
            new CustomLoginWebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public Action delegatedAuthenticationAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(DelegatedClientAuthenticationFailureEvaluator.BEAN_NAME)
        final DelegatedClientAuthenticationFailureEvaluator delegatedClientAuthenticationFailureEvaluator,
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
        final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
        @Qualifier(DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME)
        final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(
                () -> new CustomDelegatedClientAuthenticationAction(delegatedClientAuthenticationConfigurationContext,
                    delegatedClientWebflowManager, delegatedClientAuthenticationFailureEvaluator,
                    identityProviderHelper,
                    providersService, utils, ticketRegistry, casRestClient, vitamuiPortalUrl))
            .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action terminateSessionAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(LogoutManager.DEFAULT_BEAN_NAME) final LogoutManager logoutManager,
        @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
        final CasCookieBuilder ticketGrantingTicketCookieGenerator,
        @Qualifier("warnCookieGenerator") final CasCookieBuilder warnCookieGenerator,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
        final SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor,
        @Qualifier(ServiceTicketSessionTrackingPolicy.BEAN_NAME)
        final ServiceTicketSessionTrackingPolicy serviceTicketSessionTrackingPolicy) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new GeneralTerminateSessionAction(centralAuthenticationService,
                ticketGrantingTicketCookieGenerator,
                warnCookieGenerator, casProperties.getLogout(), logoutManager, applicationContext,
                defaultSingleLogoutRequestExecutor,
                utils, casRestClient, servicesManager, casProperties, frontChannelLogoutAction, ticketRegistry,
                serviceTicketSessionTrackingPolicy))
            .withId(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
            .build()
            .get();
    }

    @Bean
    public ResetPasswordController resetPasswordController(
        @Qualifier(PasswordResetUrlBuilder.BEAN_NAME) final PasswordResetUrlBuilder passwordResetUrlBuilder,
        final IdentityProviderHelper identityProviderHelper,
        final ProvidersService providersService,
        @Qualifier(CommunicationsManager.BEAN_NAME) final CommunicationsManager communicationsManager,
        @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
        final PasswordManagementService passwordManagementService) {
        return new ResetPasswordController(casProperties, passwordManagementService, communicationsManager,
            ticketRegistry,
            messageSource, utils, pmTicketFactory(), passwordResetUrlBuilder, identityProviderHelper, providersService,
            new ObjectMapper());
    }

    @Bean
    public Action loadSurrogatesListAction() {
        return StaticEventExecutionAction.SUCCESS;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action mfaSimpleMultifactorSendTokenAction(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
        final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService,
        @Qualifier("mfaSimpleMultifactorTokenCommunicationStrategy")
        final CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy,
        final CasConfigurationProperties casProperties,
        @Qualifier(CommunicationsManager.BEAN_NAME) final CommunicationsManager communicationsManager,
        @Qualifier("mfaSimpleMultifactorBucketConsumer") final BucketConsumer mfaSimpleMultifactorBucketConsumer) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> {
                val simple = casProperties.getAuthn().getMfa().getSimple();
                return new CustomSendTokenAction(
                    communicationsManager, casSimpleMultifactorAuthenticationService, simple,
                    mfaSimpleMultifactorTokenCommunicationStrategy,
                    mfaSimpleMultifactorBucketConsumer, utils);
            })
            .withId(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_SEND_TOKEN)
            .build()
            .get();
    }

    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer() {
        val cfg = new CustomCasSimpleMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, mfaSimpleAuthenticatorFlowRegistry, applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
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
        return new ConsumerExecutionAction(ctx -> {
        });
    }

    @Bean
    @RefreshScope
    public Action delegatedAuthenticationClientFinishLogoutAction() {
        return new ConsumerExecutionAction(ctx -> {
        });
    }

    @Bean
    @RefreshScope
    public Action x509Check() {
        if (x509AuthnEnabled) {
            val sslHeaderName = casProperties.getAuthn().getX509().getSslHeaderName();
            val certificateExtractor =
                new CustomRequestHeaderX509CertificateExtractor(sslHeaderName, x509AuthnMandatory);

            return new X509CertificateCredentialsRequestHeaderAction(
                initialAuthenticationAttemptWebflowEventResolver.getObject(),
                serviceTicketRequestWebflowEventResolver.getObject(),
                adaptiveAuthenticationPolicy.getObject(),
                certificateExtractor, casProperties);
        } else {
            return new StaticEventExecutionAction("error");
        }
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver(
        @Qualifier("selectiveAuthenticationProviderWebflowEventResolver")
        final CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
        @Qualifier("adaptiveAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver,
        @Qualifier("timedAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver,
        @Qualifier("globalAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver,
        @Qualifier("httpRequestAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver httpRequestAuthenticationPolicyWebflowEventResolver,
        @Qualifier("restEndpointAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver,
        @Qualifier("groovyScriptAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver groovyScriptAuthenticationPolicyWebflowEventResolver,
        @Qualifier("scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver,
        @Qualifier("registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier("predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver")
        final CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver,
        @Qualifier("principalAttributeAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier("authenticationAttributeAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier("registeredServiceAuthenticationPolicyWebflowEventResolver")
        final CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver) {

        val resolver = new CustomCasDelegatingWebflowEventResolver(casWebflowConfigurationContext,
            selectiveAuthenticationProviderWebflowEventResolver, x509AuthnMandatory);
        resolver.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(timedAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(globalAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(httpRequestAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(groovyScriptAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver);
        resolver.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(authenticationAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver);
        return resolver;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public OidcRevocationEndpointController oidcRevocationEndpointController(
        @Qualifier(OidcConfigurationContext.BEAN_NAME) final OidcConfigurationContext oidcConfigurationContext) {
        return new CustomOidcRevocationEndpointController(oidcConfigurationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION)
    public Action surrogateInitialAuthenticationAction(final CasConfigurationProperties casProperties) {
        return new CustomSurrogateInitialAuthenticationAction();
    }
}
