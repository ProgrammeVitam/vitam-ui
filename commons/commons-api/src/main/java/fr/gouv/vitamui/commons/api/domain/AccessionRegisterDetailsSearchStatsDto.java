package fr.gouv.vitamui.commons.api.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AccessionRegisterDetailsSearchStatsDto {
    private String searchText;
    private Map<String, List<String>> statusFilter;
    private DateInterval dateInterval;

    @Data
    public static class DateInterval {
        private String startDateMin;
        private String startDateMax;
    }
}

