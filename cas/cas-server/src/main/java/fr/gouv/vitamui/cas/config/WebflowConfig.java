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

import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.logout.CustomDelegatedAuthenticationClientLogoutAction;
import fr.gouv.vitamui.cas.logout.TerminateApiSessionAction;
import fr.gouv.vitamui.cas.password.PmTransientSessionTicketExpirationPolicyBuilder;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.webflow.login.VitamLoginWebflowConfigurer;
import fr.gouv.vitamui.cas.webflow.login.actions.CheckSubrogationAction;
import fr.gouv.vitamui.cas.webflow.login.actions.CustomDelegatedClientAuthenticationAction;
import fr.gouv.vitamui.cas.webflow.login.actions.CustomerSelectedAction;
import fr.gouv.vitamui.cas.webflow.login.actions.DispatcherAction;
import fr.gouv.vitamui.cas.webflow.login.actions.I18NSendPasswordResetInstructionsAction;
import fr.gouv.vitamui.cas.webflow.login.actions.ListCustomersAction;
import fr.gouv.vitamui.cas.webflow.login.actions.TriggerChangePasswordAction;
import fr.gouv.vitamui.cas.webflow.mfa.VitamMfaWebflowConfigurer;
import fr.gouv.vitamui.cas.webflow.mfa.actions.CheckMfaTokenAction;
import fr.gouv.vitamui.cas.webflow.mfa.actions.CustomSendTokenAction;
import fr.gouv.vitamui.cas.x509.CustomRequestHeaderX509CertificateExtractor;
import fr.gouv.vitamui.cas.x509.X509CasDelegatingWebflowEventResolver;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.services.ServicesManager;
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
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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

    @Bean
    public ListCustomersAction listCustomersAction(
        ProvidersService providersService,
        IdentityProviderHelper identityProviderHelper,
        CasApi casApi
    ) {
        return new ListCustomersAction(providersService, identityProviderHelper, casApi);
    }

    @Bean
    public CustomerSelectedAction customerSelectedAction() {
        return new CustomerSelectedAction();
    }

    @Bean
    public DispatcherAction dispatcherAction(
        ProvidersService providersService,
        IdentityProviderHelper identityProviderHelper,
        CasApi casApi,
        Utils utils,
        @Qualifier(CasBeans.DELEGATED_CLIENT_DISTRIBUTED_SESSION_STORE) ObjectProvider<
            SessionStore
        > delegatedClientDistributedSessionStore
    ) {
        return new DispatcherAction(
            providersService,
            identityProviderHelper,
            casApi,
            utils,
            delegatedClientDistributedSessionStore.getObject()
        );
    }

    @Bean
    public DefaultTransientSessionTicketFactory pmTicketFactory(final CasConfigurationProperties casProperties) {
        return new DefaultTransientSessionTicketFactory(
            new PmTransientSessionTicketExpirationPolicyBuilder(casProperties)
        );
    }

    @Bean
    public Action checkSubrogationAction(final CasApi casApi) {
        return new CheckSubrogationAction(casApi);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action sendPasswordResetInstructionsAction(
        @Qualifier(
            CasBeans.AUTHENTICATION_SYSTEM_SUPPORT
        ) final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier(
            CasBeans.MULTIFACTOR_AUTHENTICATION_PROVIDER_SELECTOR
        ) final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(
            CasBeans.PASSWORD_MANAGEMENT_SERVICE_DEFAULT
        ) final PasswordManagementService passwordManagementService,
        @Qualifier(CasBeans.TICKET_REGISTRY) final TicketRegistry ticketRegistry,
        @Qualifier(CasBeans.PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver,
        @Qualifier(CasBeans.COMMUNICATIONS_MANAGER) final CommunicationsManager communicationsManager,
        @Qualifier(CasBeans.PASSWORD_RESET_URL_BUILDER) final PasswordResetUrlBuilder passwordResetUrlBuilder,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper,
        final Utils utils,
        @Qualifier(CasBeans.MESSAGE_SOURCE) final HierarchicalMessageSource messageSource,
        @Value("${theme.vitamui-platform-name:VITAM-UI}") final String vitamuiPlatformName
    ) {
        final var pmTicketFactory = new DefaultTicketFactory();
        pmTicketFactory.addTicketFactory(TransientSessionTicket.class, pmTicketFactory(casProperties));

        return new I18NSendPasswordResetInstructionsAction(
            casProperties,
            communicationsManager,
            passwordManagementService,
            ticketRegistry,
            pmTicketFactory,
            defaultPrincipalResolver,
            passwordResetUrlBuilder,
            multifactorAuthenticationProviderSelector,
            authenticationSystemSupport,
            applicationContext,
            messageSource,
            providersService,
            identityProviderHelper,
            utils,
            vitamuiPlatformName
        );
    }

    @Bean
    public TriggerChangePasswordAction triggerChangePasswordAction(
        TicketRegistrySupport ticketRegistrySupport,
        Utils utils
    ) {
        return new TriggerChangePasswordAction(ticketRegistrySupport, utils);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer defaultWebflowConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasBeans.LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier(CasBeans.LOGOUT_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry logoutFlowRegistry,
        @Qualifier(CasBeans.FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices
    ) {
        final var c = new VitamLoginWebflowConfigurer(
            flowBuilderServices,
            loginFlowRegistry,
            applicationContext,
            casProperties
        );
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
        c.setOrder(10);
        return c;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public Action delegatedAuthenticationAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_AUTHENTICATION_FAILURE_EVALUATOR
        ) final DelegatedClientAuthenticationFailureEvaluator delegatedClientAuthenticationFailureEvaluator,
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_AUTHENTICATION_CONFIGURATION_CONTEXT
        ) final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_AUTHENTICATION_WEBFLOW_MANAGER
        ) final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper,
        final TicketRegistry ticketRegistry,
        final CasApi casApi,
        final Utils utils,
        @Value("${vitamui.portal.url}") final String vitamuiPortalUrl
    ) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(
                () ->
                    new CustomDelegatedClientAuthenticationAction(
                        delegatedClientAuthenticationConfigurationContext,
                        delegatedClientWebflowManager,
                        delegatedClientAuthenticationFailureEvaluator,
                        identityProviderHelper,
                        providersService,
                        utils,
                        ticketRegistry,
                        casApi,
                        vitamuiPortalUrl
                    )
            )
            .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action terminateSessionAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasBeans.LOGOUT_MANAGER) final LogoutManager logoutManager,
        @Qualifier(CasBeans.TICKET_GRANTING_COOKIE_BUILDER) final CasCookieBuilder ticketGrantingTicketCookieGenerator,
        @Qualifier(CasBeans.WARN_COOKIE_GENERATOR) final CasCookieBuilder warnCookieGenerator,
        @Qualifier(
            CasBeans.CENTRAL_AUTHENTICATION_SERVICE
        ) final CentralAuthenticationService centralAuthenticationService,
        final Utils utils,
        final CasApi casApi,
        final ServicesManager servicesManager,
        final TicketRegistry ticketRegistry,
        @Qualifier(CasBeans.FRONT_CHANNEL_LOGOUT_ACTION) final Action frontChannelLogoutAction,
        @Qualifier(
            CasBeans.SINGLE_LOGOUT_REQUEST_EXECUTOR
        ) final SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor
    ) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(
                () ->
                    new TerminateApiSessionAction(
                        centralAuthenticationService,
                        ticketGrantingTicketCookieGenerator,
                        warnCookieGenerator,
                        casProperties.getLogout(),
                        logoutManager,
                        applicationContext,
                        utils,
                        casApi,
                        servicesManager,
                        casProperties,
                        frontChannelLogoutAction,
                        ticketRegistry,
                        defaultSingleLogoutRequestExecutor
                    )
            )
            .withId(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
            .build()
            .get();
    }

    @Bean
    public Action loadSurrogatesListAction() {
        return StaticEventExecutionAction.SUCCESS;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action mfaSimpleMultifactorSendTokenAction(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(
            CasBeans.CAS_SIMPLE_MULTIFACTOR_AUTHENTICATION_SERVICE
        ) final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService,
        @Qualifier(
            CasBeans.MFA_SIMPLE_MULTIFACTOR_TOKEN_COMMUNICATION_STRATEGY
        ) final CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy,
        @Qualifier(CasBeans.COMMUNICATIONS_MANAGER) final CommunicationsManager communicationsManager,
        @Qualifier(
            CasBeans.MFA_SIMPLE_MULTIFACTOR_BUCKET_CONSUMER
        ) final BucketConsumer mfaSimpleMultifactorBucketConsumer,
        final CasConfigurationProperties casProperties,
        final Utils utils
    ) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> {
                var simple = casProperties.getAuthn().getMfa().getSimple();
                return new CustomSendTokenAction(
                    communicationsManager,
                    casSimpleMultifactorAuthenticationService,
                    simple,
                    mfaSimpleMultifactorTokenCommunicationStrategy,
                    mfaSimpleMultifactorBucketConsumer,
                    utils
                );
            })
            .withId(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_SEND_TOKEN)
            .build()
            .get();
    }

    @Bean
    @DependsOn("defaultWebflowConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer(
        @Qualifier(
            CasBeans.MFA_SIMPLE_AUTHENTICATOR_FLOW_REGISTRY
        ) final FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry,
        @Qualifier(CasBeans.LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier(CasBeans.FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext
    ) {
        final var cfg = new VitamMfaWebflowConfigurer(
            flowBuilderServices,
            loginFlowRegistry,
            mfaSimpleAuthenticatorFlowRegistry,
            applicationContext,
            casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext)
        );
        cfg.setOrder(100);
        return cfg;
    }

    @Bean
    public Action checkMfaTokenAction(final TicketRegistry ticketRegistry) {
        return new CheckMfaTokenAction(ticketRegistry);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action delegatedAuthenticationClientLogoutAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasBeans.DELEGATED_IDENTITY_PROVIDERS) final DelegatedIdentityProviders identityProviders,
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_DISTRIBUTED_SESSION_STORE
        ) final SessionStore delegatedClientDistributedSessionStore,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper
    ) {
        return BeanSupplier.of(Action.class)
            .when(
                BeanCondition.on("cas.slo.disabled")
                    .isFalse()
                    .evenIfMissing()
                    .given(applicationContext.getEnvironment())
            )
            .supply(
                () ->
                    WebflowActionBeanSupplier.builder()
                        .withApplicationContext(applicationContext)
                        .withProperties(casProperties)
                        .withAction(
                            () ->
                                new CustomDelegatedAuthenticationClientLogoutAction(
                                    identityProviders,
                                    delegatedClientDistributedSessionStore,
                                    providersService,
                                    identityProviderHelper
                                )
                        )
                        .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT)
                        .build()
                        .get()
            )
            .otherwise(() -> ConsumerExecutionAction.NONE)
            .get();
    }

    @Bean
    @RefreshScope
    public Action x509Check(
        final CasConfigurationProperties casProperties,
        @Qualifier(
            CasBeans.ADAPTIVE_AUTHENTICATION_POLICY
        ) final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier(
            CasBeans.SERVICE_TICKET_REQUEST_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier(
            CasBeans.INITIAL_AUTHENTICATION_ATTEMPT_WEBFLOW_EVENT_RESOLVER
        ) final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Value("${vitamui.authn.x509.enabled:false}") final boolean x509AuthnEnabled,
        @Value("${vitamui.authn.x509.mandatory:false}") final boolean x509AuthnMandatory
    ) {
        if (x509AuthnEnabled) {
            val sslHeaderName = casProperties.getAuthn().getX509().getSslHeaderName();
            val certificateExtractor = new CustomRequestHeaderX509CertificateExtractor(
                sslHeaderName,
                x509AuthnMandatory
            );

            return new X509CertificateCredentialsRequestHeaderAction(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                certificateExtractor,
                casProperties
            );
        } else {
            return new StaticEventExecutionAction("error");
        }
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver(
        @Qualifier(
            CasBeans.SELECTIVE_AUTHENTICATION_PROVIDER_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver,
        @Qualifier(
            CasBeans.CAS_WEBFLOW_CONFIGURATION_CONTEXT
        ) final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
        @Qualifier(
            CasBeans.ADAPTIVE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.TIMED_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.GLOBAL_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.HTTP_REQUEST_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver httpRequestAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.REST_ENDPOINT_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver,
        @Qualifier(CasBeans.GROOVY_SCRIPT_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER) final ObjectProvider<
            CasWebflowEventResolver
        > groovyScriptAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.SCRIPTED_REGISTERED_SERVICE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.REGISTERED_SERVICE_PRINCIPAL_ATTRIBUTE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.PREDICATED_PRINCIPAL_ATTRIBUTE_MULTIFACTOR_AUTHENTICATION_POLICY_EVENT_RESOLVER
        ) final CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver,
        @Qualifier(
            CasBeans.PRINCIPAL_ATTRIBUTE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.AUTHENTICATION_ATTRIBUTE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            CasBeans.REGISTERED_SERVICE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver,
        @Value("${vitamui.authn.x509.mandatory:false}") final boolean x509AuthnMandatory
    ) {
        final var resolver = new X509CasDelegatingWebflowEventResolver(
            casWebflowConfigurationContext,
            selectiveAuthenticationProviderWebflowEventResolver,
            x509AuthnMandatory
        );
        resolver.addDelegate(adaptiveAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(timedAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(globalAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(httpRequestAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(restEndpointAuthenticationPolicyWebflowEventResolver);
        groovyScriptAuthenticationPolicyWebflowEventResolver.ifAvailable(resolver::addDelegate);
        resolver.addDelegate(scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver);
        resolver.addDelegate(principalAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(authenticationAttributeAuthenticationPolicyWebflowEventResolver);
        resolver.addDelegate(registeredServiceAuthenticationPolicyWebflowEventResolver);
        return resolver;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION)
    public Action surrogateInitialAuthenticationAction() {
        return new CustomSurrogateInitialAuthenticationAction();
    }
}
