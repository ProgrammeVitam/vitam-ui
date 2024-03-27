package fr.gouv.vitamui.iam.internal.server.group.dto;

import fr.gouv.vitamui.commons.vitam.xls.dto.SheetDto;
import fr.gouv.vitamui.commons.vitam.xls.dto.ValueDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProfileGroupExportSheet extends SheetDto {
    public static final String PROFILE_GROUP_SHEET_NAME = "Liste_profils_groupes";
    public static final List<String> PROFILE_GROUP_SHEET_COLUMNS = List.of(
        "Id du groupe de profil",
        "Nom du groupe de profil",
        "Type",
        "Etat",
        "Description du groupe de profil",
        "Sous-niveau du groupe de profil",
        "Unité d’appartenance du groupe de profil",
        "Nombre d’utilisateurs rattaché au groupe de profil",
        "Date de création du groupe de profil",
        "Date de dernière modification"
    );

    public ProfileGroupExportSheet(List<Map<String, ValueDto>> lines) {
        super(PROFILE_GROUP_SHEET_NAME, PROFILE_GROUP_SHEET_COLUMNS, Collections.emptyMap(), lines, true);
    }

    @Override
    public String getName() {
        return PROFILE_GROUP_SHEET_NAME;
    }

    @Override
    public List<String> getColumns() {
        return PROFILE_GROUP_SHEET_COLUMNS;
    }
}
