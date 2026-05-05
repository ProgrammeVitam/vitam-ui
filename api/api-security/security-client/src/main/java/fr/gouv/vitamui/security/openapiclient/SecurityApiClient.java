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

package fr.gouv.vitamui.security.openapiclient;

import fr.gouv.vitamui.security.openapiclient.invoker.ApiClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

public class SecurityApiClient extends ApiClient {

    // No auth needed for security

    public SecurityApiClient(RestClient restClient) {
        super(restClient);
    }

    @Override
    protected void addHeadersToRequest(HttpHeaders headers, RestClient.RequestBodySpec requestBuilder) {
        try {
            java.util.Set<java.util.Map.Entry<String, java.util.List<String>>> entries;
            try {
                java.lang.reflect.Method headerSetMethod = HttpHeaders.class.getMethod("headerSet");
                entries = (java.util.Set<java.util.Map.Entry<String, java.util.List<String>>>) headerSetMethod.invoke(
                    headers
                );
            } catch (NoSuchMethodException e) {
                java.lang.reflect.Method entrySetMethod = HttpHeaders.class.getMethod("entrySet");
                entries = (java.util.Set<java.util.Map.Entry<String, java.util.List<String>>>) entrySetMethod.invoke(
                    headers
                );
            }
            for (java.util.Map.Entry<String, java.util.List<String>> entry : entries) {
                java.util.List<String> values = entry.getValue();
                for (String value : values) {
                    if (value != null) {
                        requestBuilder.header(entry.getKey(), value);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to add headers to request", e);
        }
    }
}
