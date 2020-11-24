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

import java.util.List;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationService;
import org.apereo.cas.services.ServicesManager;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.val;

import lombok.val;

/**
 * Specific surrogate service based on the IAM API.
 *
 *
 */
public class IamSurrogateAuthenticationService extends BaseSurrogateAuthenticationService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IamSurrogateAuthenticationService.class);

    private final CasExternalRestClient casExternalRestClient;

    private final Utils utils;

    public IamSurrogateAuthenticationService(final CasExternalRestClient casExternalRestClient,
                                             final ServicesManager servicesManager, final Utils utils) {
        super(servicesManager);
        this.casExternalRestClient = casExternalRestClient;
        this.utils = utils;
    }

    @Override
    public boolean canAuthenticateAsInternal(final String surrogate, final Principal principal, final Service service) {
        val id = (String) principal.getAttributes().get(UserPrincipalResolver.SUPER_USER_ID_ATTRIBUTE).get(0);
        boolean canAuthenticate = false;
        try {
            val subrogations = casExternalRestClient.getSubrogationsBySuperUserId(utils.buildContext(id), id);
            canAuthenticate = subrogations
                .stream()
                .filter(s -> s.getStatus() == SubrogationStatusEnum.ACCEPTED)
                .anyMatch(s -> s.getSurrogate().equals(surrogate));
        } catch (final VitamUIException e) {
            LOGGER.error("Cannot retrieve subrogations: {}", id, e);
        }
        LOGGER.debug("{} can surrogate: {}? -> {}", id, surrogate, canAuthenticate);
        return canAuthenticate;
    }

    @Override
    public List<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        throw new UnsupportedOperationException("Not allowed to choose the surrogate");
    }
}
