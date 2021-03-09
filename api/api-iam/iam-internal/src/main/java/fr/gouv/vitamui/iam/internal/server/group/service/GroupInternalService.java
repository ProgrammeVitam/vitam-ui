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
package fr.gouv.vitamui.iam.internal.server.group.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.common.EmbeddedOptions;
import fr.gouv.vitamui.iam.common.utils.IamUtils;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupInternalService extends VitamUICrudService<GroupDto, Group> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(GroupInternalService.class);

    private final GroupRepository groupRepository;

    private final CustomerRepository customerRepository;

    private final ProfileInternalService internalProfileService;

    private final UserRepository userRepository;

    private final InternalSecurityService internalSecurityService;

    private final TenantRepository tenantRepository;

    private final IamLogbookService iamLogbookService;

    private final GroupConverter groupConverter;

    private LogbookService logbookService;

    @Autowired
    public GroupInternalService(final CustomSequenceRepository sequenceRepository, final GroupRepository groupRepository,
            final CustomerRepository customerRepository, final ProfileInternalService internalProfileService, final UserRepository userRepository,
            final InternalSecurityService internalSecurityService, final TenantRepository tenantRepository, final IamLogbookService iamLogbookService,
            final GroupConverter groupConverter, final LogbookService logbookService) {
        super(sequenceRepository);
        this.groupRepository = groupRepository;
        this.customerRepository = customerRepository;
        this.internalProfileService = internalProfileService;
        this.userRepository = userRepository;
        this.internalSecurityService = internalSecurityService;
        this.tenantRepository = tenantRepository;
        this.iamLogbookService = iamLogbookService;
        this.groupConverter = groupConverter;
        this.logbookService = logbookService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupDto> getAll(final Optional<String> criteria, final Optional<String> embedded) {
        return super.getAll(criteria, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaginatedValuesDto<GroupDto> getAllPaginated(final Integer page, final Integer size, final Optional<String> criteriaJsonString,
            final Optional<String> orderBy, final Optional<DirectionDto> direction, final Optional<String> embedded) {
        return super.getAllPaginated(page, size, criteriaJsonString, orderBy, direction, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupDto getOne(final String id, final Optional<String> criteria, final Optional<String> embedded) {
        return super.getOne(id, criteria, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeCreate(final GroupDto dto) {
        final String message = "Unable to create group " + dto.getName();

        checkSetReadonly(dto.isReadonly(), message);
        checkCustomer(dto.getCustomerId(), message);
        checkNameExist(null, dto.getName(), dto.getCustomerId(), message);
        checkLevel(dto.getLevel(), message);
        checkProfiles(dto.getLevel(), dto.getCustomerId(), dto.getProfileIds(), message);
        super.checkIdentifier(dto.getIdentifier(), message);

        dto.setId(generateSuperId());
        dto.setIdentifier(getNextSequenceId(SequencesConstants.GROUP_IDENTIFIER));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Group beforePatch(final Map<String, Object> partialDto) {
        final String id = CastUtils.toString(partialDto.get("id"));
        final String message = "Unable to update group " + id;
        if(getVitamContext() != null) {
            LOGGER.info("Patch Group EvIdAppSession : {} " , getVitamContext().getApplicationSessionId());
        }
        final String customerId = CastUtils.toString(partialDto.get("customerId"));
        final Group group = find(id, customerId, message);

        checkLevel(group.getLevel(), message);
        checkIsReadonly(group.isReadonly(), message);
        Assert.isTrue(!checkMapContainsOnlyFieldsUnmodifiable(partialDto, Arrays.asList("id", "customerId", "readonly", "identifier")), message);
        final String level = CastUtils.toString(partialDto.get("level"));
        if (level != null) {
            checkLevel(level, message);
            checkModifyLevel(group, level, message);
        }

        final Boolean readonly = CastUtils.toBoolean(partialDto.get("readonly"));
        if (readonly != null) {
            checkSetReadonly(readonly, message);
        }

        @SuppressWarnings("unchecked")
        final List<String> profileIds = (List<String>) partialDto.get("profileIds");
        if (profileIds != null) {
            checkProfiles(group.getLevel(), customerId, profileIds, message);
        }

        final String name = CastUtils.toString(partialDto.get("name"));
        if (name != null) {
            checkNameExist(group.getName(), name, customerId, message);
        }

        final Boolean enabled = CastUtils.toBoolean(partialDto.get("enabled"));
        if (enabled != null) {
            checkEnabled(group.getId(), enabled, message);
        }

        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processPatch(final Group group, final Map<String, Object> partialDto) {
        final Collection<EventDiffDto> logbooks = new ArrayList<>();
        if(getVitamContext() != null) {
            LOGGER.info("Patch Group EvIdAppSession : {} " , getVitamContext().getApplicationSessionId());
        }

        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case "id" :
                case "customerId" :
                case "readonly" :
                case "identifier" :
                    break;
                case "name" :
                    logbooks.add(new EventDiffDto(GroupConverter.NAME_KEY, group.getName(), entry.getValue()));
                    group.setName(CastUtils.toString(entry.getValue()));
                    break;
                case "description" :
                    logbooks.add(new EventDiffDto(GroupConverter.DESCRIPTION_KEY, group.getDescription(), entry.getValue()));
                    group.setDescription(CastUtils.toString(entry.getValue()));
                    break;
                case "enabled" :
                    logbooks.add(new EventDiffDto(GroupConverter.ENABLED_KEY, group.isEnabled(), entry.getValue()));
                    group.setEnabled(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "level" :
                    logbooks.add(new EventDiffDto(GroupConverter.LEVEL_KEY, group.getLevel(), entry.getValue()));
                    group.setLevel(CastUtils.toString(entry.getValue()));
                    break;
                case "profileIds" :
                    final List<String> profileIds = CastUtils.toList(entry.getValue());
                    logbooks.add(new EventDiffDto(GroupConverter.PROFILE_IDS_KEY, groupConverter.convertProfileIdsToLogbook(group.getProfileIds()),
                            groupConverter.convertProfileIdsToLogbook(profileIds)));
                    group.setProfileIds(profileIds);
                    break;
                default :
                    throw new IllegalArgumentException("Unable to patch group " + group.getId() + ": key " + entry.getKey() + " is not allowed");
            }
        }
        iamLogbookService.updateGroupEvent(group, logbooks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public GroupDto patch(final Map<String, Object> partialDto) {
        final GroupDto groupDto = super.patch(partialDto);
        loadExtraInformation(groupDto, Optional.of(EmbeddedOptions.ALL.toString()));
        return groupDto;
    }

    private Group find(final String id, final String customerId, final String message) {
        Assert.isTrue(StringUtils.isNotEmpty(id), message + ": no id");
        Assert.isTrue(StringUtils.equals(customerId, getInternalSecurityService().getCustomerId()), message + ": customerId " + customerId + " is not allowed");
        return getRepository().findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new IllegalArgumentException(message + ": no group found for id " + id + " - customerId " + customerId));
    }

    private void checkEnabled(final String groupId, final boolean dtoEnable, final String message) {
        if (!dtoEnable) {
            final long count = userRepository.countByGroupId(groupId);
            Assert.isTrue(count == 0, message + ": the group is referenced by " + count + " users");
        }
    }

    private void checkLevel(final String level, final String message) {
        Assert.isTrue(Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level), "level : " + level + " format is not allowed");
        Assert.isTrue(internalSecurityService.isLevelAllowed(level), message + ": level " + level + " is not allowed");
    }

    private void checkModifyLevel(final Group group, final String dtoLevel, final String message) {
        if (!StringUtils.equals(group.getLevel(), dtoLevel)) {
            Assert.isTrue(CollectionUtils.isEmpty(group.getProfileIds()), message + ": the group contains " + group.getProfileIds().size() + " profiles");

            final long count = userRepository.countByGroupId(group.getId());
            Assert.isTrue(count == 0, message + ": the group is referenced by " + count + " users");
        }
    }

    @Override
    @Transactional
    public GroupDto create(final GroupDto dto) {
        final GroupDto group = super.create(dto);
        iamLogbookService.createGroupEvent(dto);
        return group;
    }

    private void checkProfiles(final String groupLevel, final String customerId, final List<String> dtoProfiles, final String message) {

        final List<ProfileDto> profiles = internalProfileService.getMany(dtoProfiles, Optional.of(EmbeddedOptions.ALL.toString()));

        Assert.isTrue(CollectionUtils.isNotEmpty(profiles), message + ": no profiles");

        Assert.isTrue(profiles.size() == dtoProfiles.size(), message + ": one of the profiles does not exist");

        profiles.stream().forEach(p -> Assert.isTrue(p.isEnabled(), message + ": one of the profile is disabled"));

        profiles.stream().forEach(p -> {
            LOGGER.debug("Profile : {} - profileLevel : {}, grouplevel : {}.", p, p.getLevel(), groupLevel);
            Assert.isTrue(StringUtils.equals(p.getLevel(), groupLevel), message + ": profile and group level must be equals");
        });

        profiles.stream().forEach(p -> {
            LOGGER.debug("Veryfing profile : {}", p.getId());
            LOGGER.debug("Profile : {}", p);
            Assert.isTrue(StringUtils.equals(p.getCustomerId(), customerId), message + ": profile and group customerId must be equals");
        });

        profiles.stream().forEach(p -> {
            final Tenant tenant = tenantRepository.findByIdentifier(p.getTenantIdentifier());
            Assert.notNull(tenant, message + ": the following tenant does not exist in database: " + p.getTenantIdentifier());
            Assert.isTrue(StringUtils.equals(tenant.getCustomerId(), customerId), message + ": tenant and group customerId must be equals");
        });

        for (int i = 0; i < profiles.size() - 1; i++) {
            for (int j = i + 1; j < profiles.size(); j++) {
                final ProfileDto p1 = profiles.get(i);
                final ProfileDto p2 = profiles.get(j);
                if (StringUtils.equals(p1.getApplicationName(), p2.getApplicationName()) && p1.getTenantIdentifier().equals(p2.getTenantIdentifier())) {
                    throw new IllegalArgumentException(
                            message + ": profiles " + p1.getId() + " and " + p2.getId() + " share the same applicationName and tenant");
                }
            }
        }
    }

    private void checkIsReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly group");
    }

    private void checkSetReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly must be set to false");
    }

    private void checkCustomer(final String customerId, final String message) {
        Assert.isTrue(StringUtils.equals(customerId, getInternalSecurityService().getCustomerId()), message + ": customerId " + customerId + " is not allowed");

        final Optional<Customer> customer = customerRepository.findById(customerId);
        Assert.isTrue(customer.isPresent(), message + ": customer " + customerId + " does not exist");

        Assert.isTrue(customer.get().isEnabled(), message + ": customer must be enabled");
    }

    private void checkNameExist(final String oldName, final String newName, final String customerId, final String message) {
        if (!StringUtils.equals(oldName, newName)) {
            final Criteria criteria = Criteria.where("customerId").is(customerId).and("name").is(newName);
            Assert.isTrue(!getRepository().exists(criteria), message + ": group already exists");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadExtraInformation(final GroupDto dto, final Optional<String> optEmbedded) {
        if (optEmbedded.isPresent()) {
            final String embedded = optEmbedded.get();
            if (EmbeddedOptions.ALL.toString().equalsIgnoreCase(embedded)) {
                final List<String> profileIds = dto.getProfileIds();
                final List<ProfileDto> profiles = internalProfileService.getMany(profileIds, IamUtils.buildOptionalEmbedded(EmbeddedOptions.ALL));
                profiles.sort(Comparator.comparing(ProfileDto::getApplicationName).thenComparing((p1, p2) -> p1.getTenantName().compareTo(p2.getTenantName()))
                        .thenComparing(ProfileDto::getName));
                dto.setProfiles(profiles);

                dto.setUsersCount(userRepository.countByGroupId(dto.getId()));
            }
        }
    }

    /**
     * We need to update group without looking at the readonly informations.
     * For example, we need to update the supervision profileGroup to add new profiles.
     * @param id
     * @param profileIds
     */
    public void updateProfilesById(final String id, final List<String> profileIds) {

        final Query query = new Query(Criteria.where("id").is(id));
        final Update update = Update.update("profileIds", profileIds);

        final Optional<Group> optionalGroup = groupRepository.findOne(query);
        if (optionalGroup.isPresent()) {
            final Group group = optionalGroup.get();
            final Collection<EventDiffDto> logbooks = new ArrayList<>();
            logbooks.add(new EventDiffDto(GroupConverter.PROFILE_IDS_KEY, groupConverter.convertProfileIdsToLogbook(group.getProfileIds()),
                    groupConverter.convertProfileIdsToLogbook(profileIds)));

            groupRepository.updateMulti(query, update);
            iamLogbookService.updateGroupEvent(group, logbooks);
        }
    }

    /*
     * Be cautious: This is a multi-client method.
     * Only privileged users should call this method with a customerId different from the current session
     */
    public List<String> getSubLevels(final String level, final String customerId) {
        final ArrayList<CriteriaDefinition> criterias = new ArrayList<>();
        criterias.add(Criteria.where("customerId").in(customerId));
        criterias.add(Criteria.where("enabled").is(true));
        if (!level.isEmpty()) {
            criterias.add(Criteria.where("level").regex("^" + level + "\\..+$"));
        }
        return getRepository().findAll(criterias).stream().map(Group::getLevel).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group internalConvertFromDtoToEntity(final GroupDto dto) {
        return super.internalConvertFromDtoToEntity(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupDto internalConvertFromEntityToDto(final Group group) {
        return super.internalConvertFromEntityToDto(group);
    }

    /**
    *
    * {@inheritDoc}
    */
    @Override
    public void addDataAccessRestrictions(final Collection<CriteriaDefinition> criteria) {
        super.addDataAccessRestrictions(criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkExist(final String criteriaJsonString) {
        return super.checkExist(criteriaJsonString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Group> getEntityClass() {
        return Group.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GroupRepository getRepository() {
        return groupRepository;
    }

    @Override
    protected Converter<GroupDto, Group> getConverter() {
        return groupConverter;
    }

    public JsonNode findHistoryById(final String id) throws VitamClientException {
        LOGGER.debug("findHistoryById for id" + id);
        final Integer tenantIdentifier = internalSecurityService.getTenantIdentifier();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier)
                .setAccessContract(internalSecurityService.getTenant(tenantIdentifier).getAccessContractLogbookIdentifier())
                .setApplicationSessionId(internalSecurityService.getApplicationId());

        final Optional<Group> group = getRepository().findById(id);
        group.orElseThrow(() -> new NotFoundException(String.format("No group found with id : %s", id)));
        return logbookService.findEventsByIdentifierAndCollectionNames(group.get().getIdentifier(), MongoDbCollections.GROUPS, vitamContext).toJsonNode();
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
    private VitamContext getVitamContext() {
        return internalSecurityService.buildVitamContext(internalSecurityService.getTenantIdentifier());
    }

}
