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
package fr.gouv.vitamui.commons.mongo.utils;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import fr.gouv.vitamui.commons.api.domain.BaseIdDocument;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.utils.ReflectionUtils;

public final class MongoUtils {

    private static final String BETWEEN_OPEARTOR_KEY_END = "end";
    private static final String BETWEEN_OPERATOR_KEY_START = "start";

    private MongoUtils() {
    }

    public static void addCriteria(final Optional<?> value, final String key, final List<CriteriaDefinition> criteria) {
        value.ifPresent(v -> criteria.add(Criteria.where(key).is(v)));
    }

    public static void addCriteria(final Optional<?> value, final String key, final List<CriteriaDefinition> criteria,
            final CriterionOperator operator) {
        value.ifPresent(v -> criteria.add(getCriteria(key, v, operator)));
    }

    public static void addCriteriaIn(final List<?> values, final String key, final List<CriteriaDefinition> criteria) {
        if (!values.isEmpty()) {
            criteria.add(Criteria.where(key).in(values));
        }
    }

    public static void addCriteriaGreaterThan(final Optional<?> value, final String key,
            final List<CriteriaDefinition> criteria) {
        if (value.isPresent()) {
            criteria.add(Criteria.where(key).gt(value.get()));
        }
    }

    public static void addCriteriaStartWith(final Optional<String> value, final String key,
            final List<CriteriaDefinition> criteria) {
        if (value.isPresent()) {
            final String valueToSearch = "^" + Pattern.quote(value.get()) + ".*$";
            criteria.add(Criteria.where(key).regex(Pattern.compile(valueToSearch)));
        }
    }

    public static void addCriteriaIgnoreCase(final String key, final Optional<String> value,
            final List<CriteriaDefinition> criteria) {
        if (value.isPresent()) {
            final String valueToSearch = "^" + Pattern.quote(value.get()) + "$";
            criteria.add(Criteria.where(key).regex(Pattern.compile(valueToSearch, Pattern.CASE_INSENSITIVE)));
        }
    }

    public static void addCriteriaContainsIgnoreCase(final String key, final Optional<String> value,
            final List<CriteriaDefinition> criteria) {
        if (value.isPresent() && StringUtils.isNotEmpty(value.get())) {
            criteria.add(buildCriteriaContainsIgnoreCase(key, value.get()));
        }
    }

    public static Criteria buildCriteriaStartWith(final String key, final String value, final boolean ignoreCase) {
        final String valueToSearch = "^" + Pattern.quote(value) + ".*$";
        final StringBuilder options = new StringBuilder();
        if (ignoreCase) {
            options.append("i");
        }
        return Criteria.where(key).regex(valueToSearch, options.toString());
    }

    public static CriteriaDefinition buildCriteriaContainsIgnoreCase(final String key, final String value) {
        return buildCriteriaContains(key, value, true);
    }

    public static Criteria buildCriteriaEquals(final String key, final String value, final boolean ignoreCase) {
        final String valueToSearch = "^" + Pattern.quote(value) + "$";
        final StringBuilder options = new StringBuilder();
        if (ignoreCase) {
            options.append("i");
        }
        return Criteria.where(key).regex(valueToSearch, options.toString());
    }

    public static Criteria buildCriteriaContains(final String key, final String value, final boolean ignoreCase) {
        final String valueToSearch = "^.*" + Pattern.quote(value) + ".*$";
        final StringBuilder options = new StringBuilder();
        if (ignoreCase) {
            options.append("i");
        }
        return Criteria.where(key).regex(valueToSearch, options.toString());
    }

    public static Criteria buildOrOperator(final Criteria... criteria) {
        return new Criteria().orOperator(criteria);
    }

    public static Criteria buildAndOperator(final Criteria... criteria) {
        return new Criteria().andOperator(criteria);
    }

    public static <E extends BaseIdDocument> Criteria getCriteriaDefinitionFromEntityClass(final Criterion criterion,
            final Class<E> entityClass) {
        final Type type = getTypeOfField(entityClass, criterion.getKey());
        return getCriteriaDefinitionFromFieldType(criterion, type);
    }

    public static Criteria getCriteriaDefinitionFromFieldType(final Criterion criterion, final Type type) {
        if (type == null || type.getClass() == null) {
            return null;
        }
        final Object value = getValueFromOperatorAndFieldType(criterion.getValue(), criterion.getOperator(), type);
        return getCriteria(criterion.getKey(), value, criterion.getOperator());
    }

    private static Object getValueFromOperatorAndFieldType(final Object value, final CriterionOperator operator,
            final Type fieldType) {
        final Class<?> parametrizedClazz = ReflectionUtils.getParametrizedClass(fieldType);
        switch (operator) {
            case EQUALS :
            case NOTEQUALS :
                if (ReflectionUtils.isParametrizedList(fieldType)) {
                    final Collection<Object> listValue = CastUtils.toList(value);
                    return listValue.stream().map(val -> convertValue(parametrizedClazz, val))
                            .collect(Collectors.toList());
                }
                return convertValue(parametrizedClazz, value);
            case IN :
            case NOTIN :
                final Collection<Object> listVal = CastUtils.toList(value);
                return listVal.stream().map(val -> convertValue(parametrizedClazz, val)).collect(Collectors.toList());
            case BETWEEN :
                final Map<String, Object> mapValue = CastUtils.toMap(value);
                mapValue.put(BETWEEN_OPERATOR_KEY_START, convertValue(parametrizedClazz, mapValue.get(BETWEEN_OPERATOR_KEY_START)));
                mapValue.put(BETWEEN_OPEARTOR_KEY_END, convertValue(parametrizedClazz, mapValue.get(BETWEEN_OPEARTOR_KEY_END)));
                return mapValue;
            case ELEMMATCH:
                return convertValue(QueryDto.class, value);
            default :
                return convertValue(parametrizedClazz, value);
        }
    }

    protected static Object convertValue(final Class<?> clazz, final Object value) {
        if (clazz.isEnum()) {
            return value;
        } else if (clazz.equals(OffsetDateTime.class)) {
            return OffsetDateTime.parse(value.toString());
        } else if (clazz.equals(Duration.class)) {
            return Duration.parse(value.toString());
        } else if (clazz.equals(int.class)) {
            return Integer.parseInt(value.toString());
        } else if (clazz.equals(long.class)) {
            return Long.parseLong(value.toString());
        } else if (clazz.equals(float.class)) {
            return Float.parseFloat(value.toString());
        } else if (clazz.equals(double.class)) {
            return Double.parseDouble(value.toString());
        } else if (clazz.equals(boolean.class)) {
            return Boolean.parseBoolean(value.toString());
        } else if (VitamUIUtils.canBeCastByClass(value, List.class)) {
            return value;
        } else if (clazz.equals(QueryDto.class)) {
            try {
                // we convert the linked hash map to json to re-convert it properly to query dto
                return QueryDto.fromJson(JsonUtils.toJson(value));
            }
            catch (Exception e) {
                throw new InvalidFormatException(e.getMessage(), e);
            }
        }

        return CastUtils.castValue(value, clazz);
    }

    public static Criteria getCriteria(final Criterion c) {
        return getCriteria(c.getKey(), c.getValue(), c.getOperator());
    }

    public static Criteria getCriteria(final String key, final Object val, final CriterionOperator operator) {
        if (operator == null) {
            throw new InvalidFormatException("No operator defined");
        }
        Criteria criteria;
        switch (operator) {
            case EQUALS :
                if (val instanceof String
                        && (StringUtils.contains((String) val, "*") || StringUtils.contains((String) val, ".*"))) {
                    final String valueToSearch = "^" + val.toString().replaceAll("(?<!\\.)\\*", ".*") + "$";
                    final StringBuilder options = new StringBuilder();
                    criteria = Criteria.where(key).regex(valueToSearch, options.toString());
                } else {
                    criteria = Criteria.where(key).is(val);
                }
                break;
            case NOTEQUALS :
                criteria = Criteria.where(key).ne(val);
                break;
            case EQUALSIGNORECASE :
                final StringBuilder options = new StringBuilder("i");
                final String valueToSearch = "^" + val.toString().replaceAll("(?<!\\.)\\*", ".*") + "$";
                criteria = Criteria.where(key).regex(valueToSearch, options.toString());
                break;
            case GREATER :
                criteria = Criteria.where(key).gt(val);
                break;
            case LOWER :
                criteria = Criteria.where(key).lt(val);
                break;
            case GREATERTHANOREQUALS :
                criteria = Criteria.where(key).gte(val);
                break;
            case LOWERTHANOREQUALS :
                criteria = Criteria.where(key).lte(val);
                break;
            case CONTAINSIGNORECASE :
                criteria = MongoUtils.buildCriteriaContains(key, val.toString(), true);
                break;
            case CONTAINS :
                criteria = MongoUtils.buildCriteriaContains(key, val.toString(), false);
                break;
            case STARTWITH :
                criteria = MongoUtils.buildCriteriaStartWith(key, val.toString(), false);
                break;
            case IN :
                criteria = Criteria.where(key).in((Collection) val);
                break;
            case NOTIN :
                criteria = Criteria.where(key).not().in((Collection) val);
                break;
            case BETWEEN :
                final Map<String, Object> mapVal = CastUtils.toMap(val);
                final Criteria startCriteria = Criteria.where(key).gte(mapVal.get(BETWEEN_OPERATOR_KEY_START));
                final Criteria endCriteria = Criteria.where(key).lte(mapVal.get(BETWEEN_OPEARTOR_KEY_END));
                criteria = buildAndOperator(startCriteria, endCriteria);
                break;
            case ELEMMATCH :
                criteria = Criteria.where(key).elemMatch(queryDtoToCriteria((QueryDto)val));
                break;
            default :
                throw new IllegalArgumentException("Operator " + operator + " is not supported");
        }
        return criteria;
    }

    /**
     * convert QueryDto to mongodb criteria
     * @param queryDto the QueryDto to convert
     * @return mongodb criteria
     */
    public static Criteria queryDtoToCriteria(QueryDto queryDto) {
        Collection<CriteriaDefinition> criteria = new ArrayList<>();
        queryDto.getCriterionList().forEach(criterion -> {
            criteria.add(MongoUtils.getCriteria(criterion.getKey(), criterion.getValue(), criterion.getOperator()));
        });

        // if the criteria contains subQueries, a recursive call is made for each subQuery
        queryDto.getSubQueries().forEach(queryDtoItem -> {
            criteria.add(queryDtoToCriteria(queryDtoItem));
        });

        final Criteria commonCustomCriteria = new Criteria();
        Criteria[] criteriaList = criteria.stream().map(c -> (Criteria) c).toArray(Criteria[]::new);
        switch (queryDto.getQueryOperator()){
            case AND:
                commonCustomCriteria.andOperator(criteriaList);
                break;
            case OR:
                commonCustomCriteria.orOperator(criteriaList);
                break;
            case NOR:
                commonCustomCriteria.norOperator(criteriaList);
                break;
            default:
                throw new NotImplementedException("Method is not implemented => " + queryDto.getQueryOperator().name());
        }

        return commonCustomCriteria;
    }


    public static Type getTypeOfField(final Class<?> entityClass, final String fieldName) {
        try {
            return ReflectionUtils.getTypeOfField(entityClass, fieldName);
        }
        catch (final NoSuchFieldException e) {
            throw new IllegalArgumentException("no fields " + fieldName + " found", e);
        }

    }

}
