package fr.gouv.vitamui.archive.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;


@Getter
@Setter
@ToString
public class LogbookOperationModel extends LogbookEventModel{

    @JsonProperty("events")
    private List<LogbookEventModel> events;

    @JsonProperty("obIdIn")
    private String obIdIn;

}
