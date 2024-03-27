package fr.gouv.vitamui.iam.internal.server.user.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.ResourceBundle;

@Service
public class TranslateService {
    private final ResourceBundle bundleFR = ResourceBundle.getBundle("exported-data", Locale.FRENCH);

    public String translate(boolean value) {
        return translate(Boolean.toString(value));
    }

    public String translate(String key) {
        if (key == null) {
            return null;
        }
        if (!bundleFR.containsKey(key)) {
            return key;
        }
        return bundleFR.getString(key);
    }
}
