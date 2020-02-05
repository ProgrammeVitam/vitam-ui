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
package fr.gouv.vitamui.cas.mfa.authentication;

import javax.servlet.http.HttpServletRequest;

import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Check if the MFA SMS authentication should be bypassed, depending on the IAM WS.
 *
 *
 */
public class IamMultifactorAuthenticationProviderBypass extends DefaultMultifactorAuthenticationProviderBypass {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IamMultifactorAuthenticationProviderBypass.class);

    @Autowired
    private IdentityProviderHelper identityProviderHelper;

    @Autowired
    private ProvidersService providersService;

    @Autowired
    private Utils utils;

    public IamMultifactorAuthenticationProviderBypass(final MultifactorAuthenticationProviderBypassProperties prop) {
        super(prop);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication,
            final RegisteredService registeredService, final MultifactorAuthenticationProvider provider,
            final HttpServletRequest request) {
        try {
            final UserDto user = utils.getRealUser(authentication);
            if (user != null) {

                final String email = user.getEmail();
                final boolean mfa = user.isOtp();
                LOGGER.debug("bypassing for (super)user: {}? -> mfa: {}", email, mfa);

                if (mfa && !identityProviderHelper.identifierMatchProviderPattern(providersService.getProviders(), email)) {
                    LOGGER.debug("The user: {} requests MFA but is internal: MFA is ABORTED!", email);
                    return false;
                }

                return mfa;
            }
        }
        catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return super.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request);
    }
}
