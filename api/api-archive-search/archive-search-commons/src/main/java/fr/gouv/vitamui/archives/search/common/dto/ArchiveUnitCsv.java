package fr.gouv.vitamui.archives.search.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ArchiveUnitCsv implements Serializable {

    private String id;

    private String originatingAgencyName;

    private String descriptionLevel;

    private String title;

    private String startDate;

    private String endDate;

    private String description;
}
