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

package fr.gouv.vitamui.iam.server.profile.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.mongo.utils.MongoUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.HistoryEventDto;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.common.ApiIamConstants;
import fr.gouv.vitamui.iam.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.group.domain.Group;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.server.security.AbstractResourceClientService;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The service to read, create, update and delete the profiles.
 */
@Getter
@Setter
public class ProfileService extends AbstractResourceClientService<ProfileDto, Profile> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final CustomerRepository customerRepository;
    private final GroupRepository groupRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final IamLogbookService iamLogbookService;
    private final ProfileConverter profileConverter;
    private final LogbookService logbookService;
    private final CustomerInitConfig customerInitConfig;

    @Autowired
    public ProfileService(
        final SequenceGeneratorService sequenceGeneratorService,
        final ProfileRepository profileRepository,
        final CustomerRepository customerRepository,
        final GroupRepository groupRepository,
        final TenantRepository tenantRepository,
        final UserRepository userRepository,
        final SecurityService securityService,
        final IamLogbookService iamLogbookService,
        final ProfileConverter profileConverter,
        final LogbookService logbookService,
        final CustomerInitConfig customerInitConfig
    ) {
        super(sequenceGeneratorService, securityService);
        this.profileRepository = profileRepository;
        this.customerRepository = customerRepository;
        this.groupRepository = groupRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.iamLogbookService = iamLogbookService;
        this.profileConverter = profileConverter;
        this.logbookService = logbookService;
        this.customerInitConfig = customerInitConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadExtraInformation(final ProfileDto dto, final Optional<String> optEmbedded) {
        if (optEmbedded.isPresent()) {
            final String embedded = optEmbedded.get();
            if (EmbeddedOptions.ALL.toString().equalsIgnoreCase(embedded)) {
                final Tenant tenant = Optional.ofNullable(
                    tenantRepository.findByIdentifier(dto.getTenantIdentifier())
                ).orElseThrow(
                    () -> new NotFoundException("Unable to find the following tenant: " + dto.getTenantIdentifier())
                );

                dto.setTenantName(tenant.getName());

                final Collection<Group> groups = getGroupsByProfileIds(dto.getId());
                List<String> groupIds = groups.stream().map(Group::getId).collect(Collectors.toList());
                long usersCount = userRepository.countByGroupIdIn(groupIds);
                dto.setUsersCount(usersCount);

                dto.setGroupsCount((long) groups.size());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeCreate(final ProfileDto dto) {
        final String message = "Unable to create profile " + dto.getName();

        checkSetReadonly(dto.isReadonly(), message);
        checkCustomer(dto.getCustomerId(), message);
        checkName(dto.getName(), dto.getTenantIdentifier(), dto.getLevel(), dto.getApplicationName(), message);
        checkLevel(dto.getLevel(), message);
        checkTenant(dto.getTenantIdentifier(), message);
        checkRoles(dto.getRoles(), dto.getLevel(), message);
        super.checkIdentifier(dto.getIdentifier(), message);

        dto.setId(generateSuperId());
        dto.setIdentifier(getNextSequenceId(SequencesConstants.PROFILE_IDENTIFIER));
    }

    @Override
    @Transactional
    public ProfileDto create(final ProfileDto dto) {
        final ProfileDto createdProfile = super.create(dto);
        if (dto != null && createdProfile != null) {
            createdProfile.setExternalParamId(dto.getExternalParamId());
            createdProfile.setExternalParamIdentifier(dto.getExternalParamIdentifier());
        }
        iamLogbookService.createProfileEvent(createdProfile);
        return createdProfile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeUpdate(final ProfileDto dto) {
        // this method is not implemented and should not be used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Profile beforePatch(final Map<String, Object> partialDto) {
        final String id = CastUtils.toString(partialDto.get("id"));
        final String message = "Unable to patch profile " + id;
        Assert.isTrue(
            !checkMapContainsOnlyFieldsUnmodifiable(
                partialDto,
                Arrays.asList("id", "readonly", "identifier", "customerId", "applicationName", "tenantIdentifier")
            ),
            message
        );

        final ProfileDto profileDto = getOne(id, Optional.empty(), Optional.empty());
        final String customerId = CastUtils.toString(partialDto.get("customerId"));
        final Integer tenantIdentifier = CastUtils.toInteger(partialDto.get("tenantIdentifier"));
        final Profile profile = find(id, customerId, tenantIdentifier, message);

        checkCustomer(profile.getCustomerId(), message);
        checkLevel(profile.getLevel(), message);
        checkIsReadonly(profile.isReadonly(), message);

        String level = CastUtils.toString(partialDto.get("level"));
        if (level != null) {
            checkLevel(level, message);
            checkModifyLevel(profile, level, message);
        }

        final Boolean readonly = CastUtils.toBoolean(partialDto.get("readonly"));
        if (readonly != null) {
            checkSetReadonly(readonly, message);
        }

        final List<Map<String, Object>> roleEntries = CastUtils.toList(partialDto.get("roles"));
        if (roleEntries != null) {
            final String checkedLevel = StringUtils.isEmpty(level) ? profileDto.getLevel() : level;
            final List<Role> roles = roleEntries.stream().map(this::convertToRole).collect(Collectors.toList());
            checkRoles(roles, checkedLevel, message);
        }

        String name = CastUtils.toString(partialDto.get("name"));
        if (name != null || level != null) {
            name = name == null ? profile.getName() : name;
            level = level == null ? profile.getLevel() : level;
            checkName(name, profile.getTenantIdentifier(), level, profile.getApplicationName(), message);
        }

        final Boolean enabled = CastUtils.toBoolean(partialDto.get("enabled"));
        if (enabled != null) {
            checkEnabled(profile.getId(), enabled, message);
        }

        return profile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processPatch(final Profile profile, final Map<String, Object> partialDto) {
        final Collection<EventDiffDto> logbooks = new ArrayList<>();
        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case "id":
                case "customerId":
                case "readonly":
                case "applicationName":
                case "tenantIdentifier":
                case "identifier":
                    break;
                case "name":
                    logbooks.add(new EventDiffDto(ProfileConverter.NAME_KEY, profile.getName(), entry.getValue()));
                    profile.setName(CastUtils.toString(entry.getValue()));
                    break;
                case "description":
                    logbooks.add(
                        new EventDiffDto(ProfileConverter.DESCRIPTION_KEY, profile.getDescription(), entry.getValue())
                    );
                    profile.setDescription(CastUtils.toString(entry.getValue()));
                    break;
                case "enabled":
                    logbooks.add(new EventDiffDto(ProfileConverter.ENABLED_KEY, profile.isEnabled(), entry.getValue()));
                    profile.setEnabled(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "level":
                    logbooks.add(
                        new EventDiffDto(ProfileConverter.LEVEL_KEY, profile.getDescription(), entry.getValue())
                    );
                    profile.setLevel(CastUtils.toString(entry.getValue()));
                    break;
                case "roles":
                    final List<Map<String, Object>> roleEntries = CastUtils.toList(entry.getValue());
                    final List<Role> roles = roleEntries.stream().map(this::convertToRole).collect(Collectors.toList());
                    logbooks.add(
                        new EventDiffDto(
                            ProfileConverter.ROLES_KEY,
                            profileConverter.convertRoleToLogbook(profile.getRoles()),
                            profileConverter.convertRoleToLogbook(roles)
                        )
                    );
                    profile.setRoles(roles);
                    break;
                case "externalParamId":
                    profile.setExternalParamId(CastUtils.toString(entry.getValue()));
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unable to patch profile " + profile.getId() + ": key " + entry.getKey() + " is not allowed"
                    );
            }
        }
        iamLogbookService.updateProfileEvent(profile, logbooks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProfileDto patch(final Map<String, Object> partialDto) {
        LOGGER.info("Patch {} with {}", getObjectName(), partialDto);
        final ProfileDto profileDto = super.patch(partialDto);
        loadExtraInformation(profileDto, Optional.of(EmbeddedOptions.ALL.toString()));
        return profileDto;
    }

    protected Role convertToRole(final Map<String, Object> patchEntry) {
        if (!patchEntry.containsKey("name")) {
            throw new IllegalArgumentException("No property 'name' has been found for the role : " + patchEntry);
        }
        final Role role = new Role();
        role.setName(patchEntry.get("name").toString());
        return role;
    }

    private Profile find(
        final String id,
        final String customerId,
        final Integer tenantIdentifier,
        final String message
    ) {
        Assert.isTrue(StringUtils.isNotEmpty(id), message + ": no id");
        Assert.isTrue(StringUtils.isNotEmpty(customerId), message + ": no customerId");
        Assert.isTrue(tenantIdentifier != null, message + ": no tenant Identifier");
        Assert.isTrue(
            StringUtils.equals(customerId, getSecurityService().getCustomerId()),
            message + ": customerId " + customerId + " is not allowed"
        );

        return getRepository()
            .findByIdAndCustomerIdAndTenantIdentifier(id, customerId, tenantIdentifier)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        message +
                        ": no profile found for id " +
                        id +
                        " - customerId : " +
                        customerId +
                        " - tenantIdentifier : " +
                        tenantIdentifier
                    )
            );
    }

    private void checkEnabled(final String profileId, final boolean dtoEnable, final String message) {
        if (!dtoEnable) {
            final long count = countGroupsByProfileIds(profileId);
            Assert.isTrue(count == 0, message + ": the profile is referenced by " + count + " groups");
        }
    }

    private void checkModifyLevel(final Profile profile, final String dtoLevel, final String message) {
        if (!StringUtils.equals(profile.getLevel(), dtoLevel)) {
            final long count = countGroupsByProfileIds(profile.getId());
            Assert.isTrue(count == 0, message + ": the profile is referenced by " + count + " groups");
        }
    }

    public void checkLevel(final String level, final String message) {
        Assert.isTrue(
            Pattern.matches(ApiIamConstants.LEVEL_VALID_REGEXP, level),
            "level : " + level + " format is not allowed"
        );
        Assert.isTrue(securityService.isLevelAllowed(level), message + ": level " + level + " is not allowed");
    }

    private void checkSetReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly must be set to false");
    }

    private void checkIsReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly profile");
    }

    private void checkCustomer(final String customerId, final String message) {
        Assert.isTrue(
            StringUtils.equals(customerId, getSecurityService().getCustomerId()),
            message + ": customerId " + customerId + " is not allowed"
        );

        final Optional<Customer> customer = customerRepository.findById(customerId);
        Assert.isTrue(customer.isPresent(), message + ": customer does not exist");

        Assert.isTrue(customer.get().isEnabled(), message + ": customer must be enabled");
    }

    private void checkName(
        final String name,
        final Integer tenantIdentifier,
        final String level,
        final String appName,
        final String message
    ) {
        LOGGER.debug("name : {} , level : {}", name, level);
        final Criteria criteria = MongoUtils.buildCriteriaEquals("name", name, true)
            .and("level")
            .is(level)
            .and("applicationName")
            .is(appName)
            .and("tenantIdentifier")
            .is(tenantIdentifier);
        Assert.isTrue(!getRepository().exists(criteria), message + ": profile already exists");
    }

    private void checkTenant(final Integer tenantIdentifier, final String message) {
        final Tenant tenant = tenantRepository.findByIdentifier(tenantIdentifier);
        Assert.isTrue(tenant != null, message + ": The tenant " + tenantIdentifier + " does not exist");
    }

    /**
     * This method check for the creation, update, patch of a profile, if the user can managed roles contains
     * by the profile.
     *
     * @param roles roles to check if user can managed roles
     * @param level user's level
     * @param message
     */
    private void checkRoles(final List<Role> roles, final String level, final String message) {
        Assert.isTrue(CollectionUtils.isNotEmpty(roles), message + ": no roles");
        final Integer tenantIdentifier = securityService.getTenantIdentifier();
        final List<Role> allRoles = customerInitConfig.getAllRoles();
        final List<Role> myRoles = SecurityService.getRoles(securityService.getUser(), tenantIdentifier);
        final List<Role> subRoles = getSubRoles(level, tenantIdentifier);
        final List<Role> adminVitamUIRoles = ServicesData.getAdminVitamUIRoles();

        for (final Role role : roles) {
            Assert.isTrue(allRoles.contains(role), message + ": role " + role.getName() + " does not exist");

            final boolean allow =
                myRoles.contains(role) ||
                subRoles.contains(role) ||
                (securityService.userIsRootLevel() && !adminVitamUIRoles.contains(role));

            Assert.isTrue(allow, message + ": role " + role + " is not allowed");
        }
    }

    private long countGroupsByProfileIds(final String profileId) {
        return groupRepository.countByProfileIds(profileId);
    }

    private Collection<Group> getGroupsByProfileIds(final String profileId) {
        return groupRepository.findByProfileIds(profileId);
    }

    private List<Role> getSubRoles(final String level, final Integer tenantIdentifier) {
        Assert.isTrue(tenantIdentifier != null, "Unable to getSubRoles: tenantIdentifier must be not null");

        final ArrayList<CriteriaDefinition> criterias = new ArrayList<>();
        criterias.add(Criteria.where("tenantIdentifier").is(tenantIdentifier));
        criterias.add(Criteria.where("enabled").is(true));
        if (!level.isEmpty()) {
            criterias.add(Criteria.where("level").regex("^" + level + "\\..+$"));
        }
        return getRepository()
            .findAll(criterias)
            .stream()
            .flatMap(p -> p.getRoles().stream())
            .collect(Collectors.toList());
    }

    public List<String> getSubLevels(final String level, final String customerId) {
        final ArrayList<CriteriaDefinition> criterias = new ArrayList<>();
        criterias.add(Criteria.where("customerId").in(customerId));
        criterias.add(Criteria.where("enabled").is(true));
        if (!level.isEmpty()) {
            criterias.add(Criteria.where("level").regex("^" + level + "\\..+$"));
        }
        return getRepository().findAll(criterias).stream().map(Profile::getLevel).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Profile> getEntityClass() {
        return Profile.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ProfileRepository getRepository() {
        return profileRepository;
    }

    @Override
    protected Converter<ProfileDto, Profile> getConverter() {
        return profileConverter;
    }

    public List<HistoryEventDto> findHistoryById(final String id) throws VitamClientException {
        final Integer tenantIdentifier = securityService.getTenantIdentifier();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier)
            .setAccessContract(securityService.getTenant(tenantIdentifier).getAccessContractLogbookIdentifier())
            .setApplicationSessionId(securityService.getApplicationId());

        LOGGER.debug(
            "Find History Access Contract By ID {}, EvIdAppSession : {}",
            id,
            vitamContext.getApplicationSessionId()
        );
        final Optional<Profile> profile = getRepository().findById(id);
        profile.orElseThrow(() -> new NotFoundException("No user found with id : %s".formatted(id)));
        LOGGER.debug(
            "findHistoryById : events.obId {}, events.obIdReq {}, VitamContext {}",
            profile.get().getIdentifier(),
            MongoDbCollections.PROFILES,
            vitamContext
        );
        return logbookService.findEventsByIdentifierAndCollectionNames(
            profile.get().getIdentifier(),
            MongoDbCollections.PROFILES,
            vitamContext,
            List.of(EventType.EXT_VITAMUI_CREATE_PROFILE.name(), EventType.EXT_VITAMUI_UPDATE_PROFILE.name())
        );
    }

    /**
     * Get levels matching the given criteria.
     *
     * @param criteriaJsonString criteria as json string
     * @return Matching levels
     */
    public List<String> getLevels(final Optional<String> criteriaJsonString) {
        final Document document = groupFields(criteriaJsonString, CommonConstants.LEVEL_ATTRIBUTE);
        LOGGER.debug("getLevels : {}", document);
        if (document == null) {
            return new ArrayList<>();
        }
        return (List<String>) document.get(CommonConstants.LEVEL_ATTRIBUTE);
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return List.of(
            "id",
            "applicationName",
            "name",
            "enabled",
            "description",
            LEVEL_KEY,
            TENANT_IDENTIFIER_KEY,
            CUSTOMER_ID_KEY,
            "identifier",
            EXTERNAL_PARAM_ID_KEY
        );
    }

    // Needed for tests...
    @Override
    public void addDataAccessRestrictions(Collection<CriteriaDefinition> criteria) {
        super.addDataAccessRestrictions(criteria);
    }

    @Override
    protected void addRestriction(final String key, final QueryDto query) {
        switch (key) {
            case LEVEL_KEY:
                addLevelRestriction(query);
                break;
            default:
                throw new NotImplementedException("Restriction not defined for key: " + key);
        }
    }

    /**
     * If the user is not an admin, he can only see profiles with a sub LEVEL and profiles who belongs to his group
     * Example : Users { id: 10, level: ROOT} can see only profiles at level : ROOT..* and profiles who belongs to his group
     *
     * @param query
     */
    private void addLevelRestriction(final QueryDto query) {
        final QueryDto levelQuery = new QueryDto();
        levelQuery.setQueryOperator(QueryOperator.OR);
        levelQuery.addCriterion("level", securityService.getLevel() + ".", CriterionOperator.STARTWITH);
        levelQuery.addCriterion(
            "id",
            securityService.getUser().getProfileGroup().getProfileIds(),
            CriterionOperator.IN
        );
        query.addQuery(levelQuery);
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        final Collection<String> restrictedKeys = new ArrayList<>(
            Arrays.asList(CUSTOMER_ID_KEY, LEVEL_KEY, TENANT_IDENTIFIER_KEY)
        );
        if (securityService.hasRole(ServicesData.ROLE_GET_PROFILES_ALL_TENANTS)) {
            restrictedKeys.remove(TENANT_IDENTIFIER_KEY);
        }
        if (securityService.userIsRootLevel()) {
            restrictedKeys.remove(LEVEL_KEY);
        }
        return restrictedKeys;
    }

    @Override
    protected String getVersionApiCriteria() {
        return CRITERIA_VERSION_V2;
    }
}
