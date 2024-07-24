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

import fr.gouv.vitamui.cas.authentication.IamSurrogateAuthenticationService;
import fr.gouv.vitamui.cas.authentication.UserAuthenticationHandler;
import fr.gouv.vitamui.cas.authentication.UserPrincipalResolver;
import fr.gouv.vitamui.cas.pm.IamPasswordManagementService;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.ticket.CustomOAuth20DefaultAccessTokenFactory;
import fr.gouv.vitamui.cas.ticket.DynamicTicketGrantingTicketFactory;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.x509.X509AttributeMapping;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Pac4jClientBuilder;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.iam.external.client.IdentityProviderExternalRestClient;
import lombok.SneakyThrows;
import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.EnumSet;

/**
 * Configure all beans to customize the CAS server.
 */
@Configuration
@EnableConfigurationProperties(
    { CasConfigurationProperties.class, IamClientConfigurationProperties.class, PasswordConfiguration.class }
)
public class AppConfig extends BaseTicketCatalogConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("principalFactory")
    private PrincipalFactory principalFactory;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService surrogateAuthenticationService;

    @Autowired
    private IamClientConfigurationProperties iamClientProperties;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("surrogateEligibilityAuditableExecution")
    private AuditableExecution surrogateEligibilityAuditableExecution;

    @Autowired
    @Qualifier("ticketGrantingTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator;

    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    private JwtBuilder accessTokenJwtBuilder;

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> grantingTicketExpirationPolicy;

    @Autowired
    private CipherExecutor protocolTicketCipherExecutor;

    @Autowired
    @Qualifier("accessTokenExpirationPolicy")
    private ExpirationPolicyBuilder accessTokenExpirationPolicy;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    private CipherExecutor passwordManagementCipherExecutor;

    @Autowired
    @Qualifier("passwordHistoryService")
    private PasswordHistoryService passwordHistoryService;

    @Autowired
    private PasswordConfiguration passwordConfiguration;

    @Value("${token.api.cas}")
    private String tokenApiCas;

    @Value("${ip.header}")
    private String ipHeaderName;

    @Value("${vitamui.cas.tenant.identifier}")
    private Integer casTenantIdentifier;

    @Value("${vitamui.cas.identity}")
    private String casIdentity;

    @Value("${theme.vitamui-logo-large:#{null}}")
    private String vitamuiLogoLargePath;

    @Value("${theme.vitamui-favicon:#{null}}")
    private String vitamuiFaviconPath;

    @Value("${vitamui.authn.x509.emailAttribute:}")
    private String x509EmailAttribute;

    @Value("${vitamui.authn.x509.emailAttributeParsing:}")
    private String x509EmailAttributeParsing;

    @Value("${vitamui.authn.x509.emailAttributeExpansion:}")
    private String x509EmailAttributeExpansion;

    @Value("${vitamui.authn.x509.identifierAttribute:}")
    private String x509IdentifierAttribute;

    @Value("${vitamui.authn.x509.identifierAttributeParsing:}")
    private String x509IdentifierAttributeParsing;

    @Value("${vitamui.authn.x509.identifierAttributeExpansion:}")
    private String x509IdentifierAttributeExpansion;

    @Value("${vitamui.authn.x509.defaultDomain:}")
    private String x509DefaultDomain;

    // overrides the CAS specific message converter to prevent
    // the CasRestExternalClient to use the 'application/vnd.cas.services+yaml;charset=UTF-8'
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
    public UserAuthenticationHandler userAuthenticationHandler(
        final IamExternalRestClientFactory iamRestClientFactory,
        final CasExternalRestClient casRestClient
    ) {
        return new UserAuthenticationHandler(servicesManager, principalFactory, casRestClient, utils(), ipHeaderName);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalResolver defaultPrincipalResolver(
        final ProvidersService providersService,
        @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore,
        final CasExternalRestClient casRestClient
    ) {
        val emailMapping = new X509AttributeMapping(
            x509EmailAttribute,
            x509EmailAttributeParsing,
            x509EmailAttributeExpansion
        );
        val identifierMapping = new X509AttributeMapping(
            x509IdentifierAttribute,
            x509IdentifierAttributeParsing,
            x509IdentifierAttributeExpansion
        );
        return new UserPrincipalResolver(
            principalFactory,
            casRestClient,
            utils(),
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
        final UserAuthenticationHandler userAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver") PrincipalResolver defaultPrincipalResolver
    ) {
        return plan ->
            plan.registerAuthenticationHandlerWithPrincipalResolver(
                userAuthenticationHandler,
                defaultPrincipalResolver
            );
    }

    @Bean
    @RefreshScope
    public PrincipalResolver surrogatePrincipalResolver(
        @Qualifier("defaultPrincipalResolver") PrincipalResolver defaultPrincipalResolver
    ) {
        return defaultPrincipalResolver;
    }

    @Bean
    @RefreshScope
    public PrincipalResolver x509SubjectDNPrincipalResolver(
        @Qualifier("defaultPrincipalResolver") PrincipalResolver defaultPrincipalResolver
    ) {
        return defaultPrincipalResolver;
    }

    @Bean
    public IamExternalRestClientFactory iamRestClientFactory(final RestTemplateBuilder restTemplateBuilder) {
        LOGGER.debug("Iam client factory: {}", iamClientProperties);
        return new IamExternalRestClientFactory(iamClientProperties, restTemplateBuilder);
    }

    @Bean
    public CasExternalRestClient casRestClient(final IamExternalRestClientFactory iamRestClientFactory) {
        return iamRestClientFactory.getCasExternalRestClient();
    }

    @Bean
    public IdentityProviderExternalRestClient identityProviderCrudRestClient(
        final IamExternalRestClientFactory iamRestClientFactory
    ) {
        return iamRestClientFactory.getIdentityProviderExternalRestClient();
    }

    @RefreshScope
    @Bean
    public Clients builtClients() {
        return new Clients(casProperties.getServer().getLoginUrl());
    }

    @Bean
    public ProvidersService providersService(
        @Qualifier("builtClients") final Clients builtClients,
        final IdentityProviderExternalRestClient identityProviderCrudRestClient
    ) {
        return new ProvidersService(builtClients, identityProviderCrudRestClient, pac4jClientBuilder(), utils());
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
    public Utils utils() {
        return new Utils(
            tokenApiCas,
            casTenantIdentifier,
            casIdentity,
            mailSender,
            casProperties.getServer().getPrefix()
        );
    }

    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        return new DynamicTicketGrantingTicketFactory(
            ticketGrantingTicketUniqueIdGenerator,
            grantingTicketExpirationPolicy.getObject(),
            protocolTicketCipherExecutor,
            servicesManager,
            utils()
        );
    }

    @Bean
    @RefreshScope
    public OAuth20AccessTokenFactory defaultAccessTokenFactory() {
        return new CustomOAuth20DefaultAccessTokenFactory(
            accessTokenExpirationPolicy,
            accessTokenJwtBuilder,
            servicesManager
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
        val timeout = Beans.newDuration(
            casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds()
        ).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(casProperties.getLogout().isRemoveDescendantTickets());
        registerTicketDefinition(plan, metadata);
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    public SurrogateAuthenticationService surrogateAuthenticationService(final CasExternalRestClient casRestClient) {
        return new IamSurrogateAuthenticationService(casRestClient, servicesManager, utils());
    }

    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService(
        final ProvidersService providersService,
        final TicketRegistry ticketRegistry,
        final CasExternalRestClient casRestClient
    ) {
        return new IamPasswordManagementService(
            casProperties.getAuthn().getPm(),
            passwordManagementCipherExecutor,
            casProperties.getServer().getPrefix(),
            passwordHistoryService,
            casRestClient,
            providersService,
            identityProviderHelper(),
            centralAuthenticationService.getObject(),
            utils(),
            ticketRegistry,
            passwordValidator(),
            passwordConfiguration
        );
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy() {
        return new CasSimpleMultifactorTokenCommunicationStrategy() {
            @Override
            public EnumSet<TokenSharingStrategyOptions> determineStrategy(
                CasSimpleMultifactorAuthenticationTicket token
            ) {
                return EnumSet.of(TokenSharingStrategyOptions.SMS);
            }
        };
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return new InitContextConfiguration(vitamuiLogoLargePath, vitamuiFaviconPath);
    }

    @Bean
    public ServletContextInitializer servletPasswordContextInitializer() {
        return new InitPasswordConstraintsConfiguration();
    }
}
