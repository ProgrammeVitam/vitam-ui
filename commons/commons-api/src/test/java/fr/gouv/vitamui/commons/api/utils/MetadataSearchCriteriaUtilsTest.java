/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
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
package fr.gouv.vitamui.commons.api.utils;

import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.fillQueryFromMgtRulesCriteriaList;

@ExtendWith(SpringExtension.class)
public class MetadataSearchCriteriaUtilsTest {

    @Test
    public void testFillQueryFromCriteriaListWhenNullCriteriaList() throws InvalidCreateOperationException {
        //Given
        //When
        BooleanQuery query = or();
        fillQueryFromMgtRulesCriteriaList(query, null);

        //then
        Assertions.assertTrue(query.getQueries().isEmpty());
    }

    @Test
    public void handleSimpleFieldCriteria() throws InvalidCreateOperationException {
        BooleanQuery queryToFill = new BooleanQuery(BuilderToken.QUERY.OR);
        SearchCriteriaEltDto searchCriteria = new SearchCriteriaEltDto()
            .setCriteria("ActivationDate")
            .setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS)
            .setOperator(ArchiveSearchConsts.CriteriaOperators.GT.name())
            .setValues(List.of(new CriteriaValue().setValue("2023-03-05T23:00:00.000Z")))
            .setDataType(ArchiveSearchConsts.CriteriaDataType.DATE.name());

        MetadataSearchCriteriaUtils.handleSimpleFieldCriteria(queryToFill, searchCriteria);

        Assertions.assertEquals(
            "{\"$or\":[{\"$gt\":{\"ActivationDate\":\"2023-03-05T23:00:00.000Z\"}}]}",
            queryToFill.toString()
        );
    }

    @Test
    public void handleSimpleFieldCriteria_with_DATE_and_EQ() throws InvalidCreateOperationException {
        BooleanQuery queryToFill = or();
        SearchCriteriaEltDto searchCriteria = new SearchCriteriaEltDto()
            .setCriteria("ontologyFieldDate")
            .setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS)
            .setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name())
            .setValues(List.of(new CriteriaValue().setValue("2023-03-05T23:00:00.000Z")))
            .setDataType(ArchiveSearchConsts.CriteriaDataType.DATE.name());

        // When
        MetadataSearchCriteriaUtils.handleSimpleFieldCriteria(queryToFill, searchCriteria);

        // Then
        Assertions.assertEquals(
            "{\"$or\":[" +
            "{\"$and\":[" +
            "{\"$gte\":{\"ontologyFieldDate\":\"2023-03-05T23:00:00.000Z\"}}," +
            "{\"$lt\":{\"ontologyFieldDate\":\"2023-03-06T23:00:00.000Z\"}}" +
            "]}" +
            "]}",
            queryToFill.toString()
        );
    }
}
