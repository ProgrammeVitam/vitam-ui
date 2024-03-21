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

package fr.gouv.vitamui.referential.internal.server.schema;

import fr.gouv.vitam.common.model.administration.schema.SchemaCardinality;
import fr.gouv.vitam.common.model.administration.schema.SchemaCategory;
import fr.gouv.vitam.common.model.administration.schema.SchemaOrigin;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitam.common.model.administration.schema.SchemaType;
import fr.gouv.vitamui.referential.common.dto.SchemaElementDto;
import fr.gouv.vitamui.referential.common.model.DataType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SchemaModelToSchemaElementDtoConverterTest {

    private final SchemaModelToSchemaElementDtoConverter converter = new SchemaModelToSchemaElementDtoConverter();

    @Test
    public void shouldDecorateWithDataType() {
        final String DATE_TYPE_KEY = "DataType";
        final SchemaResponse schemaResponse = new SchemaResponse();
        schemaResponse.setPath("Invoice");
        schemaResponse.setStringSize(null);
        schemaResponse.setCardinality(SchemaCardinality.MANY);
        schemaResponse.setFieldName("Invoice");
        schemaResponse.setShortName("Invoice");
        schemaResponse.setDescription("");
        schemaResponse.setSedaField("Invoice");
        schemaResponse.setApiField("Invoice");
        schemaResponse.setType(SchemaType.OBJECT);
        schemaResponse.setOrigin(SchemaOrigin.EXTERNAL);
        schemaResponse.setCollection("Unit");
        schemaResponse.setSedaVersions(List.of("2.1", "2.2", "2.3"));
        schemaResponse.setCategory(SchemaCategory.OTHER);
        schemaResponse.setApiPath("Invoice");

        schemaResponse.setType(SchemaType.KEYWORD);
        Assertions.assertThat(this.converter.convert(schemaResponse)).extracting(DATE_TYPE_KEY)
            .isEqualTo(DataType.STRING);

        schemaResponse.setType(SchemaType.TEXT);
        Assertions.assertThat(this.converter.convert(schemaResponse)).extracting(DATE_TYPE_KEY)
            .isEqualTo(DataType.STRING);

        schemaResponse.setType(SchemaType.ENUM);
        Assertions.assertThat(this.converter.convert(schemaResponse)).extracting(DATE_TYPE_KEY)
            .isEqualTo(DataType.STRING);

        schemaResponse.setType(SchemaType.DATE);
        Assertions.assertThat(this.converter.convert(schemaResponse)).extracting(DATE_TYPE_KEY)
            .isEqualTo(DataType.DATETIME);

        schemaResponse.setType(SchemaType.OBJECT);
        Assertions.assertThat(this.converter.convert(schemaResponse)).extracting(DATE_TYPE_KEY)
            .isEqualTo(DataType.OBJECT);

        schemaResponse.setType(SchemaType.BOOLEAN);
        Assertions.assertThat(this.converter.convert(schemaResponse)).extracting(DATE_TYPE_KEY)
            .isEqualTo(DataType.BOOLEAN);

        schemaResponse.setType(SchemaType.DOUBLE);
        Assertions.assertThat(this.converter.convert(schemaResponse)).extracting(DATE_TYPE_KEY)
            .isEqualTo(DataType.DOUBLE);

        schemaResponse.setType(SchemaType.LONG);
        Assertions.assertThat(this.converter.convert(schemaResponse)).extracting(DATE_TYPE_KEY)
            .isEqualTo(DataType.LONG);
    }

    @Test
    public void avoid_apiPath_null() {
        final SchemaResponse schemaResponse = new SchemaResponse();
        schemaResponse.setPath("Invoice");
        schemaResponse.setStringSize(null);
        schemaResponse.setCardinality(SchemaCardinality.MANY);
        schemaResponse.setFieldName("Invoice");
        schemaResponse.setShortName("Invoice");
        schemaResponse.setDescription("");
        schemaResponse.setSedaField("Invoice");
        schemaResponse.setApiField("Invoice");
        schemaResponse.setType(SchemaType.OBJECT);
        schemaResponse.setOrigin(SchemaOrigin.EXTERNAL);
        schemaResponse.setCollection("Unit");
        schemaResponse.setSedaVersions(List.of("2.1", "2.2", "2.3"));
        schemaResponse.setCategory(SchemaCategory.OTHER);
        schemaResponse.setApiPath(null);

        final SchemaElementDto schemaElementDto = this.converter.convert(schemaResponse);
        Assertions.assertThat(schemaElementDto.getPath()).isEqualTo("Invoice");
        Assertions.assertThat(schemaElementDto.getApiPath()).isEqualTo(schemaElementDto.getPath());
    }

}
