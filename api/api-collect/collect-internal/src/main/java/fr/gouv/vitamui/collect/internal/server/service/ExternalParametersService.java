/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.collect.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ParameterDto;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.iam.internal.client.ExternalParametersInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The service to retrieve profile thresholds.
 */
@Service
public class ExternalParametersService {

    public static final String PARAM_ACCESS_CONTRACT_NAME = "PARAM_ACCESS_CONTRACT";

    private final ExternalParametersInternalRestClient externalParametersInternalRestClient;
    private final InternalSecurityService securityService;
    private final AccessContractService accessContractService;

    @Autowired
    public ExternalParametersService(
        final ExternalParametersInternalRestClient externalParametersInternalRestClient,
        final InternalSecurityService securityService,
        AccessContractService accessContractService
    ) {
        this.externalParametersInternalRestClient = externalParametersInternalRestClient;
        this.securityService = securityService;
        this.accessContractService = accessContractService;
    }

    /**
     * Service to return the access contract defined on profil using external parameters
     *
     * @return access contract throws IllegalArgumentException
     */
    private @Nonnull String retrieveAccessContractFromExternalParam() {
        ExternalParametersDto myExternalParameter = externalParametersInternalRestClient.getMyExternalParameters(
            securityService.getHttpContext()
        );
        if (myExternalParameter == null || CollectionUtils.isEmpty(myExternalParameter.getParameters())) {
            throw new IllegalArgumentException("No external profile defined for access contract defined");
        }

        ParameterDto parameterAccessContract = myExternalParameter
            .getParameters()
            .stream()
            .filter(parameter -> PARAM_ACCESS_CONTRACT_NAME.equals(parameter.getKey()))
            .findFirst()
            .orElse(null);
        if (Objects.isNull(parameterAccessContract) || Objects.isNull(parameterAccessContract.getValue())) {
            throw new IllegalArgumentException("No access contract defined");
        }
        return parameterAccessContract.getValue();
    }

    public @Nullable AccessContractModel retrieveAccessContract() throws VitamClientException, JsonProcessingException {
        final RequestResponse<AccessContractModel> response = accessContractService.findAccessContractById(
            buildVitamContextFromExternalParam(),
            retrieveAccessContractFromExternalParam()
        );
        return (
                response != null &&
                response.isOk() &&
                CollectionUtils.isNotEmpty(((RequestResponseOK<?>) response).getResults())
            )
            ? (AccessContractModel) ((RequestResponseOK<?>) response).getResults().get(0)
            : null;
    }

    /**
     * This function create a VitamContext
     *
     * @return
     */
    public VitamContext buildVitamContextFromExternalParam() {
        return new VitamContext(securityService.getTenantIdentifier())
            .setAccessContract(retrieveAccessContractFromExternalParam())
            .setApplicationSessionId(securityService.getApplicationId());
    }
}
