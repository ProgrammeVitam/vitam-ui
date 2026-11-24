package fr.gouv.vitamui.iam.server.customer.service;

import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.domain.User;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * The OTP policy of a customer and its propagation to the users are updated in one transaction.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
class CustomerServiceUpdateTransactionTest extends AbstractLogbookIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerConverter customerConverter;

    @MockitoSpyBean
    private IamLogbookService iamLogbookServiceSpy;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = IamServerUtilsTest.buildCustomer();
        customer.setOtp(OtpEnum.OPTIONAL);
        customer = customerRepository.save(customer);
        when(securityService.getCustomerId()).thenReturn(customer.getId());
        when(securityService.userIsRootLevel()).thenReturn(true);

        userRepository.save(otpLessUser("user1", "user1@vitamui.com"));
        userRepository.save(otpLessUser("user2", "user2@vitamui.com"));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void an_interrupted_otp_propagation_leaves_the_customer_and_its_users_unchanged() {
        // The propagation fails once the first user has been saved.
        doThrow(new IllegalStateException("interrupted")).when(iamLogbookServiceSpy).updateUserEvent(any(), any());
        final CustomerDto mandatoryOtp = customerConverter.convertEntityToDto(customer);
        mandatoryOtp.setOtp(OtpEnum.MANDATORY);

        assertThatThrownBy(() -> customerService.update(mandatoryOtp)).hasMessage("interrupted");

        assertThat(customerRepository.findById(customer.getId()))
            .get()
            .extracting(Customer::getOtp)
            .isEqualTo(OtpEnum.OPTIONAL);
        assertThat(userRepository.findAll()).extracting(User::isOtp).containsOnly(false);

        // So that a retry propagates the policy again.
        // The user events need the proof tenant of the customer: out of scope here.
        doNothing().when(iamLogbookServiceSpy).updateUserEvent(any(), any());
        customerService.update(mandatoryOtp);

        assertThat(userRepository.findAll()).extracting(User::isOtp).containsOnly(true);
    }

    private User otpLessUser(final String id, final String email) {
        final User user = IamServerUtilsTest.buildUser(id, email, "groupId", customer.getId());
        user.setOtp(false);
        return user;
    }
}
