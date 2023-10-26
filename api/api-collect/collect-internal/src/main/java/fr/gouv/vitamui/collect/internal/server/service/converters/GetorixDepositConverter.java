package fr.gouv.vitamui.collect.internal.server.service.converters;

import fr.gouv.vitamui.collect.common.dto.GetorixDepositDto;
import fr.gouv.vitamui.collect.internal.server.domain.GetorixDepositModel;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;


@Getter
@Setter
public class GetorixDepositConverter implements Converter<GetorixDepositDto, GetorixDepositModel> {
    @Override
    public String convertToLogbook(GetorixDepositDto dto) {
        final Map<String, String> data = new LinkedHashMap<>();
        return ApiUtils.toJson(data);
    }

    @Override
    public GetorixDepositModel convertDtoToEntity(GetorixDepositDto dto) {
        return VitamUIUtils.copyProperties(dto, new GetorixDepositModel());
    }

    @Override
    public GetorixDepositDto convertEntityToDto(GetorixDepositModel entity) {
        return VitamUIUtils.copyProperties(entity, new GetorixDepositDto());
    }
}
