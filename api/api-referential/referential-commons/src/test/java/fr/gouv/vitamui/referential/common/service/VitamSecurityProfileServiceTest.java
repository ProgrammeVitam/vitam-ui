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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.FileFormatModel;
import fr.gouv.vitam.common.model.administration.SecurityProfileModel;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VitamSecurityProfileServiceTest {

    @Mock
    private AdminExternalClient adminExternalClient;

    private VitamSecurityProfileService vitamSecurityProfileService;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        vitamSecurityProfileService = new VitamSecurityProfileService(adminExternalClient, objectMapper);
    }

    @Test
    public void patchSecurityProfile_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.updateSecurityProfile(vitamSecurityProfile, id, jsonNode)).thenReturn(
            new RequestResponseOK<FileFormatModel>().setHttpCode(200)
        );

        assertThatCode(
            () -> vitamSecurityProfileService.patchSecurityProfile(vitamSecurityProfile, id, jsonNode)
        ).doesNotThrowAnyException();
    }

    @Test
    public void patchSecurityProfile_should_return_ok_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.updateSecurityProfile(vitamSecurityProfile, id, jsonNode)).thenReturn(
            new RequestResponseOK<FileFormatModel>().setHttpCode(400)
        );

        assertThatCode(
            () -> vitamSecurityProfileService.patchSecurityProfile(vitamSecurityProfile, id, jsonNode)
        ).doesNotThrowAnyException();
    }

    @Test
    public void patchSecurityProfile_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.updateSecurityProfile(vitamSecurityProfile, id, jsonNode)).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(
            () -> vitamSecurityProfileService.patchSecurityProfile(vitamSecurityProfile, id, jsonNode)
        ).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void deleteSecurityProfile_should_return_ok_when_vitamclient_ok()
        throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "1";

        when(
            adminExternalClient.createSecurityProfiles(
                any(VitamContext.class),
                any(InputStream.class),
                any(String.class)
            )
        ).thenReturn(new RequestResponseOK().setHttpCode(200));

        when(adminExternalClient.findSecurityProfiles(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK().setHttpCode(200)
        );

        assertThatCode(
            () -> vitamSecurityProfileService.deleteSecurityProfile(vitamSecurityProfile, id)
        ).doesNotThrowAnyException();
    }

    @Test
    public void deleteSecurityProfile_should_throw_JsonProcessingException_when_vitamclient_400()
        throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "1";

        when(
            adminExternalClient.createSecurityProfiles(
                any(VitamContext.class),
                any(InputStream.class),
                any(String.class)
            )
        ).thenReturn(new RequestResponseOK().setHttpCode(400));

        when(adminExternalClient.findSecurityProfiles(any(VitamContext.class), any(ObjectNode.class))).thenReturn(
            new RequestResponseOK().setHttpCode(400)
        );

        assertThatCode(
            () -> vitamSecurityProfileService.deleteSecurityProfile(vitamSecurityProfile, id)
        ).doesNotThrowAnyException();
    }

    @Test
    public void deleteSecurityProfile_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException, AccessExternalClientException, InvalidParseOperationException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String id = "id_0";

        when(adminExternalClient.findSecurityProfiles(vitamSecurityProfile, new Select().getFinalSelect())).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(() -> vitamSecurityProfileService.deleteSecurityProfile(vitamSecurityProfile, id)).isInstanceOf(
            VitamClientException.class
        );
    }

    @Test
    public void findSecurityProfiles_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findSecurityProfiles(vitamSecurityProfile, jsonNode)).thenReturn(
            new RequestResponseOK<SecurityProfileModel>().setHttpCode(200)
        );

        assertThatCode(
            () -> vitamSecurityProfileService.findSecurityProfiles(vitamSecurityProfile, jsonNode)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findSecurityProfiles_should_throw_BadRequestException_when_vitamclient_400()
        throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findSecurityProfiles(vitamSecurityProfile, jsonNode)).thenReturn(
            new RequestResponseOK<SecurityProfileModel>().setHttpCode(400)
        );

        assertThatCode(
            () -> vitamSecurityProfileService.findSecurityProfiles(vitamSecurityProfile, jsonNode)
        ).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findSecurityProfiles_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        JsonNode jsonNode = JsonHandler.createObjectNode();

        when(adminExternalClient.findSecurityProfiles(vitamSecurityProfile, jsonNode)).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(
            () -> vitamSecurityProfileService.findSecurityProfiles(vitamSecurityProfile, jsonNode)
        ).isInstanceOf(VitamClientException.class);
    }

    @Test
    public void findSecurityProfileById_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String securityProfileId = "SPId_0";

        when(adminExternalClient.findSecurityProfileById(vitamSecurityProfile, securityProfileId)).thenReturn(
            new RequestResponseOK<SecurityProfileModel>().setHttpCode(200)
        );

        assertThatCode(
            () -> vitamSecurityProfileService.findSecurityProfileById(vitamSecurityProfile, securityProfileId)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findSecurityProfileById_should_return_InternalServerException_when_vitamclient_400()
        throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String securityProfileId = "SPId_0";

        when(adminExternalClient.findSecurityProfileById(vitamSecurityProfile, securityProfileId)).thenReturn(
            new RequestResponseOK<SecurityProfileModel>().setHttpCode(400)
        );

        assertThatCode(
            () -> vitamSecurityProfileService.findSecurityProfileById(vitamSecurityProfile, securityProfileId)
        ).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void findSecurityProfileById_should_throw_VitamClientException_when_vitamclient_throws_VitamClientException()
        throws VitamClientException {
        VitamContext vitamSecurityProfile = new VitamContext(0);
        String securityProfileId = "SPId_0";

        when(adminExternalClient.findSecurityProfileById(vitamSecurityProfile, securityProfileId)).thenThrow(
            new VitamClientException("Exception thrown by Vitam")
        );

        assertThatCode(
            () -> vitamSecurityProfileService.findSecurityProfileById(vitamSecurityProfile, securityProfileId)
        ).isInstanceOf(VitamClientException.class);
    }
}
