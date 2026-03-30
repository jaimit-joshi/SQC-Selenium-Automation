package com.automation.pages;

import com.automation.base.BaseTest;
import com.automation.utils.ExcelUtil;
import com.automation.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import java.util.List;
import java.util.Map;

/**
 * CanvasPage - Scenario 2: Create 2 calendar events.
 * Pure click approach - no JS.
 */
public class CanvasPage extends BaseTest {

    private static final By CALENDAR_LINK = By.cssSelector(
            "#global_nav_calendar_link");
    private static final By CREATE_BTN = By.cssSelector(
            "#create_new_event_link");
    private static final By EVENT_TAB = By.xpath(
            "//span[text()='Event']/parent::a"
                    + " | //li[contains(@class,'event')]//a");
    private static final By TITLE_INPUT = By.xpath(
            "//input[contains(@id,'title')"
                    + " or @name='calendar_event[title]'"
                    + " or contains(@placeholder,'Title')]");
    private static final By CALENDAR_DD = By.xpath(
            "//select[contains(@id,'context')"
                    + " or contains(@name,'context')]");

    // Submit button - exact ID from inspect
    private static final By SUBMIT_BTN = By.cssSelector(
            "button#edit-calendar-event-submit-button");

    // The ∨ arrow spans that open the time dropdowns
    private static final By START_ARROW = By.xpath(
            "//input[@data-testid='event-form-start-time']"
                    + "/following-sibling::span"
                    + "[contains(@class,'afterElement')]");
    private static final By END_ARROW = By.xpath(
            "//input[@data-testid='event-form-end-time']"
                    + "/following-sibling::span"
                    + "[contains(@class,'afterElement')]");

    // Date field wrapper
    private static final By DATE_WRAPPER = By.xpath(
            "//input[@data-testid="
                    + "'edit-calendar-event-form-date']"
                    + "/ancestor::span"
                    + "[contains(@class,'textInput')]");

    public static String addCalendarEvents(String scenario)
            throws Exception {

        driver.get(
                "https://northeastern.instructure.com/calendar");
        pause(3000);
        ScreenshotUtil.take(scenario, "07_canvas_calendar");

        if (!driver.getCurrentUrl().contains("/calendar")) {
            try {
                safeClick(CALENDAR_LINK);
                pause(2000);
            } catch (Exception ignored) {}
        }
        ScreenshotUtil.take(scenario, "07_on_calendar");

        List<Map<String, String>> events =
                ExcelUtil.getAllRows("EventData");
        int count = Math.min(events.size(), 2);

        for (int i = 0; i < count; i++) {
            Map<String, String> ev = events.get(i);
            String p = "Event" + (i + 1) + "_";

            System.out.println("  Creating event: "
                    + ev.get("title")
                    + " | date=" + ev.get("date")
                    + " | start=" + ev.get("startTime")
                    + " | end=" + ev.get("endTime"));

            // Click "+" to create new event
            ScreenshotUtil.take(scenario,
                    p + "08_before_plus");
            safeClick(CREATE_BTN);
            pause(1500);

            // Ensure "Event" tab is selected
            try {
                WebElement tab = driver.findElement(EVENT_TAB);
                if (tab.isDisplayed()) {
                    safeClick(tab);
                    pause(500);
                }
            } catch (Exception ignored) {}

            ScreenshotUtil.take(scenario,
                    p + "08_dialog_open");

            // === FILL TITLE ===
            ScreenshotUtil.take(scenario,
                    p + "09_before_title");
            WebElement titleEl = waitVisible(TITLE_INPUT);
            titleEl.clear();
            titleEl.sendKeys(ev.get("title"));
            ScreenshotUtil.take(scenario,
                    p + "09_after_title");

            // === FILL DATE ===
            ScreenshotUtil.take(scenario,
                    p + "10_before_date");

            safeClick(DATE_WRAPPER);
            pause(500);

            Actions actions = new Actions(driver);
            actions.keyDown(Keys.COMMAND)
                    .sendKeys("a")
                    .keyUp(Keys.COMMAND)
                    .perform();
            pause(200);
            actions.sendKeys(ev.get("date")).perform();
            pause(300);

            // Click "From" label to close datepicker
            try {
                WebElement fromLabel = driver.findElement(
                        By.xpath("//span[text()='From']"));
                safeClick(fromLabel);
            } catch (Exception e) {
                safeClick(TITLE_INPUT);
            }
            pause(500);

            System.out.println("  Date set to: "
                    + ev.get("date"));
            ScreenshotUtil.take(scenario,
                    p + "10_after_date");

            // === FILL START TIME ===
            ScreenshotUtil.take(scenario,
                    p + "10b_before_times");
            selectTimeFromDropdown(START_ARROW,
                    "event-form-start-time",
                    ev.get("startTime"), "start");
            pause(500);

            // === FILL END TIME ===
            selectTimeFromDropdown(END_ARROW,
                    "event-form-end-time",
                    ev.get("endTime"), "end");
            pause(500);
            ScreenshotUtil.take(scenario,
                    p + "10b_after_times");

            // === SELECT CALENDAR ===
            ScreenshotUtil.take(scenario,
                    p + "11_before_calendar");
            try {
                WebElement dd =
                        driver.findElement(CALENDAR_DD);
                Select sel = new Select(dd);
                try {
                    sel.selectByVisibleText(
                            ev.get("calendar"));
                } catch (Exception e2) {
                    for (WebElement opt : sel.getOptions()) {
                        if (opt.getText().contains(
                                ev.get("calendar"))) {
                            sel.selectByVisibleText(
                                    opt.getText());
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
            ScreenshotUtil.take(scenario,
                    p + "11_after_calendar");

            // === CLICK SUBMIT ===
            ScreenshotUtil.take(scenario,
                    p + "12_before_submit");
            safeClick(SUBMIT_BTN);
            pause(3000);
            ScreenshotUtil.take(scenario,
                    p + "12_after_submit");

            System.out.println("  Event '"
                    + ev.get("title") + "' submitted");
        }

        pause(2000);
        ScreenshotUtil.take(scenario,
                "14_both_events_done");

        // === REAL VERIFICATION ===
        // Check if the created events are visible on the calendar
        int verifiedCount = 0;
        for (int i = 0; i < count; i++) {
            String title = events.get(i).get("title");
            try {
                List<WebElement> found = driver.findElements(
                        By.xpath("//*[contains(text(),'"
                                + title + "')]"));
                if (!found.isEmpty()) {
                    verifiedCount++;
                    System.out.println(
                            "  VERIFIED: Event '" + title
                                    + "' found on calendar");
                } else {
                    System.out.println(
                            "  NOT FOUND: Event '" + title
                                    + "' not visible on calendar");
                }
            } catch (Exception e) {
                System.out.println(
                        "  NOT FOUND: Event '" + title
                                + "' - " + e.getMessage());
            }
        }

        return verifiedCount + " of " + count
                + " events verified on Canvas calendar";
    }

    /**
     * Click ∨ arrow to open dropdown, click wrapper
     * to get focus, type to filter, click match.
     */
    private static void selectTimeFromDropdown(
            By arrowLocator, String dataTestId,
            String timeStr, String label) {
        try {
            // Step 1: Click the ∨ arrow to open dropdown
            WebElement arrow = driver.findElement(
                    arrowLocator);
            safeClick(arrow);
            pause(1000);

            // Step 2: Click the input wrapper to get focus
            WebElement inputWrapper = driver.findElement(
                    By.xpath(
                            "//input[@data-testid='" + dataTestId
                                    + "']/ancestor::span"
                                    + "[contains(@class,'inputLayout')]"));
            safeClick(inputWrapper);
            pause(500);

            // Step 3: Select all and type the time
            new Actions(driver)
                    .keyDown(Keys.COMMAND).sendKeys("a")
                    .keyUp(Keys.COMMAND).perform();
            pause(100);
            new Actions(driver)
                    .sendKeys(timeStr).perform();
            pause(1000);

            // Step 4: Click the matching option
            boolean clicked = false;
            try {
                WebElement option = driver.findElement(
                        By.xpath(
                                "//*[@role='option']"
                                        + "[normalize-space(text())='"
                                        + timeStr + "']"));
                safeClick(option);
                clicked = true;
            } catch (Exception ignored) {}

            if (!clicked) {
                try {
                    WebElement option = driver.findElement(
                            By.xpath(
                                    "//*[@role='option']"
                                            + "[contains(text(),'"
                                            + timeStr + "')]"));
                    safeClick(option);
                    clicked = true;
                } catch (Exception ignored) {}
            }

            if (!clicked) {
                new Actions(driver)
                        .sendKeys(Keys.ENTER).perform();
            }

            pause(300);
            System.out.println("  " + label
                    + " time set to: " + timeStr
                    + " (clicked: " + clicked + ")");

        } catch (Exception e) {
            System.out.println("  " + label
                    + " time FAILED: " + e.getMessage());
        }
    }
}