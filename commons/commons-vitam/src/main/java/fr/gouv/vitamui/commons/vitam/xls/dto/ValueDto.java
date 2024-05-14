package fr.gouv.vitamui.commons.vitam.xls.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ValueDto {

    private Object value;
    private Type type;

    public static ValueDto of(Object value, Type type) {
        return builder().value(value).type(type).build();
    }
}
