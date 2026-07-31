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
package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.cas.HrdEntryDto;
import fr.gouv.vitamui.iam.common.dto.cas.JitProvisionRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.PasswordPolicyDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateResponseDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.customer.service.CustomerService;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.provisioning.service.ProvisioningService;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.server.subrogation.service.SubrogationService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import fr.gouv.vitamui.iam.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.server.token.domain.Token;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Specific CAS service.
 */
@Getter
@Setter
public class CasService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";
    private static final String USER_CONFLICT = "Could not select the right user: ";

    private static final String ID = "_id";

    private static final String NB_FAILED_ATTEMPTS = "nbFailedAttempts";

    private static final String STATUS = "status";

    private static final String LAST_CONNECTION = "lastConnection";

    private static final String TOKEN_PREFIX = "TOK";

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SubrogationService subrogationService;

    @Autowired
    private SubrogationRepository subrogationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private IamLogbookService iamLogbookService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private IdentityProviderRepository identityProviderRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ProvisioningService provisioningService;

    @Value("${token.ttl}")
    @NotNull
    @Setter
    private Integer tokenTtl;

    @Value("${subrogaton.token.ttl}")
    @NotNull
    @Setter
    private Integer subrogationTokenTtl;

    @Value("${api.token.ttl}")
    @NotNull
    @Setter
    private Integer apiTokenTtl;

    @Value("${login.attempts.time.interval}")
    @NotNull
    private Integer timeIntervalForLoginAttempts;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private PasswordConfiguration passwordConfiguration;

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(CasService.class);

    /**
     * Generate a unique ticket ID with the given prefix.
     * Uses UUID for guaranteed uniqueness.
     *
     * @param prefix The prefix for the ticket ID
     * @return A unique ticket ID in the format: prefix-uuid
     */
    private static String generateUniqueTicketId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString();
    }

    public CasService() {}

    /**
     * Flattens {@code PasswordConfiguration} into a SPA-friendly {@link PasswordPolicyDto}. Extracts
     * the human messages from the active profile's constraints (defaults + customs) so the SPA can
     * render them as-is without having to know the internal ANSSI/custom layout.
     */
    public PasswordPolicyDto getPasswordPolicy() {
        java.util.List<String> messages = new java.util.ArrayList<>();
        if (passwordConfiguration != null && passwordConfiguration.getConstraints() != null) {
            var constraints = passwordConfiguration.getConstraints();
            if (constraints.getDefaults() != null) {
                constraints
                    .getDefaults()
                    .values()
                    .forEach(d -> {
                        if (d.getMessages() != null) messages.addAll(d.getMessages());
                        if (d.getSpecialChars() != null && d.getSpecialChars().getMessages() != null) {
                            messages.addAll(d.getSpecialChars().getMessages());
                        }
                    });
            }
            if (constraints.getCustoms() != null) {
                constraints
                    .getCustoms()
                    .values()
                    .forEach(c -> {
                        if (c.getMessages() != null) messages.addAll(c.getMessages());
                    });
            }
        }
        return new PasswordPolicyDto(
            passwordConfiguration != null ? passwordConfiguration.getLength() : null,
            passwordConfiguration != null ? passwordConfiguration.getProfile() : null,
            passwordConfiguration != null ? passwordConfiguration.getMaxOldPassword() : null,
            messages
        );
    }

    @Transactional
    public void updatePassword(final String email, final String rawPassword, final String customerId) {
        Optional<Customer> optCustomer = customerRepository.findById(customerId);
        if (optCustomer.isEmpty()) {
            throw new ApplicationServerException("Unable to update password : customer not found");
        }
        User user = findUserByEmailAndCustomerId(email, customerId);
        if (UserTypeEnum.NOMINATIVE != user.getType()) {
            throw new InvalidAuthenticationException("User unavailable: " + email);
        }
        checkStatus(user.getStatus(), user.getEmail());
        final Customer customer = optCustomer.get();

        final List<String> oldPasswords = user.getOldPasswords();
        if (oldPasswords != null && !oldPasswords.isEmpty()) {
            for (final String oldPassword : oldPasswords) {
                if (passwordEncoder.matches(rawPassword, oldPassword)) {
                    throw new ConflictException("The given password has already been used in the past");
                }
            }
        }

        final String encodedPassword = passwordEncoder.encode(rawPassword);
        userService.saveCurrentPasswordInOldPasswords(
            user,
            encodedPassword,
            (passwordConfiguration != null && passwordConfiguration.getMaxOldPassword() != null)
                ? passwordConfiguration.getMaxOldPassword()
                : UserService.MAX_OLD_PASSWORDS
        );

        final String existingPassword = user.getPassword();

        user.setPassword(encodedPassword);
        final OffsetDateTime nowPlusPasswordRevocationDelay = OffsetDateTime.now()
            .plusMonths(customer.getPasswordRevocationDelay());
        user.setPasswordExpirationDate(nowPlusPasswordRevocationDelay);

        userRepository.save(user);

        if (StringUtils.isEmpty(existingPassword)) {
            iamLogbookService.createPasswordEvent(user);
        } else {
            iamLogbookService.updatePasswordEvent(user);
        }
    }

    @Transactional
    public void updateNbFailedAttempsPlusLastConnectionAndStatus(
        final User user,
        final int nbFailedAttempts,
        final UserStatusEnum oldStatus
    ) {
        final UserStatusEnum newStatus = user.getStatus();
        final Query query = new Query(Criteria.where(ID).is(user.getId()));
        final Update update = Update.update(NB_FAILED_ATTEMPTS, nbFailedAttempts)
            .set(LAST_CONNECTION, OffsetDateTime.now())
            .set(STATUS, newStatus);
        mongoTemplate.updateFirst(query, update, MongoDbCollections.USERS);

        if (newStatus == UserStatusEnum.BLOCKED) {
            iamLogbookService.blockUserEvent(user, oldStatus, Duration.ofMinutes(timeIntervalForLoginAttempts));
        }
    }

    public User findUserByEmailAndCustomerId(final String email, String customerId) {
        final User user = userRepository.findByEmailIgnoreCaseAndCustomerId(email, customerId);
        if (user == null) {
            throw new NotFoundException(USER_NOT_FOUND_MESSAGE + email);
        } else if (UserTypeEnum.NOMINATIVE != user.getType()) {
            throw new InvalidAuthenticationException("User unavailable: " + email);
        }
        checkStatus(user.getStatus(), user.getEmail());
        return user;
    }

    private void checkStatus(final UserStatusEnum userStatus, final String userEmail) {
        final boolean badStatus = UserStatusEnum.ENABLED != userStatus && UserStatusEnum.BLOCKED != userStatus;
        if (badStatus) {
            throw new InvalidFormatException("User unavailable: " + userEmail);
        }
    }

    @Transactional
    public List<UserDto> getUsersByEmail(final String email, final String optEmbedded) {
        boolean loadFullProfile = checkEmbeddedOption(optEmbedded, CommonConstants.AUTH_TOKEN_PARAMETER);
        boolean isSubrogation = checkEmbeddedOption(optEmbedded, CommonConstants.SURROGATION_PARAMETER);
        boolean isApi = checkEmbeddedOption(optEmbedded, CommonConstants.API_PARAMETER);

        final List<UserDto> usersDto = userService.findUsersByEmail(email);

        return usersDto
            .stream()
            .map(user -> loadFullUserProfileIfRequired(user, loadFullProfile, isSubrogation, isApi))
            .collect(Collectors.toList());
    }

    @Transactional
    public UserDto getUserByEmailAndCustomerId(final String email, final String customerId, final String optEmbedded) {
        boolean loadFullProfile = checkEmbeddedOption(optEmbedded, CommonConstants.AUTH_TOKEN_PARAMETER);
        boolean isSubrogation = checkEmbeddedOption(optEmbedded, CommonConstants.SURROGATION_PARAMETER);
        boolean isApi = checkEmbeddedOption(optEmbedded, CommonConstants.API_PARAMETER);

        UserDto userDto = userService.findUserByEmailAndCustomerId(email, customerId);
        if (userDto == null) {
            throw new NotFoundException(USER_NOT_FOUND_MESSAGE + email);
        }
        checkStatus(userDto.getStatus(), userDto.getEmail());

        return loadFullUserProfileIfRequired(userDto, loadFullProfile, isSubrogation, isApi);
    }

    private boolean checkEmbeddedOption(String optEmbedded, String authTokenParameter) {
        if (optEmbedded == null) {
            return false;
        }
        final Set<String> values = splitIntoValues(optEmbedded);
        return values.contains(authTokenParameter);
    }

    private UserDto loadFullUserProfileIfRequired(
        UserDto user,
        boolean loadFullProfile,
        boolean subrogation,
        boolean api
    ) {
        if (!loadFullProfile) {
            return user;
        }
        final AuthUserDto authUserDto = userService.loadGroupAndProfiles(user);
        userService.addBasicCustomerAndProofTenantIdentifierInformation(authUserDto);
        userService.addTenantsByAppInformation(authUserDto);
        generateAndAddAuthToken(authUserDto, subrogation, api);
        createEventsSubrogation(user, subrogation);
        return authUserDto;
    }

    /**
     * Method to retrieve the user information
     *
     * @param loginEmail      email of the user
     * @param loginCustomerId The customerId of the user
     * @param idp             can be null
     * @param userIdentifier  can be null
     * @param optEmbedded
     * @return
     */
    @Transactional
    public UserDto getUser(
        String loginEmail,
        final String loginCustomerId,
        final String idp,
        final String userIdentifier,
        final String optEmbedded
    ) {
        // if the user depends on an external idp
        if (StringUtils.isNotBlank(idp)) {
            try {
                Optional<ProvidedUserDto> providedUser =
                    this.provisionUser(loginEmail, loginCustomerId, idp, userIdentifier);
                if (loginEmail.isBlank() && providedUser.isPresent()) {
                    loginEmail = providedUser.get().getEmail();
                }
            } catch (NotFoundException e) {
                // No ProvisioningClient configured for this IdP (typical for the SAS POC where
                // provisioning is handled by /cas/users/jit instead). Fall through to the local
                // lookup: if the user was already JIT-created it exists in Mongo, otherwise
                // getUserByEmailAndCustomerId will throw 404 and SAS will trigger a fresh JIT.
                LOGGER.debug(
                    "No provisioning client for IdP {} — falling back to local user lookup: {}",
                    idp,
                    e.getMessage()
                );
            }
        }

        return getUserByEmailAndCustomerId(loginEmail, loginCustomerId, optEmbedded);
    }

    /**
     * Method to perform auto provisioning
     *
     * @param loginEmail
     * @param loginCustomerId
     * @param idp
     * @param userIdentifier
     */
    public Optional<ProvidedUserDto> provisionUser(
        String loginEmail,
        String loginCustomerId,
        final String idp,
        final String userIdentifier
    ) {
        final IdentityProviderDto identityProvider = identityProviderService.getOne(idp);

        Assert.isTrue(
            loginCustomerId.equals(identityProvider.getCustomerId()),
            "CustomerId mismatch. LoginCustomerId : " +
            loginCustomerId +
            ", IDP customerId: " +
            identityProvider.getCustomerId()
        );

        // Do nothing is autoProvisioning is disabled
        if (!identityProvider.isAutoProvisioningEnabled()) {
            return Optional.empty();
        }

        Optional<ProvidedUserDto> providedUser = Optional.empty();

        if (StringUtils.isBlank(loginEmail)) {
            providedUser = Optional.of(getProvidedUser(loginEmail, loginCustomerId, idp, userIdentifier, null));
            loginEmail = providedUser.get().getEmail();
        }

        final boolean userExist = userRepository.existsByEmailIgnoreCaseAndCustomerId(loginEmail, loginCustomerId);
        // Try to update user
        if (userExist) {
            final UserDto user = userService.findUserByEmailAndCustomerId(loginEmail, loginCustomerId);
            if (user.isAutoProvisioningEnabled()) {
                updateUser(user, getProvidedUser(loginEmail, loginCustomerId, idp, userIdentifier, user.getGroupId()));
            }
        }
        // Try to create a new user
        else {
            if (providedUser.isEmpty()) {
                providedUser = Optional.of(getProvidedUser(loginEmail, loginCustomerId, idp, userIdentifier, null));
            }
            createNewUser(loginEmail, providedUser.get());
        }

        return providedUser;
    }

    private ProvidedUserDto getProvidedUser(
        String email,
        String loginCustomerId,
        String idp,
        String userIdentifier,
        String groupId
    ) {
        ProvidedUserDto userProvidedInfo;
        userProvidedInfo = provisioningService.getUserInformation(
            idp,
            email,
            loginCustomerId,
            groupId,
            null,
            userIdentifier
        );

        if (Objects.isNull(userProvidedInfo)) {
            throw new NotFoundException(
                "The following provided user does not exist: Email:%s, technicalId:%s, groupId:%s, idp:%s, customerId:%s".formatted(
                        email,
                        userIdentifier,
                        groupId,
                        idp,
                        loginCustomerId
                    )
            );
        }

        return userProvidedInfo;
    }

    private void createNewUser(final String email, final ProvidedUserDto providedUserInfo) {
        final UserDto user = new UserDto();
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setSubrogeable(true);
        user.setAutoProvisioningEnabled(true);

        user.setFirstname(providedUserInfo.getFirstname());
        user.setLastname(providedUserInfo.getLastname());
        user.setEmail(email);
        user.setAddress(providedUserInfo.getAddress());
        user.setInternalCode(providedUserInfo.getInternalCode());
        user.setSiteCode(providedUserInfo.getSiteCode());
        GroupDto groupDto = groupService.getGroupByUnitInternal(providedUserInfo.getUnit());
        user.setGroupId(groupDto.getId());
        user.setCustomerId(groupDto.getCustomerId());

        final Customer customer = customerRepository
            .findById(user.getCustomerId())
            .orElseThrow(() -> new NotFoundException("Cannot find customer : %s".formatted(user.getCustomerId())));
        user.setUserInfoId(createUserInfo(customer.getLanguage()).getId());

        userService.create(user);
    }

    private UserInfoDto createUserInfo(final String language) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setLanguage(language);
        return userInfoService.create(userInfoDto);
    }

    private void updateUser(final UserDto userDto, final ProvidedUserDto userProvidedInfo) {
        final Map<String, Object> userUpdate = new HashMap<>();
        updateUserMandatoryInformation(userDto, userProvidedInfo, userUpdate);
        updateUserOptionalInformation(userDto, userProvidedInfo, userUpdate);
        if (!userUpdate.isEmpty()) {
            userUpdate.put("id", userDto.getId());
            userUpdate.put("customerId", userDto.getCustomerId());
            userService.patch(userUpdate);
        }
    }

    private void updateUserOptionalInformation(
        final UserDto userDto,
        final ProvidedUserDto userInfo,
        final Map<String, Object> userUpdate
    ) {
        if (
            userInfo.getInternalCode() != null &&
            !StringUtils.equals(userInfo.getInternalCode(), userDto.getInternalCode())
        ) {
            userUpdate.put("internalCode", userInfo.getInternalCode());
        }
        if (userInfo.getSiteCode() != null && !StringUtils.equals(userInfo.getSiteCode(), userDto.getSiteCode())) {
            userUpdate.put("siteCode", userInfo.getSiteCode());
        }
        updateUserAddress(userDto, userInfo, userUpdate);
    }

    private void updateUserMandatoryInformation(
        final UserDto userDto,
        final ProvidedUserDto userInfo,
        final Map<String, Object> userUpdate
    ) {
        if (!StringUtils.equals(userDto.getFirstname(), userInfo.getFirstname())) {
            userUpdate.put("firstname", userInfo.getFirstname());
        }
        if (!StringUtils.equals(userDto.getLastname(), userInfo.getLastname())) {
            userUpdate.put("lastname", userInfo.getLastname());
        }
        updateUserGroup(userDto, userInfo, userUpdate);
    }

    private void updateUserGroup(
        final UserDto userDto,
        final ProvidedUserDto userInfo,
        final Map<String, Object> userUpdate
    ) {
        final GroupDto group = groupService.getGroupByUnitInternal(userInfo.getUnit());
        if (!StringUtils.equals(userDto.getGroupId(), group.getId())) {
            userUpdate.put("groupId", group.getId());
        }
    }

    private void updateUserAddress(
        final UserDto userDto,
        final ProvidedUserDto userInfo,
        final Map<String, Object> userUpdate
    ) {
        if (userInfo.getAddress() != null) {
            final Map<String, Object> updatedAddress = new HashMap<>();
            if (
                userInfo.getAddress().getStreet() != null &&
                (userDto.getAddress() == null ||
                    !StringUtils.equals(userInfo.getAddress().getStreet(), userDto.getAddress().getStreet()))
            ) {
                updatedAddress.put("street", userInfo.getAddress().getStreet());
            }
            if (
                userInfo.getAddress().getZipCode() != null &&
                (userDto.getAddress() == null ||
                    !StringUtils.equals(userInfo.getAddress().getZipCode(), userDto.getAddress().getZipCode()))
            ) {
                updatedAddress.put("zipCode", userInfo.getAddress().getZipCode());
            }
            if (
                userInfo.getAddress().getCity() != null &&
                (userDto.getAddress() == null ||
                    !StringUtils.equals(userInfo.getAddress().getCity(), userDto.getAddress().getCity()))
            ) {
                updatedAddress.put("city", userInfo.getAddress().getCity());
            }

            if (
                userInfo.getAddress().getCountry() != null &&
                (userDto.getAddress() == null ||
                    !StringUtils.equals(userInfo.getAddress().getCountry(), userDto.getAddress().getCountry()))
            ) {
                updatedAddress.put("country", userInfo.getAddress().getCountry());
            }

            if (!updatedAddress.isEmpty()) {
                userUpdate.put("address", updatedAddress);
            }
        }
    }

    private void createEventsSubrogation(final UserDto surrogate, final boolean isSubrogation) {
        if (isSubrogation) {
            final Subrogation subro = subrogationRepository.findOneBySurrogateAndSurrogateCustomerId(
                surrogate.getEmail(),
                surrogate.getCustomerId()
            );
            final EventType type;
            if (surrogate.getType().equals(UserTypeEnum.GENERIC)) {
                type = EventType.EXT_VITAMUI_START_SURROGATE_GENERIC;
            } else {
                type = EventType.EXT_VITAMUI_START_SURROGATE_USER;
            }
            iamLogbookService.subrogation(subro, type);
        }
    }

    protected Set<String> splitIntoValues(final String embedded) {
        final Set<String> set = new HashSet<>();
        if (embedded != null) {
            final String[] pairs = embedded.split(",");
            for (final String pair : pairs) {
                set.add(pair);
            }
        }
        return set;
    }

    private void generateAndAddAuthToken(final AuthUserDto user, final boolean isSubrogation, final boolean isApi) {
        final int ttlInMinutes = resolveTokenTtl(isSubrogation, isApi, user.getType() == UserTypeEnum.GENERIC);
        final String tokenId = persistToken(user.getId(), isSubrogation, ttlInMinutes);
        user.setLastConnection(OffsetDateTime.now());
        user.setAuthToken(tokenId);
        final Query query = new Query(Criteria.where(ID).is(user.getId()));
        final Update update = Update.update(LAST_CONNECTION, user.getLastConnection());
        mongoTemplate.updateFirst(query, update, MongoDbCollections.USERS);
    }

    /**
     * Invalidates every opaque auth token pointing to the given user id — used at the beginning of a
     * subrogation flow to force other apps still holding the super-user's token to re-authenticate
     * (they will get a fresh subrogated token via SSO instead of continuing to see the super-user).
     *
     * @return the number of tokens deleted
     */
    public long invalidateTokensOfUser(final String userId) {
        Assert.hasText(userId, "userId must not be empty");
        return tokenRepository.deleteByRefId(userId);
    }

    /**
     * Create an opaque auth token pointing to the given user, persisted in the {@code tokens} collection.
     * Used by external issuers (e.g. Spring Authorization Server POC) that only need to mint a token
     * without updating the user's lastConnection.
     */
    public String createAuthToken(final String userId, final boolean isSubrogation, final boolean isApi) {
        Assert.hasText(userId, "userId must not be empty");
        final User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE + userId));
        checkStatus(user.getStatus(), user.getEmail());
        final int ttlInMinutes = resolveTokenTtl(isSubrogation, isApi, user.getType() == UserTypeEnum.GENERIC);
        return persistToken(userId, isSubrogation, ttlInMinutes);
    }

    private int resolveTokenTtl(boolean isSubrogation, boolean isApi, boolean userIsGeneric) {
        if (isSubrogation && userIsGeneric) {
            return subrogationTokenTtl;
        }
        if (isApi) {
            return apiTokenTtl;
        }
        return tokenTtl;
    }

    /**
     * Creates a new vitam-ui user from the identity claims received from an external OIDC/SAML IdP.
     * Requires the referenced {@link IdentityProvider} to have {@code autoProvisioningEnabled=true} and
     * a {@code defaultGroupId} set — otherwise a {@link fr.gouv.vitamui.commons.api.exception.BadRequestException}
     * is thrown.
     *
     * <p>The user is created {@code status=ENABLED}, {@code type=NOMINATIVE}. The internal
     * {@code identifier} is generated by VitamUI's user sequence (not carried from the external
     * IdP); the link with the external subject relies on {@code email + customerId} uniqueness.
     */
    public UserDto jitProvisionUser(final JitProvisionRequestDto request) {
        Assert.notNull(request, "request must not be null");
        Assert.hasText(request.getEmail(), "email must not be empty");
        Assert.hasText(request.getCustomerId(), "customerId must not be empty");
        Assert.hasText(request.getIdentityProviderId(), "identityProviderId must not be empty");
        Assert.hasText(request.getSubjectId(), "subjectId must not be empty");

        final IdentityProvider idp = identityProviderRepository
            .findById(request.getIdentityProviderId())
            .orElseThrow(
                () -> new NotFoundException("Identity provider not found: " + request.getIdentityProviderId())
            );
        if (!Boolean.TRUE.equals(idp.getEnabled()) || !idp.isAutoProvisioningEnabled()) {
            throw ApiErrorGenerator.getBadRequestException(
                "Identity provider " + idp.getId() + " has no auto-provisioning enabled"
            );
        }
        if (StringUtils.isBlank(idp.getDefaultGroupId())) {
            throw ApiErrorGenerator.getBadRequestException(
                "Identity provider " + idp.getId() + " has no defaultGroupId set — JIT cannot pick a group"
            );
        }
        if (userRepository.findByEmailIgnoreCaseAndCustomerId(request.getEmail(), request.getCustomerId()) != null) {
            throw ApiErrorGenerator.getBadRequestException(
                "User already exists for email " + request.getEmail() + " in customer " + request.getCustomerId()
            );
        }

        final Customer customer = customerRepository
            .findById(request.getCustomerId())
            .orElseThrow(() -> new NotFoundException("Cannot find customer : " + request.getCustomerId()));

        final UserDto dto = new UserDto();
        dto.setEmail(request.getEmail());
        dto.setCustomerId(request.getCustomerId());
        dto.setGroupId(idp.getDefaultGroupId());
        // identifier is generated by UserService.beforeCreate via SequencesConstants.USER_IDENTIFIER;
        // must be null on create — the external subjectId is not carried in this field.
        dto.setFirstname(request.getFirstname() != null ? request.getFirstname() : "");
        dto.setLastname(request.getLastname() != null ? request.getLastname() : "");
        dto.setStatus(UserStatusEnum.ENABLED);
        dto.setType(fr.gouv.vitamui.commons.api.enums.UserTypeEnum.NOMINATIVE);
        dto.setLevel("");
        dto.setSubrogeable(false);
        dto.setOtp(false);
        dto.setAutoProvisioningEnabled(true);

        dto.setUserInfoId(createUserInfo(customer.getLanguage()).getId());
        final UserDto created = userService.create(dto);
        LOGGER.info(
            "JIT-provisioned user id={} email={} customer={} from idp={} into group={}",
            created.getId(),
            created.getEmail(),
            created.getCustomerId(),
            request.getIdentityProviderId(),
            idp.getDefaultGroupId()
        );
        return created;
    }

    /**
     * Returns the complete {@link IdentityProviderDto} for a given identity provider id — including the
     * sensitive fields (client_secret for OIDC, keystore / keys for SAML) that the SAS POC needs to
     * assemble a {@code ClientRegistration} or {@code RelyingPartyRegistration} at runtime.
     *
     * <p>Callable only by the SAS peer via mTLS ({@code @Secured(ROLE_SYSTEM_SAS)} on the controller).
     * The secrets returned in clear should still be encrypted at rest — tracked as Phase 3 debt.
     */
    public IdentityProviderDto getIdentityProviderById(final String id) {
        Assert.hasText(id, "id must not be empty");
        final IdentityProvider entity = identityProviderRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Identity provider not found: " + id));
        final IdentityProviderDto dto = new IdentityProviderDto();
        org.springframework.beans.BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * Mini Home Realm Discovery: resolves an email to the list of (customer, identity provider) tuples
     * that match through the identity provider {@code patterns} regex list.
     * Mirrors the resolution logic used today by {@code IdentityProviderHelper} inside the CAS webflow,
     * but exposed as a stateless server-side query for external issuers (e.g. Spring Authorization Server POC).
     */
    public List<HrdEntryDto> resolveHrdEntries(final String email) {
        Assert.hasText(email, "email must not be empty");
        final Iterable<IdentityProvider> providers = identityProviderRepository.findAll();
        final List<IdentityProvider> matches = java.util.stream.StreamSupport.stream(providers.spliterator(), false)
            .filter(p -> p.getPatterns() != null && !p.getPatterns().isEmpty())
            .filter(
                p ->
                    p
                        .getPatterns()
                        .stream()
                        .anyMatch(
                            pattern ->
                                java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE)
                                    .matcher(email)
                                    .matches()
                        )
            )
            .collect(Collectors.toList());

        if (matches.isEmpty()) {
            return List.of();
        }

        final Set<String> customerIds = matches
            .stream()
            .map(IdentityProvider::getCustomerId)
            .collect(Collectors.toSet());
        final Map<String, String> customerNamesById = new HashMap<>();
        customerRepository
            .findAllById(customerIds)
            .forEach(customer -> customerNamesById.put(customer.getId(), customer.getName()));

        return matches
            .stream()
            .map(
                p ->
                    new HrdEntryDto(
                        p.getCustomerId(),
                        customerNamesById.getOrDefault(p.getCustomerId(), p.getCustomerId()),
                        p.getId(),
                        p.getName(),
                        Boolean.TRUE.equals(p.getInternal()),
                        p.getProtocoleType()
                    )
            )
            .collect(Collectors.toList());
    }

    /**
     * Validates that an ACCEPTED {@link Subrogation} exists between the given super-user and surrogate
     * within the provided customer contexts, and resolves both user identifiers.
     * Used by the auth-server (SAS POC) at the {@code /login/subrogate} step.
     *
     * @throws NotFoundException if no matching subrogation exists or its status is not ACCEPTED, or if
     *                            either user cannot be resolved.
     */
    public SubrogationValidateResponseDto validateSubrogation(final SubrogationValidateRequestDto request) {
        Assert.notNull(request, "request must not be null");
        Optional<Subrogation> subro =
            subrogationRepository.findBySuperUserAndSuperUserCustomerIdAndSurrogateAndSurrogateCustomerId(
                request.getSuperUserEmail(),
                request.getSuperUserCustomerId(),
                request.getSurrogateEmail(),
                request.getSurrogateCustomerId()
            );
        if (subro.isEmpty() || subro.get().getStatus() != SubrogationStatusEnum.ACCEPTED) {
            throw new NotFoundException("No ACCEPTED subrogation between the given super-user and surrogate");
        }
        // Reject subrogations whose date is in the past — the Mongo TTL index should have purged them but
        // in practice we see stale entries lingering (TTL runs every 60s, some deployments have it disabled).
        final Date now = new Date();
        if (subro.get().getDate() != null && subro.get().getDate().before(now)) {
            throw new NotFoundException(
                "Subrogation between " +
                request.getSuperUserEmail() +
                " and " +
                request.getSurrogateEmail() +
                " has expired at " +
                subro.get().getDate()
            );
        }
        final User surrogate = userRepository.findByEmailIgnoreCaseAndCustomerId(
            request.getSurrogateEmail(),
            request.getSurrogateCustomerId()
        );
        if (surrogate == null) {
            throw new NotFoundException("Surrogate user not found: " + request.getSurrogateEmail());
        }
        final User superUser = userRepository.findByEmailIgnoreCaseAndCustomerId(
            request.getSuperUserEmail(),
            request.getSuperUserCustomerId()
        );
        if (superUser == null) {
            throw new NotFoundException("Super-user not found: " + request.getSuperUserEmail());
        }
        // Wipe every active TOK held by the super-user so that other apps (portal, ingest, …) still open
        // in another tab lose their token and, on their next API call, get a 401 → re-authenticate via
        // SSO → receive a fresh surrogated token. Without this, those apps keep showing the super-user.
        final long deleted = tokenRepository.deleteByRefId(superUser.getId());
        LOGGER.info(
            "Subrogation validate: invalidated {} TOK(s) of super-user {} to propagate the switch",
            deleted,
            request.getSuperUserEmail()
        );
        return new SubrogationValidateResponseDto(superUser.getId(), surrogate.getId());
    }

    private String persistToken(final String userId, final boolean isSubrogation, final int ttlInMinutes) {
        final Token token = new Token();
        token.setRefId(userId);
        final Date currentDate = new Date();
        token.setCreatedDate(currentDate);
        token.setUpdatedDate(DateUtils.addMinutes(currentDate, ttlInMinutes));
        token.setId(generateUniqueTicketId(TOKEN_PREFIX));
        token.setSurrogation(isSubrogation);
        tokenRepository.save(token);
        return token.getId();
    }

    public List<SubrogationDto> getSubrogationsBySuperUser(final String superUser, String superUserCustomerId) {
        final List<Subrogation> subrogations = subrogationRepository.findBySuperUserAndSuperUserCustomerId(
            superUser,
            superUserCustomerId
        );
        final List<SubrogationDto> dtos = new ArrayList<>();
        subrogations.forEach(subrogation -> dtos.add(convertFromSubrogationToDto(subrogation)));
        return dtos;
    }

    protected final SubrogationDto convertFromSubrogationToDto(final Subrogation entity) {
        if (entity != null) {
            return subrogationService.internalConvertFromEntityToDto(entity);
        } else {
            return null;
        }
    }

    @Transactional
    public void deleteSubrogationBySuperUserAndSurrogate(
        final String superUser,
        final String superUserCustomerId,
        final String surrogate,
        final String surrogateCustomerId
    ) {
        if (StringUtils.isAnyBlank(superUser, superUserCustomerId, surrogate, surrogateCustomerId)) {
            throw ApiErrorGenerator.getBadRequestException(
                "superUser, superUserCustomerId, surrogate and surrogateCustomerId must be filled"
            );
        }
        final Optional<Subrogation> subro =
            subrogationRepository.findBySuperUserAndSuperUserCustomerIdAndSurrogateAndSurrogateCustomerId(
                superUser,
                superUserCustomerId,
                surrogate,
                surrogateCustomerId
            );
        if (subro.isPresent()) {
            final Subrogation subrogation = subro.get();
            iamLogbookService.subrogation(subrogation, EventType.EXT_VITAMUI_LOGOUT_SURROGATE);
            subrogationRepository.deleteById(subrogation.getId());
        }
    }

    public PrincipalFromToken removeTokenAndGetPrincipal(final String authToken) {
        final Optional<Token> optToken = tokenRepository.findById(authToken);
        if (optToken.isPresent()) {
            tokenRepository.deleteById(authToken);
            final Token token = optToken.get();
            final String userId = token.getRefId();
            final Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                return new PrincipalFromToken(optionalUser.get().getEmail(), optionalUser.get().getCustomerId());
            }
        }
        return null;
    }

    public List<CustomerDto> getCustomersByIds(List<String> customerIds) {
        return customerService.getAllById(customerIds);
    }

    @Getter
    @AllArgsConstructor
    public static class PrincipalFromToken {

        private final String email;
        private final String customerId;
    }
}
