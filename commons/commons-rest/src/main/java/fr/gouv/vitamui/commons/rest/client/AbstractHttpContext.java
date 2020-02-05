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

import static fr.gouv.vitamui.commons.api.CommonConstants.X_TENANT_ID_HEADER;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The context of an internal REST call (to the security API).
 *
 *
 */
@Getter
@EqualsAndHashCode
public abstract class AbstractHttpContext implements Serializable {

    private static final long serialVersionUID = 5708806786058213869L;

    private final Integer tenantIdentifier;

    private final String userToken;

    private final String applicationId;

    private final String identity;

    private final String requestId;

    private final String accessContract;

    // @formatter:off
    private static final List<String> CALLS_WITHOUT_TENANT_ID = Arrays.asList(
        "/actuator/health", "/actuator/prometheus", "/error/", "/favicon.ico",
        "/swagger-resources", "/swagger-ui.html", "/v2/api-docs", "/webjars"
    );
    // @formatter:on

    public AbstractHttpContext(final Integer tenantIdentifier, final String userToken, final String applicationId,
            final String identity, final String requestId, final String accessContract) {
        this.identity = identity;
        this.tenantIdentifier = tenantIdentifier;
        this.userToken = userToken;
        this.requestId = requestId;
        this.applicationId = applicationId;
        this.accessContract = accessContract;

        initMDC();
    }

    /**
     * Init variable for logg
     */
    private void initMDC() {
        MDC.put(CommonConstants.X_REQUEST_ID_HEADER, requestId);
        MDC.put(CommonConstants.X_APPLICATION_ID_HEADER, applicationId);
    }

    public static Integer getTenantIdentifier(final String tenant, final String url) {

        try {
            if (StringUtils.isNotBlank(tenant)) {
                return Integer.valueOf(tenant);
            }
        }
        catch (final NumberFormatException e) {
            throw new BadRequestException(String.format("%s header : Integer type was expected, instead value was %s. ",
                    X_TENANT_ID_HEADER, tenant));
        }

        if (urlNeedsTenantIdHeader(url)) {
            throw new InvalidAuthenticationException(CommonConstants.X_TENANT_ID_HEADER + " header is mandatory.");
        }

        return -1;
    }

    /**
     * Checks if the URL needs a tenantIdentifier, using our whitelist.
     * @param url
     * @return
     */
    public static boolean urlNeedsTenantIdHeader(final String url) {
        return CALLS_WITHOUT_TENANT_ID.stream().noneMatch(whitelist -> StringUtils.isNotEmpty(url)
                && (whitelist.equalsIgnoreCase(url) || url.contains(whitelist)));
    }

    @Override
    public String toString() {
        return "AbstractHttpContext(tenantIdentifier=" + tenantIdentifier + ", userToken(truncated)="
                + StringUtils.substring(userToken, 0, StringUtils.length(userToken) / 2) + "****, applicationId= "
                + applicationId + ", identity=" + identity + ", requestId=" + requestId + ", accessContract="
                + accessContract + ")";
    }
}
