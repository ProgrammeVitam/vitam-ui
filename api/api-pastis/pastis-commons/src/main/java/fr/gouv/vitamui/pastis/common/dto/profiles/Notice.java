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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileStatus;
import fr.gouv.vitam.common.model.administration.ProfileFormat;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.pastis.common.util.NoticeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.Resource;
import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	public Notice(Resource r) throws IOException {
		if (r != null) {
			String fileName = r.getFilename();
			if(fileName != null ){
				Long updateDate = r.lastModified();
				long idExample = new SecureRandom().nextLong() / 1000;
				this.setId(String.valueOf(Math.abs(idExample)));
				String fileBaseName = getFileBaseName(fileName);
				if(fileBaseName != null){
					this.identifier = fileBaseName;
				}
				this.status = ArchiveUnitProfileStatus.ACTIVE;
				this.lastUpdate = new Timestamp(updateDate).toString();
				this.deactivationDate = new Timestamp(updateDate).toString();
				this.activationDate = new Timestamp(updateDate).toString();
				this.creationDate = new Timestamp(updateDate).toString();
				this.tenant = 1;
				this.version = 1;
				this.name = fileBaseName;
				if (getFileType(fileName).equals(ProfileType.PUA)) {
					InputStream inputStream = getClass().getClassLoader().getResourceAsStream("rng/" +
							fileName);
					if (inputStream != null) {
						JSONTokener tokener = new JSONTokener(new InputStreamReader(inputStream));
						JSONObject profileJson = new JSONObject(tokener);
						this.controlSchema = profileJson.getString("controlSchema");
						this.fields = NoticeUtils.convert((JSONArray) profileJson.get("fields"));
						this.description = profileJson.getString("description");
					}
				} else {
					this.path = fileName;
					this.format = ProfileFormat.RNG;
				}
			}
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
