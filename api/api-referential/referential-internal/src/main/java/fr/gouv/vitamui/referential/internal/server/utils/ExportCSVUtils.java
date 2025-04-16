package fr.gouv.vitamui.referential.internal.server.utils;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ExportCSVUtils {

    public static final byte[] BOM = new byte[] { (byte) 239, (byte) 187, (byte) 191 };

    private ExportCSVUtils() {}

    public static Resource generateCSVFile(List<String[]> lines, char separator) {
        try {
            // create a write
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8.name());

            outputStream.write(BOM);

            // create a csv writer
            ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(separator)
                .withQuoteChar(ICSVWriter.DEFAULT_QUOTE_CHARACTER)
                .withEscapeChar(ICSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .withLineEnd(ICSVWriter.DEFAULT_LINE_END)
                .build();

            lines.forEach(line -> csvWriter.writeNext(escapeCSVFields(line)));

            // close writers
            csvWriter.close();
            writer.close();

            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException e) {
            throw new BadRequestException("Unable to generate csv file ", e);
        }
    }

    private static final Pattern CSV_FIELD_ESCAPE_REGEX = Pattern.compile("^[-+=@\t\r].*");

    static String[] escapeCSVFields(String[] fields) {
        return Arrays.stream(fields)
            .map(field -> field != null && CSV_FIELD_ESCAPE_REGEX.matcher(field).matches() ? "\t" + field : field)
            .toArray(String[]::new);
    }
}
