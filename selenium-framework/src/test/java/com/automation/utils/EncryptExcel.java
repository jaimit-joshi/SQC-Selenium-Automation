package com.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * EncryptExcel - ONE-TIME utility to encrypt TestData.xlsx.
 *
 * BEFORE running:
 *   1. Set env variable:
 *      export ENCRYPTION_KEY="Your32CharacterSecretKeyHere!!"
 *   2. Make sure TestData.xlsx has your PLAIN data
 *
 * HOW TO RUN:
 *   Right-click this file -> Run As -> Java Application
 *   OR from terminal:
 *   mvn exec:java -Dexec.mainClass=
 *       "com.automation.utils.EncryptExcel"
 *
 * WHAT IT DOES:
 *   - Reads every cell in every sheet
 *   - Encrypts each value with AES-256
 *   - Saves encrypted version as TestData.xlsx
 *   - Backs up original as TestData_BACKUP.xlsx
 *
 * AFTER running:
 *   - TestData.xlsx = encrypted (all cells are gibberish)
 *   - TestData_BACKUP.xlsx = your original plain data
 *   - DELETE TestData_BACKUP.xlsx before submitting!
 */
public class EncryptExcel {

    public static void main(String[] args) {
        String dir = System.getProperty("user.dir");
        String inputPath = dir + "/TestData.xlsx";
        String backupPath =
                dir + "/TestData_BACKUP.xlsx";
        String outputPath = dir + "/TestData.xlsx";

        // Verify encryption key is available
        // (CryptoUtil will read from config.properties)
        String key = null;
        try {
            java.util.Properties props =
                    new java.util.Properties();
            props.load(new java.io.FileInputStream(
                    dir + "/config.properties"));
            key = props.getProperty("ENCRYPTION_KEY");
        } catch (Exception ignored) {}

        if (key == null) {
            key = System.getProperty("ENCRYPTION_KEY",
                    System.getenv("ENCRYPTION_KEY"));
        }

        if (key == null || key.length() != 32) {
            System.err.println(
                    "ERROR: ENCRYPTION_KEY not found "
                            + "or not 32 characters!");
            System.err.println(
                    "  Create config.properties in "
                            + "project root with:");
            System.err.println(
                    "  ENCRYPTION_KEY="
                            + "NEUSeleniumFramework2026Key!!!!!");
            System.exit(1);
        }

        System.out.println(
                "=== Excel Encryption Tool ===\n");

        try {
            // Step 1: Create backup of original
            System.out.println(
                    "1. Creating backup: "
                            + backupPath);
            copyFile(inputPath, backupPath);

            // Step 2: Read and encrypt
            System.out.println(
                    "2. Reading and encrypting...\n");

            FileInputStream fis =
                    new FileInputStream(inputPath);
            Workbook wb = new XSSFWorkbook(fis);
            fis.close();

            int totalCells = 0;

            for (int s = 0;
                 s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                String sheetName = sheet.getSheetName();
                int sheetCells = 0;

                System.out.println(
                        "   Sheet: [" + sheetName + "]");

                for (int r = 0;
                     r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;

                    for (int c = 0;
                         c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null) continue;

                        String plainValue =
                                getCellAsString(cell);

                        if (plainValue.isEmpty()) continue;

                        // Skip if already encrypted
                        if (CryptoUtil
                                .isEncrypted(plainValue)) {
                            System.out.println(
                                    "   SKIP (already encrypted): "
                                            + "Row " + r
                                            + ", Col " + c);
                            continue;
                        }

                        // Encrypt and write back
                        String encrypted =
                                CryptoUtil.encrypt(
                                        plainValue);
                        cell.setCellValue(encrypted);

                        sheetCells++;
                        totalCells++;

                        // Show progress (mask actual data)
                        String masked =
                                plainValue.length() > 3
                                        ? plainValue.substring(0, 3)
                                        + "***"
                                        : "***";
                        System.out.println(
                                "   Row " + r + ", Col " + c
                                        + ": " + masked
                                        + " -> "
                                        + encrypted
                                        .substring(0, 20)
                                        + "...");
                    }
                }
                System.out.println(
                        "   [" + sheetName + "] "
                                + sheetCells
                                + " cells encrypted\n");
            }

            // Step 3: Save encrypted file
            System.out.println(
                    "3. Saving encrypted file...");
            FileOutputStream fos =
                    new FileOutputStream(outputPath);
            wb.write(fos);
            fos.close();
            wb.close();

            System.out.println(
                    "\n=== DONE ===");
            System.out.println(
                    "Total cells encrypted: "
                            + totalCells);
            System.out.println(
                    "Encrypted file: " + outputPath);
            System.out.println(
                    "Backup file: " + backupPath);
            System.out.println(
                    "\nIMPORTANT: Delete "
                            + "TestData_BACKUP.xlsx "
                            + "before submitting!");

        } catch (Exception e) {
            System.err.println(
                    "FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convert any cell type to String for encryption.
     */
    private static String getCellAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    double numVal =
                            cell.getNumericCellValue();
                    if (numVal < 1.0) {
                        return new SimpleDateFormat(
                                "h:mm a").format(date);
                    }
                    return new SimpleDateFormat(
                            "MMM dd, yyyy").format(date);
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);

            case BOOLEAN:
                return String.valueOf(
                        cell.getBooleanCellValue());

            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        return String.valueOf(
                                cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return "";
                    }
                }

            default:
                return "";
        }
    }

    /**
     * Simple file copy utility.
     */
    private static void copyFile(
            String src, String dest) throws Exception {
        java.nio.file.Files.copy(
                java.nio.file.Paths.get(src),
                java.nio.file.Paths.get(dest),
                java.nio.file.StandardCopyOption
                        .REPLACE_EXISTING);
    }
}