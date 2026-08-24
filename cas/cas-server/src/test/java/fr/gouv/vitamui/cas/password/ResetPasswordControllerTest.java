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
package fr.gouv.vitamui.cas.password;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.common.dto.cas.PasswordResetUrlDto;
import jakarta.servlet.http.HttpServletRequest;
import org.apereo.cas.pm.PasswordResetUrlBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ResetPasswordControllerTest {

    private static final String EMAIL = "john.doe@vitamui.com";

    private static final String CUSTOMER_ID = "customerId";

    private static final String RESET_URL = "https://cas.vitamui.com/cas/login?pswdrst=PWDRST-1";

    private PasswordResetUrlBuilder passwordResetUrlBuilder;

    private ResetPasswordController controller;

    private HttpServletRequest httpRequest;

    @Before
    public void setUp() throws Throwable {
        passwordResetUrlBuilder = mock(PasswordResetUrlBuilder.class);
        httpRequest = new MockHttpServletRequest();

        when(passwordResetUrlBuilder.build(anyString())).thenReturn(URI.create(RESET_URL).toURL());

        controller = new ResetPasswordController(mock(Utils.class), passwordResetUrlBuilder, new ObjectMapper());
    }

    @Test
    public void testBuildUrlOk() throws Throwable {
        final ResponseEntity<PasswordResetUrlDto> response = controller.buildPasswordResetUrl(
            EMAIL,
            CUSTOMER_ID,
            httpRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUrl()).isEqualTo(RESET_URL);
        assertThat(response.getBody().getExpirationInMinutes()).isEqualTo(24 * 60L);
        verify(passwordResetUrlBuilder).build(anyString());
    }

    @Test
    public void testTheTokenIsBoundToTheLowercasedEmail() throws Throwable {
        controller.buildPasswordResetUrl("  JOHN.DOE@VITAMUI.COM ", CUSTOMER_ID, httpRequest);

        verify(passwordResetUrlBuilder).build(
            "{\"userEmail\":\"" + EMAIL + "\",\"customerId\":\"" + CUSTOMER_ID + "\"}"
        );
    }

    @Test
    public void testNoEmail() throws Throwable {
        final ResponseEntity<PasswordResetUrlDto> response = controller.buildPasswordResetUrl(
            "  ",
            CUSTOMER_ID,
            httpRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(passwordResetUrlBuilder, never()).build(anyString());
    }

    @Test
    public void testNoCustomerId() throws Throwable {
        final ResponseEntity<PasswordResetUrlDto> response = controller.buildPasswordResetUrl(EMAIL, "", httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(passwordResetUrlBuilder, never()).build(anyString());
    }

    @Test
    public void testUrlBuilderFailure() throws Throwable {
        when(passwordResetUrlBuilder.build(anyString())).thenThrow(new IllegalStateException("boom"));

        final ResponseEntity<PasswordResetUrlDto> response = controller.buildPasswordResetUrl(
            EMAIL,
            CUSTOMER_ID,
            httpRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
