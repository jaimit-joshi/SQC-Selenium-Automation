package com.automation.utils;

import com.automation.base.BaseTest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * ScreenshotUtil - Captures before/after screenshots per step.
 * Saves to: Screenshots/{ScenarioName}/{stepName}.png
 * Uses YOUR existing Screenshots/ folder at project root.
 */
public class ScreenshotUtil extends BaseTest {

    // Uses YOUR existing Screenshots folder
    private static final String BASE_DIR =
            System.getProperty("user.dir") + "/Screenshots";

    /**
     * Capture screenshot and save to scenario subfolder.
     * @param scenarioName subfolder name
     * @param stepName     file name (without .png)
     */
    public static void take(String scenarioName,
                            String stepName) {
        try {
            String dirPath = BASE_DIR + "/" + scenarioName;
            new File(dirPath).mkdirs();

            File src = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);

            String dest = dirPath + "/" + stepName + ".png";
            Files.copy(src.toPath(), Paths.get(dest),
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("  Screenshot: " + dest);
        } catch (Exception e) {
            System.err.println("  Screenshot failed ["
                    + stepName + "]: " + e.getMessage());
        }
    }
}