package org.apereo.cas.support.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apereo.cas.configuration.model.support.sms.SmsModeProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * This is {@link SmsModeSmsSender}.
 * To be removed when original SmsMode Service is fixed (accessToken is not added to Sms API call)
 *
 * @author Jérôme Rautureau
 * @since 6.5.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class SmsModeSmsSender implements SmsSender {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final SmsModeProperties properties;

    @Override
    public boolean send(final String from, final String to, final String message) {
        HttpResponse response = null;
        try {
            val data = new HashMap<String, Object>();
            val recipient = new HashMap<String, Object>();
            recipient.put("to", to);
            data.put("recipient", recipient);
            val body = new HashMap<String, Object>();
            body.put("text", message);
            data.put("body", body);
            data.put("from", from);

            val headers = CollectionUtils.<String, String>wrap(
                "Content-Type", MediaType.APPLICATION_JSON_VALUE,
                "Accept", MediaType.APPLICATION_JSON_VALUE,
                "X-Api-Key", properties.getAccessToken());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(properties.getUrl())
                .proxyUrl(properties.getProxyUrl())
                .headers(headers)
                .entity(MAPPER.writeValueAsString(data))
                .build();
            response = HttpUtils.execute(exec);
            val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
            val entity = response.getEntity();
            val charset = entity.getContentEncoding() != null
                ? Charset.forName(entity.getContentEncoding().getValue())
                : StandardCharsets.ISO_8859_1;
            val resp = IOUtils.toString(entity.getContent(), charset);
            LOGGER.debug("Response from SmsMode: [{}]", resp);
            return status.is2xxSuccessful();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

}
