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
package fr.gouv.vitamui.commons.mongo.repository;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mongodb.client.result.UpdateResult;

import fr.gouv.vitamui.commons.api.domain.AggregationRequestOperator;
import fr.gouv.vitamui.commons.api.domain.BaseIdDocument;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;

/**
 * VITAMUI Mongo CRUD Repository A service to read, create, update and delete an object with identifier.
 *
 *
 * @param <T> The object class to be returned from repository methods, for example find all etc.
 * @param <ID> The Identifier of the object.
 */
@NoRepositoryBean
public interface VitamUIRepository<T extends BaseIdDocument, I extends Serializable> extends PagingAndSortingRepository<T, I>, QueryByExampleExecutor<T> {

    /**
     * Generate a super ID.
     * @return
     */
    String generateSuperId();

    /**
     * Build Paginated Values.
     * @param <S>
     * @param page
     * @param size
     * @param orderBy
     * @param direction
     * @return
     */
    PaginatedValuesDto<T> getPaginatedValues(final Integer page, final Integer size, final Optional<Query> query, final Optional<String> orderBy,
            final Optional<DirectionDto> direction);

    /**
     * Retrieve paginated values for nested arrays.
     * @param collectionName
     * @param fieldName
     * @param criteriaList
     * @param page
     * @param size
     * @param orderBy
     * @param direction
     * @return
     */
    <E> PaginatedValuesDto<E> getPaginatedNestedValues(Class<E> type, final String collectionName, final String fieldName,
            final Collection<CriteriaDefinition> criteriaList, final Integer page, final Integer size, final Optional<String> orderBy,
            final Optional<DirectionDto> direction) throws JsonParseException, JsonMappingException, IOException;

    List<T> findAll(Query query);

    List<T> findAll(CriteriaDefinition... criteria);

    List<T> findAll(Collection<CriteriaDefinition> criteria);

    /**
     * Method to retrieve elements by criteria, orderBy (fields properties) and direction (ASC,DESC).
     * @param criteria
     * @param orderBy
     * @param direction
     * @param enableCollation If true, case and accents are ignored.
     * @return
     */
    List<T> findAll(Iterable<CriteriaDefinition> criteria, Optional<String> orderBy, Optional<DirectionDto> direction, boolean enableCollation);

    boolean exists(CriteriaDefinition... criteria);

    boolean exists(List<CriteriaDefinition> criteria);

    UpdateResult updateMulti(Query query, Update update);

    UpdateResult updateMulti(Query query, Update update, Class<T> clazz);

    long count(CriteriaDefinition... criteria);

    long count(List<CriteriaDefinition> criteria);

    boolean exists(Query query);

    Optional<T> findOne(Query query);

    <U> AggregationResults<U> aggregate(TypedAggregation<?> aggregation, Class<U> outputType);

    UpdateResult updateFirst(Query query, Update update);

    UpdateResult updateFirst(Query query, Update update, Class<T> clazz);

    UpdateResult upsert(Query query, Update update);

    /**
     *
     * Applies an aggregation operation (provided in operationType) on a list of fields.
     *
     * @param fields Array of field names.
     * @param criteria List of criteria.
     * @param operationType type of the aggregation operation to apply.
     * @param orderBy
     * @param direction
     * @return Map<String, Object> aggregation results.
     */
    Map<String, Object> aggregation(Iterable<String> fields,
                                    final Iterable<CriteriaDefinition> criteria,
                                    AggregationRequestOperator operationType,
                                    Optional<String> orderBy,
                                    Optional<DirectionDto> direction);
}
