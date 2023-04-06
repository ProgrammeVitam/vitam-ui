package fr.gouv.vitamui.iam.security.filter;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface TokenExtractor {

    static TokenExtractor headerExtractor(String tokenHeaderName) {
        return request -> request.getHeader(tokenHeaderName);
    }

    static TokenExtractor bearerExtractor() {
        return request -> {
            String headerAuth = request.getHeader("Authorization");
            if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
                return headerAuth.substring(7);
            }
            return null;
        };
    }

    String extract(HttpServletRequest request);
}
