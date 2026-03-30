package com.automation.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportUtil - Generates HTML test report.
 * Columns: Test Scenario Name | Actual | Expected | Pass/Fail
 * as required by the assignment.
 * Report saved to: reports/TestReport.html
 */
public class ReportUtil {

    private static List<String[]> results = new ArrayList<>();

    public static void init() {
        results = new ArrayList<>();
    }

    /** Add a test result entry */
    public static void addResult(String scenario,
                                 String actual, String expected, String status) {
        results.add(new String[]{
                scenario, actual, expected, status});
        System.out.println(
                "  [Report] " + scenario + " -> " + status);
    }

    /** Generate the full HTML report file */
    public static void generateReport() {
        String path = System.getProperty("user.dir")
                + "/reports/TestReport.html";
        String ts = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        long pass = results.stream()
                .filter(r -> "PASS".equals(r[3])).count();
        long fail = results.stream()
                .filter(r -> "FAIL".equals(r[3])).count();

        StringBuilder h = new StringBuilder();
        h.append("<!DOCTYPE html>\n<html><head>\n");
        h.append("<title>INFO6255 Test Report</title>\n");
        h.append("<style>\n");
        h.append("*{box-sizing:border-box}\n");
        h.append("body{font-family:'Segoe UI',Arial,");
        h.append("sans-serif;margin:0;padding:20px;");
        h.append("background:#f0f2f5}\n");
        h.append(".c{max-width:1100px;margin:0 auto}\n");
        h.append("h1{color:#c8102e;text-align:center;");
        h.append("margin-bottom:5px}\n");
        h.append("h2{color:#333;text-align:center;");
        h.append("font-weight:normal;font-size:16px}\n");
        h.append(".s{display:flex;justify-content:center;");
        h.append("gap:20px;margin:20px 0}\n");
        h.append(".sb{padding:15px 30px;border-radius:8px;");
        h.append("color:#fff;font-size:18px;font-weight:bold}\n");
        h.append(".bp{background:#28a745}\n");
        h.append(".bf{background:#dc3545}\n");
        h.append(".bt{background:#007bff}\n");
        h.append("table{width:100%;border-collapse:collapse;");
        h.append("background:#fff;box-shadow:0 2px 8px ");
        h.append("rgba(0,0,0,.1);border-radius:8px;");
        h.append("overflow:hidden}\n");
        h.append("th{background:#c8102e;color:#fff;");
        h.append("padding:14px 16px;text-align:left}\n");
        h.append("td{padding:12px 16px;border-bottom:");
        h.append("1px solid #eee;font-size:13px}\n");
        h.append("tr:hover{background:#f8f9fa}\n");
        h.append(".p{color:#28a745;font-weight:bold}\n");
        h.append(".f{color:#dc3545;font-weight:bold}\n");
        h.append(".ft{text-align:center;margin-top:20px;");
        h.append("color:#666;font-size:12px}\n");
        h.append("</style></head><body>\n<div class='c'>\n");

        h.append("<h1>INFO6255 Selenium Test Report</h1>\n");
        h.append("<h2>Spring 2026 | Northeastern University");
        h.append(" | ").append(ts).append("</h2>\n");

        h.append("<div class='s'>\n");
        h.append("<div class='sb bt'>Total: ");
        h.append(results.size()).append("</div>\n");
        h.append("<div class='sb bp'>Passed: ");
        h.append(pass).append("</div>\n");
        h.append("<div class='sb bf'>Failed: ");
        h.append(fail).append("</div>\n");
        h.append("</div>\n");

        h.append("<table><tr>");
        h.append("<th>#</th>");
        h.append("<th>Test Scenario Name</th>");
        h.append("<th>Actual</th>");
        h.append("<th>Expected</th>");
        h.append("<th>Pass/Fail</th></tr>\n");

        for (int i = 0; i < results.size(); i++) {
            String[] r = results.get(i);
            String cls = "PASS".equals(r[3]) ? "p" : "f";
            h.append("<tr>");
            h.append("<td>").append(i+1).append("</td>");
            h.append("<td>").append(r[0]).append("</td>");
            h.append("<td>").append(r[1]).append("</td>");
            h.append("<td>").append(r[2]).append("</td>");
            h.append("<td class='").append(cls).append("'>");
            h.append(r[3]).append("</td></tr>\n");
        }

        h.append("</table>\n");
        h.append("<p class='ft'>INFO6255 Spring 2026 - ");
        h.append("Automated Test Report</p>\n");
        h.append("</div></body></html>");

        try {
            new java.io.File(
                    System.getProperty("user.dir") + "/reports")
                    .mkdirs();
            FileWriter fw = new FileWriter(path);
            fw.write(h.toString());
            fw.close();
            System.out.println(
                    "Report generated: " + path);
        } catch (IOException e) {
            System.err.println(
                    "Report failed: " + e.getMessage());
        }
    }
}