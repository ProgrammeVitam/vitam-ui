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
package fr.gouv.vitamui.commons.mongo.service;

import com.google.common.collect.Iterators;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.*;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.service.BaseReadService;
import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;
import fr.gouv.vitamui.commons.mongo.utils.MongoUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

/**
 * A service to read, create, update and delete an object with identifier.
 *
 *
 */
public abstract class VitamUIReadService<D extends IdDto, E extends BaseIdDocument> implements BaseReadService<D, E> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamUIReadService.class);

    /**
     * Method allowing to retrieve all entities matching with the provided criteria.
     * @param criteria Criteria used for the research.
     * @return The entities matching with the provided criteria.
     */
    public List<D> getAll(final QueryDto criteria) {
        return getAll(getQuerySecured(criteria.toOptionalJson()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<D> getAll(final Optional<String> criteria) {
        return getAll(getQuerySecured(criteria));
    }

    /**
     * Method allowing to retrieve all entities matching with the provided criteria.
     * @param criteria Criteria (Json) used for the research.
     * @param embedded Extra element to load.
     * @return The entities matching with the provided criteria.
     */
    protected List<D> getAll(final Optional<String> criteria, final Optional<String> embedded) {
        return getAll(getQuerySecured(criteria), embedded);
    }

    /**
     * Method allowing to convert Iterable<E> to List<D>.
     * @param it Iterable of entities <E>.
     * @return The list of dto object <D>.
     */
    protected List<D> convertIterableToList(final Iterable<E> it) {
        return StreamSupport.stream(it.spliterator(), false).map(this::convertFromEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<D> getMany(final List<String> ids) {
        final String[] arrayIds = (ids == null) ? new String[0] : ids.toArray(new String[0]);
        final List<D> dtos = getMany(arrayIds);
        return dtos;
    }

    /**
     * Method allowing to retrieve entities according to the provided ids.
     * @param ids List of identifiers.
     * @param embedded Extra element to load.
     * @return The entities matching with the provided criteria.
     */
    protected List<D> getMany(final List<String> ids, final Optional<String> embedded) {
        final String[] arrayIds = (ids == null) ? new String[0] : ids.toArray(new String[0]);
        final List<D> dtos = getMany(arrayIds);
        loadExtraInformations(dtos, embedded);
        return dtos;
    }

    /**
     * Method allowing to load extra information into the list of dtos.
     * @param dtos Dtos to update.
     * @param embedded Extra element to load.
     */
    protected void loadExtraInformations(final List<D> dtos, final Optional<String> embedded) {
        for (final D dto : dtos) {
            loadExtraInformation(dto, embedded);
        }
    }

    /**
     * Method allowing to load extra information into a dto.
     * @param dto Dto to update.
     * @param embedded Extra element to load.
     */
    protected void loadExtraInformation(final D dto, final Optional<String> embedded) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<D> getMany(final String... ids) {
        LOGGER.debug("Get {} {}", getObjectName(), ids);
        final Iterable<String> iterable = () -> Iterators.forArray(ids);
        final Iterable<E> entities = getRepository().findAllById(iterable);
        return convertIterableToList(entities);
    }

    /**
     * Method for by pass security control. <br>
     * Use this method only for specific situation, like authentication etc...
     *
     * @param id Identifier of the entity.
     * @param embedded Extra element to load.
     * @return The provided entity.
     */
    public D getOneByPassSecurity(final String id, final Optional<String> embedded) {
        LOGGER.debug("Get {} ", getObjectName(), id);
        final Query query = new Query();
        return getOne(id, query, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public D getOne(final String id, final Optional<String> criteria) {
        return getOne(id, criteria, Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public D getOne(final String id) {
        return getOne(id, Optional.empty());
    }

    /**
     * Method allowing to retrieve an entity according to an identifier and criteria.
     * @param id Identifier of the entity.
     * @param criteria Additional criteria allowing to precise the search.
     * @param embedded Extra information to load.
     * @return The entity linked to the criteria.
     */
    protected D getOne(final String id, final Optional<String> criteria, final Optional<String> embedded) {
        LOGGER.debug("Get {} {} {}", getObjectName(), id, embedded);
        final Query query = getQuerySecured(criteria);
        return getOne(id, query, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaginatedValuesDto<D> getAllPaginated(final Integer page, final Integer size,
            final Optional<String> criteriaJsonString, final Optional<String> orderBy,
            final Optional<DirectionDto> direction) {

        final Query query = getQuerySecured(criteriaJsonString);
        final PaginatedValuesDto<E> entititesValues = getRepository().getPaginatedValues(page, size, Optional.of(query),
                orderBy, direction);

        final List<D> valuesDto = entititesValues.getValues().stream().map(this::convertFromEntityToDto)
                .collect(Collectors.toList());
        return new PaginatedValuesDto<>(valuesDto, entititesValues.getPageNum(), entititesValues.getPageSize(),
                entititesValues.isHasMore());
    }

    /**
     * Method allowing to get paginated entities according to the provided criteria.
     * Beware : the param criteriaJsonString must include the security filters.
     * @param page Number of the page.
     * @param size Size of the page.
     * @param orderBy Criterion on the sort of results.
     * @param direction Direction of the sort.
     * @param embedded Extra information to load.
     * @return The paginated result.
     */
    protected PaginatedValuesDto<D> getAllPaginated(final Integer page, final Integer size,
            final Optional<String> criteriaJsonString, final Optional<String> orderBy,
            final Optional<DirectionDto> direction, final Optional<String> embedded) {

        final Query query = getQuerySecured(criteriaJsonString);
        final PaginatedValuesDto<E> entititesValues = getRepository().getPaginatedValues(page, size, Optional.of(query),
                orderBy, direction);

        final List<D> valuesDto = entititesValues.getValues().stream().map(this::convertFromEntityToDto)
                .collect(Collectors.toList());
        if (embedded.isPresent()) {
            loadExtraInformations(valuesDto, embedded);
        }
        return new PaginatedValuesDto<>(valuesDto, entititesValues.getPageNum(), entititesValues.getPageSize(),
                entititesValues.isHasMore());
    }

    /**
     * Method allowing to get paginated entities according to the provided criteria.
     * Beware : the parameter criteriaJsonString IGNORE the security filters.
     * @param page Number of the page.
     * @param size Size of the page.
     * @param orderBy Criterion on the sort of results.
     * @param direction Direction of the sort.
     * @return The paginated result.
     */
    public PaginatedValuesDto<D> getAllPaginatedByPassSecurity(final Integer page, final Integer size,
            final Optional<String> criteriaJsonString, final Optional<String> orderBy,
            final Optional<DirectionDto> direction) {

        final Query query = getQueryUnsecured(criteriaJsonString);
        final PaginatedValuesDto<E> entititesValues = getRepository().getPaginatedValues(page, size, Optional.of(query),
                orderBy, direction);

        final List<D> valuesDto = entititesValues.getValues().stream().map(this::convertFromEntityToDto)
                .collect(Collectors.toList());
        return new PaginatedValuesDto<>(valuesDto, entititesValues.getPageNum(), entititesValues.getPageSize(),
                entititesValues.isHasMore());
    }

    /**
     * Method allowing to transform criteria to a secured query with data access restrictions.
     * @param criteriaJsonString Criteria (JSON) to convert.
     * @return The generated query.
     */
    protected Query getQuerySecured(final Optional<String> criteriaJsonString) {
        return getQuery(criteriaJsonString, true);
    }

    /**
     * Method allowing to transform criteria to a secured query with data access restrictions.
     * @param criteriaJsonString Criteria (JSON) to convert.
     * @return The generated query.
     */
    protected Optional<Criteria> getCriteriaSecured(final Optional<String> criteriaJsonString) {
        return getCriteria(criteriaJsonString, true);
    }

    /**
     * Method allowing to transform criteria to a query (without data access restrictions).
     * @param criteriaJsonString Criteria (JSON) to convert.
     * @return The generated query.
     */
    private Query getQueryUnsecured(final Optional<String> criteriaJsonString) {
        return getQuery(criteriaJsonString, false);
    }

    protected Document groupFields(Optional<String> criteriaJsonString, String... fields) {
        List<AggregationOperation> operationList = new ArrayList<>();
        Assert.notEmpty(fields, "Fields should not be empty");

        // Add criteria to aggregation operations
        Optional<Criteria> securedCriteria = getCriteriaSecured(criteriaJsonString);
        if (securedCriteria.isPresent()) {
            MatchOperation matchOperation = Aggregation.match(securedCriteria.get());
            operationList.add(matchOperation);
        }

        GroupOperation groupOperation = group();
        for (String field: fields) {
            groupOperation = groupOperation.addToSet(field).as(field);
        }
        operationList.add(groupOperation);

        TypedAggregation<E> agg = newAggregation(getEntityClass(), operationList);

        AggregationResults<Document> results = getRepository().aggregate(agg, Document.class);
        return results.getUniqueMappedResult();
    }

    /**
     * Method allowing to add restrictions on the Mongo's query.
     * @param criteria The provided criteria which must be secured.
     */
    protected void addDataAccessRestrictions(final Collection<CriteriaDefinition> criteria) {

    }

    /**
     * Method to override if specific fields
     * @param queryJsonString
     * @return
     */
    protected Collection<CriteriaDefinition> convertCriteriaJsonToMongoCriteria(
            final Optional<String> queryJsonString) {
        final Collection<CriteriaDefinition> result = new ArrayList<>();
        Criteria rootCriteria = new Criteria();
        if (queryJsonString.isPresent()) {
            final QueryDto queryDto = QueryDto.fromJson(queryJsonString);
            rootCriteria = convertQueryDtoToCriteria(queryDto);
        }
        result.add(rootCriteria);
        return result;
    }

    /**
     * Method {@link convertCriteriaJsonToMongoCriteria} should be used instead.
     * @param criteriaJsonString
     * @return
     */
    @Deprecated
    protected Collection<CriteriaDefinition> convertCriteriaJsonToMongoCriteriaV1(
            final Optional<String> criteriaJsonString) {
        final Collection<CriteriaDefinition> criteria = new ArrayList<>();
        if (criteriaJsonString.isPresent()) {
            final QueryDto criteriaDto = QueryDto.fromJson(criteriaJsonString);
            final Collection<Criterion> criterion = criteriaDto.getCriterionList();
            criterion.forEach(c -> criteria.add(convertCriterionToMongoCriteria(c)));
        }
        return criteria;
    }

    /**
     * Mrthod for convert
     * @param query
     * @return
     */
    protected Criteria convertQueryDtoToCriteria(final QueryDto query) {
        Criteria rootCriteria = new Criteria();
        final Collection<Criteria> criteria = new ArrayList<>();
        final Collection<Criterion> criterion = query.getCriterionList();

        if (!CollectionUtils.isEmpty(criterion)) {
            criterion.forEach(c -> criteria.add((Criteria) convertCriterionToMongoCriteria(c)));
        }

        if (!CollectionUtils.isEmpty(query.getSubQueries())) {
            query.getSubQueries().forEach(sub -> criteria.add(convertQueryDtoToCriteria(sub)));
        }

        if (!CollectionUtils.isEmpty(criteria)) {
            Criteria[] criteriaArray = criteria.toArray(new Criteria[criteria.size()]);
            QueryOperator queryOperator;
            if (query.getQueryOperator() == null) {
                queryOperator = QueryOperator.AND;
            } else {
                queryOperator = query.getQueryOperator();
            }
            switch (queryOperator) {
                case OR :
                    rootCriteria.orOperator(criteriaArray);
                    break;
                case NOR :
                    rootCriteria.norOperator(criteriaArray);
                    break;
                case AND :
                    rootCriteria.andOperator(criteriaArray);
                    break;
                default :
                    throw new UnsupportedOperationException("Unsupported QueryOperator " + query.getQueryOperator());
            }
        }
        return rootCriteria;
    }

    /**
     * Method allowing to transform criteria to a query .
     * @param criteriaJsonString Criteria (JSON) to convert.
     * @param querySecured Flag indicating if the restrictions on data access must be set.
     * @return The generated query.
     */
    protected Query getQuery(final Optional<String> criteriaJsonString, final boolean querySecured) {
        final Query query = new Query();
        final Optional<Criteria> rootCriteria = getCriteria(criteriaJsonString, querySecured);
        if (rootCriteria.isPresent()) {
            query.addCriteria(rootCriteria.get());
        }
        return query;
    }

    protected Optional<Criteria> getCriteria(final Optional<String> criteriaJsonString, final boolean isSecured) {
        final Collection<CriteriaDefinition> criteria = convertCriteriaJsonToMongoCriteria(criteriaJsonString);
        if (isSecured) {
            addDataAccessRestrictions(criteria);
        }
        if (!CollectionUtils.isEmpty(criteria)) {
            Criteria[] criteriaArray = criteria.toArray(new Criteria[criteria.size()]);
            final Criteria rootCriteria = new Criteria();
            rootCriteria.andOperator(criteriaArray);

            return Optional.of(rootCriteria);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Check exists with criteria.
     * @param criteriaJsonString
     * @return
     */
    protected boolean checkExist(final String criteriaJsonString) {
        final Query query = getQuery(Optional.ofNullable(criteriaJsonString), true);
        return getRepository().exists(query);
    }

    /**
     * Method to override if specific fields or criteria
     * @param c
     * @return
     */
    protected CriteriaDefinition convertCriterionToMongoCriteria(final Criterion c) {
        return MongoUtils.getCriteriaDefinitionFromEntityClass(c, getEntityClass());
    }

    /**
     * Convert from Entity to DTO.
     * @param entity
     * @return
     */
    protected final D convertFromEntityToDto(final E entity) {
        return entity != null ? internalConvertFromEntityToDto(entity) : null;
    }

    /**
     * Convert from Entity to DTO.
     *
     * @param entity entity.
     * @return the resulting DTO.
     */
    protected D internalConvertFromEntityToDto(final E entity) {
        return getConverter().convertEntityToDto(entity);
    }

    /**
     * Get Object name.
     *
     * @return the object name.
     */
    protected String getObjectName() {
        return getEntityClass().getName();
    }

    /**
     * Get Converter.
     *
     * @return the converter
     */
    protected Converter<D, E> getConverter() {
        throw new NotImplementedException("");
    }

    /**
     * Get Converter.
     *
     * @return the repository
     */
    protected abstract VitamUIRepository<E, String> getRepository();

    /**
     * The entity class
     *
     * @return the entity class
     */
    protected abstract Class<E> getEntityClass();

    /**
     * GetAll with Mongo criteria.
     * @return
     */
    private List<D> getAll(final Query query) {
        LOGGER.debug("Get all {}s", getObjectName());
        final Iterable<E> entities = (query == null) ? getRepository().findAll() : getRepository().findAll(query);
        final List<D> dtos = convertIterableToList(entities);
        return dtos;
    }

    /**
     * GetAll with Mongo criteria and Embedded.
     * @return
     */
    private List<D> getAll(final Query query, final Optional<String> embedded) {
        LOGGER.debug("Get all {}s", getObjectName());
        final Iterable<E> entities = (query == null) ? getRepository().findAll() : getRepository().findAll(query);
        final List<D> dtos = convertIterableToList(entities);
        loadExtraInformations(dtos, embedded);
        return dtos;
    }

    /**
     *
     * @param id
     * @param query
     * @param embedded
     * @return
     */
    private D getOne(final String id, final Query query, final Optional<String> embedded) {
        query.addCriteria(Criteria.where("id").is(id));
        final E entity = getRepository().findOne(query)
                .orElseThrow(() -> new NotFoundException("Entity not found " + getObjectName() + " with id : " + id));
        final D dto = convertFromEntityToDto(entity);
        loadExtraInformation(dto, embedded);
        return dto;
    }
}
