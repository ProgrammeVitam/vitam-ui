package fr.gouv.vitamui.commons.test.utils;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.CollectionUtils;

import fr.gouv.vitamui.commons.utils.VitamUIUtils;

public final class TestUtils {

    public static Map<String, Object> getMapFromObject(final Object other) {
        final Map<String, Object> map = new HashMap<>();
        final Field[] filed = FieldUtils.getAllFields(other.getClass());
        for (final Field f : filed) {
            fillMapFromObject(f, map, other);
        }
        return map;
    }

    public static List<?> getListFromList(final List<?> list) {
        final ArrayList<Object> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list)) {
            list.stream().forEach(el -> result.add(getMapFromObject(el)));
        }
        return result;
    }

    public static void fillMapFromObject(final Field f, final Map<String, Object> partialDto, final Object other) {
        final String key = f.getName();

        // Ignore this key because :
        // To produce the code coverage EclEmma adds a field
        // private static final transient boolean[] $jacocoData to your class.
        if (key.equalsIgnoreCase("$jacocoData")) {
            return;
        }
        Object value = null;
        final boolean isAccessible = f.isAccessible();
        if (!isAccessible) {
            f.setAccessible(true);
        }
        value = getValueFromType(f, other);
        f.setAccessible(isAccessible);
        partialDto.put(key, value);

    }

    private static Object getValueFromType(final Field f, final Object other) {
        final Class<?> fieldType = f.getType();
        Object val = null;
        try {
            final Optional<Object> optionalVal = Optional.ofNullable(f.get(other));
            if (optionalVal.isPresent()) {
                if (fieldType.isEnum() || Duration.class.equals(fieldType) || OffsetDateTime.class.equals(fieldType)) {
                    val = optionalVal.get().toString();
                }
                else {
                    val = optionalVal.get();
                }
            }

        }
        catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return val;
    }

    public static <T> T getCopy(final Object source, final Class<T> clazz) {
        T target;
        try {
            target = clazz.newInstance();
            VitamUIUtils.copyProperties(source, target);
            return target;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

}
