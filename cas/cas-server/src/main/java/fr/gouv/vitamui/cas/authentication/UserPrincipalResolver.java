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

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.x509.CertificateParser;
import fr.gouv.vitamui.cas.x509.X509AttributeMapping;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CasJsonWrapper;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fr.gouv.vitamui.commons.api.CommonConstants.ADDRESS_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.ANALYTICS_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.API_PARAMETER;
import static fr.gouv.vitamui.commons.api.CommonConstants.AUTHTOKEN_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.AUTH_TOKEN_PARAMETER;
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
import static fr.gouv.vitamui.commons.api.CommonConstants.SURROGATION_PARAMETER;
import static fr.gouv.vitamui.commons.api.CommonConstants.TENANTS_BY_APP_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.TYPE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.USER_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.USER_INFO_ID;

/**
 * Resolver to retrieve the user.
 */
@RequiredArgsConstructor
public class UserPrincipalResolver implements PrincipalResolver {

    public static final Pattern EMAIL_VALID_REGEXP = Pattern.compile(
        "^[_a-z0-9]+(((\\.|-)[_a-z0-9]+))*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$"
    );
    public static final String SUPER_USER_ID_ATTRIBUTE = "superUserId";
    public static final String COMPUTED_OTP = "computedOtp";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserPrincipalResolver.class);
    public static final String PROVIDER_PROTOCOL_TYPE_CERTIFICAT = "CERTIFICAT";

    private final PrincipalFactory principalFactory;

    private final CasExternalRestClient casExternalRestClient;

    private final Utils utils;

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
        final Optional<AuthenticationHandler> handler
    ) {
        // OAuth 2 authorization code flow (client credentials authentication)
        if (optPrincipal.isEmpty()) {
            return NullPrincipal.getInstance();
        }

        val principal = optPrincipal.get();
        val principalId = principal.getId();
        val requestContext = RequestContextHolder.getRequestContext();

        final boolean subrogationCall;
        String loginEmail;
        String loginCustomerId;
        String superUserEmail;
        String superUserCustomerId;

        String userProviderId;
        final Optional<String> technicalUserId;
        // x509 certificate
        if (credential instanceof X509CertificateCredential) {
            String emailFromCertificate;
            try {
                val certificate = ((X509CertificateCredential) credential).getCertificate();
                emailFromCertificate = CertificateParser.extract(certificate, x509EmailAttributeMapping);
                technicalUserId = Optional.ofNullable(
                    CertificateParser.extract(certificate, x509IdentifierAttributeMapping)
                );
            } catch (final CertificateParsingException e) {
                throw new RuntimeException(e.getMessage());
            }
            // In X509 cert authn mode, subrogation is ignored.
            subrogationCall = false;
            superUserEmail = null;
            superUserCustomerId = null;

            String userDomain;

            // If the certificate does not contain the user mail, then we use the default domain configured
            if (
                StringUtils.isBlank(emailFromCertificate) || !EMAIL_VALID_REGEXP.matcher(emailFromCertificate).matches()
            ) {
                userDomain = String.format("@%s", x509DefaultDomain);
                loginEmail = null;
            } else {
                loginEmail = emailFromCertificate;
                userDomain = emailFromCertificate;
            }

            // Certificate authn mode does not support multi-domain. Ensure a single provider matches user email.
            val availableProvidersForUserDomain = identityProviderHelper.findAllByUserIdentifier(
                providersService.getProviders(),
                userDomain
            );

            var certProviders = availableProvidersForUserDomain
                .stream()
                .filter(p -> p.getProtocoleType().equals(PROVIDER_PROTOCOL_TYPE_CERTIFICAT))
                .collect(Collectors.toList());

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

            IdentityProviderDto providerDto = certProviders.get(0);
            userProviderId = providerDto.getId();
            loginCustomerId = providerDto.getCustomerId();
        } else if (credential instanceof SurrogateUsernamePasswordCredential) {
            userProviderId = null;
            technicalUserId = Optional.empty();

            subrogationCall = true;
            loginEmail = (String) principal.getAttributes().get(Constants.FLOW_SURROGATE_EMAIL).get(0);
            loginCustomerId = (String) principal.getAttributes().get(Constants.FLOW_SURROGATE_CUSTOMER_ID).get(0);
            superUserEmail = (String) principal.getAttributes().get(Constants.FLOW_LOGIN_EMAIL).get(0);
            superUserCustomerId = (String) principal.getAttributes().get(Constants.FLOW_LOGIN_CUSTOMER_ID).get(0);
        } else if (credential instanceof UsernamePasswordCredential) {
            // login/password
            userProviderId = null;
            technicalUserId = Optional.empty();

            subrogationCall = false;
            loginEmail = (String) principal.getAttributes().get(Constants.FLOW_LOGIN_EMAIL).get(0);
            loginCustomerId = (String) principal.getAttributes().get(Constants.FLOW_LOGIN_CUSTOMER_ID).get(0);
            superUserEmail = null;
            superUserCustomerId = null;
        } else {
            // authentication delegation (+ surrogation)
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val webContext = new JEEContext(request, response);
            val clientCredential = (ClientCredential) credential;
            val providerName = clientCredential.getClientName();
            val provider = identityProviderHelper
                .findByTechnicalName(providersService.getProviders(), providerName)
                .get();
            val mailAttribute = provider.getMailAttribute();
            String email = principalId;
            if (CommonHelper.isNotBlank(mailAttribute)) {
                val mails = principal.getAttributes().get(mailAttribute);
                if (CollectionUtils.isEmpty(mails) || CommonHelper.isBlank((String) mails.get(0))) {
                    LOGGER.error(
                        "Provider: '{}' requested specific mail attribute: '{}' for id, but attribute does not exist or has no value",
                        providerName,
                        mailAttribute
                    );
                    return NullPrincipal.getInstance();
                } else {
                    val mail = (String) mails.get(0);
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

            val identifierAttribute = provider.getIdentifierAttribute();
            String identifier = principalId;
            if (CommonHelper.isNotBlank(identifierAttribute)) {
                val identifiers = principal.getAttributes().get(identifierAttribute);
                if (CollectionUtils.isEmpty(identifiers) || CommonHelper.isBlank((String) identifiers.get(0))) {
                    LOGGER.error(
                        "Provider: '{}' requested specific identifier attribute: '{}' for id, but attribute does not exist or has no value",
                        providerName,
                        identifierAttribute
                    );
                    return NullPrincipal.getInstance();
                } else {
                    val identifierAttr = (String) identifiers.get(0);
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
                email.equals(loginEmailFromSession),
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

        String embedded = AUTH_TOKEN_PARAMETER;
        if (subrogationCall) {
            embedded += "," + SURROGATION_PARAMETER;
        } else if (requestContext == null) {
            embedded += "," + API_PARAMETER;
        }
        LOGGER.debug("Computed embedded: {}", embedded);

        final UserDto user = casExternalRestClient.getUser(
            utils.buildContext(loginEmail),
            loginEmail,
            loginCustomerId,
            userProviderId,
            technicalUserId,
            Optional.of(embedded)
        );

        if (user == null) {
            LOGGER.debug("No user resolved for: {}", loginEmail);
            return null;
        } else if (user.getStatus() != UserStatusEnum.ENABLED) {
            LOGGER.debug("User cannot login: {} - User {}", loginEmail, user.toString());
            return null;
        }

        if (Objects.isNull(loginEmail)) {
            loginEmail = user.getEmail();
        }

        val attributes = new HashMap<String, List<Object>>();
        attributes.put(USER_ID_ATTRIBUTE, Collections.singletonList(user.getId()));
        attributes.put(CUSTOMER_ID_ATTRIBUTE, Collections.singletonList(user.getCustomerId()));
        attributes.put(EMAIL_ATTRIBUTE, Collections.singletonList(loginEmail));
        attributes.put(FIRSTNAME_ATTRIBUTE, Collections.singletonList(user.getFirstname()));
        attributes.put(LASTNAME_ATTRIBUTE, Collections.singletonList(user.getLastname()));
        attributes.put(IDENTIFIER_ATTRIBUTE, Collections.singletonList(user.getIdentifier()));
        val otp = user.isOtp();
        attributes.put(OTP_ATTRIBUTE, Collections.singletonList(otp));
        val otpUsername = subrogationCall ? superUserEmail : loginEmail;
        val otpCustomerId = subrogationCall ? superUserCustomerId : loginCustomerId;
        val computedOtp =
            otp &&
            identityProviderHelper.identifierMatchProviderPattern(
                providersService.getProviders(),
                otpUsername,
                otpCustomerId
            );
        attributes.put(COMPUTED_OTP, Collections.singletonList("" + computedOtp));
        attributes.put(SUBROGEABLE_ATTRIBUTE, Collections.singletonList(user.isSubrogeable()));
        attributes.put(USER_INFO_ID, Collections.singletonList(user.getUserInfoId()));
        attributes.put(PHONE_ATTRIBUTE, Collections.singletonList(user.getPhone()));
        attributes.put(MOBILE_ATTRIBUTE, Collections.singletonList(user.getMobile()));
        attributes.put(STATUS_ATTRIBUTE, Collections.singletonList(user.getStatus()));
        attributes.put(TYPE_ATTRIBUTE, Collections.singletonList(user.getType()));
        attributes.put(READONLY_ATTRIBUTE, Collections.singletonList(user.isReadonly()));
        attributes.put(LEVEL_ATTRIBUTE, Collections.singletonList(user.getLevel()));
        attributes.put(LAST_CONNECTION_ATTRIBUTE, Collections.singletonList(user.getLastConnection()));
        attributes.put(NB_FAILED_ATTEMPTS_ATTRIBUTE, Collections.singletonList(user.getNbFailedAttempts()));
        attributes.put(PASSWORD_EXPIRATION_DATE_ATTRIBUTE, Collections.singletonList(user.getPasswordExpirationDate()));
        attributes.put(GROUP_ID_ATTRIBUTE, Collections.singletonList(user.getGroupId()));
        attributes.put(ADDRESS_ATTRIBUTE, Collections.singletonList(new CasJsonWrapper(user.getAddress())));
        attributes.put(ANALYTICS_ATTRIBUTE, Collections.singletonList(new CasJsonWrapper(user.getAnalytics())));
        attributes.put(INTERNAL_CODE, Collections.singletonList(user.getInternalCode()));
        UserDto superUser = null;
        if (subrogationCall) {
            attributes.put(SUPER_USER_ATTRIBUTE, Collections.singletonList(superUserEmail));
            attributes.put(SUPER_USER_CUSTOMER_ID_ATTRIBUTE, Collections.singletonList(superUserCustomerId));
            superUser = casExternalRestClient.getUser(
                utils.buildContext(superUserEmail),
                superUserEmail,
                superUserCustomerId,
                null,
                Optional.empty(),
                Optional.empty()
            );
            if (superUser == null) {
                LOGGER.debug("No super user found for: {}", superUserEmail);
                return NullPrincipal.getInstance();
            }
            attributes.put(SUPER_USER_IDENTIFIER_ATTRIBUTE, Collections.singletonList(superUser.getIdentifier()));
            attributes.put(SUPER_USER_ID_ATTRIBUTE, Collections.singletonList(superUser.getId()));
        }
        if (user instanceof AuthUserDto) {
            final AuthUserDto authUser = (AuthUserDto) user;
            attributes.put(
                PROFILE_GROUP_ATTRIBUTE,
                Collections.singletonList(new CasJsonWrapper(authUser.getProfileGroup()))
            );
            attributes.put(CUSTOMER_IDENTIFIER_ATTRIBUTE, Collections.singletonList(authUser.getCustomerIdentifier()));
            attributes.put(
                BASIC_CUSTOMER_ATTRIBUTE,
                Collections.singletonList(new CasJsonWrapper(authUser.getBasicCustomer()))
            );
            attributes.put(AUTHTOKEN_ATTRIBUTE, Collections.singletonList(authUser.getAuthToken()));
            attributes.put(PROOF_TENANT_ID_ATTRIBUTE, Collections.singletonList(authUser.getProofTenantIdentifier()));
            attributes.put(
                TENANTS_BY_APP_ATTRIBUTE,
                Collections.singletonList(new CasJsonWrapper(authUser.getTenantsByApp()))
            );
            attributes.put(SITE_CODE, Collections.singletonList(user.getSiteCode()));
            attributes.put(CENTER_CODES, Collections.singletonList(user.getCenterCodes()));
            final Set<String> roles = new HashSet<>();
            final List<ProfileDto> profiles = authUser.getProfileGroup().getProfiles();
            profiles.forEach(profile -> profile.getRoles().forEach(role -> roles.add(role.getName())));
            attributes.put(ROLES_ATTRIBUTE, new ArrayList<>(roles));
        }
        val createdPrincipal = principalFactory.createPrincipal(user.getId(), attributes);
        if (subrogationCall) {
            val createdSuperPrincipal = principalFactory.createPrincipal(superUser.getId());
            return new SurrogatePrincipal(createdSuperPrincipal, createdPrincipal);
        } else {
            return createdPrincipal;
        }
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
