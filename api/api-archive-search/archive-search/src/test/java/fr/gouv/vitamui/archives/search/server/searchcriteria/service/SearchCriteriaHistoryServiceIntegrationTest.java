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
package fr.gouv.vitamui.archives.search.server.searchcriteria.service;

import fr.gouv.vitamui.archives.search.server.config.ConverterConfig;
import fr.gouv.vitamui.archives.search.server.searchcriteria.converter.SearchCriteriaHistoryConverter;
import fr.gouv.vitamui.archives.search.server.searchcriteria.dao.SearchCriteriaHistoryRepository;
import fr.gouv.vitamui.archives.search.server.searchcriteria.domain.SearchCriteriaHistory;
import fr.gouv.vitamui.archives.search.server.utils.Utils;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Import({ ConverterConfig.class, VitamClientTestConfig.class })
@ActiveProfiles("test")
public class SearchCriteriaHistoryServiceIntegrationTest extends AbstractMongoTests {

    private AutoCloseable mocks;

    private SearchCriteriaHistoryService service;

    private final SequenceGeneratorService sequenceGeneratorService = mock(SequenceGeneratorService.class);

    @Autowired
    private SearchCriteriaHistoryRepository repository;

    @Autowired
    private SearchCriteriaHistoryConverter searchCriteriaHistoryConverter;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockitoBean
    protected SecurityService securityService;

    @BeforeEach
    public void setup() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);

        service = new SearchCriteriaHistoryService(
            sequenceGeneratorService,
            repository,
            searchCriteriaHistoryConverter,
            securityService
        );
        repository.deleteAll();

        final AuthUserDto user = Utils.buildAuthUserDto();

        Mockito.when(securityService.getUser()).thenReturn(user);
    }

    @AfterEach
    public void cleanUp() throws Exception {
        repository.deleteAll();
        mocks.close();
    }

    @Test
    void testCreateSearchCriteria() {
        final SearchCriteriaHistory searchCriteriaHistory = Utils.buildSearchCriteriaHistory();
        SearchCriteriaHistoryDto searchCriteriaHistoryDto = searchCriteriaHistoryConverter.convertEntityToDto(
            searchCriteriaHistory
        );

        assertThat(searchCriteriaHistoryDto.getName()).isNotBlank();
        searchCriteriaHistory.setUserId("code");
        repository.save(searchCriteriaHistory);

        assertThat(service.getSearchCriteriaHistoryDtos()).hasSize(1);
    }
}
