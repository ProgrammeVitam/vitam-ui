/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */

package fr.gouv.vitamui.pastis.common.dto.profiles;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileStatus;
import fr.gouv.vitam.common.model.administration.ProfileFormat;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notice extends IdDto {

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private ArchiveUnitProfileStatus status;

    @JsonProperty("sedaVersion")
    private ProfileVersion sedaVersion;

    @JsonProperty("creationDate")
    private String creationDate;

    @JsonProperty("lastUpdate")
    private String lastUpdate;

    @JsonProperty("activationDate")
    private String activationDate;

    @JsonProperty("deactivationDate")
    private String deactivationDate;

    @JsonProperty("controlSchema")
    private String controlSchema;

    @JsonProperty("tenant")
    private Integer tenant;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("fields")
    private List<String> fields;

    @JsonProperty("path")
    private String path;

    @JsonProperty("format")
    private ProfileFormat format;

    public Notice(final Resource resource) throws IOException {
        if (resource == null) {
            return;
        }

        final String filename = resource.getFilename();

        if (filename == null) {
            return;
        }

        final long updateDate = resource.lastModified();
        final long idExample = new SecureRandom().nextLong() / 1000;
        final String id = String.valueOf(Math.abs(idExample));
        final String fileBaseName = getFileBaseName(filename);

        if (fileBaseName != null) {
            this.identifier = fileBaseName;
        }
        this.setId(id);
        this.status = ArchiveUnitProfileStatus.ACTIVE;
        this.sedaVersion = ProfileVersion.VERSION_2_1;
        this.lastUpdate = new Timestamp(updateDate).toString();
        this.deactivationDate = new Timestamp(updateDate).toString();
        this.activationDate = new Timestamp(updateDate).toString();
        this.creationDate = new Timestamp(updateDate).toString();
        this.tenant = 1;
        this.version = 1;
        this.name = fileBaseName;

        if (getFileType(filename) == ProfileType.PUA) {
            final Notice notice = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(resource.getInputStream(), Notice.class);

            this.controlSchema = notice.controlSchema;
            this.fields = notice.fields;
            this.description = notice.description;
        } else {
            this.path = filename;
            this.format = ProfileFormat.RNG;
        }
    }

    @CheckForNull
    private String getFileBaseName(String fileName) {
        String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
        return tokens[0];
    }

    public ProfileType getFileType(String fileName) {
        String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
        return tokens[1].equals("rng") ? ProfileType.PA : ProfileType.PUA;
    }

    public String serialiseString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new AfterburnerModule());
        return mapper.writeValueAsString(this);
    }
}
