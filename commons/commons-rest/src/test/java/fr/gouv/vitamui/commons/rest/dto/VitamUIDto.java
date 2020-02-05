package fr.gouv.vitamui.commons.rest.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

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
