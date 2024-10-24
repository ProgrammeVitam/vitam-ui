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

import fr.gouv.vitamui.commons.api.exception.InvalidTypeException;
import fr.gouv.vitamui.commons.api.exception.ValidationException;

import java.util.Optional;

public class EnumUtils {

    public static <E extends Enum<E>> void checkValidEnum(
        final Class<E> enumClass,
        final Optional<String> optionalEnumList
    ) {
        if (optionalEnumList.isPresent()) {
            final String enumList = optionalEnumList.get();
            final String[] values = enumList.split(",");
            for (final String enumName : values) {
                isValidEnum(enumClass, enumName.toUpperCase());
            }
        }
    }

    /**
     * Cast the object into the given type.
     *
     * @param enumName
     * @param clazz
     * @throws InvalidTypeException
     *             If the object is not null and is not assignable to the type T
     * @return
     */
    public static <T extends Enum<T>> T stringToEnum(final Class<T> enumType, final String enumName) {
        isValidEnum(enumType, enumName);
        return Enum.valueOf(enumType, enumName);
    }

    private static <E extends Enum<E>> void isValidEnum(final Class<E> enumClass, final String enumName) {
        final Boolean valid = org.apache.commons.lang3.EnumUtils.isValidEnum(enumClass, enumName);
        if (!valid) {
            throw new ValidationException("validation failed : " + enumName + " is not allowed ");
        }
    }
}
