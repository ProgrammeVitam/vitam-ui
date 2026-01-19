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

package fr.gouv.vitamui.iam.openapiclient;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.rest.client.HttpContext;
import fr.gouv.vitamui.iam.openapiclient.invoker.ApiClient;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public class IamApiClient extends ApiClient {

    public IamApiClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected void updateParamsForAuth(
        String[] authNames,
        MultiValueMap<String, String> queryParams,
        HttpHeaders headerParams,
        MultiValueMap<String, String> cookieParams
    ) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            final HttpContext context;
            if (
                authentication instanceof PreAuthenticatedAuthenticationToken &&
                authentication.getPrincipal() instanceof HttpContext
            ) {
                // Needed for the initial call to usersApi.getMe() during authentication
                context = (HttpContext) authentication.getPrincipal();
            } else if (authentication.getCredentials() instanceof HttpContext) {
                // The other calls get normal authentication credentials
                context = (HttpContext) authentication.getCredentials();
            } else {
                context = null;
            }
            if (context != null) {
                updateParamsForAuth(headerParams, context);
            }
        }
    }

    private void updateParamsForAuth(HttpHeaders headerParams, HttpContext context) {
        final Integer tenantIdentifier = context.getTenantIdentifier();
        final String userToken = context.getUserToken();
        final String applicationId = context.getApplicationId();
        final String identity = context.getIdentity();
        final String requestId = context.getRequestId();
        final String accessContractId = context.getAccessContract();
        headerParams.set(CommonConstants.X_ORIGIN_HEADER_NAME, CommonConstants.X_ORIGIN_HEADER_INTERNAL);
        if (tenantIdentifier != null) {
            headerParams.put(
                CommonConstants.X_TENANT_ID_HEADER,
                Collections.singletonList(String.valueOf(tenantIdentifier))
            );
        }
        if (userToken != null) {
            headerParams.put(CommonConstants.X_USER_TOKEN_HEADER, Collections.singletonList(userToken));
        }
        if (applicationId != null) {
            headerParams.put(CommonConstants.X_APPLICATION_ID_HEADER, Collections.singletonList(applicationId));
        }
        if (identity != null) {
            headerParams.put(CommonConstants.X_IDENTITY_HEADER, Collections.singletonList(identity));
        }
        if (requestId != null) {
            headerParams.put(CommonConstants.X_REQUEST_ID_HEADER, Collections.singletonList(requestId));
        }
        if (accessContractId != null) {
            headerParams.put(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER, Collections.singletonList(accessContractId));
        }
    }
}
