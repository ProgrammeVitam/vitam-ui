package fr.gouv.vitamui.iam.internal.server.user.converter;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.ConnectionHistoryDto;
import fr.gouv.vitamui.iam.internal.server.user.domain.ConnectionHistory;

public class ConnectionHistoryConverter implements Converter<ConnectionHistoryDto, ConnectionHistory> {

    @Override
    public String convertToLogbook(ConnectionHistoryDto dto) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ConnectionHistory convertDtoToEntity(ConnectionHistoryDto dto) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ConnectionHistoryDto convertEntityToDto(ConnectionHistory entity) {
        return VitamUIUtils.copyProperties(entity, new ConnectionHistoryDto());
    }
}
