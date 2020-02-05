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

import javax.servlet.http.HttpServletRequest;

import fr.gouv.vitamui.commons.api.CommonConstants;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InternalHttpContext extends AbstractHttpContext {

    private static final long serialVersionUID = -7426487061479187920L;

    private final String userLevel;

    private final String customerId;

    public InternalHttpContext(final Integer tenantIdentifier, final String userToken, final String customerId, final String userLevel,
            final String applicationId, final String identity, final String requestId, final String accessContract) {
        super(tenantIdentifier, userToken, applicationId, identity, requestId, accessContract);
        this.userLevel = userLevel;
        this.customerId = customerId;
    }

    public static InternalHttpContext buildFromExternalHttpContext(final ExternalHttpContext externalHttpContext) {

        final InternalHttpContext internalHttpContext = new InternalHttpContext(externalHttpContext.getTenantIdentifier(), externalHttpContext.getUserToken(),
                null, null, externalHttpContext.getApplicationId(), externalHttpContext.getIdentity(), externalHttpContext.getRequestId(),
                externalHttpContext.getAccessContract());

        return internalHttpContext;
    }

    public static InternalHttpContext buildFromExternalHttpContext(final ExternalHttpContext externalHttpContext, final String customerId,
            final String userLevel) {

        final InternalHttpContext internalHttpContext = new InternalHttpContext(externalHttpContext.getTenantIdentifier(), externalHttpContext.getUserToken(),
                customerId, userLevel, externalHttpContext.getApplicationId(), externalHttpContext.getIdentity(), externalHttpContext.getRequestId(),
                externalHttpContext.getAccessContract());

        return internalHttpContext;
    }

    public static InternalHttpContext buildFromRequest(final HttpServletRequest request) {
        final Integer tenantIdentifier = getTenantIdentifier(request.getHeader(CommonConstants.X_TENANT_ID_HEADER), request.getRequestURI());
        final String userToken = request.getHeader(CommonConstants.X_USER_TOKEN_HEADER);
        final String applicationId = request.getHeader(CommonConstants.X_APPLICATION_ID_HEADER);
        final String identity = request.getHeader(CommonConstants.X_IDENTITY_HEADER);
        final String requestId = request.getHeader(CommonConstants.X_REQUEST_ID_HEADER);
        final String accessContract = request.getHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER);
        final String customerId = request.getHeader(CommonConstants.X_CUSTOMER_ID_HEADER);
        final String userLevel = request.getHeader(CommonConstants.X_USER_LEVEL_HEADER);
        return new InternalHttpContext(tenantIdentifier, userToken, customerId, userLevel, applicationId, identity, requestId, accessContract);
    }

}
