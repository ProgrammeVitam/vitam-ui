package fr.gouv.vitamui.iam.internal.server.user.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.gouv.vitamui.commons.mongo.IdDocument;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Document(collection = MongoDbCollections.CONNECTION_HISTORY)
@Getter
@Setter
@Builder
public class ConnectionHistory extends IdDocument {

    private String userId;

    @JsonInclude(Include.NON_NULL)
    private String subrogatedUserId;

    private Date connectionDateTime;
}
