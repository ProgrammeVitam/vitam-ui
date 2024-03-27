package fr.gouv.vitamui.iam.internal.server.user.service;

import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.internal.server.user.converter.ConnectionHistoryConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.ConnectionHistoryRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.ConnectionHistory;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ConnectionHistoryServiceTest {

    @InjectMocks
    private ConnectionHistoryService connectionHistoryService;

    @Mock
    private ConnectionHistoryRepository connectionHistoryRepository;

    @Mock
    private ConnectionHistoryConverter connectionHistoryConverter;

    @Spy
    private ConnectionHistoryExportService connectionHistoryExportService;

    @Mock
    private InternalSecurityService securityService;

    @Mock
    private UserInternalService userInternalService;

    @Test
    void exportConnectionHistory() throws IOException {
        // Given
        String criteria = "{\"criteria\":[{\"key\":\"reportingDate\",\"operator\":\"BETWEEN\",\"value\":{\"start\":\"2023-01-01T00:00:00.000Z\",\"end\":\"2023-11-21T16:57:52.099Z\"}}]}";

        AuthUserDto authUserDto = new AuthUserDto();
        authUserDto.setCustomerId("customerId");
        when(securityService.getUser()).thenReturn(authUserDto);

        User user = new User();
        user.setIdentifier("userId");
        when(userInternalService.findByCustomerId("customerId")).thenReturn(List.of(user));

        when(connectionHistoryRepository.findAllByConnectionDateTimeBetweenAndUserIdIn(
            Date.from(Instant.parse("2023-01-01T00:00:00.000Z")),
            Date.from(Instant.parse("2023-11-21T16:57:52.099Z")),
            List.of("userId"))
        ).thenReturn(List.of(
            createConnectionHistory("userId", "2023-02-01T10:30:00.330Z", "")
        ));

        // When
        Resource resource = connectionHistoryService.exportConnectionHistory(Optional.ofNullable(criteria));

        // Then
        assertThat(resource).isNotNull();
        var sheet0 = new XSSFWorkbook(resource.getInputStream()).getSheetAt(0);
        assertThat(sheet0.getRow(1).getCell(0).getStringCellValue()).isEqualTo("userId");
        assertThat(sheet0.getRow(1).getCell(1).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.parse("2023-02-01"));
        assertThat(sheet0.getRow(1).getCell(2).getStringCellValue()).isEqualTo("10:30:00.330");

    }

    private ConnectionHistory createConnectionHistory(String userId, String date, String subId){
        return ConnectionHistory.builder()
            .userId(userId)
            .connectionDateTime(Date.from(Instant.parse(date)))
            .subrogatedUserId(subId)
            .build();
    }
}
