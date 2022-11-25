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

package fr.gouv.vitamui.collect.service;

import fr.gouv.vitam.common.model.objectgroup.FileInfoModel;
import fr.gouv.vitamui.archives.search.common.dto.ObjectData;
import fr.gouv.vitamui.collect.external.client.CollectExternalRestClient;
import fr.gouv.vitamui.collect.external.client.CollectExternalWebClient;
import fr.gouv.vitamui.commons.vitam.api.dto.QualifiersDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VersionsDto;
import fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierTypeEnum.BINARYMASTER;
import static fr.gouv.vitamui.commons.vitam.api.model.ObjectQualifierTypeEnum.DISSEMINATION;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProjectObjectGroupServiceTest {

    @Mock
    CollectExternalRestClient collectExternalRestClient;
    @Mock
    CollectExternalWebClient collectExternalWebClient;

    @InjectMocks
    ProjectObjectGroupService projectObjectGroupService;

    @BeforeEach
    public void BeforeEach() {
    }

    private ResultsDto newResultDto(List<QualifiersDto> qualifiers) {
        return new ResultsDto()
            .setId("whatever")
            .setQualifiers(qualifiers);
    }

    private FileInfoModel newFileInfoModel(String filename) {
        FileInfoModel fileInfoModel = new FileInfoModel();
        fileInfoModel.setFilename(filename);
        return fileInfoModel;
    }


    private VersionsDto newVersionsDto(String id, ObjectQualifierTypeEnum type, Integer version, String filename) {
        return new VersionsDto()
            .setId(id)
            .setFileInfoModel(newFileInfoModel(filename))
            .setDataObjectVersion(type.getValue() + "_" + version);
    }

    @Test
    public void setObjectData_should_return_binary_master_before_other() {
        // Given
        ObjectData objectData = new ObjectData();
        ResultsDto resultsDto = new ResultsDto()
            .setId("whatever")
            .setQualifiers(List.of(
                new QualifiersDto()
                    .setQualifier("unknow_qualifier")
                    .setVersions(List.of(
                        new VersionsDto().setId("unknow_qualifier_version_1"),
                        new VersionsDto().setId("unknow_qualifier_version_2")
                    )),
                new QualifiersDto()
                    .setQualifier(DISSEMINATION.getValue())
                    .setVersions(List.of(
                        new VersionsDto().setId("DISSEMINATION_1"),
                        new VersionsDto().setId("DISSEMINATION_2")
                    )),
                new QualifiersDto()
                    .setQualifier(BINARYMASTER.getValue())
                    .setVersions(List.of(
                        new VersionsDto().setId("BINARYMASTER_1"),
                        newVersionsDto("aeaaaaaaaahl2zz5ab23malq4gw2cnqaaaaq", BINARYMASTER, 2, "sdfghjk.pouet")
                    ))
            ));
        // When
        projectObjectGroupService.setObjectData(resultsDto, objectData);

        // Then
        assertThat(objectData.getQualifier()).isEqualTo(BINARYMASTER.getValue());
        assertThat(objectData.getVersion()).isEqualTo(2);
        assertThat(objectData.getFilename()).isEqualTo("aeaaaaaaaahl2zz5ab23malq4gw2cnqaaaaq.pouet");
    }

}
