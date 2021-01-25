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
package fr.gouv.vitamui.iam.internal.server.user.converter;

import static fr.gouv.vitamui.commons.api.CommonConstants.GPDR_DEFAULT_VALUE;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.AnalyticsDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserConverter implements Converter<UserDto, User> {

    public static final String STATUS_KEY = "Statut";

    public static final String LASTNAME_KEY = "Nom";

    public static final String FIRSTNAME_KEY = "Prénom";

    public static final String EMAIL_KEY = "Email";

    public static final String LANGUAGE_KEY = "Langue";

    public static final String MOBILE_KEY = "Numéro mobile";

    public static final String PHONE_KEY = "Numéro fixe";

    public static final String LEVEL_KEY = "Niveau";

    public static final String TYPE_KEY = "Type";

    public static final String SUBROGEABLE_KEY = "Subrogeable";

    public static final String OTP_KEY = "OTP";

    public static final String GROUP_IDENTIFIER_KEY = "Groupe de profils";

    public static final String DISABLING_DATE = "Date de désactivation";

    public static final String REMOVING_DATE = "Date de suppression";

    public static final String INTERNAL_CODE_KEY = "Code interne";

    /**
     * Used for described user's blocked duration
     */
    public static final String BLOCKED_DURATION = "Durée du blocage";

    public static final String SITE_CODE = "Code du site";

    private final GroupRepository groupRepository;

    private final AddressConverter addressConverter;

    public UserConverter(final GroupRepository groupRepository, final AddressConverter addressConverter) {
        this.groupRepository = groupRepository;
        this.addressConverter = addressConverter;
    }

    @Override
    public String convertToLogbook(final UserDto user) {
        final Map<String, String> userLogbookData = new LinkedHashMap<>();
        userLogbookData.put(LASTNAME_KEY, GPDR_DEFAULT_VALUE);
        userLogbookData.put(FIRSTNAME_KEY, GPDR_DEFAULT_VALUE);
        userLogbookData.put(EMAIL_KEY, GPDR_DEFAULT_VALUE);
        userLogbookData.put(LANGUAGE_KEY, LogbookUtils.getValue(user.getLanguage()));
        userLogbookData.put(MOBILE_KEY, GPDR_DEFAULT_VALUE);
        userLogbookData.put(PHONE_KEY, GPDR_DEFAULT_VALUE);
        userLogbookData.put(TYPE_KEY, LogbookUtils.getValue(user.getType().toString()));
        userLogbookData.put(STATUS_KEY, LogbookUtils.getValue(user.getStatus().toString()));
        userLogbookData.put(SUBROGEABLE_KEY, LogbookUtils.getValue(user.isSubrogeable()));
        userLogbookData.put(INTERNAL_CODE_KEY, LogbookUtils.getValue(user.getInternalCode()));
        userLogbookData.put(OTP_KEY, LogbookUtils.getValue(user.isOtp()));
        userLogbookData.put(DISABLING_DATE, LogbookUtils.getValue(user.getDisablingDate()));
        userLogbookData.put(REMOVING_DATE, LogbookUtils.getValue(user.getRemovingDate()));
        userLogbookData.put(SITE_CODE, LogbookUtils.getValue(user.getSiteCode()));
        AddressDto address = new AddressDto(GPDR_DEFAULT_VALUE, GPDR_DEFAULT_VALUE, GPDR_DEFAULT_VALUE, GPDR_DEFAULT_VALUE);
        addressConverter.addAddress(address, userLogbookData);
        Optional<Group> group = groupRepository.findById(user.getGroupId());
        group.ifPresent(g -> userLogbookData.put(GROUP_IDENTIFIER_KEY, g.getIdentifier()));
        return ApiUtils.toJson(userLogbookData);
    }

    @Override
    public User convertDtoToEntity(final UserDto dto) {
        final User user = new User();
        VitamUIUtils.copyProperties(dto, user);
        user.setLastname(StringUtils.upperCase(dto.getLastname()));
        user.setFirstname(StringUtils.capitalize(dto.getFirstname()));
        if (dto.getAddress() != null) {
            user.setAddress(VitamUIUtils.copyProperties(dto.getAddress(), new Address()));
        }
        return user;
    }

    @Override
    public UserDto convertEntityToDto(final User user) {
        final UserDto userDto = new UserDto();
        VitamUIUtils.copyProperties(user, userDto);
        if (user.getAddress() != null) {
            userDto.setAddress(VitamUIUtils.copyProperties(user.getAddress(), new AddressDto()));
        }
        if (user.getAnalytics() != null) {
            userDto.setAnalytics(VitamUIUtils.copyProperties(user.getAnalytics(), new AnalyticsDto()));
        }
        return userDto;
    }
}
