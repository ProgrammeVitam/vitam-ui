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
import fr.gouv.vitamui.commons.api.exception.InvalidOperationException;
import fr.gouv.vitamui.commons.api.exception.InvalidSanitizeCriteriaException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SanityCheckerTest {

    private final String TEST_BAD_JSON = "bad_json.json";

    @Before
    public void setUp() {
    }

    @Test
    public void givenJsonWhenValueIsTooBigORContainXMLTag()
        throws InvalidParseOperationException, PreconditionFailedException, IOException {
        final File file = PropertiesUtils.findFile(TEST_BAD_JSON);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.checkJsonSanity(json)).
            isInstanceOf(PreconditionFailedException.class);
    }

    @Test
    public void givenJsonWhenValueIsTooBigORContainXMLTagUsingAll()
        throws InvalidParseOperationException, IOException {
        final File file = PropertiesUtils.findFile(TEST_BAD_JSON);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.checkJsonAll(json)).
            isInstanceOf(InvalidParseOperationException.class);
    }

    @Test
    public void givenJsonStringWhenValueIsTooBigORContainXMLTagUsingAll()
        throws InvalidParseOperationException, IOException {
        final File file = PropertiesUtils.findFile(TEST_BAD_JSON);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.checkJsonAll(json.toString())).
            isInstanceOf(InvalidParseOperationException.class);
    }

    @Test
    public void sanitizeJson_should_not_fail_with_keys_$fields_$and_$roots()
        throws FileNotFoundException, InvalidParseOperationException {
        final String jsonWithProjectionRootsFieldsKeys = "probative_action_json_with_fields_projection_roots_keys.json";
        final File file = PropertiesUtils.findFile(jsonWithProjectionRootsFieldsKeys);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeJson(json)).
            doesNotThrowAnyException();
    }

    @Test
    public void givenJsonWhenGoodSanityThenReturnTrue()
        throws FileNotFoundException, InvalidParseOperationException {
        final long limit = SanityChecker.getLimitJsonSize();
        final String TEST_GOOD_JSON = "good_json_sanity.json";
        try {
            SanityChecker.setLimitJsonSize(100);
            final File file = PropertiesUtils.findFile(TEST_GOOD_JSON);
            final JsonNode json = JsonHandler.getFromFile(file);
            try {
                SanityChecker.checkJsonAll(json);
            } catch (final InvalidParseOperationException ignored) {
            }
            SanityChecker.setLimitJsonSize(10000);
            SanityChecker.checkJsonAll(json);
            SanityChecker.checkJsonAll(json.toString());
        } finally {
            SanityChecker.setLimitJsonSize(limit);
        }
    }

    @Test(expected = PreconditionFailedException.class)
    public void givenStringNotValidParam() throws InvalidParseOperationException, PreconditionFailedException {
        final String bad = "aa\u0003bb";
        SanityChecker.checkSecureParameter(bad);
    }

    @Test
    public void givenCriteriaWhenGoodSanityThenReturnTrue()
        throws FileNotFoundException, InvalidParseOperationException, PreconditionFailedException {
        final String TEST_GOOD_JSON_CRITERIA = "good_criteria.json";
        final File file = PropertiesUtils.findFile(TEST_GOOD_JSON_CRITERIA);
        final JsonNode json = JsonHandler.getFromFile(file);
        assertThatCode(() ->
            SanityChecker.sanitizeCriteria(Optional.of(json.toString()))).
            doesNotThrowAnyException();

    }

    @Test(expected = InvalidSanitizeCriteriaException.class)
    public void givenCriteriaWhenBadSanityThenReturnException()
        throws FileNotFoundException, InvalidParseOperationException {
        final String TEST_BAD_JSON_CRITERIA = "bad_criteria.json";
        final File file = PropertiesUtils.findFile(TEST_BAD_JSON_CRITERIA);
        final JsonNode json = JsonHandler.getFromFile(file);
        SanityChecker.sanitizeCriteria(Optional.of(json.toString()));
    }

    @Test(expected = PreconditionFailedException.class)
    public void testCheckSecureParameterWithBadString()
        throws PreconditionFailedException, InvalidParseOperationException {
        final String bad = "a$/§§*";
        SanityChecker.checkSecureParameter(bad);
    }

    @Test(expected = PreconditionFailedException.class)
    public void testCheckSecureParameterWithXmlString()
        throws PreconditionFailedException, InvalidParseOperationException {
        final String badText = "text<strong>text</strong>bb";
        SanityChecker.checkSecureParameter(badText);
    }

    @Test
    public void testCheckSecureParameterWithBadStringAndThrowException() {
        assertThatCode(() -> SanityChecker.checkSecureParameter("§§§§§***ù^65")).
            hasMessage("the parameter §§§§§***ù^65 is not valid");
    }

    @Test(expected = PreconditionFailedException.class)
    public void testCheckSecureParameterWithGivenStringScript()
        throws PreconditionFailedException, InvalidParseOperationException {
        final String badStringScript = "aa<script>bb";
        final String badStringCdata = "aa<![CDATA[bb";
        final String badStringEntity = "aa<!ENTITYbb";
        SanityChecker.checkSecureParameter(badStringScript);
        SanityChecker.checkSecureParameter(badStringCdata);
        SanityChecker.checkSecureParameter(badStringEntity);
    }

    @Test
    public void testCheckSecureParameterGivenStringGoodSanity()
        throws PreconditionFailedException, InvalidParseOperationException {
        final String goodText = "abcdef";
        SanityChecker.checkSecureParameter(goodText);
    }

    @Test(expected = PreconditionFailedException.class)
    public void testCheckSecureParameterGivenStringBadSize()
        throws PreconditionFailedException, InvalidParseOperationException {
        final int limit = SanityChecker.getLimitParamSize();
        try {
            final String bad = new String(StringUtils.getRandom(40));
            SanityChecker.setLimitParamSize(bad.length() - 5);
            SanityChecker.checkSecureParameter(bad);
        } finally {
            SanityChecker.setLimitParamSize(limit);
        }
    }

    @Test
    public void testIsValidFileNameWithBadName() {
        final String badString = "aa<script>bb";
        assertThatCode(() -> SanityChecker.isValidFileName(badString)).
            hasMessage("The fileName is not valid");
    }

    @Test
    public void testIsValidFileNameWithGoodName() {
        final String goodFileName = "fileName";
        assertThatCode(() -> SanityChecker.isValidFileName(goodFileName))
            .doesNotThrowAnyException();
    }

    @Test
    public void testIsValidFileNameWithXmlString() {
        final String badString = "text<strong>text</strong>bb";
        assertThatCode(() -> SanityChecker.isValidFileName(badString)).
            hasMessage("The fileName is not valid");
    }

    @Test
    public void testIsValidFileNameWhenGivenStringIsBad() {
        final String badText = "aa<![CDATA[bb";
        assertThatCode(() -> SanityChecker.isValidFileName(badText)).
            hasMessage("The fileName is not valid");
    }

    @Test
    public void sanitizeJson_should_not_fail_with_keys_$action_$add_$pull()
        throws FileNotFoundException, InvalidParseOperationException {
        final String jsonWithActionPullAddKeys = "reclassification_action_json_with_action_add_pull_keys.json";
        final File file = PropertiesUtils.findFile(jsonWithActionPullAddKeys);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeJson(json)).
            doesNotThrowAnyException();
    }

    @Test
    public void testCheckSecureParameterWithIdAsParameter() {
        assertThatCode(() -> SanityChecker.checkSecureParameter("#id")).
            doesNotThrowAnyException();
    }

    @Test
    public void sanitizeJson_should_not_fail_with_keys_$query_$in_$or()
        throws FileNotFoundException, InvalidParseOperationException {
        final String jsonWithInOrQueryKeys = "audit_action_json_with_in_or_query_keys.json";
        final File file = PropertiesUtils.findFile(jsonWithInOrQueryKeys);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeJson(json)).
            doesNotThrowAnyException();
    }

    @Test
    public void sanitizeJson_should_not_fail_with_keys_$exists()
        throws FileNotFoundException, InvalidParseOperationException {
        final String jsonWithExistsKey = "audit_action_json_with_exists_key.json";
        final File file = PropertiesUtils.findFile(jsonWithExistsKey);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeJson(json)).
            doesNotThrowAnyException();
    }

    @Test
    public void isValidParameterName_tests() {
        assertTrue(SanityChecker.isValidParameter("vitamui-primary"));
    }

    @Test
    public void givenJsonWithLongKeyThenReturnTrue()
        throws FileNotFoundException, InvalidParseOperationException, PreconditionFailedException {
        final String JSON_WITH_LONG_KEY = "json_with_long_key.json";
        final File file = PropertiesUtils.findFile(JSON_WITH_LONG_KEY);
        final JsonNode json = JsonHandler.getFromFile(file);
        assertThatCode(() ->
            SanityChecker.sanitizeCriteria(Optional.of(json.toString()))).
            doesNotThrowAnyException();

    }
    @Test
    public void sanitizeJson_should_not_fail_with_keys_$eq_$offset()
        throws FileNotFoundException, InvalidParseOperationException {
        final String jsonWithOffsetEqKeys = "logbook_operations_with_equal_offset_keys.json";
        final File file = PropertiesUtils.findFile(jsonWithOffsetEqKeys);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeJson(json)).
            doesNotThrowAnyException();
    }

    @Test
    public void sanitizeJson_should_not_fail_with_keys_$limit_$orderBy()
        throws FileNotFoundException, InvalidParseOperationException {
        final String jsonWithLimitOrderByKeys = "logbook_operations_with_limit_orderby_keys.json";
        final File file = PropertiesUtils.findFile(jsonWithLimitOrderByKeys);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeJson(json)).
            doesNotThrowAnyException();
    }

    @Test
    public void sanitizeJson_should_not_fail_with_ingest_search_keys()
        throws FileNotFoundException, InvalidParseOperationException {
        final String jsonWithIngestComplexe_Keys = "ingest_search_complexe_key.json";
        final File file = PropertiesUtils.findFile(jsonWithIngestComplexe_Keys);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeJson(json)).
            doesNotThrowAnyException();
    }

    @Test
    public void test_isValidParameterName_with_html_code() {
        assertFalse(SanityChecker.isValidParameter("<vitamui>aa<primary>"));
    }

    @Test
    public void test_sanitizeCriteria_with_object_contains_xml_tag()
        throws InvalidParseOperationException, PreconditionFailedException, IOException {
        final String OBJECT_XML_TAG = "object_with_xml_tag.json";
        final File file = PropertiesUtils.findFile(OBJECT_XML_TAG);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeCriteria(json)).
            isInstanceOf(PreconditionFailedException.class);
    }

    @Test
    public void test_checkHtmlPattern_with_html_text() {
        assertThatCode(() -> SanityChecker.checkHtmlPattern("<div class=\"col-6 form-control\"> <p class=\"title-text\">bla bla bla</p></div>"))
            .isInstanceOf(PreconditionFailedException.class)
            .hasMessageContaining("the parameter :");
    }

    @Test
    public void test_sanitizeCriteria_with_object_contains_html_tag()
        throws InvalidParseOperationException, PreconditionFailedException, IOException {
        final String OBJECT_HTML_TAG = "object_with_html_tag.json";
        final File file = PropertiesUtils.findFile(OBJECT_HTML_TAG);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeCriteria(json)).
            isInstanceOf(PreconditionFailedException.class);
    }

    @Test
    public void test_checkHtmlPattern_with_correct_text() {
        assertThatCode(() -> SanityChecker.checkHtmlPattern("test test good values"))
            .doesNotThrowAnyException();
    }

    @Test
    public void test_sanitizeCriteria_with_object_contains_correct_values()
        throws InvalidParseOperationException, PreconditionFailedException, IOException {
        final String OBJECT_WITH_CORRECT_VALUES = "object_with_correct_values.json";
        final File file = PropertiesUtils.findFile(OBJECT_WITH_CORRECT_VALUES);
        final JsonNode json = JsonHandler.getFromFile(file);
        Assertions.assertThat(json).isNotNull();
        assertThatCode(() -> SanityChecker.sanitizeCriteria(json)).
            doesNotThrowAnyException();
    }

    @Test
    public void test_checkJsonAll_with_null_object() {
        assertThatCode(() -> SanityChecker.checkJsonAll((JsonNode) null)).
            isInstanceOf(InvalidParseOperationException.class)
            .hasMessage("Json is not valid from Sanitize check");
    }

}
