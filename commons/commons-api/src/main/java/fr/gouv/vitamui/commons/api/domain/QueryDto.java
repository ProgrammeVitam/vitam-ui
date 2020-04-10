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
package fr.gouv.vitamui.commons.api.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.gouv.vitamui.commons.api.deserializer.CriterionAndQueryDtoDeserializer;
import fr.gouv.vitamui.commons.api.utils.CriteriaUtils;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Query DTO.
 *
 * Json format:
 * //QueryDto
 * {
 *     "queryOperator": "AND",
 *     "criteria": [
 *         // Criterion
 *         {
 *             "key": "tenantId",
 *             "value": "10",
 *             "operator": "EQUALS"
 *         },
 *         // Criterion
 *         {
 *             "key": "customer",
 *             "value": "toto",
 *             "operator": "CONTAINSIGNORECASE"
 *         },
 *         // QueryDto
 *         {
 *             "queryOperator": "OR",
 *             "criteria": [
 *                 {
 *                     "key": "firstname",
 *                     "value": "Pierre",
 *                     "operator": "CONTAINSIGNORECASE"
 *                 },
 *                 {
 *                     "key": "lastname",
 *                     "value": "NOLE",
 *                     "operator": "CONTAINSIGNORECASE"
 *                 }
 *             ]
 *         }
 *     ]
 * }
 *
 */
@EqualsAndHashCode
@NoArgsConstructor
@ToString(callSuper = true)
public class QueryDto {

    private QueryOperator queryOperator = QueryOperator.AND;

    private List<Criterion> criterionList = new ArrayList<>();

    private List<QueryDto> subQueries = new ArrayList<>();

    public QueryDto(final QueryOperator queryOperator) {
        this.queryOperator = queryOperator;
    }

    public void addCriterion(final Criterion criterion) {
        criterionList.add(criterion);
    }

    public void addAllCriterion(final Collection<Criterion> criterionCollection) {
        criterionList.addAll(criterionCollection);
    }

    public QueryDto addCriterion(final String key, final Object value, final CriterionOperator operator) {
        criterionList.add(new Criterion(key, value, operator));
        return this;
    }

    public void addQuery(final QueryDto query) {
        subQueries.add(query);
    }

    public String toJson() {
        return CriteriaUtils.toJson(this);
    }

    public Optional<Criterion> find(final String key) {
        return getCriterionList().stream().filter(c -> StringUtils.equals(key, c.getKey())).findFirst();
    }

    public Optional<String> toOptionalJson() {
        return Optional.of(CriteriaUtils.toJson(this));
    }

    public QueryDto keyFilter(final Collection<String> allowedKeys) {
        CriteriaUtils.checkContainsAuthorizedKeys(this, allowedKeys);
        return this;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(getCriterionList()) && (getSubQueries() == null || getSubQueries().stream().allMatch(QueryDto::isEmpty));
    }

    public static QueryDto fromJson(final Optional<String> json) {
        return json.isPresent() ? CriteriaUtils.fromJson(json.get()) : new QueryDto();
    }

    public static QueryDto fromJson(final String json) {
        return CriteriaUtils.fromJson(json);
    }

    public static QueryDto criteria() {
        return new QueryDto();
    }

    public static QueryDto orQuery() {
        return new QueryDto(QueryOperator.OR);
    }

    public static QueryDto andQuery() {
        return new QueryDto(QueryOperator.AND);
    }

    public static QueryDto criteria(final String key, final Object value, final CriterionOperator operator) {
        final QueryDto criteria = new QueryDto();
        criteria.addCriterion(key, value, operator);
        return criteria;
    }

    @JsonIgnore
    public List<Criterion> getCriterionList() {
        return criterionList;
    }

    @JsonIgnore
    public List<QueryDto> getSubQueries() {
        return subQueries;
    }

    /**
     * Set the {@link QueryDto#subQueries} and {@link QueryDto#criterionList} lists.
     * This method is only used for json deserialization.
     *
     * @param criteria List of objects which can be either a {@link QueryDto} or a {@link Criterion}.
     */
    @JsonDeserialize(contentUsing = CriterionAndQueryDtoDeserializer.class)
    private void setCriteria(final List<Object> criteria) {
        // Criterion and CriteriaDto items are in the same list called "criteria" in json format,
        // but are separated into two different list in the POJO
        criterionList = criteria.stream().filter(c -> c instanceof Criterion).map(Criterion.class::cast).collect(Collectors.toList());
        subQueries = criteria.stream().filter(c -> c instanceof QueryDto).map(QueryDto.class::cast).collect(Collectors.toList());
    }

    /**
     * Returns a collection of objects which contains the elements of subQueries and criterionList.
     * This method is only used for json serialization.
     *
     * @return List of objects which can be either a {@link QueryDto} or a {@link Criterion}.
     */
    @JsonProperty
    private Collection<Object> getCriteria() {
        // criterionList and subQueries elements will be in the same list, called "criteria", when serialized to json.
        final List<Object> objectList = new ArrayList<>();
        objectList.addAll(criterionList);
        objectList.addAll(subQueries);
        return objectList;
    }

    public QueryOperator getQueryOperator() {
        return queryOperator;
    }

    public void setQueryOperator(final QueryOperator queryOperator) {
        this.queryOperator = queryOperator;
    }

}
