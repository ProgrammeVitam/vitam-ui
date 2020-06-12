package fr.gouv.vitamui.ingest.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class LogbookEventDto extends IdDto implements Serializable {

    private String idRequest;

    private String parentId;

    private String type;

    private String typeProc;

    private String dateTime;

    private String outcome;

    private String outDetail;

    private String outMessage;

    private String data;

    private String objectId;

    private String collectionName;

    private String idAppSession;

    private String agId;

    private String agIdApp;

    private String agIdExt;

    private String rightsStatementIdentifier;

}
