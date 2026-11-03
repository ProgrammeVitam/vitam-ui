package fr.gouv.vitamui.iam.server.user.service;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code UserService.getLevels} builds its criteria like the paginated search (only allowed criteria keys
 * are accepted, results restricted to the caller's customer), and the history of a user is only available
 * for users the caller can read.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
class UserServiceCustomerScopeTest extends AbstractLogbookIntegrationTest {

    private static final String CUSTOMER_A = "customerA-caller";
    private static final String CUSTOMER_B = "customerB-other";
    private static final String LEVEL_A = "A.PUBLIC";
    private static final String LEVEL_B = "B.SECRET.ORGANISATION";
    private static final String OTHER_PASSWORD = "Other-Pa55word!";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomSequenceRepository sequenceRepository;

    private SecurityService securityServiceMock;

    private UserService userService;

    private String otherUserId;

    @BeforeEach
    void setUp() {
        securityServiceMock = Mockito.mock(SecurityService.class);
        userService = new UserService(
            new SequenceGeneratorService(sequenceRepository),
            userRepository,
            null,
            null,
            null,
            null,
            securityServiceMock,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        // The caller is authenticated on customer A, root level, allowed to read generic users, but
        // without ROLE_GET_USERS_ALL_CUSTOMERS: its scope is customer A only.
        Mockito.lenient().when(securityServiceMock.getCustomerId()).thenReturn(CUSTOMER_A);
        Mockito.lenient().when(securityServiceMock.userIsRootLevel()).thenReturn(true);
        Mockito.lenient().when(securityServiceMock.hasRole(ServicesData.ROLE_GENERIC_USERS)).thenReturn(true);
        Mockito.lenient()
            .when(securityServiceMock.hasRole(ServicesData.ROLE_GET_USERS_ALL_CUSTOMERS))
            .thenReturn(false);
        Mockito.lenient()
            .when(securityServiceMock.getTenant(Mockito.any()))
            .thenReturn(IamServerUtilsTest.buildTenantDto());
        Mockito.lenient().when(securityServiceMock.getApplicationId()).thenReturn("app");

        userRepository.save(IamServerUtilsTest.buildUser("callerA", "caller@client-a.fr", "gA", CUSTOMER_A, LEVEL_A));

        final User other = IamServerUtilsTest.buildUser("other", "other@client-b.fr", "gB", CUSTOMER_B, LEVEL_B);
        other.setPassword(new BCryptPasswordEncoder().encode(OTHER_PASSWORD));
        otherUserId = userRepository.save(other).getId();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void levels_are_scoped_to_the_callers_customer() {
        final List<String> levels = userService.getLevels(Optional.empty());

        assertThat(levels).contains(LEVEL_A);
        assertThat(levels).doesNotContain(LEVEL_B);
    }

    @Test
    void a_criterion_on_a_non_allowed_key_is_rejected() {
        // password is not an allowed criteria key: the whole request is refused.
        final Optional<String> criteria = startsWith("password", "$2a$10$");

        assertThatThrownBy(() -> userService.getLevels(criteria)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void history_of_another_customers_user_is_not_found() {
        assertThatThrownBy(() -> userService.findHistoryById(otherUserId)).isInstanceOf(NotFoundException.class);
    }

    private Optional<String> startsWith(final String field, final String value) {
        return QueryDto.criteria(field, value, CriterionOperator.STARTWITH).toOptionalJson();
    }
}
