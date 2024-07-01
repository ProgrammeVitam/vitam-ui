package fr.gouv.vitamui.commons.rest.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * VITAMUI DTO.
 *
 *
 */
@Data
public class VitamUIDto {

    @NotNull
    private String key;

    private String message;

    private Object errors;

    private List<Integer> args;
}
