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
import fr.gouv.vitamui.cas.passwordless.CustomPasswordlessDetermineDelegatedAuthenticationAction;
import fr.gouv.vitamui.cas.passwordless.CustomVerifyPasswordlessAccountAuthenticationAction;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.webflow.actions.CheckMfaTokenAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomDelegatedClientAuthenticationAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomSendTokenAction;
import fr.gouv.vitamui.cas.webflow.actions.CustomerSelectedAction;
import fr.gouv.vitamui.cas.webflow.actions.DispatcherAction;
import fr.gouv.vitamui.cas.webflow.actions.I18NSendPasswordResetInstructionsAction;
import fr.gouv.vitamui.cas.webflow.actions.ListCustomersAction;
import fr.gouv.vitamui.cas.webflow.actions.TriggerChangePasswordAction;
import fr.gouv.vitamui.cas.webflow.configurer.CustomCasSimpleMultifactorWebflowConfigurer;
import fr.gouv.vitamui.cas.webflow.configurer.CustomLoginWebflowConfigurer;
import fr.gouv.vitamui.cas.x509.CustomRequestHeaderX509CertificateExtractor;
import fr.gouv.vitamui.cas.x509.FixX509WebflowConfigurer;
import fr.gouv.vitamui.cas.x509.X509CasDelegatingWebflowEventResolver;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccountStore;
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
        @Qualifier("delegatedClientDistributedSessionStore") ObjectProvider<
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

    // TODO: Non present into xelians code
    @Bean
    public DefaultTransientSessionTicketFactory pmTicketFactory(final CasConfigurationProperties casProperties) {
        return new DefaultTransientSessionTicketFactory(
            new PmTransientSessionTicketExpirationPolicyBuilder(casProperties)
        );
    }

    // TODO: Check because email generation is not the same than xelians.
    // TODO: Check ticket registry and factory usages
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action sendPasswordResetInstructionsAction(
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME) final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier(
            MultifactorAuthenticationProviderSelector.BEAN_NAME
        ) final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(
            PasswordManagementService.DEFAULT_BEAN_NAME
        ) final PasswordManagementService passwordManagementService,
        @Qualifier(TicketRegistry.BEAN_NAME) final TicketRegistry ticketRegistry,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver,
        @Qualifier(CommunicationsManager.BEAN_NAME) final CommunicationsManager communicationsManager,
        // @Qualifier(TicketFactory.BEAN_NAME) final TicketFactory ticketFactory,
        @Qualifier(PasswordResetUrlBuilder.BEAN_NAME) final PasswordResetUrlBuilder passwordResetUrlBuilder,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper,
        final Utils utils,
        @Qualifier("messageSource") final HierarchicalMessageSource messageSource,
        // @Value("${vitamui.portal.url}") final String vitamuiPortalUrl,
        @Value("${theme.vitamui-platform-name:VITAM-UI}") final String vitamuiPlatformName
        // final CasApi casApi,
        // final CustomersApi customersApi
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
        @Qualifier(
            CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY
        ) final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier(
            CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY
        ) final FlowDefinitionRegistry logoutFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices
    ) {
        final var c = new CustomLoginWebflowConfigurer(
            flowBuilderServices,
            loginFlowRegistry,
            applicationContext,
            casProperties
        );
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public Action delegatedAuthenticationAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(
            DelegatedClientAuthenticationFailureEvaluator.BEAN_NAME
        ) final DelegatedClientAuthenticationFailureEvaluator delegatedClientAuthenticationFailureEvaluator,
        @Qualifier(
            DelegatedClientAuthenticationConfigurationContext.BEAN_NAME
        ) final DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext,
        @Qualifier(
            DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME
        ) final DelegatedClientAuthenticationWebflowManager delegatedClientWebflowManager,
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper,
        final TicketRegistry ticketRegistry,
        final CasApi casApi,
        final Utils utils,
        @Value("${vitamui.portal.url}") final String vitamuiPortalUrl
        // ,@Value("${cas.authn.surrogate.separator}") final String surrogationSeparator
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
        @Qualifier(LogoutManager.DEFAULT_BEAN_NAME) final LogoutManager logoutManager,
        @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER) final CasCookieBuilder ticketGrantingTicketCookieGenerator,
        @Qualifier("warnCookieGenerator") final CasCookieBuilder warnCookieGenerator,
        @Qualifier(CentralAuthenticationService.BEAN_NAME) final CentralAuthenticationService centralAuthenticationService,
        final Utils utils,
        final CasApi casApi,
        final ServicesManager servicesManager,
        final TicketRegistry ticketRegistry,
        @Qualifier(CasWebflowConstants.ACTION_ID_FRONT_CHANNEL_LOGOUT) final Action frontChannelLogoutAction,
        @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME) final SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor
    ) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() ->
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
            CasSimpleMultifactorAuthenticationService.BEAN_NAME
        ) final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService,
        @Qualifier(
            "mfaSimpleMultifactorTokenCommunicationStrategy"
        ) final CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy,
        @Qualifier(CommunicationsManager.BEAN_NAME) final CommunicationsManager communicationsManager,
        @Qualifier("mfaSimpleMultifactorBucketConsumer") final BucketConsumer mfaSimpleMultifactorBucketConsumer,
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
            "mfaSimpleAuthenticatorFlowRegistry"
        ) final FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry,
        @Qualifier(
            CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY
        ) final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext
    ) {
        final var cfg = new CustomCasSimpleMultifactorWebflowConfigurer(
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
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME) final DelegatedIdentityProviders identityProviders,
        @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore,
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
        @Qualifier("adaptiveAuthenticationPolicy") final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier(
            "serviceTicketRequestWebflowEventResolver"
        ) final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier(
            "initialAuthenticationAttemptWebflowEventResolver"
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
            "selectiveAuthenticationProviderWebflowEventResolver"
        ) final CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver,
        @Qualifier(
            "casWebflowConfigurationContext"
        ) final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
        @Qualifier(
            "adaptiveAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver adaptiveAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "timedAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver timedAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "globalAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver globalAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "httpRequestAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver httpRequestAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "restEndpointAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver restEndpointAuthenticationPolicyWebflowEventResolver,
        @Qualifier("groovyScriptAuthenticationPolicyWebflowEventResolver") final ObjectProvider<
            CasWebflowEventResolver
        > groovyScriptAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver"
        ) final CasWebflowEventResolver predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver,
        @Qualifier(
            "principalAttributeAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "authenticationAttributeAuthenticationPolicyWebflowEventResolver"
        ) final CasWebflowEventResolver authenticationAttributeAuthenticationPolicyWebflowEventResolver,
        @Qualifier(
            "registeredServiceAuthenticationPolicyWebflowEventResolver"
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

    /*
        TODO: chez xelians, voir si nécessaire.
     */

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action verifyPasswordlessAccountAuthenticationAction(
        @Qualifier(PasswordlessRequestParser.BEAN_NAME) final PasswordlessRequestParser passwordlessRequestParser,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(
            PasswordlessUserAccountStore.BEAN_NAME
        ) final PasswordlessUserAccountStore passwordlessUserAccountStore
    ) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(
                () ->
                    new CustomVerifyPasswordlessAccountAuthenticationAction(
                        casProperties,
                        passwordlessUserAccountStore,
                        passwordlessRequestParser
                    )
            )
            .withId(CasWebflowConstants.ACTION_ID_VERIFY_PASSWORDLESS_ACCOUNT_AUTHN)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action determineDelegatedAuthenticationAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        final ProvidersService providersService
    ) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(
                () -> new CustomPasswordlessDetermineDelegatedAuthenticationAction(casProperties, providersService)
            )
            .withId(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer x509WebflowConfigurer(
        @Qualifier(
            CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY
        ) final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext
    ) {
        return new FixX509WebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext, casProperties);
    }
}
