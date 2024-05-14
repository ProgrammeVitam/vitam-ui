package fr.gouv.vitamui.commons.vitam.xls.dto;

import java.util.Map;

public interface LineDto {
    Map<String, ValueDto> toSheetLine();
}
