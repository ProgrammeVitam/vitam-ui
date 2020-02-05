package fr.gouv.vitamui.cucumber.front.transformers;

import cucumber.api.Transformer;
import fr.gouv.vitamui.cucumber.front.utils.UserEnum;


public class UserTransformer extends Transformer<UserEnum> {

    @Override
    public UserEnum transform(final String label) {
        return UserEnum.valueOf(label.toUpperCase());
    }

}
