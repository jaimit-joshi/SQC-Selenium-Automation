package com.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ExcelUtil - Data-Driven framework utility.
 * Reads TestData.xlsx from project root.
 * Handles dates AND times from Google Sheets.
 */
public class ExcelUtil {

    private static final String FILE_PATH =
            System.getProperty("user.dir") + "/TestData.xlsx";

    public static Map<String, String> getRowData(
            String sheetName, int rowIndex) throws Exception {

        Map<String, String> data = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException(
                        "Sheet '" + sheetName + "' not found in "
                                + FILE_PATH);
            }

            Row headerRow = sheet.getRow(0);
            Row dataRow = sheet.getRow(rowIndex + 1);

            if (headerRow == null || dataRow == null) {
                throw new RuntimeException(
                        "Row " + rowIndex + " not found in '"
                                + sheetName + "'");
            }

            for (int i = 0;
                 i < headerRow.getLastCellNum(); i++) {
                String key = getCellString(
                        headerRow.getCell(i));
                String val = getCellString(
                        dataRow.getCell(i));
                if (!key.isEmpty()) {
                    data.put(key, val);
                }
            }
        }
        return data;
    }

    public static List<Map<String, String>> getAllRows(
            String sheetName) throws Exception {

        List<Map<String, String>> allData = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException(
                        "Sheet '" + sheetName + "' not found");
            }

            Row headerRow = sheet.getRow(0);

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row dataRow = sheet.getRow(r);
                if (dataRow == null) continue;

                Map<String, String> row = new LinkedHashMap<>();
                boolean hasData = false;

                for (int c = 0;
                     c < headerRow.getLastCellNum(); c++) {
                    String key = getCellString(
                            headerRow.getCell(c));
                    String val = getCellString(
                            dataRow.getCell(c));
                    if (!key.isEmpty()) {
                        row.put(key, val);
                        if (!val.isEmpty()) hasData = true;
                    }
                }
                if (hasData) allData.add(row);
            }
        }
        return allData;
    }

    /**
     * Convert any cell to String.
     * Handles: dates -> "MMM dd, yyyy"
     *          times -> "h:mm a" (e.g. "10:00 AM")
     *          numbers -> no trailing .0
     */
    private static String getCellString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    double numVal = cell.getNumericCellValue();

                    // Time-only values are < 1.0 in Excel
                    // e.g. 10:00 AM = 0.4166...
                    if (numVal < 1.0) {
                        SimpleDateFormat timeFmt =
                                new SimpleDateFormat("h:mm a");
                        return timeFmt.format(date);
                    }

                    // Full date
                    SimpleDateFormat dateFmt =
                            new SimpleDateFormat("MMM dd, yyyy");
                    return dateFmt.format(date);
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
                        double fNum =
                                cell.getNumericCellValue();
                        if (DateUtil.isCellDateFormatted(cell)) {
                            Date date =
                                    cell.getDateCellValue();
                            if (fNum < 1.0) {
                                return new SimpleDateFormat(
                                        "h:mm a").format(date);
                            }
                            return new SimpleDateFormat(
                                    "MMM dd, yyyy").format(date);
                        }
                        if (fNum == Math.floor(fNum)) {
                            return String.valueOf((long) fNum);
                        }
                        return String.valueOf(fNum);
                    } catch (Exception e2) {
                        return "";
                    }
                }

            case BLANK:
            default:
                return "";
        }
    }
}