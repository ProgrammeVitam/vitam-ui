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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.OperationDto;
import fr.gouv.vitamui.commons.mongo.converter.OperationConverter;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.dao.OperationRepository;
import fr.gouv.vitamui.commons.mongo.domain.Operation;
import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;

public class OperationService extends VitamUICrudService<OperationDto, Operation> {

    protected final OperationRepository<Operation> repository;

    private final Map<String, Converter> customConverters = new HashMap<>();

    private final Converter<OperationDto, Operation> defaultConverter;

    public OperationService(final CustomSequenceRepository sequenceRepository, final OperationRepository<Operation> repository,
            final OperationConverter defaultConverter) {
        super(sequenceRepository);
        this.repository = repository;
        this.defaultConverter = defaultConverter;
    }

    @Override
    protected void beforeCreate(final OperationDto dto) {

        dto.setCreationDate(OffsetDateTime.now());
        dto.setLastModificationDate(dto.getCreationDate());
        checkIntegrity(dto);
    }

    @Override
    protected void beforeUpdate(final OperationDto dto) {

        dto.setLastModificationDate(OffsetDateTime.now());
        checkIntegrity(dto);
    }

    /**
     * Method allowing to validate an operation.
     * @param dto Operation to check.
     */
    protected void checkIntegrity(final OperationDto dto) {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        final Set<ConstraintViolation<OperationDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Unable to validate the operation: " + violations.stream()
                    .map(violation -> String.format("%s %s", violation.getPropertyPath(), violation.getMessage())).collect(Collectors.joining(",")));
        }
    }

    @Override
    protected VitamUIRepository<Operation, String> getRepository() {
        return repository;
    }

    @Override
    protected Class<Operation> getEntityClass() {
        return Operation.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OperationDto internalConvertFromEntityToDto(final Operation entity) {
        return getConverter(entity.getType()).convertEntityToDto(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Operation internalConvertFromDtoToEntity(final OperationDto dto) {
        return getConverter(dto.getType()).convertDtoToEntity(dto);
    }

    protected Converter<OperationDto, Operation> getConverter(final String type) {
        if (!customConverters.containsKey(type)) {
            return defaultConverter;
        }
        return customConverters.get(type);
    }

    /**
     * Method allowing to add a converter to the service.
     * @param type Type of the operation.
     * @param converter Converter linked to the type.
     */
    public void addConverter(final String type, final Converter<? extends OperationDto, ? extends Operation> converter) {
        customConverters.put(type, converter);
    }

}
