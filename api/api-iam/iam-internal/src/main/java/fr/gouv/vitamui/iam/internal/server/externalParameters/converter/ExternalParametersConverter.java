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
package fr.gouv.vitamui.iam.internal.server.externalParameters.converter;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ParameterDto;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
@Setter
public class ExternalParametersConverter implements Converter<ExternalParametersDto, ExternalParameters> {

    public static final String ID_KEY = "Id";

    public static final String IDENTIFIER_KEY = "Identifiant";

    public static final String NAME_KEY = "Nom";

    public static final String PARAMETERS_KEY = "Paramètres";

    public static final String PARAMETER_KEY = "Clé";

    public static final String PARAMETER_VALUE_KEY = "Valeur";

    public ExternalParametersConverter() {
    }

    @Override
    public String convertToLogbook(final ExternalParametersDto dto) {
        final Map<String, String> logbookData = new LinkedHashMap<>();
        logbookData.put(ID_KEY, LogbookUtils.getValue(dto.getId()));
        logbookData.put(IDENTIFIER_KEY, LogbookUtils.getValue(dto.getIdentifier()));
        logbookData.put(NAME_KEY, LogbookUtils.getValue(dto.getName()));
        logbookData.put(PARAMETERS_KEY, convertParametersToLogbook(dto.getParameters()));
        return ApiUtils.toJson(logbookData);
    }

    @Override
    public ExternalParameters convertDtoToEntity(final ExternalParametersDto dto) {
        return VitamUIUtils.copyProperties(dto, new ExternalParameters());
    }

    @Override
    public ExternalParametersDto convertEntityToDto(final ExternalParameters entity) {
        return VitamUIUtils.copyProperties(entity, new ExternalParametersDto());
    }

    public String convertParametersToLogbook(final Collection<ParameterDto> parameterDtos) {
        List<String> parameters = StreamSupport.stream(parameterDtos.spliterator(), false).map(param -> {
            final Map<String, String> data = new LinkedHashMap<>();
            data.put(PARAMETER_KEY, param.getKey());
            data.put(PARAMETER_VALUE_KEY, param.getValue());
        	return ApiUtils.toJson(param);
        }).collect(Collectors.toList());
        return parameters.toString();
    }
}
