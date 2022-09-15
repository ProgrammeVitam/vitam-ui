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
package fr.gouv.vitamui.iam.internal.server.externalparamprofile.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ParameterDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.server.common.builder.ExternalParamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.common.builder.ProfileDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.externalparamprofile.converter.ExternalParamProfileConverter;
import fr.gouv.vitamui.iam.internal.server.externalparamprofile.dao.ExternalParamProfileRepository;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ExternalParamProfileInternalService {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ExternalParamProfileInternalService.class);
    public static final String PARAM_ACCESS_CONTRACT_NAME = "PARAM_ACCESS_CONTRACT";
    public static final String PARAM_BULK_OPERATIONS_THRESHOLD_NAME = "PARAM_BULK_OPERATIONS_THRESHOLD";

    private final InternalSecurityService internalSecurityService;
    private final ProfileInternalService profileInternalService;
    private final ExternalParametersInternalService externalParametersInternalService;
    private final IamLogbookService iamLogbookService;
    private final ExternalParamProfileRepository externalParamProfileRepository;
    private final LogbookService logbookService;
    private final ProfileConverter profileConverter;

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String ENABLED = "enabled";
    private static final String ACCESS_CONTRACT = "accessContract";
    private static final String USE_PLATFORM_BULK_OPERATIONS_THRESHOLD = "usePlatformThreshold";
    private static final String BULK_OPERATIONS_THRESHOLD = "bulkOperationsThreshold";
    private static final String ID_PROFILE = "idProfile";
    private static final String ID_EXTERNAL_PARAM = "idExternalParam";
    private static final String EXTERNAL_PARAM_PROFILE = "externalparamprofile";

    @Autowired
    public ExternalParamProfileInternalService(
        final ExternalParametersInternalService externalParametersInternalService,
        final ProfileInternalService profileInternalService,
        final InternalSecurityService internalSecurityService,
        final IamLogbookService iamLogbookService,
        final ExternalParamProfileRepository externalParamProfileRepository,
        final LogbookService logbookService,
        final ProfileConverter profileConverter) {
        this.externalParametersInternalService = externalParametersInternalService;
        this.internalSecurityService = internalSecurityService;
        this.profileInternalService = profileInternalService;
        this.iamLogbookService = iamLogbookService;
        this.externalParamProfileRepository = externalParamProfileRepository;
        this.logbookService = logbookService;
        this.profileConverter = profileConverter;
    }

    public PaginatedValuesDto<ExternalParamProfileDto> getAllPaginated(final Integer page, final Integer size,
        final String criteria, final String orderBy, final DirectionDto direction) {
        final Integer tenantIdentifier = internalSecurityService.getTenantIdentifier();
        QueryDto queryDto = QueryDto.fromJson(criteria);
        queryDto
            .addQuery(QueryDto.andQuery().addCriterion("tenantIdentifier", tenantIdentifier, CriterionOperator.EQUALS));
        return externalParamProfileRepository.getAllPaginated(page, size, queryDto.toJson(), orderBy, direction);
    }

    @Transactional
    public ExternalParamProfileDto create(final ExternalParamProfileDto entityDto) {
        LOGGER.debug("create access external parameter profile");
        LOGGER.debug("create {}, {}, {} ", entityDto.getName(), entityDto.getDescription(),
            entityDto.getAccessContract());
        final Integer tenantIdentifier = internalSecurityService.getTenantIdentifier();
        AuthUserDto authUserDto = internalSecurityService.getUser();

        String externalParamIdentifier =
            externalParametersInternalService.getExternalParametersRepository().generateSuperId();
        ExternalParametersDto savedExternalParametersDto = externalParametersInternalService
            .create(ExternalParamDtoBuilder.build(entityDto, externalParamIdentifier));

        ProfileDto profileDto = ProfileDtoBuilder.build(
            entityDto.getName(),
            entityDto.getDescription(),
            entityDto.isEnabled(),
            false,
            "",
            tenantIdentifier,
            CommonConstants.EXTERNAL_PARAMS_APP,
            List.of(
                ServicesData.ROLE_GET_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
                ServicesData.ROLE_EDIT_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
                ServicesData.ROLE_SEARCH_ACCESS_CONTRACT_EXTERNAL_PARAM_PROFILE,
                ServicesData.ROLE_GET_EXTERNAL_PARAMS
            ),
            authUserDto.getCustomerId(),
            savedExternalParametersDto.getId()
        );

        ProfileDto savedProfileDto = profileInternalService.create(profileDto);

        // Compléter dto
        entityDto.setExternalParamIdentifier(savedExternalParametersDto.getIdentifier());
        entityDto.setProfileIdentifier(savedProfileDto.getIdentifier());
        entityDto.setDateTime(OffsetDateTime.now());
        entityDto.setIdExternalParam(savedExternalParametersDto.getId());
        entityDto.setIdProfile(savedProfileDto.getId());
        entityDto.setId(savedProfileDto.getId());
        LOGGER.debug("savedProfileDto = {}", savedProfileDto);

        // Journalisation
        iamLogbookService.createExternalParamProfileEvent(entityDto);

        return entityDto;
    }

    public ExternalParamProfileDto getOne(final String id) {
        LOGGER.debug("get one with id : {}", id);
        return externalParamProfileRepository.findByIdProfile(id);
    }

    public JsonNode findHistoryById(final String id) throws VitamClientException {

        final Integer tenantIdentifier = internalSecurityService.getTenantIdentifier();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier)
            .setAccessContract(internalSecurityService.getTenant(tenantIdentifier).getAccessContractLogbookIdentifier())
            .setApplicationSessionId(internalSecurityService.getApplicationId());

        LOGGER.debug("Find History of external parameter profile by id {}, EvIdAppSession : {}", id,
            vitamContext.getApplicationSessionId());
        LOGGER.debug("findHistoryById : events.obId {}, events.obIdReq {}, VitamContext {}", id, "externalparamprofile",
            vitamContext);
        return logbookService
            .findEventsByIdentifierAndCollectionNames(id, EXTERNAL_PARAM_PROFILE, vitamContext)
            .toJsonNode();
    }

    @Transactional
    public ExternalParamProfileDto patch(final Map<String, Object> partialDto) {
        LOGGER.debug("Patch with {}", partialDto);
        final Collection<EventDiffDto> externalParamProfileLogbooks = new ArrayList<>();
        String idProfile = (String) partialDto.get(ID_PROFILE);

        ProfileDto profileDto = profileInternalService.getOne(idProfile);

        // Ancienne valeur
        ExternalParamProfileDto externalParamProfileDto =
            externalParamProfileRepository.findByIdProfile(idProfile);

        // Patch du profile
        patchProfile(profileDto, externalParamProfileDto, partialDto, externalParamProfileLogbooks);

        String idExternalParam = (String) partialDto.get(ID_EXTERNAL_PARAM);
        ExternalParametersDto externalParametersDto = externalParametersInternalService.getOne(idExternalParam);
        Collection<EventDiffDto> externalParametersLogbooks = new ArrayList<>();
        List<ParameterDto> newParametersList = externalParametersDto.getParameters();
        if (newParametersList == null) {
            newParametersList = new ArrayList<>();
        }
        // Journalisation

        //Access contract
        if (!externalParamProfileLogbooks.isEmpty() ||
            partialDto.get(ACCESS_CONTRACT) != null && partialDto.get(ACCESS_CONTRACT) != null) {
            newParametersList = newParametersList.stream().filter(
                parameterDto -> !ExternalParamDtoBuilder.PARAM_ACCESS_CONTRACT_NAME
                    .equals(parameterDto.getKey())).collect(
                Collectors.toList());

            String accessContract = (String) partialDto.get(ACCESS_CONTRACT);
            externalParamProfileLogbooks.add(new EventDiffDto(ExternalParamProfileConverter.ACCESS_CONTRACT,
                externalParamProfileDto.getAccessContract(), accessContract));
            externalParametersLogbooks.add(new EventDiffDto(ExternalParamProfileConverter.ACCESS_CONTRACT,
                externalParamProfileDto.getAccessContract(),
                accessContract));

            newParametersList
                .add(new ParameterDto(ExternalParamDtoBuilder.PARAM_ACCESS_CONTRACT_NAME, accessContract));

        }
        if (partialDto.get(NAME) != null) {
            externalParametersDto.setName((String) partialDto.get(NAME));
        }
        //thresholds
        if (partialDto.get(USE_PLATFORM_BULK_OPERATIONS_THRESHOLD) != null ||
            partialDto.get(BULK_OPERATIONS_THRESHOLD) != null) {

            Object bulkOperationThreshold = partialDto.get(BULK_OPERATIONS_THRESHOLD);
            //filter parameters by removing old thresholds
            newParametersList = newParametersList.stream().filter(
                parameterDto -> !ExternalParamDtoBuilder.PARAM_BULK_OPERATIONS_THRESHOLD_NAME
                    .equals(parameterDto.getKey())).collect(
                Collectors.toList());

            if (partialDto.get(USE_PLATFORM_BULK_OPERATIONS_THRESHOLD) != null) {
                boolean shouldUpdateBulkOperationThresholdFlag =
                    (boolean) partialDto.get(USE_PLATFORM_BULK_OPERATIONS_THRESHOLD);

                //replace old settings by new ones, else it will be removed
                if (!shouldUpdateBulkOperationThresholdFlag) {
                    newParametersList.add(new ParameterDto(ExternalParamDtoBuilder.PARAM_BULK_OPERATIONS_THRESHOLD_NAME,
                        ((Integer) bulkOperationThreshold).toString()));

                    externalParamProfileLogbooks.add(
                        new EventDiffDto(ExternalParamDtoBuilder.PARAM_BULK_OPERATIONS_THRESHOLD_NAME,
                            externalParamProfileDto.getBulkOperationsThreshold(), bulkOperationThreshold));
                    externalParametersLogbooks
                        .add(new EventDiffDto(ExternalParamDtoBuilder.PARAM_BULK_OPERATIONS_THRESHOLD_NAME,
                            externalParamProfileDto.getBulkOperationsThreshold(), bulkOperationThreshold));
                }
            } else {
                //replace old settings by new threshold
                newParametersList.add(new ParameterDto(ExternalParamDtoBuilder.PARAM_BULK_OPERATIONS_THRESHOLD_NAME,
                    ((Integer) bulkOperationThreshold).toString()));
                externalParamProfileLogbooks.add(
                    new EventDiffDto(ExternalParamDtoBuilder.PARAM_BULK_OPERATIONS_THRESHOLD_NAME,
                        externalParamProfileDto.getBulkOperationsThreshold(), bulkOperationThreshold));
                externalParametersLogbooks
                    .add(new EventDiffDto(ExternalParamDtoBuilder.PARAM_BULK_OPERATIONS_THRESHOLD_NAME,
                        externalParamProfileDto.getBulkOperationsThreshold(), bulkOperationThreshold));
            }
            externalParametersDto.setParameters(newParametersList);
        }
        externalParametersInternalService.update(externalParametersDto, externalParametersLogbooks);
        iamLogbookService.updateExternalParamProfileEvent(externalParamProfileDto, externalParamProfileLogbooks);

        externalParametersInternalService.update(externalParametersDto, externalParametersLogbooks);

        return externalParamProfileRepository.findByIdProfile(idProfile);
    }

    private void patchProfile(ProfileDto profileDto, ExternalParamProfileDto oldDto, Map<String, Object> partialDto,
        Collection<EventDiffDto> externalParamProfileLogbooks) {

        final Collection<EventDiffDto> logbooks = new ArrayList<>();

        for (final Map.Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case DESCRIPTION:
                    String description = (String) entry.getValue();
                    profileDto.setDescription(description);
                    logbooks.add(
                        new EventDiffDto(ProfileConverter.DESCRIPTION_KEY, oldDto.getDescription(),
                            description));
                    externalParamProfileLogbooks.add(
                        new EventDiffDto(ExternalParamProfileConverter.DESCRIPTION, oldDto.getDescription(),
                            description));
                    break;
                case NAME:
                    String name = (String) entry.getValue();
                    profileDto.setName(name);
                    logbooks.add(new EventDiffDto(ProfileConverter.NAME_KEY, oldDto.getName(), name));
                    externalParamProfileLogbooks.add(
                        new EventDiffDto(ExternalParamProfileConverter.NAME_KEY, oldDto.getName(), name));
                    break;
                case ENABLED:
                    Boolean enabled = (Boolean) entry.getValue();
                    profileDto.setEnabled(enabled);
                    logbooks.add(new EventDiffDto(ProfileConverter.ENABLED_KEY, oldDto.isEnabled(), enabled));
                    externalParamProfileLogbooks.add(
                        new EventDiffDto(ExternalParamProfileConverter.ENABLED_KEY, oldDto.isEnabled(), enabled));
                    break;
                default:
            }
        }

        if (partialDto.get(DESCRIPTION) != null || partialDto.get(NAME) != null || partialDto.get(ENABLED) != null) {
            profileInternalService.update(profileDto);
            iamLogbookService.updateProfileEvent(profileConverter.convertDtoToEntity(profileDto), logbooks);
        }
    }

    public boolean checkExist(final String criteria) {
        LOGGER.debug("criteria : {}", criteria);
        return profileInternalService.checkExist(criteria);
    }


    /**
     * Extract main informations from external parameters do
     *
     * @param externalParamProfileDto
     */
    public static void extractFieldsFromExternalParameters(ExternalParamProfileDto externalParamProfileDto) {
        if (externalParamProfileDto != null && externalParamProfileDto.getParameters() != null &&
            externalParamProfileDto.getParameters().length > 0) {
            for (ParameterDto parameterDto : externalParamProfileDto.getParameters()) {
                if (parameterDto.getKey().equals(PARAM_BULK_OPERATIONS_THRESHOLD_NAME)) {
                    try {
                        externalParamProfileDto.setBulkOperationsThreshold(Long.valueOf(parameterDto.getValue()));
                        externalParamProfileDto.setUsePlatformThreshold(false);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException(
                            "The field bulkOperationsThreshold parameter contains wrong number value");
                    }
                } else if (parameterDto.getKey().equals(PARAM_ACCESS_CONTRACT_NAME)) {
                    externalParamProfileDto.setAccessContract(parameterDto.getValue());
                }
            }
        }
    }
}
