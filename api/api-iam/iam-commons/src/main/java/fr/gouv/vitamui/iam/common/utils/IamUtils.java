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
package fr.gouv.vitamui.iam.common.utils;

import java.util.List;
import java.util.Optional;

public final class IamUtils {

    private IamUtils() {
    }

    /**
     * Method to build a string of embedded fields in following the format:
     * "field_1, field_2, field_3".
     *
     * Returns an empty string if the given enum array is empty.
     * @param embeddedField
     * @return
     */
    @SafeVarargs
    public static <E extends Enum<E>> Optional<String> buildOptionalEmbedded(final Enum<E>... embeddedField) {
        return Optional.of(buildEmbedded(embeddedField));
    }

    /**
     * Method to build a string of embedded fields in following the format:
     * "field_1, field_2, field_3".
     *
     * Returns an empty string if the given enum array is empty.
     * @param embeddedField
     * @return
     */
    @SafeVarargs
    public static <E extends Enum<E>> String buildEmbedded(final Enum<E>... embeddedField) {
        final StringBuilder sb = new StringBuilder();
        if (embeddedField.length > 0) {
            sb.append(embeddedField[0]);
            for (int i = 1; i < embeddedField.length; i++) {
                sb.append(",").append(embeddedField[i].toString());
            }
        }
        return sb.toString();
    }

    /**
     * Method to build a string of embedded fields in following the format:
     * "field_1, field_2, field_3".
     *
     * Returns an empty string if the given list is empty.
     * @param embeddedFieldList
     * @return
     */
    public static <E extends Enum<E>> Optional<String> buildOptionalEmbedded(final List<E> embeddedFieldList) {
        return Optional.of(buildEmbedded(embeddedFieldList));
    }

    /**
     * Method to build a string of embedded fields in following the format:
     * "field_1, field_2, field_3".
     *
     * Returns an empty string if the given list is empty.
     * @param embeddedFieldList
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> String buildEmbedded(final List<E> embeddedFieldList) {
        return buildEmbedded((Enum<E>[]) embeddedFieldList.toArray());
    }
}
