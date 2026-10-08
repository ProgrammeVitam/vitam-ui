/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.referential.server.rest;

import fr.gouv.vitamui.commons.api.download.DownloadClaims;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.referential.common.dto.ProfileDto;
import fr.gouv.vitamui.referential.server.service.profile.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private static final String PROFILE_ID = "PROFILE_ID";
    private static final String PROFILE_DOWNLOAD_RESOURCE = "profile-download";
    private static final String SIGNED_URL =
        "https://dev.vitamui.com/profile/signed-download/download?token=signed-token";

    @Mock
    private ProfileService profileService;

    @Mock
    private SignedDownloadTokenService signedDownloadTokenService;

    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        profileController = new ProfileController();
        profileController.setProfileService(profileService);
        profileController.setSignedDownloadTokenService(signedDownloadTokenService);
    }

    // ------------------------------------------------------------------ prepareSignedDownload

    @Test
    void prepareSignedDownload_should_use_profile_path_as_filename_when_profile_has_a_path() throws Exception {
        // Given
        ProfileDto profileDto = new ProfileDto();
        profileDto.setPath("archival-profile.rng");
        when(profileService.getOne(PROFILE_ID)).thenReturn(profileDto);
        when(signedDownloadTokenService.generateSignedUrl(any(), any())).thenReturn(SIGNED_URL);

        // When
        String result = profileController.prepareSignedDownload(PROFILE_ID);

        // Then
        assertThat(result).isEqualTo(SIGNED_URL);
        ArgumentCaptor<DownloadClaims> claimsCaptor = ArgumentCaptor.forClass(DownloadClaims.class);
        verify(signedDownloadTokenService).generateSignedUrl(claimsCaptor.capture(), any());
        assertThat(claimsCaptor.getValue().getParameters()).containsEntry("filename", "archival-profile.rng");
    }

    @Test
    void prepareSignedDownload_should_use_id_as_filename_when_profile_is_not_found() throws Exception {
        // Given
        when(profileService.getOne(PROFILE_ID)).thenReturn(null);
        when(signedDownloadTokenService.generateSignedUrl(any(), any())).thenReturn(SIGNED_URL);

        // When
        profileController.prepareSignedDownload(PROFILE_ID);

        // Then
        ArgumentCaptor<DownloadClaims> claimsCaptor = ArgumentCaptor.forClass(DownloadClaims.class);
        verify(signedDownloadTokenService).generateSignedUrl(claimsCaptor.capture(), any());
        assertThat(claimsCaptor.getValue().getParameters()).containsEntry("filename", PROFILE_ID);
    }

    @Test
    void prepareSignedDownload_should_use_id_as_filename_when_profile_path_is_blank() throws Exception {
        // Given
        ProfileDto profileDto = new ProfileDto();
        profileDto.setPath(" ");
        when(profileService.getOne(PROFILE_ID)).thenReturn(profileDto);
        when(signedDownloadTokenService.generateSignedUrl(any(), any())).thenReturn(SIGNED_URL);

        // When
        profileController.prepareSignedDownload(PROFILE_ID);

        // Then
        ArgumentCaptor<DownloadClaims> claimsCaptor = ArgumentCaptor.forClass(DownloadClaims.class);
        verify(signedDownloadTokenService).generateSignedUrl(claimsCaptor.capture(), any());
        assertThat(claimsCaptor.getValue().getParameters()).containsEntry("filename", PROFILE_ID);
    }

    // ------------------------------------------------------------------ signedDownload

    @Test
    void signedDownload_should_stream_file_and_set_headers_when_token_is_valid() throws Exception {
        // Given
        DownloadClaims claims = new DownloadClaims();
        claims.setResource(PROFILE_DOWNLOAD_RESOURCE);
        claims.setTenantId(1);
        claims.setParameters(Map.of("id", PROFILE_ID, "filename", "archival-profile.rng"));
        when(signedDownloadTokenService.validate("the-token", PROFILE_DOWNLOAD_RESOURCE)).thenReturn(claims);
        when(profileService.download(eq(PROFILE_ID), any())).thenReturn(
            ResponseEntity.ok(new ByteArrayResource("file-content".getBytes()))
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        profileController.signedDownload("the-token", response);

        // Then
        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION)).contains("archival-profile.rng");
        assertThat(response.getHeader(RestUtils.REFERRER_POLICY)).isEqualTo("no-referrer");
        assertThat(response.getContentAsString()).isEqualTo("file-content");
    }

    @Test
    void signedDownload_should_throw_BadRequestException_when_token_claims_are_missing_the_id() {
        // Given
        DownloadClaims claims = new DownloadClaims();
        claims.setResource(PROFILE_DOWNLOAD_RESOURCE);
        claims.setParameters(Map.of());
        when(signedDownloadTokenService.validate("the-token", PROFILE_DOWNLOAD_RESOURCE)).thenReturn(claims);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When / Then
        assertThatThrownBy(() -> profileController.signedDownload("the-token", response)).isInstanceOf(
            BadRequestException.class
        );
    }
}
