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
package fr.gouv.vitamui.collect.external.server.service;

import fr.gouv.vitamui.collect.internal.client.SearchCriteriaHistoryInternalRestClient;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * The service to read, create and delete the search criterias.
 *
 *
 */
@Getter
@Setter
@Service
public class SearchCriteriaHistoryExternalService extends AbstractResourceClientService<SearchCriteriaHistoryDto, SearchCriteriaHistoryDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        SearchCriteriaHistoryExternalService.class);

	private final SearchCriteriaHistoryInternalRestClient searchCriteriaHistoryInternalRestClient;

    @Autowired
    public SearchCriteriaHistoryExternalService(final SearchCriteriaHistoryInternalRestClient searchCriteriaHistoryInternalRestClient,
    		final ExternalSecurityService externalSecurityService) {
        super(externalSecurityService);
        this.searchCriteriaHistoryInternalRestClient = searchCriteriaHistoryInternalRestClient;
    }

    public List<SearchCriteriaHistoryDto> getSearchCriteriaHistory() {
        return searchCriteriaHistoryInternalRestClient.getSearchCriteriaHistory(getInternalHttpContext());
    }

    @Override
    public SearchCriteriaHistoryDto getOne(final String id, final Optional<String> embedded) {
        return super.getOne(id, embedded);
    }

    @Override
    @Transactional
    public SearchCriteriaHistoryDto create(final SearchCriteriaHistoryDto dto) {
        return super.create(dto);
    }

    @Override
    public void delete(final String id) {
        super.delete(id);
    }

    @Override
    public SearchCriteriaHistoryDto update(final SearchCriteriaHistoryDto dto) {
        LOGGER.debug("service external = Update SearchCriteriaHistory with id :{}", dto.getId());
        return super.update(dto);
    }

    @Override
    protected SearchCriteriaHistoryInternalRestClient getClient() {
        return searchCriteriaHistoryInternalRestClient;
    }
}
