
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

package fr.gouv.vitamui.ui.commons.service;

import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.BasePaginatingAndSortingWebClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;

import java.util.Optional;

public abstract class AbstractPaginateWebService<DtoType extends IdDto> extends AbstractCrudWebService<DtoType> {

    /**
     * get all with pagination and ordering
     * @param page page number (index * page size)
     * @param size size of data
     * @param criteria filtering criteria
     * @param orderBy order by field
     * @param direction order direction
     * @param context http context
     * @return paginated Values
     */
    public PaginatedValuesDto<DtoType> getAllPaginated(final Integer page, Integer size, final Optional<String> criteria, final Optional<String> orderBy,
                                                       final Optional<DirectionDto> direction, final ExternalHttpContext context) {
        size = beforePaginate(page, size);
        return getClient().getAllPaginated(context, page, size, criteria, orderBy, direction);
    }

    /**
     * get all with pagination, ordering and embeded param
     * @param page page number (index * page size)
     * @param size size of data
     * @param criteria filtering criteria
     * @param orderBy order by field
     * @param direction order direction
     * @param embedded embedded
     * @param context http context
     * @return paginated Values
     */
    public PaginatedValuesDto<DtoType> getAllPaginated(final Integer page, Integer size, final Optional<String> criteria, final Optional<String> orderBy,
                                                       final Optional<DirectionDto> direction, final Optional<String> embedded, final ExternalHttpContext context) {
        size = beforePaginate(page, size);
        return getClient().getAllPaginated(context, page, size, criteria, orderBy, direction, embedded);
    }

    /**
     * beforePaginate, controle pagination limits
     * @param page page number (index * page size)
     * @param size size of data
     * @return The number of results per page
     */
    protected abstract Integer beforePaginate(Integer page, Integer size);

    @Override
    public abstract <ContextType extends ExternalHttpContext> BasePaginatingAndSortingWebClient<ContextType, DtoType> getClient();
}
