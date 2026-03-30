package com.automation.pages;

import com.automation.base.BaseTest;
import com.automation.utils.ExcelUtil;
import com.automation.utils.ScreenshotUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Map;

/**
 * LoginPage - Handles BOTH NEU login types:
 * 1. Microsoft Azure AD SSO (Student Hub, Canvas)
 *    - uses "username" and "password" from Excel
 * 2. NEU native login (Banner Self-Service, Transcript)
 *    - uses "neuUsername" and "neuPassword" from Excel
 */
public class LoginPage extends BaseTest {

    // Microsoft Azure AD SSO locators
    private static final By MS_USERNAME =
            By.cssSelector("input[name='loginfmt']");
    private static final By MS_NEXT_BTN =
            By.cssSelector("#idSIButton9");
    private static final By MS_PASSWORD =
            By.cssSelector("input[name='passwd']");
    private static final By MS_SIGN_IN =
            By.cssSelector("#idSIButton9");
    private static final By MS_STAY_SIGNED =
            By.cssSelector("#idSIButton9");

    // NEU native login locators (Banner/Self-Service)
    private static final By NEU_USERNAME = By.xpath(
            "//input[contains(@id,'username')"
                    + " or contains(@name,'username')"
                    + " or contains(@id,'user')]");
    private static final By NEU_PASSWORD = By.xpath(
            "//input[@type='password']");
    private static final By NEU_LOGIN_BTN = By.xpath(
            "//button[contains(text(),'Log In')]"
                    + " | //input[@type='submit']"
                    + "[contains(@value,'Log In')]"
                    + " | //button[@type='submit']"
                    + " | //input[@type='submit']");

    /**
     * Login to any NEU portal - auto-detects login type.
     */
    public static void loginToNEU(String url, String scenario)
            throws Exception {

        driver.get(url);
        pause(3000);
        ScreenshotUtil.take(scenario, "01_navigate_to_url");

        handleAnyLogin(scenario);
    }

    /**
     * Detects which login page is showing and handles it.
     * Reads the appropriate credentials from Excel.
     */
    public static void handleAnyLogin(String scenario)
            throws Exception {

        Map<String, String> creds =
                ExcelUtil.getRowData("LoginData", 0);

        String currentUrl = driver.getCurrentUrl();
        String pageSource = driver.getPageSource();

        boolean isMsLogin =
                currentUrl.contains("login.microsoftonline")
                        || currentUrl.contains("login.live.com")
                        || isElementPresent(MS_USERNAME);

        boolean isNeuLogin =
                currentUrl.contains("neuidmsso")
                        || currentUrl.contains("nubanner")
                        || pageSource.contains("myNortheastern Username")
                        || pageSource.contains("myNortheastern Password");

        if (!isMsLogin && !isNeuLogin) {
            ScreenshotUtil.take(scenario,
                    "01_already_logged_in");
            return;
        }

        if (isMsLogin) {
            // Uses SSO credentials: username + password
            doMicrosoftLogin(
                    creds.get("username"),
                    creds.get("password"),
                    scenario);
        } else if (isNeuLogin) {
            // Uses NEU native credentials: neuUsername + neuPassword
            doNeuNativeLogin(
                    creds.get("neuUsername"),
                    creds.get("neuPassword"),
                    scenario);
        }
    }

    /**
     * Microsoft SSO login (Student Hub, Canvas).
     * Uses: username + password columns from Excel.
     */
    private static void doMicrosoftLogin(
            String username, String password, String scenario)
            throws Exception {

        ScreenshotUtil.take(scenario, "02_before_ms_username");
        waitVisible(MS_USERNAME).sendKeys(username);
        ScreenshotUtil.take(scenario, "02_after_ms_username");
        safeClick(MS_NEXT_BTN);
        pause(2000);

        // Handle org picker if shown
        try {
            WebElement org = waitVisible(
                    By.xpath(
                            "//div[contains(@data-test-id,'org')]"), 3);
            safeClick(org);
            pause(2000);
        } catch (Exception ignored) {}

        ScreenshotUtil.take(scenario, "03_before_ms_password");
        waitVisible(MS_PASSWORD).sendKeys(password);
        ScreenshotUtil.take(scenario, "03_after_ms_password");
        safeClick(MS_SIGN_IN);
        pause(2000);
        ScreenshotUtil.take(scenario, "04_after_ms_sign_in");

        // Duo 2FA - auto-wait
        System.out.println(
                "\n>>> APPROVE DUO 2FA ON YOUR PHONE NOW...");
        System.out.println(">>> Waiting automatically...");
        WebDriverWait duoWait = new WebDriverWait(
                driver, Duration.ofSeconds(60));
        duoWait.until(d ->
                !d.getCurrentUrl().contains("login.microsoftonline")
                        && !d.getCurrentUrl().contains("duosecurity")
                        && !d.getCurrentUrl().contains("login.live.com"));
        pause(3000);
        ScreenshotUtil.take(scenario, "05_after_duo_2fa");

        // Stay signed in prompt
        try {
            WebDriverWait sw = new WebDriverWait(
                    driver, Duration.ofSeconds(5));
            sw.until(ExpectedConditions
                    .elementToBeClickable(MS_STAY_SIGNED)).click();
            pause(2000);
        } catch (Exception ignored) {}

        ScreenshotUtil.take(scenario, "06_ms_login_complete");
    }

    /**
     * NEU native login (Banner, Self-Service, Transcript).
     * Uses: neuUsername + neuPassword columns from Excel.
     * These are DIFFERENT from the Microsoft SSO credentials.
     */
    private static void doNeuNativeLogin(
            String neuUsername, String neuPassword,
            String scenario) throws Exception {

        ScreenshotUtil.take(scenario, "02_neu_login_page");

        waitVisible(NEU_USERNAME).sendKeys(neuUsername);
        ScreenshotUtil.take(scenario, "03_neu_username");

        waitVisible(NEU_PASSWORD).sendKeys(neuPassword);
        ScreenshotUtil.take(scenario, "03_neu_password");

        safeClick(NEU_LOGIN_BTN);
        pause(3000);
        ScreenshotUtil.take(scenario, "04_neu_after_login");

        // Duo 2FA may appear here too
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("duosecurity")
                || currentUrl.contains("duo")) {
            System.out.println(
                    "\n>>> APPROVE DUO 2FA (NEU LOGIN)...");
            System.out.println(">>> Waiting automatically...");
            WebDriverWait duoWait = new WebDriverWait(
                    driver, Duration.ofSeconds(60));
            duoWait.until(d ->
                    !d.getCurrentUrl().contains("duo"));
            pause(3000);
        }

        ScreenshotUtil.take(scenario, "05_neu_login_done");
    }

    /**
     * Call this mid-scenario when a login page might appear
     * (e.g., clicking transcript link triggers NEU login).
     */
    public static void handleIfLoginAppears(String scenario)
            throws Exception {
        pause(2000);
        String url = driver.getCurrentUrl();
        String page = driver.getPageSource();

        if (url.contains("login.microsoftonline")
                || url.contains("neuidmsso")
                || url.contains("nubanner")
                || page.contains("myNortheastern Username")
                || page.contains("myNortheastern Password")
                || isElementPresent(MS_USERNAME)) {

            handleAnyLogin(scenario);
        }
    }

    /** Shortcut: login to Canvas LMS */
    public static void loginToCanvas(String scenario)
            throws Exception {
        loginToNEU(
                "https://northeastern.instructure.com/login/saml",
                scenario);
    }
}