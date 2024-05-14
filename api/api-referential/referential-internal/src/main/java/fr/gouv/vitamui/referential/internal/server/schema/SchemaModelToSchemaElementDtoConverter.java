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

import com.fasterxml.jackson.databind.util.StdConverter;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitam.common.model.administration.schema.SchemaStringSizeType;
import fr.gouv.vitam.common.model.administration.schema.SchemaType;
import fr.gouv.vitamui.referential.common.dto.SchemaElementDto;
import fr.gouv.vitamui.referential.common.model.Cardinality;
import fr.gouv.vitamui.referential.common.model.Collection;
import fr.gouv.vitamui.referential.common.model.DataType;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;

public class SchemaModelToSchemaElementDtoConverter extends StdConverter<SchemaResponse, SchemaElementDto> {

    @Override
    public SchemaElementDto convert(SchemaResponse schemaModel) {
        final SchemaStringSizeType stringTypeSize = schemaModel.getStringSize();

        return (SchemaElementDto) new SchemaElementDto()
            .setPath(schemaModel.getPath())
            .setStringSize(Optional.ofNullable(stringTypeSize).map(SchemaStringSizeType::value).orElse(null))
            .setCardinality(Cardinality.of(schemaModel.getCardinality().value()))
            .setFieldName(schemaModel.getFieldName())
            .setShortName(schemaModel.getShortName())
            .setDescription(schemaModel.getDescription())
            .setSedaField(schemaModel.getSedaField())
            .setApiField(schemaModel.getApiField())
            .setType(schemaModel.getType())
            .setOrigin(schemaModel.getOrigin())
            .setCollection(mapCollections(schemaModel))
            .setSedaVersions(schemaModel.getSedaVersions())
            .setCategory(schemaModel.getCategory())
            .setApiPath(Optional.ofNullable(schemaModel.getApiPath()).orElse(schemaModel.getPath()))
            .setDataType(convertFromIndexationType(schemaModel.getType()));
    }

    private DataType convertFromIndexationType(SchemaType indexationType) {
        switch (indexationType) {
            case KEYWORD:
            case ENUM:
            case TEXT:
                return DataType.STRING;
            case DATE:
                return DataType.DATETIME;
            case OBJECT:
            case LONG:
            case DOUBLE:
            case BOOLEAN:
                return DataType.valueOf(indexationType.getType());
        }
        return null;
    }

    private Collection mapCollections(final SchemaResponse schemaResponse) {
        if (isNull(schemaResponse.getCollection())) {
            return null;
        }
        if (Objects.equals(schemaResponse.getCollection(), "Unit")) {
            return Collection.ARCHIVE_UNIT;
        }
        return Collection.valueOf(schemaResponse.getCollection());
    }
}
