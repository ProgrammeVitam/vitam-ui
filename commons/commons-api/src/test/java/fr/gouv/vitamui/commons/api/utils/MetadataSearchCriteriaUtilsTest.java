package fr.gouv.vitamui.commons.api.utils;

import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
}
