package fr.gouv.vitamui.iam.internal.server.group.dto;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.vitam.xls.dto.LineDto;
import fr.gouv.vitamui.commons.vitam.xls.dto.ValueDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fr.gouv.vitamui.commons.vitam.xls.dto.Type.DOUBLE;
import static fr.gouv.vitamui.commons.vitam.xls.dto.Type.STRING;
import static fr.gouv.vitamui.iam.internal.server.group.dto.LinkedProfileExportSheet.LINKED_PROFILE_SHEET_COLUMNS;
import static java.lang.Double.parseDouble;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class LinkedProfileExport implements LineDto {

    private Double groupIdentifier;
    private String applicationName;
    private Integer tenantIdentifier;
    private String tenantName;
    private String profileIdentifier;
    private String profileName;

    public static LinkedProfileExport of(GroupDto groupDto, ProfileDto profileDto){
        return LinkedProfileExport.builder()
            .groupIdentifier(parseDouble(groupDto.getIdentifier()))
            .applicationName(profileDto.getApplicationName())
            .tenantIdentifier(profileDto.getTenantIdentifier())
            .tenantName(profileDto.getTenantName())
            .profileIdentifier(profileDto.getIdentifier())
            .profileName(profileDto.getName())
            .build();
    }
    public static LinkedProfileExport of(GroupDto groupDto) {
        return LinkedProfileExport.builder()
            .groupIdentifier(parseDouble(groupDto.getIdentifier()))
            .build();
    }

    public static Stream<LinkedProfileExport> createListLinked(List<GroupDto> groupsDto) {
        return groupsDto.stream()
            .flatMap(group -> {
                    if(group.getProfiles() == null || group.getProfiles().isEmpty()){
                        return Stream.of(LinkedProfileExport.of(group));
                    }
                    return group.getProfiles().stream()
                        .map(profile -> LinkedProfileExport.of(group, profile));
                });
    }

    @Override
    public Map<String, ValueDto> toSheetLine() {
        return Map.of(
            LINKED_PROFILE_SHEET_COLUMNS.get(0), ValueDto.of(groupIdentifier, DOUBLE),
            LINKED_PROFILE_SHEET_COLUMNS.get(1), ValueDto.of(applicationName, STRING),
            LINKED_PROFILE_SHEET_COLUMNS.get(2), ValueDto.of(tenantIdentifier, DOUBLE),
            LINKED_PROFILE_SHEET_COLUMNS.get(3), ValueDto.of(tenantName, STRING),
            LINKED_PROFILE_SHEET_COLUMNS.get(4), ValueDto.of(profileIdentifier, STRING),
            LINKED_PROFILE_SHEET_COLUMNS.get(5), ValueDto.of(profileName, STRING)
        );
    }
}
