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
package fr.gouv.vitamui.commons.mongo.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.Assert;

import fr.gouv.vitamui.commons.api.domain.BaseIdDocument;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.service.BaseCrudService;
import fr.gouv.vitamui.commons.mongo.CustomSequencesConstants;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;

/**
 * A service to read, create, update and delete an object with identifier.
 *
 *
 */
public abstract class VitamUICrudService<D extends IdDto, E extends BaseIdDocument> extends VitamUIReadService<D, E> implements BaseCrudService<D, E> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(VitamUICrudService.class);

    private static final String NO_ENTITY_MESSAGE = ": no entity found with id: ";

    /**
     * Service allowing to manage a sequence.
     */
    protected SequenceGeneratorService sequenceGeneratorService;

    /**
     * Constructor allowing to initialize a default sequence generator.
     * @param sequenceRepository Repository allowing to manage sequences.
     */
    public VitamUICrudService(final CustomSequenceRepository sequenceRepository) {
        sequenceGeneratorService = new SequenceGeneratorService(sequenceRepository);
    }

    /**
     * Constructor allowing to initialize a custom sequence generator.
     * @param sequenceGeneratorService Custom sequence generator.
     */
    public VitamUICrudService(final SequenceGeneratorService sequenceGeneratorService) {
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public D create(final D dto) {
        LOGGER.debug("Create {} with {}", getObjectName(), dto);
        Assert.isNull(dto.getId(), "The DTO identifier must be null for creation.");
        beforeCreate(dto);
        dto.setId(generateSuperId());
        final E entity = convertFromDtoToEntity(dto);
        beforeCreate(entity);
        final E createdEntity = getRepository().save(entity);
        return convertFromEntityToDto(createdEntity);
    }

    protected void beforeCreate(final E entity) {
    }

    /**
     * Method allowing to perform checks and provide additional information before to create an object.
     * @param dto Object to create.
     */
    protected void beforeCreate(final D dto) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public D update(final D dto) {
        LOGGER.debug("Update {} {}", getObjectName(), dto);
        beforeUpdate(dto);
        final E entity = convertFromDtoToEntity(dto);
        LOGGER.debug("Update entity {} {}", getObjectName(), entity);
        Assert.isTrue(entity != null, "Unable to update " + getObjectName() + ": entity is null");
        Assert.isTrue(getRepository().existsById(entity.getId()), "Unable to update " + getObjectName() + NO_ENTITY_MESSAGE + entity.getId());
        final E savedEntity = getRepository().save(entity);
        return convertFromEntityToDto(savedEntity);
    }

    /**
     * Method allowing to perform checks and provide additional information before to update an object.
     * @param dto Object to update.
     */
    protected void beforeUpdate(final D dto) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public D patch(final Map<String, Object> partialDto) {
        LOGGER.debug("Patch {} with {}", getObjectName(), partialDto);
        final E entity = beforePatch(partialDto);
        processPatch(entity, partialDto);
        Assert.isTrue(getRepository().existsById(entity.getId()), "Unable to patch " + getObjectName() + NO_ENTITY_MESSAGE + entity.getId());
        final E savedEntity = getRepository().save(entity);
        return convertFromEntityToDto(savedEntity);
    }

    /**
     * Method to process for patch specific fields. If value is null, update fields with null values
     * @param entity Entity to patch.
     * @param partialDto Specifics fields to update.
     */
    protected void processPatch(final E entity, final Map<String, Object> partialDto) {
    }

    protected boolean checkMapContainsOnlyFieldsUnmodifiable(final Map<String, Object> partialDto, final List<String> unmodifiableFields) {
        boolean containsOnlyFieldsUnModifiable = true;
        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            if (!unmodifiableFields.contains(entry.getKey())) {
                containsOnlyFieldsUnModifiable = false;
            }
        }
        return containsOnlyFieldsUnModifiable;
    }

    /**
     * Method allowing to perform checks/retrieve additional information before to patch the entity.
     * @param partialDto Specifics fields to update.
     * @return The entity to update.
     */
    protected E beforePatch(final Map<String, Object> partialDto) {
        final String id = (String) partialDto.get("id");
        return getRepository().findById(id).orElseThrow(() -> new IllegalArgumentException("Unable to patch " + getObjectName() + NO_ENTITY_MESSAGE + id));
    }

    /**
     * Method allowing to perform checks/retrieve additional information before to delete the entity.
     * @param partialDto Specifics fields to update.
     * @return The entity to update.
     */
    @Override
    public void delete(final String id) {
        LOGGER.debug("Delete {} {}", getObjectName(), id);
        beforeDelete(id);
        getRepository().deleteById(id);
    }

    /**
     * Method allowing to perform checks/retrieve additional information before to delete the entity.
     * @param id Id of the entity.
     */
    protected void beforeDelete(final String id) {
    }

    /**
     * Method allowing to generate the next value of the sequence.
     * If the sequence does not exist, it will be created with the default value
     * <code>CustomSequencesConstants.DEFAULT_SEQUENCE_START_VALUE</code>.
     * @param seqName The name of the sequence.
     * @return The next value of the sequence.
     */
    public String getNextSequenceId(final String seqName) {
        return getNextSequenceId(seqName, CustomSequencesConstants.DEFAULT_SEQUENCE_START_VALUE).toString();
    }

    /**
     * Method allowing to generate the next value of the sequence.
     * If the sequence does not exist, it will be created with the given default value.
     * @param seqName The name of the sequence.
     * @param defaultValue Default value of the sequence.
     * @return The next value of the sequence.
     */
    public Integer getNextSequenceId(final String seqName, final int defaultValue) {
        return sequenceGeneratorService.getNextSequenceId(seqName, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateSuperId() {
        return getRepository().generateSuperId();
    }

    /**
     * Method allowing to check of the identifier is set.
     * @param identifier Identifier to check.
     * @param message Prefix of the message's exception.
     */
    protected void checkIdentifier(final String identifier, final String message) {
        Assert.isNull(identifier, message + ": identifier must be null");
    }

    /**
     * Convert from DTO to Entity.
     * @param dto
     * @return
     */
    protected final E convertFromDtoToEntity(final D dto) {
        return dto != null ? internalConvertFromDtoToEntity(dto) : null;
    }

    /**
     * Convert from DTO to entity.
     *
     * @param dto lDTO received.
     * @return the resulting entity.
     */
    protected E internalConvertFromDtoToEntity(final D dto) {
        return getConverter().convertDtoToEntity(dto);
    }

}
