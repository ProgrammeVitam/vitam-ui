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
package fr.gouv.vitamui.cas.pm;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.cas.BaseWebflowActionTest;
import fr.gouv.vitamui.cas.provider.ProvidersService;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityAutoConfiguration;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.external.client.CasExternalRestClient;
import lombok.val;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Tests {@link IamPasswordManagementService}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServerIdentityAutoConfiguration.class, PasswordConfiguration.class})
@TestPropertySource(locations = "classpath:/application-test.properties")
public final class IamPasswordManagementServiceTest extends BaseWebflowActionTest {

    private static final String CREDENTIALS_DETAILS_FILE = "credentialsRepository/userCredentials.json";

    private IamPasswordManagementService service;

    private CasExternalRestClient casExternalRestClient;

    private ProvidersService providersService;

    private Map<String, List<Object>> authAttributes;

    private IdentityProviderDto identityProviderDto;

    private IdentityProviderHelper identityProviderHelper;

    private PasswordValidator passwordValidator;

    private Principal principal;

    private PasswordManagementProperties passwordManagementProperties;

    @Value("${cas.authn.pm.core.password-policy-pattern}")
    private String policyPattern;

    private PasswordConfiguration passwordConfiguration;

    private JsonNode jsonNode;

    @Before
    public void setUp() throws FileNotFoundException, InvalidParseOperationException {
        super.setUp();

        jsonNode =
            JsonHandler.getFromFile(PropertiesUtils.findFile(CREDENTIALS_DETAILS_FILE));
        casExternalRestClient = mock(CasExternalRestClient.class);
        providersService = mock(ProvidersService.class);
        passwordValidator = new PasswordValidator();
        identityProviderHelper = mock(IdentityProviderHelper.class);
        identityProviderDto = new IdentityProviderDto();
        identityProviderDto.setInternal(true);
        passwordManagementProperties = new PasswordManagementProperties();
        passwordManagementProperties.getCore().setPasswordPolicyPattern(encode(policyPattern));
        passwordConfiguration = new PasswordConfiguration();
        passwordConfiguration.setCheckOccurrence(true);
        passwordConfiguration.setOccurrencesCharsNumber(4);
        when(identityProviderHelper.findByUserIdentifier(any(List.class),
            eq(jsonNode.findValue("EMAIL").textValue()))).thenReturn(Optional.of(identityProviderDto));
        UserDto userDto = new UserDto();
        userDto.setLastname("ADMIN");
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
            eq(jsonNode.findValue("EMAIL").textValue()), any(Optional.class))).thenReturn(userDto);
        val utils = new Utils(null, 0, null, null, "");
        service =
            new IamPasswordManagementService(passwordManagementProperties, null, null, null, casExternalRestClient,
                providersService, identityProviderHelper, null, utils, null, passwordValidator, passwordConfiguration);
        final Map<String, AuthenticationHandlerExecutionResult> successes = new HashMap<>();
        successes.put("fake", null);
        authAttributes = new HashMap<>();
        principal = mock(Principal.class);
        flowParameters.put("authentication", new DefaultAuthentication(
            ZonedDateTime.now(),
            principal,
            new ArrayList<MessageDescriptor>(),
            new ArrayList<CredentialMetaData>(),
            authAttributes,
            successes,
            new HashMap<String, Throwable>()
        ));
    }

    @Test
    public void testChangePasswordSuccessfully() {
        assertTrue(service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("PASSWORD").textValue()),
            new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("PASSWORD").textValue(), jsonNode.findValue("PASSWORD").textValue())));
    }

    @Test
    public void testChangePasswordFailureNotMatchConfirmed() {
        assertThatCode(() -> service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("NOT_PASSWORD").textValue()),
            new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("PASSWORD").textValue(),
                jsonNode.findValue("NOT_PASSWORD").textValue()))).
            isInstanceOf(IamPasswordManagementService.PasswordConfirmException.class);
    }

    @Test
    public void testChangePasswordFailureNotConformWithRegex() {
        assertThatCode(() -> service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("BAD_PASSWORD").textValue()),
            new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("BAD_PASSWORD").textValue(),
                jsonNode.findValue("BAD_PASSWORD").textValue()))).
            isInstanceOf(IamPasswordManagementService.PasswordNotMatchRegexException.class);
    }

    @Test
    public void testChangePasswordFailureBecauseOfPresenceOfUsernameOccurenceInPassword() {
        try {
            assertTrue(service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY").textValue()),
                new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY").textValue())));
            fail("should fail");
        } catch (final IamPasswordManagementService.PasswordContainsUserDictionaryException e) {
            assertEquals("Invalid password containing an occurence of user name !", e.getValidationMessage());
        }
    }

    @Test
    public void testChangePasswordFailureBecauseOfPresenceOfUsernameOccurenceInsensitiveCaseInPassword() {
        try {
            assertTrue(service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY_INSENSITIVE").textValue()),
                new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY_INSENSITIVE").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY_INSENSITIVE").textValue())));
            fail("should fail");
        } catch (final IamPasswordManagementService.PasswordContainsUserDictionaryException e) {
            assertEquals("Invalid password containing an occurence of user name !", e.getValidationMessage());
        }
    }

    @Test
    public void testChangePasswordFailureBecauseOfGenericUser() {
        try {
            UserDto userDto = new UserDto();
            userDto.setType(UserTypeEnum.GENERIC);
            when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
                eq(jsonNode.findValue("EMAIL").textValue()),
                any(Optional.class))).thenReturn(userDto);
            assertTrue(service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue()),
                new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue(), jsonNode.findValue("PASSWORD").textValue())));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("user last name can not be null", e.getMessage());
        }
    }

    @Test
    public void testChangePasswordOKWhenUsernameLengthIsLowerThanCheckOccurrenceCharNumber() {
        UserDto userDto = new UserDto();
        userDto.setLastname("ADMI");
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
            eq(jsonNode.findValue("EMAIL").textValue()),
            any(Optional.class))).thenReturn(userDto);
        assertTrue(service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("PASSWORD").textValue()),
            new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("PASSWORD").textValue(), jsonNode.findValue("PASSWORD").textValue())));
    }

    @Test
    public void testChangePasswordFailureBecausePasswordContaisnFullUsernameThenReturnException() {
        try {
            UserDto userDto = new UserDto();
            userDto.setLastname("ADMIN");
            when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
                eq(jsonNode.findValue("EMAIL").textValue()),
                any(Optional.class))).thenReturn(userDto);
            assertTrue(service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY").textValue()),
                new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY").textValue(),
                    jsonNode.findValue("PASSWORD_CONTAINS_DICTIONARY").textValue())));
            fail("should fail");
        } catch (final IamPasswordManagementService.PasswordContainsUserDictionaryException e) {
            assertEquals("Invalid password containing an occurence of user name !", e.getValidationMessage());
        }
    }

    @Test
    public void testChangePasswordFailsBecauseOfASuperUser() {
        authAttributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL,
            Collections.singletonList("fakeSuperUser"));

        try {
            service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue()),
                new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue(),
                    jsonNode.findValue("PASSWORD").textValue()));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("cannot use password management with subrogation", e.getMessage());
        }
    }

    @Test
    public void testChangePasswordFailsBecauseOfASuperUser2() {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put(SUPER_USER_ATTRIBUTE, Collections.singletonList("fakeSuperUser"));
        attributes.put(SUPER_USER_CUSTOMER_ID_ATTRIBUTE, Collections.singletonList("fakeSuperUserCustomerId"));
        when(principal.getAttributes()).thenReturn(attributes);

        try {
            service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue()),
                new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue(),
                    jsonNode.findValue("PASSWORD").textValue()));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("cannot use password management with subrogation", e.getMessage());
        }
    }

    @Test
    public void testChangePasswordFailsBecauseUserIsExternal() {
        identityProviderDto.setInternal(null);

        try {
            service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(), null),
                new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue(),
                    jsonNode.findValue("PASSWORD").textValue()));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "only an internal user [" + jsonNode.findValue("EMAIL").textValue() + "] can change his password",
                e.getMessage());
        }
    }

    @Test
    public void testChangePasswordFailsBecauseUserIsNotLinkedToAnIdentityProvider() {
        when(identityProviderHelper.findByUserIdentifier(any(List.class),
            eq(jsonNode.findValue("EMAIL").textValue()))).thenReturn(Optional.empty());

        try {
            service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(), null),
                new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                    jsonNode.findValue("PASSWORD").textValue(),
                    jsonNode.findValue("PASSWORD").textValue()));
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("only a user [" + jsonNode.findValue("EMAIL").textValue() +
                "] linked to an identity provider can change his password", e.getMessage());
        }
    }

    @Test
    public void testChangePasswordFailsAtServer() {
        doThrow(new InvalidAuthenticationException("")).when(casExternalRestClient)
            .changePassword(any(ExternalHttpContext.class), any(String.class), any(String.class), any(String.class));

        assertFalse(service.change(new UsernamePasswordCredential(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("PASSWORD").textValue()),
            new PasswordChangeRequest(jsonNode.findValue("EMAIL").textValue(),
                jsonNode.findValue("PASSWORD").textValue(), jsonNode.findValue("PASSWORD").textValue())));
    }

    @Test
    public void testFindEmailOk() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
            eq(jsonNode.findValue("EMAIL").textValue()),
            eq(Optional.empty())))
            .thenReturn(user(UserStatusEnum.ENABLED));

        assertEquals(jsonNode.findValue("EMAIL").textValue(), service.findEmail(PasswordManagementQuery.builder()
            .username(jsonNode.findValue("EMAIL").textValue()).build()));
    }

    @Test
    public void testFindEmailErrorThrown() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
            eq(jsonNode.findValue("EMAIL").textValue()),
            eq(Optional.empty())))
            .thenThrow(new BadRequestException("error"));

        assertNull(service.findEmail(
            PasswordManagementQuery.builder().username(jsonNode.findValue("EMAIL").textValue()).build()));
    }

    @Test
    public void testFindEmailUserNull() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
            eq(jsonNode.findValue("EMAIL").textValue()),
            eq(Optional.empty())))
            .thenReturn(null);

        assertNull(service.findEmail(
            PasswordManagementQuery.builder().username(jsonNode.findValue("EMAIL").textValue()).build()));
    }

    @Test
    public void testFindEmailUserDisabled() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
            eq(jsonNode.findValue("EMAIL").textValue()),
            eq(Optional.empty())))
            .thenReturn(user(UserStatusEnum.DISABLED));

        assertNull(service.findEmail(
            PasswordManagementQuery.builder().username(jsonNode.findValue("EMAIL").textValue()).build()));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetSecurityQuestionsOk() {
        when(casExternalRestClient.getUserByEmail(any(ExternalHttpContext.class),
            eq(jsonNode.findValue("EMAIL").textValue()),
            eq(Optional.empty())))
            .thenReturn(user(UserStatusEnum.ENABLED));

        service.getSecurityQuestions(
            PasswordManagementQuery.builder().username(jsonNode.findValue("EMAIL").textValue()).build());
    }

    private UserDto user(final UserStatusEnum status) {
        val user = new UserDto();
        user.setStatus(status);
        user.setEmail(jsonNode.findValue("EMAIL").textValue());
        return user;
    }

    /*
     * application properties are by default encod with */
    private String encode(String policyPattern) {
        return new String(policyPattern.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

}
