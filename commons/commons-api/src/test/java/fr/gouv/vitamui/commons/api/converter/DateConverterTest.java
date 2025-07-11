package fr.gouv.vitamui.commons.api.converter;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateConverterTest {

    private final StringToOffsetDateTimeConverter converter = new StringToOffsetDateTimeConverter();

    private final OffsetDateTimeToStringConverter toStringconverter = new OffsetDateTimeToStringConverter();

    @Test
    public void testFromStringTOOffsetDateTime() {
        final OffsetDateTime date = converter.convert("2020-01-01T00:00:00.000+01:00");
        assertNotNull(date, "Date shouldn't be null");
        assertEquals(2020, date.getYear(), "Year is incorrect");
        assertEquals(1, date.getMonthValue(), "Month is incorrect");
        assertEquals(1, date.getDayOfMonth(), "Day is incorrect");
        assertEquals(0, date.getHour(), "Hour is incorrect");
        assertEquals(0, date.getMinute(), "Minute is incorrect");
        assertEquals(0, date.getSecond(), "Second is incorrect");
    }

    @Test
    public void testToString() {
        final OffsetDateTime date = converter.convert("2020-01-01T00:00:00.000+01:00");
        assertNotNull(toStringconverter, "Converter shouldn't be null");
        assertEquals("2020-01-01T00:00+01:00", toStringconverter.convert(date), "String Date is incorrect");
    }
}
