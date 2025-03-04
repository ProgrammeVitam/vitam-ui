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

package fr.gouv.vitamui.iam.external.server.subrogation.service;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.external.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.external.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.external.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.external.server.group.service.GroupService;
import fr.gouv.vitamui.iam.external.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.external.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.external.server.security.AbstractResourceClientService;
import fr.gouv.vitamui.iam.external.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.external.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.external.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.external.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.external.server.user.domain.User;
import fr.gouv.vitamui.iam.external.server.user.service.UserService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The service to read, create, update and delete the subrogations.
 */
@Getter
@Setter
public class SubrogationService extends AbstractResourceClientService<SubrogationDto, Subrogation> {

    private SubrogationRepository subrogationRepository;
    private UserRepository userRepository;
    private UserService userService;
    private GroupService groupService;
    private GroupRepository groupRepository;
    private ProfileRepository profilRepository;
    private ExternalSecurityService externalSecurityService;
    private CustomerRepository customerRepository;
    private final SubrogationConverter subrogationConverter;
    private final IamLogbookService iamLogbookService;

    @Value("${subrogation.ttl}")
    @NotNull
    @Setter
    private Integer subrogationTtl;

    @Value("${generic.users.subrogation.ttl}")
    @NotNull
    @Setter
    private Integer genericUsersSubrogationTtl;

    @Autowired
    public SubrogationService(
        final SequenceGeneratorService sequenceGeneratorService,
        final SubrogationRepository subrogationRepository,
        final UserRepository userRepository,
        final UserService userService,
        final GroupService groupService,
        final GroupRepository groupRepository,
        final ProfileRepository profilRepository,
        final ExternalSecurityService externalSecurityService,
        final CustomerRepository customerRepository,
        final SubrogationConverter subrogationConverter,
        final IamLogbookService iamLogbookService
    ) {
        super(sequenceGeneratorService, externalSecurityService);
        this.subrogationRepository = subrogationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.groupRepository = groupRepository;
        this.profilRepository = profilRepository;
        this.externalSecurityService = externalSecurityService;
        this.customerRepository = customerRepository;
        this.subrogationConverter = subrogationConverter;
        this.iamLogbookService = iamLogbookService;
    }

    public SubrogationDto getMySubrogationAsSuperuser() {
        AuthUserDto user = externalSecurityService.getUser();
        return internalConvertFromEntityToDto(
            subrogationRepository.findOneBySuperUserAndSuperUserCustomerId(user.getEmail(), user.getCustomerId())
        );
    }

    public SubrogationDto getMySubrogationAsSurrogate() {
        AuthUserDto user = externalSecurityService.getUser();
        Optional<Customer> optionalCustomer = customerRepository.findById(user.getCustomerId());
        Assert.isTrue(optionalCustomer.isPresent(), " No customer found with id " + user.getCustomerId());

        SubrogationDto subrogationDto = convertFromEntityToDto(
            subrogationRepository.findOneBySurrogateAndSurrogateCustomerId(user.getEmail(), user.getCustomerId())
        );
        if (subrogationDto != null) {
            subrogationDto.setSurrogateCustomerCode(optionalCustomer.get().getCode());
            subrogationDto.setSurrogateCustomerName(optionalCustomer.get().getName());
        }
        return subrogationDto;
    }

    @Override
    protected void beforeCreate(final SubrogationDto dto) {
        super.beforeCreate(dto);
        Assert.isTrue(
            dto.getStatus().equals(SubrogationStatusEnum.CREATED),
            "the subrogation must have the status CREATED at the creation"
        );
        checkUsers(dto);

        final int ttlInMinutes;
        if (dto.getStatus().equals(SubrogationStatusEnum.ACCEPTED)) {
            ttlInMinutes = genericUsersSubrogationTtl;
        } else {
            ttlInMinutes = subrogationTtl;
        }
        final Instant nowPlusXMinutes = Instant.now().plus(ttlInMinutes, ChronoUnit.MINUTES);
        dto.setDate(nowPlusXMinutes);

        checkSubrogationAlreadyExist(dto.getSurrogate(), dto.getSurrogateCustomerId());
        checkSubrogationWithSuperUserAlreadyExist(dto.getSuperUser(), dto.getSuperUserCustomerId());
    }

    private void checkSubrogationWithSuperUserAlreadyExist(final String superUser, final String customerId) {
        final Subrogation subro = subrogationRepository.findOneBySuperUserAndSuperUserCustomerId(superUser, customerId);
        if (subro != null) {
            throw new IllegalArgumentException(
                subro.getSuperUser() +
                "(" +
                subro.getSuperUserCustomerId() +
                ") is already subrogating " +
                subro.getSurrogate() +
                "(" +
                subro.getSurrogateCustomerId() +
                ")"
            );
        }
    }

    private void checkSubrogationAlreadyExist(final String email, final String customerId) {
        final Subrogation subro = subrogationRepository.findOneBySurrogateAndSurrogateCustomerId(email, customerId);
        if (subro != null) {
            throw new IllegalArgumentException(
                subro.getSurrogate() +
                "(" +
                subro.getSurrogateCustomerId() +
                ")" +
                " is already subrogated by " +
                subro.getSuperUser() +
                "(" +
                subro.getSuperUserCustomerId() +
                ")"
            );
        }
    }

    @Override
    protected void beforeUpdate(final SubrogationDto dto) {
        checkUsers(dto);
    }

    private void checkUsers(final SubrogationDto dto) {
        final String emailSurrogate = dto.getSurrogate();
        final String emailSuperUser = dto.getSuperUser();
        final User surrogate = userRepository.findByEmailIgnoreCaseAndCustomerId(
            emailSurrogate,
            dto.getSurrogateCustomerId()
        );
        final User superUser = userRepository.findByEmailIgnoreCaseAndCustomerId(
            emailSuperUser,
            dto.getSuperUserCustomerId()
        );
        Assert.isTrue(surrogate != null, "No surrogate found with email : " + emailSurrogate);
        dto.setSurrogateCustomerId(surrogate.getCustomerId());

        final Optional<Customer> optCustomer = customerRepository.findById(surrogate.getCustomerId());
        final Customer surrogateCustomer = optCustomer.orElseThrow(
            () -> new ApplicationServerException("Unable to check users : customer not found")
        );

        Assert.isTrue(surrogate.isSubrogeable(), " User is not subrogeable");
        Assert.isTrue(surrogateCustomer.isSubrogeable(), " Customer is not subrogeable");
        Assert.isTrue(surrogate.getStatus().equals(UserStatusEnum.ENABLED), "User status is not enabled");
        Assert.isTrue(superUser != null, "No superUser found with email : " + emailSuperUser);
        dto.setSuperUserCustomerId(superUser.getCustomerId());

        Assert.isTrue(
            !surrogate.getId().equals(superUser.getId()),
            "Users cannot subrogate itself, email : " + emailSuperUser
        );
        AuthUserDto currentUser = externalSecurityService.getUser();
        Assert.isTrue(
            StringUtils.equals(emailSuperUser, currentUser.getEmail()),
            "Only super user can create subrogation"
        );
        Assert.isTrue(
            StringUtils.equals(dto.getSuperUserCustomerId(), currentUser.getCustomerId()),
            "Only super user can create subrogation"
        );
        dto.setStatus(
            UserTypeEnum.GENERIC.equals(surrogate.getType())
                ? SubrogationStatusEnum.ACCEPTED
                : SubrogationStatusEnum.CREATED
        );
    }

    @Override
    protected String getObjectName() {
        return "subrogation";
    }

    @Override
    protected VitamUIRepository<Subrogation, String> getRepository() {
        return subrogationRepository;
    }

    @Override
    protected void beforeDelete(final String id) {
        final Optional<Subrogation> subrogation = subrogationRepository.findById(id);
        if (subrogation.isPresent()) {
            final String emailSuperUser = subrogation.get().getSuperUser();
            final String emailCurrentUser = externalSecurityService.getUser().getEmail();
            Assert.isTrue(emailSuperUser.equals(emailCurrentUser), "Only super user can stop subrogation");
        }
    }

    public SubrogationDto accept(final String id) {
        final Optional<Subrogation> optSubrogation = subrogationRepository.findById(id);
        final Subrogation subro = optSubrogation.orElseThrow(
            () -> new IllegalArgumentException("Unable to accept subrogation: no subrogation found with id=" + id)
        );
        final AuthUserDto currentUser = externalSecurityService.getUser();

        Assert.isTrue(
            subro.getSurrogate().equals(currentUser.getEmail()),
            "Users " + currentUser.getEmail() + " can't accept subrogation of " + subro.getSurrogate()
        );
        Assert.isTrue(
            subro.getSurrogateCustomerId().equals(currentUser.getCustomerId()),
            "Users " + currentUser.getCustomerId() + " can't accept subrogation of " + subro.getSurrogate()
        );
        subro.setStatus(SubrogationStatusEnum.ACCEPTED);

        final Date nowPlusXMinutes = DateUtils.addMinutes(new Date(), subrogationTtl);
        subro.setDate(nowPlusXMinutes);

        final Subrogation createdSubro = subrogationRepository.save(subro);
        return convertFromEntityToDto(createdSubro);
    }

    @Transactional
    public void decline(final String id) {
        final Optional<Subrogation> optSubrogation = subrogationRepository.findById(id);
        final Subrogation subro = optSubrogation.orElseThrow(
            () -> new IllegalArgumentException("Unable to decline subrogation: no subrogation found with id=" + id)
        );

        if (subro.getStatus().equals(SubrogationStatusEnum.ACCEPTED)) {
            iamLogbookService.subrogation(subro, EventType.EXT_VITAMUI_STOP_SURROGATE);
        } else {
            iamLogbookService.subrogation(subro, EventType.EXT_VITAMUI_DECLINE_SURROGATE);
        }
        final String emailCurrentUser = externalSecurityService.getUser().getEmail();
        Assert.isTrue(
            subro.getSurrogate().equals(emailCurrentUser),
            "Users " + emailCurrentUser + " can't decline subrogation of " + subro.getSurrogate()
        );
        subrogationRepository.deleteById(id);
    }

    @Override
    protected Class<Subrogation> getEntityClass() {
        return Subrogation.class;
    }

    @Override
    protected Converter<SubrogationDto, Subrogation> getConverter() {
        return subrogationConverter;
    }

    public PaginatedValuesDto<UserDto> getGenericUsers(
        final Integer page,
        final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction
    ) {
        final QueryDto criteriaFiltered = QueryDto.fromJson(criteria);
        criteriaFiltered.addCriterion("type", UserTypeEnum.GENERIC, CriterionOperator.EQUALS);

        return getUsers(page, size, criteriaFiltered.toOptionalJson(), orderBy, direction);
    }

    public PaginatedValuesDto<UserDto> getUsers(
        final Integer page,
        final Integer size,
        final Optional<String> criteria,
        final Optional<String> orderBy,
        final Optional<DirectionDto> direction
    ) {
        return userService.getAllPaginatedByPassSecurity(page, size, criteria, orderBy, direction);
    }

    public GroupDto getGroupById(final String id, final Optional<String> embedded) {
        return groupService.getOneByPassSecurity(id, embedded);
    }

    @Override
    protected Collection<String> getRestrictedKeys() {
        return CollectionUtils.emptyCollection();
    }

    @Override
    protected Collection<String> getAllowedKeys() {
        return List.of("surrogateCustomerId");
    }

    @Override
    protected String getVersionApiCriteria() {
        return CRITERIA_VERSION_V2;
    }
}
