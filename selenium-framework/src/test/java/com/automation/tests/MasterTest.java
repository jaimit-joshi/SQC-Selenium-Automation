package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.*;
import com.automation.utils.ReportUtil;
import com.automation.utils.ScreenshotUtil;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.*;
import java.io.File;

/**
 * MasterTest - Orchestrates all 5 test scenarios.
 */
public class MasterTest extends BaseTest {

    @BeforeSuite
    public void initSuite() {
        setUp();
        new File(System.getProperty("user.dir")
                + "/Screenshots").mkdirs();
        new File(System.getProperty("user.dir")
                + "/downloads").mkdirs();
        new File(System.getProperty("user.dir")
                + "/reports").mkdirs();
        ReportUtil.init();
        System.out.println("\n===== Suite Started =====\n");
    }

    @Test(priority = 1, description =
            "Scenario 1: Download the latest transcript")
    public void scenario1_DownloadTranscript() {
        String name = "Scenario1_DownloadTranscript";
        String expected = "Transcript downloaded as PDF";
        try {
            System.out.println("\n>>> " + name);
            LoginPage.loginToNEU(
                    "https://me.northeastern.edu", name);
            String actual =
                    StudentHubPage.downloadTranscript(name);

            // Real assertion: verify PDF was actually found
            boolean passed = actual.toLowerCase()
                    .contains("downloaded as pdf");
            ReportUtil.addResult(
                    name, actual, expected,
                    passed ? "PASS" : "FAIL");

            Assert.assertTrue(passed,
                    "Expected PDF download but got: "
                            + actual);
            System.out.println(">>> PASSED\n");
        } catch (Exception | AssertionError e) {
            ScreenshotUtil.take(name, "FAILURE");
            ReportUtil.addResult(
                    name, "Failed: " + e.getMessage(),
                    expected, "FAIL");
            System.out.println(
                    ">>> FAILED: " + e.getMessage() + "\n");
            Assert.fail(e.getMessage());
        }
    }

    @Test(priority = 2, description =
            "Scenario 2: Add two Event tasks on Canvas")
    public void scenario2_AddCalendarEvents() {
        String name = "Scenario2_AddCalendarEvents";
        String expected =
                "2 of 2 events verified on Canvas calendar";
        try {
            System.out.println("\n>>> " + name);
            LoginPage.loginToCanvas(name);
            String actual =
                    CanvasPage.addCalendarEvents(name);

            // Real assertion: verify events were found
            boolean passed = actual.startsWith("2 of 2");
            ReportUtil.addResult(
                    name, actual, expected,
                    passed ? "PASS" : "FAIL");

            Assert.assertTrue(passed,
                    "Expected 2 events verified but got: "
                            + actual);
            System.out.println(">>> PASSED\n");
        } catch (Exception | AssertionError e) {
            ScreenshotUtil.take(name, "FAILURE");
            ReportUtil.addResult(
                    name, "Failed: " + e.getMessage(),
                    expected, "FAIL");
            System.out.println(
                    ">>> FAILED: " + e.getMessage() + "\n");
            Assert.fail(e.getMessage());
        }
    }

    @Test(priority = 3, description =
            "Scenario 3: Reserve a spot in Snell Library")
    public void scenario3_ReserveLibrarySpot() {
        String name = "Scenario3_ReserveLibrary";
        String expected =
                "Library filters applied and verified";
        try {
            System.out.println("\n>>> " + name);
            String actual =
                    LibraryPage.reserveLibrarySpot(name);

            // Real assertion: verify no MISMATCH in result
            boolean passed =
                    !actual.contains("MISMATCH")
                            && !actual.contains("Could not read");
            ReportUtil.addResult(
                    name, actual, expected,
                    passed ? "PASS" : "FAIL");

            Assert.assertTrue(passed,
                    "Filter verification failed: " + actual);
            System.out.println(">>> PASSED\n");
        } catch (Exception | AssertionError e) {
            ScreenshotUtil.take(name, "FAILURE");
            ReportUtil.addResult(
                    name, "Failed: " + e.getMessage(),
                    expected, "FAIL");
            System.out.println(
                    ">>> FAILED: " + e.getMessage() + "\n");
            Assert.fail(e.getMessage());
        }
    }

    @Test(priority = 4, description =
            "Scenario 4: Download Dataset (NEGATIVE TEST)")
    public void scenario4_DownloadDataset_NEGATIVE() {
        String name = "Scenario4_DownloadDataset_NEGATIVE";
        try {
            System.out.println("\n>>> " + name);
            System.out.println(
                    ">>> NEGATIVE TEST - Expected to FAIL");
            DRSPage.downloadDataset(name);

            ReportUtil.addResult(name,
                    "Dataset not downloaded",
                    "Dataset should NOT be downloaded",
                    "PASS");
            System.out.println(">>> PASSED\n");

        } catch (AssertionError e) {
            ScreenshotUtil.take(name, "FAILURE");
            ReportUtil.addResult(name,
                    "Dataset downloaded successfully",
                    "Dataset should NOT be downloaded",
                    "FAIL");
            System.out.println(
                    ">>> FAILED: Dataset downloaded\n");
            throw e;

        } catch (Exception e) {
            ScreenshotUtil.take(name, "FAILURE");
            ReportUtil.addResult(name,
                    "Error: " + e.getMessage(),
                    "Dataset should NOT be downloaded",
                    "FAIL");
            System.out.println(
                    ">>> FAILED: " + e.getMessage() + "\n");
            throw new AssertionError(e.getMessage());

        } finally {
            try {
                driver.getTitle();
            } catch (Exception e) {
                System.out.println(
                        ">>> Browser crashed, restarting...");
                try { driver.quit(); }
                catch (Exception ignored) {}
                setUp();
            }
        }
    }

    @Test(priority = 5, alwaysRun = true, description =
            "Scenario 5: Update the Academic Calendar")
    public void scenario5_UpdateAcademicCalendar() {
        String name = "Scenario5_UpdateAcademicCalendar";
        String expected =
                "Academic Calendar checkbox state changed";
        try {
            System.out.println("\n>>> " + name);
            LoginPage.loginToNEU(
                    "https://student.me.northeastern.edu", name);
            String actual =
                    StudentHubPage.updateAcademicCalendar(name);

            // Real assertion: verify checkbox state changed
            boolean passed =
                    actual.contains("state changed from");
            ReportUtil.addResult(
                    name, actual, expected,
                    passed ? "PASS" : "FAIL");

            Assert.assertTrue(passed,
                    "Checkbox state did not change: "
                            + actual);
            System.out.println(">>> PASSED\n");
        } catch (Exception | AssertionError e) {
            ScreenshotUtil.take(name, "FAILURE");
            ReportUtil.addResult(
                    name, "Failed: " + e.getMessage(),
                    expected, "FAIL");
            System.out.println(
                    ">>> FAILED: " + e.getMessage() + "\n");
            Assert.fail(e.getMessage());
        }
    }

    @AfterSuite
    public void cleanupSuite() {
        ReportUtil.generateReport();
        System.out.println(
                "\n===== Report: reports/TestReport.html =====");
        tearDown();
        System.out.println("===== Suite Completed =====");
    }
}