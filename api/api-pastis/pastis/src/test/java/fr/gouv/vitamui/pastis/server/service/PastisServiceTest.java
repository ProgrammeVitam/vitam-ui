/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.pastis.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.api.exception.InvalidFileSanitizeException;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.profiles.Notice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileType;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileVersion;
import fr.gouv.vitamui.pastis.common.exception.TechnicalException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
class PastisServiceTest {

    @InjectMocks
    PastisService pastisService;

    @Test
    void createProfileByType_with_xxe_should_fail() {
        Resource resource = new ClassPathResource("profiles/test-xxe.xml");
        Assertions.assertThrows(
            TechnicalException.class,
            () -> pastisService.createProfileByType(resource, ProfileType.PA, ProfileVersion.VERSION_2_1)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "../application.yml", "../../META-INF/MANIFEST.MF", "/etc/passwd", "sub/../../file.rng" })
    void loadProfile_with_invalid_path_should_fail(String path) {
        Notice notice = new Notice();
        notice.setPath(path);
        Assertions.assertThrows(InvalidFileSanitizeException.class, () -> pastisService.loadProfile(notice));
    }

    @Test
    void loadProfile_should_load_rng_from_profile_directory() throws Exception {
        ReflectionTestUtils.setField(pastisService, "rngLocation", "rng/");
        Notice notice = new Notice();
        notice.setPath("PA_Exemple.rng");
        ProfileResponse profileResponse = pastisService.loadProfile(notice);
        Assertions.assertNotNull(profileResponse.getProfile());
    }

    @Test
    void loadProfile_with_unknown_file_should_fail() {
        ReflectionTestUtils.setField(pastisService, "rngLocation", "rng/");
        Notice notice = new Notice();
        notice.setPath("unknown.rng");
        Assertions.assertThrows(TechnicalException.class, () -> pastisService.loadProfile(notice));
    }

    @Test
    void loadProfileFromFile_should_detect_rng_extension_when_filename_has_several_dots() throws Exception {
        final String fileName = "OK_profilRNG_mail_Seda2.3.rng";
        try (InputStream rngStream = new ClassPathResource("rng/PA_Exemple.rng").getInputStream()) {
            MultipartFile file = new MockMultipartFile("file", fileName, "application/xml", rngStream);
            ProfileResponse profileResponse = pastisService.loadProfileFromFile(file, fileName, false);
            Assertions.assertEquals(ProfileType.PA, profileResponse.getType());
            Assertions.assertNotNull(profileResponse.getProfile());
        }
    }

    @Test
    void loadProfile_should_keep_dataType_declared_in_file_over_default_table() throws Exception {
        ReflectionTestUtils.setField(pastisService, "rngLocation", "rng/");
        Notice notice = new Notice();
        notice.setPath("PA_Exemple.rng");

        ProfileResponse profileResponse = pastisService.loadProfile(notice);

        // RNGConstants maps "Size" to positiveInteger by default, but PA_Exemple.rng declares it as token.
        ElementProperties sizeElement = profileResponse
            .getProfile()
            .flattened()
            .filter(element -> "Size".equals(element.getName()))
            .findFirst()
            .orElseThrow();
        Assertions.assertEquals("token", sizeElement.getDataType());
    }

    @Test
    void getArchiveProfile_should_fail_when_root_element_has_no_name() throws Exception {
        final ElementProperties profile = new ObjectMapper()
            .readValue(new ClassPathResource("rng/ProfileJson.json").getInputStream(), ElementProperties.class);
        Assertions.assertFalse(pastisService.getArchiveProfile(profile, ProfileVersion.VERSION_2_1).isEmpty());

        final ElementProperties namelessProfile = new ElementProperties();
        Assertions.assertThrows(
            TechnicalException.class,
            () -> pastisService.getArchiveProfile(namelessProfile, ProfileVersion.VERSION_2_1)
        );
    }

    @Test
    void loadProfileFromFile_should_keep_seda_2_4_version_of_rng() throws Exception {
        String rngSeda24 = readProfileOk()
            .replace("fr:gouv:culture:archivesdefrance:seda:v2.1", "fr:gouv:culture:archivesdefrance:seda:v2.4");

        ProfileResponse profileResponse = pastisService.loadProfileFromFile(
            new MockMultipartFile("file", rngSeda24.getBytes(StandardCharsets.UTF_8)),
            "profile_seda_2_4.rng",
            false
        );

        Assertions.assertEquals(ProfileVersion.VERSION_2_4, profileResponse.getSedaVersion());
    }

    @Test
    void loadProfileFromFile_should_use_seda_2_3_when_rng_has_no_seda_version() throws Exception {
        String rngWithoutSedaVersion = readProfileOk()
            .replace("xmlns:seda=\"fr:gouv:culture:archivesdefrance:seda:v2.1\"", "")
            .replace("xmlns=\"fr:gouv:culture:archivesdefrance:seda:v2.1\"", "")
            .replace("ns=\"fr:gouv:culture:archivesdefrance:seda:v2.1\"", "");
        Assertions.assertFalse(rngWithoutSedaVersion.contains("seda:v2"));

        ProfileResponse profileResponse = pastisService.loadProfileFromFile(
            new MockMultipartFile("file", rngWithoutSedaVersion.getBytes(StandardCharsets.UTF_8)),
            "profile_without_seda_version.rng",
            false
        );

        Assertions.assertEquals(ProfileVersion.VERSION_2_3, profileResponse.getSedaVersion());
    }

    private static String readProfileOk() throws Exception {
        try (var inputStream = new ClassPathResource("profiles/profile_ok.rng").getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
