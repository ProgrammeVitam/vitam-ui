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

import fr.gouv.vitamui.cas.authentication.*;
import fr.gouv.vitamui.cas.pm.IamPasswordManagementService;
import lombok.SneakyThrows;
import lombok.val;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.ticket.CustomOAuth20DefaultAccessTokenFactory;
import fr.gouv.vitamui.cas.ticket.DynamicTicketGrantingTicketFactory;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Saml2ClientBuilder;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.iam.external.client.IdentityProviderExternalRestClient;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Configure all beans to customize the CAS server.
 *
 *
 */
@Configuration
@EnableConfigurationProperties({ CasConfigurationProperties.class, IamClientConfigurationProperties.class })
@Import(ServerIdentityAutoConfiguration.class)
public class AppConfig extends BaseTicketCatalogConfigurer {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AppConfig.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("principalFactory")
    private PrincipalFactory principalFactory;

    @Autowired
    @Qualifier("clientAuthenticationHandler")
    private AuthenticationHandler clientAuthenticationHandler;

    @Autowired
    @Qualifier("clientAuthenticationMetaDataPopulator")
    private AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService surrogateAuthenticationService;

    @Autowired
    private IamClientConfigurationProperties iamClientProperties;

    // needed to allow a proper creation of the IamExternalRestClientFactory
    @Autowired
    private ServerIdentityConfiguration serverIdentityConfiguration;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("surrogateEligibilityAuditableExecution")
    private AuditableExecution surrogateEligibilityAuditableExecution;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

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
    @Qualifier("delegatedClientDistributedSessionStore")
    private SessionStore delegatedClientDistributedSessionStore;

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    private CipherExecutor passwordManagementCipherExecutor;

    @Autowired
    @Qualifier("passwordHistoryService")
    private PasswordHistoryService passwordHistoryService;

    @Value("${token.api.cas}")
    private String tokenApiCas;

    @Value("${api.token.ttl}")
    private Integer apiTokenTtl;

    @Value("${ip.header}")
    private String ipHeaderName;

    @Value("${vitamui.cas.tenant.identifier}")
    private Integer casTenantIdentifier;

    @Value("${vitamui.cas.identity}")
    private String casIdentity;

    @Autowired
    @Qualifier("oauthSecConfig")
    private ObjectProvider<Config> oauthSecConfig;

    @Autowired
    @Qualifier("accessTokenGrantRequestExtractors")
    private Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    @Bean
    public SecurityInterceptor requiresAuthenticationAuthorizeInterceptor() {
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(), Authenticators.CAS_OAUTH_CLIENT, JEEHttpActionAdapter.INSTANCE);
        interceptor.setAuthorizers("none");
        return interceptor;
    }

    @Bean
    public SecurityInterceptor requiresAuthenticationAccessTokenInterceptor() {
        val secConfig = oauthSecConfig.getObject();
        val clients =
                Objects.requireNonNull(secConfig).getClients().findAllClients().stream().filter(client -> client instanceof DirectClient).map(Client::getName)
                        .collect(Collectors.joining(","));
        val interceptor = new SecurityInterceptor(oauthSecConfig.getObject(), clients, JEEHttpActionAdapter.INSTANCE);
        interceptor.setAuthorizers("none");
        return interceptor;
    }

    @Bean
    public HandlerInterceptor oauthHandlerInterceptorAdapter() {
        return new OAuth20HandlerInterceptorAdapter(requiresAuthenticationAccessTokenInterceptor(), requiresAuthenticationAuthorizeInterceptor(),
                accessTokenGrantRequestExtractors);
    }

    @Bean
    public UserAuthenticationHandler userAuthenticationHandler() {
        return new UserAuthenticationHandler(servicesManager, principalFactory, casRestClient(), utils(), ipHeaderName);
    }

    @Bean
    @RefreshScope
    public PrincipalResolver surrogatePrincipalResolver() {
        return new UserPrincipalResolver(principalFactory, casRestClient(), utils(), delegatedClientDistributedSessionStore,
            identityProviderHelper(), providersService());
    }

    @Bean
    public AuthenticationEventExecutionPlanConfigurer registerInternalHandler() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(userAuthenticationHandler(), surrogatePrincipalResolver());
    }

    @Bean
    public AuthenticationEventExecutionPlanConfigurer pac4jAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandlerWithPrincipalResolver(clientAuthenticationHandler, surrogatePrincipalResolver());
            plan.registerAuthenticationMetadataPopulator(clientAuthenticationMetaDataPopulator);
        };
    }

    @Bean
    public AuthenticationPostProcessor surrogateAuthenticationPostProcessor() {
        return new DelegatedSurrogateAuthenticationPostProcessor(surrogateAuthenticationService, servicesManager, eventPublisher,
            registeredServiceAccessStrategyEnforcer, surrogateEligibilityAuditableExecution, delegatedClientDistributedSessionStore);
    }

    @Bean
    public IamExternalRestClientFactory iamRestClientFactory() {
        LOGGER.debug("Iam client factory: {}", iamClientProperties);
        return new IamExternalRestClientFactory(iamClientProperties, restTemplateBuilder);
    }

    @Bean
    public CasExternalRestClient casRestClient() {
        return iamRestClientFactory().getCasExternalRestClient();
    }

    @Bean
    public IdentityProviderExternalRestClient identityProviderCrudRestClient() {
        return iamRestClientFactory().getIdentityProviderExternalRestClient();
    }

    @Bean
    public ProvidersService providersService() {
        return new ProvidersService();
    }

    @Bean
    public Saml2ClientBuilder saml2ClientBuilder() {
        return new Saml2ClientBuilder();
    }

    @Bean
    public IdentityProviderHelper identityProviderHelper() {
        return new IdentityProviderHelper();
    }

    @Bean
    public Utils utils() {
        return new Utils(tokenApiCas, casTenantIdentifier, casIdentity, mailSender);
    }

    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        return new DynamicTicketGrantingTicketFactory(ticketGrantingTicketUniqueIdGenerator, grantingTicketExpirationPolicy.getObject(),
            protocolTicketCipherExecutor);
    }

    @Bean
    @RefreshScope
    public OAuth20AccessTokenFactory defaultAccessTokenFactory() {
        return new CustomOAuth20DefaultAccessTokenFactory(accessTokenExpirationPolicy,
            accessTokenJwtBuilder, servicesManager);
    }

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        final TicketDefinition metadata = buildTicketDefinition(plan, "TOK", OAuth20DefaultAccessToken.class, Ordered.HIGHEST_PRECEDENCE);
        metadata.getProperties().setStorageName("oauthAccessTokensCache");
        metadata.getProperties().setStorageTimeout(apiTokenTtl);
        registerTicketDefinition(plan, metadata);
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        return new IamSurrogateAuthenticationService(casRestClient(), servicesManager, utils());
    }

    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        return new IamPasswordManagementService(casProperties.getAuthn().getPm(), passwordManagementCipherExecutor,
            casProperties.getServer().getPrefix(), passwordHistoryService, casRestClient(), providersService(),
            identityProviderHelper(), centralAuthenticationService.getObject(), utils(), ticketRegistry);
    }
}
