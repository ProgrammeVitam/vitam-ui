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

import static fr.gouv.vitamui.commons.api.CommonConstants.GPDR_DEFAULT_VALUE;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.security.client.dto.BasicCustomerDto;
import fr.gouv.vitamui.commons.security.client.dto.GraphicIdentityDto;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.internal.server.application.service.ApplicationInternalService;
import fr.gouv.vitamui.iam.internal.server.common.ApiIamInternalConstants;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.domain.Tenant;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;

import static fr.gouv.vitamui.commons.api.CommonConstants.APPLICATION_ID;

/**
 * The service to read, create, update and delete the users.
 *
 *
 */
@Getter
@Setter
public class UserInternalService extends VitamUICrudService<UserDto, User> {

    private static final int MAX_OLD_PASSWORDS = 3;

    private UserRepository userRepository;

    private GroupInternalService groupInternalService;

    private ProfileInternalService profileInternalService;

    private UserEmailInternalService userEmailInternalService;

    private TenantRepository tenantRepository;

    private InternalSecurityService internalSecurityService;

    private CustomerRepository customerRepository;

    private ProfileRepository profilRepository;

    private GroupRepository groupRepository;

    private final IamLogbookService iamLogbookService;

    private final UserConverter userConverter;

    private final String ADMIN_EMAIL_PATTERN = "admin@";

    private final String PORTAL_APP_IDENTIFIER = "PORTAL_APP";

    private MongoTransactionManager mongoTransactionManager;

    private LogbookService logbookService;

    private AddressService addressService;

    private final ApplicationInternalService applicationInternalService;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(UserInternalService.class);

    @Autowired
    public UserInternalService(final CustomSequenceRepository sequenceRepository, final UserRepository userRepository,
            final GroupInternalService groupInternalService, final ProfileInternalService profileInternalService,
            final UserEmailInternalService userEmailInternalService, final TenantRepository tenantRepository,
            final InternalSecurityService internalSecurityService, final CustomerRepository customerRepository, final ProfileRepository profilRepository,
            final GroupRepository groupRepository, final IamLogbookService iamLogbookService, final UserConverter userConverter,
            final MongoTransactionManager mongoTransactionManager, final LogbookService logbookService, final AddressService addressService,
            ApplicationInternalService applicationInternalService) {
        super(sequenceRepository);
        this.userRepository = userRepository;
        this.groupInternalService = groupInternalService;
        this.profileInternalService = profileInternalService;
        this.userEmailInternalService = userEmailInternalService;
        this.tenantRepository = tenantRepository;
        this.internalSecurityService = internalSecurityService;
        this.customerRepository = customerRepository;
        this.profilRepository = profilRepository;
        this.groupRepository = groupRepository;
        this.iamLogbookService = iamLogbookService;
        this.userConverter = userConverter;
        this.mongoTransactionManager = mongoTransactionManager;
        this.logbookService = logbookService;
        this.addressService = addressService;
        this.applicationInternalService = applicationInternalService;
    }

    /**
     * This method must be only used by the Authentification Service during the authentication process
     */
    public UserDto findUserById(final String id) {
        return super.getOneByPassSecurity(id, Optional.empty());
    }

    public UserDto findUserByEmail(final String email) {
        final User user = getRepository().findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found for email: " + email);
        }
        return convertFromEntityToDto(user);
    }

    public AuthUserDto getMe() {
        return internalSecurityService.getUser();
    }

    @Override
    protected void beforeCreate(final UserDto dto) {
        final String message = "Unable to create user " + dto.getEmail();

        checkSetReadonly(dto.isReadonly(), message);
        checkCustomer(dto.getCustomerId(), message);
        checkEmail(dto.getEmail(), dto.getCustomerId(), message);
        checkGroupId(dto.getGroupId(), message);
        super.checkIdentifier(dto.getIdentifier(), message);

        final GroupDto groupDto = getGroupDtoById(dto.getGroupId(), message);
        checkGroup(groupDto, dto.getCustomerId(), message);
        checkLevel(groupDto.getLevel(), message);
        checkOtp(dto);

        if (dto.getPhone() != null) {
            checkPhoneNumber(dto.getPhone());
        }

        if (dto.getMobile() != null) {
            checkPhoneNumber(dto.getMobile());
        }

        dto.setLevel(groupDto.getLevel());
        dto.setIdentifier(getNextSequenceId(SequencesConstants.USER_IDENTIFIER));
        dto.setPasswordExpirationDate(getPasswordExpirationDate(dto.getCustomerId()));
    }

    /**
     * User Creation.
     * Email sent to user is not mandatory for user creation.
     * Also we can't use {@link Transactional} because before sending an email, CAS check user existence and with the transaction the user isn't processed yet.
     * {@inheritDoc}
     */
    @Override
    public UserDto create(final UserDto userDto) {

        UserDto createdUserDto = null;

        TransactionStatus status = null;
        if (mongoTransactionManager != null) {
            final TransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
            status = mongoTransactionManager.getTransaction(definition);
        }

        try {
            createdUserDto = super.create(userDto);
            iamLogbookService.createUserEvent(createdUserDto);

            if (mongoTransactionManager != null) {
                mongoTransactionManager.commit(status);
            }
        }
        catch (final Exception e) {
            if (mongoTransactionManager != null) {
                mongoTransactionManager.rollback(status);
            }
            throw e;
        }
        userEmailInternalService.sendCreationEmail(createdUserDto);
        return createdUserDto;
    }

    protected OffsetDateTime getPasswordExpirationDate(final String customerId) {
        final Optional<Customer> customer = customerRepository.findById(customerId);
        Assert.isTrue(customer.isPresent(), "Customer does not exist");

        return OffsetDateTime.now().plusDays(customer.get().getPasswordRevocationDelay());
    }

    @Override
    protected void beforeUpdate(final UserDto dto) {
        final String message = "Unable to update user " + dto.getId();
        final User user = find(dto.getId(), dto.getCustomerId(), message);

        checkIsReadonly(user.isReadonly(), message);
        checkCustomer(user.getCustomerId(), message);
        checkLevel(user.getLevel(), message);

        checkSetReadonly(dto.isReadonly(), message);
        if (!StringUtils.equalsIgnoreCase(user.getEmail(), dto.getEmail())) {
            checkEmail(dto.getEmail(), user.getCustomerId(), message);
        }
        checkSetReadonly(dto.isReadonly(), message);

        final GroupDto groupDto = getGroupDtoById(dto.getGroupId(), message);
        checkGroup(groupDto, user.getCustomerId(), message);
        checkLevel(groupDto.getLevel(), message);
        checkOtp(dto);

        if (dto.getPhone() != null && !StringUtils.equalsIgnoreCase(user.getPhone(), dto.getPhone())) {
            checkPhoneNumber(dto.getPhone());
        }

        if (dto.getMobile() != null && !StringUtils.equalsIgnoreCase(user.getMobile(), dto.getMobile())) {
            checkPhoneNumber(dto.getMobile());
        }

        dto.setLevel(groupDto.getLevel());
        dto.setIdentifier(user.getIdentifier());
        dto.setPasswordExpirationDate(user.getPasswordExpirationDate());
    }

    /**
     * User Update.
     * We can't use {@link Transactional} because before sending an email, CAS check user existence and with the transaction the user isn't processed yet.
     * {@inheritDoc}
     */
    @Override
    public UserDto update(final UserDto dto) {
        boolean sendMail = false;
        UserDto updatedUser = null;

        TransactionStatus status = null;
        if (mongoTransactionManager != null) {
            final TransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
            status = mongoTransactionManager.getTransaction(definition);
        }

        try {

            final VitamContext vitamContext =  internalSecurityService.buildVitamContext(internalSecurityService.getTenantIdentifier());
            if(vitamContext != null) {
                LOGGER.info("Update User EvIdAppSession : {} " , vitamContext.getApplicationSessionId());
            }

            LOGGER.info("Update {} {}", getObjectName(), dto);
            beforeUpdate(dto);
            final User entity = convertFromDtoToEntity(dto);
            final String entityId = entity.getId();
            final Optional<User> optExistingUser = getRepository().findById(entityId);
            Assert.isTrue(optExistingUser.isPresent(), "Unable to update " + getObjectName() + ": no entity found with id: " + entityId);

            final User existingUser = optExistingUser.get();
            entity.setPassword(existingUser.getPassword());
            entity.setOldPasswords(existingUser.getOldPasswords());

            final UserStatusEnum existingStatus = existingUser.getStatus();
            final UserStatusEnum newStatus = dto.getStatus();
            if (statusEquals(newStatus, UserStatusEnum.ENABLED) && statusEquals(existingStatus, UserStatusEnum.DISABLED)) {
                saveCurrentPasswordInOldPasswords(entity, entity.getPassword());
                entity.setPassword(null);
                entity.setPasswordExpirationDate(OffsetDateTime.now());
                entity.setNbFailedAttempts(0);
                sendMail = true;

                final AuthUserDto authUserDto = internalSecurityService.getUser();
                iamLogbookService.revokePasswordEvent(dto, authUserDto.getSuperUserIdentifier());
            }

            final User savedEntity = getRepository().save(entity);
            updatedUser = convertFromEntityToDto(savedEntity);

            if (mongoTransactionManager != null) {
                mongoTransactionManager.commit(status);
            }
        }
        catch (final Exception e) {
            if (mongoTransactionManager != null) {
                mongoTransactionManager.rollback(status);
            }
            throw e;
        }

        if (sendMail) {
            userEmailInternalService.sendCreationEmail(updatedUser);
        }

        return updatedUser;
    }

    public void saveCurrentPasswordInOldPasswords(final User user, final String newPassword) {
        if (StringUtils.isNotBlank(newPassword)) {
            List<String> oldPasswords = user.getOldPasswords();
            if (oldPasswords == null) {
                oldPasswords = new ArrayList<>();
            }
            oldPasswords.add(0, newPassword);
            if (oldPasswords.size() > MAX_OLD_PASSWORDS) {
                oldPasswords = oldPasswords.subList(0, MAX_OLD_PASSWORDS);
            }
            user.setOldPasswords(oldPasswords);
        }
    }

    private boolean statusEquals(final UserStatusEnum status, final UserStatusEnum expectedStatus) {
        return status != null && status.equals(expectedStatus);
    }

    /**
     * User Patch.
     * We can't use {@link Transactional} because before sending an email, CAS check user existence and with the transaction the user isn't processed yet.
     * {@inheritDoc}
     */
    @Override
    public UserDto patch(final Map<String, Object> partialDto) {
        boolean sendMail = false;
        UserDto dto = null;

        TransactionStatus status = null;
        if (mongoTransactionManager != null) {
            final TransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
            status = mongoTransactionManager.getTransaction(definition);
        }

        try {
            LOGGER.info("Patch {} with {}", getObjectName(), partialDto);

            String email = CastUtils.toString(partialDto.get("email"));
            if (email != null) {
                partialDto.put("email", email.toLowerCase());
            }
            final User entity = beforePatch(partialDto);
            final UserStatusEnum existingStatus = entity.getStatus();
            processPatch(entity, partialDto);
            Assert.isTrue(getRepository().existsById(entity.getId()), "Unable to patch " + getObjectName() + ": no entity found with id: " + entity.getId());

            final UserStatusEnum newStatus = entity.getStatus();
            if (statusEquals(existingStatus, UserStatusEnum.DISABLED) && statusEquals(newStatus, UserStatusEnum.ENABLED)) {
                entity.setPassword(null);
                entity.setPasswordExpirationDate(OffsetDateTime.now());
                entity.setNbFailedAttempts(0);
                sendMail = true;

                final AuthUserDto authUserDto = internalSecurityService.getUser();
                iamLogbookService.revokePasswordEvent(entity, authUserDto.getSuperUserIdentifier());
            }

            final User savedEntity = getRepository().save(entity);
            dto = convertFromEntityToDto(savedEntity);

            if (mongoTransactionManager != null) {
                mongoTransactionManager.commit(status);
            }
        }
        catch (final Exception e) {
            if (mongoTransactionManager != null) {
                mongoTransactionManager.rollback(status);
            }
            throw e;
        }

        if (sendMail) {
            userEmailInternalService.sendCreationEmail(dto);
        }
        return dto;
    }

    @Override
    protected User beforePatch(final Map<String, Object> partialDto) {
        final String id = CastUtils.toString(partialDto.get("id"));
        final String message = "Unable to patch user " + id;
        final String customerId = CastUtils.toString(partialDto.get("customerId"));
        final User user = find(id, customerId, message);

        Assert.isTrue(!partialDto.containsKey("password"), message + user.getId() + " : cannot patch password");
        Assert.isTrue(!partialDto.containsKey("identifier"), message + user.getId() + " cannot patch identifier");
        Assert.isTrue(!partialDto.containsKey("readonly"), message + user.getId() + " cannot patch readonly");
        Assert.isTrue(!partialDto.containsKey("level"), message + user.getId() + " cannot patch level");
        Assert.isTrue(!UserStatusEnum.BLOCKED.toString().equals(partialDto.get("status")),
                "User cann't be blocked by API, this action need a special workflow for be realised");
        Assert.isTrue(!checkMapContainsOnlyFieldsUnmodifiable(partialDto, Arrays.asList("id", "customerId")), message);

        checkLevel(user.getLevel(), message);
        checkIsReadonly(user.isReadonly(), message);
        final Boolean userHasOtp = CastUtils.toBoolean(partialDto.get("otp"));
        if (userHasOtp != null) {
            checkOtp(user, userHasOtp);
        }

        final String email = CastUtils.toString(partialDto.get("email"));
        if (email != null) {
            checkEmail(email, user.getCustomerId(), message);
        }

        final String groupId = CastUtils.toString(partialDto.get("groupId"));
        if (!StringUtils.isEmpty(groupId)) {
            final GroupDto groupDto = getGroupDtoById(groupId, message);
            checkGroup(groupDto, customerId, message);
            if (!StringUtils.equals(user.getLevel(), groupDto.getLevel())) {
                checkLevel(groupDto.getLevel(), message);
                partialDto.put("level", groupDto.getLevel());
            }
        }

        final String phone = CastUtils.toString(partialDto.get("phone"));
        final String mobile = CastUtils.toString(partialDto.get("mobile"));
        if (phone != null && !StringUtils.equalsIgnoreCase(user.getPhone(), phone)) {
            checkPhoneNumber(phone);
        }

        if (mobile != null && !StringUtils.equalsIgnoreCase(user.getMobile(), mobile)) {
            checkPhoneNumber(mobile);
        }

        return user;
    }

    @Override
    protected void processPatch(final User user, final Map<String, Object> partialDto) {
        final Collection<EventDiffDto> logbooks = new ArrayList<>();
        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case "id":
                case "customerId":
                    break;
                case "email" :
                    logbooks.add(new EventDiffDto(UserConverter.EMAIL_KEY, GPDR_DEFAULT_VALUE, GPDR_DEFAULT_VALUE));
                    user.setEmail(CastUtils.toString(entry.getValue()));
                    break;
                case "firstname" :
                    logbooks.add(new EventDiffDto(UserConverter.FIRSTNAME_KEY, GPDR_DEFAULT_VALUE, GPDR_DEFAULT_VALUE));
                    user.setFirstname(CastUtils.toString(entry.getValue()));
                    break;
                case "lastname" :
                    logbooks.add(new EventDiffDto(UserConverter.LASTNAME_KEY, GPDR_DEFAULT_VALUE, GPDR_DEFAULT_VALUE));
                    user.setLastname(CastUtils.toString(entry.getValue()));
                    break;
                case "language":
                    logbooks.add(new EventDiffDto(UserConverter.LANGUAGE_KEY, user.getLanguage(), entry.getValue()));
                    user.setLanguage(CastUtils.toString(entry.getValue()));
                    break;
                case "type":
                    final String typeAsString = CastUtils.toString(entry.getValue());
                    logbooks.add(new EventDiffDto(UserConverter.TYPE_KEY, user.getType(), typeAsString));
                    user.setType(EnumUtils.stringToEnum(UserTypeEnum.class, typeAsString));
                    break;
                case "level":
                    logbooks.add(new EventDiffDto(UserConverter.LEVEL_KEY, user.getLevel(), entry.getValue()));
                    user.setLevel(CastUtils.toString(entry.getValue()));
                    break;
                case "mobile" :
                    logbooks.add(new EventDiffDto(UserConverter.MOBILE_KEY, GPDR_DEFAULT_VALUE, GPDR_DEFAULT_VALUE));
                    user.setMobile(CastUtils.toString(entry.getValue()));
                    break;
                case "phone" :
                    logbooks.add(new EventDiffDto(UserConverter.PHONE_KEY, GPDR_DEFAULT_VALUE, GPDR_DEFAULT_VALUE));
                    user.setPhone(CastUtils.toString(entry.getValue()));
                    break;

                case "groupId":
                    final GroupDto oldGroup = groupInternalService.getOne(user.getGroupId(), Optional.empty(), Optional.empty());

                    if(CastUtils.toString(entry.getValue()).isEmpty()) {
                        logbooks.add(new EventDiffDto(UserConverter.GROUP_IDENTIFIER_KEY, oldGroup.getIdentifier(), Optional.empty()));
                        user.setGroupId(CastUtils.toString(entry.getValue()));
                    }
                    else {
                    final GroupDto newGroup = groupInternalService.getOne(CastUtils.toString(entry.getValue()), Optional.empty(), Optional.empty());
                    logbooks.add(new EventDiffDto(UserConverter.GROUP_IDENTIFIER_KEY, oldGroup.getIdentifier(), newGroup.getIdentifier()));
                    user.setGroupId(CastUtils.toString(entry.getValue()));
            }
                    break;
                case "status" :
                    final String status = CastUtils.toString(entry.getValue());
                    logbooks.add(new EventDiffDto(UserConverter.STATUS_KEY, user.getStatus(), status));
                    user.setStatus(EnumUtils.stringToEnum(UserStatusEnum.class, status));

                    if(user.getStatus()== UserStatusEnum.DISABLED) {
                        logbooks.add(new EventDiffDto(UserConverter.DISABLING_DATE, user.getDisablingDate(),
                            OffsetDateTime.now()));
                        user.setDisablingDate(OffsetDateTime.now());
                    }
                    if(user.getStatus() == UserStatusEnum.REMOVED) {
                        logbooks.add(new EventDiffDto(UserConverter.REMOVING_DATE, user.getRemovingDate(),
                            OffsetDateTime.now()));
                        user.setRemovingDate(OffsetDateTime.now());
                        user.setDisablingDate(null);
                    }
                    if(user.getStatus() == UserStatusEnum.ENABLED) {
                        user.setDisablingDate(null);
                        user.setRemovingDate(null);
                    }

                    break;
                case "subrogeable" :
                    logbooks.add(new EventDiffDto(UserConverter.SUBROGEABLE_KEY, user.isSubrogeable(), entry.getValue()));
                    user.setSubrogeable(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "otp" :
                    logbooks.add(new EventDiffDto(UserConverter.OTP_KEY, user.isOtp(), entry.getValue()));
                    user.setOtp(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "address" :
                    final Address address = user.getAddress();
                    if (address == null) {
                        user.setAddress(new Address());
                    }
                    addressService.processPatch(user.getAddress(), CastUtils.toMap(entry.getValue()), logbooks, true);
                    break;
                case "internalCode" :
                    logbooks.add(new EventDiffDto(UserConverter.INTERNAL_CODE_KEY, user.getInternalCode(), entry.getValue()));
                    user.setInternalCode(CastUtils.toString(entry.getValue()));
                    break;
                case "siteCode" :
                    logbooks.add(new EventDiffDto(UserConverter.SITE_CODE, user.getSiteCode(), entry.getValue()));
                    user.setSiteCode(CastUtils.toString(entry.getValue()));
                    break;
                default :
                    throw new IllegalArgumentException("Unable to patch group " + user.getId() + ": key " + entry.getKey() + " is not allowed");
            }
        }
        iamLogbookService.updateUserEvent(user, logbooks);
    }

    public void updateOtpForUsersByCustomerId(final boolean otp, final String id) {
        final Query query = new Query(Criteria.where("customerId").is(id));
        final List<User> users = userRepository.findAll(query);
        for (final User u : users) {
            final EventDiffDto evData = new EventDiffDto(UserConverter.OTP_KEY, u.isOtp(), otp);
            u.setOtp(otp);
            userRepository.save(u);
            iamLogbookService.updateUserEvent(u, Arrays.asList(evData));
        }
        final Update update = Update.update("otp", otp);
        userRepository.updateMulti(query, update);

    }

    private User find(final String id, final String customerId, final String message) {
        Assert.isTrue(StringUtils.isNotEmpty(id), message + ": no id");

        Assert.isTrue(StringUtils.equals(customerId, getInternalSecurityService().getCustomerId()), message + ": customerId " + customerId + " is not allowed");

        return getRepository().findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new IllegalArgumentException(message + ": no user found for id " + id + " - customerId " + customerId));
    }

    private void checkIsReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly user ");
    }

    private void checkGroup(final GroupDto groupDto, final String customerId, final String message) {
        Assert.isTrue(groupDto != null, message + ": group does not exist");

        Assert.isTrue(StringUtils.equals(groupDto.getCustomerId(), customerId), message + ": group and user customerId must be equals");

        Assert.isTrue(groupDto.isEnabled(), message + ": group must be enabled");
    }

    private void checkSetReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly must be set to false");
    }

    private void checkEmail(final String email, final String customerId, final String message) {
        Assert.notNull(email, "email : " + email + " format is not allowed");
        Assert.isTrue(Pattern.matches(ApiIamInternalConstants.EMAIL_VALID_REGEXP, email), "email : " + email + " format is not allowed");
        Assert.isNull(getRepository().findByEmail(email), message + ": mail already exists");
        if (email.matches(ADMIN_EMAIL_PATTERN + ".*")) {
            final Query query = new Query();
            query.addCriteria(Criteria.where("email").regex("^" + ADMIN_EMAIL_PATTERN));
            query.addCriteria(Criteria.where("customerId").is(customerId));
            final Optional<User> adminUser = getRepository().findOne(query);
            Assert.isTrue(!adminUser.isPresent(), message + ": admin user already exists");
        }
    }

    private void checkPhoneNumber(final String phoneNumber) {
        Assert.isTrue(Pattern.matches(ApiIamInternalConstants.PHONE_NUMBER_VALID_REGEXP, phoneNumber),
                "Phone Number : " + phoneNumber + " format is not allowed");
    }

    private void checkLevel(final String level, final String message) {
        Assert.isTrue(Pattern.matches(ApiIamInternalConstants.LEVEL_VALID_REGEXP, level), "level : " + level + " format is not allowed");
        Assert.isTrue(internalSecurityService.isLevelAllowed(level), message + ": level " + level + " is not allowed");
    }

    private void checkGroupId(final String groupId, final String message) {
        Assert.isTrue(groupId != null, message + ": groupId must not be null");
    }

    private void checkCustomer(final String customerId, final String message) {

        final Optional<Customer> customer = customerRepository.findById(customerId);
        Assert.isTrue(customer.isPresent(), message + ": customer does not exist");

        Assert.isTrue(customer.get().isEnabled(), message + ": customer must be enabled");
    }

    private void checkOtp(final UserDto userDto) {
        if (UserTypeEnum.GENERIC == userDto.getType() && !userDto.isOtp()) {
            return;
        }
        checkOtp(userDto.isOtp(), userDto.getEmail(), userDto.getMobile(), userDto.getCustomerId());
    }

    private void checkOtp(final User user, final Boolean userHasOtp) {
        checkOtp(userHasOtp, user.getEmail(), user.getMobile(), user.getCustomerId());
    }

    private void checkOtp(final boolean userHasOtp, final String userEmail, final String userMobile, final String customerId) {

        final Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApplicationServerException("Unable to check opt for user " + userEmail + " - Customer  not found: " + customerId));

        if (OtpEnum.MANDATORY.equals(customer.getOtp()) && !userHasOtp) {
            throw new IllegalArgumentException("Unable to disable otp for user:" + userEmail + " . Otp is mandatory this customer");
        }

        if (OtpEnum.DISABLED.equals(customer.getOtp()) && userHasOtp) {
            throw new IllegalArgumentException("Unable to enable otp for user:" + userEmail + " . Otp is mandatory this customer");
        }

        if (userHasOtp && StringUtils.isEmpty(userMobile)) {
            throw new IllegalArgumentException("Unable to enable otp for user:" + userEmail + " without a mobile phone");
        }
    }

    private GroupDto getGroupDtoByIdByPassSecurity(final String groupId, final String message) {
        try {
            return groupInternalService.getOneByPassSecurity(groupId, Optional.empty());
        }
        catch (final NotFoundException e) {
            throw new IllegalArgumentException(message + ": group does not exist");
        }
    }

    private GroupDto getGroupDtoById(final String groupId, final String message) {
        try {
            return groupInternalService.getOne(groupId, Optional.empty(), Optional.empty());
        }
        catch (final NotFoundException e) {
            throw new IllegalArgumentException(message + ": group does not exist");
        }
    }

    @Override
    protected User internalConvertFromDtoToEntity(final UserDto dto) {
        return super.internalConvertFromDtoToEntity(dto);
    }

    @Override
    public UserDto internalConvertFromEntityToDto(final User user) {
        return super.internalConvertFromEntityToDto(user);
    }

    public UserDto getDefaultAdminUser(final String customerId) {
        final Optional<Customer> customer = customerRepository.findById(customerId);
        if (!customer.isPresent()) {
            throw new NotFoundException("No customer found for: " + customerId);
        }
        final String email = ApiIamInternalConstants.ADMIN_CLIENT_PREFIX_EMAIL + CommonConstants.EMAIL_SEPARATOR
                + customer.get().getDefaultEmailDomain().replace(".*", "");

        final ArrayList<CriteriaDefinition> criteria = new ArrayList<>();
        criteria.add(Criteria.where("customerId").in(customerId));
        criteria.add(Criteria.where("email").is(email));

        final List<User> users = getRepository().findAll(criteria);
        if (users.isEmpty()) {
            throw new NotFoundException("No admin user found for email: " + email);
        }
        return convertFromEntityToDto(users.get(0));
    }

    public long countByGroupId(final String profileGroupId) {
        return getRepository().countByGroupId(profileGroupId);
    }

    public void addBasicCustomerAndProofTenantIdentifierInformation(final AuthUserDto userDto) {
        final String id = userDto.getId();
        final Customer customer = customerRepository.findById(userDto.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cannot find customer : " + userDto.getCustomerId() + " of the user: " + id));
        userDto.setCustomerIdentifier(customer.getIdentifier());
        final BasicCustomerDto basicCustomerDto = new BasicCustomerDto();
        basicCustomerDto.setId(customer.getId());
        basicCustomerDto.setIdentifier(customer.getIdentifier());
        basicCustomerDto.setName(customer.getName());
        basicCustomerDto.setCompanyName(customer.getCompanyName());
        if (customer.getGraphicIdentity() != null) {
            final GraphicIdentityDto graphicIdentity = new GraphicIdentityDto();
            graphicIdentity.setHasCustomGraphicIdentity(customer.getGraphicIdentity().isHasCustomGraphicIdentity());
            graphicIdentity.setHeaderDataBase64(customer.getGraphicIdentity().getLogoHeaderBase64());
            graphicIdentity.setFooterDataBase64(customer.getGraphicIdentity().getLogoFooterBase64());
            graphicIdentity.setPortalDataBase64(customer.getGraphicIdentity().getLogoPortalBase64());
            graphicIdentity.setThemeColors(customer.getGraphicIdentity().getThemeColors());
            graphicIdentity.setPortalMessage(customer.getGraphicIdentity().getPortalMessage());
            graphicIdentity.setPortalTitle(customer.getGraphicIdentity().getPortalTitle());
            basicCustomerDto.setGraphicIdentity(graphicIdentity);
        }
        userDto.setBasicCustomer(basicCustomerDto);
        userDto.setProofTenantIdentifier(findTenantByCustomerId(customer.getId(), userDto.getId()).getIdentifier());

    }

    private Tenant findTenantByCustomerId(final String customerId, final String userId) {
        final Optional<Tenant> proofTenant = tenantRepository.findByCustomerId(customerId).stream().filter(Tenant::isProof).findFirst();
        if (!proofTenant.isPresent()) {
            throw new NotFoundException("Cannot find any proof tenant attached for customer : " + customerId + " of the user: " + userId);
        }

        return proofTenant.get();
    }

    public AuthUserDto loadGroupAndProfiles(final UserDto userDto) {

        final AuthUserDto authUserDto = new AuthUserDto(userDto);

        final String groupId = userDto.getGroupId();
        final GroupDto groupDto = getGroupDtoByIdByPassSecurity(groupId, "Unable to embed group");
        authUserDto.setProfileGroup(groupDto);

        final List<String> profileIds = groupDto.getProfileIds();
        final List<ProfileDto> profiles = profileInternalService.getMany(profileIds.toArray(new String[0]));
        if (profiles.size() != profileIds.size()) {
            final List<String> profilesNotFound = profileIds.stream()
                    .filter((profileId) -> profiles.stream().filter((profile) -> profile.getId().equals(profileId)).count() == 0).collect(Collectors.toList());
            LOGGER.error("Unable to embed group {} for user {}, profiles {} don't exist.", groupId, userDto.getId(), profilesNotFound);
            LOGGER.info("profile non trouvé {} ", profilesNotFound);
            LOGGER.info("profile touvé {}", profileIds);
            throw new ApplicationServerException("Unable to embed group " + groupId + " for user " + userDto.getId() + " : one of the profiles does not exist");
        }
        groupDto.setProfiles(profiles);

        profiles.forEach(p -> {
            final Tenant tenant = tenantRepository.findByIdentifier(p.getTenantIdentifier());
            p.setTenantName(tenant.getName());
        });
        return authUserDto;
    }

    @Override
    public void addDataAccessRestrictions(final Collection<CriteriaDefinition> criteria) {
        super.addDataAccessRestrictions(criteria);
    }

    public void addTenantsByAppInformation(final AuthUserDto authUserDto) {

        final Map<String, Set<TenantDto>> tenantsByApp = new HashMap<>();
        if (authUserDto.getProfileGroup().getProfiles() != null) {
            authUserDto.getProfileGroup().getProfiles().stream().filter(profile -> profile.getTenantName() != null).forEach(profile -> {
                if (!tenantsByApp.containsKey(profile.getApplicationName())) {
                    tenantsByApp.put(profile.getApplicationName(), new HashSet<>());
                }
                final TenantDto tenant = VitamUIUtils.copyProperties(tenantRepository.findByIdentifier(profile.getTenantIdentifier()), new TenantDto());
                tenantsByApp.get(profile.getApplicationName()).add(tenant);
            });
        }

        final List<TenantInformationDto> tenantsData = new ArrayList<>();
        tenantsByApp.entrySet().stream().forEach(entry -> {
            final TenantInformationDto appInformations = new TenantInformationDto();
            appInformations.setName(entry.getKey());
            appInformations.setTenants(entry.getValue());
            tenantsData.add(appInformations);
        });

        authUserDto.setTenantsByApp(tenantsData);
    }

    @Override
    public boolean checkExist(final String criteriaJsonString) {
        return super.checkExist(criteriaJsonString);
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected UserRepository getRepository() {
        return userRepository;
    }

    @Override
    protected Converter<UserDto, User> getConverter() {
        return userConverter;
    }

    public JsonNode findHistoryById(final String id) throws VitamClientException {
        LOGGER.debug("findHistoryById for id" + id);
        final Integer tenantIdentifier = internalSecurityService.getTenantIdentifier();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier)
                .setAccessContract(internalSecurityService.getTenant(tenantIdentifier).getAccessContractLogbookIdentifier())
                .setApplicationSessionId(internalSecurityService.getApplicationId());

        final Optional<User> user = getRepository().findById(id);
        user.orElseThrow(() -> new NotFoundException(String.format("No user found with id : %s", id)));
        return logbookService.findEventsByIdentifierAndCollectionNames(user.get().getIdentifier(), MongoDbCollections.USERS, vitamContext).toJsonNode();
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

    public UserDto patchAnalytics(final Map<String, Object> partialDto) {
        checkAnalyticsAllowedFields(partialDto);

        AuthUserDto loggedUser = getMe();
        User user = getUserById(loggedUser.getId());

        partialDto.forEach((key, value) -> {
            switch (key) {
                case APPLICATION_ID:
                    patchApplicationAnalytics(user, CastUtils.toString(value));
                    break;
                case "lastTenantIdentifier":
                    patchLastTenantIdentifier(user, CastUtils.toInteger(value));
                    break;
            }
        });

        return convertFromEntityToDto(getRepository().save(user));
    }

    private User getUserById(final String id) {
        return getRepository().findById(id).orElseThrow(() -> new NotFoundException(String.format("No user found with id : %s", id)));
    }

    private void checkAnalyticsAllowedFields(final Map<String, Object> partialDto) {
        Set<String> analyticsPatchAllowedFields = Set.of(APPLICATION_ID, "lastTenantIdentifier");

        if (MapUtils.isEmpty(partialDto)) {
            throw new IllegalArgumentException("Unable to patch user analytics : payload is empty");
        }

        partialDto.keySet().forEach(key -> {
            if (!analyticsPatchAllowedFields.contains(key)) {
                throw new IllegalArgumentException(String.format("Unable to patch user analytics key : %s is not allowed", key));
            }
        });
    }

    private void patchApplicationAnalytics(final User user, String applicationId) {
        checkApplicationAccessPermission(applicationId);
        user.getAnalytics().tagApplicationAsLastUsed(applicationId);
    }

    private void patchLastTenantIdentifier(final User user, Integer tenantIdentifier) {
        user.getAnalytics().setLastTenantIdentifier(tenantIdentifier);
    }

    private void checkApplicationAccessPermission(String applicationId) {
        List<ApplicationDto> loggedUserApplications = applicationInternalService.getAll(Optional.empty(), Optional.empty());
        boolean userHasPermission = loggedUserApplications.stream().anyMatch(application -> Objects.equals(application.getIdentifier(), applicationId));
        if (!userHasPermission && !applicationId.equals(PORTAL_APP_IDENTIFIER)) {
            throw new IllegalArgumentException(String.format("User has no permission to access to the application : %s", applicationId));
        }
    }


    @Override
    protected Document groupFields(final Optional<String> criteriaJsonString, final String... fields) {
        return super.groupFields(criteriaJsonString, fields);
    }

}
