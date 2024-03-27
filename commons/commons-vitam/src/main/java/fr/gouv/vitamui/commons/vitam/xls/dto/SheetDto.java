package fr.gouv.vitamui.commons.vitam.xls.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SheetDto {

    private String name;

    private List<String> columns;

    private Map<String, String> columnsFormat = new HashMap<>();

    private List<Map<String, ValueDto>> lines;

    private boolean autoSizeRows = false;

}
