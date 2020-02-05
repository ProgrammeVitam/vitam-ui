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
package fr.gouv.vitamui.iam.internal.server.group.converter;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupConverter implements Converter<GroupDto, Group> {

    public static final String ENABLED_KEY = "Activé";

    public static final String NAME_KEY = "Nom";

    public static final String LEVEL_KEY = "Niveau";

    public static final String DESCRIPTION_KEY = "Description";

    public static final String PROFILE_IDS_KEY = "Liste des profils";

    private final ProfileRepository profileRepository;

    public GroupConverter(final ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public String convertToLogbook(final GroupDto dto) {
        final Map<String, String> logbookData = new LinkedHashMap<>();
        logbookData.put(NAME_KEY, LogbookUtils.getValue(dto.getName()));
        logbookData.put(DESCRIPTION_KEY, LogbookUtils.getValue(dto.getDescription()));
        logbookData.put(LEVEL_KEY, LogbookUtils.getValue(dto.getLevel()));
        logbookData.put(ENABLED_KEY, LogbookUtils.getValue(dto.isEnabled()));
        logbookData.put(PROFILE_IDS_KEY, convertProfileIdsToLogbook(dto.getProfileIds()));
        return ApiUtils.toJson(logbookData);
    }

    @Override
    public Group convertDtoToEntity(final GroupDto dto) {
        return VitamUIUtils.copyProperties(dto, new Group());
    }

    @Override
    public GroupDto convertEntityToDto(final Group entity) {
        return VitamUIUtils.copyProperties(entity, new GroupDto());
    }

    public String convertProfileIdsToLogbook(final Collection<String> profileIds) {
        Iterable<Profile> profiles = profileRepository.findAllById(profileIds);
        List<Integer> ids = StreamSupport.stream(profiles.spliterator(), false)
                .map(p -> Integer.parseInt(p.getIdentifier())).collect(Collectors.toList());
        ids.sort(Comparator.naturalOrder());
        return ids.toString();
    }
}
