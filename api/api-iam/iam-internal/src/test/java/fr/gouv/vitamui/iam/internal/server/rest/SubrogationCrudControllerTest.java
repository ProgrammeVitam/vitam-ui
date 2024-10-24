package fr.gouv.vitamui.iam.internal.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.test.rest.AbstractCrudControllerTest;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;

/**
 * Tests the {@link SubrogationInternalController}.
 */
@ExtendWith(MockitoExtension.class)
public final class SubrogationCrudControllerTest extends AbstractCrudControllerTest<SubrogationDto, Owner> {

    private static final String SURROGATE_EMAIL = "sub.roger@vitamui.com";
    private static final String SUPER_USER_EMAIL = "sub.rogateur@vitamui.com";
    private static final String SUPER_USER_CUSTOMER_ID = "systemCustomerId";
    private static final String SURROGATE_CREATE_EMAIL = "surrogate@test.fr";
    private static final String SURROGATE_CUSTOMER_ID = "customerId";
    private static final OffsetDateTime NOW = OffsetDateTime.now();
    private static final User SURROGATE;
    private static final User SURROGATE_CREATE;
    private static final User SUPERUSER;

    static {
        SURROGATE = new User();
        SURROGATE.setId(SURROGATE_EMAIL);
        SURROGATE.setEmail(SURROGATE_EMAIL);
        SURROGATE.setCustomerId(SURROGATE_CUSTOMER_ID);
        SURROGATE.setType(UserTypeEnum.NOMINATIVE);
        SURROGATE.setStatus(UserStatusEnum.ENABLED);
        SURROGATE.setSubrogeable(true);

        SURROGATE_CREATE = new User();
        SURROGATE_CREATE.setId(SURROGATE_CREATE_EMAIL);
        SURROGATE_CREATE.setEmail(SURROGATE_CREATE_EMAIL);
        SURROGATE_CREATE.setCustomerId(SURROGATE_CUSTOMER_ID);
        SURROGATE_CREATE.setType(UserTypeEnum.NOMINATIVE);
        SURROGATE_CREATE.setStatus(UserStatusEnum.ENABLED);
        SURROGATE_CREATE.setSubrogeable(true);

        SUPERUSER = new User();
        SUPERUSER.setId(SUPER_USER_EMAIL);
        SUPERUSER.setEmail(SUPER_USER_EMAIL);
        SUPERUSER.setCustomerId(SUPER_USER_CUSTOMER_ID);
    }

    @InjectMocks
    private SubrogationInternalController controller;

    @Mock
    private UserInternalService internalUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ProfileRepository profilRepository;

    @Mock
    private SubrogationRepository subrogationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private GroupInternalService groupInternalService;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private UserInternalService userInternalService;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private IamLogbookService iamLogbookService;

    @Override
    @BeforeEach
    public void setup() {
        super.setup();
        SubrogationConverter subrogationConverter = new SubrogationConverter(userRepository);
        final SubrogationInternalService service = new SubrogationInternalService(
            sequenceGeneratorService,
            subrogationRepository,
            userRepository,
            userInternalService,
            groupInternalService,
            groupRepository,
            profilRepository,
            internalSecurityService,
            customerRepository,
            subrogationConverter,
            iamLogbookService
        );
        service.setGenericUsersSubrogationTtl(15);
        service.setSubrogationTtl(15);
        controller.setInternalSubrogationService(service);
        controller.setInternalUserService(internalUserService);
    }

    @Override
    protected CrudController<SubrogationDto> getController() {
        return controller;
    }

    @Override
    protected SubrogationDto buildDto() {
        final SubrogationDto dto = new SubrogationDto();
        dto.setStatus(SubrogationStatusEnum.CREATED);
        dto.setDate(NOW);
        dto.setSurrogate(SURROGATE_EMAIL);
        dto.setSuperUser(SUPER_USER_EMAIL);
        return dto;
    }

    @Override
    protected void prepareServices() {
        super.prepareServices();
    }

    @Override
    public void testCreationOK() throws InvalidParseOperationException, PreconditionFailedException {
        final SubrogationDto dto = buildDto();
        dto.setSurrogate(SURROGATE_CREATE_EMAIL);
        dto.setSurrogateCustomerId(SURROGATE_CUSTOMER_ID);
        dto.setSuperUser(SUPER_USER_EMAIL);
        dto.setSuperUserCustomerId(SUPER_USER_CUSTOMER_ID);
        prepareServices();
        when(
            userRepository.findByEmailIgnoreCaseAndCustomerId(SURROGATE_CREATE_EMAIL, SURROGATE_CUSTOMER_ID)
        ).thenReturn(SURROGATE_CREATE);
        when(
            subrogationRepository.findOneBySurrogateAndSurrogateCustomerId(
                SURROGATE_CREATE_EMAIL,
                SURROGATE_CUSTOMER_ID
            )
        ).thenReturn(null);
        when(internalSecurityService.getUser()).thenReturn(
            IamDtoBuilder.buildAuthUserDto("id", SUPER_USER_EMAIL, SUPER_USER_CUSTOMER_ID)
        );
        getController().create(dto);
    }

    @Test
    public void testCreationFailed() throws PreconditionFailedException {
        final SubrogationDto dto = buildDto();
        dto.setSurrogate(SURROGATE_CREATE_EMAIL);
        prepareServices();
        Assertions.assertThrows(IllegalArgumentException.class, () -> getController().create(dto));
    }

    @Override
    @Test
    public void testUpdateOK() throws PreconditionFailedException {
        final SubrogationDto dto = buildDto();
        dto.setId(ID);
        prepareServices();
        Assertions.assertThrows(NotImplementedException.class, () -> getController().update(ID, dto));
    }

    @Override
    @Test
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() throws PreconditionFailedException {
        final SubrogationDto dto = buildDto();
        dto.setId("anotherId");
        prepareServices();
        Assertions.assertThrows(NotImplementedException.class, () -> getController().update(ID, dto));
    }
}
