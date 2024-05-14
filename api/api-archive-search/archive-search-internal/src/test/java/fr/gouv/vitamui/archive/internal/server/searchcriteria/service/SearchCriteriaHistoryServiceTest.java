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
package fr.gouv.vitamui.archive.internal.server.searchcriteria.service;

import fr.gouv.vitamui.archive.internal.server.searchcriteria.converter.SearchCriteriaHistoryConverter;
import fr.gouv.vitamui.archive.internal.server.searchcriteria.dao.SearchCriteriaHistoryRepository;
import fr.gouv.vitamui.archive.internal.server.searchcriteria.domain.SearchCriteriaHistory;
import fr.gouv.vitamui.archive.internal.server.utils.Utils;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchCriteriaHistoryServiceTest {

    private SearchCriteriaHistoryInternalService service;

    private final SearchCriteriaHistoryRepository ssearchCriteriaHistoryRepository = mock(
        SearchCriteriaHistoryRepository.class
    );

    private final InternalSecurityService internalSecurityService = mock(InternalSecurityService.class);

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        SearchCriteriaHistoryServiceTest.class
    );

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private final SearchCriteriaHistoryConverter searchCriteriaHistoryConverter = new SearchCriteriaHistoryConverter();

    @BeforeEach
    public void setup() throws Exception {
        service = new SearchCriteriaHistoryInternalService(
            sequenceRepository,
            ssearchCriteriaHistoryRepository,
            searchCriteriaHistoryConverter,
            internalSecurityService
        );

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void testCreateSearchCriteriaHistory() throws Exception {
        final SearchCriteriaHistoryDto searchCriteriaHistoryDto = searchCriteriaHistoryConverter.convertEntityToDto(
            Utils.buildSearchCriteriaHistory()
        );

        final SearchCriteriaHistory other = new SearchCriteriaHistory();
        VitamUIUtils.copyProperties(searchCriteriaHistoryDto, other);
        other.setId(UUID.randomUUID().toString());

        when(ssearchCriteriaHistoryRepository.save(ArgumentMatchers.any())).thenReturn(other);

        final AuthUserDto user = Utils.buildAuthUserDto();

        Mockito.when(internalSecurityService.getUser()).thenReturn(user);

        final SearchCriteriaHistoryDto created = service.create(searchCriteriaHistoryDto);

        final SearchCriteriaHistoryDto result = new SearchCriteriaHistoryDto();
        result.setId(created.getId());
        result.setUserId(created.getUserId());
        result.setName(created.getName());
        result.setSearchCriteriaList(created.getSearchCriteriaList());
        result.setSavingDate(created.getSavingDate());

        Assertions.assertNotNull("SearchCriteriaHistoryDto id should be defined", created.getId());
        assertThat(result).isEqualToComparingFieldByField(created);
    }
}
