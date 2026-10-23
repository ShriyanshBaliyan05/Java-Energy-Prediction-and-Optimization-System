package com.energy.service;

import com.energy.database.PredictionDAO;
import com.energy.model.DailyConsumption;
import com.energy.model.Prediction;
import com.energy.utils.Constants;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportService {

    public static class ReportData {
        public String title;
        public String subtitle;
        public List<String> summaryLines = new ArrayList<>();
        public String[] columns;
        public List<String[]> rows = new ArrayList<>();
    }
    public ReportData buildConsumptionReport(String fromDate, String toDate) {
        AnalyticsService analyticsService = new AnalyticsService();
        AnalyticsService.Snapshot snap = analyticsService.buildSnapshot(fromDate, toDate);

        ReportData data = new ReportData();
        data.title = "Energy Consumption Summary Report";
        data.subtitle = "Period: " + fromDate + " to " + toDate;

        data.summaryLines.add("Total Records Logged: " + snap.recordCount);
        data.summaryLines.add("Days With Logged Data: " + snap.dayCount);
        data.summaryLines.add(String.format("Total Consumption: %.2f kWh", snap.totalKwh));
        data.summaryLines.add(String.format("Total Energy Cost: %s%.2f", Constants.CURRENCY_SYMBOL, snap.totalCost));
        data.summaryLines.add(String.format("Average Daily Consumption: %.2f kWh", snap.avgDailyKwh));
        if (snap.peakDay != null) {
            data.summaryLines.add(String.format("Peak Consumption Day: %s (%.2f kWh)", snap.peakDay, snap.peakDayKwh));
        }

        data.columns = new String[] { "Date", "Consumption (kWh)" };
        if (snap.dailySeries != null) {
            for (DailyConsumption dc : snap.dailySeries) {
                data.rows.add(new String[] { dc.getDate(), String.format("%.3f", dc.getTotalKwh()) });
            }
        }
        return data;
    }

    public ReportData buildCategoryBreakdownReport(String fromDate, String toDate) {
        AnalyticsService analyticsService = new AnalyticsService();
        AnalyticsService.Snapshot snap = analyticsService.buildSnapshot(fromDate, toDate);

        ReportData data = new ReportData();
        data.title = "Category Breakdown Report";
        data.subtitle = "Period: " + fromDate + " to " + toDate;
        data.summaryLines.add(String.format("Total Consumption: %.2f kWh", snap.totalKwh));

        if (snap.topLogged != null && !snap.topLogged.isEmpty()) {
            AnalyticsService.ApplianceRank top = snap.topLogged.get(0);
            data.summaryLines.add(String.format("Top Logged Consumer: %s (%.2f kWh)",
                    top.name, top.kwh));
        }
        if (snap.topProjected != null && !snap.topProjected.isEmpty()) {
            AnalyticsService.ApplianceRank top = snap.topProjected.get(0);
            data.summaryLines.add(String.format("Top Projected Consumer: %s (%.2f kWh/mo)",
                    top.name, top.kwh));
        }

        data.columns = new String[] { "Category", "Consumption (kWh)", "Share (%)" };
        if (snap.categoryShares != null) {
            for (AnalyticsService.CategoryShare cc : snap.categoryShares) {
                data.rows.add(new String[] {
                        cc.category,
                        String.format("%.3f", cc.kwh),
                        String.format("%.1f", cc.percent)
                });
            }
        }
        return data;
    }

    public ReportData buildBillReport(String fromDate, String toDate) {
        AnalyticsService analyticsService = new AnalyticsService();
        AnalyticsService.Snapshot snap = analyticsService.buildSnapshot(fromDate, toDate);

        BillCalculationService billService = new BillCalculationService();
        BillCalculationService.BillBreakdown bill = billService.calculateBillUsingSavedDefaults(snap.totalKwh, 0.0);

        ReportData data = new ReportData();
        data.title = "Electricity Bill Estimate Report";
        data.subtitle = "Based on consumption from " + fromDate + " to " + toDate + " using saved tariff/charge defaults";

        data.summaryLines.add(String.format("ESTIMATED BILL: %s%.2f", Constants.CURRENCY_SYMBOL, bill.estimatedBill));

        data.columns = new String[] { "Line Item", "Amount" };
        data.rows.add(new String[] { "Total Consumption (kWh)", String.format("%.2f", bill.totalKwh) });
        data.rows.add(new String[] { "Tariff (per kWh)", Constants.CURRENCY_SYMBOL + String.format("%.2f", bill.tariff) });
        data.rows.add(new String[] { "Energy Cost", Constants.CURRENCY_SYMBOL + String.format("%.2f", bill.energyCost) });
        data.rows.add(new String[] { "Fixed Charges", Constants.CURRENCY_SYMBOL + String.format("%.2f", bill.fixedCharges) });
        data.rows.add(new String[] { "Additional Charges", Constants.CURRENCY_SYMBOL + String.format("%.2f", bill.additionalCharges) });
        data.rows.add(new String[] { "Subtotal", Constants.CURRENCY_SYMBOL + String.format("%.2f", bill.subtotal) });
        data.rows.add(new String[] { "Tax (" + String.format("%.1f", bill.taxPercent) + "%)",
                Constants.CURRENCY_SYMBOL + String.format("%.2f", bill.taxAmount) });
        data.rows.add(new String[] { "ESTIMATED BILL", Constants.CURRENCY_SYMBOL + String.format("%.2f", bill.estimatedBill) });
        return data;
    }

    public ReportData buildPredictionReport() {
        PredictionDAO dao = new PredictionDAO();
        List<Prediction> list = dao.getAll();

        ReportData data = new ReportData();
        data.title = "Prediction History Report";
        data.subtitle = "All predictions saved to date (" + list.size() + " total)";
        data.summaryLines.add("Total Predictions Logged: " + list.size());

        data.columns = new String[] { "Date", "Period", "Algorithm", "Predicted (kWh)", "Actual (kWh)", "Accuracy (%)" };
        for (Prediction p : list) {
            Double acc = p.getAccuracyPercent();
            Double actual = p.getActualConsumption();
            data.rows.add(new String[] {
                    p.getPredictionDate(),
                    p.getPredictionPeriod(),
                    p.getAlgorithm(),
                    String.format("%.2f", p.getPredictedConsumption()),
                    actual != null ? String.format("%.2f", actual) : "-",
                    acc != null ? String.format("%.1f", acc) : "-"
            });
        }
        return data;
    }

    public ReportData buildOptimizationReport() {
        OptimizationService service = new OptimizationService();
        OptimizationService.Plan plan = service.buildPlan();

        ReportData data = new ReportData();
        data.title = "Energy Optimization Report";
        data.subtitle = "Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

        data.summaryLines.add("Appliances Analyzed: " + plan.applianceCount);
        data.summaryLines.add(String.format("Baseline Monthly Consumption: %.2f kWh", plan.baselineMonthlyKwh));
        data.summaryLines.add(String.format("Optimized Monthly Consumption: %.2f kWh", plan.optimizedMonthlyKwh));
        data.summaryLines.add(String.format("Potential Monthly Savings: %.2f kWh (%s%.2f, %.1f%%)",
                plan.potentialSavingsKwh, Constants.CURRENCY_SYMBOL, plan.potentialSavingsCost, plan.potentialSavingsPercent));

        data.columns = new String[] {
                "Appliance", "Category", "Action", "Baseline (kWh/mo)",
                "Optimized (kWh/mo)", "Savings (kWh)", "Savings Cost", "Savings (%)", "Included"
        };
        for (OptimizationService.Recommendation r : plan.recommendations) {
            data.rows.add(new String[] {
                    r.applianceName, r.category, r.title,
                    String.format("%.2f", r.baselineMonthlyKwh),
                    String.format("%.2f", r.optimizedMonthlyKwh),
                    String.format("%.2f", r.savingsKwh),
                    Constants.CURRENCY_SYMBOL + String.format("%.2f", r.savingsCost),
                    String.format("%.1f", r.savingsPercent),
                    r.included ? "Yes" : "No"
            });
        }
        return data;
    }


    public void exportToCsv(ReportData data, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            pw.println(escapeCsv(data.title));
            pw.println(escapeCsv(data.subtitle));
            pw.println();

            if (!data.summaryLines.isEmpty()) {
                for (String line : data.summaryLines) {
                    pw.println(escapeCsv(line));
                }
                pw.println();
            }

            pw.println(joinCsv(data.columns));
            for (String[] row : data.rows) {
                pw.println(joinCsv(row));
            }
        }
    }

    private String joinCsv(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(escapeCsv(row[i]));
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public void exportToPdf(ReportData data, File file) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDFont bodyFont = PDType1Font.HELVETICA;
            PDFont boldFont = PDType1Font.HELVETICA_BOLD;

            PdfCursor cursor = new PdfCursor(doc);

            cursor.writeLine(boldFont, 16, sanitize(data.title));
            cursor.gap(6);
            cursor.writeLine(bodyFont, 10, sanitize(data.subtitle));
            cursor.gap(10);

            if (!data.summaryLines.isEmpty()) {
                for (String line : data.summaryLines) {
                    cursor.writeLine(bodyFont, 10, sanitize(line));
                }
                cursor.gap(10);
            }

            float tableWidth = cursor.pageWidth() - 2 * cursor.margin;
            float[] colWidths = new float[data.columns.length];
            for (int i = 0; i < colWidths.length; i++) colWidths[i] = tableWidth / colWidths.length;

            cursor.writeRow(boldFont, 9, data.columns, colWidths);
            cursor.horizontalLine();

            for (String[] row : data.rows) {
                cursor.writeRow(bodyFont, 9, row, colWidths);
            }

            cursor.close();
            doc.save(file);
        }
    }

    private String sanitize(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append((c >= 32 && c < 127) ? c : ' ');
        }
        return sb.toString();
    }

    private class PdfCursor {
        final PDDocument doc;
        final float margin = 50f;
        PDPage page;
        PDPageContentStream cs;
        float y;

        PdfCursor(PDDocument doc) throws IOException {
            this.doc = doc;
            newPage();
        }

        float pageWidth() {
            return page.getMediaBox().getWidth();
        }

        private void newPage() throws IOException {
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = page.getMediaBox().getHeight() - margin;
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < margin) {
                cs.close();
                newPage();
            }
        }

        void gap(float amount) {
            y -= amount;
        }

        void writeLine(PDFont font, int size, String text) throws IOException {
            ensureSpace(size + 4);
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(margin, y);
            cs.showText(text == null ? "" : text);
            cs.endText();
            y -= (size + 4);
        }

        void writeRow(PDFont font, int size, String[] values, float[] colWidths) throws IOException {
            ensureSpace(size + 6);
            float x = margin;
            for (int i = 0; i < values.length; i++) {
                String text = truncate(values[i] == null ? "" : values[i], colWidths[i], font, size);
                cs.beginText();
                cs.setFont(font, size);
                cs.newLineAtOffset(x, y);
                cs.showText(text);
                cs.endText();
                x += colWidths[i];
            }
            y -= (size + 6);
        }

        void horizontalLine() throws IOException {
            ensureSpace(6);
            cs.setLineWidth(0.5f);
            cs.moveTo(margin, y);
            cs.lineTo(pageWidth() - margin, y);
            cs.stroke();
            y -= 8;
        }

        private String truncate(String text, float maxWidth, PDFont font, int size) throws IOException {
            if (font.getStringWidth(text) / 1000f * size <= maxWidth) return text;
            String result = text;
            while (result.length() > 1 && font.getStringWidth(result + "...") / 1000f * size > maxWidth) {
                result = result.substring(0, result.length() - 1);
            }
            return result + "...";
        }

        void close() throws IOException {
            cs.close();
        }
    }
}
