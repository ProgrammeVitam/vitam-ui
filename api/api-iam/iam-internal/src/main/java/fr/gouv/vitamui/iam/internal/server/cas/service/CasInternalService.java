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
package fr.gouv.vitamui.iam.internal.server.cas.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.ConflictException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.rest.ApiErrorGenerator;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.provisioning.service.ProvisioningInternalService;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.token.domain.Token;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInfoInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
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

/**
 * Specific CAS service.
 */
@Getter
@Setter
public class CasInternalService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";

    private static final String ID = "_id";

    private static final String NB_FAILED_ATTEMPTS = "nbFailedAttempts";

    private static final String STATUS = "status";

    private static final String LAST_CONNECTION = "lastConnection";

    private static final String TOKEN_PREFIX = "TOK";

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserInternalService internalUserService;

    @Autowired
    private UserInfoInternalService userInfoInternalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SubrogationInternalService internalSubrogationService;

    @Autowired
    private SubrogationRepository subrogationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantInternalService internalTenantService;

    @Autowired
    private IamLogbookService iamLogbookService;

    @Autowired
    private IdentityProviderInternalService identityProviderInternalService;

    @Autowired
    private GroupInternalService groupInternalService;

    @Autowired
    private ProvisioningInternalService provisioningInternalService;

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
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CasInternalService.class);

    private static final UniqueTicketIdGenerator TICKET_GENERATOR = new DefaultUniqueTicketIdGenerator();

    public CasInternalService() {}

    @Transactional
    public void updatePassword(final String email, final String rawPassword) {
        final User user = checkUserInformations(email);
        final Optional<Customer> optCustomer = customerRepository.findById(user.getCustomerId());
        final Customer customer = optCustomer.orElseThrow(
            () -> new ApplicationServerException("Unable to update password : customer not found")
        );

        final List<String> oldPasswords = user.getOldPasswords();
        if (oldPasswords != null && !oldPasswords.isEmpty()) {
            for (final String oldPassword : oldPasswords) {
                if (passwordEncoder.matches(rawPassword, oldPassword)) {
                    throw new ConflictException("The given password has already been used in the past");
                }
            }
        }

        final String encodedPassword = passwordEncoder.encode(rawPassword);
        internalUserService.saveCurrentPasswordInOldPasswords(
            user,
            encodedPassword,
            (passwordConfiguration != null && passwordConfiguration.getMaxOldPassword() != null)
                ? passwordConfiguration.getMaxOldPassword()
                : UserInternalService.MAX_OLD_PASSWORDS
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

    public User findEntityByEmail(final String email) {
        return checkUserInformations(email);
    }

    private User checkUserInformations(final String email) {
        final User user = userRepository.findByEmail(email);
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
    public UserDto getUserByEmail(final String email, final Optional<String> optEmbedded) {
        boolean loadFullProfile = false;
        boolean isSubrogation = false;
        boolean isApi = false;
        if (optEmbedded.isPresent()) {
            final String embedded = optEmbedded.get();
            final Set<String> values = splitIntoValues(embedded);
            if (values.contains(CommonConstants.AUTH_TOKEN_PARAMETER)) {
                loadFullProfile = true;
            }
            if (values.contains(CommonConstants.SURROGATION_PARAMETER)) {
                isSubrogation = true;
            }
            if (values.contains(CommonConstants.API_PARAMETER)) {
                isApi = true;
            }
        }

        final UserDto userDto = internalUserService.findUserByEmail(email);
        if (userDto == null) {
            throw new NotFoundException(USER_NOT_FOUND_MESSAGE + email);
        }
        checkStatus(userDto.getStatus(), userDto.getEmail());
        if (loadFullProfile) {
            final AuthUserDto authUserDto = internalUserService.loadGroupAndProfiles(userDto);
            internalUserService.addBasicCustomerAndProofTenantIdentifierInformation(authUserDto);
            internalUserService.addTenantsByAppInformation(authUserDto);
            generateAndAddAuthToken(authUserDto, isSubrogation, isApi);
            createEventsSubrogation(userDto, isSubrogation);
            return authUserDto;
        } else {
            return userDto;
        }
    }

    /**
     * Method to retrieve the user informations
     * @param email email of the user
     * @param idp can be null
     * @param userIdentifier can be null
     * @param optEmbedded
     * @return
     */
    @Transactional
    public UserDto getUser(String email, final String idp, final String userIdentifier, final String optEmbedded) {
        // if the user depends on an external idp
        if (StringUtils.isNotBlank(idp)) {
            Optional<ProvidedUserDto> providedUser = this.provisionUser(email, idp, userIdentifier);
            if (email.isBlank() && providedUser.isPresent()) {
                email = providedUser.get().getEmail();
            }
        }

        return getUserByEmail(email, Optional.ofNullable(optEmbedded));
    }

    /**
     * Method to perform auto provisioning
     * @param email
     * @param idp
     * @param userIdentifier
     */
    public Optional<ProvidedUserDto> provisionUser(String email, final String idp, final String userIdentifier) {
        final IdentityProviderDto identityProvider = identityProviderInternalService.getOne(idp);

        // Do nothing is autoProvisioning is disabled
        if (!identityProvider.isAutoProvisioningEnabled()) {
            return Optional.empty();
        }

        Optional<ProvidedUserDto> providedUser = Optional.empty();

        if (StringUtils.isBlank(email)) {
            providedUser = Optional.of(
                getProvidedUser(email, idp, userIdentifier, null, identityProvider.getCustomerId())
            );
            email = providedUser.get().getEmail();
        }

        final boolean userExist = userRepository.existsByEmail(email);
        // Try to update user
        if (userExist) {
            final UserDto user = internalUserService.findUserByEmail(email);
            if (user.isAutoProvisioningEnabled()) {
                updateUser(
                    user,
                    getProvidedUser(email, idp, userIdentifier, user.getGroupId(), identityProvider.getCustomerId())
                );
            }
        }
        // Try to create a new user
        else {
            if (providedUser.isEmpty()) {
                providedUser = Optional.of(
                    getProvidedUser(email, idp, userIdentifier, null, identityProvider.getCustomerId())
                );
            }
            createNewUser(email, providedUser.get());
        }

        return providedUser;
    }

    private ProvidedUserDto getProvidedUser(
        String email,
        String idp,
        String userIdentifier,
        String groupId,
        String customerId
    ) {
        ProvidedUserDto userProvidedInfo;
        userProvidedInfo = provisioningInternalService.getUserInformation(
            idp,
            email,
            groupId,
            null,
            userIdentifier,
            customerId
        );

        if (Objects.isNull(userProvidedInfo)) {
            throw new NotFoundException(
                String.format(
                    "The following provided user does not exist: Email:%s, technicalId:%s, groupId:%s, idp:%s, customerId:%s",
                    email,
                    userIdentifier,
                    groupId,
                    idp,
                    customerId
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
        GroupDto groupDto = getGroupByUnit(providedUserInfo.getUnit());
        user.setGroupId(groupDto.getId());
        user.setCustomerId(groupDto.getCustomerId());

        final Customer customer = customerRepository
            .findById(user.getCustomerId())
            .orElseThrow(() -> new NotFoundException(String.format("Cannot find customer : %s", user.getCustomerId())));
        user.setUserInfoId(createUserInfo(customer.getLanguage()).getId());

        internalUserService.create(user);
    }

    private UserInfoDto createUserInfo(final String language) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setLanguage(language);
        return userInfoInternalService.create(userInfoDto);
    }

    private GroupDto getGroupByUnit(final String unit) {
        QueryDto criteria = QueryDto.criteria("units", List.of(unit), CriterionOperator.IN);
        final List<GroupDto> groups = groupInternalService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assert.notEmpty(groups, String.format("No group found for the given unit : %s", unit));
        return groups.get(0);
    }

    private void updateUser(final UserDto userDto, final ProvidedUserDto userProvidedInfo) {
        final Map<String, Object> userUpdate = new HashMap<>();
        updateUserMandatoryInformation(userDto, userProvidedInfo, userUpdate);
        updateUserOptionalInformation(userDto, userProvidedInfo, userUpdate);
        if (!userUpdate.isEmpty()) {
            userUpdate.put("id", userDto.getId());
            userUpdate.put("customerId", userDto.getCustomerId());
            internalUserService.patch(userUpdate);
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
        QueryDto criteria = QueryDto.criteria("units", List.of(userInfo.getUnit()), CriterionOperator.IN);
        final List<GroupDto> groups = groupInternalService.getAll(Optional.of(criteria.toJson()), Optional.empty());
        Assert.notEmpty(groups, String.format("No group found for the given unit : %s", userInfo.getUnit()));
        if (!StringUtils.equals(userDto.getGroupId(), groups.get(0).getId())) {
            userUpdate.put("groupId", groups.get(0).getId());
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
            final Subrogation subro = subrogationRepository.findOneBySurrogate(surrogate.getEmail());
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
        final Date nowPlusXMinutes = DateUtils.addMinutes(new Date(), ttlInMinutes);
        token.setUpdatedDate(nowPlusXMinutes);
        token.setId(TICKET_GENERATOR.getNewTicketId(TOKEN_PREFIX));
        token.setSurrogation(isSubrogation);
        tokenRepository.save(token);
        user.setLastConnection(OffsetDateTime.now());
        user.setAuthToken(token.getId());
        final Query query = new Query(Criteria.where(ID).is(user.getId()));
        final Update update = Update.update(LAST_CONNECTION, user.getLastConnection());
        mongoTemplate.updateFirst(query, update, MongoDbCollections.USERS);
    }

    public UserDto getUserProfileById(final String id) {
        final UserDto user = internalUserService.getOne(id, Optional.empty());
        if (user == null) {
            throw new NotFoundException(USER_NOT_FOUND_MESSAGE + id);
        }
        checkStatus(user.getStatus(), user.getEmail());

        return user;
    }

    public List<SubrogationDto> getSubrogationsBySuperUser(final String superUser) {
        final List<Subrogation> subrogations = subrogationRepository.findBySuperUser(superUser);
        final List<SubrogationDto> dtos = new ArrayList<>();
        subrogations.forEach(subrogation -> dtos.add(convertFromSubrogationToDto(subrogation)));
        return dtos;
    }

    protected final SubrogationDto convertFromSubrogationToDto(final Subrogation entity) {
        if (entity != null) {
            return internalSubrogationService.internalConvertFromEntityToDto(entity);
        } else {
            return null;
        }
    }

    @Transactional
    public void deleteSubrogationBySuperUserAndSurrogate(final String superUser, final String surrogate) {
        if (StringUtils.isBlank(superUser) || StringUtils.isBlank(surrogate)) {
            throw ApiErrorGenerator.getBadRequestException("superUser and surrogate must be filled");
        }
        final Optional<Subrogation> subro = subrogationRepository.findBySuperUserAndSurrogate(superUser, surrogate);
        if (subro.isPresent()) {
            final Subrogation subrogation = subro.get();
            iamLogbookService.subrogation(subrogation, EventType.EXT_VITAMUI_LOGOUT_SURROGATE);
            subrogationRepository.deleteById(subrogation.getId());
        }
    }

    public String removeTokenAndGetUsername(final String authToken) {
        final Optional<Token> optToken = tokenRepository.findById(authToken);
        if (optToken.isPresent()) {
            tokenRepository.deleteById(authToken);
            final Token token = optToken.get();
            final String userId = token.getRefId();
            final Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                return optionalUser.get().getEmail();
            }
        }
        return null;
    }
}
