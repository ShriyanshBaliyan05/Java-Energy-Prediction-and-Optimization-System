package com.energy.ui;

import com.energy.components.RoundedPanel;
import com.energy.components.SimpleBarChart;
import com.energy.database.PredictionDAO;
import com.energy.model.DailyConsumption;
import com.energy.model.Prediction;
import com.energy.service.PredictionService;
import com.energy.utils.Constants;
import com.energy.utils.DateUtils;
import com.energy.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PredictionPanel extends JPanel {

    private final PredictionService predictionService = new PredictionService();
    private final PredictionDAO predictionDAO = new PredictionDAO();

    private JComboBox<String> categoryBox;
    private JComboBox<String> periodBox;
    private RoundedPanel resultContainer;
    private SimpleBarChart chart;
    private JButton saveBtn;

    private DefaultTableModel historyTableModel;
    private JTable historyTable;

    private PredictionService.PredictionResult lastResult;

    public PredictionPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Constants.COLOR_BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        center.add(buildResultSection());
        center.add(Box.createVerticalStrut(20));
        center.add(buildChartSection());
        center.add(Box.createVerticalStrut(20));
        center.add(buildHistorySection());

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadHistory();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Energy Prediction");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        categoryBox = new JComboBox<>(Constants.APPLIANCE_CATEGORIES_WITH_ALL);
        periodBox = new JComboBox<>(new String[]{
                PredictionService.PERIOD_DAILY, PredictionService.PERIOD_WEEKLY, PredictionService.PERIOD_MONTHLY
        });

        JButton generateBtn = new JButton("Generate Prediction");
        generateBtn.setFont(Constants.FONT_LABEL_BOLD);
        generateBtn.addActionListener(e -> generatePrediction());

        controls.add(new JLabel("Category:"));
        controls.add(categoryBox);
        controls.add(new JLabel("Period:"));
        controls.add(periodBox);
        controls.add(generateBtn);

        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private RoundedPanel buildResultSection() {
        resultContainer = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        resultContainer.setLayout(new BoxLayout(resultContainer, BoxLayout.Y_AXIS));
        resultContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        renderEmptyResult();
        return resultContainer;
    }

    private void renderEmptyResult() {
        resultContainer.removeAll();
        JLabel heading = new JLabel("Prediction Result");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultContainer.add(heading);
        resultContainer.add(Box.createVerticalStrut(10));

        JLabel empty = new JLabel("Choose a category and period, then click \"Generate Prediction\".");
        empty.setFont(Constants.FONT_LABEL);
        empty.setForeground(Constants.COLOR_TEXT_SECONDARY);
        empty.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultContainer.add(empty);

        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private JComponent buildChartSection() {
        RoundedPanel panel = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Historical Trend vs Predicted");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        panel.add(heading, BorderLayout.NORTH);

        chart = new SimpleBarChart();
        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    private JComponent buildHistorySection() {
        RoundedPanel panel = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel heading = new JLabel("Saved Predictions");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        headerRow.add(heading, BorderLayout.WEST);

        saveBtn = new JButton("Save Current Prediction to History");
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(e -> saveCurrentPrediction());
        headerRow.add(saveBtn, BorderLayout.EAST);

        panel.add(headerRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Saved On", "Period / Scope", "Predicted (kWh)", "Actual (kWh)", "Accuracy", "Algorithm"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(26);
        historyTable.setFont(Constants.FONT_LABEL);
        historyTable.getTableHeader().setFont(Constants.FONT_LABEL_BOLD);

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setPreferredSize(new Dimension(100, 200));
        panel.add(tableScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton recordActualBtn = new JButton("Record Actual Consumption");
        recordActualBtn.addActionListener(e -> recordActualForSelected());
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelectedPrediction());
        actions.add(recordActualBtn);
        actions.add(deleteBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void generatePrediction() {
        String category = (String) categoryBox.getSelectedItem();
        String period = (String) periodBox.getSelectedItem();

        SwingWorker<PredictionService.PredictionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected PredictionService.PredictionResult doInBackground() {
                return predictionService.predict(category, period);
            }

            @Override
            protected void done() {
                try {
                    PredictionService.PredictionResult result = get();
                    if (result == null) {
                        lastResult = null;
                        saveBtn.setEnabled(false);
                        JOptionPane.showMessageDialog(PredictionPanel.this,
                                "No historical energy records found"
                                        + (category.equals("All Categories") ? "" : " for category \"" + category + "\"")
                                        + ".\nPlease add energy records first (Energy Input screen).",
                                "No Data Available", JOptionPane.WARNING_MESSAGE);
                        renderEmptyResult();
                        chart.setData(new ArrayList<>(), new ArrayList<>(), -1, "");
                        return;
                    }
                    lastResult = result;
                    saveBtn.setEnabled(true);
                    renderResult(result);
                    renderChart(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void renderResult(PredictionService.PredictionResult r) {
        resultContainer.removeAll();

        JLabel heading = new JLabel("Prediction Result");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultContainer.add(heading);
        resultContainer.add(Box.createVerticalStrut(12));

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        statsRow.add(statBox("Predicted Consumption", String.format("%.2f kWh", r.predictedConsumption), Constants.COLOR_ACCENT));
        statsRow.add(statBox("Predicted Cost", Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", r.predictedCost), Constants.COLOR_ACCENT_GREEN));
        statsRow.add(statBox("Data Points Used", String.valueOf(r.dataPointsUsed), Constants.COLOR_ACCENT_ORANGE));

        String accuracyText = r.backtestAccuracyPercent != null
                ? String.format("%.1f%%", r.backtestAccuracyPercent)
                : "N/A (need 3+ days)";
        statsRow.add(statBox("Backtest Accuracy", accuracyText, Constants.COLOR_ACCENT_RED));

        resultContainer.add(statsRow);
        resultContainer.add(Box.createVerticalStrut(15));

        String algoText = "Algorithm Used: " + r.algorithm
                + (r.rSquared != null ? String.format("   (R\u00B2 = %.3f)", r.rSquared) : "");
        JLabel algoLabel = new JLabel(algoText);
        algoLabel.setFont(Constants.FONT_LABEL_BOLD);
        algoLabel.setForeground(Constants.COLOR_TEXT_PRIMARY);
        algoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultContainer.add(algoLabel);
        resultContainer.add(Box.createVerticalStrut(8));

        JTextArea limitations = new JTextArea(r.limitationsNote);
        limitations.setEditable(false);
        limitations.setOpaque(false);
        limitations.setLineWrap(true);
        limitations.setWrapStyleWord(true);
        limitations.setFont(Constants.FONT_LABEL);
        limitations.setForeground(Constants.COLOR_TEXT_SECONDARY);
        limitations.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultContainer.add(limitations);

        resultContainer.revalidate();
        resultContainer.repaint();
    }

    private JPanel statBox(String label, String value, Color accent) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(Constants.FONT_VALUE.deriveFont(18f));
        valueLabel.setForeground(accent);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(Constants.FONT_SMALL);
        titleLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);

        box.add(valueLabel);
        box.add(Box.createVerticalStrut(4));
        box.add(titleLabel);
        return box;
    }

    private void renderChart(PredictionService.PredictionResult r) {
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (DailyConsumption dc : r.series) {
            labels.add(dc.getDate());
            values.add(dc.getTotalKwh());
        }
        labels.add("Predicted");
        values.add(r.predictedDailyKwh);
        chart.setData(labels, values, values.size() - 1, "kWh");
    }

    private void saveCurrentPrediction() {
        if (lastResult == null) return;

        Prediction p = new Prediction();
        p.setPredictionDate(DateUtils.today());
        p.setPredictionPeriod(lastResult.period + " | Category: " + lastResult.category);
        p.setPredictedConsumption(lastResult.predictedConsumption);
        p.setActualConsumption(null);
        p.setAlgorithm(lastResult.algorithm);

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return predictionDAO.insert(p);
            }

            @Override
            protected void done() {
                try {
                    if (get() > 0) {
                        JOptionPane.showMessageDialog(PredictionPanel.this, "Prediction saved to history.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadHistory();
                    } else {
                        JOptionPane.showMessageDialog(PredictionPanel.this, "Failed to save prediction.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void loadHistory() {
        SwingWorker<List<Prediction>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Prediction> doInBackground() {
                return predictionDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    List<Prediction> list = get();
                    historyTableModel.setRowCount(0);
                    for (Prediction p : list) {
                        Double acc = p.getAccuracyPercent();
                        historyTableModel.addRow(new Object[]{
                                p.getId(), p.getCreatedAt(), p.getPredictionPeriod(),
                                String.format("%.2f", p.getPredictedConsumption()),
                                p.getActualConsumption() != null ? String.format("%.2f", p.getActualConsumption()) : "-",
                                acc != null ? String.format("%.1f%%", acc) : "-",
                                p.getAlgorithm()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void refreshData() {
        loadHistory();
    }

    private void recordActualForSelected() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a saved prediction first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) historyTableModel.getValueAt(row, 0);
        String input = JOptionPane.showInputDialog(this, "Enter the actual consumption (kWh) recorded for this period:");
        if (input == null) return;
        input = input.trim();
        if (!ValidationUtils.isValidNonNegativeDouble(input)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid non-negative number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double actual = Double.parseDouble(input);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return predictionDAO.updateActual(id, actual);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadHistory();
                        JOptionPane.showMessageDialog(PredictionPanel.this, "Actual consumption recorded. Accuracy computed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void deleteSelectedPrediction() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a saved prediction to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) historyTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this saved prediction?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return predictionDAO.delete(id);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) loadHistory();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }
}
