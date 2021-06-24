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
package fr.gouv.vitamui.iam.internal.server.customer.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.LanguageDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.mongo.CustomSequencesConstants;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.enums.Application;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.common.utils.EntityFactory;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.domain.Group;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.domain.Profile;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The service to init customers.
 */

@Getter
@Setter
public class InitCustomerService {

    public static final String EXTERNAL_PARAM_DEFAULT_ACCESS_CONTRACT_PREFIX =
        "default_ac_";
    public static final String EXTERNAL_PARAMETER_FO_DEFAULT_ACCESS_CONTRACT_NAME_PREFIX =
        "Profil pour la gestion des paramétrages externes ";
    public static final String EXTERNAL_PARAMS_PROFILE_FOR_DEFAULT_ACCESS_CONTRACT =
        "Profil pour la gestion des paramétrages externes ";

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private IdentityProviderRepository identityProviderRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerInternalService internalOwnerService;

    @Autowired
    private TenantInternalService internalTenantService;

    @Autowired
    private UserInternalService internalUserService;

    @Autowired
    private ProfileInternalService internalProfileService;

    @Autowired
    private GroupInternalService internalGroupService;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    @Autowired
    private IamLogbookService iamLogbookService;

    @Autowired
    private OwnerConverter ownerConverter;

    @Autowired
    private IdentityProviderConverter idpConverter;

    @Autowired
    private InitVitamTenantService initVitamTenantService;

    @Autowired
    private CustomerInitConfig customerInitConfig;

    @Autowired
    private ExternalParametersInternalService externalParametersInternalService;


    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(InitCustomerService.class);

    @Transactional()
    public void initCustomer(final String tenantName, final CustomerDto customerDto, final List<OwnerDto> owners) {
        final List<OwnerDto> ownerDtos = owners;
        final List<OwnerDto> createdOwnerDtos = createOwners(ownerDtos, customerDto.getId());

        createIdentityProvider(customerDto.getId(), customerDto.getDefaultEmailDomain());
        ExternalParametersDto fullAccessContract = initFullAccessContractExternalParameter(customerDto.getIdentifier());

        final Tenant proofTenantDto =
            createProofTenant(tenantName, createdOwnerDtos.get(0).getId(), customerDto.getId(), fullAccessContract);
        final List<Profile> createdAdminProfiles = createAdminProfiles(customerDto, proofTenantDto);
        Profile fullAccessContractProfile =
            createExternalParameterProfileForDefaultAccessContract(customerDto, proofTenantDto,
                fullAccessContract.getId());
        createdAdminProfiles.add(fullAccessContractProfile);

        final Group createdAdminGroup = createAdminGroup(customerDto, createdAdminProfiles);
        createAdminUser(customerDto, createdAdminGroup);

        List<Profile> customProfiles = createCustomProfiles(customerDto, proofTenantDto);

        List<Group> customGroups =
            createCustomGroups(customerDto, proofTenantDto, customProfiles);
        createCustomUsers(customerDto, customGroups);

    }

    private ExternalParametersDto initFullAccessContractExternalParameter(String customerIdentifier) {
        ExternalParametersDto fullAccessContract = new ExternalParametersDto();
        fullAccessContract.setIdentifier(EXTERNAL_PARAM_DEFAULT_ACCESS_CONTRACT_PREFIX + customerIdentifier);
        fullAccessContract.setName(EXTERNAL_PARAMETER_FO_DEFAULT_ACCESS_CONTRACT_NAME_PREFIX + customerIdentifier);
        return fullAccessContract;
    }

    private OwnerDto saveOwner(final OwnerDto dto) {
        final Owner o = ownerConverter.convertDtoToEntity(dto);
        o.setId(ownerRepository.generateSuperId());
        o.setIdentifier(generateIdentifier(SequencesConstants.OWNER_IDENTIFIER));
        final Owner ownerSaved = ownerRepository.save(o);
        final OwnerDto ownerDto = ownerConverter.convertEntityToDto(ownerSaved);
        iamLogbookService.createOwnerEventInitCustomer(ownerDto);
        return ownerDto;
    }

    private IdentityProvider saveIdentityProvider(final IdentityProvider idp) {
        idp.setId(identityProviderRepository.generateSuperId());
        iamLogbookService.createIdpEventInitCustomer(idp);
        return identityProviderRepository.save(idp);
    }

    private Tenant saveTenant(final Tenant tenant) {
        tenant.setId(tenantRepository.generateSuperId());
        iamLogbookService.createTenantEventInitCustomer(tenant);
        return tenantRepository.save(tenant);
    }

    private Profile saveProfile(final Profile profile) {
        profile.setId(profileRepository.generateSuperId());
        iamLogbookService.createProfileEvent(profile);
        return profileRepository.save(profile);
    }

    private Group saveGroup(final Group group) {
        group.setId(groupRepository.generateSuperId());
        iamLogbookService.createGroupEvent(group);
        return groupRepository.save(group);
    }

    private UserDto saveUser(final UserDto dto) {
        return internalUserService.create(dto);
    }

    private List<OwnerDto> createOwners(final List<OwnerDto> ownerDtos, final String customerId) {
        final ArrayList<OwnerDto> owners = new ArrayList<>();
        for (final OwnerDto ownerDto : ownerDtos) {
            ownerDto.setCustomerId(customerId);
            owners.add(saveOwner(ownerDto));
        }
        return owners;
    }

    private IdentityProvider createIdentityProvider(final String customerId, final String domain) {
        final IdentityProvider idp = new IdentityProvider();
        idp.setIdentifier(generateIdentifier(SequencesConstants.IDP_IDENTIFIER));
        idp.setCustomerId(customerId);
        idp.setPatterns(Arrays.asList(".*@" + domain));
        idp.setName("default");
        idp.setInternal(true);
        idp.setEnabled(true);
        idp.setTechnicalName(idpConverter.buildTechnicalName("default") + idp.getIdentifier());
        return saveIdentityProvider(idp);
    }

    private Tenant createProofTenant(final String tenantName, final String ownerId, final String customerId,
        final ExternalParametersDto fullAccessContractDto) {
        final Tenant tenant = new Tenant();
        tenant.setCustomerId(customerId);
        tenant.setName(tenantName);
        tenant.setProof(true);
        tenant.setOwnerId(ownerId);
        tenant.setEnabled(true);
        tenant.setReadonly(false);
        tenant.setIdentifier(internalTenantService.getNextSequenceId(SequencesConstants.TENANT_IDENTIFIER, 100));
        Tenant createdTenant = initVitamTenantService.init(tenant, fullAccessContractDto);
        externalParametersInternalService.create(fullAccessContractDto);
        return saveTenant(createdTenant);
    }

    private List<Profile> createCustomProfiles(CustomerDto customerDto, Tenant proofTenant) {
        List<Profile> profiles = new ArrayList<>();
        if (customerInitConfig.getProfiles() != null) {
            customerInitConfig.getProfiles().forEach(p -> {
                Profile profile = EntityFactory.buildProfile(p.getName() + " " + proofTenant.getIdentifier(),
                    generateIdentifier(SequencesConstants.PROFILE_IDENTIFIER),
                    p.getDescription(),
                    true,
                    p.getLevel(),
                    proofTenant.getIdentifier(),
                    p.getAppName(),
                    p.getRoles(),
                    customerDto.getId());
                profiles.add(saveProfile(profile));
            });
        }
        return profiles;
    }

    private Profile createExternalParameterProfileForDefaultAccessContract(CustomerDto customerDto, Tenant proofTenant,
        String externalParameterId) {
        //Adding nex profile for default access contract defined in External Parameter application
        Profile defaultAccessContractProfile = EntityFactory
            .buildProfile(EXTERNAL_PARAMS_PROFILE_FOR_DEFAULT_ACCESS_CONTRACT + " " + proofTenant.getIdentifier(),
                generateIdentifier(SequencesConstants.PROFILE_IDENTIFIER),
                EXTERNAL_PARAMS_PROFILE_FOR_DEFAULT_ACCESS_CONTRACT + " " + proofTenant.getIdentifier(),
                true,
                "",
                proofTenant.getIdentifier(),
                Application.EXTERNAL_PARAMS.name(),
                List.of(ServicesData.ROLE_GET_EXTERNAL_PARAMS),
                customerDto.getId(), externalParameterId);
        return saveProfile(defaultAccessContractProfile);
    }

    private List<Group> createCustomGroups(CustomerDto customerDto, Tenant proofTenant,
        List<Profile> profilesAvailable) {
        List<Group> groups = new ArrayList<>();
        if (customerInitConfig.getProfilesGroups() != null) {
            customerInitConfig.getProfilesGroups().forEach(g -> {
                Group group = EntityFactory.buildGroup(g.getName(),
                    generateIdentifier(SequencesConstants.GROUP_IDENTIFIER),
                    g.getDescription(),
                    true,
                    g.getLevel(), profilesAvailable.stream().filter(p -> g.getProfiles()
                        .contains(p.getName().substring(0, p.getName().indexOf(" " + proofTenant.getIdentifier()))))
                        .collect(Collectors.toList()),
                    customerDto.getId());
                groups.add(saveGroup(group));
            });
        }
        return groups;
    }

    private List<UserDto> createCustomUsers(CustomerDto customerDto, List<Group> groupsAvailable) {
        List<UserDto> users = new ArrayList<>();

        if (customerInitConfig.getUsers() != null) {
            Map<String, Group> groupsByName =
                groupsAvailable.stream().collect(Collectors.toMap(Group::getName, Function.identity()));
            customerInitConfig.getUsers().forEach(u -> {
                UserDto userDto = new UserDto();
                userDto.setOtp(false);
                userDto.setType(UserTypeEnum.GENERIC);
                userDto.setSubrogeable(true);
                userDto.setLastname(u.getLastName());
                userDto.setFirstname(u.getFirstName());
                userDto.setLanguage(getLanguage(customerDto));
                userDto.setGroupId(groupsByName.get(u.getProfilesGroupName()).getId());
                userDto.setLevel(u.getLevel());
                userDto.setCustomerId(customerDto.getId());
                userDto.setEmail(u.getEmailPrefix() + CommonConstants.EMAIL_SEPARATOR +
                    customerDto.getDefaultEmailDomain().replace(".*", ""));
                users.add(saveUser(userDto));
            });
        }
        return users;
    }

    private List<Profile> createAdminProfiles(final CustomerDto customerDto, final Tenant proofTenant) {
        final List<Profile> profiles = new ArrayList<>();

        final Profile userProfile =
            EntityFactory.buildProfile(ServicesData.SERVICE_USERS + " " + proofTenant.getIdentifier(),
                generateIdentifier(SequencesConstants.PROFILE_IDENTIFIER),
                ApiIamInternalConstants.USERS_PROFILE_DESCRIPTION,
                true,
                ApiIamInternalConstants.ADMIN_LEVEL,
                proofTenant.getIdentifier(),
                CommonConstants.USERS_APPLICATIONS_NAME,
                ApiIamInternalConstants.getUsersRoles(),
                customerDto.getId());
        profiles.add(saveProfile(userProfile));

        final Profile groupProfile =
            EntityFactory.buildProfile(ServicesData.SERVICE_GROUPS + " " + proofTenant.getIdentifier(),
                generateIdentifier(SequencesConstants.PROFILE_IDENTIFIER),
                ApiIamInternalConstants.GROUPS_PROFILE_DESCRIPTION,
                true,
                ApiIamInternalConstants.ADMIN_LEVEL,
                proofTenant.getIdentifier(),
                CommonConstants.PROFILES_GROUPS_APPLICATIONS_NAME,
                ApiIamInternalConstants.getGroupsRoles(),
                customerDto.getId());
        profiles.add(saveProfile(groupProfile));

        final Profile profileUserProfileDto =
            EntityFactory.buildProfile(ServicesData.SERVICE_PROFILES + " " + proofTenant.getIdentifier(),
                generateIdentifier(SequencesConstants.PROFILE_IDENTIFIER),
                ApiIamInternalConstants.PROFILE_DESCRIPTION,
                true,
                ApiIamInternalConstants.ADMIN_LEVEL,
                proofTenant.getIdentifier(),
                CommonConstants.PROFILES_APPLICATIONS_NAME,
                ApiIamInternalConstants.getProfilesRoles(),
                customerDto.getId());
        profiles.add(saveProfile(profileUserProfileDto));

        final Profile accountProfile =
            EntityFactory.buildProfile(ServicesData.SERVICE_ACCOUNTS + " " + proofTenant.getIdentifier(),
                generateIdentifier(SequencesConstants.PROFILE_IDENTIFIER),
                ApiIamInternalConstants.ACCOUNT_PROFILE_DESCRIPTION,
                true,
                ApiIamInternalConstants.ADMIN_LEVEL,
                proofTenant.getIdentifier(),
                CommonConstants.ACCOUNTS_APPLICATIONS_NAME,
                ApiIamInternalConstants.getAccountRoles(),
                customerDto.getId());
        profiles.add(saveProfile(accountProfile));

        if (customerInitConfig.getAdminProfiles() != null) {
            customerInitConfig.getAdminProfiles().forEach(p -> {
                Profile profile = EntityFactory.buildProfile(p.getName() + " " + proofTenant.getIdentifier(),
                    generateIdentifier(SequencesConstants.PROFILE_IDENTIFIER),
                    p.getDescription(),
                    true,
                    p.getLevel(),
                    proofTenant.getIdentifier(),
                    p.getAppName(),
                    p.getRoles(),
                    customerDto.getId());
                profiles.add(saveProfile(profile));
            });
        }


        final List<Profile> tenantProfiles =
            internalTenantService.getDefaultProfiles(proofTenant.getCustomerId(), proofTenant.getIdentifier());

        for (final Profile p : tenantProfiles) {
            profiles.add(saveProfile(p));
        }

        return profiles;
    }

    private Group createAdminGroup(final CustomerDto customerDto, final List<Profile> profiles) {
        final Group group = EntityFactory.buildGroup(getAdminClientRootName(customerDto),
            generateIdentifier(SequencesConstants.GROUP_IDENTIFIER),
            ApiIamInternalConstants.ADMIN_CLIENT_ROOT,
            true,
            ApiIamInternalConstants.ADMIN_LEVEL,
            profiles,
            customerDto.getId());
        return saveGroup(group);
    }

    private UserDto createAdminUser(final CustomerDto customerDto, final Group group) {
        final UserDto userDto = new UserDto();
        userDto.setOtp(false);
        userDto.setType(UserTypeEnum.GENERIC);
        userDto.setSubrogeable(true);
        userDto.setLastname(ApiIamInternalConstants.ADMIN_CLIENT_LASTNAME);
        userDto.setFirstname(ApiIamInternalConstants.ADMIN_CLIENT_FIRSTNAME);
        userDto.setLanguage(getLanguage(customerDto));
        userDto.setGroupId(group.getId());
        userDto.setLevel(ApiIamInternalConstants.ADMIN_LEVEL);
        userDto.setCustomerId(customerDto.getId());
        userDto.setEmail(
            ApiIamInternalConstants.ADMIN_CLIENT_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR +
                customerDto.getDefaultEmailDomain().replace(".*", ""));
        return saveUser(userDto);
    }

    private String getLanguage(final CustomerDto customerDto) {
        return (customerDto.getLanguage() == null ? LanguageDto.FRENCH : customerDto.getLanguage()).toString();
    }

    public String getAdminClientRootName(final CustomerDto customerDto) {
        return ApiIamInternalConstants.ADMIN_CLIENT_ROOT + " " + customerDto.getCode();
    }

    protected String generateIdentifier(final String sequenceName) {
        final Optional<CustomSequence> customSequence =
            sequenceRepository.incrementSequence(sequenceName, CustomSequencesConstants.SEQUENCE_INCREMENT_VALUE);
        customSequence
            .orElseThrow(() -> new InternalServerException("Sequence with name : " + sequenceName + " didn't exist"));
        return String.valueOf(customSequence.get().getSequence());
    }
}
