/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
package fr.gouv.vitamui.pastis.standalone.config;

import fr.gouv.vitamui.pastis.common.service.JsonFromPUA;
import fr.gouv.vitamui.pastis.common.service.PuaFromJSON;
import fr.gouv.vitamui.pastis.common.service.PuaPastisValidator;
import fr.gouv.vitamui.pastis.server.service.PastisService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Configuration
public class PastisConfiguration {

    private ResourceLoader resourceLoader;

    @Value("${cors.allowed-origins}")
    private String origins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins(origins.split(",")).allowCredentials(true);
            }
        };
    }

    @Bean
    public ErrorViewResolver customErrorViewResolver() {
        final ModelAndView redirectToIndexHtml = new ModelAndView("forward:/index.html", emptyMap(), OK);
        return (request, status, model) -> status == NOT_FOUND ? redirectToIndexHtml : null;
    }

    @Bean
    public JsonFromPUA jsonFromPUA() {
        return new JsonFromPUA();
    }

    @Bean
    public PuaFromJSON puaFromJSON() {
        return new PuaFromJSON(puaPastisValidator());
    }

    @Bean
    public PastisService pastisService() {
        return new PastisService(this.resourceLoader, puaPastisValidator(), jsonFromPUA(), puaFromJSON());
    }

    @Bean
    public PuaPastisValidator puaPastisValidator() {
        return new PuaPastisValidator();
    }
}
