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

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthAccessTokenProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenExpirationPolicy;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.pac4j.DelegatedSessionCookieManager;
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fr.gouv.vitamui.cas.authentication.DelegatedSurrogateAuthenticationPostProcessor;
import fr.gouv.vitamui.cas.authentication.SurrogatedUserPrincipalFactory;
import fr.gouv.vitamui.cas.authentication.UserAuthenticationHandler;
import fr.gouv.vitamui.cas.authentication.UserPrincipalResolver;
import fr.gouv.vitamui.cas.metrics.TimedAspect;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.ticket.CustomAccessTokenFactory;
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
import io.micrometer.core.instrument.MeterRegistry;

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
    @Qualifier("pac4jDelegatedSessionCookieManager")
    private DelegatedSessionCookieManager delegatedSessionCookieManager;

    @Autowired
    private DelegatedClientWebflowManager delegatedClientWebflowManager;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    @Qualifier("ticketGrantingTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator ticketGrantingTicketUniqueIdGenerator;

    @Autowired
    private ExpirationPolicy grantingTicketExpirationPolicy;

    @Autowired
    private CipherExecutor protocolTicketCipherExecutor;

    @Value("${token.api.cas}")
    private String tokenApiCas;

    @Value("${api.token.ttl}")
    private Integer apiTokenTtl;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public UserAuthenticationHandler userAuthenticationHandler() {
        return new UserAuthenticationHandler(servicesManager, principalFactory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapperDontFailOnUnknowProperties = new ObjectMapper();
        mapperDontFailOnUnknowProperties.registerModule(new JavaTimeModule());
        mapperDontFailOnUnknowProperties.registerModule(new Jdk8Module());
        mapperDontFailOnUnknowProperties.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapperDontFailOnUnknowProperties;
    }

    @Bean
    public UserPrincipalResolver userResolver() {
        return new UserPrincipalResolver();
    }

    @Bean
    public AuthenticationEventExecutionPlanConfigurer registerInternalHandler(final UserAuthenticationHandler userAuthenticationHandler,
            final UserPrincipalResolver userResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(userAuthenticationHandler, userResolver);
    }

    @Bean
    public AuthenticationEventExecutionPlanConfigurer pac4jAuthenticationEventExecutionPlanConfigurer(final UserPrincipalResolver userResolver) {
        return plan -> {
            plan.registerAuthenticationHandlerWithPrincipalResolver(clientAuthenticationHandler, userResolver);
            plan.registerMetadataPopulator(clientAuthenticationMetaDataPopulator);
        };
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
    public PrincipalFactory surrogatePrincipalFactory() {
        return new SurrogatedUserPrincipalFactory();
    }

    @Bean
    public AuthenticationPostProcessor surrogateAuthenticationPostProcessor() {
        return new DelegatedSurrogateAuthenticationPostProcessor(surrogateAuthenticationService, servicesManager, eventPublisher,
                registeredServiceAccessStrategyEnforcer, surrogateEligibilityAuditableExecution);
    }

    @Bean
    public IdentityProviderHelper identityProviderHelper() {
        return new IdentityProviderHelper();
    }

    @Bean
    public Utils utils() {
        return new Utils(casRestClient(), delegatedClientWebflowManager, delegatedSessionCookieManager, tokenApiCas);
    }

    @Bean
    public TicketGrantingTicketFactory defaultTicketGrantingTicketFactory() {
        return new DynamicTicketGrantingTicketFactory(ticketGrantingTicketUniqueIdGenerator, grantingTicketExpirationPolicy, protocolTicketCipherExecutor);
    }

    @Bean
    public TimedAspect timedAspect(@Autowired final MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }

    @Bean
    public ExpirationPolicy accessTokenExpirationPolicy() {
        final OAuthAccessTokenProperties oauth = casProperties.getAuthn().getOauth().getAccessToken();
        if (casProperties.getLogout().isRemoveDescendantTickets()) {
            return new OAuthAccessTokenExpirationPolicy(Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).getSeconds(),
                    Beans.newDuration(oauth.getTimeToKillInSeconds()).getSeconds());
        }
        return new OAuthAccessTokenExpirationPolicy.OAuthAccessTokenSovereignExpirationPolicy(Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).getSeconds(),
                Beans.newDuration(oauth.getTimeToKillInSeconds()).getSeconds());
    }

    @Bean
    @RefreshScope
    public AccessTokenFactory defaultAccessTokenFactory() {
        return new CustomAccessTokenFactory(accessTokenExpirationPolicy());
    }

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        final TicketDefinition metadata = buildTicketDefinition(plan, "TOK", AccessTokenImpl.class, Ordered.HIGHEST_PRECEDENCE);
        metadata.getProperties().setStorageName("oauthAccessTokensCache");
        metadata.getProperties().setStorageTimeout(apiTokenTtl);
        registerTicketDefinition(plan, metadata);
    }
}
