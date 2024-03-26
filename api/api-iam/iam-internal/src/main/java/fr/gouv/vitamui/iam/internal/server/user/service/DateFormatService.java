package fr.gouv.vitamui.iam.internal.server.user.service;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class DateFormatService {
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(DateFormatService.class);

    public String formatDate(String text) {
        return format(text, DAY_FORMATTER);
    }

    public String formatTime(String text) {
        return format(text, TIME_FORMATTER);
    }

    private String format(String text, DateTimeFormatter formatter) {
        if (text == null) {
            return null;
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(text);
            return localDateTime.format(formatter);
        } catch (DateTimeParseException e) {
            LOGGER.error("can not parse date time ", text);
            return null;
        }
    }
}