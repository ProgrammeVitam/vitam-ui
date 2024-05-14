package fr.gouv.vitamui.iam.internal.server.group.dto;

import fr.gouv.vitamui.commons.vitam.xls.dto.SheetDto;
import fr.gouv.vitamui.commons.vitam.xls.dto.ValueDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LinkedProfileExportSheet extends SheetDto {

    public static final String LINKED_PROFILE_SHEET_NAME = "Lien_profils_groupes";
    public static final List<String> LINKED_PROFILE_SHEET_COLUMNS = List.of(
        "Id du groupe de profil",
        "Nom de l'application autorisée",
        "Id du tenant autorisé",
        "Nom du tenant",
        "Id du profil rattaché",
        "Nom du profil rattaché"
    );

    public LinkedProfileExportSheet(List<Map<String, ValueDto>> lines) {
        super(LINKED_PROFILE_SHEET_NAME, LINKED_PROFILE_SHEET_COLUMNS, Collections.emptyMap(), lines, true);
    }

    @Override
    public String getName() {
        return LINKED_PROFILE_SHEET_NAME;
    }

    @Override
    public List<String> getColumns() {
        return LINKED_PROFILE_SHEET_COLUMNS;
    }
}
