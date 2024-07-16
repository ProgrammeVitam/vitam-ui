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
package fr.gouv.vitamui.referential.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.error.VitamError;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.FileFormatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VitamUIAccessContractServiceTest {

    @Mock
    private AdminExternalClient adminExternalClient;

    @InjectMocks
    private VitamUIAccessContractService vitamUIAccessContractService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(VitamUIAccessContractServiceTest.class);
    }

    @Test
    public void patchAccessContract_should_return_ok_when_vitamclient_ok()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.updateAccessContract(vitamSecurityProfile, id, jsonNode)).thenReturn(
            new RequestResponseOK<FileFormatModel>().setHttpCode(200)
        );

        assertThatCode(
            () -> vitamUIAccessContractService.patchAccessContract(vitamSecurityProfile, id, jsonNode)
        ).doesNotThrowAnyException();
    }

    @Test
    public void patchAccessContract_should_return_400_when_vitamclient_400()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.updateAccessContract(vitamSecurityProfile, id, jsonNode)).thenReturn(
            new VitamError("ERR_VITAM").setHttpCode(400).setMessage("DSL malformated")
        );

        assertThatCode(
            () -> vitamUIAccessContractService.patchAccessContract(vitamSecurityProfile, id, jsonNode)
        ).hasMessageContaining("DSL malformated");
    }

    @Test
    public void patchAccessContract_should_throw_InvalidParseOperationException_when_vitamclient_throws_InvalidParseOperationException()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.updateAccessContract(vitamSecurityProfile, id, jsonNode)).thenThrow(
            new InvalidParseOperationException("Exception thrown by Vitam")
        );

        assertThatCode(
            () -> vitamUIAccessContractService.patchAccessContract(vitamSecurityProfile, id, jsonNode)
        ).isInstanceOf(InvalidParseOperationException.class);
    }

    @Test
    public void patchAccessContract_should_throw_AccessExternalClientException_when_vitamclient_throws_AccessExternalClientException()
        throws InvalidParseOperationException, AccessExternalClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.updateAccessContract(eq(vitamSecurityProfile), eq(id), eq(jsonNode))).thenThrow(
            new AccessExternalClientException("Exception thrown by Vitam")
        );

        assertThatCode(
            () -> vitamUIAccessContractService.patchAccessContract(vitamSecurityProfile, id, jsonNode)
        ).isInstanceOf(AccessExternalClientException.class);
    }
}
