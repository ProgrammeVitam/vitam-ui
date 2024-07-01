package fr.gouv.vitamui.commons.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessionRegisterSearchDto {

    private String orderBy;
    private DirectionDto direction;

    private String searchText;
    private Map<String, List<String>> filters;
    private EndDateInterval endDateInterval;
    private String opi;
    private String originatingAgency;
    private List<String> originatingAgencies;
    private List<String> archivalAgreements;
    private List<String> archivalProfiles;
    private List<String> acquisitionInformations;

    private String elimination;
    private String transferReply;

    @Data
    public static class EndDateInterval {

        private String endDateMin;
        private String endDateMax;
    }
}
