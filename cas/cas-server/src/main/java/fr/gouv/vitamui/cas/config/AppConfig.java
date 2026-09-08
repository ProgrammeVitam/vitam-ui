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
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.DefaultDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
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
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.data.mongodb.observability.ContextProviderFactory;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClient;

import java.util.EnumSet;

import static fr.gouv.vitamui.commons.api.CommonConstants.X_ORIGIN_HEADER_EXTERNAL;
import static fr.gouv.vitamui.commons.api.CommonConstants.X_ORIGIN_HEADER_NAME;

/**
 * Configure tous les beans pour personnaliser le serveur d'authentification.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(
    { CasConfigurationProperties.class, IamClientConfigurationProperties.class, PasswordConfiguration.class }
)
public class AppConfig extends BaseTicketCatalogConfigurer {

    // remplace le convertisseur de message spécifique à CAS pour empêcher
    // le CasRestExternalClient d'utiliser le type de contenu
    // 'application/vnd.cas.services+yaml;charset=UTF-8'
    // et d'échouer
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
        @Qualifier(CasBeans.PRINCIPAL_FACTORY) final PrincipalFactory principalFactory,
        @Qualifier(CasBeans.SERVICES_MANAGER) final ServicesManager servicesManager
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
        @Qualifier(
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
        final LoginPwdAuthenticationHandler loginPwdAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver
    ) {
        return plan ->
            plan.registerAuthenticationHandlerWithPrincipalResolver(
                loginPwdAuthenticationHandler,
                defaultPrincipalResolver
            );
    }

    // Les modules de subrogation et X509 recherchent chacun leur propre bean de résolveur : les deux pointent vers l'unique bean par défaut.
    @Bean(name = { "surrogatePrincipalResolver", "x509SubjectDNPrincipalResolver" })
    @RefreshScope
    public PrincipalResolver surrogateAndX509PrincipalResolvers(
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver
    ) {
        return defaultPrincipalResolver;
    }

    /**
     * Nous devons définir notre customizer pour remplacer l'en-tête X_ORIGIN de
     * IamApiClient.java pour l'usage de CAS.
     *
     * @return un customizer de rest client.
     */
    @Bean
    @Qualifier(CasBeans.REST_CLIENT_CUSTOMIZER)
    public RestClientCustomizer restClientCustomizer() {
        return builder ->
            builder.requestInterceptor((request, body, execution) -> {
                request.getHeaders().set(X_ORIGIN_HEADER_NAME, X_ORIGIN_HEADER_EXTERNAL);

                LOGGER.debug("Final request URI: {}, headers: {}", request.getURI(), request.getHeaders());

                return execution.execute(request, body);
            });
    }

    @Bean
    public IamApiClientsFactory iamApiClientsFactory(
        final IamClientConfigurationProperties iamClientProperties,
        final RestClient.Builder restClientBuilder,
        @Qualifier(CasBeans.REST_CLIENT_CUSTOMIZER) final RestClientCustomizer restClientCustomizer
    ) {
        // Personnalise une copie : le builder injecté est celui partagé de Spring Boot, et l'intercepteur X-Origin
        // ne doit pas se propager à tous les autres RestClient de l'application.
        final var builder = restClientBuilder.clone();
        restClientCustomizer.customize(builder);
        return new IamApiClientsFactory(iamClientProperties, builder);
    }

    @Bean
    public IamApiDecorator iamApiDecorator(
        final Utils utils,
        @Value("${vitamui.cas.service-account:admin@change-it.fr}") final String serviceAccount
    ) {
        return new IamApiDecorator(utils, serviceAccount);
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
        @Qualifier(CasBeans.ACCESS_TOKEN_ID_GENERATOR) final UniqueTicketIdGenerator accessTokenIdGenerator,
        @Qualifier(CasBeans.ACCESS_TOKEN_EXPIRATION_POLICY) final ExpirationPolicyBuilder accessTokenExpirationPolicy,
        @Qualifier(CasBeans.SERVICES_MANAGER) final ServicesManager servicesManager,
        @Qualifier(CasBeans.ACCESS_TOKEN_JWT_BUILDER) final JwtBuilder accessTokenJwtBuilder,
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
    public SurrogateAuthenticationService surrogateAuthenticationService(
        final CasApi casApi,
        @Qualifier(CasBeans.SERVICES_MANAGER) final ServicesManager servicesManager
    ) {
        return new IamSurrogateAuthenticationService(casApi, servicesManager);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PasswordManagementService passwordChangeService(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasBeans.PASSWORD_MANAGEMENT_CIPHER_EXECUTOR) final CipherExecutor passwordManagementCipherExecutor,
        @Qualifier(PasswordHistoryService.BEAN_NAME) final PasswordHistoryService passwordHistoryService,
        final ProvidersService providersService,
        final CasApi casApi,
        final IdentityProviderHelper identityProviderHelper,
        final Utils utils,
        final PasswordValidator passwordValidator
    ) {
        return new IamPasswordManagementService(
            casProperties.getAuthn().getPm(),
            passwordManagementCipherExecutor,
            casProperties.getServer().getPrefix(),
            passwordHistoryService,
            casApi,
            providersService,
            identityProviderHelper,
            utils,
            passwordValidator
        );
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy() {
        return token -> EnumSet.of(CasSimpleMultifactorTokenCommunicationStrategy.TokenSharingStrategyOptions.SMS);
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

    // Le contexte d'authentification déléguée est celui par défaut de CAS : il récupère déjà les deux beans
    // surchargés ici, delegatedIdentityProviders et delegatedAuthenticationCredentialExtractor.

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedAuthenticationPreProcessor surrogateDelegatedAuthenticationPreProcessor() {
        return (principal, client, credential, service) -> principal;
    }

    /**
     * Surcharge le delegatedAuthenticationCredentialExtractor par défaut de CAS pour éviter une NoClassDefFoundError
     * sur PasswordlessWebflowUtils lorsque le module passwordless n'est pas dans le classpath.
     * La fonctionnalité de subrogation externe n'est pas supportée.
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
