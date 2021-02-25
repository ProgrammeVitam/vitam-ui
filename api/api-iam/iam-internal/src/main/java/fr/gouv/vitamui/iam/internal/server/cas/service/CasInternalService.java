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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
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
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.token.domain.Token;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import lombok.Getter;
import lombok.Setter;

/**
 * Specific CAS service.
 *
 *
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

    @SuppressWarnings("unused")
    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CasInternalService.class);

    private static final UniqueTicketIdGenerator TICKET_GENERATOR = new DefaultUniqueTicketIdGenerator();

    @Transactional
    public void updatePassword(final String email, final String rawPassword) {
        final User user = checkUserInformations(email);
        final Optional<Customer> optCustomer = customerRepository.findById(user.getCustomerId());
        final Customer customer = optCustomer.orElseThrow(() -> new ApplicationServerException("Unable to update password : customer not found"));

        final List<String> oldPasswords = user.getOldPasswords();
        if (oldPasswords != null && !oldPasswords.isEmpty()) {
            for (final String oldPassword : oldPasswords) {
                if (passwordEncoder.matches(rawPassword, oldPassword)) {
                    throw new ConflictException("The given password has already been used in the past");
                }
            }
        }

        final String encodedPassword = passwordEncoder.encode(rawPassword);
        internalUserService.saveCurrentPasswordInOldPasswords(user, encodedPassword);

        final String existingPassword = user.getPassword();

        user.setPassword(encodedPassword);
        final OffsetDateTime nowPlusPasswordRevocationDelay = OffsetDateTime.now().plusMonths(customer.getPasswordRevocationDelay());
        user.setPasswordExpirationDate(nowPlusPasswordRevocationDelay);

        userRepository.save(user);

        if (StringUtils.isEmpty(existingPassword)) {
            iamLogbookService.createPasswordEvent(user);
        }
        else {
            iamLogbookService.updatePasswordEvent(user);
        }
    }

    @Transactional
    public void updateNbFailedAttempsPlusLastConnectionAndStatus(final User user, final int nbFailedAttempts, final UserStatusEnum oldStatus) {
        final UserStatusEnum newStatus = user.getStatus();
        final Query query = new Query(Criteria.where(ID).is(user.getId()));
        final Update update = Update.update(NB_FAILED_ATTEMPTS, nbFailedAttempts).set(LAST_CONNECTION, OffsetDateTime.now()).set(STATUS, newStatus);
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
        }
        else if (UserTypeEnum.NOMINATIVE != user.getType()) {
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

        }
        else {
            return userDto;
        }
    }

    private void createEventsSubrogation(final UserDto surrogate, final boolean isSubrogation) {
        if (isSubrogation) {
            final Subrogation subro = subrogationRepository.findOneBySurrogate(surrogate.getEmail());
            final EventType type;
            if (surrogate.getType().equals(UserTypeEnum.GENERIC)) {
                type = EventType.EXT_VITAMUI_START_SURROGATE_GENERIC;
            }
            else {
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
        }
        else if (isApi) {
            ttlInMinutes = apiTokenTtl;
        }
        else {
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
        }
        else {
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
