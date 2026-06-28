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

package fr.gouv.vitamui.collect.server.service;

import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ProjectObjectGroupServiceTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    CollectService collectService;

    ProjectObjectGroupService projectObjectGroupService;

    @BeforeEach
    public void beforeEach() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        projectObjectGroupService = new ProjectObjectGroupService(collectService, objectMapper);
    }

    @Test
    void findObjectById() throws VitamClientException, JacksonException {
        String resultStringValue =
            """
            {
              "httpCode": 200,
              "$hits": {
                "total": 1,
                "offset": 0,
                "limit": 0,
                "size": 1
              },
              "$results": [
                {
                  "_id": "aebaaaaaaehjuynkaa3goamemgtl6wiaaaba",
                  "_tenant": 1,
                  "FileInfo": {
                    "Filename": "file1.pem"
                  },
                  "_nbc": 1,
                  "_opi": "aeeaaaaaaghjuynkaa3goamemgtj73yaaaaq",
                  "_qualifiers": [
                    {
                      "qualifier": "BinaryMaster",
                      "_nbc": 1,
                      "versions": [
                        {
                          "_id": "aebqaaaaaghjuynkaa3goamemgtl6wiaaaaq",
                          "DataObjectVersion": "BinaryMaster_1",
                          "FormatIdentification": {
                            "FormatLitteral": "Plain Text File",
                            "MimeType": "text/plain",
                            "FormatId": "x-fmt/111"
                          },
                          "FileInfo": {
                            "Filename": "file1.pem"
                          },
                          "Size": 2313,
                          "Uri": "Content/aebqaaaaaghjuynkaa3goamemgtl6wiaaaaq.pem",
                          "MessageDigest": "5391974484dbd1a3a9c4d3892f4bfc19c4b79fd4b27e8059ce92ca742e7f627b9f6dfa7a9c27484254214615210e796eeb29440da97251388a942a3d581c594e",
                          "Algorithm": "SHA-512",
                          "_opi": "aeeaaaaaaghjuynkaa3goamemgtj73yaaaaq"
                        }
                      ]
                    }
                  ],
                  "_acd": "2022-11-10T13:07:08.784",
                  "_aud": "2022-11-10T13:07:09.039",
                  "_v": 1,
                  "_av": 1
                }
              ],
              "$facetResults": [],
              "$context": {}
            }\
            """;
        RequestResponse<JsonNode> mockResponse = RequestResponse.parseFromResponse(
            Response.ok(resultStringValue).build()
        );
        Mockito.when(collectService.getObjectById(any(), eq("aebaaaaaaehjuynkaa3goamemgtl6wiaaaba"))).thenReturn(
            mockResponse
        );

        ResultsDto resultsDto = projectObjectGroupService.findObjectById("aebaaaaaaehjuynkaa3goamemgtl6wiaaaba", null);

        Assertions.assertEquals(resultsDto.getId(), "aebaaaaaaehjuynkaa3goamemgtl6wiaaaba");
        Assertions.assertEquals(resultsDto.getOpi(), "aeeaaaaaaghjuynkaa3goamemgtj73yaaaaq");
        Assertions.assertEquals(resultsDto.getQualifiers().size(), 1);
        Assertions.assertEquals(resultsDto.getQualifiers().get(0).getQualifier(), "BinaryMaster");
        Assertions.assertEquals(resultsDto.getQualifiers().get(0).getVersions().size(), 1);
        Assertions.assertEquals(
            resultsDto.getQualifiers().get(0).getVersions().get(0).getFileInfoModel().getFilename(),
            "file1.pem"
        );
        Assertions.assertEquals(
            resultsDto.getQualifiers().get(0).getVersions().get(0).getFormatIdentification().getMimeType(),
            "text/plain"
        );
        Assertions.assertEquals(
            resultsDto.getQualifiers().get(0).getVersions().get(0).getDataObjectVersion(),
            "BinaryMaster_1"
        );
    }
}
