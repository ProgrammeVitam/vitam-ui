/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.iam.server.idp.domain;

import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;
import org.springframework.data.mapping.PersistentProperty;

/**
 * Applied per-field via {@link org.springframework.data.convert.ValueConverter @ValueConverter}
 * on the sensitive members of {@link IdentityProvider}. Delegates to {@link EncryptedSecretCipher}
 * — see there for the wire format and the backwards-compat behaviour.
 */
public class EncryptedSecretConverter
    implements PropertyValueConverter<String, String, ValueConversionContext<? extends PersistentProperty<?>>> {

    private final EncryptedSecretCipher cipher;

    public EncryptedSecretConverter(EncryptedSecretCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public String read(String value, ValueConversionContext<? extends PersistentProperty<?>> context) {
        return cipher.decrypt(value);
    }

    @Override
    public String write(String value, ValueConversionContext<? extends PersistentProperty<?>> context) {
        return cipher.encrypt(value);
    }
}
