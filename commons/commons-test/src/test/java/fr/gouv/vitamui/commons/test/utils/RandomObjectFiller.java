package fr.gouv.vitamui.commons.test.utils;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

public class RandomObjectFiller {

    private static Random random = new Random();

    private RandomObjectFiller() {
        // do nothing
    }

    /**
     * Class for generate a random object with primitive and wrapper fields
     * (Integer, Long, Double, Float, String, BigInteger)
     *
     * @param clazz to be generate randomly
     * @return Instance of clazz
     * @throws Exception if clazz don't have a constructor without parameters or a final fields
     */
    @Deprecated
    public static <T> T createAndFill(final Class<T> clazz) throws Exception {
        final T instance = clazz.newInstance();
        for (final Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            final Object value = getRandomValueForField(field);
            field.set(instance, value);
        }
        return instance;
    }

    private static Object getRandomValueForField(final Field field) throws Exception {
        final Class<?> type = field.getType();

        // Note that we must handle the different types here! This is just an
        // example, so this list is not complete! Adapt this to your needs!
        if (type.isEnum()) {
            final Object[] enumValues = type.getEnumConstants();
            return enumValues[random.nextInt(enumValues.length)];
        } else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return random.nextInt();
        } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
            return random.nextLong();
        } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return random.nextBoolean();
        } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
            return random.nextDouble();
        } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
            return random.nextFloat();
        } else if (type.equals(String.class)) {
            return UUID.randomUUID().toString();
        } else if (type.equals(BigInteger.class)) {
            return BigInteger.valueOf(random.nextInt());
        }
        return createAndFill(type);
    }

}
