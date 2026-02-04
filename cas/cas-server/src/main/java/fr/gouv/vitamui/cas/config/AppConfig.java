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

import fr.gouv.vitamui.cas.authentication.LoginPwdAuthenticationHandler;
import fr.gouv.vitamui.cas.authentication.UserPrincipalResolver;
import fr.gouv.vitamui.cas.delegation.CustomDelegatedIdentityProviders;
import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.password.IamPasswordManagementService;
import fr.gouv.vitamui.cas.passwordless.CustomPasswordlessUserAccountStore;
import fr.gouv.vitamui.cas.surrogation.IamSurrogateAuthenticationService;
import fr.gouv.vitamui.cas.ticket.CustomOAuth20DefaultAccessTokenFactory;
import fr.gouv.vitamui.cas.ticket.DynamicTicketGrantingTicketFactory;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.x509.X509AttributeMapping;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Pac4jClientBuilder;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import fr.gouv.vitamui.iam.openapiclient.CustomersApi;
import fr.gouv.vitamui.iam.openapiclient.IamApiClientsFactory;
import fr.gouv.vitamui.iam.openapiclient.IdentityProvidersApi;
import io.micrometer.observation.ObservationRegistry;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationPostProcessor;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.data.mongodb.observability.ContextProviderFactory;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.commons.api.CommonConstants.EMAIL_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;

/**
 * Configure all beans to customize the CAS server.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(
    { CasConfigurationProperties.class, IamClientConfigurationProperties.class, PasswordConfiguration.class }
)
public class AppConfig extends BaseTicketCatalogConfigurer {

    // overrides the CAS specific message converter to prevent
    // the CasRestExternalClient to use the
    // 'application/vnd.cas.services+yaml;charset=UTF-8'
    // content type and to fail
    @Bean
    public HttpMessageConverter yamlHttpMessageConverter() {
        return null;
    }

    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @Bean
    public LoginPwdAuthenticationHandler loginPwdAuthenticationHandler(
        final CasApi casApi,
        @Value("${ip.header}") final String ipHeaderName,
        @Qualifier("principalFactory") final PrincipalFactory principalFactory,
        @Qualifier("servicesManager") final ServicesManager servicesManager
    ) {
        return new LoginPwdAuthenticationHandler(servicesManager, principalFactory, casApi, ipHeaderName);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalResolver defaultPrincipalResolver(
        @Value("${vitamui.authn.x509.emailAttribute:}") final String x509EmailAttribute,
        @Value("${vitamui.authn.x509.emailAttributeParsing:}") final String x509EmailAttributeParsing,
        @Value("${vitamui.authn.x509.emailAttributeExpansion:}") final String x509EmailAttributeExpansion,
        @Value("${vitamui.authn.x509.identifierAttribute:}") final String x509IdentifierAttribute,
        @Value("${vitamui.authn.x509.identifierAttributeParsing:}") final String x509IdentifierAttributeParsing,
        @Value("${vitamui.authn.x509.identifierAttributeExpansion:}") final String x509IdentifierAttributeExpansion,
        @Value("${vitamui.authn.x509.defaultDomain:}") final String x509DefaultDomain,
        @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore,
        @Qualifier(PrincipalFactory.BEAN_NAME) PrincipalFactory principalFactory,
        final ProvidersService providersService,
        final CasApi casApi
    ) {
        final var emailMapping = new X509AttributeMapping(
            x509EmailAttribute,
            x509EmailAttributeParsing,
            x509EmailAttributeExpansion
        );
        final var identifierMapping = new X509AttributeMapping(
            x509IdentifierAttribute,
            x509IdentifierAttributeParsing,
            x509IdentifierAttributeExpansion
        );
        return new UserPrincipalResolver(
            principalFactory,
            casApi,
            delegatedClientDistributedSessionStore,
            identityProviderHelper(),
            providersService,
            emailMapping,
            identifierMapping,
            x509DefaultDomain
        );
    }

    @Bean
    public AuthenticationEventExecutionPlanConfigurer registerInternalHandler(
        final LoginPwdAuthenticationHandler loginPwdAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver") final PrincipalResolver defaultPrincipalResolver
    ) {
        return plan ->
            plan.registerAuthenticationHandlerWithPrincipalResolver(
                loginPwdAuthenticationHandler,
                defaultPrincipalResolver
            );
    }

    @Bean
    @RefreshScope
    public PrincipalResolver surrogatePrincipalResolver(
        @Qualifier("defaultPrincipalResolver") final PrincipalResolver defaultPrincipalResolver
    ) {
        return defaultPrincipalResolver;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectDNPrincipalResolver(
        @Qualifier("defaultPrincipalResolver") final PrincipalResolver defaultPrincipalResolver
    ) {
        return defaultPrincipalResolver;
    }

    @Bean
    public IamApiClientsFactory iamApiClientsFactory(
        final IamClientConfigurationProperties iamClientProperties,
        final RestTemplateBuilder restTemplateBuilder,
        @Qualifier("restTemplateCustomizer") final RestTemplateCustomizer restTemplateCustomizer
    ) {
        return new IamApiClientsFactory(
            iamClientProperties,
            restTemplateBuilder.additionalCustomizers(restTemplateCustomizer)
        );
    }

    @Bean
    public CasApi casApi(final IamApiClientsFactory iamApiClientsFactory) {
        return iamApiClientsFactory.getCasApi();
    }

    @Bean
    public CustomersApi customersApi(final IamApiClientsFactory iamApiClientsFactory) {
        return iamApiClientsFactory.getCustomersApi();
    }

    @Bean
    public IdentityProvidersApi identityProvidersApi(final IamApiClientsFactory iamApiClientsFactory) {
        return iamApiClientsFactory.getIdentityProvidersApi();
    }

    @Bean
    @RefreshScope
    public Clients builtClients(final CasConfigurationProperties casProperties) {
        return new Clients(casProperties.getServer().getLoginUrl());
    }

    @Bean
    public ProvidersService providersService(
        final Clients builtClients,
        final IdentityProvidersApi identityProvidersApi,
        final Pac4jClientBuilder pac4jClientBuilder
    ) {
        return new ProvidersService(builtClients, identityProvidersApi, pac4jClientBuilder);
    }

    @Bean
    public Pac4jClientBuilder pac4jClientBuilder() {
        return new Pac4jClientBuilder();
    }

    @Bean
    public IdentityProviderHelper identityProviderHelper() {
        return new IdentityProviderHelper();
    }

    @Bean
    public Utils utils(
        @Value("${cas_secret_token}") @NotNull final String tokenApiCas,
        @Value("${vitamui.cas.tenant.identifier}") final Integer casTenantIdentifier,
        @Value("${vitamui.cas.identity}") final String casIdentity,
        final JavaMailSender mailSender,
        final CasConfigurationProperties casProperties
        // TODO: Voir ce qui change entre nous et xelians
        // , final TicketRegistry ticketRegistry,
        // @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER) final
        // CasCookieBuilder ticketGrantingTicketCookieGenerator
    ) {
        return new Utils(
            tokenApiCas,
            casTenantIdentifier,
            casIdentity,
            mailSender,
            casProperties.getServer().getPrefix()
        );
    }

    // TODO: Seems no required in cas v7
    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory(
        @Qualifier(ServicesManager.BEAN_NAME) ServicesManager servicesManager,
        @Qualifier(
            "ticketGrantingTicketUniqueIdGenerator"
        ) final UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator,
        @Qualifier("grantingTicketExpirationPolicy") final ObjectProvider<
            ExpirationPolicyBuilder
        > grantingTicketExpirationPolicy,
        @Qualifier("protocolTicketCipherExecutor") final CipherExecutor protocolTicketCipherExecutor,
        final Utils utils
    ) {
        return new DynamicTicketGrantingTicketFactory(
            ticketGrantingTicketUniqueIdGenerator,
            grantingTicketExpirationPolicy.getObject(),
            protocolTicketCipherExecutor,
            servicesManager,
            utils
        );
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OAuth20AccessTokenFactory defaultAccessTokenFactory(
        @Qualifier("accessTokenIdGenerator") final UniqueTicketIdGenerator accessTokenIdGenerator,
        @Qualifier("accessTokenExpirationPolicy") final ExpirationPolicyBuilder accessTokenExpirationPolicy,
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
        @Qualifier("accessTokenJwtBuilder") final JwtBuilder accessTokenJwtBuilder,
        @Qualifier(
            TicketTrackingPolicy.BEAN_NAME_DESCENDANT_TICKET_TRACKING
        ) final TicketTrackingPolicy descendantTicketsTrackingPolicy
    ) {
        return new CustomOAuth20DefaultAccessTokenFactory(
            accessTokenIdGenerator,
            accessTokenExpirationPolicy,
            accessTokenJwtBuilder,
            servicesManager,
            descendantTicketsTrackingPolicy
        );
    }

    @Override
    public void configureTicketCatalog(final TicketCatalog plan, final CasConfigurationProperties casProperties) {
        final TicketDefinition metadata = buildTicketDefinition(
            plan,
            "TOK",
            OAuth20AccessToken.class,
            OAuth20DefaultAccessToken.class,
            Ordered.HIGHEST_PRECEDENCE
        );
        metadata.getProperties().setStorageName(casProperties.getAuthn().getOauth().getAccessToken().getStorageName());
        final var timeout = Beans.newDuration(
            casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds()
        ).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(casProperties.getTicket().isTrackDescendantTickets());
        registerTicketDefinition(plan, metadata);
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    public SurrogateAuthenticationService surrogateAuthenticationService(
        final CasApi casApi,
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager
    ) {
        return new IamSurrogateAuthenticationService(casApi, servicesManager);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PasswordManagementService passwordChangeService(
        final CasConfigurationProperties casProperties,
        @Qualifier("passwordManagementCipherExecutor") final CipherExecutor passwordManagementCipherExecutor,
        @Qualifier(PasswordHistoryService.BEAN_NAME) final PasswordHistoryService passwordHistoryService,
        final ProvidersService providersService,
        final TicketRegistry ticketRegistry,
        final CasApi casApi,
        final IdentityProviderHelper identityProviderHelper,
        final Utils utils,
        final PasswordValidator passwordValidator,
        @Qualifier("centralAuthenticationService") final CentralAuthenticationService centralAuthenticationService,
        final PasswordConfiguration passwordConfiguration
    ) {
        return new IamPasswordManagementService(
            casProperties.getAuthn().getPm(),
            passwordManagementCipherExecutor,
            casProperties.getServer().getPrefix(),
            passwordHistoryService,
            casApi,
            providersService,
            identityProviderHelper,
            centralAuthenticationService,
            utils,
            ticketRegistry,
            passwordValidator,
            passwordConfiguration
        );
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy() {
        return new CasSimpleMultifactorTokenCommunicationStrategy() {
            @Override
            public EnumSet<TokenSharingStrategyOptions> determineStrategy(
                final CasSimpleMultifactorAuthenticationTicket token
            ) {
                return EnumSet.of(TokenSharingStrategyOptions.SMS);
            }
        };
    }

    @Bean
    public ServletContextInitializer servletContextInitializer(
        @Value("${theme.vitamui-logo-large:#{null}}") final String vitamuiLargeLogoPath,
        @Value("${theme.vitamui-favicon:#{null}}") final String vitamuiFaviconPath
    ) {
        return new InitContextConfiguration(vitamuiLargeLogoPath, vitamuiFaviconPath);
    }

    @Bean
    public ServletContextInitializer servletPasswordContextInitializer() {
        return new InitPasswordConstraintsConfiguration();
    }

    @Bean
    public PasswordlessUserAccountStore passwordlessUserAccountStore(
        final ProvidersService providersService,
        final IdentityProviderHelper identityProviderHelper,
        final CasApi casApi
    ) {
        return new CustomPasswordlessUserAccountStore(providersService, identityProviderHelper, casApi);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer passwordManagementAuthenticationExecutionPlanConfigurer() {
        return plan -> {};
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public DelegatedIdentityProviders delegatedIdentityProviders(final ProvidersService providersService) {
        return new CustomDelegatedIdentityProviders(providersService);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedClientAuthenticationConfigurationContext delegatedClientAuthenticationConfigurationContext(
        @Qualifier(
            SingleLogoutRequestExecutor.BEAN_NAME
        ) final SingleLogoutRequestExecutor defaultSingleLogoutRequestExecutor,
        @Qualifier(
            AuditableExecution.AUDITABLE_EXECUTION_DELEGATED_AUTHENTICATION_ACCESS
        ) final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
        @Qualifier(
            "serviceTicketRequestWebflowEventResolver"
        ) final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier(
            "initialAuthenticationAttemptWebflowEventResolver"
        ) final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier("adaptiveAuthenticationPolicy") final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        final CasConfigurationProperties casProperties,
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME) final DelegatedIdentityProviders identityProviders,
        @Qualifier(
            DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME
        ) final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer,
        @Qualifier(
            "delegatedClientIdentityProviderConfigurationPostProcessor"
        ) final DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor,
        @Qualifier(
            "delegatedClientDistributedSessionCookieGenerator"
        ) final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator,
        @Qualifier(
            CentralAuthenticationService.BEAN_NAME
        ) final CentralAuthenticationService centralAuthenticationService,
        @Qualifier(
            "pac4jDelegatedClientNameExtractor"
        ) final DelegatedClientNameExtractor pac4jDelegatedClientNameExtractor,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME) final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier(ArgumentExtractor.BEAN_NAME) final ArgumentExtractor argumentExtractor,
        @Qualifier(TicketRegistry.BEAN_NAME) final TicketRegistry ticketRegistry,
        @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore,
        @Qualifier(TicketFactory.BEAN_NAME) final TicketFactory ticketFactory,
        @Qualifier(
            AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS
        ) final AuditableExecution registeredServiceAccessStrategyEnforcer,
        @Qualifier(
            "delegatedClientIdentityProviderRedirectionStrategy"
        ) final DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy,
        @Qualifier(
            SingleSignOnParticipationStrategy.BEAN_NAME
        ) final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
        @Qualifier(
            AuthenticationServiceSelectionPlan.BEAN_NAME
        ) final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
        @Qualifier(
            "delegatedAuthenticationCookieGenerator"
        ) final CasCookieBuilder delegatedAuthenticationCookieGenerator,
        @Qualifier(
            "delegatedAuthenticationCredentialExtractor"
        ) final DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(LogoutExecutionPlan.BEAN_NAME) final LogoutExecutionPlan logoutExecutionPlan,
        final ObjectProvider<List<DelegatedClientAuthenticationRequestCustomizer>> customizersProvider,
        final List<DelegatedClientIdentityProviderAuthorizer> delegatedClientIdentityProviderAuthorizers // TODO: Vérifier pourquoi on arrive pas à instancier 1 seul authorizer
    ) {
        final var customizers = Optional.ofNullable(customizersProvider.getIfAvailable())
            .orElseGet(ArrayList::new)
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .collect(Collectors.toList());

        final var authorizers = delegatedClientIdentityProviderAuthorizers;

        return DelegatedClientAuthenticationConfigurationContext.builder()
            .credentialExtractor(delegatedAuthenticationCredentialExtractor)
            .initialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver)
            .serviceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver)
            .adaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy)
            .identityProviders(identityProviders)
            .ticketRegistry(ticketRegistry)
            .applicationContext(applicationContext)
            .servicesManager(servicesManager)
            .delegatedAuthenticationPolicyEnforcer(registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer)
            .authenticationSystemSupport(authenticationSystemSupport)
            .casProperties(casProperties)
            .centralAuthenticationService(centralAuthenticationService)
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies)
            .singleSignOnParticipationStrategy(webflowSingleSignOnParticipationStrategy)
            .sessionStore(delegatedClientDistributedSessionStore)
            .argumentExtractor(argumentExtractor)
            .ticketFactory(ticketFactory)
            .delegatedClientIdentityProvidersProducer(delegatedClientIdentityProviderConfigurationProducer)
            .delegatedClientIdentityProviderConfigurationPostProcessor(
                delegatedClientIdentityProviderConfigurationPostProcessor
            )
            .delegatedClientCookieGenerator(delegatedAuthenticationCookieGenerator)
            .delegatedClientDistributedSessionCookieGenerator(delegatedClientDistributedSessionCookieGenerator)
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer)
            .delegatedClientAuthenticationRequestCustomizers(customizers)
            .delegatedClientNameExtractor(pac4jDelegatedClientNameExtractor)
            .delegatedClientIdentityProviderAuthorizers(authorizers)
            .delegatedClientIdentityProviderRedirectionStrategy(delegatedClientIdentityProviderRedirectionStrategy)
            .singleLogoutRequestExecutor(defaultSingleLogoutRequestExecutor)
            .logoutExecutionPlan(logoutExecutionPlan)
            .build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedAuthenticationPreProcessor surrogateDelegatedAuthenticationPreProcessor() {
        return (principal, client, credential, service) -> principal;
    }

    @Bean
    MongoClientSettingsBuilderCustomizer mongoMetricsSynchronousContextProvider(ObservationRegistry registry) {
        return clientSettingsBuilder ->
            clientSettingsBuilder
                .contextProvider(ContextProviderFactory.create(registry))
                .addCommandListener(new MongoObservationCommandListener(registry));
    }

    /**
     * Ne fonctionne pas entièrement, nécessite un équivalent complet pour la
     * nouvelle API.
     *
     * TODO: A remplacer ou améloirer.
     *
     * @param utils
     * @return
     */
    @Bean
    @Qualifier("restTemplateCustomizer")
    public RestTemplateCustomizer restTemplateCustomizer(final Utils utils) {
        return restTemplate ->
            restTemplate
                .getInterceptors()
                .add((request, body, execution) -> {
                    final var context = SecurityContextHolder.getContext();
                    final boolean hasPrincipal =
                        context != null &&
                        context.getAuthentication() != null &&
                        context.getAuthentication().getPrincipal() != null;
                    final HttpContext httpContext;

                    if (hasPrincipal) {
                        Object principal = context.getAuthentication().getPrincipal();
                        if (principal instanceof Principal principalObj) {
                            final Map<String, List<Object>> attributes = principalObj.getAttributes();
                            final String principalEmail = (String) utils.getAttributeValue(attributes, EMAIL_ATTRIBUTE);
                            final String superUserEmail = (String) utils.getAttributeValue(
                                attributes,
                                SUPER_USER_ATTRIBUTE
                            );
                            final String superUserCustomerId = (String) utils.getAttributeValue(
                                attributes,
                                SUPER_USER_CUSTOMER_ID_ATTRIBUTE
                            );
                            if (StringUtils.isNotBlank(superUserCustomerId)) {
                                httpContext = utils.buildContext(superUserEmail);
                            } else {
                                httpContext = utils.buildContext(principalEmail);
                            }
                        } else if (principal instanceof String principalString) {
                            httpContext = utils.buildContext(principalString);
                        } else {
                            httpContext = utils.buildContext("admin@change-it.fr");
                        }
                    } else {
                        httpContext = utils.buildContext("admin@change-it.fr");
                    }

                    if (httpContext.getUserToken() != null) {
                        request.getHeaders().add(CommonConstants.X_USER_TOKEN_HEADER, httpContext.getUserToken());
                    }
                    if (httpContext.getTenantIdentifier() != null) {
                        request
                            .getHeaders()
                            .add(CommonConstants.X_TENANT_ID_HEADER, httpContext.getTenantIdentifier().toString());
                    }
                    if (httpContext.getRequestId() != null) {
                        request.getHeaders().add(CommonConstants.X_REQUEST_ID_HEADER, httpContext.getRequestId());
                    }
                    if (httpContext.getApplicationId() != null) {
                        request
                            .getHeaders()
                            .add(CommonConstants.X_APPLICATION_ID_HEADER, httpContext.getApplicationId());
                    }

                    // Hack for CAS - CAS is considered as an external server requiring proper roles
                    request
                        .getHeaders()
                        .add(CommonConstants.X_ORIGIN_HEADER_NAME, CommonConstants.X_ORIGIN_HEADER_EXTERNAL);

                    return execution.execute(request, body);
                });
    }
}
