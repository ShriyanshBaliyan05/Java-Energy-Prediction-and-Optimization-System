package com.energy.ui;

import com.energy.service.ReportService;
import com.energy.utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ReportsPanel extends JPanel {

    private static final Color CARD_BG = new Color(30, 34, 45);
    private static final Color BORDER_COLOR = new Color(55, 60, 72);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 12);

    private final ReportService reportService = new ReportService();

    private JComboBox<String> reportTypeCombo;
    private JSpinner fromSpinner;
    private JSpinner toSpinner;
    private JButton generateButton;
    private JButton exportCsvButton;
    private JButton exportPdfButton;
    private JLabel statusLabel;
    private JTextArea summaryArea;
    private JTable previewTable;
    private DefaultTableModel tableModel;

    private ReportService.ReportData currentData;

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Constants.COLOR_BG_DARK);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Reports");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        reportTypeCombo = new JComboBox<>(new String[] {
                "Consumption Summary",
                "Category Breakdown",
                "Bill Estimate",
                "Prediction History",
                "Optimization Plan"
        });
        reportTypeCombo.addActionListener(e -> updateDateControlsEnabled());

        Calendar cal = Calendar.getInstance();
        Date to = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        Date from = cal.getTime();

        fromSpinner = new JSpinner(new SpinnerDateModel(from, null, null, Calendar.DAY_OF_MONTH));
        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));
        toSpinner = new JSpinner(new SpinnerDateModel(to, null, null, Calendar.DAY_OF_MONTH));
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "yyyy-MM-dd"));

        generateButton = new JButton("Generate Preview");
        styleButton(generateButton, Constants.COLOR_ACCENT);
        generateButton.addActionListener(e -> generateReport());

        controls.add(new JLabel("Report:"));
        controls.add(reportTypeCombo);
        controls.add(new JLabel("From:"));
        controls.add(fromSpinner);
        controls.add(new JLabel("To:"));
        controls.add(toSpinner);
        controls.add(generateButton);

        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        summaryArea = new JTextArea(6, 0);
        summaryArea.setEditable(false);
        summaryArea.setFont(MONO_FONT);
        summaryArea.setForeground(Color.WHITE);
        summaryArea.setBackground(CARD_BG);
        summaryArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setText("Select a report type and date range, then click \"Generate Preview\".");
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        summaryScroll.setPreferredSize(new Dimension(100, 110));

        tableModel = new DefaultTableModel();
        previewTable = new JTable(tableModel);
        previewTable.setFont(BODY_FONT);
        previewTable.setRowHeight(24);
        previewTable.setBackground(CARD_BG);
        previewTable.setForeground(Color.WHITE);
        previewTable.setGridColor(BORDER_COLOR);
        previewTable.getTableHeader().setFont(Constants.FONT_LABEL_BOLD);
        JScrollPane tableScroll = new JScrollPane(previewTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        center.add(summaryScroll, BorderLayout.NORTH);
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(buildFooter(), BorderLayout.SOUTH);
        return center;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(BODY_FONT);
        footer.add(statusLabel, BorderLayout.WEST);

        JPanel exportButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        exportButtons.setOpaque(false);

        exportCsvButton = new JButton("Export to CSV");
        styleButton(exportCsvButton, Constants.COLOR_ACCENT_GREEN);
        exportCsvButton.setEnabled(false);
        exportCsvButton.addActionListener(e -> exportReport(true));

        exportPdfButton = new JButton("Export to PDF");
        styleButton(exportPdfButton, Constants.COLOR_ACCENT_ORANGE);
        exportPdfButton.setEnabled(false);
        exportPdfButton.addActionListener(e -> exportReport(false));

        exportButtons.add(exportCsvButton);
        exportButtons.add(exportPdfButton);
        footer.add(exportButtons, BorderLayout.EAST);
        return footer;
    }

    private void styleButton(JButton button, Color bg) {
        button.setFont(Constants.FONT_LABEL_BOLD);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void updateDateControlsEnabled() {
        String type = (String) reportTypeCombo.getSelectedItem();
        boolean needsDates = !"Prediction History".equals(type) && !"Optimization Plan".equals(type);
        fromSpinner.setEnabled(needsDates);
        toSpinner.setEnabled(needsDates);
    }

    private void generateReport() {
        String type = (String) reportTypeCombo.getSelectedItem();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        String from = fmt.format((Date) fromSpinner.getValue());
        String to = fmt.format((Date) toSpinner.getValue());

        generateButton.setEnabled(false);
        setStatus("Generating report...", false);

        SwingWorker<ReportService.ReportData, Void> worker = new SwingWorker<>() {
            @Override
            protected ReportService.ReportData doInBackground() {
                switch (type) {
                    case "Consumption Summary":
                        return reportService.buildConsumptionReport(from, to);
                    case "Category Breakdown":
                        return reportService.buildCategoryBreakdownReport(from, to);
                    case "Bill Estimate":
                        return reportService.buildBillReport(from, to);
                    case "Prediction History":
                        return reportService.buildPredictionReport();
                    case "Optimization Plan":
                        return reportService.buildOptimizationReport();
                    default:
                        return reportService.buildConsumptionReport(from, to);
                }
            }

            @Override
            protected void done() {
                generateButton.setEnabled(true);
                try {
                    currentData = get();
                    displayReport(currentData);
                    exportCsvButton.setEnabled(true);
                    exportPdfButton.setEnabled(true);
                    setStatus("Report generated successfully.", false);
                } catch (Exception ex) {
                    setStatus("Failed to generate report: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    private void displayReport(ReportService.ReportData data) {
        StringBuilder sb = new StringBuilder();
        sb.append(data.title).append("\n");
        sb.append(data.subtitle).append("\n\n");
        for (String line : data.summaryLines) {
            sb.append(line).append("\n");
        }
        summaryArea.setText(sb.toString());
        summaryArea.setCaretPosition(0);

        tableModel.setDataVector(toObjectRows(data.rows), data.columns);
        if (data.rows.isEmpty()) {
            setStatus("Report generated, but no matching data was found for this selection.", false);
        }
    }

    private Object[][] toObjectRows(java.util.List<String[]> rows) {
        Object[][] result = new Object[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            result[i] = rows.get(i);
        }
        return result;
    }

    private void exportReport(boolean csv) {
        if (currentData == null) return;

        JFileChooser chooser = new JFileChooser();
        String defaultName = currentData.title.replaceAll("[^a-zA-Z0-9]+", "_") + (csv ? ".csv" : ".pdf");
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(csv
                ? new FileNameExtensionFilter("CSV Files", "csv")
                : new FileNameExtensionFilter("PDF Files", "pdf"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        String ext = csv ? ".csv" : ".pdf";
        if (!file.getName().toLowerCase().endsWith(ext)) {
            file = new File(file.getParentFile(), file.getName() + ext);
        }
        final File targetFile = file;

        setStatus("Exporting...", false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (csv) {
                    reportService.exportToCsv(currentData, targetFile);
                } else {
                    reportService.exportToPdf(currentData, targetFile);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    setStatus("Exported successfully to: " + targetFile.getAbsolutePath(), false);
                } catch (Exception ex) {
                    setStatus("Export failed: " + ex.getMessage(), true);
                }
            }
        };
        worker.execute();
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Constants.COLOR_ACCENT_RED : Constants.COLOR_ACCENT_GREEN);
    }
}