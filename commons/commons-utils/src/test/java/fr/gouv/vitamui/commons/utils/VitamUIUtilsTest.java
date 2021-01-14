package fr.gouv.vitamui.commons.utils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class VitamUIUtilsTest {

    @Test
    public void testEmailValidator() {
        final String badFormat = "te.com";
        Assert.assertFalse("Bad emails format", VitamUIUtils.isValidEmail(badFormat));
        final String goodFormat = "test@test.com";
        Assert.assertTrue("True emails format", VitamUIUtils.isValidEmail(goodFormat));
    }

    @Test
    public void testRandomByte() throws IOException {
        final byte[] data = VitamUIUtils.getRandom(2);

        Assert.assertNotNull("Byte[] data is null.", data);
        Assert.assertEquals("Byte size invalid.", 2, data.length);
    }

    @Test
    public void testRandomEmptyByte() throws IOException {
        final byte[] data = VitamUIUtils.getRandom(0);

        Assert.assertNotNull("Byte[] data is null.", data);
        Assert.assertEquals("Byte size invalid.", 0, data.length);
    }

    @Test
    public void testConvertStringToDate() throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
        Date date = VitamUIUtils.convertStringToDate("2020-01-01");

        System.out.println(date);
        Assert.assertNotNull("Date is null.", date);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals("Year is incorrect", 2020, calendar.get(Calendar.YEAR));
        assertEquals("Month is incorrect", 0, calendar.get(Calendar.MONTH));
        assertEquals("Day is incorrect", 1, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("Hour is incorrect", 0, calendar.get(Calendar.HOUR));
        assertEquals("Minute is incorrect", 0, calendar.get(Calendar.MINUTE));
        assertEquals("Second is incorrect", 0, calendar.get(Calendar.SECOND));

        date = VitamUIUtils.convertStringToDate("2020-01-01T00:00:00");
        calendar.setTime(date);
        assertEquals("Year is incorrect", 2020, calendar.get(Calendar.YEAR));
        assertEquals("Month is incorrect", 0, calendar.get(Calendar.MONTH));
        assertEquals("Day is incorrect", 1, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("Hour is incorrect", 1, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals("Minute is incorrect", 0, calendar.get(Calendar.MINUTE));
        assertEquals("Second is incorrect", 0, calendar.get(Calendar.SECOND));

        date = VitamUIUtils.convertStringToDate("2020-01-01T00:00:00.000+01:00");
        calendar.setTime(date);
        assertEquals("Year is incorrect", 2020, calendar.get(Calendar.YEAR));
        assertEquals("Month is incorrect", 0, calendar.get(Calendar.MONTH));
        assertEquals("Day is incorrect", 1, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals("Hour is incorrect", 0, calendar.get(Calendar.HOUR));
        assertEquals("Minute is incorrect", 0, calendar.get(Calendar.MINUTE));
        assertEquals("Second is incorrect", 0, calendar.get(Calendar.SECOND));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void givenUsingTheJdk_whenUnmodifiableListIsCreated_thenNotModifiable() {
        List<String> list = VitamUIUtils.listOf("one", "two", "three");
        list.add("four");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateApplicationId_whenApplicationNameIsNull_thenIllegalArgumentException() {
        VitamUIUtils.generateApplicationId("", null, "userIdentifier", "", "custsomerIdentifier", "x-request-id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateApplicationId_whenApplicationNameIsEmpty_thenIllegalArgumentException() {
        VitamUIUtils.generateApplicationId("", "", "userIdentifier", "", "custsomerIdentifier", "x-request-id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateApplicationId_whenUserIdentifiernNameIsEmpty_thenIllegalArgumentException() {
        VitamUIUtils.generateApplicationId("", "applicationName", "", "", "custsomerIdentifier", "x-request-id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateApplicationId_whenUserIdentifiernNameIsNull_thenIllegalArgumentException() {
        VitamUIUtils.generateApplicationId("", "applicationName", null, "", "custsomerIdentifier", "x-request-id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateApplicationId_whenCustomerIdentifiernIsEmpty_thenIllegalArgumentException() {
        VitamUIUtils.generateApplicationId("", "applicationName", "userIdentifier", "", "", "x-request-id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateApplicationId_whenCustomerIdentifiernIsNull_thenIllegalArgumentException() {
        VitamUIUtils.generateApplicationId("", "applicationName", "userIdentifier", "", null, "x-request-id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateApplicationId_whenRequestIdIsEmpty_thenIllegalArgumentException() {
        VitamUIUtils.generateApplicationId("", "applicationName", "userIdentifier", "", "customerIdentifier", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateApplicationId_whenRequestIdIsNull_thenIllegalArgumentException() {
        VitamUIUtils.generateApplicationId("", "applicationName", "userIdentifier", "", "customerIdentifier", null);
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
        String expected = "[host:\"172.18.102.247:8008\", authorization:\"Basic **********\", proxy-authorization:\"Bearer **********\", proxy-authenticate:\"Digest **********\", x-application-id:\"INGEST_APP\", x-forwarded-server:\"env1.vitamui.fr, env2.vitamui.fr, env3.vitamui.fr\"]";
        assertEquals(expected, result);

    }
}
