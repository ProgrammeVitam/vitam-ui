/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.commons.security;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

public class PasswordValidatorTest {

    private static final String CREDENTIALS_DETAILS_FILE = "credentialsRepository/userCredentials.json";
    private final String POLICY_PATTERN =
        "'(^(?=(?:.*[a-z]){2,})(?=(?:.*[A-Z]){2,})(?=(?:.*[\\d]){2,})[A-Za-zÀ-ÿ0-9$@!%*#£?&=\\-\\/:;\\(\\)\"\\.,\\?!'\\[\\]{}^\\+\\=_\\\\\\|~<>`]{12,}$)|(^(?=(.*[$@!%*#£?&=\\-\\/:;\\(\\)\"\\.,\\?!'\\[\\]{}^\\+\\=_\\\\\\|~<>`]){2,})(?=(?:.*[A-Z]){2,})(?=(?:.*[\\d]){2,})[A-Za-zÀ-ÿ0-9$@!%*#£?&=\\-\\/:;\\(\\)\"\\.,\\?!'\\[\\]{}^\\+\\=_\\\\\\|~<>`]{12,}$)|(^(?=(.*[$@!%*#£?&=\\-\\/:;\\(\\)\"\\.,\\?!'\\[\\]{}^\\+\\=_\\\\\\|~<>`]){2,})(?=(?:.*[a-z]){2,})(?=(?:.*[\\d]){2,})[A-Za-zÀ-ÿ0-9$@!%*#£?&=\\-\\/:;\\(\\)\"\\.,\\?!'\\[\\]{}^\\+\\=_\\\\\\|~<>`]{12,}$)|(^(?=(.*[$@!%*#£?&=\\-\\/:;\\(\\)\"\\.,\\?!'\\[\\]{}^\\+\\=_\\\\\\|~<>`]){2,})(?=(?:.*[a-z]){2,})(?=(?:.*[A-Z]){2,})[A-Za-zÀ-ÿ0-9$@!%*#£?&=\\-\\/:;\\(\\)\"\\.,\\?!'\\[\\]{}^\\+\\=_\\\\\\|~<>`]{12,}$)'";

    PasswordValidator passwordValidator;

    private JsonNode jsonNode;

    @BeforeEach
    public void setUp() throws FileNotFoundException, InvalidParseOperationException {
        jsonNode = JsonHandler.getFromFile(PropertiesUtils.findFile(CREDENTIALS_DETAILS_FILE));
        passwordValidator = new PasswordValidator();
    }

    @Test
    public void givenPasswordValidThenOK() {
        boolean valid = passwordValidator.isValid(POLICY_PATTERN, jsonNode.findValue("PASSWORD_VALID_1").textValue());
        Assertions.assertThat(valid).isTrue();
    }

    @Test
    public void givenPassword1ValidButContainsOccurrenceOfUserLastNameThenTrue() {
        boolean valid = passwordValidator.isContainsUserOccurrences(
            jsonNode.findValue("USER_LASTNAME").textValue(),
            jsonNode.findValue("PASSWORD_VALID_2").textValue(),
            Integer.parseInt(jsonNode.findValue("MAX_OCCURRENCES_CHARS_TO_CHECK").textValue())
        );
        Assertions.assertThat(valid).isTrue();
    }

    @Test
    public void givenPassword2ValidButContainsOccurrenceOfUserLastNameThenTrue() {
        boolean valid = passwordValidator.isContainsUserOccurrences(
            jsonNode.findValue("USER_LASTNAME").textValue(),
            jsonNode.findValue("PASSWORD_VALID_3").textValue(),
            Integer.parseInt(jsonNode.findValue("MAX_OCCURRENCES_CHARS_TO_CHECK").textValue())
        );
        Assertions.assertThat(valid).isTrue();
    }

    @Test
    public void givenPassword3ValidButContainsOccurrenceOfUserLastNameThenFalse() {
        boolean valid = passwordValidator.isContainsUserOccurrences(
            jsonNode.findValue("USER_LASTNAME").textValue(),
            jsonNode.findValue("PASSWORD_VALID_1").textValue(),
            Integer.parseInt(jsonNode.findValue("MAX_OCCURRENCES_CHARS_TO_CHECK").textValue())
        );
        Assertions.assertThat(valid).isFalse();
    }

    @Test
    public void givenPasswordConfirmationOKThenTrue() {
        boolean valid = passwordValidator.isEqualConfirmed(
            jsonNode.findValue("PASSWORD_VALID_1").textValue(),
            jsonNode.findValue("PASSWORD_VALID_1").textValue()
        );
        Assertions.assertThat(valid).isTrue();
    }

    @Test
    public void givenPasswordConfirmationKOThenFalse() {
        boolean valid = passwordValidator.isEqualConfirmed(
            jsonNode.findValue("PASSWORD_VALID_1").textValue(),
            jsonNode.findValue("PASSWORD_VALID_2").textValue()
        );
        Assertions.assertThat(valid).isFalse();
    }
}
