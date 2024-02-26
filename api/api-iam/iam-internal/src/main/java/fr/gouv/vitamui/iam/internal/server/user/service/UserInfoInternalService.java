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
package fr.gouv.vitamui.iam.internal.server.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserInfoConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserInfoRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.UserInfo;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


@Getter
@Setter
public class UserInfoInternalService extends VitamUICrudService<UserInfoDto, UserInfo> {


    private UserInfoRepository userInfoRepository;

    private InternalSecurityService internalSecurityService;

    private final UserInfoConverter userInfoConverter;

    private final IamLogbookService iamLogbookService;

    private LogbookService logbookService;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserInfoInternalService.class);

    @Autowired
    public UserInfoInternalService(final SequenceGeneratorService sequenceGeneratorService,
        final UserInfoRepository userInfoRepository,
        final InternalSecurityService internalSecurityService, final UserInfoConverter userInfoConverter,
        final IamLogbookService iamLogbookService, final LogbookService logbookService) {
        super(sequenceGeneratorService);
        this.userInfoRepository = userInfoRepository;
        this.internalSecurityService = internalSecurityService;
        this.userInfoConverter = userInfoConverter;
        this.iamLogbookService = iamLogbookService;
        this.logbookService = logbookService;
    }


    public UserInfoDto getMe() {
        final AuthUserDto user = internalSecurityService.getUser();
        final String userInfoId = user.getUserInfoId();
        if (StringUtils.isBlank(userInfoId)) {
            throw new ApplicationServerException("user must have user information id ", user.getId());
        }
        final Optional<UserInfo> userInfoOptional = userInfoRepository.findById(userInfoId);
        final UserInfo userInfo =
            userInfoOptional.orElseThrow(() -> new ApplicationServerException("user info not found ", userInfoId));
        return userInfoConverter.convertEntityToDto(userInfo);
    }

    @Override
    @Transactional
    public UserInfoDto patch(final Map<String, Object> partialDto) {
        LOGGER.info("Patch {} with {}", getObjectName(), partialDto);
        return super.patch(partialDto);
    }


    @Override
    protected void processPatch(final UserInfo userInfo, final Map<String, Object> partialDto) {
        final Collection<EventDiffDto> logbooks = new ArrayList<>();
        for (final Map.Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case "id":
                    break;
                case "language":
                    logbooks.add(
                        new EventDiffDto(UserInfoConverter.LANGUAGE_KEY, userInfo.getLanguage(), entry.getValue()));
                    userInfo.setLanguage(CastUtils.toString(entry.getValue()));
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unable to patch group " + userInfo.getId() + ": key " + entry.getKey() + " is not allowed");
            }
        }
        iamLogbookService.updateUserInfoEvent(userInfo, logbooks);
    }

    @Override
    @Transactional
    public UserInfoDto create(final UserInfoDto userInfoDto) {
        final UserInfoDto createdUserInfoDto = super.create(userInfoDto);
        iamLogbookService.createUserInfoEvent(createdUserInfoDto);
        return createdUserInfoDto;
    }

    @Override
    protected Class<UserInfo> getEntityClass() {
        return UserInfo.class;
    }

    @Override
    protected UserInfoRepository getRepository() {
        return userInfoRepository;
    }

    @Override
    protected Converter<UserInfoDto, UserInfo> getConverter() {
        return userInfoConverter;
    }

    public JsonNode findHistoryById(final String id) throws VitamClientException {
        LOGGER.debug("findHistoryById for id " + id);
        final Integer tenantIdentifier = internalSecurityService.getTenantIdentifier();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier)
            .setAccessContract(internalSecurityService.getTenant(tenantIdentifier).getAccessContractLogbookIdentifier())
            .setApplicationSessionId(internalSecurityService.getApplicationId());

        final Optional<UserInfo> userInfo = getRepository().findById(id);
        userInfo.orElseThrow(() -> new NotFoundException(String.format("No user information found with id : %s", id)));
        return logbookService.findEventsByIdentifierAndCollectionNames(userInfo.get().getIdentifier(),
            MongoDbCollections.USER_INFOS, vitamContext).toJsonNode();
    }

    @Override
    protected void beforeCreate(final UserInfoDto dto) {
        final String message = "Unable to create user info " + dto.getId();

        super.checkIdentifier(dto.getIdentifier(), message);

        dto.setIdentifier(getNextSequenceId(SequencesConstants.USER_INFOS_IDENTIFIER));
    }

    private UserInfo find(final String id, final String message) {
        Assert.isTrue(StringUtils.isNotEmpty(id), message + ": no id");

        return getRepository().findById(id)
            .orElseThrow(() -> new IllegalArgumentException(message + ": no user info found for id " + id));
    }
}
