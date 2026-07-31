/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.config;

import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Standalone Thymeleaf engines for outgoing emails — one for HTML, one for plain text. Two engines
 * (rather than one with two resolvers) keep the resolution unambiguous when the same base name
 * exists in both formats (which is the common case for a multipart mail).
 *
 * <p>Kept independent from Spring MVC's Thymeleaf autoconfiguration (which stays disabled via
 * {@code spring.thymeleaf.enabled=false}) so the auth-server's vanilla static SPAs aren't
 * inadvertently intercepted by view resolution.
 */
@Configuration
public class MailTemplateEngineConfig {

    public static final String TEMPLATE_PREFIX = "templates/mail/";

    @Bean
    public SpringTemplateEngine mailHtmlEngine() {
        return buildEngine(".html", TemplateMode.HTML);
    }

    @Bean
    public SpringTemplateEngine mailTextEngine() {
        return buildEngine(".txt", TemplateMode.TEXT);
    }

    private static SpringTemplateEngine buildEngine(String suffix, TemplateMode mode) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        ClassLoaderTemplateResolver r = new ClassLoaderTemplateResolver();
        r.setPrefix(TEMPLATE_PREFIX);
        r.setSuffix(suffix);
        r.setTemplateMode(mode);
        r.setCharacterEncoding(StandardCharsets.UTF_8.name());
        r.setCacheable(true);
        engine.addTemplateResolver(r);
        return engine;
    }
}
