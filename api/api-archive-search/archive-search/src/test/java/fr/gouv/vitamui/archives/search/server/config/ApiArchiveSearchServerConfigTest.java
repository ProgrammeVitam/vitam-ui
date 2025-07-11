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

package fr.gouv.vitamui.archives.search.server.config;

import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.v2.AccessExternalClientV2;
import fr.gouv.vitam.ingest.external.client.IngestExternalClient;
import fr.gouv.vitamui.archives.search.server.searchcriteria.service.SearchCriteriaHistoryService;
import fr.gouv.vitamui.archives.search.server.service.ArchiveSearchAgenciesService;
import fr.gouv.vitamui.archives.search.server.service.ArchiveSearchService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyCommonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class ApiArchiveSearchServerConfigTest {

    @MockitoBean(name = "adminExternalClient")
    private AdminExternalClient adminExternalClient;

    @MockitoBean(name = "accessExternalClient")
    private AccessExternalClient accessExternalClient;

    @MockitoBean(name = "accessExternalClientV2")
    private AccessExternalClientV2 accessExternalClientV2;

    @MockitoBean(name = "ingestExternalClient")
    private IngestExternalClient ingestExternalClient;

    @MockitoBean(name = "agencyCommonService")
    private AgencyCommonService agencyCommonService;

    @Autowired
    private ArchiveSearchService archiveSearchService;

    @Autowired
    private ArchiveSearchAgenciesService archiveSearchAgenciesService;

    @Test
    void testArchiveInternalConf() {
        Assertions.assertNotNull(archiveSearchService);
    }

    @Autowired
    private SearchCriteriaHistoryService searchCriteriaHistoryService;

    @Test
    void testSearchCriteriaHistoryServiceConf() {
        Assertions.assertNotNull(searchCriteriaHistoryService);
    }
}
