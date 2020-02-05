package fr.gouv.vitamui.cucumber.front.pages.search;

import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.pages.PageObject;

@DefaultUrl("/tenant")
public class TenantSearchPage extends PageObject {

    public boolean isAppSearchPageDisplayed() {
        return $("//div[@class='vitamui-container']/h2").isDisplayed();
    }

}
