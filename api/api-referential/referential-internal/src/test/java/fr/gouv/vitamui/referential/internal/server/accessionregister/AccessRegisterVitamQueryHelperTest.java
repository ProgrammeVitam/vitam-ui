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
package fr.gouv.vitamui.referential.internal.server.accessionregister;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.api.domain.AccessionRegisterSearchDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.FileNotFoundException;
import java.io.IOException;

class AccessRegisterVitamQueryHelperTest {

    private static String getResourceAsString(String name) throws FileNotFoundException {
        return PropertiesUtils.getResourceAsString("json/access-register/" + name).trim();
    }

    private static AccessionRegisterSearchDto getResourceAsCriteria(String name)
        throws FileNotFoundException, InvalidParseOperationException {
        return JsonHandler.getFromInputStream(
            PropertiesUtils.getResourceAsStream("json/access-register/" + name),
            AccessionRegisterSearchDto.class
        );
    }

    @Test
    void createQueryDSL_test_with_all_params()
        throws InvalidCreateOperationException, InvalidParseOperationException, IOException, JSONException {
        //given
        //when
        JsonNode jsonNode = AccessRegisterVitamQueryHelper.createQueryDSL(
            getResourceAsCriteria("request-with-all-params.json"),
            12,
            1,
            "messageIdentifierOrder",
            DirectionDto.DESC
        );
        JSONAssert.assertEquals(jsonNode.toString(), getResourceAsString("dsl01.json"), false);
    }

    @Test
    void createQueryDSL_test_without_acquisitionInformations()
        throws InvalidCreateOperationException, InvalidParseOperationException, IOException, JSONException {
        //given
        //when
        JsonNode jsonNode = AccessRegisterVitamQueryHelper.createQueryDSL(
            getResourceAsCriteria("request-without-acquisitionInformations.json"),
            12,
            1,
            "messageIdentifierOrder",
            DirectionDto.DESC
        );
        JSONAssert.assertEquals(jsonNode.toString(), getResourceAsString("dsl02.json"), false);
    }

    @Test
    void createQueryDSL_test_with_acquisitionInformations_full()
        throws InvalidCreateOperationException, InvalidParseOperationException, IOException, JSONException {
        //given
        //when
        JsonNode jsonNode = AccessRegisterVitamQueryHelper.createQueryDSL(
            getResourceAsCriteria("request-with-acquisitionInformations-full.json"),
            12,
            1,
            "messageIdentifierOrder",
            DirectionDto.DESC
        );
        JSONAssert.assertEquals(jsonNode.toString(), getResourceAsString("dsl03.json"), false);
    }

    @Test
    void createQueryDSL_test_with_acquisitionInformations_variant_1()
        throws InvalidCreateOperationException, InvalidParseOperationException, IOException, JSONException {
        //given
        //when
        JsonNode jsonNode = AccessRegisterVitamQueryHelper.createQueryDSL(
            getResourceAsCriteria("request-with-acquisitionInformations-variant-1.json"),
            12,
            1,
            "messageIdentifierOrder",
            DirectionDto.DESC
        );
        JSONAssert.assertEquals(jsonNode.toString(), getResourceAsString("dsl04.json"), false);
    }

    @Test
    void createQueryDSL_test_with_acquisitionInformations_variant_2()
        throws InvalidCreateOperationException, InvalidParseOperationException, IOException, JSONException {
        //given
        //when
        JsonNode jsonNode = AccessRegisterVitamQueryHelper.createQueryDSL(
            getResourceAsCriteria("request-with-acquisitionInformations-variant-2.json"),
            12,
            1,
            "messageIdentifierOrder",
            DirectionDto.DESC
        );
        JSONAssert.assertEquals(jsonNode.toString(), getResourceAsString("dsl05.json"), false);
    }

    @Test
    void createQueryDSL_test_with_acquisitionInformations_variant_3()
        throws InvalidCreateOperationException, InvalidParseOperationException, IOException, JSONException {
        //given
        //when
        JsonNode jsonNode = AccessRegisterVitamQueryHelper.createQueryDSL(
            getResourceAsCriteria("request-with-acquisitionInformations-variant-3.json"),
            12,
            1,
            "messageIdentifierOrder",
            DirectionDto.DESC
        );
        JSONAssert.assertEquals(jsonNode.toString(), getResourceAsString("dsl06.json"), false);
    }

    @Test
    void createQueryDSL_test_with_order_only()
        throws InvalidCreateOperationException, InvalidParseOperationException, FileNotFoundException, JSONException {
        //given
        //when
        JsonNode jsonNode = AccessRegisterVitamQueryHelper.createQueryDSL(
            new AccessionRegisterSearchDto(),
            12,
            1,
            "messageIdentifierOrder",
            null
        );
        JSONAssert.assertEquals(jsonNode.toString(), getResourceAsString("dsl07.json"), false);
    }

    @Test
    void createQueryDSL_test_without_order()
        throws InvalidCreateOperationException, InvalidParseOperationException, FileNotFoundException, JSONException {
        //given
        //when
        JsonNode jsonNode = AccessRegisterVitamQueryHelper.createQueryDSL(
            new AccessionRegisterSearchDto(),
            12,
            1,
            null,
            null
        );
        JSONAssert.assertEquals(jsonNode.toString(), getResourceAsString("dsl08.json"), false);
    }
}
