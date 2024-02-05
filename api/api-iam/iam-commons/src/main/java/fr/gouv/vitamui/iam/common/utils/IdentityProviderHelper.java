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
package fr.gouv.vitamui.iam.common.utils;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper to work with identity providers.
 */
public class IdentityProviderHelper {

    public Optional<IdentityProviderDto> findByTechnicalName(final List<IdentityProviderDto> providers, final String name) {
        for (final IdentityProviderDto provider : providers) {
            if (StringUtils.equals(provider.getTechnicalName(), name)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }


    public List<IdentityProviderDto> findByUserIdentifier(final List<IdentityProviderDto> providers, final String identifier) {
        List<IdentityProviderDto> list = new ArrayList<>();
        List<IdentityProviderDto> list2 = providers.stream()
            .filter(provider -> provider.getPatterns().stream().anyMatch(pattern -> Pattern.compile(pattern).matcher(identifier).matches()))
            .collect(Collectors.toList());
        for (final IdentityProviderDto provider : providers) {
            for (final String pattern : provider.getPatterns()) {
                if (Pattern.compile(pattern).matcher(identifier).matches()) {
                    list.add(provider);
                }
            }
        }
        return list;
    }

    public IdentityProviderDto findByUserIdentifierAndCustomerId(final List<IdentityProviderDto> providers, final String identifier, final String customerId) {
        List<IdentityProviderDto> list = providers.stream()
            .filter(provider -> StringUtils.equals(provider.getCustomerId(), customerId))
            .filter(provider -> provider.getPatterns().stream().anyMatch(pattern -> Pattern.compile(pattern).matcher(identifier).matches()))
            .collect(Collectors.toList());
        if (list.size() > 1) {
            throw new IllegalStateException("Multiple providers found for one organisation and one user...");
        }
        if (list.isEmpty()) {
            throw new IllegalStateException("No providers found for one organisation and one user...");
        }
        return list.get(0);
    }

    public boolean identifierMatchProviderPattern(final List<IdentityProviderDto> providers, final String identifier) {
        List<IdentityProviderDto> providersMatching = findByUserIdentifier(providers, identifier);
        return providersMatching.stream().anyMatch(IdentityProviderDto::getInternal);
    }

}
