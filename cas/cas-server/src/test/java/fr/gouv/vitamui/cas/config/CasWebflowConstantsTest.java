package fr.gouv.vitamui.cas.config;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Pins the <em>values</em> of the {@link CasWebflowConstants} the webflow configurers rely on.
 *
 * <p>These constants are {@code static final String}, so javac inlines them into our class files. A CAS upgrade that
 * changes a value while keeping the field name is therefore invisible: {@code VitamLoginWebflowConfigurer} keeps
 * wiring transitions to the value captured at compile time, the module still compiles, and the flow silently points
 * at states CAS no longer knows. Recompiling hides the problem rather than revealing it.
 *
 * <p>Reading the fields reflectively bypasses inlining and returns what the CAS jar actually holds today, so
 * comparing against pinned literals turns that class of change into a build failure.
 */
public class CasWebflowConstantsTest {

    /** Field name -&gt; value, as of CAS 7.0.10.1. */
    private static final Map<String, String> EXPECTED_VALUES = Map.ofEntries(
        entry("ACTION_ID_DELEGATED_AUTHENTICATION", "delegatedAuthenticationAction"),
        entry("ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT", "delegatedAuthenticationClientLogoutAction"),
        entry("ACTION_ID_FRONT_CHANNEL_LOGOUT", "frontChannelLogoutAction"),
        entry("ACTION_ID_INITIAL_FLOW_SETUP", "initialFlowSetupAction"),
        entry("ACTION_ID_INIT_LOGIN_ACTION", "initializeLoginAction"),
        entry("ACTION_ID_MFA_SIMPLE_SEND_TOKEN", "mfaSimpleMultifactorSendTokenAction"),
        entry("ACTION_ID_OTP_AUTHENTICATION_ACTION", "oneTimeTokenAuthenticationWebflowAction"),
        entry("ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION", "surrogateInitialAuthenticationAction"),
        entry("ACTION_ID_TERMINATE_SESSION", "terminateSessionAction"),
        entry("ACTION_ID_TICKET_GRANTING_TICKET_CHECK", "ticketGrantingTicketCheckAction"),
        entry("BEAN_NAME_FLOW_BUILDER_SERVICES", "flowBuilderServices"),
        entry("BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY", "loginFlowRegistry"),
        entry("BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY", "logoutFlowRegistry"),
        entry("STATE_ID_ACCOUNT_DISABLED", "casAccountDisabledView"),
        entry("STATE_ID_GATEWAY_REQUEST_CHECK", "gatewayRequestCheck"),
        entry("STATE_ID_HAS_SERVICE_CHECK", "hasServiceCheck"),
        entry("STATE_ID_INIT_LOGIN_FORM", "initializeLoginForm"),
        entry("STATE_ID_MUST_CHANGE_PASSWORD", "casMustChangePassView"),
        entry("STATE_ID_REAL_SUBMIT", "realSubmit"),
        entry("STATE_ID_SEND_RESET_PASSWORD_ACCT_INFO", "casResetPasswordSendInstructionsView"),
        entry("STATE_ID_SIMPLE_MFA_SEND_TOKEN", "sendSimpleToken"),
        entry("STATE_ID_STOP_WEBFLOW", "stopWebflow"),
        entry("STATE_ID_SUCCESS", "success"),
        entry("STATE_ID_TERMINATE_SESSION", "terminateSession"),
        entry("STATE_ID_TICKET_GRANTING_TICKET_CHECK", "ticketGrantingTicketCheck"),
        entry("STATE_ID_UNAVAILABLE", "unavailable"),
        entry("STATE_ID_VIEW_LOGIN_FORM", "viewLoginForm"),
        entry("TRANSITION_ID_ERROR", "error"),
        entry("TRANSITION_ID_GENERATE", "generate"),
        entry("TRANSITION_ID_RESEND", "resend"),
        entry("TRANSITION_ID_RESET_PASSWORD", "resetPassword"),
        entry("TRANSITION_ID_STOP", "stop"),
        entry("TRANSITION_ID_SUBMIT", "submit"),
        entry("TRANSITION_ID_SUCCESS", "success"),
        entry("TRANSITION_ID_TICKET_GRANTING_TICKET_INVALID", "invalid"),
        entry("TRANSITION_ID_TICKET_GRANTING_TICKET_NOT_EXISTS", "notExists"),
        entry("TRANSITION_ID_TICKET_GRANTING_TICKET_VALID", "valid"),
        entry("VAR_ID_CREDENTIAL", "credential")
    );

    @Test
    public void constantsUsedByTheWebflowKeepTheirValue() throws Exception {
        final Map<String, String> missing = new TreeMap<>();
        final Map<String, String> actual = new LinkedHashMap<>();

        for (final String fieldName : EXPECTED_VALUES.keySet()) {
            try {
                final Field field = CasWebflowConstants.class.getField(fieldName);
                actual.put(fieldName, String.valueOf(field.get(null)));
            } catch (final NoSuchFieldException e) {
                missing.put(fieldName, EXPECTED_VALUES.get(fieldName));
            }
        }

        assertThat(missing).as("CasWebflowConstants fields used by the webflow configurers that CAS removed").isEmpty();

        assertThat(actual)
            .as(
                "CasWebflowConstants values changed. These constants are inlined at compile time, so the webflow " +
                "configurers still wire the old values and the module keeps compiling: review every use " +
                "before updating this list."
            )
            .containsExactlyInAnyOrderEntriesOf(EXPECTED_VALUES);
    }
}
