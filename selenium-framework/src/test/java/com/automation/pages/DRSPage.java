package com.automation.pages;

import com.automation.base.BaseTest;
import com.automation.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import org.testng.Assert;
import java.io.File;

/**
 * DRSPage - Scenario 4: Download a Dataset (NEGATIVE TEST).
 */
public class DRSPage extends BaseTest {

    private static final By DRS_LINK = By.cssSelector(
            "a[aria-label*='digital repository service']");
    private static final By DATASETS_BTN = By.cssSelector(
            "a[href='/datasets']");
    private static final By ZIP_FILE_BTN = By.cssSelector(
            "a[title='Zip File']");

    public static void downloadDataset(String scenario)
            throws Exception {

        // Clean downloads folder
        String dlDir = System.getProperty("user.dir")
                + "/downloads";
        File dir = new File(dlDir);
        dir.mkdirs();
        File[] oldZips = dir.listFiles(
                (d, n) -> n.endsWith(".zip"));
        if (oldZips != null) {
            for (File f : oldZips) { f.delete(); }
        }

        // Step a: Open Scholar OneSearch
        driver.get(
                "https://onesearch.library.northeastern.edu"
                        + "/discovery/search?vid=01NEU_INST:NU&lang=en");
        pause(2000);
        ScreenshotUtil.take(scenario,
                "01_onesearch_page");

        // Click Digital Repository Service
        ScreenshotUtil.take(scenario,
                "02_before_drs_click");
        try {
            safeClick(DRS_LINK);
        } catch (Exception e) {
            driver.get(
                    "https://repository.library"
                            + ".northeastern.edu/");
        }
        pause(2000);
        switchIfNewTab(2);
        ScreenshotUtil.take(scenario,
                "02_after_drs_page");

        // Step b: Click Datasets under Featured Content
        ScreenshotUtil.take(scenario,
                "03_before_datasets");
        safeClick(DATASETS_BTN);
        pause(2000);
        ScreenshotUtil.take(scenario,
                "03_after_datasets");

        // Step c: Click first "Zip File" button
        ScreenshotUtil.take(scenario,
                "04_before_zip_click");
        safeClick(ZIP_FILE_BTN);
        System.out.println("  Clicked Zip File button");

        // Wait up to 120s, checking every 60s
        boolean downloaded = false;
        int maxChecks = 2;

        for (int i = 1; i <= maxChecks; i++) {
            System.out.println(
                    "  Waiting 60 seconds (check "
                            + i + "/" + maxChecks + ")...");
            pause(60000);

            File[] zipFiles = dir.listFiles(
                    (d, n) -> n.endsWith(".zip"));
            downloaded = zipFiles != null
                    && zipFiles.length > 0;

            if (downloaded) {
                System.out.println("  Downloaded: "
                        + zipFiles[0].getName()
                        + " (after " + (i * 60) + "s)");
                break;
            } else {
                System.out.println(
                        "  No zip found after "
                                + (i * 60) + "s");
            }
        }

        ScreenshotUtil.take(scenario,
                "04_after_zip_click");
        ScreenshotUtil.take(scenario,
                "05_final_state");

        // Assert download should NOT have happened
        Assert.assertFalse(downloaded,
                "Dataset was downloaded but should NOT "
                        + "have been");
    }
}