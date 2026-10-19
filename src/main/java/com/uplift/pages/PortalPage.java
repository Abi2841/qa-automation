package com.uplift.pages;

import com.uplift.config.ConfigReader;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

/**
 * Behaviour shared by every page behind a login in both portals: navigation menu and logout.
 */
public abstract class PortalPage extends BasePage {

    private final By logoutAction = By.xpath("//*[self::a or self::button or @role='menuitem' or @role='button']"
            + "[contains(translate(normalize-space(.),'LOGUTSIN','logutsin'),'log out')"
            + " or contains(translate(normalize-space(.),'LOGUTSIN','logutsin'),'logout')"
            + " or contains(translate(normalize-space(.),'LOGUTSIN','logutsin'),'sign out')]");
    private final By userMenuToggles = By.cssSelector(
            "[aria-haspopup='menu'], [aria-haspopup='true'], [data-testid*='user'], [class*='avatar'], [class*='profile']");

    protected PortalPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks a menu item by its exact text, falling back to "contains" (e.g. "Employers 12").
     */
    protected void navigateTo(String menuText) {
        By exact = navItemExactly(menuText);
        if (isDisplayed(exact, ConfigReader.getShortWait())) {
            click(exact);
        } else {
            click(actionWithText(menuText));
        }
    }

    /**
     * Logs out, opening the user/profile menu first if the logout action is hidden inside it.
     */
    public void logout() {
        if (!isDisplayed(logoutAction, 2)) {
            List<WebElement> toggles = driver.findElements(userMenuToggles);
            for (WebElement toggle : toggles) {
                if (!WaitUtils.isDisplayedSafely(toggle)) {
                    continue;
                }
                toggle.click();
                if (isDisplayed(logoutAction, 2)) {
                    break;
                }
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            }
        }
        click(logoutAction);
    }
}
