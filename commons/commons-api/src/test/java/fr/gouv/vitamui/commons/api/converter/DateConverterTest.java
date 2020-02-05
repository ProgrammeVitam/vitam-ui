package fr.gouv.vitamui.commons.api.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.OffsetDateTime;

import org.junit.Test;

public class DateConverterTest {

    private final StringToOffsetDateTimeConverter converter = new StringToOffsetDateTimeConverter();

    private final OffsetDateTimeToStringConverter toStringconverter = new OffsetDateTimeToStringConverter();

    @Test
    public void testFromStringTOOffsetDateTime() {
        final OffsetDateTime date = converter.convert("2020-01-01T00:00:00.000+01:00");
        assertNotNull("Date shouldn't be null", date);
        assertEquals("Year is incorrect", 2020, date.getYear());
        assertEquals("Month is incorrect", 1, date.getMonthValue());
        assertEquals("Day is incorrect", 1, date.getDayOfMonth());
        assertEquals("Hour is incorrect", 0, date.getHour());
        assertEquals("Minute is incorrect", 0, date.getMinute());
        assertEquals("Second is incorrect", 0, date.getSecond());
    }

    @Test
    public void testToString() {
        final OffsetDateTime date = converter.convert("2020-01-01T00:00:00.000+01:00");
        assertNotNull("Converter shouldn't be null", toStringconverter);
        assertEquals("String Date is incorrect", "2020-01-01T00:00+01:00", toStringconverter.convert(date));
    }

}
