/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.commons.api.utils;

import java.util.List;
import java.util.Map;

import fr.gouv.vitamui.commons.api.exception.InvalidTypeException;

public final class CastUtils {

    private CastUtils() {
    }

    /**
     * Cast the object into String.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static String toString(final Object value) {
        try {
            return (String) value;
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
    }

    /**
     * Cast the object into Boolean.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static Boolean toBoolean(final Object value) {
        try {
            return (Boolean) value;
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
    }

    /**
     * Cast the object into Integer.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static Integer toInteger(final Object value) {
        if(value == null) {
            return null;
        }
        try {
            return value instanceof Number ? ((Number) value).intValue() : (Integer) value;
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
    }

    /**
     * Cast the object into Long.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static Long toLong(final Object value) {
        if(value == null) {
            return null;
        }
        try {
            return (value != null && value instanceof Number) ? ((Number) value).longValue() : (Long) value;
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
    }

    /**
     * Cast the object into int.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static int toInt(final Object value) {
        if(value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
    }

    /**
     * Cast the object into Double.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static Double toDouble(final Object value) {
        try {
            return value instanceof Number ? ((Number) value).doubleValue() : (Double) value;
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
    }

    /**
     * Cast the object into List.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(final Object value) {
        try {
            return List.class.cast(value);
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
    }

    /**
     * Cast the object into List.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> toMap(final Object value) {
        try {
            return Map.class.cast(value);
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
    }

    /**
     * Cast the object into the given type.
     * @param value
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static <T> T castValue(final Object value, final Class<T> clazz) {
        T t;
        try {
            t = clazz.cast(value);
        }
        catch (final ClassCastException e) {
            throw new InvalidTypeException(e.getMessage());
        }
        return t;
    }
}
