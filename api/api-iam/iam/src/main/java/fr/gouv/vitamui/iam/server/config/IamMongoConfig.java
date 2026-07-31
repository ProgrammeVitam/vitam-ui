/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.iam.server.config;

import fr.gouv.vitamui.commons.api.converter.OffsetDateTimeToStringConverter;
import fr.gouv.vitamui.commons.api.converter.StringToOffsetDateTimeConverter;
import fr.gouv.vitamui.iam.server.idp.domain.EncryptedSecretCipher;
import fr.gouv.vitamui.iam.server.idp.domain.EncryptedSecretConverter;
import fr.gouv.vitamui.iam.server.idp.domain.IdentityProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

/**
 * IAM-specific Mongo customisations that overlay the ones from {@code commons-mongo}. Uses
 * {@code @Primary} so the shared bean is superseded without needing a second scan configuration.
 *
 * <p>The four sensitive fields of {@link IdentityProvider} are wrapped in an
 * {@link EncryptedSecretConverter} so they roundtrip as {@code {enc:v1}...} at rest while staying
 * plain {@code String} in the DTO layer — CAS on {@code develop} and SAS both consume plain values
 * transparently.
 */
@Configuration
public class IamMongoConfig {

    @Bean
    @Primary
    public MongoCustomConversions iamMongoCustomConversions(EncryptedSecretCipher cipher) {
        return MongoCustomConversions.create(config -> {
            // Keep the shared converters that used to live in commons-mongo — anything already relying
            // on OffsetDateTime <-> String in IAM keeps working.
            config.registerConverter(new OffsetDateTimeToStringConverter());
            config.registerConverter(new StringToOffsetDateTimeConverter());

            EncryptedSecretConverter secretConverter = new EncryptedSecretConverter(cipher);
            config.configurePropertyConversions(reg -> {
                reg.registerConverter(IdentityProvider.class, "clientSecret", secretConverter);
                reg.registerConverter(IdentityProvider.class, "keystoreBase64", secretConverter);
                reg.registerConverter(IdentityProvider.class, "keystorePassword", secretConverter);
                reg.registerConverter(IdentityProvider.class, "privateKeyPassword", secretConverter);
            });
        });
    }
}
