package com.automation.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.*;

/**
 * BaseTest - Foundation class for the entire framework.
 * Contains WebDriver setup and reusable helper methods.
 */
public class BaseTest {

    protected static WebDriver driver;
    protected static WebDriverWait wait;
    protected static JavascriptExecutor js;
    public static final int TIMEOUT = 30;

    /**
     * Initialize Chrome with a dedicated test profile.
     * First run: login once. Future runs: cookies persist.
     * No need to close your regular Chrome browser.
     */
    public static void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // Dedicated test profile - no conflict with open Chrome
        String testProfile = System.getProperty("user.dir")
                + "/chrome-test-profile";
        options.addArguments("--user-data-dir=" + testProfile);

        // Configure download and print preferences
        HashMap<String, Object> prefs = new HashMap<>();
        String downloadDir = System.getProperty("user.dir")
                + "/downloads";
        prefs.put("download.default_directory", downloadDir);
        prefs.put("download.prompt_for_download", false);
        prefs.put("plugins.always_open_pdf_externally", true);
        prefs.put(
                "printing.print_preview_sticky_settings.appState",
                "{\"recentDestinations\":[{\"id\":\"Save as PDF\","
                        + "\"origin\":\"local\",\"account\":\"\"}],"
                        + "\"selectedDestinationId\":\"Save as PDF\","
                        + "\"version\":2}");
        prefs.put("savefile.default_directory", downloadDir);

        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--kiosk-printing");
        options.addArguments("--disable-popup-blocking");

        driver = new ChromeDriver(options);
        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver,
                Duration.ofSeconds(TIMEOUT));
        js = (JavascriptExecutor) driver;
    }

    // ==========================================================
    // CLICK HELPERS - Uses JS fallback when normal click blocked
    // ==========================================================

    /** Safe click by locator with JavaScript fallback */
    public static void safeClick(By locator) {
        try {
            WebElement el = wait.until(
                    ExpectedConditions.elementToBeClickable(locator));
            scrollTo(el);
            pause(300);
            el.click();
        } catch (ElementClickInterceptedException
                 | StaleElementReferenceException e) {
            WebElement el = driver.findElement(locator);
            js.executeScript("arguments[0].click();", el);
        } catch (TimeoutException e) {
            WebElement el = driver.findElement(locator);
            js.executeScript(
                    "arguments[0].scrollIntoView(true);", el);
            pause(500);
            js.executeScript("arguments[0].click();", el);
        }
    }

    /** Safe click on a WebElement directly */
    public static void safeClick(WebElement el) {
        try {
            wait.until(
                    ExpectedConditions.elementToBeClickable(el));
            scrollTo(el);
            pause(300);
            el.click();
        } catch (Exception e) {
            js.executeScript("arguments[0].click();", el);
        }
    }

    // ==========================================================
    // WAIT HELPERS
    // ==========================================================

    /** Wait for element to be visible */
    public static WebElement waitVisible(By locator) {
        return wait.until(
                ExpectedConditions
                        .visibilityOfElementLocated(locator));
    }

    /** Wait for element to be clickable */
    public static WebElement waitClickable(By locator) {
        return wait.until(
                ExpectedConditions.elementToBeClickable(locator));
    }

    /** Wait with custom timeout (seconds) */
    public static WebElement waitVisible(
            By locator, int secs) {
        WebDriverWait w = new WebDriverWait(
                driver, Duration.ofSeconds(secs));
        return w.until(
                ExpectedConditions
                        .visibilityOfElementLocated(locator));
    }

    // ==========================================================
    // SCROLL HELPERS
    // ==========================================================

    /** Scroll element to center of viewport */
    public static void scrollTo(WebElement el) {
        js.executeScript(
                "arguments[0].scrollIntoView("
                        + "{behavior:'smooth',block:'center'});", el);
        pause(500);
    }

    /** Scroll to absolute bottom of page */
    public static void scrollToBottom() {
        js.executeScript(
                "window.scrollTo(0,"
                        + " document.body.scrollHeight);");
        pause(1000);
    }

    // ==========================================================
    // WINDOW/TAB HELPERS
    // ==========================================================

    /** Switch to the newest opened tab */
    public static void switchToLatestWindow() {
        ArrayList<String> tabs = new ArrayList<>(
                driver.getWindowHandles());
        driver.switchTo().window(
                tabs.get(tabs.size() - 1));
        pause(1000);
    }

    /** Close current tab, switch back to previous */
    public static void closeAndSwitchBack() {
        driver.close();
        ArrayList<String> tabs = new ArrayList<>(
                driver.getWindowHandles());
        driver.switchTo().window(
                tabs.get(tabs.size() - 1));
    }

    /** Switch to new tab only if one was opened */
    public static void switchIfNewTab(int expectedMinTabs) {
        if (driver.getWindowHandles().size()
                >= expectedMinTabs) {
            switchToLatestWindow();
        }
    }

    // ==========================================================
    // FORM HELPERS
    // ==========================================================

    /** Select dropdown by visible text */
    public static void selectByText(
            By locator, String text) {
        WebElement el = waitVisible(locator);
        new Select(el).selectByVisibleText(text);
    }

    /** Clear and type into a field */
    public static void typeText(By locator, String text) {
        WebElement el = waitVisible(locator);
        el.clear();
        pause(200);
        el.sendKeys(text);
    }

    /** Check if element exists without throwing */
    public static boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /** Thread.sleep wrapper */
    public static void pause(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ignored) {}
    }

    /** Quit the browser */
    public static void tearDown() {
        if (driver != null) { driver.quit(); }
    }
}
