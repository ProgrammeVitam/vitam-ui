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
package fr.gouv.vitamui.commons.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class CriteriaUtils {

    private CriteriaUtils() {
    }

    public static void checkFormat(final String criteriaJson) {
        final QueryDto criteriaDto = fromJson(criteriaJson);
        checkFormat(criteriaDto);
    }

    public static void checkFormat(final QueryDto criteriaDto) {
        checkCriterionList(criteriaDto.getCriterionList());
        if (criteriaDto.getSubQueries() != null) {
            criteriaDto.getSubQueries().forEach(CriteriaUtils::checkFormat);
        }
    }

    private static void checkCriterionList(final Collection<Criterion> criteria) {
        if (!CollectionUtils.isEmpty(criteria)) {
            final Set<String> keys = criteria.stream().map(Criterion::getKey).collect(Collectors.toSet());
            if (criteria.size() != keys.size()) {
                throw new BadRequestException("Only an unique key by criteria is allowed");
            }
            criteria.forEach(CriteriaUtils::checkCriterion);
        }
    }


    private static void checkCriterion(final Criterion criterion) {
        final CriterionOperator operator = criterion.getOperator();
        if (operator == null) {
            throw new BadRequestException("Operator not defined for criterion : " + criterion.getKey());
        }
        if (criterion.getKey() == null) {
            throw new BadRequestException("Key not defined for criterion : " + criterion.toString());
        }
        if (criterion.getValue() == null && !CriterionOperator.EQUALS.equals(criterion.getOperator())) {
            throw new BadRequestException("Value not defined for criterion : " + criterion.getKey());
        }
        switch (operator) {
            case BETWEEN :
                try {
                    final Map<String, Object> c = (Map<String, Object>) criterion.getValue();
                    if (!(c.containsKey("start") && c.containsKey("end"))) {
                        throw new BadRequestException("Can't determine start or end value for operator BETWEEN for criterion : " + criterion.getKey());
                    }
                } catch (final ClassCastException e) {
                    throw new BadRequestException("Value is not defined as a map with operator BETWEEN for criterion : " + criterion.getKey(), e);
                }
                break;
            case IN :
                try {
                    final List<Object> c = (List<Object>) criterion.getValue();
                } catch (final ClassCastException e) {
                    throw new BadRequestException("Value is not defined as an array with operator IN for criterion : " + criterion.getKey(), e);
                }
                break;
            default :
                break;
        }
    }

    /**
     * Check if criteria contains only allowed keys
     * @param queryDto
     * @param allowedKeys
     */
    public static void checkContainsAuthorizedKeys(final QueryDto queryDto, final Collection<String> allowedKeys) {
        queryDto.getCriterionList().forEach(criterion -> {
            if (allowedKeys.contains(criterion.getKey())) {
                return;
            }

            // if we have a ElemMatch operator we have to check that current field is allowed and his child field also
            // field.childField
            String keyWithPoint = criterion.getKey() + ".";
            if (criterion.getOperator().equals(CriterionOperator.ELEMMATCH) &&
                allowedKeys.stream().anyMatch(key -> key.startsWith(keyWithPoint))) {
                // we recurse on children's to check the allowed key
                try {
                    QueryDto elemMatchQuery = QueryDto.fromJson(JsonUtils.toJson(criterion.getValue()));
                    List<String> elemAllowedKeys =
                        allowedKeys.stream()
                            .filter(key -> key.startsWith(keyWithPoint))
                            .map(key -> key.replaceFirst(keyWithPoint, ""))
                            .collect(Collectors.toList());
                    checkContainsAuthorizedKeys(elemMatchQuery, elemAllowedKeys);
                }
                catch (JsonProcessingException e) {
                    throw new InvalidFormatException(e.getMessage(), e);
                }
                return;
            }

            throw new ForbiddenException("Criterion with key : " + criterion.getKey() + " is not allowed");
        });
        queryDto.getSubQueries().forEach(queryDtoItem -> checkContainsAuthorizedKeys(queryDtoItem, allowedKeys));
    }

    public static QueryDto fromJson(final String criteriaJson) {
        if (StringUtils.isBlank(criteriaJson) || StringUtils.equals(criteriaJson, "null")) {
                return new QueryDto();
        }
        try {
            return JsonUtils.fromJson(criteriaJson, QueryDto.class);
        }
        catch (final IOException e) {
            throw new InvalidFormatException("criteria is mal formed :" + e.getMessage(), e);
        }
    }

    public static String toJson(final QueryDto criteria) {
        try {
            return JsonUtils.toJson(criteria);
        } catch (final JsonProcessingException e) {
            throw new InvalidFormatException(e.getMessage(), e);

        }
    }

    public static boolean isNullOrEmpty(final QueryDto criteria) {
        return criteria == null || criteria.isEmpty();
    }

}
