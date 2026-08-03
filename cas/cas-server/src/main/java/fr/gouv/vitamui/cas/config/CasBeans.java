/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.ArgumentExtractor;

public final class CasBeans {

    private CasBeans() {}

    // === OAuth / Tokens ===
    public static final String ACCESS_TOKEN_ID_GENERATOR = "accessTokenIdGenerator";
    public static final String ACCESS_TOKEN_EXPIRATION_POLICY = "accessTokenExpirationPolicy";
    public static final String ACCESS_TOKEN_JWT_BUILDER = "accessTokenJwtBuilder";

    // === Ticket Granting Ticket ===
    public static final String TGT_ID_GENERATOR = "ticketGrantingTicketUniqueIdGenerator";
    public static final String TGT_EXPIRATION_POLICY = "grantingTicketExpirationPolicy";

    // === Crypto ===
    public static final String PROTOCOL_TICKET_CIPHER_EXECUTOR = "protocolTicketCipherExecutor";
    public static final String PASSWORD_MANAGEMENT_CIPHER_EXECUTOR = "passwordManagementCipherExecutor";

    // === Spring / REST ===
    public static final String REST_CLIENT_CUSTOMIZER = "restClientCustomizer";

    // === Spring / Web ===
    public static final String MESSAGE_SOURCE = "messageSource";
    public static final String CORS_HTTP_WEB_REQUEST_CONFIGURATION_SOURCE = "corsHttpWebRequestConfigurationSource";
    public static final String SECURITY_CONTEXT_REPOSITORY = "securityContextRepository";
    public static final String ARGUMENT_EXTRACTOR = ArgumentExtractor.BEAN_NAME;

    // === Webflow / Authentication ===
    public static final String SERVICE_TICKET_REQUEST_WEBFLOW_EVENT_RESOLVER =
        "serviceTicketRequestWebflowEventResolver";
    public static final String INITIAL_AUTHENTICATION_ATTEMPT_WEBFLOW_EVENT_RESOLVER =
        "initialAuthenticationAttemptWebflowEventResolver";
    public static final String ADAPTIVE_AUTHENTICATION_POLICY = "adaptiveAuthenticationPolicy";

    // === Webflow / Resolvers ===
    public static final String SELECTIVE_AUTHENTICATION_PROVIDER_WEBFLOW_EVENT_RESOLVER =
        "selectiveAuthenticationProviderWebflowEventResolver";
    public static final String CAS_WEBFLOW_CONFIGURATION_CONTEXT = "casWebflowConfigurationContext";
    public static final String ADAPTIVE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "adaptiveAuthenticationPolicyWebflowEventResolver";
    public static final String TIMED_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "timedAuthenticationPolicyWebflowEventResolver";
    public static final String GLOBAL_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "globalAuthenticationPolicyWebflowEventResolver";
    public static final String HTTP_REQUEST_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "httpRequestAuthenticationPolicyWebflowEventResolver";
    public static final String REST_ENDPOINT_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "restEndpointAuthenticationPolicyWebflowEventResolver";
    public static final String GROOVY_SCRIPT_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "groovyScriptAuthenticationPolicyWebflowEventResolver";
    public static final String SCRIPTED_REGISTERED_SERVICE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "scriptedRegisteredServiceAuthenticationPolicyWebflowEventResolver";
    public static final String REGISTERED_SERVICE_PRINCIPAL_ATTRIBUTE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver";
    public static final String PREDICATED_PRINCIPAL_ATTRIBUTE_MULTIFACTOR_AUTHENTICATION_POLICY_EVENT_RESOLVER =
        "predicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver";
    public static final String PRINCIPAL_ATTRIBUTE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "principalAttributeAuthenticationPolicyWebflowEventResolver";
    public static final String AUTHENTICATION_ATTRIBUTE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "authenticationAttributeAuthenticationPolicyWebflowEventResolver";
    public static final String REGISTERED_SERVICE_AUTHENTICATION_POLICY_WEBFLOW_EVENT_RESOLVER =
        "registeredServiceAuthenticationPolicyWebflowEventResolver";

    // === Webflow / Definitions ===
    /**
     * CAS 7.3 merged the separate login and logout flow definition registries into a single registry, so the
     * former BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY and BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY are gone.
     */
    public static final String FLOW_DEFINITION_REGISTRY = CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY;

    public static final String FLOW_BUILDER_SERVICES = CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES;
    public static final String MFA_SIMPLE_AUTHENTICATOR_FLOW_REGISTRY = "mfaSimpleAuthenticatorFlowRegistry";

    // === Webflow / Actions ===
    public static final String FRONT_CHANNEL_LOGOUT_ACTION = CasWebflowConstants.ACTION_ID_FRONT_CHANNEL_LOGOUT;

    // === Delegation / Pac4j ===
    public static final String DELEGATED_CLIENT_DISTRIBUTED_SESSION_STORE = "delegatedClientDistributedSessionStore";
    public static final String DELEGATED_CLIENT_IDENTITY_PROVIDER_CONFIGURATION_POST_PROCESSOR =
        "delegatedClientIdentityProviderConfigurationPostProcessor";
    public static final String DELEGATED_CLIENT_DISTRIBUTED_SESSION_COOKIE_GENERATOR =
        "delegatedClientDistributedSessionCookieGenerator";
    public static final String PAC4J_DELEGATED_CLIENT_NAME_EXTRACTOR = "pac4jDelegatedClientNameExtractor";
    public static final String DELEGATED_CLIENT_IDENTITY_PROVIDER_REDIRECTION_STRATEGY =
        "delegatedClientIdentityProviderRedirectionStrategy";

    // === Cookies ===
    public static final String DELEGATED_AUTHENTICATION_COOKIE_GENERATOR = "delegatedAuthenticationCookieGenerator";
    public static final String WARN_COOKIE_GENERATOR = "warnCookieGenerator";
    public static final String TICKET_GRANTING_COOKIE_BUILDER =
        CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER;

    // === Authentication ===
    public static final String DELEGATED_AUTHENTICATION_CREDENTIAL_EXTRACTOR =
        "delegatedAuthenticationCredentialExtractor";
    public static final String PRINCIPAL_FACTORY = PrincipalFactory.BEAN_NAME;

    // === Services / Management ===
    public static final String SERVICES_MANAGER = ServicesManager.BEAN_NAME;
    public static final String CENTRAL_AUTHENTICATION_SERVICE = CentralAuthenticationService.BEAN_NAME;

    // === Authentication / Support ===
    public static final String AUTHENTICATION_SYSTEM_SUPPORT = AuthenticationSystemSupport.BEAN_NAME;
    public static final String MULTIFACTOR_AUTHENTICATION_PROVIDER_SELECTOR =
        MultifactorAuthenticationProviderSelector.BEAN_NAME;

    // === MFA ===
    public static final String CAS_SIMPLE_MULTIFACTOR_AUTHENTICATION_SERVICE =
        CasSimpleMultifactorAuthenticationService.BEAN_NAME;
    public static final String MFA_SIMPLE_MULTIFACTOR_TOKEN_COMMUNICATION_STRATEGY =
        "mfaSimpleMultifactorTokenCommunicationStrategy";
    public static final String MFA_SIMPLE_MULTIFACTOR_BUCKET_CONSUMER = "mfaSimpleMultifactorBucketConsumer";

    // === Tickets / Registry ===
    public static final String TICKET_REGISTRY = TicketRegistry.BEAN_NAME;
    public static final String PRINCIPAL_RESOLVER = PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER;

    // === Logout ===
    public static final String LOGOUT_MANAGER = LogoutManager.DEFAULT_BEAN_NAME;
    public static final String SINGLE_LOGOUT_REQUEST_EXECUTOR = SingleLogoutRequestExecutor.BEAN_NAME;

    // === Delegation / Contexts ===
    public static final String DELEGATED_CLIENT_AUTHENTICATION_FAILURE_EVALUATOR =
        DelegatedClientAuthenticationFailureEvaluator.BEAN_NAME;
    public static final String DELEGATED_CLIENT_AUTHENTICATION_CONFIGURATION_CONTEXT =
        DelegatedClientAuthenticationConfigurationContext.BEAN_NAME;
    public static final String DELEGATED_CLIENT_AUTHENTICATION_WEBFLOW_MANAGER =
        DelegatedClientAuthenticationWebflowManager.DEFAULT_BEAN_NAME;
    public static final String DELEGATED_IDENTITY_PROVIDERS = DelegatedIdentityProviders.BEAN_NAME;

    // === Communications ===
    public static final String COMMUNICATIONS_MANAGER = CommunicationsManager.BEAN_NAME;

    // === Password Management ===
    public static final String PASSWORD_RESET_URL_BUILDER = PasswordResetUrlBuilder.BEAN_NAME;
    public static final String PASSWORD_MANAGEMENT_SERVICE_DEFAULT = PasswordManagementService.DEFAULT_BEAN_NAME;

    // === OAuth / OIDC ===
    public static final String OIDC_REQUEST_SUPPORT = "oidcRequestSupport";
    public static final String OAUTH_CAS_CLIENT = "oauthCasClient";
    public static final String OIDC_CONFIGURATION_CONTEXT = OidcConfigurationContext.BEAN_NAME;
    public static final String OAUTH20_REQUEST_PARAMETER_RESOLVER = OAuth20RequestParameterResolver.BEAN_NAME;
}
