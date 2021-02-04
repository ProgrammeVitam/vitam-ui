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
import fr.gouv.vitam.common.StringUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.exception.InvalidSanitizeCriteriaException;
import fr.gouv.vitamui.commons.api.exception.InvalidSanitizeParameterException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class SanityCheckerTest {

    private final String TEST_BAD_JSON = "bad_json";
    private final String TEST_GOOD_JSON = "good_json_sanity";
    private final String TEST_GOOD_JSON_CRITERIA = "good_criteria.json";
    private final String TEST_BAD_JSON_CRITERIA = "bad_criteria.json";

    @Before
    public void setUp() {
    }


    @Test(expected = InvalidParseOperationException.class)
    public void givenJsonWhenValueIsTooBigORContainXMLTag()
        throws InvalidParseOperationException, IOException {
        final File file = PropertiesUtils.findFile(TEST_BAD_JSON);
        final JsonNode json = JsonHandler.getFromFile(file);
        assertNotNull(json);
        SanityChecker.checkJsonSanity(json);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void givenJsonWhenValueIsTooBigORContainXMLTagUsingAll()
        throws InvalidParseOperationException, IOException {
        final File file = PropertiesUtils.findFile(TEST_BAD_JSON);
        final JsonNode json = JsonHandler.getFromFile(file);
        assertNotNull(json);
        SanityChecker.checkJsonAll(json);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void givenJsonStringWhenValueIsTooBigORContainXMLTagUsingAll()
        throws InvalidParseOperationException, IOException {
        final File file = PropertiesUtils.findFile(TEST_BAD_JSON);
        final JsonNode json = JsonHandler.getFromFile(file);
        assertNotNull(json);
        SanityChecker.checkJsonAll(json.toString());
    }

    @Test
    public void givenJsonWhenGoodSanityThenReturnTrue()
        throws FileNotFoundException, InvalidParseOperationException {
        final long limit = SanityChecker.getLimitJsonSize();
        try {
            SanityChecker.setLimitJsonSize(100);
            final File file = PropertiesUtils.findFile(TEST_GOOD_JSON);
            final JsonNode json = JsonHandler.getFromFile(file);
            try {
                SanityChecker.checkJsonAll(json);
                fail("Should failed with an exception");
            } catch (final InvalidParseOperationException e) {}
            SanityChecker.setLimitJsonSize(10000);
            SanityChecker.checkJsonAll(json);
            SanityChecker.checkJsonAll(json.toString());
        } finally {
            SanityChecker.setLimitJsonSize(limit);
        }
    }

    @Test
    public void givenStringGoodSanity() throws InvalidParseOperationException {
        final String good = "abcdef";
        SanityChecker.checkParameter(good);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void givenStringBadSize() throws InvalidParseOperationException {
        final int limit = SanityChecker.getLimitParamSize();
        try {
            final String bad = new String(StringUtils.getRandom(40));
            SanityChecker.setLimitParamSize(bad.length() - 5);
            SanityChecker.checkParameter(bad);
        } finally {
            SanityChecker.setLimitParamSize(limit);
        }
    }

    @Test(expected = InvalidParseOperationException.class)
    public void givenStringScript() throws InvalidParseOperationException {
        final String bad = "aa<script>bb";
        SanityChecker.checkParameter(bad);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void givenStringCdata() throws InvalidParseOperationException {
        final String bad = "aa<![CDATA[bb";
        SanityChecker.checkParameter(bad);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void givenStringEntity() throws InvalidParseOperationException {
        final String bad = "aa<!ENTITYbb";
        SanityChecker.checkParameter(bad);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void givenStringXml() throws InvalidParseOperationException {
        final String bad = "aa<strong>bb</strong>bb";
        SanityChecker.checkParameter(bad);
    }

    @Test(expected = InvalidParseOperationException.class)
    public void givenStringNotPrintable() throws InvalidParseOperationException {
        final String bad = "aa\u0003bb";
        SanityChecker.checkParameter(bad);
    }

    @Test(expected = InvalidSanitizeParameterException.class)
    public void givenStringNotValidParam() {
        final String bad = "aa\u0003bb";
        SanityChecker.check(bad);
    }

    @Test
    public void givenCriteriaWhenGoodSanityThenReturnTrue()
        throws FileNotFoundException, InvalidParseOperationException {
            final File file = PropertiesUtils.findFile(TEST_GOOD_JSON_CRITERIA);
            final JsonNode json = JsonHandler.getFromFile(file);
            SanityChecker.sanitizeCriteria(Optional.of(json.toString()));
    }

    @Test(expected = InvalidSanitizeCriteriaException.class)
    public void givenCriteriaWhenBadSanityThenReturnException()
        throws FileNotFoundException, InvalidParseOperationException {
        final File file = PropertiesUtils.findFile(TEST_BAD_JSON_CRITERIA);
        final JsonNode json = JsonHandler.getFromFile(file);
        SanityChecker.sanitizeCriteria(Optional.of(json.toString()));
    }

}
