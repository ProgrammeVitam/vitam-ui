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
package fr.gouv.vitamui.iam.internal.server.tenant.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.common.utils.EntityFactory;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.customer.service.InitCustomerService;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * The service to read, create, update and delete the tenants.
 */
@Getter
@Setter
public class TenantInternalService extends VitamUICrudService<TenantDto, Tenant> {


    private final TenantRepository tenantRepository;

    private final CustomerRepository customerRepository;

    private final OwnerRepository ownerRepository;

    private final GroupRepository groupRepository;

    private final ProfileRepository profileRepository;

    private final UserRepository userRepository;

    private final GroupInternalService internalGroupService;

    private final UserInternalService internalUserService;

    private final OwnerInternalService internalOwnerService;

    private final ProfileInternalService internalProfileService;

    private final InternalSecurityService internalSecurityService;

    private final IamLogbookService iamLogbookService;

    private final TenantConverter tenantConverter;

    private final InitVitamTenantService initVitamTenantService;

    private final LogbookService logbookService;

    private CustomerInitConfig customerInitConfig;

    private final ExternalParametersRepository externalParametersRepository;

    private final ExternalParametersInternalService externalParametersInternalService;



    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TenantInternalService.class);

    @Autowired
    public TenantInternalService(final CustomSequenceRepository sequenceRepository,
        final TenantRepository tenantRepository,
        final CustomerRepository customerRepository, final OwnerRepository ownerRepository,
        final GroupRepository groupRepository,
        final ProfileRepository profileRepository, final UserRepository userRepository,
        final GroupInternalService internalGroupService,
        final UserInternalService internalUserService, final OwnerInternalService internalOwnerService,
        final ProfileInternalService internalProfileService,
        final InternalSecurityService internalSecurityService, final IamLogbookService iamLogbookService,
        final TenantConverter tenantConverter,
        final InitVitamTenantService initVitamTenantService, final LogbookService logbookService,
        final CustomerInitConfig customerInitConfig, final ExternalParametersRepository externalParametersRepository,
        final ExternalParametersInternalService externalParametersInternalService
    ) {
        super(sequenceRepository);
        this.tenantRepository = tenantRepository;
        this.customerRepository = customerRepository;
        this.ownerRepository = ownerRepository;
        this.groupRepository = groupRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.internalGroupService = internalGroupService;
        this.internalUserService = internalUserService;
        this.internalOwnerService = internalOwnerService;
        this.internalProfileService = internalProfileService;
        this.internalSecurityService = internalSecurityService;
        this.iamLogbookService = iamLogbookService;
        this.tenantConverter = tenantConverter;
        this.initVitamTenantService = initVitamTenantService;
        this.logbookService = logbookService;
        this.customerInitConfig = customerInitConfig;
        this.externalParametersRepository = externalParametersRepository;
        this.externalParametersInternalService = externalParametersInternalService;

    }

    /**
     * List of every profile needed when a new tenant is created.
     *
     * @return
     */
    public List<Profile> getDefaultProfiles(final String customerId, final Integer tenantIdentifier) {
        final List<Profile> profiles = new ArrayList<>();


        profiles.add(EntityFactory.buildProfile(ApiIamInternalConstants.HIERARCHY_PROFILE_NAME + " " + tenantIdentifier,
            getNextSequenceId(SequencesConstants.PROFILE_IDENTIFIER),
            ApiIamInternalConstants.HIERARCHY_PROFILE_DESCRIPTION,
            true,
            ApiIamInternalConstants.ADMIN_LEVEL,
            tenantIdentifier,
            CommonConstants.HIERARCHY_PROFILE_APPLICATIONS_NAME,
            ApiIamInternalConstants.getHierarchyRoles(),
            customerId));

        if (customerInitConfig.getTenantProfiles() != null) {
            customerInitConfig.getTenantProfiles()
                .forEach(p -> profiles.add(EntityFactory.buildProfile(p.getName() + " " + tenantIdentifier,
                    getNextSequenceId(SequencesConstants.PROFILE_IDENTIFIER),
                    p.getDescription(),
                    true,
                    p.getLevel(),
                    tenantIdentifier,
                    p.getAppName(),
                    p.getRoles(),
                    customerId)));
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
        if (!tenantDto.isProof()) {
            checkSetReadonly(tenantDto.isReadonly(), message);
        }
        ExternalParametersDto fullAccessContract = checkAndGetExternalParameterByIdentifier(
            InitCustomerService.EXTERNAL_PARAM_DEFAULT_ACCESS_CONTRACT_PREFIX + customer.getIdentifier(), message);
        tenantDto.setIdentifier(generateTenantIdentifier());
        initVitamTenantService.init(tenantDto, fullAccessContract);
        final String name = tenantDto.getName() != null ? tenantDto.getName().trim() : tenantDto.getName();
        final List<Tenant> tenants =
            tenantRepository.findByNameIgnoreCaseAndCustomerId(name, tenantDto.getCustomerId());
        Assert.isTrue(tenants == null || tenants.isEmpty(),
            message + ": a tenant with the name: " + name + " already exists.");
    }



    @Override
    @Transactional
    public TenantDto create(final TenantDto tenantDto) {

        final TenantDto createdTenantDto = super.create(tenantDto);

        iamLogbookService.createTenantEvent(createdTenantDto);

        final List<Profile> profiles =
            getDefaultProfiles(createdTenantDto.getCustomerId(), createdTenantDto.getIdentifier());
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
            Assert.isTrue(tenants.size() == 1 && tenants.contains(tenant),
                message + ": a tenant with the name: " + name + " already exists.");
        }
    }

    @Override
    protected Tenant beforePatch(final Map<String, Object> partialDto) {
        final String id = CastUtils.toString(partialDto.get("id"));

        final String message = "Unable to patch tenant " + id;
        final Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Entity not found " + getObjectName() + " with id : " + id));

        Assert.isTrue(!checkMapContainsOnlyFieldsUnmodifiable(partialDto,
            Arrays.asList("id", "customerId", "readonly", "identifier", "proof")), message);

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
            final List<Tenant> tenants =
                tenantRepository.findByNameIgnoreCaseAndCustomerId(name.trim(), tenant.getCustomerId());
            if (tenants != null && !tenants.isEmpty()) {
                Assert.isTrue(tenants.size() == 1 && tenants.contains(tenant),
                    message + ": a tenant with the name: " + name + " already exists.");
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
        final VitamContext vitamContext =
            internalSecurityService.buildVitamContext(internalSecurityService.getTenantIdentifier());
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
                    final OwnerDto oldOwner = internalOwnerService.getOne(tenant.getOwnerId(), Optional.empty());
                    final OwnerDto newOwner =
                        internalOwnerService.getOne(CastUtils.toString(entry.getValue()), Optional.empty());

                    logbooks.add(new EventDiffDto(TenantConverter.OWNER_ID_KEY, oldOwner.getIdentifier(),
                        newOwner.getIdentifier()));
                    tenant.setOwnerId(CastUtils.toString(entry.getValue()));
                    break;
                case "accessContractHoldingIdentifier":
                    final String accessContractHoldingIdentifier = CastUtils.toString(entry.getValue());
                    logbooks.add(new EventDiffDto(TenantConverter.ACCESS_CONTRACT_HOLDING_IDENTIFIER_KEY,
                        tenant.getAccessContractHoldingIdentifier(),
                        accessContractHoldingIdentifier));
                    tenant.setAccessContractHoldingIdentifier(accessContractHoldingIdentifier);
                    break;
                case "accessContractLogbookIdentifier":
                    final String accessContractLogbookIdentifier = CastUtils.toString(entry.getValue());
                    logbooks.add(new EventDiffDto(TenantConverter.ACCESS_CONTRACT_LOGBOOK_IDENTIFIER_KEY,
                        tenant.getAccessContractLogbookIdentifier(),
                        accessContractLogbookIdentifier));
                    tenant.setAccessContractLogbookIdentifier(accessContractLogbookIdentifier);
                    break;
                case "ingestContractHoldingIdentifier":
                    final String ingestContractHoldingIdentifier = CastUtils.toString(entry.getValue());
                    logbooks.add(new EventDiffDto(TenantConverter.INGEST_CONTRACT_HOLDING_IDENTIFIER_KEY,
                        tenant.getIngestContractHoldingIdentifier(),
                        ingestContractHoldingIdentifier));
                    tenant.setIngestContractHoldingIdentifier(ingestContractHoldingIdentifier);
                    break;
                case "itemIngestContractIdentifier":
                    final String itemIngestContractIdentifier = CastUtils.toString(entry.getValue());
                    logbooks.add(new EventDiffDto(TenantConverter.ITEM_INGEST_CONTRACT_IDENTIFIER_KEY,
                        tenant.getItemIngestContractIdentifier(),
                        itemIngestContractIdentifier));
                    tenant.setItemIngestContractIdentifier(itemIngestContractIdentifier);
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unable to patch tenant " + tenant.getId() + ": key " + entry.getKey() + " is not allowed");
            }
        }
        iamLogbookService.updateTenantEvent(tenant, logbooks);
    }

    private void checkIsReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly tenant");
    }

    private void checkIdentifier(final int identifier1, final int identifier2, final String message) {
        Assert.isTrue(identifier1 == identifier2,
            message + ": tenant identifiers " + identifier1 + " and " + identifier2 + " are not equals");
    }

    private void checkProof(final boolean isProof, final String customerId, final String message) {
        if (isProof) {
            final Optional<Tenant> optTenant = tenantRepository.findByCustomerIdAndProofIsTrue(customerId);
            Assert.isTrue(!optTenant.isPresent(),
                message + ": a proof tenant already exists for customerId: " + customerId);
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
        Assert.isTrue(StringUtils.equals(ownerCustId, tenantCustId),
            message + " owner.customerId " + ownerCustId + " and tenant.customerId " + tenantCustId +
                " must be equals");
    }

    private void checkOwner(final Tenant tenant, final String ownerId, final String message) {
        final Optional<Owner> optOwner = ownerRepository.findById(ownerId);
        Assert.isTrue(optOwner.isPresent(), message + ": owner " + ownerId + " does not exist");

        final String ownerCustId = optOwner.get().getCustomerId();
        final String tenantCustId = tenant.getCustomerId();
        Assert.isTrue(StringUtils.equals(ownerCustId, tenantCustId),
            message + " owner.customerId " + ownerCustId + " and tenant.customerId " + tenantCustId +
                " must be equals");
    }

    private ExternalParametersDto checkAndGetExternalParameterByIdentifier(final String externalParameterIdentifier,
        final String message) {
        final Optional<ExternalParameters> optExternalParameter =
            externalParametersRepository.findByIdentifier(externalParameterIdentifier);
        Assert.isTrue(optExternalParameter.isPresent(),
            message + ": External Parameter with identifier" + externalParameterIdentifier + " does not exist");
        return externalParametersInternalService.internalConvertFromEntityToDto(optExternalParameter.get());
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
        return tenantRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Entity not found " + getObjectName() + " with id : " + id));
    }

    private Profile saveProfile(final Profile profile) {
        profile.setId(profileRepository.generateSuperId());
        profile.setIdentifier(getNextSequenceId(SequencesConstants.PROFILE_IDENTIFIER));
        iamLogbookService.createProfileEvent(profile);
        return profileRepository.save(profile);
    }

    private void addAdminProfilesToAdminGroup(final String customerId, final List<Profile> profiles) {
        final String[] apps = {CommonConstants.HIERARCHY_PROFILE_APPLICATIONS_NAME};

        final UserDto adminUserDto = internalUserService.getDefaultAdminUser(customerId);
        final GroupDto adminGroupDto =
            internalGroupService.getOne(adminUserDto.getGroupId(), Optional.empty(), Optional.empty());

        for (final String app : apps) {
            final Profile profile = profiles.stream().filter(p -> app.equals(p.getApplicationName())).findFirst()
                .orElseThrow(() -> new ApplicationServerException(
                    String.format("Profile not found for app %s and customer %s.", app, customerId)));
            adminGroupDto.getProfileIds().add(profile.getId());
        }
        internalGroupService.updateProfilesById(adminGroupDto.getId(), adminGroupDto.getProfileIds());
    }

    public JsonNode findHistoryById(final String id) throws VitamClientException {
        LOGGER.debug("findHistoryById for id" + id);

        final Integer tenantIdentifier = internalSecurityService.getTenantIdentifier();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier)
            .setAccessContract(internalSecurityService.getTenant(tenantIdentifier).getAccessContractLogbookIdentifier())
            .setApplicationSessionId(internalSecurityService.getApplicationId());

        final Optional<Tenant> tenant = getRepository().findById(id);
        tenant.orElseThrow(() -> new NotFoundException(String.format("No tenant found with id : %s", id)));

        LOGGER.info("Tenant History EvIdAppSession : {} ",
            internalSecurityService.buildVitamContext(internalSecurityService.getTenantIdentifier())
                .getApplicationSessionId());
        return logbookService.findEventsByIdentifierAndCollectionNames(String.valueOf(tenant.get().getIdentifier()),
            MongoDbCollections.TENANTS, vitamContext)
            .toJsonNode();
    }

    private synchronized int generateTenantIdentifier() {
        return getNextSequenceId(SequencesConstants.TENANT_IDENTIFIER, 100);
    }

    @Override
    protected Tenant internalConvertFromDtoToEntity(final TenantDto dto) {
        return super.internalConvertFromDtoToEntity(dto);
    }

    @Override
    protected TenantDto internalConvertFromEntityToDto(final Tenant tenant) {
        return super.internalConvertFromEntityToDto(tenant);
    }

    @Override
    public boolean checkExist(final String criteriaJsonString) {
        return super.checkExist(criteriaJsonString);
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
}
