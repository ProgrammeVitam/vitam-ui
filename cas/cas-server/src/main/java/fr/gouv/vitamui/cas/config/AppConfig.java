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
import fr.gouv.vitamui.cas.surrogation.IamSurrogateAuthenticationService;
import fr.gouv.vitamui.cas.ticket.CustomOAuth20DefaultAccessTokenFactory;
import fr.gouv.vitamui.cas.ticket.DynamicTicketGrantingTicketFactory;
import fr.gouv.vitamui.cas.util.IamApiDecorator;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.x509.X509AttributeMapping;
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
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
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
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.data.mongodb.observability.ContextProviderFactory;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.commons.api.CommonConstants.X_ORIGIN_HEADER_EXTERNAL;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_ORIGIN_HEADER_NAME;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_USER_TOKEN_HEADER;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_XSRF_TOKEN_HEADER;

/**
 * Configure all beans to customize the CAS server.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(
    {
        CasConfigurationProperties.class,
        IamClientConfigurationProperties.class,
        PasswordConfiguration.class,
        // MailSenderAutoConfiguration normally registers these; CAS 7.3 excludes it, see javaMailSender below.
        MailProperties.class,
    }
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

    /**
     * CAS 7.3 added MailSenderAutoConfiguration to the list its CasWebApplication excludes, so Spring Boot no
     * longer builds a JavaMailSender from spring.mail.* and the context fails on Utils, which needs one. This
     * rebuilds it the way MailSenderPropertiesConfiguration used to.
     *
     * <p>Utils tolerates a null sender and simply logs, so leaving the dependency optional would have turned every
     * password reset and MFA message into a silent no-op instead of a startup failure.
     */
    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(final MailProperties mailProperties) {
        final var sender = new JavaMailSenderImpl();
        sender.setHost(mailProperties.getHost());
        if (mailProperties.getPort() != null) {
            sender.setPort(mailProperties.getPort());
        }
        sender.setUsername(mailProperties.getUsername());
        sender.setPassword(mailProperties.getPassword());
        sender.setProtocol(mailProperties.getProtocol());
        if (mailProperties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
        }
        if (!mailProperties.getProperties().isEmpty()) {
            final var javaMailProperties = new Properties();
            javaMailProperties.putAll(mailProperties.getProperties());
            sender.setJavaMailProperties(javaMailProperties);
        }
        return sender;
    }

    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @Bean
    public LoginPwdAuthenticationHandler loginPwdAuthenticationHandler(
        final CasApi casApi,
        @Value("${ip.header}") final String ipHeaderName,
        @Qualifier(CasBeans.PRINCIPAL_FACTORY) final PrincipalFactory principalFactory
    ) {
        return new LoginPwdAuthenticationHandler(principalFactory, casApi, ipHeaderName);
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
        // In CAS 7.3 accessTokenJwtBuilder depends on the principal resolver, which closes a cycle:
        //   accessTokenJwtBuilder -> defaultPrincipalResolver -> delegatedClientDistributedSessionStore
        //     -> defaultTicketFactory -> defaultAccessTokenFactoryConfigurer -> defaultAccessTokenFactory
        //       -> accessTokenJwtBuilder
        // The session store is only read while resolving a principal, never during construction, so injecting it
        // lazily breaks the cycle where it costs nothing.
        @Lazy @Qualifier(
            CasBeans.DELEGATED_CLIENT_DISTRIBUTED_SESSION_STORE
        ) final SessionStore delegatedClientDistributedSessionStore,
        @Qualifier(CasBeans.PRINCIPAL_FACTORY) PrincipalFactory principalFactory,
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
        // Injected as the interface: CAS 7.3 advises authentication handlers, so this arrives as a JDK dynamic
        // proxy that cannot be cast back to the implementation class.
        @Qualifier("loginPwdAuthenticationHandler") final AuthenticationHandler loginPwdAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver
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
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver
    ) {
        return defaultPrincipalResolver;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectDNPrincipalResolver(
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver
    ) {
        return defaultPrincipalResolver;
    }

    private static final String MASKED_HEADER_VALUE = "***";

    /**
     * Headers whose value is a credential and must never reach the logs. The IAM service account token is
     * long-lived, so a single DEBUG line is enough to leak a credential that stays replayable.
     */
    private static final Set<String> SENSITIVE_HEADERS = sensitiveHeaders();

    private static Set<String> sensitiveHeaders() {
        // HTTP header names are case-insensitive, so the lookup must be too.
        final Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(List.of(X_USER_TOKEN_HEADER, X_XSRF_TOKEN_HEADER, HttpHeaders.AUTHORIZATION, HttpHeaders.COOKIE));
        return Collections.unmodifiableSet(names);
    }

    /**
     * Copies the headers, replacing the value of every credential-bearing one. The remaining headers
     * (tenant, application, identity, trace) are what makes the log line useful, so they are kept as is.
     *
     * @param headers the outgoing request headers.
     * @return a copy safe to log.
     */
    private static HttpHeaders maskSensitiveHeaders(final HttpHeaders headers) {
        final HttpHeaders masked = new HttpHeaders();
        headers.forEach(
            (name, values) ->
                masked.addAll(name, SENSITIVE_HEADERS.contains(name) ? List.of(MASKED_HEADER_VALUE) : values)
        );
        return masked;
    }

    /**
     * We must define our customizer to replace X_ORIGIN header from
     * IamApiClient.java for CAS usage.
     *
     * <p>Deliberately not a bean: Spring Boot applies every RestClientCustomizer bean to the auto-configured
     * RestClient.Builder, so exposing it made the interceptor run twice on each IAM call and forced the
     * EXTERNAL origin on every other rest client built from that builder.
     *
     * @return a rest client customizer for the IAM clients.
     */
    private RestClientCustomizer iamRestClientCustomizer() {
        return builder ->
            builder.requestInterceptor((request, body, execution) -> {
                request.getHeaders().set(X_ORIGIN_HEADER_NAME, X_ORIGIN_HEADER_EXTERNAL);

                LOGGER.debug(
                    "Final request URI: {}, headers: {}",
                    request.getURI(),
                    maskSensitiveHeaders(request.getHeaders())
                );

                return execution.execute(request, body);
            });
    }

    @Bean
    public IamApiClientsFactory iamApiClientsFactory(
        final IamClientConfigurationProperties iamClientProperties,
        final RestClient.Builder restClientBuilder
    ) {
        iamRestClientCustomizer().customize(restClientBuilder);

        return new IamApiClientsFactory(iamClientProperties, restClientBuilder);
    }

    @Bean
    public IamApiDecorator iamApiDecorator(Utils utils) {
        return new IamApiDecorator(utils);
    }

    @Bean
    public CasApi casApi(final IamApiClientsFactory iamApiClientsFactory, final IamApiDecorator iamApiDecorator) {
        return iamApiDecorator.decorate(iamApiClientsFactory.getCasApi());
    }

    @Bean
    public CustomersApi customersApi(
        final IamApiClientsFactory iamApiClientsFactory,
        final IamApiDecorator iamApiDecorator
    ) {
        return iamApiDecorator.decorate(iamApiClientsFactory.getCustomersApi());
    }

    @Bean
    public IdentityProvidersApi identityProvidersApi(
        final IamApiClientsFactory iamApiClientsFactory,
        final IamApiDecorator iamApiDecorator
    ) {
        return iamApiDecorator.decorate(iamApiClientsFactory.getIdentityProvidersApi());
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
    ) {
        return new Utils(
            tokenApiCas,
            casTenantIdentifier,
            casIdentity,
            mailSender,
            casProperties.getServer().getPrefix()
        );
    }

    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory(
        @Qualifier(CasBeans.SERVICES_MANAGER) ServicesManager servicesManager,
        @Qualifier(CasBeans.TGT_ID_GENERATOR) final UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator,
        @Qualifier(CasBeans.TGT_EXPIRATION_POLICY) final ObjectProvider<
            ExpirationPolicyBuilder
        > grantingTicketExpirationPolicy,
        @Qualifier(CasBeans.PROTOCOL_TICKET_CIPHER_EXECUTOR) final CipherExecutor protocolTicketCipherExecutor,
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
        @Qualifier(CasBeans.TICKET_REGISTRY) final TicketRegistry ticketRegistry,
        @Qualifier(CasBeans.ACCESS_TOKEN_EXPIRATION_POLICY) final ExpirationPolicyBuilder accessTokenExpirationPolicy,
        @Qualifier(CasBeans.SERVICES_MANAGER) final ServicesManager servicesManager,
        @Qualifier(CasBeans.ACCESS_TOKEN_JWT_BUILDER) final JwtBuilder accessTokenJwtBuilder,
        @Qualifier(
            TicketTrackingPolicy.BEAN_NAME_DESCENDANT_TICKET_TRACKING
        ) final TicketTrackingPolicy descendantTicketsTrackingPolicy
    ) {
        return new CustomOAuth20DefaultAccessTokenFactory(
            ticketRegistry,
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
        @Qualifier(CasBeans.SERVICES_MANAGER) final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier(
            RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME
        ) final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
        final ConfigurableApplicationContext applicationContext
    ) {
        return new IamSurrogateAuthenticationService(
            casApi,
            servicesManager,
            casProperties,
            principalAccessStrategyEnforcer,
            applicationContext
        );
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PasswordManagementService passwordChangeService(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasBeans.PASSWORD_MANAGEMENT_CIPHER_EXECUTOR) final CipherExecutor passwordManagementCipherExecutor,
        @Qualifier(PasswordHistoryService.BEAN_NAME) final PasswordHistoryService passwordHistoryService,
        final ProvidersService providersService,
        final TicketRegistry ticketRegistry,
        final CasApi casApi,
        final IdentityProviderHelper identityProviderHelper,
        final Utils utils,
        final PasswordValidator passwordValidator,
        @Qualifier(
            CasBeans.CENTRAL_AUTHENTICATION_SERVICE
        ) final CentralAuthenticationService centralAuthenticationService,
        final PasswordConfiguration passwordConfiguration
    ) {
        return new IamPasswordManagementService(
            casProperties,
            passwordManagementCipherExecutor,
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
            CasBeans.SERVICE_TICKET_REQUEST_WEBFLOW_EVENT_RESOLVER
        ) final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier(
            CasBeans.INITIAL_AUTHENTICATION_ATTEMPT_WEBFLOW_EVENT_RESOLVER
        ) final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier(
            CasBeans.ADAPTIVE_AUTHENTICATION_POLICY
        ) final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasBeans.SERVICES_MANAGER) final ServicesManager servicesManager,
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME) final DelegatedIdentityProviders identityProviders,
        @Qualifier(
            DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME
        ) final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer,
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_IDENTITY_PROVIDER_CONFIGURATION_POST_PROCESSOR
        ) final DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor,
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_DISTRIBUTED_SESSION_COOKIE_GENERATOR
        ) final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator,
        @Qualifier(
            CasBeans.CENTRAL_AUTHENTICATION_SERVICE
        ) final CentralAuthenticationService centralAuthenticationService,
        @Qualifier(
            CasBeans.PAC4J_DELEGATED_CLIENT_NAME_EXTRACTOR
        ) final DelegatedClientNameExtractor pac4jDelegatedClientNameExtractor,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME) final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier(ArgumentExtractor.BEAN_NAME) final ArgumentExtractor argumentExtractor,
        @Qualifier(TicketRegistry.BEAN_NAME) final TicketRegistry ticketRegistry,
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_DISTRIBUTED_SESSION_STORE
        ) final SessionStore delegatedClientDistributedSessionStore,
        @Qualifier(TicketFactory.BEAN_NAME) final TicketFactory ticketFactory,
        @Qualifier(
            AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS
        ) final AuditableExecution registeredServiceAccessStrategyEnforcer,
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_IDENTITY_PROVIDER_REDIRECTION_STRATEGY
        ) final DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy,
        @Qualifier(
            SingleSignOnParticipationStrategy.BEAN_NAME
        ) final SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy,
        @Qualifier(
            AuthenticationServiceSelectionPlan.BEAN_NAME
        ) final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
        @Qualifier(
            CasBeans.DELEGATED_AUTHENTICATION_COOKIE_GENERATOR
        ) final CasCookieBuilder delegatedAuthenticationCookieGenerator,
        @Qualifier(
            CasBeans.DELEGATED_AUTHENTICATION_CREDENTIAL_EXTRACTOR
        ) final DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(LogoutExecutionPlan.BEAN_NAME) final LogoutExecutionPlan logoutExecutionPlan,
        final ObjectProvider<List<DelegatedClientAuthenticationRequestCustomizer>> customizersProvider,
        final List<DelegatedClientIdentityProviderAuthorizer> delegatedClientIdentityProviderAuthorizers
    ) {
        final var customizers = Optional.ofNullable(customizersProvider.getIfAvailable())
            .orElseGet(ArrayList::new)
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .collect(Collectors.toList());

        final var authorizers = delegatedClientIdentityProviderAuthorizers;

        return DelegatedClientAuthenticationConfigurationContext.builder()
            // CAS 7.3 accepts a list of credential extractors instead of a single one.
            .credentialExtractors(List.of(delegatedAuthenticationCredentialExtractor))
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

    /**
     * Override the default CAS delegatedAuthenticationCredentialExtractor to avoid a NoClassDefFoundError
     * on PasswordlessWebflowUtils when the passwordless module is not in the classpath.
     * External surrogate feature is not supported.
     */
    @Bean(
        name = { "delegatedAuthenticationCredentialExtractor", "surrogateDelegatedAuthenticationCredentialExtractor" }
    )
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor(
        @Qualifier(
            CasBeans.DELEGATED_CLIENT_DISTRIBUTED_SESSION_STORE
        ) final SessionStore delegatedClientDistributedSessionStore
    ) {
        return new DefaultDelegatedAuthenticationCredentialExtractor(delegatedClientDistributedSessionStore);
    }

    @Bean
    MongoClientSettingsBuilderCustomizer mongoMetricsSynchronousContextProvider(ObservationRegistry registry) {
        return clientSettingsBuilder ->
            clientSettingsBuilder
                .contextProvider(ContextProviderFactory.create(registry))
                .addCommandListener(new MongoObservationCommandListener(registry));
    }
}
