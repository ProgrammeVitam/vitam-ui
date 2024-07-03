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
package fr.gouv.vitamui.iam.internal.server.logbook.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ExternalParamProfileDto;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.logbook.service.EventService;
import fr.gouv.vitamui.commons.logbook.util.LogbookUtils;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.config.Converters;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.internal.server.user.domain.ConnectionHistory;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.domain.UserInfo;
import fr.gouv.vitamui.iam.internal.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * Class.
 *
 *
 */
public class IamLogbookService {

    private final EventService eventService;

    private final InternalSecurityService internalSecurityService;

    private static final Logger LOGGER = LoggerFactory.getLogger(IamLogbookService.class);

    private final Converters converters;

    private final TenantRepository tenantRepository;

    private final ConnectionHistoryService connectionHistoryService;

    @Value("#{new Boolean('${user.connection.tracing.enabled}')}")
    private boolean isTraceConnectionActive;

    public IamLogbookService(
        final EventService logbookService,
        final InternalSecurityService internalSecurityService,
        final Converters converters,
        final TenantRepository tenantRepository,
        final ConnectionHistoryService connectionHistoryService
    ) {
        eventService = logbookService;
        this.internalSecurityService = internalSecurityService;
        this.converters = converters;
        this.tenantRepository = tenantRepository;
        this.connectionHistoryService = connectionHistoryService;
    }

    /**
     *
     * @param sourceEvent
     */
    @Transactional
    public void createCustomerEvent(final CustomerDto sourceEvent) {
        LOGGER.debug("Create Customer {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.CUSTOMERS,
            EventType.EXT_VITAMUI_CREATE_CUSTOMER,
            converters.getCustomerConverter().convertToLogbook(sourceEvent)
        );
    }

    @Transactional
    public void subrogation(final SubrogationDto sourceEvent, final EventType eventType) {
        String msg = null;
        switch (eventType) {
            case EXT_VITAMUI_DECLINE_SURROGATE:
                msg = "Surrogate decline subrogation";
                break;
            case EXT_VITAMUI_STOP_SURROGATE:
                msg = "SuperUser stop subrogation";
                break;
            case EXT_VITAMUI_LOGOUT_SURROGATE:
                msg = "SuperUser logout from subrogation";
                break;
            case EXT_VITAMUI_START_SURROGATE_GENERIC:
                msg = "Generic's user subrogation started";
                break;
            case EXT_VITAMUI_START_SURROGATE_USER:
                msg = "User subrogation started";
                break;
            default:
                break;
        }
        LOGGER.debug(msg + "{}", sourceEvent.toString());
        create(
            getProofTenantIdentifierByCustomerId(sourceEvent.getSuperUserCustomerId()),
            sourceEvent.getId(),
            MongoDbCollections.SUBROGATIONS,
            eventType,
            converters.getSubrogationConverter().convertToLogbook(sourceEvent)
        );
    }

    @Transactional
    public void subrogation(final Subrogation sourceEvent, final EventType eventType) {
        this.subrogation(converters.getSubrogationConverter().convertEntityToDto(sourceEvent), eventType);
    }

    /**
     *
     * @param sourceEvent
     */
    @Transactional
    public void createProfileEvent(final ProfileDto sourceEvent) {
        LOGGER.debug("Create Profile {}", sourceEvent.toString());
        create(
            sourceEvent.getTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.PROFILES,
            EventType.EXT_VITAMUI_CREATE_PROFILE,
            converters.getProfileConverter().convertToLogbook(sourceEvent)
        );
    }

    /**
     *
     * @param sourceEvent
     */
    @Transactional
    public void createIdpEvent(final IdentityProviderDto sourceEvent) {
        LOGGER.debug("Create Provider {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.PROVIDERS,
            EventType.EXT_VITAMUI_CREATE_IDP,
            converters.getIdpConverter().convertToLogbook(sourceEvent)
        );
    }

    /**
     *
     * @param sourceEvent
     */
    public void createIdpEvent(final IdentityProvider sourceEvent) {
        createIdpEvent(converters.getIdpConverter().convertEntityToDto(sourceEvent));
    }

    /**
     *
     * @param sourceEvent
     */
    public void createIdpEventInitCustomer(final IdentityProvider sourceEvent) {
        LOGGER.debug("Create Provider {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.PROVIDERS,
            EventType.EXT_VITAMUI_CREATE_IDP,
            converters.getIdpConverter().convertToLogbook(converters.getIdpConverter().convertEntityToDto(sourceEvent))
        );
    }

    /**
     *
     * @param sourceEvent
     */
    @Transactional
    public void createProfileEvent(final Profile sourceEvent) {
        createProfileEvent(converters.getProfileConverter().convertEntityToDto(sourceEvent));
    }

    /**
     *
     * @param sourceEvent
     */
    @Transactional
    public void createUserEvent(final UserDto sourceEvent) {
        LOGGER.debug("Create User {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.USERS,
            EventType.EXT_VITAMUI_CREATE_USER,
            converters.getUserConverter().convertToLogbook(sourceEvent)
        );
    }

    /**
     *
     * @param sourceEvent
     */
    public void createUserInfoEvent(final UserInfoDto sourceEvent) {
        LOGGER.info("Create User Info {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.USER_INFOS,
            EventType.EXT_VITAMUI_CREATE_USER_INFO,
            converters.getUserInfoConverter().convertToLogbook(sourceEvent)
        );
    }

    /**
     *
     * @param sourceEvent
     */
    @Transactional
    public void createOwnerEvent(final OwnerDto sourceEvent) {
        LOGGER.debug("Create Owner {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.OWNERS,
            EventType.EXT_VITAMUI_CREATE_OWNER,
            converters.getOwnerConverter().convertToLogbook(sourceEvent)
        );
    }

    public void createOwnerEventInitCustomer(final OwnerDto sourceEvent) {
        LOGGER.debug("Create Owner {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.OWNERS,
            EventType.EXT_VITAMUI_CREATE_OWNER,
            converters.getOwnerConverter().convertToLogbook(sourceEvent)
        );
    }

    /**
     *
     * @param sourceEvent
     */
    @Transactional
    public void createGroupEvent(final GroupDto sourceEvent) {
        LOGGER.debug("Create Group {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.GROUPS,
            EventType.EXT_VITAMUI_CREATE_GROUP,
            converters.getGroupConverter().convertToLogbook(sourceEvent)
        );
    }

    public void createGroupEvent(final Group group) {
        createGroupEvent(converters.getGroupConverter().convertEntityToDto(group));
    }

    /**
     *
     * @param sourceEvent
     */
    @Transactional
    public void createTenantEvent(final TenantDto sourceEvent) {
        LOGGER.debug("Create Tenant {}", sourceEvent.toString());

        create(
            getCurrentProofTenantIdentifier(),
            String.valueOf(sourceEvent.getIdentifier()),
            MongoDbCollections.TENANTS,
            EventType.EXT_VITAMUI_CREATE_TENANT,
            converters.getTenantConverter().convertToLogbook(sourceEvent)
        );
    }

    public void createTenantEventInitCustomer(final Tenant sourceEvent) {
        LOGGER.debug("Create Tenant {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            String.valueOf(sourceEvent.getIdentifier()),
            MongoDbCollections.TENANTS,
            EventType.EXT_VITAMUI_CREATE_TENANT,
            converters
                .getTenantConverter()
                .convertToLogbook(converters.getTenantConverter().convertEntityToDto(sourceEvent))
        );
    }

    public void createTenantEvent(final Tenant sourceEvent) {
        createTenantEvent(converters.getTenantConverter().convertEntityToDto(sourceEvent));
    }

    /**
     *
     * @param profile
     * @param logbooks
     */
    @Transactional
    public void updateProfileEvent(final Profile profile, final Collection<EventDiffDto> logbooks) {
        LOGGER.debug("Update Profile {}", profile.toString());
        update(
            profile.getTenantIdentifier(),
            profile.getIdentifier(),
            MongoDbCollections.PROFILES,
            EventType.EXT_VITAMUI_UPDATE_PROFILE,
            logbooks
        );
    }

    /**
     *
     * @param group
     * @param logbooks
     */
    @Transactional
    public void updateGroupEvent(final Group group, final Collection<EventDiffDto> logbooks) {
        LOGGER.debug("Update Group {}", group.toString());
        update(
            getCurrentProofTenantIdentifier(),
            group.getIdentifier(),
            MongoDbCollections.GROUPS,
            EventType.EXT_VITAMUI_UPDATE_GROUP,
            logbooks
        );
    }

    /**
     *
     * @param user
     * @param logbooks
     */
    @Transactional
    public void updateUserEvent(final User user, final Collection<EventDiffDto> logbooks) {
        LOGGER.debug("Update User {}", user.toString());
        update(
            getCurrentProofTenantIdentifier(),
            user.getIdentifier(),
            MongoDbCollections.USERS,
            EventType.EXT_VITAMUI_UPDATE_USER,
            logbooks
        );
    }

    /**
     *
     * @param userInfo
     * @param logbooks
     */
    public void updateUserInfoEvent(final UserInfo userInfo, final Collection<EventDiffDto> logbooks) {
        LOGGER.info("Update User Info {}", userInfo.toString());
        update(
            getCurrentProofTenantIdentifier(),
            userInfo.getIdentifier(),
            MongoDbCollections.USER_INFOS,
            EventType.EXT_VITAMUI_UPDATE_USER_INFO,
            logbooks
        );
    }

    /**
     *
     * @param tenant
     * @param logbooks
     */
    @Transactional
    public void updateTenantEvent(final Tenant tenant, final Collection<EventDiffDto> logbooks) {
        LOGGER.debug("Update tenant {}", tenant.toString());
        update(
            getCurrentProofTenantIdentifier(),
            String.valueOf(tenant.getIdentifier()),
            MongoDbCollections.TENANTS,
            EventType.EXT_VITAMUI_UPDATE_TENANT,
            logbooks
        );
    }

    /**
     *
     * @param owner
     * @param logbooks
     */
    @Transactional
    public void updateOwnerEvent(final Owner owner, final Collection<EventDiffDto> logbooks) {
        LOGGER.debug("Update Owner {}", owner.toString());
        update(
            getCurrentProofTenantIdentifier(),
            owner.getIdentifier(),
            MongoDbCollections.OWNERS,
            EventType.EXT_VITAMUI_UPDATE_OWNER,
            logbooks
        );
    }

    /**
     *
     * @param idp
     * @param logbooks
     */
    @Transactional
    public void updateIdpEvent(final IdentityProvider idp, final Collection<EventDiffDto> logbooks) {
        LOGGER.debug("Update Provider {}", idp.toString());
        update(
            getCurrentProofTenantIdentifier(),
            idp.getIdentifier(),
            MongoDbCollections.PROVIDERS,
            EventType.EXT_VITAMUI_UPDATE_IDP,
            logbooks
        );
    }

    /**
     *
     * @param customer
     * @param logbooks
     */
    @Transactional
    public void updateCustomerEvent(final Customer customer, final Collection<EventDiffDto> logbooks) {
        LOGGER.debug("Update Customer {}", customer.toString());
        update(
            getCurrentProofTenantIdentifier(),
            customer.getIdentifier(),
            MongoDbCollections.CUSTOMERS,
            EventType.EXT_VITAMUI_UPDATE_CUSTOMER,
            logbooks
        );
    }

    /**
     * Track the password creation event.
     *
     * @param user the user for whom the password is changed
     */
    public void createPasswordEvent(final User user) {
        LOGGER.debug("create password for user: {}", user.toString());
        create(
            getProofTenantIdentifierByCustomerId(user.getCustomerId()),
            user.getIdentifier(),
            MongoDbCollections.USERS,
            EventType.EXT_VITAMUI_PASSWORD_INIT,
            ""
        );
    }

    /**
     * Track the password update event.
     *
     * @param user the user for whom the password is updated
     */
    public void updatePasswordEvent(final User user) {
        LOGGER.debug("update password for user: {}", user.toString());
        create(
            getProofTenantIdentifierByCustomerId(user.getCustomerId()),
            user.getIdentifier(),
            MongoDbCollections.USERS,
            EventType.EXT_VITAMUI_PASSWORD_CHANGE,
            ""
        );
    }

    /**
     * Track the password revocation event (because of a user reactivation).
     *
     * @param user the user identifier for whom the password is revoked
     * @param superUser the super user
     */
    @Transactional
    public void revokePasswordEvent(final User user, final String superUser) {
        LOGGER.debug("revoke password for user: {} / superUser: {}", user, superUser);
        final Map<String, String> logbookData = new HashMap<>();
        if (superUser != null) {
            logbookData.put("Super utilisateur", superUser);
        }
        final String json = ApiUtils.toJson(logbookData);
        create(
            getProofTenantIdentifierByCustomerId(user.getCustomerId()),
            user.getIdentifier(),
            MongoDbCollections.USERS,
            EventType.EXT_VITAMUI_PASSWORD_REVOCATION,
            json
        );
    }

    @Transactional
    public void revokePasswordEvent(final UserDto dto, final String superUserIdentifier) {
        revokePasswordEvent(converters.getUserConverter().convertDtoToEntity(dto), superUserIdentifier);
    }

    /**
     * Track the user blocked when login.
     *
     * @param user the blocked user
     * @param oldStatus the old user status
     * @param duration of user's blocked
     */
    public void blockUserEvent(final User user, final UserStatusEnum oldStatus, final Duration duration) {
        LOGGER.debug("block user: {} / oldStatus: {}", user.toString(), oldStatus);
        final List<EventDiffDto> updates = Arrays.asList(
            new EventDiffDto("Statut", oldStatus.toString(), user.getStatus().toString())
        );
        final ObjectNode evDetData = LogbookUtils.getEvData(updates);
        evDetData.put(UserConverter.BLOCKED_DURATION, duration.toString());
        create(
            getProofTenantIdentifierByCustomerId(user.getCustomerId()),
            user.getIdentifier(),
            MongoDbCollections.USERS,
            EventType.EXT_VITAMUI_BLOCK_USER,
            evDetData.toString()
        );
    }

    /**
     * Track a login event.
     *
     * @param user the authenticated user
     * @param surrogateIdentifier the surrogate identifier
     * @param ip the user IP
     * @param errorMessage the error message
     */
    public void loginEvent(
        final User user,
        final String surrogateIdentifier,
        final String ip,
        final String errorMessage
    ) {
        LOGGER.debug(
            "Login statut: {} / user: {} - {} / surrogate: {} / IP: {} / errorMessage: {}",
            errorMessage != null ? StatusCode.KO : StatusCode.OK,
            user.getIdentifier(),
            user.getEmail(),
            surrogateIdentifier,
            ip,
            errorMessage
        );
        if (errorMessage == null && isTraceConnectionActive) {
            ConnectionHistory connectionHistory = ConnectionHistory.builder()
                .userId(user.getIdentifier())
                .subrogatedUserId(surrogateIdentifier)
                .connectionDateTime(new Date())
                .build();
            this.connectionHistoryService.saveUserConnection(connectionHistory);
        }
    }

    /**
     *
     * @param identifier
     * @param collectionNames
     * @param eventType
     * @param evData
     */
    @Transactional
    public void create(
        final Integer tenantIdentifier,
        final String identifier,
        final String collectionNames,
        final EventType eventType,
        final String evData
    ) {
        eventService.logCreate(
            internalSecurityService.getHttpContext(),
            getAccessContractLogbookIdentifier(tenantIdentifier),
            tenantIdentifier,
            identifier,
            collectionNames,
            eventType,
            evData
        );
    }

    /**
     *
     * @param identifier
     * @param collectionNames
     * @param eventType
     * @param evData
     */
    @Transactional
    public void update(
        final Integer tenantIdentifier,
        final String identifier,
        final String collectionNames,
        final EventType eventType,
        final Collection<EventDiffDto> evData
    ) {
        eventService.logUpdate(
            internalSecurityService.getHttpContext(),
            getAccessContractLogbookIdentifier(tenantIdentifier),
            tenantIdentifier,
            identifier,
            collectionNames,
            eventType,
            evData
        );
    }

    private String getAccessContractLogbookIdentifier(final Integer tenantIdentifier)
        throws ApplicationServerException {
        final Optional<Tenant> tenant = tenantRepository.findOne(
            Query.query(Criteria.where("identifier").is(tenantIdentifier))
        );
        tenant.orElseThrow(() -> new NotFoundException("No tenant found with identifier : " + tenantIdentifier));
        return StringUtils.defaultIfBlank(
            tenant.get().getAccessContractLogbookIdentifier(),
            CommonConstants.DEFAULT_LOGBOOK_ACCESS_CONTRACT_IDENTIFIER
        );
    }

    private Integer getCurrentProofTenantIdentifier() {
        return internalSecurityService.getProofTenantIdentifier();
    }

    private Integer getProofTenantIdentifierByCustomerId(final String customerId) {
        return getProofTenantByCustomerId(customerId).getIdentifier();
    }

    public Tenant getProofTenantByCustomerId(final String customerId) {
        final Optional<Tenant> optTenant = tenantRepository.findOne(
            Query.query(Criteria.where("customerId").is(customerId).and("proof").is(true))
        );

        optTenant.orElseThrow(() -> new NotFoundException("No proof tenant found for customerId : " + customerId));
        return optTenant.get();
    }

    /**
     * @param sourceEvent source de l'évènement
     */
    public void createExternalParametersEvent(final ExternalParametersDto sourceEvent) {
        LOGGER.info("Create ExternalParameters {}", sourceEvent.toString());
        create(
            getCurrentProofTenantIdentifier(),
            sourceEvent.getIdentifier(),
            MongoDbCollections.EXTERNAL_PARAMETERS,
            EventType.EXT_VITAMUI_CREATE_EXTERNAL_PARAM,
            converters.getExternalParametersConverter().convertToLogbook(sourceEvent)
        );
    }

    public void updateExternalParametersEvent(
        final ExternalParametersDto externalParametersDto,
        final Collection<EventDiffDto> logbooks
    ) {
        LOGGER.info("Update ExternalParameters {}", externalParametersDto.toString());
        update(
            getCurrentProofTenantIdentifier(),
            externalParametersDto.getIdentifier(),
            MongoDbCollections.EXTERNAL_PARAMETERS,
            EventType.EXT_VITAMUI_UPDATE_EXTERNAL_PARAM,
            logbooks
        );
    }

    /**
     * @param externalParamProfileDto source de l'évènement
     */
    public void createExternalParamProfileEvent(final ExternalParamProfileDto externalParamProfileDto) {
        LOGGER.info("Create ExternalParameter Profile {}", externalParamProfileDto.toString());
        create(
            getCurrentProofTenantIdentifier(),
            externalParamProfileDto.getIdProfile(),
            "externalparamprofile",
            EventType.EXT_VITAMUI_CREATE_EXTERNAL_PARAM_PROFILE,
            converters.getExternalParamProfileConverter().convertToLogbook(externalParamProfileDto)
        );
    }

    /**
     *
     * @param externalParamProfileDto object containing infos for parameterize logbooks infos
     * @param logbooks logbooks
     */
    public void updateExternalParamProfileEvent(
        final ExternalParamProfileDto externalParamProfileDto,
        final Collection<EventDiffDto> logbooks
    ) {
        LOGGER.info("Update Profile {}", externalParamProfileDto.toString());
        update(
            getCurrentProofTenantIdentifier(),
            externalParamProfileDto.getIdProfile(),
            "externalparamprofile",
            EventType.EXT_VITAMUI_UPDATE_EXTERNAL_PARAM_PROFILE,
            logbooks
        );
    }
}
