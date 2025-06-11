/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.archives.search.server.service;

import fr.gouv.vitamui.iam.openapiclient.ExternalParametersApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Optional;

/**
 * The service to retrieve profil thresholds.
 */
@Service
public class ArchiveSearchThresholdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveSearchThresholdService.class);
    public static final String PARAM_BULK_OPERATIONS_THRESHOLD_NAME = "PARAM_BULK_OPERATIONS_THRESHOLD";

    private final ExternalParametersApi externalParametersApi;

    public ArchiveSearchThresholdService(final ExternalParametersApi externalParametersApi) {
        this.externalParametersApi = externalParametersApi;
    }

    /**
     * Service to return the threshold defined on profil using external parameters
     *
     * @return Optional of threshold otherwise Optional.empty
     */
    public Optional<Long> retrieveProfilThresholds() {
        Optional<Long> thresholdOpt = Optional.empty();
        Map<String, String> myExternalParameter = externalParametersApi.getMyExternalParameters();
        if (myExternalParameter != null && !CollectionUtils.isEmpty(myExternalParameter.entrySet())) {
            Map.Entry<String, String> parameterThreshold = myExternalParameter
                .entrySet()
                .stream()
                .filter(parameter -> PARAM_BULK_OPERATIONS_THRESHOLD_NAME.equals(parameter.getKey()))
                .findFirst()
                .orElse(null);
            if (parameterThreshold != null && parameterThreshold.getValue() != null) {
                try {
                    Long thresholdValue = Long.valueOf(parameterThreshold.getValue());
                    thresholdOpt = Optional.of(thresholdValue);
                } catch (NumberFormatException nfe) {
                    LOGGER.error(
                        "external parameter of bulk threshold contains wrong integer value {}, it will not be used ",
                        parameterThreshold.getValue()
                    );
                    throw new IllegalArgumentException(
                        "external parameter of bulk threshold contains wrong integer value " +
                        parameterThreshold.getValue() +
                        ", it will not be used "
                    );
                }
            }
        }
        return thresholdOpt;
    }
}
