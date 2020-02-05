package fr.gouv.vitamui.iam.internal.server.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.NotImplementedException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.rest.CrudController;
import fr.gouv.vitamui.commons.test.rest.AbstractCrudControllerTest;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Tests the {@link SubrogationInternalController}.
 *
 *
 */
public final class SubrogationCrudControllerTest extends AbstractCrudControllerTest<SubrogationDto, Owner> {

    private static final String SURROGATE_EMAIL = "sub.roger@vitamui.com";

    private static final String SUPER_USER_EMAIL = "sub.rogateur@vitamui.com";

    private static final String SURROGATE_CREATE_EMAIL = "surrogate@test.fr";

    private static final OffsetDateTime NOW = OffsetDateTime.now();

    private static final User SURROGATE;

    private static final User SURROGATE_CREATE;

    private static final User SUPERUSER;
    static {
        SURROGATE = new User();
        SURROGATE.setId(SURROGATE_EMAIL);
        SURROGATE.setEmail(SURROGATE_EMAIL);
        SURROGATE.setType(UserTypeEnum.NOMINATIVE);
        SURROGATE.setStatus(UserStatusEnum.ENABLED);
        SURROGATE.setSubrogeable(true);

        SURROGATE_CREATE = new User();
        SURROGATE_CREATE.setId(SURROGATE_CREATE_EMAIL);
        SURROGATE_CREATE.setEmail(SURROGATE_CREATE_EMAIL);
        SURROGATE_CREATE.setType(UserTypeEnum.NOMINATIVE);
        SURROGATE_CREATE.setStatus(UserStatusEnum.ENABLED);
        SURROGATE_CREATE.setSubrogeable(true);

        SUPERUSER = new User();
        SUPERUSER.setId(SUPER_USER_EMAIL);
        SUPERUSER.setEmail(SUPER_USER_EMAIL);
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

    private final CustomSequenceRepository sequenceRepository = mock(CustomSequenceRepository.class);

    private SubrogationConverter subrogationConverter;

    @Mock
    private IamLogbookService iamLogbookService;

    @Override
    @Before
    public void setup() {
        super.setup();
        subrogationConverter = new SubrogationConverter(userRepository);
        final SubrogationInternalService service = new SubrogationInternalService(sequenceRepository, subrogationRepository, userRepository,
                userInternalService, groupInternalService, groupRepository, profilRepository, internalSecurityService, customerRepository, subrogationConverter,
                iamLogbookService);
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
        Mockito.when(subrogationRepository.findOneBySurrogate(SURROGATE_EMAIL)).thenReturn(new Subrogation());
        Mockito.when(userRepository.findByEmail(SURROGATE_EMAIL)).thenReturn(SURROGATE);
        Mockito.when(userRepository.findByEmail(SUPER_USER_EMAIL)).thenReturn(SUPERUSER);
        final Customer customer = new Customer();
        customer.setSubrogeable(true);
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        super.prepareServices();
    }

    @Override
    public void testCreationOK() {
        final SubrogationDto dto = buildDto();
        dto.setSurrogate(SURROGATE_CREATE_EMAIL);
        prepareServices();
        Mockito.when(userRepository.findByEmail(SURROGATE_CREATE_EMAIL)).thenReturn(SURROGATE_CREATE);
        Mockito.when(subrogationRepository.findOneBySurrogate(SURROGATE_CREATE_EMAIL)).thenReturn(null);
        Mockito.when(internalSecurityService.getUser()).thenReturn(IamDtoBuilder.buildAuthUserDto("id", SUPER_USER_EMAIL));
        getController().create(dto);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationFailed() {
        final SubrogationDto dto = buildDto();
        dto.setSurrogate(SURROGATE_CREATE_EMAIL);
        prepareServices();
        Mockito.when(userRepository.findByEmail(SURROGATE_CREATE_EMAIL)).thenReturn(SURROGATE_CREATE);
        Mockito.when(subrogationRepository.findOneBySurrogate(SURROGATE_CREATE_EMAIL)).thenReturn(null);
        final Customer customer = new Customer();
        customer.setSubrogeable(false);
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        getController().create(dto);

    }

    @Override
    @Test(expected = NotImplementedException.class)
    public void testUpdateOK() {
        final SubrogationDto dto = buildDto();
        dto.setId(ID);
        prepareServices();
        getController().update(ID, dto);
    }

    @Override
    @Test(expected = NotImplementedException.class)
    public void testUpdateFailsAsDtoIdAndPathIdAreDifferentOK() {
        final SubrogationDto dto = buildDto();
        dto.setId("anotherId");
        prepareServices();
        getController().update(ID, dto);
    }
}
