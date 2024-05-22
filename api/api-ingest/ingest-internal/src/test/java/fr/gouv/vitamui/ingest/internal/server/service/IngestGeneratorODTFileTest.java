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
 *
 */

package fr.gouv.vitamui.ingest.internal.server.service;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitamui.ingest.common.dto.ArchiveUnitDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.io.FileNotFoundException;
import java.util.List;

class IngestGeneratorODTFileTest {

    IngestGeneratorODTFile ingestGeneratorODTFile = new IngestGeneratorODTFile();

    @Test
    void getValuesForDynamicTable_avoid_attachment_units_with_updateOperation() throws FileNotFoundException {
        Document atr = ingestGeneratorODTFile.convertStringToXMLDocument(
            PropertiesUtils.getResourceAsString("ODT_report/atr.xml")
        );
        Document manifest = ingestGeneratorODTFile.convertStringToXMLDocument(
            PropertiesUtils.getResourceAsString("ODT_report/manifest.xml")
        );

        List<ArchiveUnitDto> archiveUnitDtos = ingestGeneratorODTFile.getValuesForDynamicTable(atr, manifest);

        Assertions.assertEquals(archiveUnitDtos.size(), 249);
        Assertions.assertFalse(archiveUnitDtos.stream().anyMatch(a -> a.getTitle().equals("Discours du ministre")));
    }
}
