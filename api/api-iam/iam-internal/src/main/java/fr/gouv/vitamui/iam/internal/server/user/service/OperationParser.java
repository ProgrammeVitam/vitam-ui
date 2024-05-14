package fr.gouv.vitamui.iam.internal.server.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OperationParser {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OperationParser.class);

    private static final String OLD_VALUE_PREFIX = "-";

    private static final String NEW_VALUE_PREFIX = "+";

    private static final String DIFF_KEY = "diff";

    private final ObjectMapper objectMapper;

    private final TranslateService translateService;

    public String parseOldValues(String json) {
        return parse(json, OLD_VALUE_PREFIX);
    }

    public String parseNewValues(String json) {
        return parse(json, NEW_VALUE_PREFIX);
    }

    public String parseUserId(String evIdAppSession) {
        if (evIdAppSession == null) {
            return null;
        }
        String[] sessionData = evIdAppSession.split(":");
        if (sessionData.length < 4) {
            return null;
        }
        return sessionData[3];
    }

    private String parse(String json, String prefix) {
        try {
            final HashMap<String, HashMap<String, String>> updateOperation = objectMapper.readValue(
                json,
                HashMap.class
            );

            if (updateOperation == null || !updateOperation.containsKey(DIFF_KEY)) {
                return "";
            }

            return updateOperation
                .get(DIFF_KEY)
                .entrySet()
                .stream()
                .filter(s -> s.getKey().startsWith(prefix))
                .map(
                    s ->
                        removePrefix(s.getKey(), prefix) +
                        ":" +
                        translateService.translate(removePrefix(s.getValue(), prefix))
                )
                .collect(Collectors.joining(","));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new InternalServerException(String.format("cannot parse operation %s", json), e);
        }
    }

    private String removePrefix(String value, String prefix) {
        return value.replace(prefix, "");
    }
}
