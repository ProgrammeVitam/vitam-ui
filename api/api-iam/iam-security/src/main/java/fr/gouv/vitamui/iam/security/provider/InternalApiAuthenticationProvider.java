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
package fr.gouv.vitamui.iam.security.provider;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.authentication.InternalAuthentication;
import fr.gouv.vitamui.iam.security.service.InternalAuthentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * General authentication provider for the Internal API.
 *
 *
 */

public class InternalApiAuthenticationProvider implements AuthenticationProvider {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(InternalApiAuthenticationProvider.class);

    private final InternalAuthentificationService internalAuthentificationService;

    /**
     * InternalApiAuthenticationProvider.
     * @param internalAuthentificationService
     */
    @Autowired
    public InternalApiAuthenticationProvider(final InternalAuthentificationService internalAuthentificationService) {
        this.internalAuthentificationService = internalAuthentificationService;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) {
        if (supports(authentication.getClass())) {
            final PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authentication;
            final InternalHttpContext principal = (InternalHttpContext) token.getPrincipal();
            LOGGER.debug("Principal: {}", principal);

            if (principal != null) {
                final AuthUserDto userProfile = internalAuthentificationService.getUserFromHttpContext(principal);
                return new InternalAuthentication(userProfile, principal);
            }
        }

        throw new BadCredentialsException("Unable to authenticate REST call");
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

}
