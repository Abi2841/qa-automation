package com.uplift.pages.employer;

import com.uplift.config.ConfigReader;
import com.uplift.pages.BasePage;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for the Employer admin first login, reached from the invite email link:
 * enter the verification code, then set a password. Handles both a single code field and
 * one-box-per-digit code inputs, and code + password on one screen or on two consecutive screens.
 */
public class EmployerActivationPage extends BasePage {

    private final By codeInput = By.xpath("//input[@autocomplete='one-time-code']"
            + " | //label[contains(translate(normalize-space(.),'VERIFCATONDE','verifcatonde'),'code')]"
            + "/following::input[not(@type='hidden') and not(@type='password')][1]"
            + " | //input[contains(translate(@name,'CODE','code'),'code') or contains(translate(@id,'CODE','code'),'code')"
            + " or contains(translate(@placeholder,'CODE','code'),'code')]");
    private final By singleDigitInputs = By.cssSelector("input[maxlength='1']");
    private final By passwordInputs = By.cssSelector("input[type='password']");
    private final By submitButton = By.xpath("//button[@type='submit'] | //button[contains(translate(normalize-space(.),"
            + "'VERIFYCONTINUESUBMITSETPASWORD','verifycontinuesubmitsetpasword'),'verify')"
            + " or contains(translate(normalize-space(.),'VERIFYCONTINUESUBMITSETPASWORD','verifycontinuesubmitsetpasword'),'continue')"
            + " or contains(translate(normalize-space(.),'VERIFYCONTINUESUBMITSETPASWORD','verifycontinuesubmitsetpasword'),'set password')]");

    public EmployerActivationPage(WebDriver driver) {
        super(driver);
    }

    public EmployerActivationPage open(String inviteLink) {
        driver.get(inviteLink);
        return this;
    }

    public void enterVerificationCode(String code) {
        List<WebElement> digits = driver.findElements(singleDigitInputs).stream()
                .filter(WaitUtils::isDisplayedSafely)
                .collect(Collectors.toList());
        if (digits.size() >= code.length()) {
            for (int i = 0; i < code.length(); i++) {
                digits.get(i).sendKeys(String.valueOf(code.charAt(i)));
            }
        } else {
            sendKeys(codeInput, code);
        }
    }

    /**
     * Types the new password into every visible password field (password + confirm password).
     */
    public void setPassword(String password) {
        List<WebElement> fields = driver.findElements(passwordInputs).stream()
                .filter(WaitUtils::isDisplayedSafely)
                .collect(Collectors.toList());
        for (WebElement field : fields) {
            field.clear();
            field.sendKeys(password);
        }
    }

    /**
     * Completes the first login and returns the employer dashboard.
     */
    public EmployerDashboardPage activate(String code, String password) {
        visible(By.xpath("//input[not(@type='hidden')]"));
        enterVerificationCode(code);
        if (!isDisplayed(passwordInputs, 1)) {
            // Two-step flow: verify the code first, then the password screen appears
            click(submitButton);
            visible(passwordInputs);
        }
        setPassword(password);
        click(submitButton);
        WaitUtils.waitUntil(driver, d -> d.findElements(passwordInputs).stream().noneMatch(WaitUtils::isDisplayedSafely),
                ConfigReader.getExplicitWait());
        return new EmployerDashboardPage(driver);
    }
}
