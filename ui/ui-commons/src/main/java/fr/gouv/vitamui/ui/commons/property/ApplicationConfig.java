package fr.gouv.vitamui.ui.commons.property;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;

@ConfigurationProperties
@Getter
public class ApplicationConfig {

    @Value("${cas.external-url}")
    @NotNull
    private String casExternalUrl;

    @Value("${cas.callback-url}")
    @NotNull
    private String casCallbackUrl;

    @Value("${ui.url}")
    @NotNull
    private String uiUrl;

    @Value("${ui.redirect-url}")
    @NotNull
    private String uiRedirectUrl;

    @Value("${theme}")
    private Map<String, String> themeColors;

}
