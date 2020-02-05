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
import static fr.gouv.vitamui.commons.api.CommonConstants.TYPE_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.USER_ID_ATTRIBUTE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
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
import lombok.Getter;
import lombok.Setter;

/**
 * Resolver to retrieve the user.
 *
 *
 */
@Getter
@Setter
public class UserPrincipalResolver implements PrincipalResolver {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserPrincipalResolver.class);

    @Autowired
    private PrincipalFactory principalFactory;

    @Autowired
    private CasExternalRestClient casExternalRestClient;

    @Autowired
    private Utils utils;

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> principal, final Optional<AuthenticationHandler> handler) {
        return resolve(principal.get().getId(), principal.get().getAttributes());
    }

    public Principal resolve(final String username, final Map<String, Object> oldAttributes) {
        String superUsername = null;
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        if (requestContext != null) {
            final MutableAttributeMap<Object> flow = requestContext.getFlowScope();
            // try to find the super user from the regular surrogation flow or in an authentication delegation
            if (flow != null) {
                final Object credential = flow.get("credential");
                if (credential instanceof SurrogateUsernamePasswordCredential) {
                    superUsername = ((SurrogateUsernamePasswordCredential) credential).getUsername();
                }
                else if (credential instanceof ClientCredential) {
                    final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                    final boolean hasSurrogateInRequest = request.getAttribute(Constants.SURROGATE) != null;
                    if (hasSurrogateInRequest) {
                        superUsername = ((ClientCredential) credential).getUserProfile().getId();
                    }
                }
            }
        }

        boolean authToken = true;
        final boolean finalSurrogationCall = oldAttributes.containsKey(SUPER_USER_ATTRIBUTE);
        // this is not called by the surrogation principal factory, but the surrogation is in progress, it will be called again
        // spare the extra info on this call by not requesting the auth token
        if (!finalSurrogationCall && superUsername != null) {
            LOGGER.debug("No final surrogation call but surrogation in progress -> no auth token");
            authToken = false;
        }

        LOGGER.debug("Resolving username: {} | superUsername: {} | authToken: {} | finalSurrogationCall: {}", username, superUsername, authToken,
                finalSurrogationCall);

        String embedded = null;
        if (authToken) {
            embedded = AUTH_TOKEN_PARAMETER;
            if (finalSurrogationCall) {
                embedded += "," + SURROGATION_PARAMETER;
            }
            else if (requestContext == null) {
                embedded += "," + API_PARAMETER;
            }
        }
        else if (finalSurrogationCall) {
            embedded = SURROGATION_PARAMETER;
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
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(USER_ID_ATTRIBUTE, user.getId());
        attributes.put(CUSTOMER_ID_ATTRIBUTE, user.getCustomerId());
        attributes.put(EMAIL_ATTRIBUTE, username);
        attributes.put(FIRSTNAME_ATTRIBUTE, user.getFirstname());
        attributes.put(LASTNAME_ATTRIBUTE, user.getLastname());
        attributes.put(IDENTIFIER_ATTRIBUTE, user.getIdentifier());
        attributes.put(OTP_ATTRIBUTE, user.isOtp());
        attributes.put(SUBROGEABLE_ATTRIBUTE, user.isSubrogeable());
        attributes.put(LANGUAGE_ATTRIBUTE, user.getLanguage());
        attributes.put(PHONE_ATTRIBUTE, user.getPhone());
        attributes.put(MOBILE_ATTRIBUTE, user.getMobile());
        attributes.put(STATUS_ATTRIBUTE, user.getStatus());
        attributes.put(TYPE_ATTRIBUTE, user.getType());
        attributes.put(READONLY_ATTRIBUTE, user.isReadonly());
        attributes.put(LEVEL_ATTRIBUTE, user.getLevel());
        attributes.put(LAST_CONNECTION_ATTRIBUTE, user.getLastConnection());
        attributes.put(NB_FAILED_ATTEMPTS_ATTRIBUTE, user.getNbFailedAttempts());
        attributes.put(PASSWORD_EXPIRATION_DATE_ATTRIBUTE, user.getPasswordExpirationDate());
        attributes.put(GROUP_ID_ATTRIBUTE, user.getGroupId());
        attributes.put(ADDRESS_ATTRIBUTE, new CasJsonWrapper(user.getAddress()));
        if (finalSurrogationCall) {
            attributes.put(SUPER_USER_ATTRIBUTE, superUsername);
            final UserDto superUser = casExternalRestClient.getUserByEmail(utils.buildContext(superUsername), superUsername, Optional.empty());
            attributes.put(SUPER_USER_IDENTIFIER_ATTRIBUTE, superUser.getIdentifier());

        }
        if (user instanceof AuthUserDto) {
            final AuthUserDto authUser = (AuthUserDto) user;
            attributes.put(PROFILE_GROUP_ATTRIBUTE, new CasJsonWrapper(authUser.getProfileGroup()));
            attributes.put(CUSTOMER_IDENTIFIER_ATTRIBUTE, authUser.getCustomerIdentifier());
            attributes.put(BASIC_CUSTOMER_ATTRIBUTE, new CasJsonWrapper(authUser.getBasicCustomer()));
            attributes.put(AUTHTOKEN_ATTRIBUTE, authUser.getAuthToken());
            attributes.put(PROOF_TENANT_ID_ATTRIBUTE, authUser.getProofTenantIdentifier());
            attributes.put(TENANTS_BY_APP_ATTRIBUTE, new CasJsonWrapper(authUser.getTenantsByApp()));
            final Set<String> roles = new HashSet<>();
            final List<ProfileDto> profiles = authUser.getProfileGroup().getProfiles();
            profiles.forEach(profile -> profile.getRoles().forEach(role -> roles.add(role.getName())));
            attributes.put(ROLES_ATTRIBUTE, new ArrayList(roles));
        }
        return principalFactory.createPrincipal(user.getId(), attributes);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential || credential instanceof ClientCredential;
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        return null;
    }
}
