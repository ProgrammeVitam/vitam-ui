/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.archives.search.service;

import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalRestClient;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalWebClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ArchivesSearchServiceTest {
    public final String ARCHIVE_UNITS_RESULTS_CSV = "data/vitam_archive_units_response.csv";

    private ArchivesSearchService archivesSearchService;

    @Mock
    private ArchiveSearchExternalRestClient archiveSearchExternalRestClient;

    @Mock
    private ArchiveSearchExternalWebClient archiveSearchExternalWebClient;

    @Mock
    private CommonService commonService;

    @Before
    public void init() {
        archivesSearchService = new ArchivesSearchService(commonService, archiveSearchExternalRestClient,
            archiveSearchExternalWebClient);
    }

    @Test
    public void testIngest() {
        Assert.assertNotNull(archivesSearchService);
    }

    @Test
    public void testGetFilingHolding() {
        when(archiveSearchExternalRestClient.getFilingHoldingScheme(ArgumentMatchers.any())).thenReturn(
            new VitamUISearchResponseDto());
        Assert.assertNotNull(archivesSearchService.findFilingHoldingScheme(null));
    }

    @Test
    public void testExportCsv() throws IOException {
        Resource resource = new ByteArrayResource(ArchivesSearchServiceTest.class.getClassLoader()
            .getResourceAsStream(ARCHIVE_UNITS_RESULTS_CSV).readAllBytes());
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        SearchCriteriaDto query = new SearchCriteriaDto();

        when(archiveSearchExternalRestClient
            .exportCsvArchiveUnitsByCriteria(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
            (new ResponseEntity<>(resource, HttpStatus.OK)));
        Assert.assertNotNull(archivesSearchService.exportCsvArchiveUnitsByCriteria(query, context));
    }
}
