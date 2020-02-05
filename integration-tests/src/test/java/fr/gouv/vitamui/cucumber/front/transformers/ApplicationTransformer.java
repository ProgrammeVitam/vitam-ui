package fr.gouv.vitamui.cucumber.front.transformers;

import cucumber.api.Transformer;
import fr.gouv.vitamui.cucumber.front.utils.ApplicationEnum;


public class ApplicationTransformer extends Transformer<ApplicationEnum> {

    @Override
    public ApplicationEnum transform(final String label) {
        return ApplicationEnum.valueOf(label.toUpperCase());
    }

}
