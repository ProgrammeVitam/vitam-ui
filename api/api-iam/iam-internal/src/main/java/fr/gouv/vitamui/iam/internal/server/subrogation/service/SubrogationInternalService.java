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
package fr.gouv.vitamui.iam.internal.server.subrogation.service;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.logbook.service.EventService;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.logbook.common.EventType;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the subrogations.
 *
 *
 */
@Getter
@Setter
public class SubrogationInternalService extends VitamUICrudService<SubrogationDto, Subrogation> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SubrogationInternalService.class);

    private SubrogationRepository subrogationRepository;

    private UserRepository userRepository;

    private UserInternalService userInternalService;

    private GroupInternalService groupInternalService;

    private GroupRepository groupRepository;

    private ProfileRepository profilRepository;

    private InternalSecurityService internalSecurityService;

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
    public SubrogationInternalService(final CustomSequenceRepository sequenceRepository, final SubrogationRepository subrogationRepository,
            final UserRepository userRepository, final UserInternalService userInternalService, final GroupInternalService groupInternalService,
            final GroupRepository groupRepository, final ProfileRepository profilRepository, final InternalSecurityService internalSecurityService,
            final CustomerRepository customerRepository, final SubrogationConverter subrogationConverter, final IamLogbookService iamLogbookService) {
        super(sequenceRepository);
        this.subrogationRepository = subrogationRepository;
        this.userRepository = userRepository;
        this.userInternalService = userInternalService;
        this.groupInternalService = groupInternalService;
        this.groupRepository = groupRepository;
        this.profilRepository = profilRepository;
        this.internalSecurityService = internalSecurityService;
        this.customerRepository = customerRepository;
        this.subrogationConverter = subrogationConverter;
        this.iamLogbookService = iamLogbookService;
    }

    public SubrogationDto getMySubrogationAsSuperuser() {
        return internalConvertFromEntityToDto(subrogationRepository.findOneBySuperUser(getCurrentUserEmail()));
    }

    protected String getCurrentUserEmail() {
        return internalSecurityService.getUser().getEmail();
    }

    public SubrogationDto getMySubrogationAsSurrogate() {
        return convertFromEntityToDto(subrogationRepository.findOneBySurrogate(getCurrentUserEmail()));
    }

    @Override
    protected Subrogation internalConvertFromDtoToEntity(final SubrogationDto dto) {
        return super.internalConvertFromDtoToEntity(dto);
    }

    @Override
    public SubrogationDto internalConvertFromEntityToDto(final Subrogation entity) {
        return super.internalConvertFromEntityToDto(entity);
    }

    @Override
    protected void beforeCreate(final SubrogationDto dto) {
        super.beforeCreate(dto);
        Assert.isTrue(dto.getStatus().equals(SubrogationStatusEnum.CREATED), "the subrogation must have the status CREATED at the creation");
        checkUsers(dto);

        final int ttlInMinutes;
        if (dto.getStatus().equals(SubrogationStatusEnum.ACCEPTED)) {
            ttlInMinutes = genericUsersSubrogationTtl;
        }
        else {
            ttlInMinutes = subrogationTtl;
        }
        final OffsetDateTime nowPlusXMinutes = OffsetDateTime.now().plusMinutes(ttlInMinutes);
        dto.setDate(nowPlusXMinutes);

        checkSubrogationAlreadyExist(dto.getSurrogate());
        checkSubrogationWithSuperUserAlreadyExist(dto.getSuperUser());
    }

    private void checkSubrogationWithSuperUserAlreadyExist(final String superUser) {
        final Subrogation subro = subrogationRepository.findOneBySuperUser(superUser);
        Assert.isTrue(subro == null, (subro != null ? subro.getSuperUser() : "") + " is already subrogating " + (subro != null ? subro.getSurrogate() : ""));
    }

    private void checkSubrogationAlreadyExist(final String email) {
        final Subrogation subro = subrogationRepository.findOneBySurrogate(email);
        Assert.isTrue(subro == null, email + " is already subrogated by " + (subro != null ? subro.getSuperUser() : ""));
    }

    @Override
    protected void beforeUpdate(final SubrogationDto dto) {
        checkUsers(dto);
    }

    private void checkUsers(final SubrogationDto dto) {

        final String emailSurrogate = dto.getSurrogate();
        final String emailSuperUser = dto.getSuperUser();
        final User surrogate = userRepository.findByEmail(emailSurrogate);
        final User superUser = userRepository.findByEmail(emailSuperUser);
        Assert.isTrue(surrogate != null, "No surrogate found with email : " + emailSurrogate);
        dto.setSurrogateCustomerId(surrogate.getCustomerId());

        final Optional<Customer> optCustomer = customerRepository.findById(surrogate.getCustomerId());
        final Customer surrogateCustomer = optCustomer.orElseThrow(() -> new ApplicationServerException("Unable to check users : customer not found"));

        Assert.isTrue(surrogate.isSubrogeable(), " User is not subrogeable");
        Assert.isTrue(surrogateCustomer.isSubrogeable(), " Customer is not subrogeable");
        Assert.isTrue(surrogate.getStatus().equals(UserStatusEnum.ENABLED), "User status is not enabled");
        Assert.isTrue(superUser != null, "No superUser found with email : " + emailSuperUser);
        dto.setSuperUserCustomerId(superUser.getCustomerId());

        Assert.isTrue(!surrogate.getId().equals(superUser.getId()), "Users cannot subrogate itself, email : " + emailSuperUser);
        final String emailCurrentUser = internalSecurityService.getUser().getEmail();
        Assert.isTrue(StringUtils.equals(emailSuperUser, emailCurrentUser), "Only super user can create subrogation");
        dto.setStatus(UserTypeEnum.GENERIC.equals(surrogate.getType()) ? SubrogationStatusEnum.ACCEPTED : SubrogationStatusEnum.CREATED);
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
            final String emailCurrentUser = internalSecurityService.getUser().getEmail();
            Assert.isTrue(emailSuperUser.equals(emailCurrentUser), "Only super user can stop subrogation");
        }
    }

    public SubrogationDto accept(final String id) {
        final Optional<Subrogation> optSubrogation = subrogationRepository.findById(id);
        final Subrogation subro = optSubrogation
                .orElseThrow(() -> new IllegalArgumentException("Unable to accept subrogation: no subrogation found with id=" + id));
        final String emailCurrentUser = internalSecurityService.getUser().getEmail();

        Assert.isTrue(subro.getSurrogate().equals(emailCurrentUser), "Users " + emailCurrentUser + " can't accept subrogation of " + subro.getSurrogate());
        subro.setStatus(SubrogationStatusEnum.ACCEPTED);

        final Date nowPlusXMinutes = DateUtils.addMinutes(new Date(), subrogationTtl);
        subro.setDate(nowPlusXMinutes);

        final Subrogation createdSubro = subrogationRepository.save(subro);
        return convertFromEntityToDto(createdSubro);
    }

    @Transactional
    public void decline(final String id) {
        final Optional<Subrogation> optSubrogation = subrogationRepository.findById(id);
        final Subrogation subro = optSubrogation
                .orElseThrow(() -> new IllegalArgumentException("Unable to decline subrogation: no subrogation found with id=" + id));

        if (subro.getStatus().equals(SubrogationStatusEnum.ACCEPTED)) {
            iamLogbookService.subrogation(subro, EventType.EXT_VITAMUI_STOP_SURROGATE);
        }
        else {
            iamLogbookService.subrogation(subro, EventType.EXT_VITAMUI_DECLINE_SURROGATE);
        }
        final String emailCurrentUser = internalSecurityService.getUser().getEmail();
        Assert.isTrue(subro.getSurrogate().equals(emailCurrentUser), "Users " + emailCurrentUser + " can't decline subrogation of " + subro.getSurrogate());
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

    public PaginatedValuesDto<UserDto> getUsers(final Integer page, final Integer size, final Optional<String> criteria, final Optional<String> orderBy,
            final Optional<DirectionDto> direction) {
        return userInternalService.getAllPaginatedByPassSecurity(page, size, criteria, orderBy, direction);
    }

    public GroupDto getGroupById(final String id, final Optional<String> embedded) {
        return groupInternalService.getOneByPassSecurity(id, embedded);
    }
}
