package fr.gouv.vitamui.iam.internal.server.group.service;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.iam.internal.server.application.service.ApplicationInternalService;
import fr.gouv.vitamui.iam.internal.server.group.dto.LinkedProfileExport;
import fr.gouv.vitamui.iam.internal.server.group.dto.ProfileGroupExport;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.gouv.vitamui.iam.internal.server.group.dto.LinkedProfileExportSheet.LINKED_PROFILE_SHEET_COLUMNS;
import static fr.gouv.vitamui.iam.internal.server.group.dto.LinkedProfileExportSheet.LINKED_PROFILE_SHEET_NAME;
import static fr.gouv.vitamui.iam.internal.server.group.dto.ProfileGroupExportSheet.PROFILE_GROUP_SHEET_COLUMNS;
import static fr.gouv.vitamui.iam.internal.server.group.dto.ProfileGroupExportSheet.PROFILE_GROUP_SHEET_NAME;
import static fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest.buildGroupDto;
import static fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest.buildProfileDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupExportServiceTest {

    private final ApplicationInternalService applicationInternalService = mock(ApplicationInternalService.class);
    private GroupExportService groupExportService = new GroupExportService(applicationInternalService);

    @Test
    void testExportProfileGroups_first_sheet_OK() throws IOException {
        // Given
        final List<ProfileDto> profilesDto = List.of(
            buildProfileDto("1068", "Profil Archives Full", "customerID", 218, "ARCHIVE_APP")
        );
        final List<GroupDto> groupsDto = List.of(
            buildGroupDto("135", "GROUPE_ADMIN_CLIENT_ROOT 000001", "customerID", List.of("1068", "1069"))
        );

        groupsDto.forEach(g -> g.setIdentifier(g.getId()));
        profilesDto.forEach(p -> p.setIdentifier(p.getId()));

        var data = Stream.concat(
            groupsDto.stream().map(ProfileGroupExport::fromGroup),
            profilesDto.stream().map(ProfileGroupExport::fromProfile)
        ).collect(Collectors.toList());

        // When
        final Resource excelFile = groupExportService.exportProfileGroups(groupsDto, profilesDto, Map.of());

        // Then
        assertThat(excelFile).isNotNull();

        // SHEET 0
        final XSSFSheet sheet0 = new XSSFWorkbook(excelFile.getInputStream()).getSheetAt(0);
        assertThat(sheet0.getSheetName()).isEqualTo(PROFILE_GROUP_SHEET_NAME);

        // Assert titles
        var titleSheet0 = getRawAsListString(sheet0, 0, PROFILE_GROUP_SHEET_COLUMNS.size());
        assertThat(titleSheet0).containsExactlyElementsOf(PROFILE_GROUP_SHEET_COLUMNS);

        // Assert contents
        assertProfileGroupRows(sheet0, data);
    }

    @Test
    void testExportProfileGroups_second_sheet_OK() throws IOException {
        // Given
        final List<ProfileDto> profilesDto = List.of(
            createProfileDto("1068", "Profil Archives Full", 218, "tenant 218", "ARCHIVE_APP"),
            createProfileDto("33088", "Profile Agency", 111, "tenant 111", "APP Agency")
        );
        final List<GroupDto> groupsDto = List.of(
            createGroup(
                "135",
                "GROUPE_ADMIN_CLIENT_ROOT 000001",
                "customerID",
                List.of("1068", "1069"),
                List.of(profilesDto.get(0))
            ),
            createGroup("136", "Groupe de l'utilisateur support", "customerID", List.of(), List.of())
        );

        groupsDto.forEach(g -> g.setIdentifier(g.getId()));
        profilesDto.forEach(p -> p.setIdentifier(p.getId()));

        // When
        when(applicationInternalService.findApplicationByIdentifier(List.of("ARCHIVE_APP"))).thenReturn(
            Map.of("ARCHIVE_APP", "APP Archives")
        );
        final Resource excelFile = groupExportService.exportProfileGroups(groupsDto, profilesDto, Map.of());

        // Then
        assertThat(excelFile).isNotNull();

        // SHEET 0
        final XSSFSheet sheet1 = new XSSFWorkbook(excelFile.getInputStream()).getSheetAt(1);
        assertThat(sheet1.getSheetName()).isEqualTo(LINKED_PROFILE_SHEET_NAME);

        // Assert titles
        var titleSheet0 = getRawAsListString(sheet1, 0, LINKED_PROFILE_SHEET_COLUMNS.size());
        assertThat(titleSheet0).containsExactlyElementsOf(LINKED_PROFILE_SHEET_COLUMNS);

        // Assert contents
        LinkedProfileExport line1 = new LinkedProfileExport(
            135D,
            "APP Archives",
            218,
            "tenant 218",
            "1068",
            "Profil Archives Full"
        );

        assertLinkedProfileRows(sheet1, List.of(line1));
    }

    private GroupDto createGroup(
        String id,
        String name,
        String customerID,
        List<String> profilesIds,
        List<ProfileDto> profiles
    ) {
        GroupDto groupDto = buildGroupDto(id, name, customerID, profilesIds);
        groupDto.setProfiles(profiles);
        return groupDto;
    }

    private List<String> getRawAsListString(XSSFSheet sheet, int rowIndex, int colNum) {
        return IntStream.range(0, colNum)
            .mapToObj(i -> sheet.getRow(rowIndex).getCell(i).getStringCellValue())
            .collect(Collectors.toList());
    }

    private void assertProfileGroupRows(XSSFSheet sheet0, List<ProfileGroupExport> dataList) {
        IntStream.range(0, dataList.size()).forEach(i -> {
            var actualIdentifier = sheet0.getRow(i + 1).getCell(0).getNumericCellValue();
            var expectedIdentifier = dataList.get(i).getIdentifier();
            assertThat(actualIdentifier).isEqualTo(expectedIdentifier.doubleValue());

            var actualName = sheet0.getRow(i + 1).getCell(1).getStringCellValue();
            var expectedName = dataList.get(i).getName();
            assertThat(actualName).isEqualTo(expectedName);

            var actualType = sheet0.getRow(i + 1).getCell(2).getStringCellValue();
            assertThat(actualType).isEqualTo(dataList.get(i).getType());

            var actualStatus = sheet0.getRow(i + 1).getCell(3).getStringCellValue();
            assertThat(actualStatus).isEqualTo(dataList.get(i).getStatus());

            var actualDescription = sheet0.getRow(i + 1).getCell(4).getStringCellValue();
            assertThat(actualDescription).isEqualTo(dataList.get(i).getDescription());

            var actualSubLevel = sheet0.getRow(i + 1).getCell(5).getStringCellValue();
            assertThat(actualSubLevel).isEqualTo(dataList.get(i).getSubLevel());

            var actualUnits = sheet0.getRow(i + 1).getCell(6).getStringCellValue();
            assertThat(actualUnits).isEqualTo(dataList.get(i).getUnits());

            var actualUserCount = sheet0.getRow(i + 1).getCell(7).getNumericCellValue();
            assertThat(actualUserCount).isEqualTo(dataList.get(i).getUsersCount().doubleValue());

            var actualCreated = sheet0.getRow(i + 1).getCell(8).getStringCellValue();
            assertThat(actualCreated).isEqualTo(dataList.get(i).getCreatedAt());

            var actualLastUpdated = sheet0.getRow(i + 1).getCell(9).getStringCellValue();
            assertThat(actualLastUpdated).isEqualTo(dataList.get(i).getLastModified());
        });
    }

    private void assertLinkedProfileRows(XSSFSheet sheet0, List<LinkedProfileExport> dataList) {
        IntStream.range(0, dataList.size()).forEach(i -> {
            var actualGroupIdentifier = sheet0.getRow(i + 1).getCell(0).getNumericCellValue();
            var expectedGroupIdentifier = dataList.get(i).getGroupIdentifier();
            assertThat(actualGroupIdentifier).isEqualTo(expectedGroupIdentifier.doubleValue());

            var actualApplicationName = sheet0.getRow(i + 1).getCell(1).getStringCellValue();
            var expectedApplicationName = dataList.get(i).getApplicationName();
            assertThat(actualApplicationName).isEqualTo(expectedApplicationName);

            var actualTenantId = sheet0.getRow(i + 1).getCell(2).getNumericCellValue();
            var expectedTenantId = Optional.ofNullable(dataList.get(i).getTenantIdentifier())
                .map(Integer::doubleValue)
                .orElse(null);
            assertThat(actualTenantId).isEqualTo(expectedTenantId);

            var actualTenantName = sheet0.getRow(i + 1).getCell(3).getStringCellValue();
            assertThat(actualTenantName).isEqualTo(dataList.get(i).getTenantName());

            var actualProfileIdentifier = sheet0.getRow(i + 1).getCell(4).getStringCellValue();
            assertThat(actualProfileIdentifier).isEqualTo(dataList.get(i).getProfileIdentifier());

            var actualProfileName = sheet0.getRow(i + 1).getCell(5).getStringCellValue();
            assertThat(actualProfileName).isEqualTo(dataList.get(i).getProfileName());
        });
    }

    private ProfileDto createProfileDto(
        String id,
        String name,
        int tenantId,
        String tenantName,
        String applicationName
    ) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setIdentifier(id);
        profileDto.setId(id);
        profileDto.setName(name);
        profileDto.setTenantIdentifier(tenantId);
        profileDto.setTenantName(tenantName);
        profileDto.setApplicationName(applicationName);
        return profileDto;
    }
}
