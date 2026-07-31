/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.config;

import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.context.annotation.Configuration;

/**
 * Persists the HTTP session in the {@code auth-server} Mongo database (collection {@code sessions}
 * by default) via the {@code org.mongodb:mongodb-spring-session} integration. Without this the SAS
 * would reset every user's session at each restart — the {@code OAuth2Authorization} rows survive
 * (chantier #5) but the tab-side SSO cookie would point at nothing.
 *
 * <p>Backing MongoTemplate is the one already configured for the app (via
 * {@code spring-boot-starter-data-mongodb}, URI in application.yml). The session collection carries a
 * TTL index managed by the integration, so idle rows are purged automatically — no cron needed.
 */
@Configuration
@EnableMongoHttpSession
public class SessionConfig {}
