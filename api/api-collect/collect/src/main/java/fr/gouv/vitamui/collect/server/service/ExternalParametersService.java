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

package fr.gouv.vitamui.collect.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractCommonService;
import fr.gouv.vitamui.iam.openapiclient.ExternalParametersApi;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * The service to retrieve profile thresholds.
 * Extends the common ExternalParametersService from iam-security with
 * collect-specific logic to retrieve the full AccessContractModel.
 */
@Service
public class ExternalParametersService extends fr.gouv.vitamui.iam.security.service.ExternalParametersService {

    private final AccessContractCommonService accessContractCommonService;

    public ExternalParametersService(
        final ExternalParametersApi externalParametersApi,
        final SecurityService securityService,
        final AccessContractCommonService accessContractCommonService
    ) {
        super(externalParametersApi, securityService);
        this.accessContractCommonService = accessContractCommonService;
    }

    public @Nullable AccessContractModel retrieveAccessContract() throws VitamClientException, JsonProcessingException {
        final RequestResponse<AccessContractModel> response = accessContractCommonService.findAccessContractById(
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
}
