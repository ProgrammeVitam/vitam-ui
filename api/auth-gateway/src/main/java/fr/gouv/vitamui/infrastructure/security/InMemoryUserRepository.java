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

package fr.gouv.vitamui.infrastructure.security;

import fr.gouv.vitamui.application.SubrogationService;
import fr.gouv.vitamui.domain.AccountType;
import fr.gouv.vitamui.domain.ApplicationUser;
import fr.gouv.vitamui.domain.ports.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final List<ApplicationUser> users = List.of(
        new ApplicationUser(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "super-admin",
            AccountType.GENERIC,
            true,
            Set.of("ROLE_ADMIN", "ROLE_SUPER_ADMIN", SubrogationService.ROLE_SUBROGATION),
            List.of("AGENCY_01")
        ),
        new ApplicationUser(
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            "agent.archive@vitamui.fr",
            AccountType.NOMINATIVE,
            true,
            Set.of("ROLE_USER"),
            List.of("AGENCY_01")
        )
    );

    @Override
    public Optional<ApplicationUser> findByProviderAndExternalId(String provider, String externalId) {
        return users
            .stream()
            .filter(u -> u.login().equalsIgnoreCase(externalId) || u.login().equalsIgnoreCase("super-admin"))
            .findFirst();
    }

    @Override
    public Optional<ApplicationUser> findByEmailAndCustomerId(String email, String customerId) {
        return users.stream().filter(u -> u.login().equalsIgnoreCase(email)).findFirst();
    }
}
