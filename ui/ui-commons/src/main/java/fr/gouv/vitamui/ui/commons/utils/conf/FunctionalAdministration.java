package fr.gouv.vitamui.ui.commons.utils.conf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FunctionalAdministration {
    private Map<Integer, List<String>> listEnableExternalIdentifiers;

    public Map<Integer, List<String>> getListEnableExternalIdentifiers() {
        return listEnableExternalIdentifiers;
    }

    public void setListEnableExternalIdentifiers(Map<Integer, List<String>> listEnableExternalIdentifiers) {
        this.listEnableExternalIdentifiers = listEnableExternalIdentifiers;
    }
}
