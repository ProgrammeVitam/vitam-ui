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

package fr.gouv.vitamui.collect.internal.server.service.externalparameters;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.collect.internal.server.service.externalParameters.AccessContractConverter;
import fr.gouv.vitamui.collect.internal.server.service.externalParameters.AccessContractInternalService;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AccessContractInternalServiceTest {

    private AccessContractInternalService accessContractInternalService;

    private final AccessContractService accessContractService = mock(AccessContractService.class);

    ObjectMapper objectMapper = new ObjectMapper();

    private final AccessContractConverter converter = new AccessContractConverter();

    @BeforeEach
    public void setup() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        accessContractInternalService = new AccessContractInternalService(accessContractService, objectMapper, converter);

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void getOne_should_return_ok_when_vitamClient_ok() throws VitamClientException {

        VitamContext vitamContext = new VitamContext(1);
        final String accessContractIdentifier = "contractTNR";
        Set<String> rootUnits = new HashSet<>();

        RequestResponseOK<AccessContractModel> response1 = new RequestResponseOK<>();
        response1.setHttpCode(200);
        response1.setHits(1, 1, 1, 1);
        response1.addResult(createAccessContractModel(accessContractIdentifier, "contrat d'accès", 0, true,
            false, rootUnits));

        when(accessContractService.findAccessContractById(any(), any()))
            .thenReturn(response1);

        // THEN
        assertThatCode(()-> accessContractInternalService.getOne(vitamContext, accessContractIdentifier))
            .doesNotThrowAnyException();

    }

    @Test
    void getOne_should_throw_InternalServerException_when_vitamClient_throw_VitamClientException() throws VitamClientException {

        VitamContext vitamContext = new VitamContext(1);
        final String accessContractIdentifier = "contractTNR";

        when(accessContractService.findAccessContractById(any(), any()))
            .thenThrow(new VitamClientException("Exception thrown by Vitam"));

        assertThatCode(() -> {
            accessContractInternalService.getOne(vitamContext, accessContractIdentifier);
        }).isInstanceOf(InternalServerException.class)
            .hasMessageContaining("Unable to get Access Contract");
    }

    @Test
    void getOne_should_return_ok_when_vitamClient_send_not_found_message() throws VitamClientException {

        VitamContext vitamContext = new VitamContext(1);
        final String accessContractIdentifier = "contractTNR";

        RequestResponseOK<AccessContractModel> response1 = new RequestResponseOK<>();
        response1.setHttpCode(400);
        response1.setHits(1, 1, 1, 1);

        when(accessContractService.findAccessContractById(any(), any()))
            .thenReturn(response1);

        // THEN
        assertThatCode(()-> accessContractInternalService.getOne(vitamContext, accessContractIdentifier))
            .doesNotThrowAnyException();

    }

    private AccessContractModel createAccessContractModel(String identifier, String name, Integer tenant,
        Boolean writingPermission, Boolean everyOriginatingAgency, Set<String> rootUnits) {
        AccessContractModel accessContractModel = new AccessContractModel();
        accessContractModel.setIdentifier(identifier);
        accessContractModel.setName(name);
        accessContractModel.setTenant(tenant);
        accessContractModel.setWritingPermission(writingPermission);
        accessContractModel.setEveryOriginatingAgency(everyOriginatingAgency);
        accessContractModel.setRootUnits(rootUnits);
        return accessContractModel;
    }
}
