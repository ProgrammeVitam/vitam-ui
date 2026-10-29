/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
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

import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.commons.api.exception.InvalidFileSanitizeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test Class for SafeFileChecker
 */

public class SafeFileCheckerTest {

    private final String VALID_ROOT_PATH = "mydirectory";
    private final String INVALID_ROOT_PATH = "my|filena?me?query=<code>danger</code>.json";
    private final String INVALID_PATH = "file#..$$/text";

    private static final List<String> validFilenames = new ArrayList<>();

    @BeforeAll
    public static void setUpBeforeClass() {
        validFilenames.add("my|filena?me<.json");
        validFilenames.add(".file");
    }

    @Test
    void checkValidPath() {
        assertThatCode(() -> SafeFileChecker.checkSafeFilePath(VALID_ROOT_PATH)).doesNotThrowAnyException();
    }

    @Test
    void checkInvalidPath() {
        assertThatCode(() -> SafeFileChecker.checkSafeFilePath(INVALID_PATH)).hasMessage(
            "Security check error : Invalid name (" + INVALID_PATH + ")"
        );
    }

    @Test
    void checkInValidPath() {
        assertThatThrownBy(() -> SafeFileChecker.checkSafeFilePath(INVALID_ROOT_PATH)).hasMessage(
            "Security check error : Invalid name (" + INVALID_ROOT_PATH + ")"
        );
    }

    @Test
    void checkAnotherInValidPath() {
        assertThatThrownBy(() -> SafeFileChecker.checkSafeFilePath("../../etc/password")).hasMessageContaining(
            "Security check error : Invalid name (../../etc/password)"
        );
    }

    @Test
    void checkNotPlainFileNames() {
        for (String path : List.of(".", "..", "dir/file.json", "dir\\file.json", "/file.json", "file\0.json")) {
            assertThatThrownBy(() -> SafeFileChecker.checkSafeFilePath(path)).isInstanceOf(
                InvalidFileSanitizeException.class
            );
        }
    }

    @Test
    void checkValidSubNamePaths() {
        for (String subPath : validFilenames) {
            assertThatCode(() -> SafeFileChecker.checkSafeFilePath(subPath)).doesNotThrowAnyException();
        }
    }
}
