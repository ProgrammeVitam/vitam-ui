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
import fr.gouv.vitam.common.model.administration.OntologyStringTypeSize;
import fr.gouv.vitam.common.model.administration.OntologyType;
import fr.gouv.vitam.common.model.administration.SchemaModel;
import fr.gouv.vitamui.referential.common.dto.ExtendedOntologyDto;
import fr.gouv.vitamui.referential.common.model.Cardinality;
import fr.gouv.vitamui.referential.common.model.Collection;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class SchemaModelToExtendedOntologyDtoConverter extends StdConverter<SchemaModel, ExtendedOntologyDto> {
    @Override
    public ExtendedOntologyDto convert(SchemaModel schemaModel) {
        final OntologyStringTypeSize stringTypeSize = schemaModel.getStringSize();
        final String stringSize = stringTypeSize != null ? stringTypeSize.value().toLowerCase(Locale.ROOT) : null;

        return (ExtendedOntologyDto) new ExtendedOntologyDto()
            //        setId(schemaModel.getId()) // No mapping
            //        setTenant(schemaModel.getTenant()) // No mapping
            //        setVersion(schemaModel.getVersion()) // Don't know if still used
            //        setDepth(schemaModel.getDepth()) // Not precomputed
            .setPath(schemaModel.getPath())
            //        setDataType(schemaModel.getDataType()) // Mapped by Type
            .setDataSize(stringSize)
            .setCardinality(Cardinality.of(schemaModel.getCardinality().value()))
            .setIdentifier(schemaModel.getIdentifier())
            .setShortName(schemaModel.getShortName())
            .setDescription(schemaModel.getDescription())
            //        setCreationDate(schemaModel.getCreationDate()) // No mapping
            //        setLastUpdate(schemaModel.getLastUpdate()) // No mapping
            .setSedaField(schemaModel.getSedaField())
            .setApiField(schemaModel.getApiField())
            .setType(schemaModel.getType())
            .setOrigin(schemaModel.getOrigin())
            .setCollections(mapCollections(schemaModel))
            .setSedaVersions(schemaModel.getSedaVersions())
            .setRootPaths(null);
    }

    private List<Collection> mapCollections(final SchemaModel schemaModel) {
        return schemaModel.getCollections().stream().map(collection -> {
            if (Objects.equals(collection, "Unit")) {
                return Collection.ARCHIVE_UNIT;
            }
            return Collection.of(collection);
        }).collect(Collectors.toList());
    }
}
