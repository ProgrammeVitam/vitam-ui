package fr.gouv.vitamui.commons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VitamUIUtilsTest {

    @Test
    public void testEmailValidator() {
        final String badFormat = "te.com";
        Assertions.assertFalse(VitamUIUtils.isValidEmail(badFormat), "Bad emails format");
        final String goodFormat = "test@test.com";
        Assertions.assertTrue(VitamUIUtils.isValidEmail(goodFormat), "True emails format");
    }

    @Test
    public void testRandomByte() throws IOException {
        final byte[] data = VitamUIUtils.getRandom(2);

        Assertions.assertNotNull(data, "Byte[] data is null.");
        Assertions.assertEquals(2, data.length, "Byte size invalid.");
    }

    @Test
    public void testRandomEmptyByte() throws IOException {
        final byte[] data = VitamUIUtils.getRandom(0);

        Assertions.assertNotNull(data, "Byte[] data is null.");
        Assertions.assertEquals(0, data.length, "Byte size invalid.");
    }

    @Test
    public void testConvertStringToDate() throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
        Date date = VitamUIUtils.convertStringToDate("2020-01-01");

        System.out.println(date);
        Assertions.assertNotNull(date, "Date is null.");
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(2020, calendar.get(Calendar.YEAR), "Year is incorrect");
        assertEquals(0, calendar.get(Calendar.MONTH), "Month is incorrect");
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH), "Day is incorrect");
        assertEquals(0, calendar.get(Calendar.HOUR), "Hour is incorrect");
        assertEquals(0, calendar.get(Calendar.MINUTE), "Minute is incorrect");
        assertEquals(0, calendar.get(Calendar.SECOND), "Second is incorrect");

        date = VitamUIUtils.convertStringToDate("2020-01-01T00:00:00");
        calendar.setTime(date);
        assertEquals(2020, calendar.get(Calendar.YEAR), "Year is incorrect");
        assertEquals(0, calendar.get(Calendar.MONTH), "Month is incorrect");
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH), "Day is incorrect");
        assertEquals(1, calendar.get(Calendar.HOUR_OF_DAY), "Hour is incorrect");
        assertEquals(0, calendar.get(Calendar.MINUTE), "Minute is incorrect");
        assertEquals(0, calendar.get(Calendar.SECOND), "Second is incorrect");

        date = VitamUIUtils.convertStringToDate("2020-01-01T00:00:00.000+01:00");
        calendar.setTime(date);
        assertEquals(2020, calendar.get(Calendar.YEAR), "Year is incorrect");
        assertEquals(0, calendar.get(Calendar.MONTH), "Month is incorrect");
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH), "Day is incorrect");
        assertEquals(0, calendar.get(Calendar.HOUR), "Hour is incorrect");
        assertEquals(0, calendar.get(Calendar.MINUTE), "Minute is incorrect");
        assertEquals(0, calendar.get(Calendar.SECOND), "Second is incorrect");
    }

    @Test
    public void givenUsingTheJdk_whenUnmodifiableListIsCreated_thenNotModifiable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            List<String> list = VitamUIUtils.listOf("one", "two", "three");
            list.add("four");
        });
    }

    @Test
    public void generateApplicationId_whenApplicationNameIsNull_thenIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                VitamUIUtils.generateApplicationId(
                    "",
                    null,
                    "userIdentifier",
                    "",
                    "custsomerIdentifier",
                    "x-request-id"
                )
        );
    }

    @Test
    public void generateApplicationId_whenApplicationNameIsEmpty_thenIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                VitamUIUtils.generateApplicationId("", "", "userIdentifier", "", "custsomerIdentifier", "x-request-id")
        );
    }

    @Test
    public void generateApplicationId_whenUserIdentifiernNameIsEmpty_thenIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                VitamUIUtils.generateApplicationId("", "applicationName", "", "", "custsomerIdentifier", "x-request-id")
        );
    }

    @Test
    public void generateApplicationId_whenUserIdentifiernNameIsNull_thenIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                VitamUIUtils.generateApplicationId(
                    "",
                    "applicationName",
                    null,
                    "",
                    "custsomerIdentifier",
                    "x-request-id"
                )
        );
    }

    @Test
    public void generateApplicationId_whenCustomerIdentifiernIsEmpty_thenIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> VitamUIUtils.generateApplicationId("", "applicationName", "userIdentifier", "", "", "x-request-id")
        );
    }

    @Test
    public void generateApplicationId_whenCustomerIdentifiernIsNull_thenIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> VitamUIUtils.generateApplicationId("", "applicationName", "userIdentifier", "", null, "x-request-id")
        );
    }

    @Test
    public void generateApplicationId_whenRequestIdIsEmpty_thenIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                VitamUIUtils.generateApplicationId(
                    "",
                    "applicationName",
                    "userIdentifier",
                    "",
                    "customerIdentifier",
                    ""
                )
        );
    }

    @Test
    public void generateApplicationId_whenRequestIdIsNull_thenIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                VitamUIUtils.generateApplicationId(
                    "",
                    "applicationName",
                    "userIdentifier",
                    "",
                    "customerIdentifier",
                    null
                )
        );
    }

    @Test
    public void testSecureHeadersLogging() throws IOException {
        HttpHeaders headers = new HttpHeaders();

        headers.add("host", "172.18.102.247:8008");
        headers.add("authorization", "Basic sqklqsduqjksfknszazdfsdsdsq==");
        headers.add("proxy-authorization", "Bearer sqklqsduqjksfknszazdfsdsdsq==");
        headers.add("proxy-authenticate", "Digest sqklqsduqjksfknszazdfsdsdsq==");
        headers.add("x-application-id", "INGEST_APP");
        headers.add("x-forwarded-server", "env1.vitamui.fr, env2.vitamui.fr, env3.vitamui.fr");
        String result = VitamUIUtils.secureFormatHeadersLogging(headers);
        String expected =
            "[host:\"172.18.102.247:8008\", authorization:\"Basic **********\", proxy-authorization:\"Bearer **********\", proxy-authenticate:\"Digest **********\", x-application-id:\"INGEST_APP\", x-forwarded-server:\"env1.vitamui.fr, env2.vitamui.fr, env3.vitamui.fr\"]";
        assertEquals(expected, result);
    }

    @Test
    public void convertSizeToKiloByteByteCountSI() throws IOException {
        assertEquals("624 go", VitamUIUtils.convertSizeToGigaByte(670_014_898_176L));
        assertEquals("0 go", VitamUIUtils.convertSizeToGigaByte(0L));
        assertEquals("0.000001 go", VitamUIUtils.convertSizeToGigaByte(1_000L));
        assertEquals("0.001118 go", VitamUIUtils.convertSizeToGigaByte(1_200_000L));
        assertEquals("0.073388 go", VitamUIUtils.convertSizeToGigaByte(78_800_000));
        assertEquals("1 go", VitamUIUtils.convertSizeToGigaByte(1_073_741_824L));
        assertEquals("0.000215 go", VitamUIUtils.convertSizeToGigaByte(231_246L));
    }
}
