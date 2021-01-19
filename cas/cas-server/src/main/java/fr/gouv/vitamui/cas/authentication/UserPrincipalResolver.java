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

import static fr.gouv.vitamui.commons.api.CommonConstants.ADDRESS_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.API_PARAMETER;
import static fr.gouv.vitamui.commons.api.CommonConstants.AUTHTOKEN_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.AUTH_TOKEN_PARAMETER;
import static fr.gouv.vitamui.commons.api.CommonConstants.BASIC_CUSTOMER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.CUSTOMER_IDENTIFIER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.CUSTOMER_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.EMAIL_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.FIRSTNAME_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.GROUP_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.IDENTIFIER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.LANGUAGE_ATTRIBUTE;
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
import static fr.gouv.vitamui.commons.api.CommonConstants.STATUS_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUBROGEABLE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_IDENTIFIER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SURROGATION_PARAMETER;
import static fr.gouv.vitamui.commons.api.CommonConstants.TENANTS_BY_APP_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SITE_CODE;
import static fr.gouv.vitamui.commons.api.CommonConstants.TYPE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.USER_ID_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.ANALYTICS_ATTRIBUTE;

import java.util.*;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import lombok.RequiredArgsConstructor;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.CommonHelper;
import org.springframework.webflow.execution.RequestContextHolder;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CasJsonWrapper;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;

import lombok.val;

/**
 * Resolver to retrieve the user.
 *
 *
 */
@RequiredArgsConstructor
public class UserPrincipalResolver implements PrincipalResolver {

    public static final String SUPER_USER_ID_ATTRIBUTE = "superUserId";
    public static final String COMPUTED_OTP = "computedOtp";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserPrincipalResolver.class);

    private final PrincipalFactory principalFactory;

    private final CasExternalRestClient casExternalRestClient;

    private final Utils utils;

    private final SessionStore sessionStore;

    private final IdentityProviderHelper identityProviderHelper;

    private final ProvidersService providersService;

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> optPrincipal, final Optional<AuthenticationHandler> handler) {

        val principal = optPrincipal.get();
        String userId = principal.getId();
        val requestContext = RequestContextHolder.getRequestContext();

        boolean surrogationCall;
        String username;
        String superUsername;
        if (credential instanceof SurrogateUsernamePasswordCredential) {
            val surrogationCredential = (SurrogateUsernamePasswordCredential) credential;
            username = surrogationCredential.getSurrogateUsername();
            superUsername = surrogationCredential.getUsername();
            surrogationCall = true;
        } else if (credential instanceof UsernamePasswordCredential) {
            username = userId;
            superUsername = null;
            surrogationCall = false;
        } else { // ClientCredential
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val webContext = new JEEContext(request, response, sessionStore);
            String providerName = (String) sessionStore.get(webContext, Constants.PROVIDER_TECHNICAL_NAME).orElse(null);
            // no provider name in session, we didn't go through DispatcherAction
            // it means we have an idp parameter in request or cookie
            if (providerName == null) {
                providerName = utils.getIdpValue(request);
            }
            val provider = identityProviderHelper.findByTechnicalName(providersService.getProviders(), providerName).get();
            val mailAttribute = provider.getMailAttribute();
            if (CommonHelper.isNotBlank(mailAttribute)) {
                val mails = principal.getAttributes().get(mailAttribute);
                if (mails == null || mails.size() == 0 || CommonHelper.isBlank((String) mails.get(0))) {
                    LOGGER.error("Provider: '{}' requested specific mail attribute: '{}' for id, but attribute does not exist or has no value", providerName, mailAttribute);
                    return NullPrincipal.getInstance();
                } else {
                    val mail = (String) mails.get(0);
                    LOGGER.error("Provider: '{}' requested specific mail attribute: '{}' for id: '{}' replaced by: '{}'", providerName, mailAttribute, userId, mail);
                    userId = mail;
                }
            }
            val surrogateInSession = sessionStore.get(webContext, Constants.SURROGATE).orElse(null);
            if (surrogateInSession != null) {
                username = (String) surrogateInSession;
                superUsername = userId;
                surrogationCall = true;
            } else {
                username = userId;
                superUsername = null;
                surrogationCall = false;
            }
        }

        LOGGER.debug("Resolving username: {} | superUsername: {} | surrogationCall: {}", username, superUsername, surrogationCall);

        String embedded = AUTH_TOKEN_PARAMETER;
        if (surrogationCall) {
            embedded += "," + SURROGATION_PARAMETER;
        } else if (requestContext == null) {
            embedded += "," + API_PARAMETER;
        }
        LOGGER.debug("Computed embedded: {}", embedded);
        final UserDto user = casExternalRestClient.getUserByEmail(utils.buildContext(username), username, Optional.ofNullable(embedded));
        if (user == null) {
            LOGGER.debug("No user resolved for: {}", username);
            return null;
        }
        else if (user.getStatus() != UserStatusEnum.ENABLED) {
            LOGGER.debug("User cannot login: {} - User {}", username, user.toString());
            return null;
        }
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(USER_ID_ATTRIBUTE, Collections.singletonList(user.getId()));
        attributes.put(CUSTOMER_ID_ATTRIBUTE, Collections.singletonList(user.getCustomerId()));
        attributes.put(EMAIL_ATTRIBUTE, Collections.singletonList(username));
        attributes.put(FIRSTNAME_ATTRIBUTE, Collections.singletonList(user.getFirstname()));
        attributes.put(LASTNAME_ATTRIBUTE, Collections.singletonList(user.getLastname()));
        attributes.put(IDENTIFIER_ATTRIBUTE, Collections.singletonList(user.getIdentifier()));
        val otp = user.isOtp();
        attributes.put(OTP_ATTRIBUTE, Collections.singletonList(otp));
        val otpUsername = superUsername != null ? superUsername : username;
        val computedOtp = otp && identityProviderHelper.identifierMatchProviderPattern(providersService.getProviders(), otpUsername);
        attributes.put(COMPUTED_OTP, Collections.singletonList("" + computedOtp));
        attributes.put(SUBROGEABLE_ATTRIBUTE, Collections.singletonList(user.isSubrogeable()));
        attributes.put(LANGUAGE_ATTRIBUTE, Collections.singletonList(user.getLanguage()));
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
        if (surrogationCall) {
            attributes.put(SUPER_USER_ATTRIBUTE, Collections.singletonList(superUsername));
            final UserDto superUser = casExternalRestClient.getUserByEmail(utils.buildContext(superUsername), superUsername, Optional.empty());
            attributes.put(SUPER_USER_IDENTIFIER_ATTRIBUTE, Collections.singletonList(superUser.getIdentifier()));
            attributes.put(SUPER_USER_ID_ATTRIBUTE, Collections.singletonList(superUser.getId()));
        }
        if (user instanceof AuthUserDto) {
            final AuthUserDto authUser = (AuthUserDto) user;
            attributes.put(PROFILE_GROUP_ATTRIBUTE, Collections.singletonList(new CasJsonWrapper(authUser.getProfileGroup())));
            attributes.put(CUSTOMER_IDENTIFIER_ATTRIBUTE, Collections.singletonList(authUser.getCustomerIdentifier()));
            attributes.put(BASIC_CUSTOMER_ATTRIBUTE, Collections.singletonList(new CasJsonWrapper(authUser.getBasicCustomer())));
            attributes.put(AUTHTOKEN_ATTRIBUTE, Collections.singletonList(authUser.getAuthToken()));
            attributes.put(PROOF_TENANT_ID_ATTRIBUTE, Collections.singletonList(authUser.getProofTenantIdentifier()));
            attributes.put(TENANTS_BY_APP_ATTRIBUTE, Collections.singletonList(new CasJsonWrapper(authUser.getTenantsByApp())));
            attributes.put(SITE_CODE, Collections.singletonList(user.getSiteCode()));
            final Set<String> roles = new HashSet<>();
            final List<ProfileDto> profiles = authUser.getProfileGroup().getProfiles();
            profiles.forEach(profile -> profile.getRoles().forEach(role -> roles.add(role.getName())));
            attributes.put(ROLES_ATTRIBUTE, new ArrayList(roles));
        }
        return principalFactory.createPrincipal(user.getId(), attributes);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential || credential instanceof ClientCredential
            || credential instanceof SurrogateUsernamePasswordCredential;
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        return null;
    }
}
