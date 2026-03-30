package com.automation.pages;

import com.automation.base.BaseTest;
import com.automation.utils.ExcelUtil;
import com.automation.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import java.util.List;
import java.util.Map;

/**
 * StudentHubPage - Scenario 1 (Transcript) & Scenario 5 (Calendar).
 */
public class StudentHubPage extends BaseTest {

    private static final By RESOURCES_TAB = By.xpath(
            "//a[contains(text(),'Resources')]"
                    + " | //span[contains(text(),'Resources')]/parent::a");
    private static final By ACADEMICS_SECTION = By.xpath(
            "//*[contains(text(),'Academics, Classes')"
                    + " and contains(text(),'Registration')]");
    private static final By UNOFFICIAL_TRANSCRIPT = By.xpath(
            "//a[contains(text(),'Unofficial Transcript')]");
    private static final By ACADEMIC_CALENDAR = By.xpath(
            "//a[contains(text(),'Academic Calendar')]");

    private static final By AC_CALENDAR_BOX = By.cssSelector(
            "a[href*='academic-calendar'][class*='item']");

    // ======================================================
    // SCENARIO 1: Download the latest transcript
    // ======================================================
    public static String downloadTranscript(String scenario)
            throws Exception {

        Map<String, String> data =
                ExcelUtil.getRowData("TranscriptData", 0);

        // Step a-b: Go to Student Hub
        driver.get("https://me.northeastern.edu");
        pause(3000);
        ScreenshotUtil.take(scenario, "07_my_neu_portal");

        try {
            WebElement hubLink = waitClickable(By.xpath(
                    "//a[contains(@href,'student.me.northeastern')"
                            + " or contains(text(),'Student Hub')]"));
            safeClick(hubLink);
            pause(3000);
        } catch (Exception e) {
            driver.get("https://student.me.northeastern.edu");
            pause(3000);
        }
        switchIfNewTab(2);
        ScreenshotUtil.take(scenario, "07_student_hub");

        // Step c: Click Resources
        ScreenshotUtil.take(scenario, "08_before_resources");
        safeClick(RESOURCES_TAB);
        pause(2000);
        ScreenshotUtil.take(scenario, "08_after_resources");

        // Step d: Click Academics, Classes & Registration
        ScreenshotUtil.take(scenario, "09_before_academics");
        safeClick(ACADEMICS_SECTION);
        pause(2000);
        ScreenshotUtil.take(scenario, "09_after_academics");

        // Step e: Click Unofficial Transcript
        ScreenshotUtil.take(scenario, "10_before_transcript");
        safeClick(UNOFFICIAL_TRANSCRIPT);
        pause(3000);
        switchIfNewTab(2);
        pause(3000);
        ScreenshotUtil.take(scenario, "10_after_transcript");

        // Handle NEU native login if it appears
        LoginPage.handleIfLoginAppears(scenario);
        pause(3000);

        // Check for iframe
        try {
            WebElement iframe = driver.findElement(
                    By.tagName("iframe"));
            driver.switchTo().frame(iframe);
            pause(1000);
        } catch (Exception ignored) {}

        // Step f: Select Transcript Level = Graduate
        ScreenshotUtil.take(scenario, "11_before_level");

        WebElement levelDropdown = driver.findElement(By.xpath(
                "//div[@id='transcriptLevelSelection']//a"
                        + " | //div[@id='transcriptLevelSelection']"
                        + " | //*[@id='transcriptLevelSelection']"
                        + "//span[contains(@class,'select2-arrow')]"));
        safeClick(levelDropdown);
        pause(1500);
        ScreenshotUtil.take(scenario, "11_level_opened");

        WebElement gradOption = waitClickable(By.xpath(
                "//div[contains(@class,'select2-result')]"
                        + "[contains(text(),'Graduate')]"
                        + " | //li[contains(@class,'select2-result')]"
                        + "[contains(text(),'Graduate')]"
                        + " | //span[contains(@class,'select2-result')]"
                        + "[contains(text(),'Graduate')]"
                        + " | //div[contains(@class,'ui-select-choices')]"
                        + "//*[contains(text(),'Graduate')]"));
        safeClick(gradOption);
        pause(3000);
        ScreenshotUtil.take(scenario, "11_after_level");

        // Select Transcript Type = Audit Transcript
        ScreenshotUtil.take(scenario, "12_before_type");

        WebElement typeDropdown = driver.findElement(By.xpath(
                "//div[contains(@xe-section,'transcriptType')]"
                        + "//a[contains(@class,'select2-choice')]"
                        + " | //div[contains(@xe-section,'transcriptType')]"
                        + "//div[contains(@class,'select2-container')]"
                        + " | //div[contains(@xe-section,'transcriptType')]"
                        + "//*[contains(@class,'select2-arrow')]"));
        safeClick(typeDropdown);
        pause(1500);
        ScreenshotUtil.take(scenario, "12_type_opened");

        WebElement auditOption = waitClickable(By.xpath(
                "//div[contains(@class,'select2-result')]"
                        + "[contains(text(),'Audit Transcript')]"
                        + " | //li[contains(@class,'select2-result')]"
                        + "[contains(text(),'Audit Transcript')]"
                        + " | //span[contains(@class,'select2-result')]"
                        + "[contains(text(),'Audit Transcript')]"
                        + " | //div[contains(@class,'ui-select-choices')]"
                        + "//*[contains(text(),'Audit Transcript')]"));
        safeClick(auditOption);
        pause(3000);
        ScreenshotUtil.take(scenario, "12_after_type");

        System.out.println("  Level and Type selected");

        // Wait for transcript to load
        pause(5000);
        ScreenshotUtil.take(scenario, "13_transcript_loaded");

        // Check if Submit button exists
        boolean submitted = false;
        String[] submitXpaths = {
                "//input[@type='submit']",
                "//button[@type='submit']",
                "//button[contains(text(),'Submit')]",
                "//input[contains(@value,'Submit')]"
        };
        for (String xpath : submitXpaths) {
            try {
                WebElement btn = driver.findElement(
                        By.xpath(xpath));
                if (btn.isDisplayed()) {
                    safeClick(btn);
                    submitted = true;
                    System.out.println(
                            "  Clicked Submit button");
                    break;
                }
            } catch (Exception ignored) {}
        }
        if (!submitted) {
            System.out.println(
                    "  No Submit - transcript auto-loaded");
        }

        pause(5000);
        ScreenshotUtil.take(scenario, "13_after_submit");

        // Switch back from iframe
        try { driver.switchTo().defaultContent(); }
        catch (Exception ignored) {}

        // Step g: Click Print button
        ScreenshotUtil.take(scenario, "14_before_print");
        safeClick(By.id("print"));
        pause(5000);
        ScreenshotUtil.take(scenario, "14_after_print");

        // === REAL VERIFICATION ===
        // Check if a PDF file was actually created in downloads
        String dlDir = System.getProperty("user.dir")
                + "/downloads";
        java.io.File dir = new java.io.File(dlDir);
        boolean pdfFound = false;
        String pdfName = "";

        // Wait up to 15 seconds for the PDF to appear
        for (int i = 0; i < 5; i++) {
            java.io.File[] pdfFiles = dir.listFiles(
                    (d, n) -> n.toLowerCase().endsWith(".pdf"));
            if (pdfFiles != null && pdfFiles.length > 0) {
                pdfFound = true;
                pdfName = pdfFiles[pdfFiles.length - 1].getName();
                break;
            }
            pause(3000);
        }

        if (pdfFound) {
            System.out.println(
                    "  VERIFIED: PDF found - " + pdfName);
            return "Transcript downloaded as PDF: " + pdfName;
        } else {
            System.out.println(
                    "  VERIFICATION FAILED: No PDF in downloads");
            return "PDF file NOT found in downloads folder";
        }
    }

    // ======================================================
    // SCENARIO 5: Update the Academic Calendar
    // ======================================================
    public static String updateAcademicCalendar(String scenario)
            throws Exception {

        driver.get("https://student.me.northeastern.edu/");
        pause(3000);
        ScreenshotUtil.take(scenario, "07_student_hub");

        ScreenshotUtil.take(scenario, "08_before_resources");
        safeClick(RESOURCES_TAB);
        pause(2000);
        ScreenshotUtil.take(scenario, "08_after_resources");

        ScreenshotUtil.take(scenario, "09_before_academics");
        safeClick(ACADEMICS_SECTION);
        pause(2000);
        ScreenshotUtil.take(scenario, "09_after_academics");

        ScreenshotUtil.take(scenario, "10_before_ac_link");
        safeClick(ACADEMIC_CALENDAR);
        pause(3000);
        switchIfNewTab(2);
        pause(2000);
        ScreenshotUtil.take(scenario, "10_after_ac_link");

        // Click "Academic Calendar" on registrar page
        ScreenshotUtil.take(scenario, "11_before_ac_box");
        safeClick(AC_CALENDAR_BOX);
        pause(3000);
        switchIfNewTab(2);
        pause(3000);
        ScreenshotUtil.take(scenario, "11_after_ac_box");

        // Scroll down to reach the calendar section
        WebElement body = driver.findElement(
                By.tagName("body"));
        for (int i = 0; i < 5; i++) {
            body.sendKeys(Keys.PAGE_DOWN);
            pause(600);
        }
        pause(2000);
        ScreenshotUtil.take(scenario, "12_scrolled_to_calendar");

        // Exact filter label texts shown in the Calendars panel
        String[] filterLabels = {
                "Semester - All Levels (SEM)",
                "Quarter - CPS Graduate (QTR)",
                "Holidays - United States (USA)",
                "Holidays - Canada (CAN)"
        };

        // Locate all trumba iframes
        List<WebElement> allIframes = driver.findElements(
                By.cssSelector("iframe[id*='trumba']"));
        System.out.println("  Found " + allIframes.size()
                + " trumba iframes");

        if (allIframes.size() < 8) {
            throw new RuntimeException(
                    "Expected at least 8 trumba iframes, "
                            + "found " + allIframes.size());
        }

        // Switch directly into the 8th iframe (index 7)
        WebElement targetIframe = allIframes.get(7);
        driver.switchTo().defaultContent();
        driver.switchTo().frame(targetIframe);
        pause(1000);

        // Scroll up inside the iframe to start from the top,
        // then slowly scroll down until all 4 labels are visible
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, 0);");
        pause(800);

        WebElement found = null;
        String foundLabel = null;

        outer:
        for (int scroll = 0; scroll < 10; scroll++) {
            for (String label : filterLabels) {
                List<WebElement> matches = driver.findElements(
                        By.xpath("//*[normalize-space(text())='"
                                + label + "']"));
                if (!matches.isEmpty()) {
                    found = matches.get(0);
                    foundLabel = label;
                    System.out.println(
                            "  Located filter label: "
                                    + label);
                    break outer;
                }
            }
            js.executeScript("window.scrollBy(0, 150);");
            pause(500);
        }

        if (found == null) {
            throw new RuntimeException(
                    "Could not find any filter label text "
                            + "in iframe 7");
        }

        // Scroll the found label into view so all 4 are visible
        js.executeScript(
                "arguments[0].scrollIntoView("
                        + "{behavior:'smooth', block:'center'});",
                found);
        pause(1000);
        ScreenshotUtil.take(scenario, "12c_filter_labels_visible");

        // Checkboxes have class twCalendarListCheckbox and
        // aria-label containing the label text — find directly
        List<WebElement> cbs = new java.util.ArrayList<>();
        for (String label : filterLabels) {
            List<WebElement> cb = driver.findElements(By.xpath(
                    "//input[contains(@class,'twCalendarListCheckbox')]"
                            + "[contains(@aria-label,'" + label + "')]"));
            if (!cb.isEmpty()) {
                cbs.add(cb.get(0));
                System.out.println(
                        "  Found checkbox for: " + label);
            } else {
                System.out.println(
                        "  No checkbox found for: " + label);
            }
        }

        System.out.println("  Total filter checkboxes found: "
                + cbs.size());

        if (cbs.isEmpty()) {
            throw new RuntimeException(
                    "Could not find any checkboxes next to "
                            + "filter labels in iframe 7");
        }

        // Pick a random checkbox from however many were found
        java.util.Random rand = new java.util.Random();
        int randomIndex = rand.nextInt(cbs.size());
        WebElement selectedCb = cbs.get(randomIndex);
        String selectedLabel = filterLabels[randomIndex];
        System.out.println("  Randomly selected: ["
                + selectedLabel + "]");

        ScreenshotUtil.take(scenario, "13_before_uncheck");

        // Scroll the chosen checkbox into view, then uncheck
        js.executeScript(
                "arguments[0].scrollIntoView("
                        + "{behavior:'smooth', block:'center'});",
                selectedCb);
        pause(800);

        // === REAL VERIFICATION ===
        // Capture state BEFORE click
        boolean stateBefore = selectedCb.isSelected();
        System.out.println("  State BEFORE click: "
                + (stateBefore ? "checked" : "unchecked"));

        if (selectedCb.isSelected()) {
            selectedCb.click();
            System.out.println("  Unchecked: ["
                    + selectedLabel + "]");
        } else {
            selectedCb.click();
            pause(500);
            selectedCb.click();
            System.out.println("  Toggled: ["
                    + selectedLabel + "]");
        }
        pause(2000);

        // Capture state AFTER click
        boolean stateAfter = selectedCb.isSelected();
        System.out.println("  State AFTER click: "
                + (stateAfter ? "checked" : "unchecked"));

        ScreenshotUtil.take(scenario, "13_after_uncheck");

        pause(5000);
        // Switch back to main page
        driver.switchTo().defaultContent();
        ScreenshotUtil.take(scenario, "14_done");

        // Verify the state actually changed
        if (stateBefore != stateAfter) {
            return "Checkbox [" + selectedLabel
                    + "] state changed from "
                    + (stateBefore ? "checked" : "unchecked")
                    + " to "
                    + (stateAfter ? "checked" : "unchecked");
        } else {
            return "Checkbox [" + selectedLabel
                    + "] state did NOT change - still "
                    + (stateAfter ? "checked" : "unchecked");
        }
    }
}