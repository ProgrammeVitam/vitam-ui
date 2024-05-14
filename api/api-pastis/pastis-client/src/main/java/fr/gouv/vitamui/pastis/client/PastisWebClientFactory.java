package fr.gouv.vitamui.pastis.client;

import fr.gouv.vitamui.commons.rest.client.BaseWebClientFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;

public class PastisWebClientFactory extends BaseWebClientFactory {

    public PastisWebClientFactory(RestClientConfiguration restClientConfiguration) {
        super(restClientConfiguration);
    }

    public PastisTransformationWebClient getPastisTransformationWebClient() {
        return new PastisTransformationWebClient(getWebClient(), getBaseUrl());
    }

    @Override
    public String getBaseUrl() {
        return super.getBaseUrl();
    }
}
