package fr.gouv.vitamui.cucumber.front.pages.search;

import java.util.List;

import org.openqa.selenium.WebElement;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.thucydides.core.annotations.DefaultUrl;
import net.thucydides.core.pages.PageObject;

@DefaultUrl("/tenant/9")
public class ArchiveSearchPage extends PageObject {

    @FindBy(css = ".vitamui-table-row")
    private List<WebElement> resultRows;

    @FindBy(css = ".search button")
    WebElement searchButton;

    public boolean isAppSearchPageDisplayed() {
        return $("//app-search-page").isDisplayed();
    }

    public void searchByText(final String keyword) {
        $("//div[@class='search']/input").sendKeys(keyword);
        searchButton.click();
    }

    public int getResultRowNumber() {
        return resultRows.size();
    }

}
