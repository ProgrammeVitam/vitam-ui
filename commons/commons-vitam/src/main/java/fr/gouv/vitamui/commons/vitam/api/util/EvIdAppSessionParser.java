package fr.gouv.vitamui.commons.vitam.api.util;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EvIdAppSessionParser {

    private static final String UNKNOWN_VALUE = "-";
    private static final Integer API_CONTEXT_INDEX = 2;
    private static final Integer USER_INDEX = 3;
    private static final Integer SUBROGATOR_INDEX = 4;

    public static String parseApiContextId(String evIdAppSession) {
        return parseEvIdAppSession(evIdAppSession, API_CONTEXT_INDEX);
    }

    public static String parseUserId(String evIdAppSession) {
        return parseEvIdAppSession(evIdAppSession, USER_INDEX);
    }

    public static String parseSubrogatorId(String evIdAppSession) {
        return parseEvIdAppSession(evIdAppSession, SUBROGATOR_INDEX);
    }

    private static String parseEvIdAppSession(String evIdAppSession, int index) {
        return Optional.ofNullable(evIdAppSession)
            .map(v -> v.split(":"))
            .map(sessionData -> ArrayUtils.get(sessionData, index))
            .filter(v -> !UNKNOWN_VALUE.equals(v))
            .orElse(null);
    }
}
