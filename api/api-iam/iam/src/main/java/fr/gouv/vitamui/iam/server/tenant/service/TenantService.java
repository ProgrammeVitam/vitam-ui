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

package fr.gouv.vitamui.iam.server.tenant.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.configuration.VitamConfigurationDto;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.NoRightsException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.CustomSequencesConstants;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.ConfigurationService;
import fr.gouv.vitamui.commons.vitam.api.dto.HistoryEventDto;
import fr.gouv.vitamui.iam.common.enums.Application;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.common.ApiIamConstants;
import fr.gouv.vitamui.iam.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.server.common.utils.EntityFactory;
import fr.gouv.vitamui.iam.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.server.security.AbstractResourceClientService;
import fr.gouv.vitamui.iam.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * The service to read, create, update and delete the tenants.
 */
@Getter
@Setter
public class TenantService extends AbstractResourceClientService<TenantDto, Tenant> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantService.class);

    private final String TENANT_INSUFFICIENT_PERMISSION_MESSAGE =
        "Unable to access to the tenant %s: insufficient permissions.";

    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;
    private final ProfileRepository profileRepository;
    private final GroupService groupService;
    private final UserService userService;
    private final OwnerService ownerService;
    private final SecurityService securityService;
    private final IamLogbookService iamLogbookService;
    private final TenantConverter tenantConverter;
    private final InitVitamTenantService initVitamTenantService;
    private final LogbookService logbookService;
    private final CustomerInitConfig customerInitConfig;
    private final ExternalParametersRepository externalParametersRepository;
    private final ExternalParametersService externalParametersService;
    private final ConfigurationService configurationService;

    @Autowired
    public TenantService(
        final SequenceGeneratorService sequenceGeneratorService,
        final TenantRepository tenantRepository,
        final CustomerRepository customerRepository,
        final OwnerRepository ownerRepository,
        final ProfileRepository profileRepository,
        final GroupService groupService,
        final UserService userService,
        final OwnerService ownerService,
        final SecurityService securityService,
        final IamLogbookService iamLogbookService,
        final TenantConverter tenantConverter,
        final InitVitamTenantService initVitamTenantService,
        final LogbookService logbookService,
        final CustomerInitConfig customerInitConfig,
        final ExternalParametersRepository externalParametersRepository,
        final ExternalParametersService externalParametersService,
        final ConfigurationService configurationService
    ) {
        super(sequenceGeneratorService, securityService);
        this.tenantRepository = tenantRepository;
        this.customerRepository = customerRepository;
        this.ownerRepository = ownerRepository;
        this.profileRepository = profileRepository;
        this.groupService = groupService;
        this.userService = userService;
        this.ownerService = ownerService;
        this.securityService = securityService;
        this.iamLogbookService = iamLogbookService;
        this.tenantConverter = tenantConverter;
        this.initVitamTenantService = initVitamTenantService;
        this.logbookService = logbookService;
        this.customerInitConfig = customerInitConfig;
        this.externalParametersRepository = externalParametersRepository;
        this.externalParametersService = externalParametersService;
        this.configurationService = configurationService;
    }

    /**
     * List of every profile needed when a new tenant is created.
     *
     * @return
     */
    public List<Profile> getDefaultProfiles(final String customerId, final Integer tenantIdentifier) {
        final List<Profile> profiles = new ArrayList<>();

        profiles.add(
            EntityFactory.buildProfile(
                ApiIamConstants.HIERARCHY_PROFILE_NAME + " " + tenantIdentifier,
                getNextSequenceId(SequencesConstants.PROFILE_IDENTIFIER),
                ApiIamConstants.HIERARCHY_PROFILE_DESCRIPTION,
                true,
                ApiIamConstants.ADMIN_LEVEL,
                tenantIdentifier,
                CommonConstants.HIERARCHY_PROFILE_APPLICATIONS_NAME,
                ApiIamConstants.getHierarchyRoles(),
                customerId
            )
        );

        if (customerInitConfig.getTenantProfiles() != null) {
            customerInitConfig
                .getTenantProfiles()
                .forEach(
                    p ->
                        profiles.add(
                            EntityFactory.buildProfile(
                                p.getName() + " " + tenantIdentifier,
                                getNextSequenceId(SequencesConstants.PROFILE_IDENTIFIER),
                                p.getDescription(),
                                true,
                                p.getLevel(),
                                tenantIdentifier,
                                p.getAppName(),
                                p.getRoles(),
                                customerId
                            )
                        )
                );
        }

        //@formatter:on

        return profiles;
    }

    @Override
    protected void beforeCreate(final TenantDto tenantDto) {
        final String message = "Unable to create tenant " + tenantDto.getName();
        Customer customer = checkCustomer(tenantDto.getCustomerId(), message);
        checkIdentifier(tenantDto.getIdentifier(), message);
        checkOwner(tenantDto, message);
        checkProof(tenantDto.isProof(), tenantDto.getCustomerId(), message);
        this.checkIfTenantIdIsAvailable(tenantDto.getIdentifier());
        if (!tenantDto.isProof()) {
            checkSetReadonly(tenantDto.isReadonly(), message);
        }
        ExternalParametersDto fullAccessContract = initFullAccessContractExternalParameter(
            customer.getIdentifier(),
            tenantDto.getName()
        );
        LOGGER.debug(
            "Initializing VITAM Tenant with customer identifier {} and tenant name {}",
            customer.getIdentifier(),
            tenantDto.getName()
        );
        initVitamTenantService.init(tenantDto, fullAccessContract);
        fullAccessContract.setName(
            ExternalParametersService.EXTERNAL_PARAMETER_IDENTIFIER_PREFIX + tenantDto.getIdentifier()
        );
        externalParametersService.create(fullAccessContract);
        final String name = tenantDto.getName() != null ? tenantDto.getName().trim() : tenantDto.getName();
        final List<Tenant> tenants = tenantRepository.findByNameIgnoreCaseAndCustomerId(
            name,
            tenantDto.getCustomerId()
        );
        Assert.isTrue(
            tenants == null || tenants.isEmpty(),
            message + ": a tenant with the name: " + name + " already exists."
        );
    }

    @Override
    @Transactional
    public TenantDto create(final TenantDto tenantDto) {
        Optional<Customer> customerOptional = customerRepository.findById(tenantDto.getCustomerId());
        Assert.isTrue(customerOptional.isPresent(), "No customer found with id " + tenantDto.getCustomerId());
        Customer customer = customerOptional.get();
        final TenantDto createdTenantDto = super.create(tenantDto);
        Optional<ExternalParameters> fullAccessContractOpt = externalParametersRepository.findByIdentifier(
            ExternalParametersService.EXTERNAL_PARAMETER_IDENTIFIER_PREFIX +
            customer.getIdentifier() +
            "_" +
            tenantDto.getName()
        );
        Assert.isTrue(
            fullAccessContractOpt.isPresent(),
            "No external parameter found with id " +
            ExternalParametersService.EXTERNAL_PARAMETER_IDENTIFIER_PREFIX +
            customer.getIdentifier() +
            "_" +
            tenantDto.getName()
        );
        createExternalParameterProfileForDefaultAccessContract(
            tenantDto.getCustomerId(),
            tenantDto.getIdentifier(),
            fullAccessContractOpt.get().getId()
        );

        iamLogbookService.createTenantEvent(createdTenantDto);
        final List<Profile> profiles = getDefaultProfiles(
            createdTenantDto.getCustomerId(),
            createdTenantDto.getIdentifier()
        );
        profiles.forEach(profile -> saveProfile(profile));
        addAdminProfilesToAdminGroup(createdTenantDto.getCustomerId(), profiles);
        return createdTenantDto;
    }

    @Override
    protected void beforeUpdate(final TenantDto tenantDto) {
        final Tenant tenant = findById(tenantDto.getId());

        final String message = "Unable to update tenant " + tenantDto.getId();
        checkCustomer(tenantDto.getCustomerId(), message);
        checkIsReadonly(tenant.isReadonly(), message);
        checkIdentifier(tenant.getIdentifier(), tenantDto.getIdentifier(), message);
        checkOwner(tenant, tenantDto.getOwnerId(), message);
        checkSetReadonly(tenantDto.isReadonly(), message);

        final String name = tenantDto.getName() != null ? tenantDto.getName().trim() : tenantDto.getName();
        final List<Tenant> tenants = tenantRepository.findByNameIgnoreCaseAndCustomerId(name, tenant.getCustomerId());
        if (tenants != null && !tenants.isEmpty()) {
            Assert.isTrue(
                tenants.size() == 1 && tenants.contains(tenant),
                message + ": a tenant with the name: " + name + " already exists."
            );
        }
    }

    @Override
    protected Tenant beforePatch(final Map<String, Object> partialDto) {
        final String id = CastUtils.toString(partialDto.get("id"));

        final String message = "Unable to patch tenant " + id;
        final Tenant tenant = tenantRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Entity not found " + getObjectName() + " with id : " + id));

        Assert.isTrue(
            !checkMapContainsOnlyFieldsUnmodifiable(
                partialDto,
                Arrays.asList("id", "customerId", "readonly", "identifier", "proof")
            ),
            message
        );

        final String customerId = CastUtils.toString(partialDto.get("customerId"));
        if (customerId != null) {
            checkCustomer(customerId, message);
        }

        checkIsReadonly(tenant.isReadonly(), message);

        final Integer identifier = CastUtils.toInteger(partialDto.get("identifier"));
        if (identifier != null) {
            checkIdentifier(tenant.getIdentifier(), identifier, message);
        }

        final Boolean readonly = CastUtils.toBoolean(partialDto.get("readonly"));
        if (readonly != null) {
            checkSetReadonly(readonly, message);
        }

        final String ownerId = CastUtils.toString(partialDto.get("ownerId"));
        if (ownerId != null) {
            checkOwner(tenant, ownerId, message);
        }

        final String name = CastUtils.toString(partialDto.get("name"));
        if (name != null) {
            final List<Tenant> tenants = tenantRepository.findByNameIgnoreCaseAndCustomerId(
                name.trim(),
                tenant.getCustomerId()
            );
            if (tenants != null && !tenants.isEmpty()) {
                Assert.isTrue(
                    tenants.size() == 1 && tenants.contains(tenant),
                    message + ": a tenant with the name: " + name + " already exists."
                );
            }
        }

        return tenant;
    }

    @Override
    @Transactional
    public TenantDto patch(final Map<String, Object> partialDto) {
        return super.patch(partialDto);
    }

    @Override
    protected void processPatch(final Tenant tenant, final Map<String, Object> partialDto) {
        final Collection<EventDiffDto> logbooks = new ArrayList<>();
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        if (vitamContext != null) {
            LOGGER.info("Patch Tenant EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        }

        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case "id":
                case "readonly":
                case "customerId":
                case "identifier":
                case "proof":
                    break;
                case "name":
                    logbooks.add(new EventDiffDto(TenantConverter.NAME_KEY, tenant.getName(), entry.getValue()));
                    tenant.setName(CastUtils.toString(entry.getValue()));
                    break;
                case "enabled":
                    logbooks.add(new EventDiffDto(TenantConverter.ENABLED_KEY, tenant.getEnabled(), entry.getValue()));
                    tenant.setEnabled(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "ownerId":
                    final OwnerDto oldOwner = ownerService.getOne(tenant.getOwnerId(), Optional.empty());
                    final OwnerDto newOwner = ownerService.getOne(
                        CastUtils.toString(entry.getValue()),
                        Optional.empty()
                    );

                    logbooks.add(
                        new EventDiffDto(
                            TenantConverter.OWNER_ID_KEY,
                            oldOwner.getIdentifier(),
                            newOwner.getIdentifier()
                        )
                    );
                    tenant.setOwnerId(CastUtils.toString(entry.getValue()));
                    break;
                case "accessContractHoldingIdentifier":
                    final String accessContractHoldingIdentifier = CastUtils.toString(entry.getValue());
                    logbooks.add(
                        new EventDiffDto(
                            TenantConverter.ACCESS_CONTRACT_HOLDING_IDENTIFIER_KEY,
                            tenant.getAccessContractHoldingIdentifier(),
                            accessContractHoldingIdentifier
                        )
                    );
                    tenant.setAccessContractHoldingIdentifier(accessContractHoldingIdentifier);
                    break;
                case "accessContractLogbookIdentifier":
                    final String accessContractLogbookIdentifier = CastUtils.toString(entry.getValue());
                    logbooks.add(
                        new EventDiffDto(
                            TenantConverter.ACCESS_CONTRACT_LOGBOOK_IDENTIFIER_KEY,
                            tenant.getAccessContractLogbookIdentifier(),
                            accessContractLogbookIdentifier
                        )
                    );
                    tenant.setAccessContractLogbookIdentifier(accessContractLogbookIdentifier);
                    break;
                case "ingestContractHoldingIdentifier":
                    final String ingestContractHoldingIdentifier = CastUtils.toString(entry.getValue());
                    logbooks.add(
                        new EventDiffDto(
                            TenantConverter.INGEST_CONTRACT_HOLDING_IDENTIFIER_KEY,
                            tenant.getIngestContractHoldingIdentifier(),
                            ingestContractHoldingIdentifier
                        )
                    );
                    tenant.setIngestContractHoldingIdentifier(ingestContractHoldingIdentifier);
                    break;
                case "itemIngestContractIdentifier":
                    final String itemIngestContractIdentifier = CastUtils.toString(entry.getValue());
                    logbooks.add(
                        new EventDiffDto(
                            TenantConverter.ITEM_INGEST_CONTRACT_IDENTIFIER_KEY,
                            tenant.getItemIngestContractIdentifier(),
                            itemIngestContractIdentifier
                        )
                    );
                    tenant.setItemIngestContractIdentifier(itemIngestContractIdentifier);
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unable to patch tenant " + tenant.getId() + ": key " + entry.getKey() + " is not allowed"
                    );
            }
        }
        iamLogbookService.updateTenantEvent(tenant, logbooks);
    }

    private void checkIsReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly tenant");
    }

    private void checkIdentifier(final int identifier1, final int identifier2, final String message) {
        Assert.isTrue(
            identifier1 == identifier2,
            message + ": tenant identifiers " + identifier1 + " and " + identifier2 + " are not equals"
        );
    }

    private void checkProof(final boolean isProof, final String customerId, final String message) {
        if (isProof) {
            final Optional<Tenant> optTenant = tenantRepository.findByCustomerIdAndProofIsTrue(customerId);
            Assert.isTrue(
                optTenant.isEmpty(),
                message + ": a proof tenant already exists for customerId: " + customerId
            );
        }
    }

    private void checkIdentifier(final Integer identifier, final String message) {
        if (identifier != null) {
            final Tenant tenant = tenantRepository.findByIdentifier(identifier);
            Assert.isNull(tenant, message + ": a tenant with the identifier: " + identifier + " already exists.");
        }
    }

    private Customer checkCustomer(final String customerId, final String message) {
        final Optional<Customer> customer = customerRepository.findById(customerId);
        Assert.isTrue(customer.isPresent(), message + ": customer does not exist");
        return customer.get();
    }

    private void checkSetReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly must be set to false");
    }

    private void checkOwner(final TenantDto tenantDto, final String message) {
        final Optional<Owner> optOwner = ownerRepository.findById(tenantDto.getOwnerId());
        Assert.isTrue(optOwner.isPresent(), message + ": owner " + tenantDto.getOwnerId() + " does not exist");

        final String ownerCustId = optOwner.get().getCustomerId();
        final String tenantCustId = optOwner.get().getCustomerId();
        Assert.isTrue(
            StringUtils.equals(ownerCustId, tenantCustId),
            message + " owner.customerId " + ownerCustId + " and tenant.customerId " + tenantCustId + " must be equals"
        );
    }

    private void checkOwner(final Tenant tenant, final String ownerId, final String message) {
        final Optional<Owner> optOwner = ownerRepository.findById(ownerId);
        Assert.isTrue(optOwner.isPresent(), message + ": owner " + ownerId + " does not exist");

        final String ownerCustId = optOwner.get().getCustomerId();
        final String tenantCustId = tenant.getCustomerId();
        Assert.isTrue(
            StringUtils.equals(ownerCustId, tenantCustId),
            message + " owner.customerId " + ownerCustId + " and tenant.customerId " + tenantCustId + " must be equals"
        );
    }

    public TenantDto findByIdentifier(final Integer identifier) {
        return convertFromEntityToDto(tenantRepository.findByIdentifier(identifier));
    }

    public List<TenantDto> findByCustomerId(final String customerId) {
        return convertIterableToList(tenantRepository.findByCustomerId(customerId));
    }

    /**
     * Method allowing to retrieve a tenant in the repository.
     *
     * @param id Id of the tenant.
     * @return The tenant linked to the id.
     */
    protected Tenant findById(final String id) {
        return tenantRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Entity not found " + getObjectName() + " with id : " + id));
    }

    private Profile saveProfile(final Profile profile) {
        profile.setId(profileRepository.generateSuperId());
        profile.setIdentifier(getNextSequenceId(SequencesConstants.PROFILE_IDENTIFIER));
        iamLogbookService.createProfileEvent(profile);
        return profileRepository.save(profile);
    }

    private void addAdminProfilesToAdminGroup(final String customerId, final List<Profile> profiles) {
        final String[] apps = { CommonConstants.HIERARCHY_PROFILE_APPLICATIONS_NAME };

        final UserDto adminUserDto = userService.getDefaultAdminUser(customerId);
        final GroupDto adminGroupDto = groupService.getOne(
            adminUserDto.getGroupId(),
            Optional.empty(),
            Optional.empty()
        );

        for (final String app : apps) {
            final Profile profile = profiles
                .stream()
                .filter(p -> app.equals(p.getApplicationName()))
                .findFirst()
                .orElseThrow(
                    () ->
                        new ApplicationServerException(
                            "Profile not found for app %s and customer %s.".formatted(app, customerId)
                        )
                );
            adminGroupDto.getProfileIds().add(profile.getId());
        }
        groupService.updateProfilesById(adminGroupDto.getId(), adminGroupDto.getProfileIds());
    }

    public List<HistoryEventDto> findHistoryById(final String id) throws VitamClientException {
        LOGGER.debug("findHistoryById for id" + id);

        final Integer tenantIdentifier = securityService.getTenantIdentifier();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier)
            .setAccessContract(securityService.getTenant(tenantIdentifier).getAccessContractLogbookIdentifier())
            .setApplicationSessionId(securityService.getApplicationId());

        final Optional<Tenant> tenant = getRepository().findById(id);
        tenant.orElseThrow(() -> new NotFoundException("No tenant found with id : %s".formatted(id)));

        LOGGER.info(
            "Tenant History EvIdAppSession : {} ",
            securityService.buildVitamContext(securityService.getTenantIdentifier()).getApplicationSessionId()
        );
        return logbookService.findEventsByIdentifierAndCollectionNames(
            String.valueOf(tenant.get().getIdentifier()),
            MongoDbCollections.TENANTS,
            vitamContext,
            List.of(EventType.EXT_VITAMUI_CREATE_TENANT.name(), EventType.EXT_VITAMUI_UPDATE_TENANT.name())
        );
    }

    @Override
    protected TenantRepository getRepository() {
        return tenantRepository;
    }

    @Override
    protected String getObjectName() {
        return "tenant";
    }

    @Override
    protected Class<Tenant> getEntityClass() {
        return Tenant.class;
    }

    @Override
    protected Converter<TenantDto, Tenant> getConverter() {
        return tenantConverter;
    }

    private Profile createExternalParameterProfileForDefaultAccessContract(
        String customerId,
        Integer tenantIdentifier,
        String externalParameterId
    ) {
        Profile defaultAccessContractProfile = EntityFactory.buildProfile(
            ExternalParametersService.EXTERNAL_PARAMS_PROFILE_NAME_PREFIX + " " + tenantIdentifier,
            String.valueOf(
                sequenceGeneratorService.getNextSequenceId(
                    SequencesConstants.PROFILE_IDENTIFIER,
                    CustomSequencesConstants.DEFAULT_SEQUENCE_INCREMENT_VALUE
                )
            ),
            ExternalParametersService.EXTERNAL_PARAMS_PROFILE_NAME_PREFIX + " " + tenantIdentifier,
            true,
            "",
            tenantIdentifier,
            Application.EXTERNAL_PARAMS.name(),
            List.of(ServicesData.ROLE_GET_EXTERNAL_PARAMS),
            customerId,
            externalParameterId
        );
        return saveProfile(defaultAccessContractProfile);
    }

    private ExternalParametersDto initFullAccessContractExternalParameter(
        String customerIdentifier,
        String tenantName
    ) {
        ExternalParametersDto fullAccessContract = new ExternalParametersDto();
        fullAccessContract.setIdentifier(
            ExternalParametersService.EXTERNAL_PARAMETER_IDENTIFIER_PREFIX + customerIdentifier + "_" + tenantName
        );
        fullAccessContract.setName(
            ExternalParametersService.EXTERNAL_PARAMETER_NAME_PREFIX + customerIdentifier + "_" + tenantName
        );
        return fullAccessContract;
    }

    public List<Integer> extractUnusedTenants(List<Integer> currentTenants, List<Integer> availableTenants) {
        List<Integer> copyList = new ArrayList<>(availableTenants);
        copyList.removeAll(currentTenants);
        return copyList;
    }

    public List<Integer> getAvailableTenantsIds() {
        LOGGER.debug("Retrieve free tenant list ");
        VitamConfigurationDto vitamConfigurationDto = configurationService.getVitamPublicConfigurations();
        List<Integer> tenantsList = vitamConfigurationDto.getTenants();
        List<TenantDto> currentUsedTenants = this.getAll(Optional.empty());
        List<Integer> tenantCouldNotUse = new ArrayList<>(vitamConfigurationDto.getAdminTenant());
        tenantCouldNotUse.addAll(currentUsedTenants.stream().map(TenantDto::getIdentifier).toList());
        return extractUnusedTenants(tenantCouldNotUse, tenantsList);
    }

    public void checkIfTenantIdIsAvailable(Integer tenantId) {
        List<Integer> availableTenantsId = getAvailableTenantsIds();
        Assert.isTrue(
            CollectionUtils.isNotEmpty(availableTenantsId) && availableTenantsId.contains(tenantId),
            "The tenant " + tenantId + ": is already used "
        );
    }

    /**
     * Method allowing to check if a current user can update the tenant with the following customer.
     *
     * @param customerId Identifier of the customer.
     * @return true if the action is allowed, false otherwise.
     */
    protected boolean canAccessToCustomer(final String customerId) {
        return (
            securityService.hasRole(ServicesData.ROLE_UPDATE_TENANTS_ALL_CUSTOMERS) ||
            customerId.equals(securityService.getCustomerId())
        );
    }

    /**
     * Check if a current user can access the tenant.
     *
     * @param tenantIdentifier Identifier of the tenant.
     * @return true if the action is allowed, false otherwise.
     */
    protected boolean canAccessToTenant(final Integer tenantIdentifier) {
        if (!securityService.hasRole(ServicesData.ROLE_GET_ALL_TENANTS)) {
            final Integer securityTenantIdentifier = securityService.getTenantIdentifier();
            return Objects.equals(securityTenantIdentifier, tenantIdentifier);
        }
        return true;
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return List.of("id", CUSTOMER_ID_KEY, "enabled", "proof", "name", "identifier", "ownerId");
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        final List<String> restrictedKeys = new ArrayList<>();
        if (!securityService.hasRole(ServicesData.ROLE_GET_ALL_TENANTS)) {
            restrictedKeys.add(CUSTOMER_ID_KEY);
            if (!securityService.hasRole(ServicesData.ROLE_GET_TENANTS_MY_CUSTOMER)) {
                restrictedKeys.add("identifier");
            }
        }
        return restrictedKeys;
    }

    @Override
    protected void checkCustomerCriteria(final Criterion customerCriteria) {
        Assert.isTrue(
            canAccessToCustomer(CastUtils.toString(customerCriteria.getValue())),
            "customerId's criteria is not equal to the customerId from context"
        );
    }

    @Override
    protected void addRestriction(final String key, final QueryDto criteria) {
        switch (key) {
            case "identifier":
                final Optional<Criterion> criterionOpt = criteria.find("identifier");
                if (criterionOpt.isPresent()) {
                    checkIdentifierCriteria(criterionOpt.get());
                } else {
                    criteria.addCriterion(getIdentifierRestriction());
                }
                break;
            default:
                throw new NotImplementedException("Restriction not defined for key: " + key);
        }
    }

    private Criterion getIdentifierRestriction() {
        return new Criterion("identifier", securityService.getTenantIdentifier(), CriterionOperator.EQUALS);
    }

    /**
     * Method allowing to check the content of the criterion for the field "identifier"
     *
     * @param identifierCriterion Criterion linked to the identifier to check.
     */
    protected void checkIdentifierCriteria(final Criterion identifierCriterion) {
        final List<Integer> identifiers = new ArrayList<>();
        switch (identifierCriterion.getOperator()) {
            case EQUALS:
                identifiers.add(CastUtils.toInteger(identifierCriterion.getValue()));
                break;
            case IN:
                identifiers.addAll(CastUtils.toList(identifierCriterion.getValue()));
                break;
            default:
                throw new IllegalArgumentException(
                    "Operation " + identifierCriterion.getOperator() + " is not supported for field : identifier"
                );
        }
        final List<Integer> wrongIdentifiers = identifiers.stream().filter(this::canAccessToTenant).toList();
        if (!wrongIdentifiers.isEmpty()) {
            throw new NoRightsException(TENANT_INSUFFICIENT_PERMISSION_MESSAGE.formatted(wrongIdentifiers.toString()));
        }
    }

    @Override
    protected String getVersionApiCriteria() {
        return CRITERIA_VERSION_V2;
    }
}
