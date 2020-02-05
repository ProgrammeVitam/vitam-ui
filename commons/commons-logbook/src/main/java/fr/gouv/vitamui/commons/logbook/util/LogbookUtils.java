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
package fr.gouv.vitamui.commons.logbook.util;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import fr.gouv.vitamui.commons.utils.JsonUtils;

/**
 *
 * Logbook utility
 *
 *
 */
public class LogbookUtils {

    private static final String DIFF_KEY_WORDS = "diff";

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(LogbookUtils.class);

    /**
     * Method for get evData from a list of logbookDataUpdateDto
     * @param logbooks
     * @return
     */
    public static ObjectNode getEvData(final Collection<EventDiffDto> logbooks) {
        try {
            ObjectNode evData = JsonUtils.getEmptyObjectNode();
            ObjectNode diff = JsonUtils.getEmptyObjectNode();
            logbooks.forEach((l) -> {
                if (StringUtils.isBlank(l.getKey())) {
                    throw new IllegalArgumentException("key should not be blank");
                }
                String key = StringUtils.capitalize(l.getKey().toLowerCase());
                String oldValue = l.getOldValue() != null ? l.getOldValue().toString() : StringUtils.EMPTY;
                String newValue = l.getNewValue() != null ? l.getNewValue().toString() : StringUtils.EMPTY;
                diff.put("-" + key, oldValue);
                diff.put("+" + key, newValue);

            });
            evData.set(DIFF_KEY_WORDS, diff);
            return evData;
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw ApiErrorGenerator.getInternalServerException(e);
        }
    }

    public static String getValue(final Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        } else {
            return value.toString();
        }
    }
}
