package com.energy.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.energy.components.RoundedPanel;
import com.energy.service.OptimizationService;
import com.energy.utils.Constants;

public class OptimizationPanel extends JPanel {

    private final OptimizationService optimizationService = new OptimizationService();

    private JLabel baselineLabel;
    private JLabel optimizedLabel;
    private JLabel savingsKwhLabel;
    private JLabel savingsCostLabel;
    private JLabel savingsPctLabel;
    private JTextArea noteArea;
    private JTextArea detailArea;

    private DefaultTableModel tableModel;
    private JTable table;
    private OptimizationService.Plan currentPlan;

    public OptimizationPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Constants.COLOR_BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Energy Optimization");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.COLOR_TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(buildSummaryRow());
        center.add(Box.createVerticalStrut(16));
        center.add(buildTableSection());
        center.add(Box.createVerticalStrut(16));
        center.add(buildDetailSection());

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        baselineLabel = valueLabel();
        optimizedLabel = valueLabel();
        savingsKwhLabel = valueLabel();
        savingsCostLabel = valueLabel();
        savingsPctLabel = valueLabel();

        row.add(summaryCard("Baseline (monthly)", baselineLabel));
        row.add(summaryCard("Optimized (monthly)", optimizedLabel));
        row.add(summaryCard("Savings (kWh)", savingsKwhLabel));
        row.add(summaryCard("Savings (cost)", savingsCostLabel));
        row.add(summaryCard("Savings %", savingsPctLabel));
        return row;
    }

    private JLabel valueLabel() {
        JLabel l = new JLabel("-");
        l.setFont(Constants.FONT_VALUE.deriveFont(16f));
        l.setForeground(Constants.COLOR_ACCENT_GREEN);
        return l;
    }

    private RoundedPanel summaryCard(String title, JLabel value) {
        RoundedPanel p = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel t = new JLabel(title);
        t.setFont(Constants.FONT_SMALL);
        t.setForeground(Constants.COLOR_TEXT_SECONDARY);
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildTableSection() {
        RoundedPanel panel = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel heading = new JLabel("Recommendations (tick to include in totals)");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        header.add(heading, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Recalculate");
        refreshBtn.addActionListener(e -> refreshData());
        header.add(refreshBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Include", "Appliance", "Action", "Baseline kWh", "Optimized kWh", "Saved kWh", "Saved cost", "Saved %"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(Constants.FONT_LABEL);
        table.getTableHeader().setFont(Constants.FONT_LABEL_BOLD);
        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 0 && currentPlan != null) {
                syncInclusionFromTable();
                optimizationService.applySelectionTotals(currentPlan, optimizationService.getTariff());
                renderSummary(currentPlan);
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelectedDetail();
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(100, 240));
        panel.add(tableScroll, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildDetailSection() {
        RoundedPanel panel = new RoundedPanel(16, Constants.COLOR_CARD_BG);
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(100, 180));

        JLabel heading = new JLabel("Why this recommendation (formula)");
        heading.setFont(Constants.FONT_HEADING);
        heading.setForeground(Constants.COLOR_TEXT_PRIMARY);
        panel.add(heading, BorderLayout.NORTH);

        detailArea = new JTextArea("Select a row to see the exact formula and assumptions.");
        detailArea.setEditable(false);
        detailArea.setOpaque(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(Constants.FONT_LABEL);
        detailArea.setForeground(Constants.COLOR_TEXT_PRIMARY);

        noteArea = new JTextArea();
        noteArea.setEditable(false);
        noteArea.setOpaque(false);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(Constants.FONT_SMALL);
        noteArea.setForeground(Constants.COLOR_TEXT_SECONDARY);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(detailArea);
        texts.add(Box.createVerticalStrut(8));
        texts.add(noteArea);
        panel.add(texts, BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        SwingWorker<OptimizationService.Plan, Void> worker = new SwingWorker<>() {
            @Override
            protected OptimizationService.Plan doInBackground() {
                return optimizationService.buildPlan();
            }

            @Override
            protected void done() {
                try {
                    currentPlan = get();
                    renderTable(currentPlan);
                    renderSummary(currentPlan);
                    noteArea.setText(currentPlan.note);
                    if (currentPlan.applianceCount == 0) {
                        detailArea.setText("No appliances registered. Add appliances first — recommendations are computed from Power, Quantity, Hours and Star rating, not from random values.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void renderSummary(OptimizationService.Plan plan) {
        baselineLabel.setText(String.format("%.2f kWh", plan.baselineMonthlyKwh));
        optimizedLabel.setText(String.format("%.2f kWh", plan.optimizedMonthlyKwh));
        savingsKwhLabel.setText(String.format("%.2f kWh", plan.potentialSavingsKwh));
        savingsCostLabel.setText(Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", plan.potentialSavingsCost));
        savingsPctLabel.setText(String.format("%.1f%%", plan.potentialSavingsPercent));
    }

    private void renderTable(OptimizationService.Plan plan) {
        tableModel.setRowCount(0);
        for (OptimizationService.Recommendation r : plan.recommendations) {
            tableModel.addRow(new Object[]{
                    r.included,
                    r.applianceName,
                    r.title,
                    String.format("%.2f", r.baselineMonthlyKwh),
                    String.format("%.2f", r.optimizedMonthlyKwh),
                    String.format("%.2f", r.savingsKwh),
                    Constants.CURRENCY_SYMBOL + " " + String.format("%.2f", r.savingsCost),
                    String.format("%.1f%%", r.savingsPercent)
            });
        }
    }

    private void syncInclusionFromTable() {
        if (currentPlan == null) return;
        List<OptimizationService.Recommendation> recs = currentPlan.recommendations;
        for (int i = 0; i < recs.size() && i < tableModel.getRowCount(); i++) {
            Object v = tableModel.getValueAt(i, 0);
            recs.get(i).included = Boolean.TRUE.equals(v);
        }
    }

    private void showSelectedDetail() {
        int row = table.getSelectedRow();
        if (currentPlan == null || row < 0 || row >= currentPlan.recommendations.size()) return;
        OptimizationService.Recommendation r = currentPlan.recommendations.get(row);
        detailArea.setText(
                r.applianceName + "  (" + r.category + ")\n"
                        + r.title + "\n\n"
                        + r.description + "\n\n"
                        + "Formula: " + r.formula + "\n"
                        + String.format("Baseline = %.2f kWh/month    Optimized = %.2f kWh/month    Savings = %.2f kWh (%.1f%%)",
                        r.baselineMonthlyKwh, r.optimizedMonthlyKwh, r.savingsKwh, r.savingsPercent)
        );
    }
}