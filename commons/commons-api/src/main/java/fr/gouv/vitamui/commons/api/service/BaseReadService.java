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
package fr.gouv.vitamui.commons.api.service;

import java.util.List;
import java.util.Optional;

import fr.gouv.vitamui.commons.api.domain.BaseIdDocument;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;

/**
 * Service allowing to read entities.
 *
 *
 * @param <D> Type of output object (DTO).
 * @param <E> Type of the entity (Entity).
 */
public interface BaseReadService<D extends IdDto, E extends BaseIdDocument> {

    /**
     * Method allowing to retrieve all entities matching with the given criteria.
     * @param criteria Criteria (Json) used for the research.
     * @return The entities matching with the given criteria.
     */
    List<D> getAll(Optional<String> criteria);

    /**
     * Method allowing to retrieve entities according to the given ids.
     * @param ids List of identifiers.
     * @return The entities linked to the given identifiers.
     */
    List<D> getMany(final List<String> ids);

    /**
     * Method allowing to retrieve entities according to the given ids.
     * @param ids List of identifiers.
     * @return The entities linked to the given identifiers.
     */
    List<D> getMany(final String... ids);

    /**
     * Method allowing to retrieve an entity according to an identifier and criteria.
     * @param id Identifier of the entity.
     * @param criteria Additional criteria allowing to precise the search.
     * @return The entity linked to the criteria.
     */
    public D getOne(String id, Optional<String> criteria);

    /**
     * Method allowing to retrieve an entity according to an identifier.
     * @param id Identifier of the entity.
     * @return The entity linked to the criteria.
     */
    public D getOne(String id);

    /**
     * Method allowing to get paginated entities according to the given criteria.
     * Beware : the param criteriaJsonString must include the security filters.
     * @param page Number of the page.
     * @param size Size of the page.
     * @param orderBy Criterion on the sort of results.
     * @param direction Direction of the sort.
     * @return The paginated result.
     */
    PaginatedValuesDto<D> getAllPaginated(final Integer page, final Integer size, final Optional<String> criteria,
            final Optional<String> orderBy, final Optional<DirectionDto> direction);
}
