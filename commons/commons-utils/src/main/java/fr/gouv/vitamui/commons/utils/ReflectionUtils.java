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
package fr.gouv.vitamui.commons.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


/**
 *
 * Class Helper for doing reflection on java object
 *
 *
 */
public class ReflectionUtils {

    /**
     * Return true if type is assignable to a List.class
     * @param type to test
     * @return
     * @throws IllegalArgumentException if type is null
     */
    public static boolean isParametrizedList(final Type type) throws IllegalArgumentException {
        if (  type instanceof ParameterizedType ) {
            final Class<?> clazz = castTypeToClass(((ParameterizedType) type).getRawType());
            return clazz.isAssignableFrom(List.class) ;
        }
        return false ;
    }

    /**
     *
     * @param type
     * @return Cast Type Object To Class
     */
    public static Class<?> castTypeToClass(final Type type) {
        return (Class<?>) type;
    }

    /**
     * Return the minimal class for a type <br>
     * Example : <br>
     * - List<E> : return E.class <br>
     * - String : return String.class <br>
     * @param type
     * @return Class<?>
     * @throws IllegalArgumentException if type is a List.class with out parameterizedTypes
     */
    public static Class<?> getParametrizedClass(final Type type) throws IllegalArgumentException {
        if (type instanceof ParameterizedType ) {
            final List<Type> parameterizedTypes = Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
            final Optional<Type> optParamType = parameterizedTypes.stream().findFirst() ;
            final Type paramType =  optParamType.orElseThrow(() -> new IllegalArgumentException("Missing parameterized types")) ;
            return castTypeToClass(paramType);
        }
        return castTypeToClass(type);
    }

    /**
     *
     * @param entityClass
     * @param fieldName
     * @return
     * @throws NoSuchFieldException if no field with {@code fieldName} exist in {@code entityClass}
     */
    public static Type getTypeOfField(final Class<?> entityClass, final String fieldName) throws NoSuchFieldException {

        // Field composed
        if (fieldName.contains(".")) {
            String[] fieldsName = fieldName.split(Pattern.quote("."));
            // TODO exception if field doesn't exist
            final Field field = FieldUtils.getField(entityClass, fieldsName[0], true);
            fieldsName = ArrayUtils.removeElement(fieldsName, fieldsName[0]);
            Class<?> subClass = ReflectionUtils.isParametrizedList(field.getGenericType()) ?
                    getParametrizedClass(field.getGenericType()) : field.getType();
            return getTypeOfField(subClass, StringUtils.join(fieldsName, "."));
        }

        try {
            final Field field = FieldUtils.getField(entityClass, fieldName, true);
            return field.getGenericType();
        }
        catch (final Exception e) {
            throw new NoSuchFieldException("no fields " + fieldName + " found");
        }
    }

    /**
    * Method allowing to check if a given field exists in the class.
    * @param entityClass Class to analyze.
    * @param fieldName Field to check.
    * @return True if the field exists, false otherwhise.
    */
   public static boolean hasField(final Class<?> entityClass, final String fieldName){

//       LOGGER.debug("getTypeOfField : entityClass {} - field {}", entityClass, fieldName);
       if (fieldName.contains(".")) {
           String[] fields = fieldName.split(Pattern.quote("."));
           // TODO exception if field doesn't exist
           final Field field = FieldUtils.getField(entityClass, fields[0], true);
           // If the field does not exist, we return false.
           if (field == null) {
               return false;
           }
           fields = ArrayUtils.removeElement(fields, fields[0]);
           Class<?> subClass = ReflectionUtils.isParametrizedList(field.getGenericType()) ?
                   getParametrizedClass(field.getGenericType()) : field.getType();
//           LOGGER.debug("getTypeOfField : entityClass {} - fields {}", subClass, fields);
           return hasField(subClass, StringUtils.join(fields, "."));
       }

       try {
           return FieldUtils.getField(entityClass, fieldName, true) != null;
       }
       catch (final Exception e) {
           return false;
       }
   }
}
