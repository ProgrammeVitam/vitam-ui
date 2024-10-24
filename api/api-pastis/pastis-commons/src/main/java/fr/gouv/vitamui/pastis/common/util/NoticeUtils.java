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

package fr.gouv.vitamui.pastis.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.pastis.common.dto.profiles.Notice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class NoticeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeUtils.class);

    private NoticeUtils() {}

    public static Notice getNoticeFromPUA(JSONObject jsonPUA) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(jsonPUA.toString(), Notice.class);
    }

    public static ProfileResponse convertToProfileResponse(Notice notice) {
        ProfileResponse profileResponse = new ProfileResponse();
        try {
            profileResponse.setId(notice.getId());
            profileResponse.setType(getFileType(notice));
            profileResponse.setSedaVersion(notice.getSedaVersion());
            profileResponse.setName(notice.getIdentifier());
            profileResponse.setNotice(getNoticeFromPUA(new JSONObject(notice.serialiseString())));
        } catch (IOException e) {
            LOGGER.debug("Error while convert notice to profileResponse {}", notice);
        }

        return profileResponse;
    }

    public static List<String> convert(JSONArray jsonArray) throws JsonProcessingException {
        List<String> list;
        ObjectMapper objectMapper = new ObjectMapper();
        list = objectMapper.readValue(jsonArray.toString(), new TypeReference<>() {});
        return list;
    }

    public static ProfileType getFileType(Notice notice) {
        return notice.getPath() != null && notice.getControlSchema() == null ? ProfileType.PA : ProfileType.PUA;
    }
}
