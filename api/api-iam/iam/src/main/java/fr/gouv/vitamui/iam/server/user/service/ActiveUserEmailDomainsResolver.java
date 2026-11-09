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

package fr.gouv.vitamui.iam.server.user.service;

import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves the email domains used by the active users of a customer. Kept separate from {@code CustomerService} and
 * {@code IdentityProviderService} so that both can reuse the same logic without depending on each other, which
 * would otherwise introduce a circular bean dependency (both services are involved in the creation of the user
 * services beans).
 */
public final class ActiveUserEmailDomainsResolver {

    /**
     * BLOCKED is considered active: it is a temporary status set after too many failed login attempts and the
     * account reverts to ENABLED on its own at the next successful login.
     */
    private static final Set<UserStatusEnum> ACTIVE_USER_STATUSES = EnumSet.of(
        UserStatusEnum.ENABLED,
        UserStatusEnum.BLOCKED
    );

    private ActiveUserEmailDomainsResolver() {}

    public static List<String> resolve(final UserRepository userRepository, final String customerId) {
        return userRepository
            .findByCustomerId(customerId)
            .stream()
            .filter(
                user -> ACTIVE_USER_STATUSES.contains(user.getStatus()) && StringUtils.contains(user.getEmail(), "@")
            )
            .map(user -> user.getEmail().substring(user.getEmail().indexOf('@') + 1).toLowerCase())
            .distinct()
            .collect(Collectors.toList());
    }
}
