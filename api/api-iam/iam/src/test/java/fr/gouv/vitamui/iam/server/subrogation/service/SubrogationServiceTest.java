package fr.gouv.vitamui.iam.server.subrogation.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.common.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link SubrogationService}.
 */
@ExtendWith(MockitoExtension.class)
public final class SubrogationServiceTest {

    public static final String USER1_EMAIL = "one@vitamui.com";
    public static final String USER1_CUSTOMER_ID = "customerId2";
    public static final String USER2_EMAIL = "two@vitamui.com";
    public static final String USER2_CUSTOMER_ID = "customerId1";
    public static final String USER3_CUSTOMER_ID = "customerId3";
    public static final String USER3_EMAIL = "three@vitamui.com";
    public static final String BAD_USER_EMAIL = "baduser@vitamui.com";
    public static final String SUBROGATION_ID = "ID_SUB";
    private SubrogationService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ProfileRepository profilRepository;

    @Mock
    private SubrogationRepository subrogationRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @Mock
    private IamLogbookService iamLogbookService;

    @BeforeEach
    public void setUp() {
        SubrogationConverter subrogationConverter = new SubrogationConverter(userRepository);
        service = new SubrogationService(
            sequenceGeneratorService,
            subrogationRepository,
            userRepository,
            userService,
            groupService,
            groupRepository,
            profilRepository,
            securityService,
            customerRepository,
            subrogationConverter,
            iamLogbookService
        );
        service.setGenericUsersSubrogationTtl(15);
        service.setSubrogationTtl(15);
    }

    @Test
    public void testCreateSubrogation() {
        final Customer customer = buildCustomer();
        final User user1 = new User();
        final User user2 = new User();
        final AuthUserDto extUser1 = IamDtoBuilder.buildAuthUserDto("id", USER1_EMAIL, USER1_CUSTOMER_ID);
        final AuthUserDto extUser2 = IamDtoBuilder.buildAuthUserDto("id2", USER2_EMAIL, USER2_CUSTOMER_ID);
        VitamUIUtils.copyProperties(extUser1, user1);
        VitamUIUtils.copyProperties(extUser2, user2);
        final Subrogation subro = buildSubrogation();
        Mockito.when(securityService.getUser()).thenReturn(
            IamDtoBuilder.buildAuthUserDto("id", USER1_EMAIL, USER1_CUSTOMER_ID)
        );
        Mockito.when(subrogationRepository.save(ArgumentMatchers.any())).thenReturn(subro);
        Mockito.when(userRepository.findByEmailIgnoreCaseAndCustomerId(USER2_EMAIL, USER2_CUSTOMER_ID)).thenReturn(
            user2
        );
        Mockito.when(userRepository.findByEmailIgnoreCaseAndCustomerId(USER1_EMAIL, USER1_CUSTOMER_ID)).thenReturn(
            user1
        );
        Mockito.when(customerRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(customer));

        final SubrogationDto subroToCreate = new SubrogationDto();
        VitamUIUtils.copyProperties(subro, subroToCreate);
        subroToCreate.setId(null);
        final SubrogationDto dto = service.create(subroToCreate);
        assertNotNull(dto, "Subrogation shouldn't be null");
        assertNotNull(dto.getId(), "Subrogation should have an id");
        assertThat(dto.getSuperUser()).isEqualTo(subroToCreate.getSuperUser());
        assertThat(dto.getSuperUserCustomerId()).isEqualTo(subroToCreate.getSuperUserCustomerId());
        assertThat(dto.getSurrogate()).isEqualTo(subroToCreate.getSurrogate());
        assertThat(dto.getSurrogateCustomerId()).isEqualTo(subroToCreate.getSurrogateCustomerId());
        assertThat(dto.getStatus()).isEqualTo(SubrogationStatusEnum.CREATED);
    }

    /**
     * Test that an user cannot create a subrogation for another user.
     */
    @Test
    public void testCreateSubrogationForAnotherUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            final Customer customer = buildCustomer();
            final User user1 = new User();
            final User user2 = new User();
            final User user3 = new User();
            final AuthUserDto extUser1 = IamDtoBuilder.buildAuthUserDto("id", USER1_EMAIL, USER1_CUSTOMER_ID);
            final AuthUserDto extUser2 = IamDtoBuilder.buildAuthUserDto("id2", USER2_EMAIL, USER2_CUSTOMER_ID);
            final AuthUserDto extUser3 = IamDtoBuilder.buildAuthUserDto("id3", USER3_EMAIL, USER3_CUSTOMER_ID);
            VitamUIUtils.copyProperties(extUser1, user1);
            VitamUIUtils.copyProperties(extUser2, user2);
            VitamUIUtils.copyProperties(extUser3, user3);
            final Subrogation subro = buildSubrogation();
            Mockito.when(userRepository.findByEmailIgnoreCaseAndCustomerId(USER2_EMAIL, USER2_CUSTOMER_ID)).thenReturn(
                user2
            );
            Mockito.when(customerRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(customer));

            final SubrogationDto subroToCreate = new SubrogationDto();
            VitamUIUtils.copyProperties(subro, subroToCreate);
            subroToCreate.setId(null);
            subroToCreate.setSuperUser(USER3_EMAIL);
            final SubrogationDto dto = service.create(subroToCreate);
            assertNotNull(dto, "Subrogation shouldn't be null");
            assertNotNull(dto.getId(), "Subrogation should have an id");
            assertThat(dto.getSuperUser()).isEqualTo(subroToCreate.getSuperUser());
            assertThat(dto.getSurrogate()).isEqualTo(subroToCreate.getSurrogate());
            assertThat(dto.getStatus()).isEqualTo(SubrogationStatusEnum.CREATED);
        });
    }

    @Test
    public void testAccept() {
        final String idSubro = "ID";
        final Subrogation subro = buildSubrogation();
        Mockito.when(subrogationRepository.findById(idSubro)).thenReturn(Optional.of(subro));
        Mockito.when(securityService.getUser()).thenReturn(
            IamDtoBuilder.buildAuthUserDto("id", USER2_EMAIL, USER2_CUSTOMER_ID)
        );
        Mockito.when(subrogationRepository.save(ArgumentMatchers.any())).thenAnswer(
            AdditionalAnswers.returnsFirstArg()
        );
        final SubrogationDto dto = service.accept(idSubro);
        assertThat(dto.getStatus()).isEqualTo(SubrogationStatusEnum.ACCEPTED);
    }

    @Test
    public void testAcceptBadUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            final Subrogation subro = buildSubrogation();
            Mockito.when(subrogationRepository.findById(SUBROGATION_ID)).thenReturn(Optional.of(subro));
            Mockito.when(securityService.getUser()).thenReturn(
                IamDtoBuilder.buildAuthUserDto("id", BAD_USER_EMAIL, USER1_CUSTOMER_ID)
            );
            try {
                service.accept(SUBROGATION_ID);
                fail();
            } catch (final IllegalArgumentException e) {
                assertThat(e.getMessage()).isEqualTo(
                    "Users " + BAD_USER_EMAIL + " can't accept subrogation of " + subro.getSurrogate()
                );
                throw e;
            }
        });
    }

    @Test
    public void testDecline() {
        final Subrogation subro = buildSubrogation();
        Mockito.when(subrogationRepository.findById(SUBROGATION_ID)).thenReturn(Optional.of(subro));
        Mockito.when(securityService.getUser()).thenReturn(
            IamDtoBuilder.buildAuthUserDto("id", USER2_EMAIL, USER2_CUSTOMER_ID)
        );
        service.decline(SUBROGATION_ID);
    }

    @Test
    public void testDeclineBadUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            final Subrogation subro = buildSubrogation();
            Mockito.when(subrogationRepository.findById(SUBROGATION_ID)).thenReturn(Optional.of(subro));
            Mockito.when(securityService.getUser()).thenReturn(
                IamDtoBuilder.buildAuthUserDto("id", BAD_USER_EMAIL, USER2_CUSTOMER_ID)
            );
            try {
                service.decline(SUBROGATION_ID);
                fail();
            } catch (final IllegalArgumentException e) {
                assertThat(e.getMessage()).isEqualTo(
                    "Users " + BAD_USER_EMAIL + " can't decline subrogation of " + subro.getSurrogate()
                );
                throw e;
            }
        });
    }

    private Subrogation buildSubrogation() {
        final Subrogation dto = new Subrogation();
        dto.setDate(new Date());
        dto.setSuperUser(USER1_EMAIL);
        dto.setSuperUserCustomerId(USER1_CUSTOMER_ID);
        dto.setSurrogate(USER2_EMAIL);
        dto.setSurrogateCustomerId(USER2_CUSTOMER_ID);
        dto.setId(SUBROGATION_ID);
        dto.setStatus(SubrogationStatusEnum.CREATED);
        return dto;
    }

    private Customer buildCustomer() {
        final String generatedString = generateRandomString();
        final Customer customer = IamServerUtilsTest.buildCustomer(
            null,
            "Integration tests : " + generatedString,
            Integer.toString(ThreadLocalRandom.current().nextInt(1000000, 10000000)),
            List.of(CommonConstants.EMAIL_SEPARATOR + generatedString)
        );
        customer.setSubrogeable(true);
        return customer;
    }

    private String generateRandomString() {
        return RandomStringUtils.random(10, true, false);
    }
}
