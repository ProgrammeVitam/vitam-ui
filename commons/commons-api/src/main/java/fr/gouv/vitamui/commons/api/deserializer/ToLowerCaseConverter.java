package fr.gouv.vitamui.commons.api.deserializer;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.StdConverter;

public class ToLowerCaseConverter extends StdConverter<String,String> {

    @Override
    public String convert(String value) {
        return value.toLowerCase();
    }
}
