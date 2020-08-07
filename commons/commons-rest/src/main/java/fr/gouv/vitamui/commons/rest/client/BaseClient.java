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
package fr.gouv.vitamui.commons.rest.client;

import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A REST client to check existence, read, created, update and delete an object with identifier.
 *
 *
 */
@EqualsAndHashCode
@ToString
public abstract class BaseClient<C extends AbstractHttpContext> implements RestClient {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(BaseClient.class);

    protected String baseUrl;

    public BaseClient(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    protected MultiValueMap<String, String> buildHeaders(final AbstractHttpContext context) {
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Assert.notNull(context, "The call context cannot be null");
        if (context instanceof InternalHttpContext) {
            buildHeaders((InternalHttpContext) context, headers);
        } else if (context instanceof ExternalHttpContext) {
            buildHeaders((ExternalHttpContext) context, headers);
        } else {
            LOGGER.warn("Not implemented for type {}", context.getClass());
        }
        return headers;
    }

    private void buildHeaders(final InternalHttpContext context, final MultiValueMap<String, String> headers) {
        buildCommonHeaders(context, headers);
        buildHeadersInternal(context, headers);
    }

    private void buildHeaders(final ExternalHttpContext context, final MultiValueMap<String, String> headers) {
        buildCommonHeaders(context, headers);
        buildHeadersExternal(context, headers);
    }

    private void buildHeadersExternal(final ExternalHttpContext context, final MultiValueMap<String, String> headers) {

    }

    private void buildCommonHeaders(final AbstractHttpContext context, final MultiValueMap<String, String> headers) {
        final Integer tenantIdentifier = context.getTenantIdentifier();
        final String userToken = context.getUserToken();
        final String applicationId = context.getApplicationId();
        final String identity = context.getIdentity();
        final String requestId = context.getRequestId();
        final String accessContractId = context.getAccessContract();
        if (tenantIdentifier != null) {
            headers.put(CommonConstants.X_TENANT_ID_HEADER,
                    Collections.singletonList(String.valueOf(tenantIdentifier)));
        }
        if (userToken != null) {
            headers.put(CommonConstants.X_USER_TOKEN_HEADER, Collections.singletonList(userToken));
        }
        if (applicationId != null) {
            headers.put(CommonConstants.X_APPLICATION_ID_HEADER, Collections.singletonList(applicationId));
        }
        if (identity != null) {
            headers.put(CommonConstants.X_IDENTITY_HEADER, Collections.singletonList(identity));
        }
        if (requestId != null) {
            headers.put(CommonConstants.X_REQUEST_ID_HEADER, Collections.singletonList(requestId));
        }
        if (accessContractId != null) {
            headers.put(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER, Collections.singletonList(accessContractId));
        }
    }

    private void buildHeadersInternal(final InternalHttpContext context, final MultiValueMap<String, String> headers) {
        final String customerId = context.getCustomerId();
        final String userLevel = context.getUserLevel();
        if (userLevel != null) {
            headers.put(CommonConstants.X_USER_LEVEL_HEADER, Collections.singletonList(userLevel));
        }
        if (customerId != null) {
            headers.put(CommonConstants.X_CUSTOMER_ID_HEADER, Collections.singletonList(customerId));
        }
    }

    protected void checkResponse(final ResponseEntity response, final Integer... acceptedStatus) {
        Assert.notNull(response, "The server response cannot be null");
        final int responseStatus = response.getStatusCodeValue();
        final List<Integer> status;
        if (acceptedStatus == null || acceptedStatus.length == 0) {
            status = Arrays.asList(200);
        } else {
            status = Arrays.asList(acceptedStatus);
        }
        if (!status.contains(responseStatus)) {
            throw new InternalServerException("HTTP error: " + responseStatus);
        }
    }

    protected String getUrl() {
        if (baseUrl != null) {
            return baseUrl + getPathUrl();
        } else {
            return getPathUrl();
        }
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Method for get UriBuilder from Url
     * @return
     */
    protected URIBuilder getUriBuilderFromUrl() {
        try {
            return new URIBuilder(getUrl());
        }
        catch (final URISyntaxException e) {
            throw new ApplicationServerException(e.getMessage());
        }
    }

    /**
     * Method allowing to generate an URI builder.
     * @param url Url to reach.
     * @return The linked builder.
     */
    protected URIBuilder getUriBuilder(final String url) {
        try {
            return new URIBuilder(url);
        }
        catch (final URISyntaxException exception) {
            throw new ApplicationServerException(exception.getMessage(), exception);
        }
    }

    /**
     * Method for get UriBuilder from Url
     * @return
     */
    protected URIBuilder getUriBuilderFromPath(final String path) {
        try {
            return new URIBuilder(getUrl() + path);
        }
        catch (final URISyntaxException e) {
            throw new ApplicationServerException(e.getMessage());
        }
    }

    protected URI buildUriBuilder(final URIBuilder builder) {
        try {
            return builder.build();
        }
        catch (final URISyntaxException e) {
            throw new ApplicationServerException(e.getMessage());
        }
    }

}
