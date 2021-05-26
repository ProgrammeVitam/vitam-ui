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
package fr.gouv.vitamui.iam.internal.server.externalparamprofile.converter;

import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ExternalParamProfileConverter {

    public static final String EXTERNAL_PARAM_IDENTIFIER = "externalParamIdentifier";

    public static final String ID_EXTERNAL_PARAM = "idExternalParam";

    public static final String PROFILE_IDENTIFIER = "profileIdentifier";

    public static final String ID_PROFILE = "idProfile";

    public static final String NAME_KEY = "Nom";

    public static final String ACCESS_CONTRACT = "AccessContract";

    public static final String PARAMETER_VALUE_KEY = "Valeur";

    public static final String DESCRIPTION = "Description";

    public static final String ENABLED_KEY = "Activé";

    public static final String DATE_TIME = "DateTime";

    public ExternalParamProfileConverter() {
        // default constructor
    }

    public String convertToLogbook(final ExternalParamProfileDto dto) {
        final Map<String, String> logbookData = new LinkedHashMap<>();
        logbookData.put(EXTERNAL_PARAM_IDENTIFIER, LogbookUtils.getValue(dto.getExternalParamIdentifier()));
        logbookData.put(ID_EXTERNAL_PARAM, LogbookUtils.getValue(dto.getIdExternalParam()));
        logbookData.put(PROFILE_IDENTIFIER, LogbookUtils.getValue(dto.getProfileIdentifier()));
        logbookData.put(ID_PROFILE, LogbookUtils.getValue(dto.getIdProfile()));
        logbookData.put(NAME_KEY, LogbookUtils.getValue(dto.getName()));
        logbookData.put(DESCRIPTION, LogbookUtils.getValue(dto.getDescription()));
        logbookData.put(ENABLED_KEY, LogbookUtils.getValue(dto.isEnabled()));
        logbookData.put(ACCESS_CONTRACT, LogbookUtils.getValue(dto.getAccessContract()));
        logbookData.put(DATE_TIME, LogbookUtils.getValue(dto.getDateTime()));
        return ApiUtils.toJson(logbookData);
    }

}
