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
import fr.gouv.vitamui.commons.rest.client.HttpContextHolder;
import fr.gouv.vitamui.iam.openapiclient.invoker.ApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class IamApiClient extends ApiClient {

    public IamApiClient(RestClient restClient) {
        super(restClient);
    }

    @Override
    protected void updateParamsForAuth(
        String[] authNames,
        MultiValueMap<String, String> queryParams,
        HttpHeaders headerParams,
        MultiValueMap<String, String> cookieParams
    ) {
        resolveContext()
            .ifPresentOrElse(
                context -> {
                    applyHeaders(context, headerParams);
                    log.debug("IAM headers applied. Context={}", context);
                },
                () -> log.warn("No HttpContext available for authNames={}", Arrays.toString(authNames))
            );
    }

    private Optional<HttpContext> resolveContext() {
        return resolveFromHolder().or(this::resolveFromSecurityContext);
    }

    /**
     * First priority: context manually set in HttpContextHolder.
     */
    private Optional<HttpContext> resolveFromHolder() {
        return HttpContextHolder.get();
    }

    /**
     * Fallback: try to resolve HttpContext from Spring Security.
     */
    private Optional<HttpContext> resolveFromSecurityContext() {
        Authentication authentication = Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .orElse(null);

        if (authentication == null) {
            return Optional.empty();
        }

        // Special case: initial usersApi.getMe() call during authentication
        if (
            authentication instanceof PreAuthenticatedAuthenticationToken &&
            authentication.getPrincipal() instanceof HttpContext context
        ) {
            return Optional.of(context);
        }

        // Standard case: HttpContext stored in credentials
        if (authentication.getCredentials() instanceof HttpContext context) {
            return Optional.of(context);
        }

        return Optional.empty();
    }

    /**
     * Apply IAM-related headers based on the resolved HttpContext.
     */
    private void applyHeaders(HttpContext context, HttpHeaders headers) {
        headers.set(CommonConstants.X_ORIGIN_HEADER_NAME, CommonConstants.X_ORIGIN_HEADER_INTERNAL);

        putIfNotNull(
            headers,
            CommonConstants.X_TENANT_ID_HEADER,
            () -> Optional.ofNullable(context.getTenantIdentifier()).map(String::valueOf).orElse(null)
        );

        putIfNotNull(headers, CommonConstants.X_USER_TOKEN_HEADER, context::getUserToken);

        putIfNotNull(headers, CommonConstants.X_APPLICATION_ID_HEADER, context::getApplicationId);

        putIfNotNull(headers, CommonConstants.X_IDENTITY_HEADER, context::getIdentity);

        putIfNotNull(headers, CommonConstants.X_REQUEST_ID_HEADER, context::getRequestId);

        putIfNotNull(headers, CommonConstants.X_ACCESS_CONTRACT_ID_HEADER, context::getAccessContract);
    }

    /**
     * Add header only if the supplied value is not null.
     */
    private void putIfNotNull(HttpHeaders headers, String headerName, Supplier<String> valueSupplier) {
        String value = valueSupplier.get();
        if (value != null) {
            headers.set(headerName, value);
        }
    }

    @Override
    protected void addHeadersToRequest(HttpHeaders headers, RestClient.RequestBodySpec requestBuilder) {
        try {
            java.util.Set<java.util.Map.Entry<String, java.util.List<String>>> entries;
            try {
                java.lang.reflect.Method headerSetMethod = HttpHeaders.class.getMethod("headerSet");
                entries = (java.util.Set<java.util.Map.Entry<String, java.util.List<String>>>) headerSetMethod.invoke(
                    headers
                );
            } catch (NoSuchMethodException e) {
                java.lang.reflect.Method entrySetMethod = HttpHeaders.class.getMethod("entrySet");
                entries = (java.util.Set<java.util.Map.Entry<String, java.util.List<String>>>) entrySetMethod.invoke(
                    headers
                );
            }
            for (java.util.Map.Entry<String, java.util.List<String>> entry : entries) {
                java.util.List<String> values = entry.getValue();
                for (String value : values) {
                    if (value != null) {
                        requestBuilder.header(entry.getKey(), value);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to add headers to request", e);
        }
    }
}
