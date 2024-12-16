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

package fr.gouv.vitamui.referential.internal.server.schema;

import fr.gouv.vitam.common.model.administration.schema.SchemaCardinality;
import fr.gouv.vitam.common.model.administration.schema.SchemaInputModel;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.referential.common.dto.ImportSchemaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ImportSchemaConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportSchemaConverter.class);

    /**
     * Converts an ImportSchemaDto to a SchemaInputModel.
     *
     * @param dto the ImportSchemaDto to convert
     * @return the converted SchemaInputModel
     */
    public SchemaInputModel convertDtoToVitam(final ImportSchemaDto dto) {
        validateDto(dto);
        final SchemaInputModel importSchema = VitamUIUtils.copyProperties(dto, new SchemaInputModel());
        copyCustomProperties(dto, importSchema);
        return importSchema;
    }

    /**
     * Converts a SchemaInputModel to an ImportSchemaDto.
     *
     * @param schemaInputModel the SchemaInputModel to convert
     * @return the converted ImportSchemaDto
     */
    public ImportSchemaDto convertVitamToDto(final SchemaInputModel schemaInputModel) {
        final ImportSchemaDto dto = VitamUIUtils.copyProperties(schemaInputModel, new ImportSchemaDto());
        copyCustomProperties(schemaInputModel, dto);
        return dto;
    }

    /**
     * Converts a list of ImportSchemaDto objects to SchemaInputModel objects.
     *
     * @param dtos the list of ImportSchemaDto objects
     * @return the converted list of SchemaInputModel objects
     */
    public List<SchemaInputModel> convertDtosToVitams(final List<ImportSchemaDto> dtos) {
        return dtos.stream().map(this::convertDtoToVitam).collect(Collectors.toList());
    }

    /**
     * Converts a list of SchemaInputModel objects to ImportSchemaDto objects.
     *
     * @param importSchema the list of SchemaInputModel objects
     * @return the converted list of ImportSchemaDto objects
     */
    public List<ImportSchemaDto> convertVitamsToDtos(final List<SchemaInputModel> importSchema) {
        return importSchema.stream().map(this::convertVitamToDto).collect(Collectors.toList());
    }

    /**
     * Validates the provided DTO.
     *
     * @param dto the DTO to validate
     */
    void validateDto(final ImportSchemaDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }
        if (dto.getCardinality() == null || dto.getCardinality().isBlank()) {
            throw new IllegalArgumentException("Cardinality cannot be null or blank");
        }
        try {
            SchemaCardinality.valueOf(dto.getCardinality());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid cardinality value: {}", dto.getCardinality(), e);
            throw new IllegalArgumentException("Invalid cardinality: " + dto.getCardinality(), e);
        }
    }

    /**
     * Copies custom properties from a DTO to a SchemaInputModel.
     *
     * @param dto          the source DTO
     * @param importSchema the target SchemaInputModel
     */
    private void copyCustomProperties(final ImportSchemaDto dto, final SchemaInputModel importSchema) {
        if (dto.getPath() != null) {
            importSchema.setPath(dto.getPath());
        }
        importSchema.setCardinality(SchemaCardinality.valueOf(dto.getCardinality()));
        importSchema.setObject(dto.isObject());
        importSchema.setShortName(dto.getShortName());
        importSchema.setDescription(dto.getDescription());
    }

    /**
     * Copies custom properties from a SchemaInputModel to a DTO.
     *
     * @param schemaInputModel the source SchemaInputModel
     * @param dto              the target DTO
     */
    void copyCustomProperties(final SchemaInputModel schemaInputModel, final ImportSchemaDto dto) {
        if (schemaInputModel.getPath() != null) {
            dto.setPath(schemaInputModel.getPath());
        }
        dto.setCardinality(String.valueOf(schemaInputModel.getCardinality()));
        dto.setObject(schemaInputModel.isObject());
        dto.setShortName(schemaInputModel.getShortName());
        dto.setDescription(schemaInputModel.getDescription());
    }
}
