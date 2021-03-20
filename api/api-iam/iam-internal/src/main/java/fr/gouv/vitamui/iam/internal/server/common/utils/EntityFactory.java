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
package fr.gouv.vitamui.iam.internal.server.common.utils;

import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;

import java.util.List;
import java.util.stream.Collectors;

public final class EntityFactory {

    private EntityFactory() {
    }

    public static Profile buildProfile(final String name, final String identifier, final String description,
        final boolean isReadonly, final String level, final Integer tenant, final String service,
        final List<String> roleNames, final String customerId) {

        final Profile profile = new Profile();
        profile.setIdentifier(identifier);
        profile.setName(name);
        profile.setDescription(description);
        profile.setReadonly(isReadonly);
        profile.setLevel(level);
        profile.setEnabled(true);
        profile.setTenantIdentifier(tenant);
        profile.setApplicationName(service);
        profile.setCustomerId(customerId);
        profile.setRoles(roleNames.stream().map(Role::new).collect(Collectors.toList()));
        return profile;
    }

    public static Profile buildProfile(final String name, final String identifier, final String description,
        final boolean isReadonly, final String level, final Integer tenant, final String service,
        final List<String> roleNames, final String customerId, final String externalParameterId) {

        final Profile profile =
            buildProfile(name, identifier, description, isReadonly, level, tenant, service, roleNames, customerId);
        profile.setExternalParamId(externalParameterId);
        return profile;
    }

    public static Group buildGroup(final String name, final String identifier, final String description,
        final boolean isReadonly, final String level, final List<Profile> profiles, final String customerId) {

        final Group group = new Group();
        group.setIdentifier(identifier);
        group.setCustomerId(customerId);
        group.setName(name);
        group.setDescription(description);
        group.setLevel(level);
        group.setEnabled(true);
        group.setProfileIds(profiles.stream().map(Profile::getId).collect(Collectors.toList()));
        return group;
    }

}
