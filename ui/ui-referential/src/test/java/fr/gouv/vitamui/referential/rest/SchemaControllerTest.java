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

package fr.gouv.vitamui.referential.rest;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.referential.common.dto.SchemaDto;
import fr.gouv.vitamui.referential.common.model.Collection;
import fr.gouv.vitamui.referential.service.SchemaService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitamui.referential.common.model.Collection.ARCHIVE_UNIT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(controllers = SchemaController.class)
public class SchemaControllerTest extends UiReferentialRestControllerTest<IdDto> {
    @MockBean private SchemaService schemaService;
    @Value("${ui-referential.prefix}")
    protected String moduleName;

    @Test
    public void shouldListSchemas() throws Exception {
        final Collection[] collections = {ARCHIVE_UNIT};
        final List<SchemaDto> schemas = List.of();

        Mockito.when(schemaService.getSchemas(any(), eq(collections))).thenReturn(schemas);

        final Map<String, Object> params = new HashMap<>();

        params.put("collections", ARCHIVE_UNIT);

        super.performGet("", params)
            .andDo(print()).andExpect(status().isOk()).andExpect(content().string(Matchers.containsString("[]")));

        Mockito.verify(schemaService, Mockito.times(1)).getSchemas(any(), eq(collections));
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return null;
    }

    @Override
    protected IdDto buildDto() {
        return null;
    }

    @Override
    protected VitamUILogger getLog() {
        return null;
    }

    @Override
    protected void preparedServices() {

    }

    @Override
    protected String getRessourcePrefix() {
        return "/" + moduleName + "/schemas";
    }
}