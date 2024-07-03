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
package fr.gouv.vitamui.collect.internal.server.service;

import fr.gouv.vitamui.collect.internal.server.dao.SearchCriteriaHistoryRepository;
import fr.gouv.vitamui.collect.internal.server.domain.SearchCriteriaHistoryCollect;
import fr.gouv.vitamui.collect.internal.server.service.converters.SearchCriteriaHistoryConverter;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Getter
@Setter
public class SearchCriteriaHistoryInternalService
    extends VitamUICrudService<SearchCriteriaHistoryDto, SearchCriteriaHistoryCollect> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchCriteriaHistoryInternalService.class);
    private static final String MAX_SEARCH_CRITERIA_SAVED_ACHIEVED =
        "L’enregistrement n'est pas possible car vous avez atteint le nombre limite de recherches enregistrées. Veuillez supprimer au moins une de vos recherches.";

    private final SearchCriteriaHistoryRepository searchCriteriaHistoryRepo;

    private final SearchCriteriaHistoryConverter searchCriteriaHistoryConverter;

    private final InternalSecurityService internalSecurityService;

    @Autowired
    public SearchCriteriaHistoryInternalService(
        final CustomSequenceRepository sequenceRepository,
        final SearchCriteriaHistoryRepository searchCriteriaHistoryRepo,
        final SearchCriteriaHistoryConverter searchCriteriaHistoryConverter,
        final InternalSecurityService internalSecurityService
    ) {
        super(sequenceRepository);
        this.searchCriteriaHistoryRepo = searchCriteriaHistoryRepo;
        this.searchCriteriaHistoryConverter = searchCriteriaHistoryConverter;
        this.internalSecurityService = internalSecurityService;
    }

    /**
     * Retrieve the search criteria history of the specific authentified user.
     *
     * @return
     */
    public List<SearchCriteriaHistoryDto> getSearchCriteriaHistoryDtos() {
        LOGGER.debug("getSearchCriteriaHistoryDtos");
        AuthUserDto authUserDto = internalSecurityService.getUser();

        LOGGER.debug("Get the search history for user : {}", authUserDto.getIdentifier());
        QueryDto criteria = new QueryDto();
        criteria.addCriterion(new Criterion("userId", authUserDto.getIdentifier(), CriterionOperator.EQUALS));
        return this.getAll(criteria);
    }

    @Override
    protected void beforeCreate(final SearchCriteriaHistoryDto dto) {
        AuthUserDto authUserDto = internalSecurityService.getUser();
        dto.setUserId(authUserDto.getIdentifier());
        List<SearchCriteriaHistoryDto> list = getSearchCriteriaHistoryDtos();
        if (null != list && list.size() >= 10) {
            LOGGER.warn(MAX_SEARCH_CRITERIA_SAVED_ACHIEVED);
            throw new IllegalArgumentException(MAX_SEARCH_CRITERIA_SAVED_ACHIEVED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SearchCriteriaHistoryRepository getRepository() {
        return searchCriteriaHistoryRepo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<SearchCriteriaHistoryCollect> getEntityClass() {
        return SearchCriteriaHistoryCollect.class;
    }

    @Override
    protected String getObjectName() {
        return "searchCriteriaHistory";
    }

    @Override
    protected Converter<SearchCriteriaHistoryDto, SearchCriteriaHistoryCollect> getConverter() {
        return searchCriteriaHistoryConverter;
    }
}
