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
package fr.gouv.vitamui.iam.internal.server.externalParameters.service;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.enums.Application;
import fr.gouv.vitamui.iam.internal.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Getter
@Setter
public class ExternalParametersInternalService extends VitamUICrudService<ExternalParametersDto, ExternalParameters> {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ExternalParametersInternalService.class);

    private final ExternalParametersRepository externalParametersRepository;

    private final ExternalParametersConverter externalParametersConverter;

    private final InternalSecurityService internalSecurityService;

    @Autowired
    public ExternalParametersInternalService(
        final CustomSequenceRepository sequenceRepository,
        final ExternalParametersRepository externalParametersRepository,
        final ExternalParametersConverter externalParametersConverter,
        final InternalSecurityService internalSecurityService) {
        super(sequenceRepository);
        this.externalParametersRepository = externalParametersRepository;
        this.externalParametersConverter = externalParametersConverter;
        this.internalSecurityService = internalSecurityService;
    }

    /**
     * Retrieve the external parameters associated to the authenticated user.
     *
     * @return
     */
    public ExternalParametersDto getMyExternalParameters() {
        LOGGER.debug("GetMyExternalParameters");
        AuthUserDto authUserDto = internalSecurityService.getUser();

        if (authUserDto != null && authUserDto.getProfileGroup() != null &&
            authUserDto.getProfileGroup().getProfiles() != null) {

            Optional<ProfileDto> externalParametersProfile = authUserDto.getProfileGroup().getProfiles().stream()
                .filter(p -> Application.EXTERNAL_PARAMS.toString().equalsIgnoreCase(p.getApplicationName()))
                .findFirst();
            if (!externalParametersProfile.isEmpty() && externalParametersProfile.isPresent()) {
                return this.getOne(externalParametersProfile.get().getExternalParamId());
            }
        }

        return null;
    }

    @Override
    @Transactional
    public ExternalParametersDto create(final ExternalParametersDto dto) {
        final ExternalParametersDto externalParameter = super.create(dto);
        return externalParameter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ExternalParametersRepository getRepository() {
        return externalParametersRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalParameters internalConvertFromDtoToEntity(final ExternalParametersDto dto) {
        return super.internalConvertFromDtoToEntity(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalParametersDto internalConvertFromEntityToDto(final ExternalParameters parameters) {
        return super.internalConvertFromEntityToDto(parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<ExternalParameters> getEntityClass() {
        return ExternalParameters.class;
    }

    @Override
    protected String getObjectName() {
        return "externalParameters";
    }


    @Override
    protected Converter<ExternalParametersDto, ExternalParameters> getConverter() {
        return externalParametersConverter;
    }
}
