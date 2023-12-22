package fr.gouv.vitamui.iam.internal.server.group.dto;

import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.vitam.xls.dto.LineDto;
import fr.gouv.vitamui.commons.vitam.xls.dto.ValueDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.ap.internal.util.Strings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static fr.gouv.vitamui.commons.vitam.xls.dto.Type.DOUBLE;
import static fr.gouv.vitamui.commons.vitam.xls.dto.Type.STRING;
import static fr.gouv.vitamui.iam.internal.server.group.dto.ProfileGroupExportSheet.PROFILE_GROUP_SHEET_COLUMNS;

@Builder
@AllArgsConstructor
@Getter
@Setter
public final class ProfileGroupExport implements LineDto {
    public static final String TYPE_PROFILE_GROUP = "Groupe de profil";
    public static final String TYPE_PROFILE = "Profil";
    public static final String DATE_FR_PIPE_TIME_WITH_SECONDS = "dd/MM/yyyy '|' HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FR_PIPE_TIME_WITH_SECONDS);

    private String id;
    private Integer identifier;
    private String name;
    private String type;
    private String status;
    private String description;
    private String subLevel;
    private String units;
    private Long usersCount;
    private String createdAt;
    private String lastModified;


    public static ProfileGroupExport fromGroup(GroupDto groupDto) {
        return ProfileGroupExport.builder()
            .id(groupDto.getId())
            .identifier(Integer.valueOf(groupDto.getIdentifier()))
            .name(groupDto.getName())
            .type(TYPE_PROFILE_GROUP)
            .status(translateEnable(groupDto.isEnabled()))
            .description(groupDto.getDescription())
            .subLevel(groupDto.getLevel())
            .units(join("|", groupDto.getUnits()))
            .usersCount(Optional.ofNullable(groupDto.getUsersCount()).orElse(0L))
            .createdAt("")
            .lastModified("")
            .build();
    }

    public static ProfileGroupExport fromProfile(ProfileDto profileDto){
        return ProfileGroupExport.builder()
            .id(profileDto.getId())
            .identifier(Integer.valueOf(profileDto.getIdentifier()))
            .name(profileDto.getName())
            .type(TYPE_PROFILE)
            .status(translateEnable(profileDto.isEnabled()))
            .description(profileDto.getDescription())
            .subLevel(profileDto.getLevel())
            .usersCount(Optional.ofNullable(profileDto.getUsersCount()).orElse(0L))
            .units("")
            .createdAt("")
            .lastModified("")
            .build();
    }

    @Override
    public Map<String, ValueDto> toSheetLine() {
        return Map.of(
           PROFILE_GROUP_SHEET_COLUMNS.get(0), ValueDto.of(identifier, DOUBLE),
           PROFILE_GROUP_SHEET_COLUMNS.get(1), ValueDto.of(name, STRING),
           PROFILE_GROUP_SHEET_COLUMNS.get(2), ValueDto.of(type, STRING),
           PROFILE_GROUP_SHEET_COLUMNS.get(3), ValueDto.of(status, STRING),
           PROFILE_GROUP_SHEET_COLUMNS.get(4), ValueDto.of(description, STRING),
           PROFILE_GROUP_SHEET_COLUMNS.get(5), ValueDto.of(subLevel, STRING),
           PROFILE_GROUP_SHEET_COLUMNS.get(6), ValueDto.of(units, STRING),
           PROFILE_GROUP_SHEET_COLUMNS.get(7), ValueDto.of(usersCount, DOUBLE),
           PROFILE_GROUP_SHEET_COLUMNS.get(8), ValueDto.of(parseDateToFormat(createdAt), STRING),
           PROFILE_GROUP_SHEET_COLUMNS.get(9), ValueDto.of(parseDateToFormat(lastModified), STRING)
        );
    }

    static String join(String separator, Collection<String> collection){
        if(collection == null){
            return "";
        }
        return String.join(separator, collection);
    }

    private static String translateEnable(boolean enable){
        return enable ? "actif" : "inactif";
    }

    private String parseDateToFormat(String date){
        if(Strings.isEmpty(date)){
            return "";
        }
        var frDate = LocalDateTime.parse(date).plusHours(1);
        return frDate.format(FORMATTER);
    }


}
