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
package fr.gouv.vitamui.commons.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.UnAuthorizedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;

/**
 *
 * RestController of Application Front-End should extend this class
 *
 *
 */
public abstract class AbstractUiRestController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(AbstractUiRestController.class);

    protected AbstractUiRestController() {
        // do nothing
    }

    protected static HttpServletRequest getCurrentHttpRequest() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        throw new ApplicationServerException("Not called in the context of an HTTP request");
    }

    protected AuthUserDto getAuthenticatedUser() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            final AuthUserDto principal = (AuthUserDto) auth.getPrincipal();
            return principal;
        }
        throw new UnAuthorizedException("User is not connected");
    }

    protected ExternalHttpContext buildUiHttpContext() {
        final AuthUserDto principal = getAuthenticatedUser();
        final HttpServletRequest request = getCurrentHttpRequest();
        return ExternalHttpContext.buildFromUiRequest(request, principal);
    }

    protected ExternalHttpContext buildUiHttpContext(final Integer tenantIdentifier) {
        final AuthUserDto principal = getAuthenticatedUser();
        final HttpServletRequest request = getCurrentHttpRequest();
        return ExternalHttpContext.buildFromUiRequest(request, principal, tenantIdentifier, null);
    }

    protected ExternalHttpContext buildUiHttpContext(final Integer tenantIdentifier, final String accessContractId) {
        final AuthUserDto principal = getAuthenticatedUser();
        final HttpServletRequest request = getCurrentHttpRequest();
        return ExternalHttpContext.buildFromUiRequest(request, principal, tenantIdentifier, accessContractId);
    }
}
