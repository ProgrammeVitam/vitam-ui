package fr.gouv.vitamui.iam.internal.server.group.service;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookEventDto;
import fr.gouv.vitamui.commons.vitam.xls.ExcelUtils;
import fr.gouv.vitamui.commons.vitam.xls.dto.ValueDto;
import fr.gouv.vitamui.iam.internal.server.application.service.ApplicationInternalService;
import fr.gouv.vitamui.iam.internal.server.group.dto.LinkedProfileExport;
import fr.gouv.vitamui.iam.internal.server.group.dto.LinkedProfileExportSheet;
import fr.gouv.vitamui.iam.internal.server.group.dto.ProfileGroupExport;
import fr.gouv.vitamui.iam.internal.server.group.dto.ProfileGroupExportSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.gouv.vitamui.commons.logbook.common.EventType.EXT_VITAMUI_CREATE_GROUP;
import static fr.gouv.vitamui.commons.logbook.common.EventType.EXT_VITAMUI_UPDATE_GROUP;

@Service
@RequiredArgsConstructor
public class GroupExportService {

    public static final String DATE_TIME_FORMAT_ISO_WITH_MS = "yyyy-MM-dd'T'HH_mm_ss.SSSSSS";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_ISO_WITH_MS = DateTimeFormatter.ofPattern(
        DATE_TIME_FORMAT_ISO_WITH_MS
    );

    private final ApplicationInternalService applicationInternalService;

    public static String getFilename() {
        return String.format("export-groupes-%s.xlsx", DATE_TIME_FORMATTER_ISO_WITH_MS.format(LocalDateTime.now()));
    }

    public Resource exportProfileGroups(
        List<GroupDto> groupsDto,
        List<ProfileDto> profilesDto,
        Map<String, List<LogbookEventDto>> groupEvents
    ) {
        List<Map<String, ValueDto>> dataFirstSheet = getProfilesGroupRows(groupsDto, profilesDto, groupEvents);
        List<Map<String, ValueDto>> dataSecondSheet = getLinkedProfileGroupRows(groupsDto);

        try {
            return ExcelUtils.generateWorkbook(
                List.of(new ProfileGroupExportSheet(dataFirstSheet), new LinkedProfileExportSheet(dataSecondSheet))
            );
        } catch (IOException e) {
            throw new InternalServerException("An error occurred while creating the xls profile groups list export", e);
        }
    }

    private List<Map<String, ValueDto>> getProfilesGroupRows(
        List<GroupDto> groupsDto,
        List<ProfileDto> profilesDto,
        Map<String, List<LogbookEventDto>> groupEvents
    ) {
        var groupExport = groupsDto.stream().map(ProfileGroupExport::fromGroup).collect(Collectors.toList());
        addDateEvent(groupExport, groupEvents);

        return Stream.concat(groupExport.stream(), profilesDto.stream().map(ProfileGroupExport::fromProfile))
            .sorted(Comparator.comparing(ProfileGroupExport::getType).thenComparing(ProfileGroupExport::getIdentifier))
            .map(ProfileGroupExport::toSheetLine)
            .collect(Collectors.toList());
    }

    private List<Map<String, ValueDto>> getLinkedProfileGroupRows(List<GroupDto> groupsDto) {
        var linkedProfiles = LinkedProfileExport.createListLinked(groupsDto)
            .sorted(Comparator.comparing(LinkedProfileExport::getGroupIdentifier))
            .collect(Collectors.toList());

        var applicationIds = linkedProfiles
            .stream()
            .map(LinkedProfileExport::getApplicationName)
            .filter(id -> !(id == null || id.isEmpty()))
            .distinct()
            .collect(Collectors.toList());
        var applicationsMap = mapIdApplicationToNames(applicationIds);

        return linkedProfiles
            .stream()
            .map(linkedProfile -> {
                var appId = linkedProfile.getApplicationName();
                if (!(appId == null || appId.isEmpty())) {
                    linkedProfile.setApplicationName(applicationsMap.get(linkedProfile.getApplicationName()));
                }
                return linkedProfile;
            })
            .map(LinkedProfileExport::toSheetLine)
            .collect(Collectors.toList());
    }

    private Map<String, String> mapIdApplicationToNames(List<String> idApplications) {
        return this.applicationInternalService.findApplicationByIdentifier(idApplications);
    }

    private void addDateEvent(
        List<ProfileGroupExport> profileGroupExports,
        Map<String, List<LogbookEventDto>> groupEvents
    ) {
        var evtCreations = groupEvents.getOrDefault(EXT_VITAMUI_CREATE_GROUP.name(), List.of());
        var evtUpdates = groupEvents.getOrDefault(EXT_VITAMUI_UPDATE_GROUP.name(), List.of());

        if (!evtCreations.isEmpty()) {
            profileGroupExports.forEach(
                groupExport ->
                    evtCreations
                        .stream()
                        .filter(
                            evt ->
                                Integer.valueOf(evt.getObId()).equals(groupExport.getIdentifier()) &&
                                evt.getEvDateTime() != null
                        )
                        .max(Comparator.comparing(LogbookEventDto::getEvDateTime))
                        .ifPresent(evt -> groupExport.setCreatedAt(evt.getEvDateTime()))
            );
        }

        if (!evtUpdates.isEmpty()) {
            profileGroupExports.forEach(
                groupExport ->
                    evtUpdates
                        .stream()
                        .filter(
                            evt ->
                                Integer.valueOf(evt.getObId()).equals(groupExport.getIdentifier()) &&
                                evt.getEvDateTime() != null
                        )
                        .max(Comparator.comparing(LogbookEventDto::getEvDateTime))
                        .ifPresent(evt -> groupExport.setLastModified(evt.getEvDateTime()))
            );
        }
    }
}
