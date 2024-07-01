/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.iam.internal.server.externalparamprofile.dao;

import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.externalparamprofile.service.ExternalParamProfileInternalService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;

import java.util.ArrayList;
import java.util.List;

public class ExternalParamProfileRepository {

    private final MongoOperations mongoOperations;

    private static final String PARAMETERS = "parameters";
    private static final String COUNT = "count";
    private static final String DESCRIPTION = "description";
    private static final String ENABLED = "enabled";
    private static final String EXTERNAL = "external";
    private static final String EXTERNAL_IDENTIFIER = "external.identifier";
    private static final String EXTERNAL_PARAM_ID = "externalParamId";
    private static final String EXTERNAL_PARAM_IDENTIFIER = "externalParamIdentifier";
    private static final String EXTERNAL_ID = "external._id";
    private static final String EXTERNAL_PARAMETERS = "external.parameters";
    private static final String ID = "_id";
    private static final String ID_EXTERNAL_PARAM = "idExternalParam";
    private static final String ID_PROFILE = "idProfile";
    private static final String IDENTIFIER = "identifier";
    private static final String NAME = "name";
    private static final String PROFILE_ID = "profileIdentifier";

    public ExternalParamProfileRepository(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public ExternalParamProfileDto findByIdProfile(String idProfile) {
        String criteria =
            "{\"criteria\":[{\"queryOperator\":\"AND\",\"criteria\":[{\"key\":\"_id\",\"value\":\"" +
            idProfile +
            "\",\"operator\":\"EQUALSIGNORECASE\"}]}]}";
        Aggregation aggregation = buildAggregation(criteria, null, null);
        ExternalParamProfileDto profileDto = mongoOperations
            .aggregate(aggregation, MongoDbCollections.PROFILES, ExternalParamProfileDto.class)
            .getUniqueMappedResult();
        if (profileDto != null) {
            ExternalParamProfileInternalService.extractFieldsFromExternalParameters(profileDto);
        }
        return profileDto;
    }

    public PaginatedValuesDto<ExternalParamProfileDto> getAllPaginated(
        final Integer pageNumber,
        final Integer size,
        final String criteria,
        final String orderBy,
        final DirectionDto direction
    ) {
        Aggregation aggregation = buildAggregation(criteria, orderBy, direction);
        List<AggregationOperation> operations = aggregation.getPipeline().getOperations();

        //Count
        Aggregation countAggregation = Aggregation.newAggregation(operations.toArray(new AggregationOperation[0]));
        countAggregation.getPipeline().add(Aggregation.count().as(COUNT));
        NumberOfResults counter = mongoOperations
            .aggregate(countAggregation, MongoDbCollections.PROFILES, NumberOfResults.class)
            .getUniqueMappedResult();

        //pagination
        Aggregation paginateAggregation = Aggregation.newAggregation(operations.toArray(new AggregationOperation[0]));
        SkipOperation skipOperation = Aggregation.skip((long) pageNumber * size);
        LimitOperation limitOperation = Aggregation.limit(size);
        paginateAggregation.getPipeline().add(skipOperation).add(limitOperation);

        List<ExternalParamProfileDto> dtos = mongoOperations
            .aggregate(paginateAggregation, MongoDbCollections.PROFILES, ExternalParamProfileDto.class)
            .getMappedResults();
        dtos.stream().forEach(dto -> ExternalParamProfileInternalService.extractFieldsFromExternalParameters(dto));

        int count = counter != null ? counter.getCount() : 0;
        boolean hasMore = pageNumber * size + dtos.size() < count;
        return new PaginatedValuesDto<>(dtos, pageNumber, size, hasMore);
    }

    private Aggregation buildAggregation(final String criteria, final String orderBy, final DirectionDto direction) {
        List<AggregationOperation> operations = new ArrayList<>();

        if (criteria != null) {
            MatchOperation matchOperation = Aggregation.match(CriteriaQueryHelper.getCriteria(criteria));
            operations.add(matchOperation);
        }

        LookupOperation lookupOperation = Aggregation.lookup(
            MongoDbCollections.EXTERNAL_PARAMETERS,
            EXTERNAL_PARAM_ID,
            ID,
            EXTERNAL
        );
        operations.add(lookupOperation);

        UnwindOperation externalUnwindOperation = Aggregation.unwind(EXTERNAL, false);
        operations.add(externalUnwindOperation);

        ProjectionOperation projectionOperation = Aggregation.project(NAME, DESCRIPTION, ENABLED)
            .andExpression(IDENTIFIER)
            .as(PROFILE_ID)
            .andExpression(ID)
            .as(ID_PROFILE)
            .andExpression(PARAMETERS)
            .as(EXTERNAL_PARAMETERS)
            .andInclude(Fields.from(Fields.field(PARAMETERS, EXTERNAL_PARAMETERS)))
            .andInclude(Fields.from(Fields.field(EXTERNAL_PARAM_IDENTIFIER, EXTERNAL_IDENTIFIER)))
            .andInclude(Fields.from(Fields.field(ID_EXTERNAL_PARAM, EXTERNAL_ID)));

        operations.add(projectionOperation);

        if (orderBy != null && direction != null) {
            SortOperation sortOperation = Aggregation.sort(Sort.Direction.valueOf(direction.name()), orderBy);
            operations.add(sortOperation);
        }

        return Aggregation.newAggregation(operations);
    }

    private static class NumberOfResults {

        private int count = 0;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "NumberOfResults{" + "count=" + count + '}';
        }
    }
}
