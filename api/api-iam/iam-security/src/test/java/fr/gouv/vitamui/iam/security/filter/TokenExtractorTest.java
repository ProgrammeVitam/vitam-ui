package fr.gouv.vitamui.iam.security.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TokenExtractorTest {

    private static final String TOKEN_HEADER_NAME = "tokenHeaderName";

    private static final String BEARER_TOKEN_HEADER_NAME = "Authorization";

    private static final String USER_TOKEN = "USER-TOKEN";

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldExtractTokenFromHeader() {
        given(request.getHeader(TOKEN_HEADER_NAME)).willReturn(USER_TOKEN);

        String actualToken = TokenExtractor.headerExtractor(TOKEN_HEADER_NAME).extract(request);

        assertThat(actualToken).isEqualTo(USER_TOKEN);
    }

    @Test
    void shouldExtractBearerTokenFromHeader() {
        given(request.getHeader(BEARER_TOKEN_HEADER_NAME)).willReturn("Bearer " + USER_TOKEN);

        String actualToken = TokenExtractor.bearerExtractor().extract(request);

        assertThat(actualToken).isEqualTo(USER_TOKEN);
    }

    @Test
    void shouldReturnNullWhenTokenIsEmpty() {
        given(request.getHeader(BEARER_TOKEN_HEADER_NAME)).willReturn(null);

        String actualToken = TokenExtractor.bearerExtractor().extract(request);

        assertThat(actualToken).isNull();
    }
}
