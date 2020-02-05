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
package fr.gouv.vitamui.iam.internal.server.subrogation.converter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;

public class SubrogationConverter implements Converter<SubrogationDto, Subrogation> {

    public static final String SURROGATE_KEY = "surrogate";

    public static final String SUPER_USER_KEY = "superUser";

    private final UserRepository userRepository;

    public SubrogationConverter(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String convertToLogbook(final SubrogationDto dto) {
        final Map<String, String> logbookData = new HashMap<>();
        logbookData.put(SURROGATE_KEY, getUserId(dto.getSurrogate()));
        logbookData.put(SUPER_USER_KEY, getUserId(dto.getSuperUser()));
        return ApiUtils.toJson(logbookData);
    }

    private String getUserId(final String email) {
        final User u = userRepository.findByEmail(email);
        return u.getIdentifier();
    }

    @Override
    public Subrogation convertDtoToEntity(final SubrogationDto dto) {
        final Subrogation entity = new Subrogation();
        VitamUIUtils.copyProperties(dto, entity);
        if (dto != null && dto.getDate() != null) {
            entity.setDate(Date.from(dto.getDate().toInstant()));
        }
        return entity;
    }

    @Override
    public SubrogationDto convertEntityToDto(final Subrogation entity) {
        final SubrogationDto dto = new SubrogationDto();
        VitamUIUtils.copyProperties(entity, dto);
        if (entity != null && entity.getDate() != null) {
            dto.setDate(OffsetDateTime.ofInstant(entity.getDate().toInstant(), ZoneOffset.UTC));
        }
        return dto;
    }
}
