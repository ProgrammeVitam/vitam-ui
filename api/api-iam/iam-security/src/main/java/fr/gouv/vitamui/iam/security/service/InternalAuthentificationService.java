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
package fr.gouv.vitamui.iam.security.service;

import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.client.UserInternalRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Internal authentication service
 *
 *
 */

public class InternalAuthentificationService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(InternalAuthentificationService.class);

    private final UserInternalRestClient userInternalRestClient;

    @Autowired
    public InternalAuthentificationService(final UserInternalRestClient userInternalRestClient) {
        this.userInternalRestClient = userInternalRestClient;
    }

    /**
     * Retrieve user profile and check user security.
     * @param httpContext
     * @return
     */
    public AuthUserDto getUserFromHttpContext(final InternalHttpContext httpContext) {
        final String userToken = httpContext.getUserToken();
        if (userToken == null) {
            throw new BadCredentialsException("User token not found: " + userToken);
        }

        AuthUserDto userProfile;
        try {
            userProfile = userInternalRestClient.getMe(httpContext);
        }
        catch (final Exception ex) {
            LOGGER.error("Can't find User's Profile for {}. {}", httpContext, ex.getMessage());
            throw new BadCredentialsException(ex.getMessage());
        }

        if (userProfile == null) {
            throw new NotFoundException("User profile not found for token: " + userToken);
        }
        return userProfile;
    }
}
