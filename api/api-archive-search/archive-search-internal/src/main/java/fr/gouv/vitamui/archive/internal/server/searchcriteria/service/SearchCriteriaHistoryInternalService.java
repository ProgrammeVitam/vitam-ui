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
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.List;

@Getter
@Setter
public class SearchCriteriaHistoryInternalService extends VitamUICrudService<SearchCriteriaHistoryDto, SearchCriteriaHistory> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SearchCriteriaHistoryInternalService.class);

    private final SearchCriteriaHistoryRepository searchCriteriaHistoryRepo;

    private final SearchCriteriaHistoryConverter searchCriteriaHistoryConverter;

    private final InternalSecurityService internalSecurityService;

    @Autowired
    public SearchCriteriaHistoryInternalService(
    		final CustomSequenceRepository sequenceRepository,
    		final SearchCriteriaHistoryRepository searchCriteriaHistoryRepo,
    		final SearchCriteriaHistoryConverter searchCriteriaHistoryConverter,
    		final InternalSecurityService internalSecurityService) {
        super(sequenceRepository);
        this.searchCriteriaHistoryRepo = searchCriteriaHistoryRepo;
        this.searchCriteriaHistoryConverter = searchCriteriaHistoryConverter;
        this.internalSecurityService = internalSecurityService;
    }

    /**
     * Retrieve the search criteria history of the specific authentified user.
     * @return
     */
    public List<SearchCriteriaHistoryDto> getSearchCriteriaHistoryDtos() {
        LOGGER.debug("getSearchCriteriaHistoryDtos");
    	AuthUserDto authUserDto = internalSecurityService.getUser();

        LOGGER.debug("Get the search history for user : {}", authUserDto.getIdentifier());
        QueryDto criteria = new QueryDto();
        criteria.addCriterion(new Criterion("userId", authUserDto.getIdentifier() , CriterionOperator.EQUALS));
        List<SearchCriteriaHistoryDto> searchCriteriaHistoryDtoList = this.getAll(criteria);
        return searchCriteriaHistoryDtoList;
    }

    @Override
    protected void beforeCreate(final SearchCriteriaHistoryDto dto) {
        AuthUserDto authUserDto = internalSecurityService.getUser();
        dto.setUserId(authUserDto.getIdentifier());
        List<SearchCriteriaHistoryDto> list = getSearchCriteriaHistoryDtos();
        Assert.isTrue(list != null && list.size() < 10, "L’enregistrement n'est pas possible car vous avez atteint le nombre limite de recherches enregistrées. Veuillez supprimer au moins une de vos recherches.");
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
	protected Class<SearchCriteriaHistory> getEntityClass() {
	      return SearchCriteriaHistory.class;
	}

    @Override
    protected String getObjectName() {
        return "searchCriteriaHistory";
    }


    @Override
    protected Converter<SearchCriteriaHistoryDto, SearchCriteriaHistory> getConverter() {
        return searchCriteriaHistoryConverter;
    }
}
