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
package fr.gouv.vitamui.commons.mongo.repository.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mongodb.client.result.UpdateResult;

import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.mongo.IdDocument;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.mongo.domain.DocumentWithItems;
import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;
import fr.gouv.vitamui.commons.mongo.repository.VitamUISequenceRepository;
import fr.gouv.vitamui.commons.utils.JsonUtils;

/**
 * {@inheritDoc}
 */
public class VitamUIRepositoryImpl<T extends IdDocument, ID extends Serializable> extends SimpleMongoRepository<T, ID>
        implements VitamUIRepository<T, ID>, VitamUISequenceRepository<T, ID> {

    private final MongoOperations mongoOperations;

    private final MongoEntityInformation<T, ID> entityInformation;

    /**
     * Build Class with metadata and mongoOperations.
     * @param metadata
     * @param mongoOperations
     */
    public VitamUIRepositoryImpl(final MongoEntityInformation<T, ID> metadata, final MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateSuperId() {
        final String part1 = UUID.randomUUID().toString().replaceAll("-", "");
        final String part2 = UUID.randomUUID().toString().replaceAll("-", "");
        final String suffix = (part1 + part2).substring(0, 40);
        return new ObjectId() + suffix;
    }

    /**
     * {@inheritDoc}
     * @param <S>
     */
    @Override
    public PaginatedValuesDto<T> getPaginatedValues(final Integer page, final Integer size, final Optional<Query> query, final Optional<String> orderBy,
            final Optional<DirectionDto> direction) {
        Pageable pageable;

        final Query finalQuery = query.orElse(new Query());

        // Build Pageable
        final Sort sort = extractSort(orderBy, direction);
        if (sort != null) {
            pageable = PageRequest.of(page, size, sort);

            // Enables case insensitive and accent insensitive search and sorting
            finalQuery.collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.secondary()).numericOrderingEnabled());
        }
        else {
            pageable = PageRequest.of(page, size);
        }
        final Page<T> paginate = this.findAll(finalQuery, pageable);
        return new PaginatedValuesDto<>(paginate.getContent(), page, size, paginate.hasNext());
    }

    /**
     * {@inheritDoc}
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Override
    @SuppressWarnings({ "rawtypes" })
    public <E> PaginatedValuesDto<E> getPaginatedNestedValues(final Class<E> type, final String collectionName, final String fieldName,
            final Collection<CriteriaDefinition> criteriaList, final Integer page, final Integer size, final Optional<String> orderBy,
            final Optional<DirectionDto> direction) throws JsonParseException, JsonMappingException, IOException {
        // collectionName, fieldName are mandatory in order to get the paginated nested values
        Assert.isTrue(StringUtils.isNotEmpty(collectionName), "CollectionName should be provided.");
        Assert.isTrue(StringUtils.isNotEmpty(fieldName), "FiledName should be provided.");
        Assert.isTrue(CollectionUtils.isNotEmpty(criteriaList), "Criteria to narrow documents should be provided.");

        // build projection fields, we retrieve document id and nested objects
        // the fieldName must be provided in order to do the slice operation
        final List<String> projectionFields = new ArrayList<>();
        projectionFields.add("_id");
        projectionFields.add(fieldName);
        if (orderBy.isPresent()) {
            final String[] list = orderBy.get().split(",");
            projectionFields.addAll(Arrays.asList(list));
        }
        final List<AggregationOperation> operations = new ArrayList<>();

        // we add the sort operation if needed
        final Sort sort = extractSort(orderBy, direction);
        if (sort != null) {
            operations.add(sort(sort));
        }

        // we add the filtering criteria
        for (final CriteriaDefinition criteria : criteriaList) {
            operations.add(match(criteria));
        }

        // we need to retrieve the nested objects with the list total size
        // @formatter:off
        operations.add(
                project(projectionFields.stream().toArray(String[]::new))
                    .and(fieldName).size().as("itemsSize")
                    .and(fieldName).slice(size, page * size).as("items")
        );
        // @formatter:on
        final AggregationResults<DocumentWithItems> result = mongoOperations.aggregate(Aggregation.newAggregation(operations), collectionName,
                DocumentWithItems.class);

        // we build the paginated items, we use Jackson to retrieve the right object type for the generic.
        // or else each value will be returned as LinkedHashMap.
        final boolean hasNext = result.getUniqueMappedResult().getItemsSize() > ((page * size) + size);
        return new PaginatedValuesDto<>(JsonUtils.convertValueList(result.getUniqueMappedResult().getItems(), type), page, size, hasNext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findAll(final Iterable<CriteriaDefinition> criteria, final Optional<String> orderBy, final Optional<DirectionDto> direction,
            final boolean enableCollation) {

        final Query query = new Query();
        for (final CriteriaDefinition c : criteria) {
            query.addCriteria(c);
        }

        final Sort sort = extractSort(orderBy, direction);
        if (sort != null) {
            query.with(sort);
        }

        if (enableCollation) {
            // Enables case insensitive and accent insensitive search and sorting
            query.collation(Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.secondary()));
        }

        return this.findAll(query);
    }

    private Sort extractSort(final Optional<String> orderBy, final Optional<DirectionDto> direction) {
        if (orderBy.isPresent() && direction.isPresent()) {
            return Sort.by(Direction.valueOf(direction.get().name()), orderBy.get());
        }

        if (orderBy.isPresent()) {
            return Sort.by(orderBy.get());
        }

        if (direction.isPresent()) {
            throw new IllegalArgumentException("orderby parameter is mandatory when using direction");
        }

        return null;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Optional<CustomSequence> incrementSequence(final String nameSquence, final Number incrementValue) {
        return updateFirst("name", nameSquence, new Update().inc("sequence", incrementValue));
    }

    /**
     * Update first element matching element using update function.
     * @param nameSequence
     * @param keyValue
     * @param entityClass
     * @param update
     * @return
     */
    private Optional<CustomSequence> updateFirst(final String nameSequence, final Object keyValue, final Update update) {
        final Query query = new Query(Criteria.where(nameSequence).is(keyValue));
        final UpdateResult result = mongoOperations.updateFirst(query, update, CustomSequence.class);
        return retrieveElementIfExists(query, result);
    }

    /**
     * Retrieve element if execution was successful. If not return null.
     * @param entityClass must not be {@literal null}.
     * @param query must not be {@literal null}.
     * @param writeResult must not be {@literal null}.
     * @return
     */
    private Optional<CustomSequence> retrieveElementIfExists(final Query query, final UpdateResult writeResult) {
        Optional<CustomSequence> result = null;
        if (writeResult.getMatchedCount() == 1) {
            result = Optional.ofNullable(mongoOperations.findOne(query, CustomSequence.class));
        }
        else {
            result = Optional.empty();
        }
        return result;
    }

    /**
     * Create query with pageable
     * @param query must not be null
     * @param pageable must not be null
     * @return
     */
    public Page<T> findAll(final Query query, final Pageable pageable) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(pageable, "Pageable must not be null!");

        final Long count = mongoOperations.count(query, entityInformation.getCollectionName());
        final List<T> list = findAll(query.with(pageable));

        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public List<T> findAll(final Collection<CriteriaDefinition> criteria) {
        return this.findAll(criteria.toArray(new CriteriaDefinition[criteria.size()]));
    }

    @Override
    public List<T> findAll(final CriteriaDefinition... criteria) {
        final Query query = new Query();
        for (final CriteriaDefinition c : criteria) {
            if (c != null) {
                query.addCriteria(c);
            }
        }
        return mongoOperations.find(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    @Override
    public List<T> findAll(@Nullable final Query query) {
        if (query == null) {
            return Collections.emptyList();
        }
        return mongoOperations.find(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    @Override
    public Optional<T> findOne(final Query query) {
        return Optional.ofNullable(mongoOperations.findOne(query, entityInformation.getJavaType(), entityInformation.getCollectionName()));
    }

    @Override
    public boolean exists(final List<CriteriaDefinition> criteria) {
        return this.exists(criteria.toArray(new CriteriaDefinition[criteria.size()]));
    }

    @Override
    public boolean exists(final CriteriaDefinition... criteria) {
        final Query query = new Query();
        for (final CriteriaDefinition c : criteria) {
            query.addCriteria(c);
        }
        return this.exists(query);
    }

    @Override
    public boolean exists(final Query query) {
        return mongoOperations.exists(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    @Override
    public long count(final List<CriteriaDefinition> criteria) {
        return this.count(criteria.toArray(new CriteriaDefinition[criteria.size()]));
    }

    @Override
    public long count(final CriteriaDefinition... criteria) {
        final Query query = new Query();
        for (final CriteriaDefinition c : criteria) {
            query.addCriteria(c);
        }
        return mongoOperations.count(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    @Override
    public UpdateResult updateMulti(final Query query, final Update update) {
        return mongoOperations.updateMulti(query, update, entityInformation.getJavaType());
    }

    @Override
    public UpdateResult updateMulti(final Query query, final Update update, final Class<T> clazz) {
        return mongoOperations.updateMulti(query, update, clazz);
    }

    @Override
    public <U> AggregationResults<U> aggregate(final TypedAggregation<?> aggregation, final Class<U> outputType) {
        return mongoOperations.aggregate(aggregation, outputType);
    }

    @Override
    public UpdateResult updateFirst(final Query query, final Update update) {
        return mongoOperations.updateFirst(query, update, entityInformation.getJavaType());
    }

    @Override
    public UpdateResult updateFirst(final Query query, final Update update, final Class<T> clazz) {
        return mongoOperations.updateFirst(query, update, clazz);
    }

    @Override
    public UpdateResult upsert(final Query query, final Update update) {
        return mongoOperations.upsert(query, update, entityInformation.getJavaType());
    }
}
