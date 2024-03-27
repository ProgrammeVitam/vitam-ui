package fr.gouv.vitamui.iam.internal.server.user.service;

import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.ConnectionHistoryDto;
import fr.gouv.vitamui.iam.internal.server.user.converter.ConnectionHistoryConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.ConnectionHistoryRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.ConnectionHistory;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectionHistoryService {

    private final ConnectionHistoryRepository connectionHistoryRepository;
    private final ConnectionHistoryConverter connectionHistoryConverter;
    private final ConnectionHistoryExportService connectionHistoryExportService;
    private final InternalSecurityService securityService;
    private final UserInternalService userInternalService;

    @Autowired
    public ConnectionHistoryService(final ConnectionHistoryRepository connectionHistoryRepository,
                                    final ConnectionHistoryExportService exportService,
                                    final InternalSecurityService securityService,
                                    final @Lazy UserInternalService userInternalService) {
        this.connectionHistoryRepository = connectionHistoryRepository;
        this.connectionHistoryConverter = new ConnectionHistoryConverter();
        this.connectionHistoryExportService = exportService;
        this.securityService = securityService;
        this.userInternalService = userInternalService;
    }

    public void saveUserConnection(ConnectionHistory connectionHistory){
        connectionHistoryRepository.save(connectionHistory);
    }

    public void deleteByUserId(String userId){
        connectionHistoryRepository.deleteByUserId(userId);
    }

    public Resource exportConnectionHistory(Optional<String> optCriteria) {
        Map<String, String> mapCriteria = optCriteria.map(criteria -> (Map<String, String>) QueryDto.fromJson(criteria).getCriterionList().get(0).getValue())
            .orElseGet(Collections::emptyMap);

        Date start = Optional.ofNullable(mapCriteria.getOrDefault("start", null))
            .map(Instant::parse)
            .map(Date::from)
            .orElse(null);

        Date end = Optional.ofNullable(mapCriteria.getOrDefault("end", null))
            .map(Instant::parse)
            .map(Date::from)
            .orElse(null);

        Assert.notNull(start, "The  start date must not be null");
        Assert.notNull(end, "The end date must not be null");

        final AuthUserDto connectedUser = this.securityService.getUser();
        List<String> userIdsFromCustomer = this.userInternalService.findByCustomerId(connectedUser.getCustomerId()).stream().map(User::getIdentifier).collect(Collectors.toList());


        List<ConnectionHistoryDto> connectionHistoryDto =  findConnectionHistoryBetweenAndUserIds(start, end, userIdsFromCustomer);

        try {
            return connectionHistoryExportService.generateWorkbook(connectionHistoryDto);
        } catch (IOException e) {
            throw new InternalServerException("An error occurred while creating Users Connection History xls ", e);
        }
    }

    private List<ConnectionHistoryDto> findConnectionHistoryBetweenAndUserIds(Date start, Date end, List<String> userIds){
        var listConnection = connectionHistoryRepository.findAllByConnectionDateTimeBetweenAndUserIdIn(start, end, userIds);
        return listConnection.stream().map(connectionHistoryConverter::convertEntityToDto)
            .collect(Collectors.toList());
    }

}
