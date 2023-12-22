package fr.gouv.vitamui.iam.common.dto;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ConnectionHistoryDto extends IdDto {

    private String userId;

    private String subrogatedUserId;

    private Date connectionDateTime;
}
