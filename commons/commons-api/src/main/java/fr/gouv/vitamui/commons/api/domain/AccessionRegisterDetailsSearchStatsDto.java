package fr.gouv.vitamui.commons.api.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AccessionRegisterDetailsSearchStatsDto {
    private String searchText;
    private Map<String, List<String>> statusFilter;
    private EndDateInterval dateInterval;
    private AdvancedSearchData advancedSearch;

    @Data
    public static class EndDateInterval {
        private String endDateMin;
        private String endDateMax;
    }

    @Data
    public static class AdvancedSearchData {
        private List<String> originatingAgencies;
        private List<String> archivalAgreements;
        private List<String> archivalProfiles;
        private List<String> acquisitionInformations;
        private String elimination;
        private String transfer;
        private String preservation;
    }
}

