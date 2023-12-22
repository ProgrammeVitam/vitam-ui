package fr.gouv.vitamui.commons.api.dtos;

import fr.gouv.vitamui.commons.api.enums.ErrorImportFileMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ErrorImportFile implements Serializable {

    private Integer line;

    private Character column;

    private ErrorImportFileMessage error;

    /**
     * Data passed for the front labels
     */
    private String data;
}
