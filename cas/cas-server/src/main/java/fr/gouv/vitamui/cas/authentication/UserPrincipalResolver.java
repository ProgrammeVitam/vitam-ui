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
package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.cas.delegation.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.x509.CertificateParser;
import fr.gouv.vitamui.cas.x509.X509AttributeMapping;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.utils.RawJson;
import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesRequestDto;
import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesResponseDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IamUtils;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogatePrincipal;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.jee.context.JEEContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.webflow.execution.RequestContextHolder;

import java.security.cert.CertificateParsingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static fr.gouv.vitamui.commons.api.CommonConstants.ADDRESS_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.ANALYTICS_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.AUTHTOKEN_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.BASIC_CUSTOMER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.CENTER_CODES;
import static fr.gouv.vitamui.commons.api.CommonConstants.CUSTOMER_IDENTIFIER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.CUSTOMER_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.EMAIL_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.FIRSTNAME_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.GROUP_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.IDENTIFIER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.INTERNAL_CODE;
import static fr.gouv.vitamui.commons.api.CommonConstants.LASTNAME_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.LAST_CONNECTION_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.LEVEL_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.MOBILE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.NB_FAILED_ATTEMPTS_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.OTP_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.PASSWORD_EXPIRATION_DATE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.PHONE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.PROFILE_GROUP_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.PROOF_TENANT_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.READONLY_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.ROLES_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SITE_CODE;
import static fr.gouv.vitamui.commons.api.CommonConstants.STATUS_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUBROGEABLE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_IDENTIFIER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.TENANTS_BY_APP_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.TYPE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.USER_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.USER_INFO_ID;

/**
 * Résolveur chargé de récupérer l'utilisateur.
 */
@Slf4j
@RequiredArgsConstructor
public class UserPrincipalResolver implements PrincipalResolver {

    public static final Pattern EMAIL_VALID_REGEXP = Pattern.compile(IamUtils.EMAIL_VALID_REGEXP);
    public static final String SUPER_USER_ID_ATTRIBUTE = "superUserId";
    public static final String COMPUTED_OTP = "computedOtp";

    public static final String PROVIDER_PROTOCOL_TYPE_CERTIFICAT = "CERTIFICAT";

    private static final String DEFAULT_PROVIDER = "";

    private final PrincipalFactory principalFactory;

    private final CasApi casApi;

    private final SessionStore sessionStore;

    private final IdentityProviderHelper identityProviderHelper;

    private final ProvidersService providersService;

    private final X509AttributeMapping x509EmailAttributeMapping;

    private final X509AttributeMapping x509IdentifierAttributeMapping;

    private final String x509DefaultDomain;

    @Override
    public Principal resolve(
        final Credential credential,
        final Optional<Principal> optPrincipal,
        final Optional<AuthenticationHandler> handler,
        final Optional<org.apereo.cas.authentication.principal.Service> service
    ) {
        // Flux OAuth 2 par code d'autorisation (authentification par identifiants client)
        if (optPrincipal.isEmpty()) {
            return NullPrincipal.getInstance();
        }

        final var principal = optPrincipal.get();
        final var principalId = principal.getId();
        final var requestContext = RequestContextHolder.getRequestContext();

        final boolean subrogationCall;
        String loginEmail;
        String loginCustomerId;
        String superUserEmail;
        String superUserCustomerId;

        String userProviderId;
        final Optional<String> technicalUserId;
        // certificat x509
        if (credential instanceof X509CertificateCredential) {
            String emailFromCertificate;
            try {
                final var certificate = ((X509CertificateCredential) credential).getCertificate();
                emailFromCertificate = CertificateParser.extract(certificate, x509EmailAttributeMapping);
                technicalUserId = Optional.ofNullable(
                    CertificateParser.extract(certificate, x509IdentifierAttributeMapping)
                );
            } catch (final CertificateParsingException e) {
                throw new RuntimeException(e.getMessage());
            }
            // En mode d'authentification par certificat X509, la subrogation est ignorée.
            subrogationCall = false;
            superUserEmail = null;
            superUserCustomerId = null;

            String userDomain;

            // Si le certificat ne contient pas l'e-mail de l'utilisateur, on utilise le domaine
            // par défaut configuré
            if (
                StringUtils.isBlank(emailFromCertificate) || !EMAIL_VALID_REGEXP.matcher(emailFromCertificate).matches()
            ) {
                userDomain = String.format("@%s", x509DefaultDomain);
                loginEmail = null;
            } else {
                loginEmail = emailFromCertificate;
                userDomain = emailFromCertificate;
            }

            // Certificate authn mode does not support multi-domain. Ensure a single
            // provider matches user email.
            final var availableProvidersForUserDomain = identityProviderHelper.findAllProvidersByUserIdentifier(
                providersService.getProviders(),
                userDomain
            );

            final var certProviders = availableProvidersForUserDomain
                .stream()
                .filter(p -> p.getProtocoleType().equals(PROVIDER_PROTOCOL_TYPE_CERTIFICAT))
                .toList();

            if (certProviders.isEmpty()) {
                LOGGER.warn(
                    "Cert authentication failed - No valid certificate identity provider found for: {}",
                    userDomain
                );
                return NullPrincipal.getInstance();
            }
            if (certProviders.size() > 1) {
                LOGGER.warn(
                    "Cert authentication failed - Too many certificate identity providers found for: {}",
                    userDomain
                );
                return NullPrincipal.getInstance();
            }

            IdentityProviderDto providerDto = certProviders.getFirst();
            userProviderId = providerDto.getId();
            loginCustomerId = providerDto.getCustomerId();
        } else if (credential instanceof SurrogateUsernamePasswordCredential) {
            userProviderId = null;
            technicalUserId = Optional.empty();

            subrogationCall = true;
            loginEmail = (String) principal.getAttributes().get(Constants.FLOW_SURROGATE_EMAIL).getFirst();
            loginCustomerId = (String) principal.getAttributes().get(Constants.FLOW_SURROGATE_CUSTOMER_ID).getFirst();
            superUserEmail = (String) principal.getAttributes().get(Constants.FLOW_LOGIN_EMAIL).getFirst();
            superUserCustomerId = (String) principal.getAttributes().get(Constants.FLOW_LOGIN_CUSTOMER_ID).getFirst();
        } else if (credential instanceof UsernamePasswordCredential) {
            // identifiant/mot de passe
            userProviderId = null;
            technicalUserId = Optional.empty();

            subrogationCall = false;
            loginEmail = (String) principal.getAttributes().get(Constants.FLOW_LOGIN_EMAIL).getFirst();
            loginCustomerId = (String) principal.getAttributes().get(Constants.FLOW_LOGIN_CUSTOMER_ID).getFirst();
            superUserEmail = null;
            superUserCustomerId = null;
        } else {
            // authentification déléguée (+ subrogation)
            final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            final var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            final var webContext = new JEEContext(request, response);
            final var clientCredential = (ClientCredential) credential;
            final var providerName = clientCredential.getClientName();
            final var provider = identityProviderHelper
                .findByTechnicalName(providersService.getProviders(), providerName)
                .get();
            final var mailAttribute = provider.getMailAttribute();
            String email = principalId;
            if (CommonHelper.isNotBlank(mailAttribute)) {
                final var mails = principal.getAttributes().get(mailAttribute);
                if (CollectionUtils.isEmpty(mails) || CommonHelper.isBlank((String) mails.getFirst())) {
                    LOGGER.error(
                        "Provider: '{}' requested specific mail attribute: '{}' for id, but attribute does not exist or has no value",
                        providerName,
                        mailAttribute
                    );
                    return NullPrincipal.getInstance();
                } else {
                    final var mail = (String) mails.getFirst();
                    LOGGER.info(
                        "Provider: '{}' requested specific mail attribute: '{}' for id: '{}' replaced by: '{}'",
                        providerName,
                        mailAttribute,
                        principalId,
                        mail
                    );
                    email = mail;
                }
            }

            final var identifierAttribute = provider.getIdentifierAttribute();
            String identifier = principalId;
            if (CommonHelper.isNotBlank(identifierAttribute)) {
                final var identifiers = principal.getAttributes().get(identifierAttribute);
                if (CollectionUtils.isEmpty(identifiers) || CommonHelper.isBlank((String) identifiers.getFirst())) {
                    LOGGER.error(
                        "Provider: '{}' requested specific identifier attribute: '{}' for id, but attribute does not exist or has no value",
                        providerName,
                        identifierAttribute
                    );
                    return NullPrincipal.getInstance();
                } else {
                    final var identifierAttr = (String) identifiers.getFirst();
                    LOGGER.info(
                        "Provider: '{}' requested specific identifier attribute: '{}' for id: '{}' replaced by: '{}'",
                        providerName,
                        identifierAttribute,
                        principalId,
                        identifierAttr
                    );
                    identifier = identifierAttr;
                }
            }

            String surrogateEmailFromSession = (String) sessionStore
                .get(webContext, Constants.FLOW_SURROGATE_EMAIL)
                .orElse(null);
            String surrogateCustomerIdFromSession = (String) sessionStore
                .get(webContext, Constants.FLOW_SURROGATE_CUSTOMER_ID)
                .orElse(null);
            String loginEmailFromSession = (String) sessionStore
                .get(webContext, Constants.FLOW_LOGIN_EMAIL)
                .orElseThrow();
            String loginCustomerIdFromSession = (String) sessionStore
                .get(webContext, Constants.FLOW_LOGIN_CUSTOMER_ID)
                .orElseThrow();

            sessionStore.set(webContext, Constants.FLOW_SURROGATE_EMAIL, null);
            sessionStore.set(webContext, Constants.FLOW_SURROGATE_CUSTOMER_ID, null);
            sessionStore.set(webContext, Constants.FLOW_LOGIN_EMAIL, null);
            sessionStore.set(webContext, Constants.FLOW_LOGIN_CUSTOMER_ID, null);

            Assert.isTrue(
                StringUtils.isNotBlank(email) &&
                StringUtils.isNotBlank(loginEmailFromSession) &&
                email.equalsIgnoreCase(loginEmailFromSession),
                String.format("Invalid user from Idp : Expected: '%s', actual: '%s'", loginEmailFromSession, email)
            );

            if (surrogateEmailFromSession != null && surrogateCustomerIdFromSession != null) {
                userProviderId = null;
                technicalUserId = Optional.empty();
                subrogationCall = true;

                loginEmail = surrogateEmailFromSession;
                loginCustomerId = surrogateCustomerIdFromSession;
                superUserEmail = loginEmailFromSession;
                superUserCustomerId = loginCustomerIdFromSession;
            } else {
                userProviderId = provider.getId();
                technicalUserId = Optional.of(identifier);
                subrogationCall = false;

                loginEmail = loginEmailFromSession;
                loginCustomerId = loginCustomerIdFromSession;
                superUserEmail = null;
                superUserCustomerId = null;
            }
        }

        LOGGER.debug(
            "Resolving loginEmail: {} | loginCustomerId: {} | superUserEmail: {} | superUserCustomerId: {} |" +
            " subrogationCall: {}",
            loginEmail,
            loginCustomerId,
            superUserEmail,
            superUserCustomerId,
            subrogationCall
        );

        // C'est l'IAM qui porte le principal : à partir de l'identité résolue par la credential, il assemble tout
        // le jeu d'attributs (y compris la décision OTP et la résolution du super-utilisateur) à partir de ses propres données.
        // Le serveur d'authentification se contente de construire la requête et de republier la réponse avec les types exacts
        // qu'attend le jeton.
        final var request = new PrincipalAttributesRequestDto();
        request.setLoginEmail(loginEmail);
        request.setLoginCustomerId(loginCustomerId);
        request.setIdentityProviderId(userProviderId);
        request.setUserIdentifier(technicalUserId.orElse(null));
        request.setSuperUserEmail(superUserEmail);
        request.setSuperUserCustomerId(superUserCustomerId);
        request.setApiContext(requestContext == null);

        final PrincipalAttributesResponseDto principalAttributes;
        try {
            principalAttributes = casApi.buildPrincipalAttributes(request);
        } catch (final RuntimeException e) {
            LOGGER.debug("No user resolved for: {} ({})", loginEmail, e.getMessage());
            return null;
        }

        if (!UserStatusEnum.ENABLED.name().equals(principalAttributes.getStatus())) {
            LOGGER.debug("User cannot login: {} - status {}", loginEmail, principalAttributes.getStatus());
            return null;
        }

        if (Objects.isNull(loginEmail)) {
            loginEmail = principalAttributes.getEmail();
        }

        final var attributes = buildAttributes(principalAttributes, loginEmail, subrogationCall);

        Principal createdPrincipal;
        try {
            createdPrincipal = principalFactory.createPrincipal(principalAttributes.getUserId(), attributes);
        } catch (final Throwable e) {
            LOGGER.error("Error creating principal", e);
            throw new RuntimeException(e);
        }
        if (subrogationCall) {
            if (principalAttributes.getSuperUserId() == null) {
                LOGGER.debug("No super user found for: {}", superUserEmail);
                return NullPrincipal.getInstance();
            }
            Principal createdSuperPrincipal;
            try {
                createdSuperPrincipal = principalFactory.createPrincipal(principalAttributes.getSuperUserId());
            } catch (final Throwable e) {
                LOGGER.error("Error creating super principal", e);
                throw new RuntimeException(e);
            }
            return new SurrogatePrincipal(createdSuperPrincipal, createdPrincipal);
        } else {
            return createdPrincipal;
        }
    }

    Map<String, List<Object>> buildAttributes(
        final PrincipalAttributesResponseDto p,
        final String loginEmail,
        final boolean subrogationCall
    ) {
        final var attributes = new HashMap<String, List<Object>>();
        attributes.put(USER_ID_ATTRIBUTE, Collections.singletonList(p.getUserId()));
        attributes.put(CUSTOMER_ID_ATTRIBUTE, Collections.singletonList(p.getCustomerId()));
        attributes.put(EMAIL_ATTRIBUTE, Collections.singletonList(loginEmail));
        attributes.put(FIRSTNAME_ATTRIBUTE, Collections.singletonList(p.getFirstname()));
        attributes.put(LASTNAME_ATTRIBUTE, Collections.singletonList(p.getLastname()));
        attributes.put(IDENTIFIER_ATTRIBUTE, Collections.singletonList(p.getIdentifier()));
        attributes.put(OTP_ATTRIBUTE, Collections.singletonList(p.isOtp()));
        attributes.put(COMPUTED_OTP, Collections.singletonList("" + p.isComputedOtp()));
        attributes.put(SUBROGEABLE_ATTRIBUTE, Collections.singletonList(p.isSubrogeable()));
        attributes.put(USER_INFO_ID, Collections.singletonList(p.getUserInfoId()));
        attributes.put(PHONE_ATTRIBUTE, Collections.singletonList(p.getPhone()));
        attributes.put(MOBILE_ATTRIBUTE, Collections.singletonList(p.getMobile()));
        attributes.put(
            STATUS_ATTRIBUTE,
            Collections.singletonList(p.getStatus() != null ? UserStatusEnum.valueOf(p.getStatus()) : null)
        );
        attributes.put(
            TYPE_ATTRIBUTE,
            Collections.singletonList(p.getType() != null ? UserTypeEnum.valueOf(p.getType()) : null)
        );
        attributes.put(READONLY_ATTRIBUTE, Collections.singletonList(p.isReadonly()));
        attributes.put(LEVEL_ATTRIBUTE, Collections.singletonList(p.getLevel()));
        attributes.put(LAST_CONNECTION_ATTRIBUTE, Collections.singletonList(p.getLastConnection()));
        attributes.put(NB_FAILED_ATTEMPTS_ATTRIBUTE, Collections.singletonList(p.getNbFailedAttempts()));
        attributes.put(PASSWORD_EXPIRATION_DATE_ATTRIBUTE, Collections.singletonList(p.getPasswordExpirationDate()));
        attributes.put(GROUP_ID_ATTRIBUTE, Collections.singletonList(p.getGroupId()));
        attributes.put(ADDRESS_ATTRIBUTE, Collections.singletonList(new RawJson(p.getAddressJson())));
        attributes.put(ANALYTICS_ATTRIBUTE, Collections.singletonList(new RawJson(p.getAnalyticsJson())));
        attributes.put(INTERNAL_CODE, Collections.singletonList(p.getInternalCode()));

        if (subrogationCall) {
            attributes.put(SUPER_USER_ATTRIBUTE, Collections.singletonList(p.getSuperUserEmail()));
            attributes.put(SUPER_USER_CUSTOMER_ID_ATTRIBUTE, Collections.singletonList(p.getSuperUserCustomerId()));
            attributes.put(SUPER_USER_IDENTIFIER_ATTRIBUTE, Collections.singletonList(p.getSuperUserIdentifier()));
            attributes.put(SUPER_USER_ID_ATTRIBUTE, Collections.singletonList(p.getSuperUserId()));
        }
        if (p.isAuthenticated()) {
            attributes.put(PROFILE_GROUP_ATTRIBUTE, Collections.singletonList(new RawJson(p.getProfileGroupJson())));
            attributes.put(CUSTOMER_IDENTIFIER_ATTRIBUTE, Collections.singletonList(p.getCustomerIdentifier()));
            attributes.put(BASIC_CUSTOMER_ATTRIBUTE, Collections.singletonList(new RawJson(p.getBasicCustomerJson())));
            attributes.put(AUTHTOKEN_ATTRIBUTE, Collections.singletonList(p.getAuthToken()));
            attributes.put(PROOF_TENANT_ID_ATTRIBUTE, Collections.singletonList(p.getProofTenantIdentifier()));
            attributes.put(TENANTS_BY_APP_ATTRIBUTE, Collections.singletonList(new RawJson(p.getTenantsByAppJson())));
            attributes.put(SITE_CODE, Collections.singletonList(p.getSiteCode()));
            attributes.put(CENTER_CODES, Collections.singletonList(p.getCenterCodes()));
            attributes.put(ROLES_ATTRIBUTE, new ArrayList<>(p.getRoles()));
        }
        return attributes;
    }

    @Override
    public boolean supports(final Credential credential) {
        return (
            credential instanceof UsernamePasswordCredential ||
            credential instanceof ClientCredential ||
            credential instanceof X509CertificateCredential
        );
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        return null;
    }
}
