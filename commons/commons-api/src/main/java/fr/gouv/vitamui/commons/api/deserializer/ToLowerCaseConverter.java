package fr.gouv.vitamui.commons.api.deserializer;

import tools.jackson.databind.util.StdConverter;

public class ToLowerCaseConverter extends StdConverter<String, String> {

    @Override
    public String convert(String value) {
        return value.toLowerCase();
    }
}
