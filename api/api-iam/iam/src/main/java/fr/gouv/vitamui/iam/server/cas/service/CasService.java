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

import com.fasterxml.jackson.core.JsonProcessingException;
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
import fr.gouv.vitamui.commons.utils.JsonUtils;
import fr.gouv.vitamui.iam.auth.contract.HrdEntryDto;
import fr.gouv.vitamui.iam.auth.contract.PasswordPolicyDto;
import fr.gouv.vitamui.iam.auth.contract.PrincipalAttributesRequestDto;
import fr.gouv.vitamui.iam.auth.contract.SubrogationValidateRequestDto;
import fr.gouv.vitamui.iam.auth.contract.SubrogationValidateResponseDto;
import fr.gouv.vitamui.iam.auth.contract.UserPrincipalAttributes;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
            Optional<ProvidedUserDto> providedUser =
                this.provisionUser(loginEmail, loginCustomerId, idp, userIdentifier);
            if (loginEmail.isBlank() && providedUser.isPresent()) {
                loginEmail = providedUser.get().getEmail();
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
        final Token token = new Token();
        token.setRefId(user.getId());
        final int ttlInMinutes;
        if (isSubrogation && user.getType() == UserTypeEnum.GENERIC) {
            ttlInMinutes = subrogationTokenTtl;
        } else if (isApi) {
            ttlInMinutes = apiTokenTtl;
        } else {
            ttlInMinutes = tokenTtl;
        }
        Date currentDate = new Date();
        token.setCreatedDate(currentDate);
        final Date nowPlusXMinutes = DateUtils.addMinutes(currentDate, ttlInMinutes);
        token.setUpdatedDate(nowPlusXMinutes);
        token.setId(generateUniqueTicketId(TOKEN_PREFIX));
        token.setSurrogation(isSubrogation);
        tokenRepository.save(token);
        user.setLastConnection(OffsetDateTime.now());
        user.setAuthToken(token.getId());
        final Query query = new Query(Criteria.where(ID).is(user.getId()));
        final Update update = Update.update(LAST_CONNECTION, user.getLastConnection());
        mongoTemplate.updateFirst(query, update, MongoDbCollections.USERS);
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

    /**
     * The authentication attributes of a user, ready to be carried as-is by the token.
     *
     * The authentication server builds this map itself today, which forces it to know the sixty-odd
     * attribute names of {@link CommonConstants} and how each one derives from the user model. Adding an
     * attribute here used to mean changing the authentication server as well.
     *
     * Every value is a string, booleans and dates included. That is not a loss of fidelity: it is the
     * form in which they already reach the applications, since {@code AuthUserDto.buildFromAttributes}
     * reads them back with {@code Boolean.parseBoolean((String) value)} or
     * {@code OffsetDateTime.parse((String) value)}. Composite attributes are serialised to JSON with the
     * very {@link JsonUtils} that {@code CasJsonWrapper.toString()} used, so the transmitted string is
     * identical.
     *
     * An attribute whose value is missing is omitted rather than set to {@code null}: the reader
     * switches on the keys that are present, and an absent key is equivalent to a null one there.
     */
    public Map<String, List<String>> buildPrincipalAttributes(final PrincipalAttributesRequestDto request) {
        Assert.notNull(request, "request must not be null");

        final boolean subrogation = StringUtils.isNotBlank(request.getSuperUserEmail());

        // The authentication token is always requested; subrogation and non-browser calls additionally
        // require their own block, exactly as the authentication server's resolver does today.
        String embedded = CommonConstants.AUTH_TOKEN_PARAMETER;
        if (subrogation) {
            embedded += "," + CommonConstants.SURROGATION_PARAMETER;
        } else if (request.isApiContext()) {
            embedded += "," + CommonConstants.API_PARAMETER;
        }

        final UserDto user = getUser(
            request.getLoginEmail(),
            request.getLoginCustomerId(),
            request.getIdentityProviderId(),
            request.getUserIdentifier(),
            embedded
        );
        if (user == null) {
            throw new NotFoundException(USER_NOT_FOUND_MESSAGE + request.getLoginEmail());
        }

        UserDto superUser = null;
        if (subrogation) {
            superUser = getUserByEmailAndCustomerId(
                request.getSuperUserEmail(),
                request.getSuperUserCustomerId(),
                null
            );
            if (superUser == null) {
                throw new NotFoundException(USER_NOT_FOUND_MESSAGE + request.getSuperUserEmail());
            }
        }
        return toPrincipalAttributes(user, request, superUser);
    }

    /**
     * Turns an already resolved user into the attribute map. Kept apart from the resolution, this
     * conversion can be checked on its own: what makes the behaviour identical is the shape of the
     * values, not the way the user was looked up.
     */
    public Map<String, List<String>> toPrincipalAttributes(
        final UserDto user,
        final PrincipalAttributesRequestDto request,
        final UserDto superUser
    ) {
        final boolean subrogation = StringUtils.isNotBlank(request.getSuperUserEmail());
        final Map<String, List<String>> attributes = new LinkedHashMap<>();

        put(attributes, CommonConstants.USER_ID_ATTRIBUTE, user.getId());
        put(attributes, CommonConstants.CUSTOMER_ID_ATTRIBUTE, user.getCustomerId());
        put(attributes, CommonConstants.EMAIL_ATTRIBUTE, user.getEmail());
        put(attributes, CommonConstants.FIRSTNAME_ATTRIBUTE, user.getFirstname());
        put(attributes, CommonConstants.LASTNAME_ATTRIBUTE, user.getLastname());
        put(attributes, CommonConstants.IDENTIFIER_ATTRIBUTE, user.getIdentifier());
        put(attributes, CommonConstants.OTP_ATTRIBUTE, user.isOtp());
        put(
            attributes,
            UserPrincipalAttributes.COMPUTED_OTP,
            user.isOtp() &&
            authenticatesWithInternalProvider(otpEmail(request, subrogation), otpCustomerId(request, subrogation))
        );
        put(attributes, CommonConstants.SUBROGEABLE_ATTRIBUTE, user.isSubrogeable());
        put(attributes, CommonConstants.USER_INFO_ID, user.getUserInfoId());
        put(attributes, CommonConstants.PHONE_ATTRIBUTE, user.getPhone());
        put(attributes, CommonConstants.MOBILE_ATTRIBUTE, user.getMobile());
        put(attributes, CommonConstants.STATUS_ATTRIBUTE, user.getStatus());
        put(attributes, CommonConstants.TYPE_ATTRIBUTE, user.getType());
        put(attributes, CommonConstants.READONLY_ATTRIBUTE, user.isReadonly());
        put(attributes, CommonConstants.LEVEL_ATTRIBUTE, user.getLevel());
        put(attributes, CommonConstants.LAST_CONNECTION_ATTRIBUTE, user.getLastConnection());
        put(attributes, CommonConstants.NB_FAILED_ATTEMPTS_ATTRIBUTE, user.getNbFailedAttempts());
        put(attributes, CommonConstants.PASSWORD_EXPIRATION_DATE_ATTRIBUTE, user.getPasswordExpirationDate());
        put(attributes, CommonConstants.GROUP_ID_ATTRIBUTE, user.getGroupId());
        putJson(attributes, CommonConstants.ADDRESS_ATTRIBUTE, user.getAddress());
        putJson(attributes, CommonConstants.ANALYTICS_ATTRIBUTE, user.getAnalytics());
        put(attributes, CommonConstants.INTERNAL_CODE, user.getInternalCode());

        if (subrogation) {
            addSuperUserAttributes(attributes, request, superUser);
        }
        if (user instanceof AuthUserDto authUser && authUser.getProfileGroup() != null) {
            addAuthenticatedUserAttributes(attributes, authUser);
        }
        return attributes;
    }

    private String otpEmail(final PrincipalAttributesRequestDto request, final boolean subrogation) {
        return subrogation ? request.getSuperUserEmail() : request.getLoginEmail();
    }

    private String otpCustomerId(final PrincipalAttributesRequestDto request, final boolean subrogation) {
        return subrogation ? request.getSuperUserCustomerId() : request.getLoginCustomerId();
    }

    /**
     * Mirrors {@code IdentityProviderHelper.identifierMatchProviderPattern}: the user really does
     * authenticate with a password rather than through a delegation. OTP only makes sense in that case.
     */
    private boolean authenticatesWithInternalProvider(final String email, final String customerId) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(customerId)) {
            return false;
        }
        return StreamSupport.stream(identityProviderRepository.findAll().spliterator(), false)
            .filter(provider -> customerId.equals(provider.getCustomerId()))
            .filter(provider -> provider.getPatterns() != null)
            .filter(provider -> matchesAnyPattern(provider, email))
            .findFirst()
            .map(provider -> Boolean.TRUE.equals(provider.getInternal()))
            .orElse(false);
    }

    private void addSuperUserAttributes(
        final Map<String, List<String>> attributes,
        final PrincipalAttributesRequestDto request,
        final UserDto superUser
    ) {
        put(attributes, CommonConstants.SUPER_USER_ATTRIBUTE, request.getSuperUserEmail());
        put(attributes, CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE, request.getSuperUserCustomerId());
        if (superUser != null) {
            put(attributes, CommonConstants.SUPER_USER_IDENTIFIER_ATTRIBUTE, superUser.getIdentifier());
            put(attributes, UserPrincipalAttributes.SUPER_USER_ID, superUser.getId());
        }
    }

    private void addAuthenticatedUserAttributes(
        final Map<String, List<String>> attributes,
        final AuthUserDto authUser
    ) {
        putJson(attributes, CommonConstants.PROFILE_GROUP_ATTRIBUTE, authUser.getProfileGroup());
        put(attributes, CommonConstants.CUSTOMER_IDENTIFIER_ATTRIBUTE, authUser.getCustomerIdentifier());
        putJson(attributes, CommonConstants.BASIC_CUSTOMER_ATTRIBUTE, authUser.getBasicCustomer());
        put(attributes, CommonConstants.AUTHTOKEN_ATTRIBUTE, authUser.getAuthToken());
        put(attributes, CommonConstants.PROOF_TENANT_ID_ATTRIBUTE, authUser.getProofTenantIdentifier());
        putJson(attributes, CommonConstants.TENANTS_BY_APP_ATTRIBUTE, authUser.getTenantsByApp());
        put(attributes, CommonConstants.SITE_CODE, authUser.getSiteCode());
        put(attributes, CommonConstants.CENTER_CODES, authUser.getCenterCodes());

        final Set<String> roles = new HashSet<>();
        authUser
            .getProfileGroup()
            .getProfiles()
            .forEach(profile -> profile.getRoles().forEach(role -> roles.add(role.getName())));
        attributes.put(CommonConstants.ROLES_ATTRIBUTE, new ArrayList<>(roles));
    }

    private void put(final Map<String, List<String>> attributes, final String name, final Object value) {
        if (value != null) {
            attributes.put(name, List.of(String.valueOf(value)));
        }
    }

    private void putJson(final Map<String, List<String>> attributes, final String name, final Object value) {
        if (value == null) {
            return;
        }
        try {
            attributes.put(name, List.of(JsonUtils.toJson(value)));
        } catch (final JsonProcessingException e) {
            throw new ApplicationServerException("Could not serialize the attribute " + name, e);
        }
    }

    /**
     * Validates that a subrogation really lets this super user take this user's place, and resolves both
     * identifiers.
     *
     * The authentication server today asks for every subrogation of the super user and filters them
     * itself. A targeted query is enough, and the list of subrogations stops travelling over the wire.
     *
     * The expiry date is checked here, which the current filtering does not do. The Mongo TTL index
     * ({@code expireAfterSeconds = 0} on {@code Subrogation.date}) is meant to purge stale entries, but
     * it only runs once a minute and may be disabled depending on the deployment: relying on it leaves a
     * window during which an expired subrogation stays usable.
     *
     * @throws NotFoundException when no accepted and still valid subrogation matches, or when either
     *                           account cannot be found. A refusal is never an empty response.
     */
    public SubrogationValidateResponseDto validateSubrogation(final SubrogationValidateRequestDto request) {
        Assert.notNull(request, "request must not be null");

        final Optional<Subrogation> subrogation =
            subrogationRepository.findBySuperUserAndSuperUserCustomerIdAndSurrogateAndSurrogateCustomerId(
                request.getSuperUserEmail(),
                request.getSuperUserCustomerId(),
                request.getSurrogateEmail(),
                request.getSurrogateCustomerId()
            );

        if (subrogation.isEmpty() || subrogation.get().getStatus() != SubrogationStatusEnum.ACCEPTED) {
            throw new NotFoundException("No accepted subrogation between the given super-user and surrogate");
        }
        if (subrogation.get().getDate() != null && subrogation.get().getDate().before(new Date())) {
            throw new NotFoundException("The subrogation between the given super-user and surrogate has expired");
        }

        final User superUser = userRepository.findByEmailIgnoreCaseAndCustomerId(
            request.getSuperUserEmail(),
            request.getSuperUserCustomerId()
        );
        final User surrogate = userRepository.findByEmailIgnoreCaseAndCustomerId(
            request.getSurrogateEmail(),
            request.getSurrogateCustomerId()
        );
        if (superUser == null || surrogate == null) {
            throw new NotFoundException("Could not resolve both users of the subrogation");
        }

        return new SubrogationValidateResponseDto(superUser.getId(), surrogate.getId());
    }

    /**
     * The password policy IAM enforces, so that the authentication server displays exactly the
     * constraints that will be checked.
     *
     * The labels are flattened in configuration order: the default constraints first, with the special
     * character ones interleaved, then the custom constraints.
     */
    public PasswordPolicyDto getPasswordPolicy() {
        final List<String> messages = new ArrayList<>();
        if (passwordConfiguration != null && passwordConfiguration.getConstraints() != null) {
            final var constraints = passwordConfiguration.getConstraints();
            if (constraints.getDefaults() != null) {
                constraints
                    .getDefaults()
                    .values()
                    .forEach(constraint -> {
                        if (constraint.getMessages() != null) {
                            messages.addAll(constraint.getMessages());
                        }
                        if (
                            constraint.getSpecialChars() != null && constraint.getSpecialChars().getMessages() != null
                        ) {
                            messages.addAll(constraint.getSpecialChars().getMessages());
                        }
                    });
            }
            if (constraints.getCustoms() != null) {
                constraints
                    .getCustoms()
                    .values()
                    .forEach(constraint -> {
                        if (constraint.getMessages() != null) {
                            messages.addAll(constraint.getMessages());
                        }
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

    /**
     * Home Realm Discovery: resolves an email to the customers its bearer may authenticate through, and
     * to the identity provider to use in each of them.
     *
     * Two sources can point at a customer, but they do not carry the same weight. Existing accounts have
     * the final say: as soon as at least one account carries the address, only their customers are
     * offered. Provider patterns only come second, when no account exists — an external provider
     * provisions on first login, and the address is then the only clue available.
     *
     * That ordering is what preserves non-disclosure. An unknown address whose domain matches a provider
     * is routed just like a known one, and the failure only happens after the password is entered, in a
     * generic form. Resolving both sources as a union, or dropping internal providers that have no
     * account, would make the absence of an account observable before any authentication.
     *
     * Only the routing is indistinguishable, not the whole response: {@code userStatus} stays empty for
     * want of an account. That field is meant for the authentication server, which has to decide the fate
     * of a disabled account and already held the information; it must not surface in what the user
     * observes.
     *
     * Within a customer, the provider kept is the first one whose pattern matches, taken in identifier
     * order — the very order that puts the internal provider ahead of the delegations. A customer
     * therefore appears only once. The provider may be absent when an account exists in a customer where
     * no provider covers the address: it is up to the authentication server to turn that case into a
     * configuration error.
     *
     * @return the entries sorted by customer code, possibly empty when nothing matches.
     */
    public List<HrdEntryDto> resolveHrdEntries(final String email) {
        Assert.hasText(email, "email must not be empty");

        // Identifier order puts the internal provider ahead of the delegations of a same customer; it
        // therefore decides which one is kept when several of them cover the same address.
        final List<IdentityProvider> providers = StreamSupport.stream(
            identityProviderRepository.findAll().spliterator(),
            false
        )
            .sorted(Comparator.comparing(IdentityProvider::getIdentifier, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());

        final List<User> existingUsers = userRepository.findAllByEmailIgnoreCase(email);
        final Map<String, User> userByCustomerId = new LinkedHashMap<>();
        existingUsers.forEach(user -> userByCustomerId.putIfAbsent(user.getCustomerId(), user));

        final Map<String, IdentityProvider> providerByCustomerId = new LinkedHashMap<>();
        if (userByCustomerId.isEmpty()) {
            providers
                .stream()
                .filter(provider -> matchesAnyPattern(provider, email))
                .forEach(provider -> providerByCustomerId.putIfAbsent(provider.getCustomerId(), provider));
        } else {
            userByCustomerId
                .keySet()
                .forEach(
                    customerId ->
                        providerByCustomerId.put(customerId, firstMatchingProvider(providers, customerId, email))
                );
        }

        if (providerByCustomerId.isEmpty()) {
            LOGGER.debug("HRD: no identity provider resolved for this email (existingUsers={})", existingUsers.size());
            return List.of();
        }

        final Map<String, Customer> customersById = new HashMap<>();
        customerRepository
            .findAllById(providerByCustomerId.keySet())
            .forEach(customer -> customersById.put(customer.getId(), customer));

        return providerByCustomerId
            .entrySet()
            .stream()
            .map(
                entry ->
                    toHrdEntry(
                        entry.getKey(),
                        entry.getValue(),
                        customersById.get(entry.getKey()),
                        userByCustomerId.get(entry.getKey())
                    )
            )
            .sorted(Comparator.comparing(HrdEntryDto::getCustomerCode, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());
    }

    private IdentityProvider firstMatchingProvider(
        final List<IdentityProvider> providers,
        final String customerId,
        final String email
    ) {
        return providers
            .stream()
            .filter(provider -> customerId.equals(provider.getCustomerId()))
            .filter(provider -> matchesAnyPattern(provider, email))
            .findFirst()
            .orElse(null);
    }

    private boolean matchesAnyPattern(final IdentityProvider provider, final String email) {
        return (
            provider.getPatterns() != null &&
            provider
                .getPatterns()
                .stream()
                .anyMatch(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(email).matches())
        );
    }

    private HrdEntryDto toHrdEntry(
        final String customerId,
        final IdentityProvider provider,
        final Customer customer,
        final User user
    ) {
        return new HrdEntryDto(
            customerId,
            customer != null ? customer.getCode() : null,
            customer != null ? customer.getName() : customerId,
            provider != null ? provider.getId() : null,
            provider != null ? provider.getName() : null,
            provider != null && Boolean.TRUE.equals(provider.getInternal()),
            provider != null ? provider.getProtocoleType() : null,
            user != null && user.getStatus() != null ? user.getStatus().toString() : null
        );
    }

    @Getter
    @AllArgsConstructor
    public static class PrincipalFromToken {

        private final String email;
        private final String customerId;
    }
}
