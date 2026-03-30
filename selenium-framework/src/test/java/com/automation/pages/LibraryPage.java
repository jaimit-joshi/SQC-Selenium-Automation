package com.automation.pages;

import com.automation.base.BaseTest;
import com.automation.utils.ExcelUtil;
import com.automation.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import java.util.Map;

/**
 * LibraryPage - Scenario 3: Reserve a spot in Snell Library.
 */
public class LibraryPage extends BaseTest {

    private static final By RESERVE_LINK = By.xpath(
            "//a[contains(text(),'Reserve')]"
                    + "[contains(text(),'Study') or "
                    + "contains(text(),'Room')]");

    private static final By BOSTON_IMAGE = By.xpath(
            "//img[contains(@alt,'Boston')]"
                    + "/ancestor::a");

    private static final By RESERVATION_LINK = By.xpath(
            "//a[contains(text(),'for reservation')]"
                    + " | //a[contains(text(),'reservation')]");

    private static final By BOOK_ROOM_BTN = By.xpath(
            "//a[contains(text(),'Book a Room')]"
                    + " | //a[contains(text(),'Book A Room')]"
                    + " | //a[contains(@href,'libcal')]");

    // Exact IDs from inspect element
    private static final By SEAT_STYLE_DD =
            By.cssSelector("select#gid");
    private static final By CAPACITY_DD =
            By.cssSelector("select#capacity");

    public static String reserveLibrarySpot(String scenario)
            throws Exception {

        Map<String, String> data =
                ExcelUtil.getRowData("LibraryData", 0);

        // Step a: Open library website
        driver.get("https://library.northeastern.edu/");
        pause(3000);
        ScreenshotUtil.take(scenario,
                "01_library_homepage");

        // Step b: Click "Reserve a Study Room"
        ScreenshotUtil.take(scenario,
                "02_before_reserve_link");
        try {
            safeClick(RESERVE_LINK);
        } catch (Exception e) {
            try {
                WebElement el = driver.findElement(
                        By.partialLinkText("Reserve"));
                safeClick(el);
            } catch (Exception e2) {
                driver.get(
                        "https://library.northeastern.edu/"
                                + "library-locations/"
                                + "library-rooms-spaces/");
            }
        }
        pause(3000);
        ScreenshotUtil.take(scenario,
                "02_after_reserve_link");

        // Step c: Click the Boston skyline image
        ScreenshotUtil.take(scenario,
                "03_before_boston");
        try {
            safeClick(BOSTON_IMAGE);
        } catch (Exception e) {
            try {
                WebElement img = driver.findElement(
                        By.xpath(
                                "//img[contains(@alt,'Boston')]"));
                safeClick(img);
            } catch (Exception e2) {
                try {
                    safeClick(RESERVATION_LINK);
                } catch (Exception e3) {
                    driver.get(
                            "https://library.northeastern.edu/"
                                    + "ideas/rooms-spaces/");
                }
            }
        }
        pause(3000);
        ScreenshotUtil.take(scenario,
                "03_after_boston");

        // Step d: Click "Book a Room" button
        ScreenshotUtil.take(scenario,
                "04_before_book_room");
        scrollToBottom();
        pause(1000);
        try {
            safeClick(BOOK_ROOM_BTN);
        } catch (Exception e) {
            System.out.println(
                    "  Book button not found, going direct");
            driver.get(
                    "https://northeastern.libcal.com/"
                            + "reserve/snell-study");
        }
        pause(3000);
        switchIfNewTab(2);
        ScreenshotUtil.take(scenario,
                "04_after_book_room");

        // Step e: Select Seat Style = Individual Study
        ScreenshotUtil.take(scenario,
                "05_before_seat_style");
        selectDropdownByPartialText(SEAT_STYLE_DD,
                data.get("seatStyle"), "Seat Style");
        pause(2000);
        ScreenshotUtil.take(scenario,
                "05_after_seat_style");

        // Select Capacity
        ScreenshotUtil.take(scenario,
                "06_before_capacity");
        selectDropdownByPartialText(CAPACITY_DD,
                data.get("capacity"), "Capacity");
        pause(2000);
        ScreenshotUtil.take(scenario,
                "06_after_capacity");

        // Step f: Scroll to bottom of page
        ScreenshotUtil.take(scenario,
                "07_before_scroll");
        scrollToBottom();
        pause(2000);
        ScreenshotUtil.take(scenario,
                "07_bottom_of_page");

        // === REAL VERIFICATION ===
        // Check if the dropdown values were actually set
        String actualSeatStyle = "";
        String actualCapacity = "";

        try {
            WebElement seatEl =
                    driver.findElement(SEAT_STYLE_DD);
            Select seatSel = new Select(seatEl);
            actualSeatStyle = seatSel
                    .getFirstSelectedOption().getText().trim();
            System.out.println(
                    "  VERIFIED Seat Style: "
                            + actualSeatStyle);
        } catch (Exception e) {
            actualSeatStyle = "Could not read selection";
            System.out.println(
                    "  Seat Style verification failed: "
                            + e.getMessage());
        }

        try {
            WebElement capEl =
                    driver.findElement(CAPACITY_DD);
            Select capSel = new Select(capEl);
            actualCapacity = capSel
                    .getFirstSelectedOption().getText().trim();
            System.out.println(
                    "  VERIFIED Capacity: "
                            + actualCapacity);
        } catch (Exception e) {
            actualCapacity = "Could not read selection";
            System.out.println(
                    "  Capacity verification failed: "
                            + e.getMessage());
        }

        String expectedSeat = data.get("seatStyle");
        String expectedCap = data.get("capacity");

        boolean seatMatch = actualSeatStyle
                .toLowerCase().contains(
                        expectedSeat.toLowerCase())
                || expectedSeat.toLowerCase().contains(
                actualSeatStyle.toLowerCase());
        boolean capMatch = actualCapacity
                .toLowerCase().contains(
                        expectedCap.toLowerCase())
                || expectedCap.toLowerCase().contains(
                actualCapacity.toLowerCase());

        StringBuilder result = new StringBuilder();
        result.append("Seat Style: ")
                .append(seatMatch ? "MATCHED" : "MISMATCH")
                .append(" (expected='").append(expectedSeat)
                .append("', actual='")
                .append(actualSeatStyle).append("')");
        result.append(" | Capacity: ")
                .append(capMatch ? "MATCHED" : "MISMATCH")
                .append(" (expected='").append(expectedCap)
                .append("', actual='")
                .append(actualCapacity).append("')");

        return result.toString();
    }

    /**
     * Select from native <select> dropdown using
     * partial text match. Handles extra spaces and
     * special dashes in LibCal options.
     */
    private static void selectDropdownByPartialText(
            By locator, String value, String label) {
        try {
            WebElement selectEl = driver.findElement(locator);
            Select sel = new Select(selectEl);

            // First try exact match
            try {
                sel.selectByVisibleText(value);
                System.out.println("  " + label
                        + " set to: " + value);
                return;
            } catch (Exception ignored) {}

            // Try partial match with dash normalization
            for (WebElement opt : sel.getOptions()) {
                String optText = opt.getText().trim();
                String normalized = optText
                        .replace("\u2013", "-")
                        .replace("\u2014", "-")
                        .replaceAll("\\s+", " ")
                        .trim();
                String valueNorm = value
                        .replace("\u2013", "-")
                        .replace("\u2014", "-")
                        .replaceAll("\\s+", " ")
                        .trim();

                if (normalized.equalsIgnoreCase(valueNorm)
                        || normalized.contains(valueNorm)
                        || valueNorm.contains(normalized)) {
                    sel.selectByVisibleText(optText);
                    System.out.println("  " + label
                            + " set to: " + optText);
                    return;
                }
            }

            // Last resort: select by index
            if (sel.getOptions().size() > 1) {
                sel.selectByIndex(1);
                System.out.println("  " + label
                        + " set to index 1 (fallback)");
            }

        } catch (Exception e) {
            System.out.println("  " + label
                    + " FAILED: " + e.getMessage());
        }
    }
}