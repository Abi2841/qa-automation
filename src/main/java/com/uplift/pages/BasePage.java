package com.uplift.pages;

import com.uplift.config.ConfigReader;
import com.uplift.reporting.ReportLogger;
import com.uplift.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Base Page Object encapsulating common browser actions and explicit wait strategies.
 *
 * Locator strategy: the QA portals were not available while the framework was built, so fields
 * are located by the visible labels named in the assignment's application guide (e.g. "IBAN",
 * "PPSN", "Add employer"), with id/name/placeholder/aria-label fallbacks. When the real DOM is
 * inspected, only the locator fields in the page classes need to change.
 */
public abstract class BasePage {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final Pattern SUCCESS_TEXT = Pattern.compile(
            "(?i)success|created|saved|added|invited|invitation sent|has been sent");

    protected static final By SUCCESS_MESSAGE = By.cssSelector(
            "[role='alert'], [role='status'], [aria-live], .toast, [class*='toast'], [class*='snackbar'],"
                    + " [class*='notification'], [class*='alert'], [class*='success']");
    protected static final By VALIDATION_MESSAGE = By.cssSelector(
            "[role='alert'], .invalid-feedback, .error-message, .field-error, .text-danger, .help-block.error,"
                    + " [class*='error']:not(input):not(form), [class*='invalid']:not(input):not(form), [id*='error']");

    protected final WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    // ------------------------------------------------------------------ locator builders

    private static String lower(String xpathExpr) {
        return "translate(" + xpathExpr + ",'" + UPPER + "','" + LOWER + "')";
    }

    private static String literal(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        return "concat('" + value.replace("'", "',\"'\",'") + "')";
    }

    private static String compact(String value) {
        return value.toLowerCase().replaceAll("[\\s_-]", "");
    }

    /**
     * Input, select or textarea identified by any of the given label texts (case-insensitive "contains"),
     * or by id / name / placeholder / aria-label containing the same words.
     */
    protected static By fieldByLabel(String... labels) {
        StringBuilder labelCond = new StringBuilder();
        StringBuilder attrCond = new StringBuilder();
        for (String label : labels) {
            if (labelCond.length() > 0) {
                labelCond.append(" or ");
                attrCond.append(" or ");
            }
            String l = literal(label.toLowerCase());
            String c = literal(compact(label));
            labelCond.append("contains(").append(lower("normalize-space(.)")).append(",").append(l).append(")");
            attrCond.append("contains(").append(lower("@aria-label")).append(",").append(l).append(")")
                    .append(" or contains(").append(lower("@placeholder")).append(",").append(l).append(")");
            for (String attr : new String[]{"@id", "@name", "@data-testid"}) {
                attrCond.append(" or contains(translate(").append(attr).append(",'").append(UPPER).append(" _-','")
                        .append(LOWER).append("'),").append(c).append(")");
            }
        }
        String fieldTypes = "self::input[not(@type='hidden') and not(@type='checkbox') and not(@type='radio')"
                + " and not(@type='submit') and not(@type='button')] or self::select or self::textarea";
        return By.xpath("//label[" + labelCond + "]/following::*[" + fieldTypes + "][1]"
                + " | //*[" + fieldTypes + "][" + attrCond + "]");
    }

    /**
     * Button, link, tab or menu item whose visible text contains ALL the given words.
     */
    protected static By actionWithText(String... words) {
        String cond = java.util.Arrays.stream(words)
                .map(w -> "contains(" + lower("normalize-space(.)") + "," + literal(w.toLowerCase()) + ")")
                .collect(Collectors.joining(" and "));
        return By.xpath("//*[self::button or self::a or @role='button' or @role='tab' or @role='menuitem'"
                + " or @role='link' or (self::input and (@type='submit' or @type='button'))]"
                + "[" + cond + " or (self::input and contains(" + lower("@value") + ","
                + literal(words[0].toLowerCase()) + "))]");
    }

    /**
     * Navigation link / tab whose visible text is exactly the given text (case-insensitive).
     */
    protected static By navItemExactly(String text) {
        return By.xpath("//*[self::a or self::button or @role='tab' or @role='menuitem' or @role='link']"
                + "[" + lower("normalize-space(.)") + "=" + literal(text.toLowerCase()) + "]");
    }

    /**
     * Table row, list item or ARIA row containing the given text.
     */
    protected static By rowContaining(String text) {
        String t = literal(text);
        return By.xpath("//tr[contains(normalize-space(.)," + t + ")] | //*[@role='row'][contains(normalize-space(.),"
                + t + ")] | //li[contains(normalize-space(.)," + t + ")]");
    }

    // ------------------------------------------------------------------ actions

    protected WebElement visible(By locator) {
        return WaitUtils.waitForFirstVisible(driver, locator, ConfigReader.getExplicitWait());
    }

    protected void click(By locator) {
        WebElement element = WaitUtils.waitForFirstClickable(driver, locator, ConfigReader.getExplicitWait());
        scrollIntoView(element);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            // A sticky header or fading toast covered the element; a JS click still exercises its handler
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    protected void sendKeys(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return visible(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        return isDisplayed(locator, ConfigReader.getShortWait());
    }

    protected boolean isDisplayed(By locator, int timeoutSeconds) {
        try {
            WaitUtils.waitForFirstVisible(driver, locator, timeoutSeconds);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    /**
     * Sets a form field: dropdowns are selected by visible text, everything else is typed.
     * Empty or null values leave the field untouched (used by "mandatory field empty" scenarios).
     */
    protected void setField(By locator, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        WebElement element = visible(locator);
        if ("select".equalsIgnoreCase(element.getTagName())) {
            selectOption(element, value, false);
        } else {
            element.clear();
            element.sendKeys(value);
        }
    }

    /**
     * Selects an option by visible text. If {@code fallbackToFirst} is true and the preferred value is not
     * offered, the first real option is chosen and logged (values like "occupation" vary per environment).
     */
    protected void setDropdown(By locator, String preferred, boolean fallbackToFirst) {
        WebElement element = visible(locator);
        if ("select".equalsIgnoreCase(element.getTagName())) {
            selectOption(element, preferred, fallbackToFirst);
            return;
        }
        // Custom (non-native) dropdown or free-text input
        String role = element.getAttribute("role");
        boolean combobox = "combobox".equalsIgnoreCase(role) || "true".equals(element.getAttribute("aria-haspopup"))
                || "true".equals(element.getAttribute("readonly"));
        if (!combobox && "input".equalsIgnoreCase(element.getTagName())) {
            element.clear();
            element.sendKeys(preferred);
        } else {
            scrollIntoView(element);
            element.click();
        }
        By options = By.xpath("//*[@role='option' or (self::li and ancestor::*[@role='listbox'])]");
        By preferredOption = By.xpath("//*[@role='option' or (self::li and ancestor::*[@role='listbox'])]"
                + "[contains(" + lower("normalize-space(.)") + "," + literal(preferred.toLowerCase()) + ")]");
        if (isDisplayed(preferredOption, ConfigReader.getShortWait())) {
            click(preferredOption);
        } else if (isDisplayed(options, 1)) {
            if (!fallbackToFirst) {
                throw new IllegalStateException("Option '" + preferred + "' not offered in dropdown " + locator);
            }
            WebElement first = WaitUtils.waitForFirstVisible(driver, options, 1);
            ReportLogger.warning("Option '" + preferred + "' not offered; selected '" + first.getText() + "'");
            first.click();
        }
        // No list appeared: the value was typed into a free-text field
    }

    private void selectOption(WebElement selectElement, String value, boolean fallbackToFirst) {
        Select select = new Select(selectElement);
        for (WebElement option : select.getOptions()) {
            if (option.getText().trim().equalsIgnoreCase(value)) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }
        for (WebElement option : select.getOptions()) {
            if (option.getText().toLowerCase().contains(value.toLowerCase())) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }
        if (!fallbackToFirst) {
            throw new IllegalStateException("Option '" + value + "' not found in dropdown");
        }
        List<WebElement> real = select.getOptions().stream()
                .filter(o -> !o.getText().trim().isEmpty() && !o.getText().toLowerCase().startsWith("select")
                        && !o.getText().toLowerCase().startsWith("please") && o.isEnabled())
                .collect(Collectors.toList());
        if (real.isEmpty()) {
            throw new IllegalStateException("Dropdown has no selectable options");
        }
        ReportLogger.warning("Option '" + value + "' not offered; selected '" + real.get(0).getText() + "'");
        select.selectByVisibleText(real.get(0).getText());
    }

    /**
     * Answers a yes/no style question (e.g. the PEP question) by clicking the radio/label whose text
     * or value equals the answer, located after the question text.
     */
    protected void answerQuestion(String questionText, String answer) {
        String q = literal(questionText.toLowerCase());
        String a = literal(answer.toLowerCase());
        String question = "//*[contains(" + lower("normalize-space(text())") + "," + q + ")]";
        By option = By.xpath("(" + question + "/following::label[" + lower("normalize-space(.)") + "=" + a + "]"
                + " | " + question + "/following::input[@type='radio'][" + lower("@value") + "=" + a + "]"
                + " | " + question + "/following::*[@role='radio'][" + lower("normalize-space(.)") + "=" + a + "])");
        WebElement element = WaitUtils.waitForFirstVisible(driver, option, ConfigReader.getExplicitWait());
        scrollIntoView(element);
        element.click();
    }

    /**
     * Enters an ISO (yyyy-MM-dd) date. HTML5 date inputs get the ISO value through the native setter
     * (typing is locale-dependent); text inputs get the date typed in the configured UI format.
     */
    protected void setDate(By locator, String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return;
        }
        WebElement element = visible(locator);
        if ("date".equalsIgnoreCase(element.getAttribute("type"))) {
            ((JavascriptExecutor) driver).executeScript(
                    "const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;"
                            + "setter.call(arguments[0], arguments[1]);"
                            + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));"
                            + "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                    element, isoDate);
        } else {
            String formatted = LocalDate.parse(isoDate)
                    .format(DateTimeFormatter.ofPattern(ConfigReader.getProperty("ui.date.format", "dd/MM/yyyy")));
            element.clear();
            element.sendKeys(formatted);
        }
    }

    // ------------------------------------------------------------------ verification helpers

    /**
     * Returns the text of a visible success toast/banner, or null if none appears within the timeout.
     * Call immediately after the action, because toasts disappear after a few seconds.
     */
    public String waitForSuccessMessage(int timeoutSeconds) {
        try {
            return WaitUtils.waitUntil(driver, d -> d.findElements(SUCCESS_MESSAGE).stream()
                    .filter(WaitUtils::isDisplayedSafely)
                    .map(WebElement::getText)
                    .filter(t -> SUCCESS_TEXT.matcher(t).find())
                    .findFirst()
                    .orElse(null), timeoutSeconds);
        } catch (TimeoutException e) {
            return null;
        }
    }

    /**
     * Waits until the visible page text contains the given text.
     */
    public boolean pageContains(String text, int timeoutSeconds) {
        try {
            return WaitUtils.waitUntil(driver, d -> getPageText().contains(text), timeoutSeconds);
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getPageText() {
        return driver.findElement(By.tagName("body")).getText();
    }
}
