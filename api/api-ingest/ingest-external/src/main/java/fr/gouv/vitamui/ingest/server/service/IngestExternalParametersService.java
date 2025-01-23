/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.ingest.server.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.iam.external.client.ExternalParametersExternalRestClient;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * The service to retrieve profile thresholds.
 */
@Service
public class IngestExternalParametersService {

    public static final String PARAM_ACCESS_CONTRACT_NAME = "PARAM_ACCESS_CONTRACT";

    private final ExternalParametersExternalRestClient externalParametersExternalRestClient;
    private final ExternalSecurityService externalSecurityService;

    @Autowired
    public IngestExternalParametersService(
        final ExternalParametersExternalRestClient externalParametersExternalRestClient,
        final ExternalSecurityService externalSecurityService
    ) {
        this.externalParametersExternalRestClient = externalParametersExternalRestClient;
        this.externalSecurityService = externalSecurityService;
    }

    /**
     * Service to return the access contract defined on profile using external parameters
     *
     * @return access contract
     * throws IllegalArgumentException
     */
    public String retrieveAccessContractFromExternalParam() {
        Map<String, String> myExternalParameter = externalParametersExternalRestClient.getMyExternalParameters(
            externalSecurityService.getHttpContext()
        );
        if (myExternalParameter == null || CollectionUtils.isEmpty(myExternalParameter.entrySet())) {
            throw new IllegalArgumentException("No external profile defined for access contract defined");
        }

        Map.Entry<String, String> parameterAccessContract = myExternalParameter
            .entrySet()
            .stream()
            .filter(entry -> PARAM_ACCESS_CONTRACT_NAME.equals(entry.getKey()))
            .findFirst()
            .orElse(null);
        if (Objects.isNull(parameterAccessContract) || Objects.isNull(parameterAccessContract.getValue())) {
            throw new IllegalArgumentException("No access contract defined");
        }
        return parameterAccessContract.getValue();
    }

    /**
     * This function create a VitamContext
     *
     * @return Vitam Context
     */
    public VitamContext buildVitamContextFromExternalParam() {
        return new VitamContext(externalSecurityService.getTenantIdentifier())
            .setAccessContract(retrieveAccessContractFromExternalParam())
            .setApplicationSessionId(externalSecurityService.getApplicationId());
    }
}
