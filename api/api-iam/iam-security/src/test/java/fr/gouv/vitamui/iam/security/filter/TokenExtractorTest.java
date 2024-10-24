/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.iam.security.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TokenExtractorTest {

    private static final String TOKEN_HEADER_NAME = "tokenHeaderName";

    private static final String BEARER_TOKEN_HEADER_NAME = "Authorization";

    private static final String USER_TOKEN = "USER-TOKEN";

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldExtractTokenFromHeader() {
        given(request.getHeader(TOKEN_HEADER_NAME)).willReturn(USER_TOKEN);

        String actualToken = TokenExtractor.headerExtractor(TOKEN_HEADER_NAME).extract(request);

        assertThat(actualToken).isEqualTo(USER_TOKEN);
    }

    @Test
    void shouldExtractBearerTokenFromHeader() {
        given(request.getHeader(BEARER_TOKEN_HEADER_NAME)).willReturn("Bearer " + USER_TOKEN);

        String actualToken = TokenExtractor.bearerExtractor().extract(request);

        assertThat(actualToken).isEqualTo(USER_TOKEN);
    }

    @Test
    void shouldReturnNullWhenTokenIsEmpty() {
        given(request.getHeader(BEARER_TOKEN_HEADER_NAME)).willReturn(null);

        String actualToken = TokenExtractor.bearerExtractor().extract(request);

        assertThat(actualToken).isNull();
    }
}
